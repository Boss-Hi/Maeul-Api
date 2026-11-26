package com.bosshi.maeul.gacha.service;

import com.bosshi.maeul.gacha.*;
import com.bosshi.maeul.gacha.dto.GachaOdds;
import com.bosshi.maeul.gacha.dto.OwnedItem;
import com.bosshi.maeul.gacha.dto.Voucher;
import com.bosshi.maeul.gacha.entity.*;
import com.bosshi.maeul.gacha.repository.*;
import com.bosshi.maeul.gacha.type.VoucherStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;

/**
 * 가챠 (ADR 0014). 일이 일어난 순간 뽑아 결과를 남기고, 상품(교환권), 아이템, 포인트를 지급한다.
 * 재고가 없는 상품은 빼고 남은 결과끼리 뽑는다.
 */
@Service
@Transactional(readOnly = true)
public class GachaService implements GachaDrawer {

    private final GachaOutcomeRepository outcomeRepository;
    private final GachaResultRepository resultRepository;
    private final PrizeRepository prizeRepository;
    private final ItemRepository itemRepository;
    private final PrizeVoucherRepository voucherRepository;
    private final UserItemRepository userItemRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final RandomGenerator random;

    @Autowired
    public GachaService(GachaOutcomeRepository outcomeRepository, GachaResultRepository resultRepository,
                        PrizeRepository prizeRepository, ItemRepository itemRepository,
                        PrizeVoucherRepository voucherRepository, UserItemRepository userItemRepository,
                        ApplicationEventPublisher eventPublisher) {
        this(outcomeRepository, resultRepository, prizeRepository, itemRepository, voucherRepository, userItemRepository,
                eventPublisher, new SecureRandom());
    }

    GachaService(GachaOutcomeRepository outcomeRepository, GachaResultRepository resultRepository,
                 PrizeRepository prizeRepository, ItemRepository itemRepository,
                 PrizeVoucherRepository voucherRepository, UserItemRepository userItemRepository,
                 ApplicationEventPublisher eventPublisher, RandomGenerator random) {
        this.outcomeRepository = outcomeRepository;
        this.resultRepository = resultRepository;
        this.prizeRepository = prizeRepository;
        this.itemRepository = itemRepository;
        this.voucherRepository = voucherRepository;
        this.userItemRepository = userItemRepository;
        this.eventPublisher = eventPublisher;
        this.random = random;
    }

    @Override
    @Transactional
    public GachaDraw draw(Long userId, GachaType gachaType, String refKey) {
        Optional<GachaResult> existing = resultRepository.findByUserIdAndGachaTypeAndRefKey(userId, gachaType, refKey);
        if (existing.isPresent()) {
            return toDraw(existing.get(), voucherIds(List.of(existing.get())));
        }

        List<GachaOutcome> candidates = new ArrayList<>(available(gachaType));
        GachaOutcome picked = null;
        while (picked == null) {
            GachaOutcome outcome = GachaPicker.pick(candidates, random)
                    .orElseThrow(() -> new IllegalStateException(gachaType + " 가챠에 뽑을 수 있는 결과가 없습니다."));
            if (outcome.getRewardType() == RewardType.PRIZE && prizeRepository.takeOne(outcome.getPrizeId()) == 0) {
                candidates.remove(outcome);
                continue;
            }
            picked = outcome;
        }

        GachaResult result = resultRepository.saveAndFlush(GachaResult.builder()
                .userId(userId)
                .gachaType(gachaType)
                .refKey(refKey)
                .outcomeId(picked.getId())
                .rewardType(picked.getRewardType())
                .label(picked.getLabel())
                .amount(picked.getAmount())
                .prizeId(picked.getPrizeId())
                .itemId(picked.getItemId())
                .build());
        Long voucherId = grant(result);
        return toDraw(result, voucherId == null ? Map.of() : Map.of(result.getId(), voucherId));
    }

    /** 사용자의 가챠 결과 (최근 순) */
    public Page<GachaDraw> results(Long userId, Pageable pageable) {
        Page<GachaResult> page = resultRepository.findByUserIdOrderByIdDesc(userId, pageable);
        Map<Long, Long> vouchers = voucherIds(page.getContent());
        return page.map(result -> toDraw(result, vouchers));
    }

    /** 아직 확인하지 않은 가챠 결과 (뽑은 순) */
    public List<GachaDraw> unseen(Long userId) {
        List<GachaResult> results = resultRepository.findByUserIdAndSeenAtIsNullOrderByIdAsc(userId);
        Map<Long, Long> vouchers = voucherIds(results);
        return results.stream().map(result -> toDraw(result, vouchers)).toList();
    }

    /** 결과를 확인함으로 표시한다. 이미 확인했으면 그대로 둔다 */
    @Transactional
    public GachaDraw markSeen(Long userId, Long resultId) {
        GachaResult result = resultRepository.findByIdAndUserId(resultId, userId)
                .orElseThrow(() -> new IllegalArgumentException("가챠 결과를 찾을 수 없습니다. ID: " + resultId));
        if (result.getSeenAt() == null) {
            result.setSeenAt(LocalDateTime.now());
        }
        return toDraw(result, voucherIds(List.of(result)));
    }

    /** 가챠의 지금 확률 (사용자에게 공개) */
    public GachaOdds odds(GachaType gachaType) {
        List<GachaOutcome> candidates = available(gachaType);
        long total = candidates.stream().mapToLong(GachaPicker::weight).sum();
        return new GachaOdds(gachaType, candidates.stream()
                .map(outcome -> new GachaOdds.Entry(outcome.getLabel(), outcome.getRewardType(), outcome.getAmount(),
                        (double) GachaPicker.weight(outcome) / total))
                .toList());
    }

    public List<OwnedItem> items(Long userId) {
        List<UserItem> owned = userItemRepository.findByUserIdOrderByIdAsc(userId);
        Map<Long, Item> items = itemRepository.findAllById(owned.stream().map(UserItem::getItemId).toList()).stream()
                .collect(Collectors.toMap(Item::getId, Function.identity()));
        return owned.stream()
                .map(userItem -> {
                    Item item = items.get(userItem.getItemId());
                    return new OwnedItem(userItem.getItemId(), item == null ? null : item.getName(),
                            item == null ? null : item.getImageUrl(), userItem.getQuantity());
                })
                .toList();
    }

    public List<Voucher> vouchers(Long userId) {
        List<PrizeVoucher> vouchers = voucherRepository.findByUserIdOrderByIdDesc(userId);
        Map<Long, String> names = prizeRepository.findAllById(vouchers.stream().map(PrizeVoucher::getPrizeId).toList()).stream()
                .collect(Collectors.toMap(Prize::getId, Prize::getName));
        return vouchers.stream()
                .map(voucher -> new Voucher(voucher.getId(), voucher.getPrizeId(), names.get(voucher.getPrizeId()),
                        voucher.getStatus(), voucher.getCreatedAt(), voucher.getFulfilledAt()))
                .toList();
    }

    /** 가중치가 있고, 상품이면 재고가 남은 결과 */
    private List<GachaOutcome> available(GachaType gachaType) {
        List<GachaOutcome> outcomes = outcomeRepository.findByGachaTypeAndDeletedAtIsNullOrderByIdAsc(gachaType).stream()
                .filter(outcome -> GachaPicker.weight(outcome) > 0)
                .toList();
        Set<Long> prizeIds = outcomes.stream()
                .filter(outcome -> outcome.getRewardType() == RewardType.PRIZE)
                .map(GachaOutcome::getPrizeId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> inStock = prizeIds.isEmpty() ? Set.of() : prizeRepository.findByIdInAndDeletedAtIsNull(prizeIds).stream()
                .filter(prize -> prize.getStock() != null && prize.getStock() > 0)
                .map(Prize::getId)
                .collect(Collectors.toSet());
        return outcomes.stream()
                .filter(outcome -> outcome.getRewardType() != RewardType.PRIZE || inStock.contains(outcome.getPrizeId()))
                .toList();
    }

    /** 결과의 보상을 지급한다. 상품이면 발급한 교환권 ID 를 돌려준다. 펫 경험치는 가챠를 부른 쪽이 기록한다 */
    private Long grant(GachaResult result) {
        switch (result.getRewardType()) {
            case PRIZE -> {
                return voucherRepository.save(PrizeVoucher.builder()
                        .userId(result.getUserId())
                        .prizeId(result.getPrizeId())
                        .gachaResultId(result.getId())
                        .status(VoucherStatus.ISSUED)
                        .build()).getId();
            }
            case ITEM -> {
                UserItem owned = userItemRepository.findByUserIdAndItemId(result.getUserId(), result.getItemId())
                        .orElseGet(() -> UserItem.builder().userId(result.getUserId()).itemId(result.getItemId()).quantity(0).build());
                owned.setQuantity(owned.getQuantity() + 1);
                userItemRepository.save(owned);
            }
            case POINT -> eventPublisher.publishEvent(new GachaPointsWonEvent(result.getUserId(), result.getId(), result.getAmount()));
            case FAT_EXP, NONE -> {
            }
        }
        return null;
    }

    private Map<Long, Long> voucherIds(List<GachaResult> results) {
        List<Long> prizeResultIds = results.stream()
                .filter(result -> result.getRewardType() == RewardType.PRIZE)
                .map(GachaResult::getId)
                .toList();
        if (prizeResultIds.isEmpty()) {
            return Map.of();
        }
        return voucherRepository.findByGachaResultIdIn(prizeResultIds).stream()
                .collect(Collectors.toMap(PrizeVoucher::getGachaResultId, PrizeVoucher::getId));
    }

    private static GachaDraw toDraw(GachaResult result, Map<Long, Long> voucherIds) {
        return new GachaDraw(result.getId(), result.getGachaType(), result.getRewardType(), result.getLabel(), result.getAmount(),
                result.getPrizeId(), result.getItemId(), voucherIds.get(result.getId()), result.getCreatedAt(), result.getSeenAt());
    }
}
