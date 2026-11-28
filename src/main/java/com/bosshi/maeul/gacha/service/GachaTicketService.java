package com.bosshi.maeul.gacha.service;

import com.bosshi.maeul.gacha.GachaTicketIssuedEvent;
import com.bosshi.maeul.gacha.GachaTicketIssuer;
import com.bosshi.maeul.gacha.TicketEventType;
import com.bosshi.maeul.gacha.dto.TicketView;
import com.bosshi.maeul.gacha.entity.GachaTicket;
import com.bosshi.maeul.gacha.entity.GachaTicketRule;
import com.bosshi.maeul.gacha.entity.OfflineGachaLog;
import com.bosshi.maeul.gacha.entity.OfflinePrizeKind;
import com.bosshi.maeul.gacha.entity.PrizeQr;
import com.bosshi.maeul.gacha.entity.Store;
import com.bosshi.maeul.gacha.repository.GachaTicketRepository;
import com.bosshi.maeul.gacha.repository.GachaTicketRuleRepository;
import com.bosshi.maeul.gacha.repository.OfflineGachaLogRepository;
import com.bosshi.maeul.gacha.repository.OfflinePrizeKindRepository;
import com.bosshi.maeul.gacha.repository.PrizeQrRepository;
import com.bosshi.maeul.gacha.type.OfflineGachaAction;
import com.bosshi.maeul.gacha.type.TicketStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 오프라인 가챠권 (ADR 0016). 이벤트가 일어나면 발급하고, 사장님이 QR 을 스캔하면 사용됨으로 바꾼다.
 * 사장님은 되돌릴 수 없고 운영자만 취소할 수 있다.
 */
@Service
@Transactional(readOnly = true)
public class GachaTicketService implements GachaTicketIssuer {

    private final GachaTicketRepository ticketRepository;
    private final GachaTicketRuleRepository ruleRepository;
    private final PrizeQrRepository prizeQrRepository;
    private final OfflinePrizeKindRepository kindRepository;
    private final OfflineGachaLogRepository logRepository;
    private final StoreService storeService;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    @Autowired
    public GachaTicketService(GachaTicketRepository ticketRepository, GachaTicketRuleRepository ruleRepository,
                              PrizeQrRepository prizeQrRepository, OfflinePrizeKindRepository kindRepository,
                              OfflineGachaLogRepository logRepository, StoreService storeService,
                              ApplicationEventPublisher eventPublisher) {
        this(ticketRepository, ruleRepository, prizeQrRepository, kindRepository, logRepository, storeService, eventPublisher, Clock.systemDefaultZone());
    }

    GachaTicketService(GachaTicketRepository ticketRepository, GachaTicketRuleRepository ruleRepository,
                       PrizeQrRepository prizeQrRepository, OfflinePrizeKindRepository kindRepository,
                       OfflineGachaLogRepository logRepository, StoreService storeService,
                       ApplicationEventPublisher eventPublisher, Clock clock) {
        this.ticketRepository = ticketRepository;
        this.ruleRepository = ruleRepository;
        this.prizeQrRepository = prizeQrRepository;
        this.kindRepository = kindRepository;
        this.logRepository = logRepository;
        this.storeService = storeService;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void issue(Long userId, TicketEventType eventType, String refKey) {
        GachaTicketRule rule = ruleRepository.findById(eventType).orElse(null);
        if (rule == null || !rule.isEnabled() || ticketRepository.existsByUserIdAndEventTypeAndRefKey(userId, eventType, refKey)) {
            return;
        }
        GachaTicket ticket = GachaTicket.builder()
                .userId(userId)
                .code(QrCodes.newCode(QrCodes.TICKET_PREFIX))
                .eventType(eventType)
                .refKey(refKey)
                .expiresAt(LocalDateTime.now(clock).plusDays(rule.getValidityDays()))
                .build();
        ticketRepository.save(ticket);
        eventPublisher.publishEvent(new GachaTicketIssuedEvent(userId, ticket.getId(), eventType, ticket.getExpiresAt()));
    }

    /** 내 가챠권 (최근 순). 상품이 연결된 권에는 상품 이름이 붙는다 */
    public List<TicketView> myTickets(Long userId) {
        List<GachaTicket> tickets = ticketRepository.findByUserIdAndDeletedAtIsNullOrderByIdDesc(userId);
        Map<Long, PrizeQr> qrByTicket = prizeQrRepository.findByTicketIdIn(tickets.stream().map(GachaTicket::getId).toList()).stream()
                .collect(Collectors.toMap(PrizeQr::getTicketId, Function.identity()));
        Map<Long, String> kindNames = kindRepository.findByIdIn(qrByTicket.values().stream().map(PrizeQr::getKindId).distinct().toList()).stream()
                .collect(Collectors.toMap(OfflinePrizeKind::getId, OfflinePrizeKind::getName));
        LocalDateTime now = LocalDateTime.now(clock);
        return tickets.stream().map(ticket -> {
            PrizeQr qr = qrByTicket.get(ticket.getId());
            return new TicketView(ticket, statusOf(ticket, qr != null, now), qr == null ? null : kindNames.get(qr.getKindId()));
        }).toList();
    }

    /** 본인의 가챠권만 (QR 이미지를 만들 때) */
    public GachaTicket ownTicket(Long userId, Long ticketId) {
        return ticketRepository.findById(ticketId)
                .filter(ticket -> ticket.getUserId().equals(userId) && !ticket.isDeleted())
                .orElseThrow(() -> new IllegalArgumentException("가챠권을 찾을 수 없습니다: " + ticketId));
    }

    /** 사장님이 가챠권 QR 을 스캔한다. 한 번만 성공하고 사용 이력이 남는다 */
    @Transactional
    public GachaTicket use(Long ownerUserId, String code) {
        Store store = storeService.requireStoreOf(ownerUserId);
        if (!QrCodes.hasPrefix(code, QrCodes.TICKET_PREFIX)) {
            throw new IllegalArgumentException("가챠권 QR 이 아닙니다.");
        }
        GachaTicket ticket = ticketRepository.findByCodeAndDeletedAtIsNull(code)
                .orElseThrow(() -> new IllegalArgumentException("가챠권을 찾을 수 없습니다."));
        LocalDateTime now = LocalDateTime.now(clock);
        if (ticket.getUsedAt() != null) {
            throw new IllegalArgumentException("이미 사용된 가챠권입니다.");
        }
        if (!ticket.getExpiresAt().isAfter(now)) {
            throw new IllegalArgumentException("유효기간이 지난 가챠권입니다.");
        }
        if (ticketRepository.markUsed(ticket.getId(), store.getId(), ownerUserId, now) == 0) {
            throw new IllegalArgumentException("이미 사용된 가챠권입니다.");
        }
        logRepository.save(OfflineGachaLog.builder().action(OfflineGachaAction.TICKET_USED)
                .ticketId(ticket.getId()).storeId(store.getId()).actorUserId(ownerUserId).build());
        return ticketRepository.findById(ticket.getId()).orElseThrow();
    }

    /** 운영자가 사용을 취소한다. 취소된 권은 다시 쓸 수 있다. 상품이 연결된 권은 상품 지급을 먼저 취소해야 한다 */
    @Transactional
    public void cancelUse(Long ticketId, Long adminUserId, String reason) {
        requireReason(reason);
        GachaTicket ticket = ticketRepository.findById(ticketId)
                .filter(t -> !t.isDeleted())
                .orElseThrow(() -> new IllegalArgumentException("가챠권을 찾을 수 없습니다: " + ticketId));
        if (ticket.getUsedAt() == null) {
            throw new IllegalArgumentException("사용되지 않은 가챠권입니다.");
        }
        if (prizeQrRepository.existsByTicketId(ticketId)) {
            throw new IllegalArgumentException("상품이 연결된 가챠권입니다. 상품 QR 지급을 먼저 취소하세요.");
        }
        Long storeId = ticket.getUsedStoreId();
        if (ticketRepository.clearUse(ticketId) == 0) {
            throw new IllegalArgumentException("사용되지 않은 가챠권입니다.");
        }
        logRepository.save(OfflineGachaLog.builder().action(OfflineGachaAction.TICKET_USE_CANCELLED)
                .ticketId(ticketId).storeId(storeId).actorUserId(adminUserId).reason(reason).build());
    }

    public GachaTicketRule rule(TicketEventType eventType) {
        return ruleRepository.findById(eventType)
                .orElseThrow(() -> new IllegalArgumentException("가챠권 발급 설정이 없습니다: " + eventType));
    }

    /** 바꾼 값은 그 뒤 발급분부터 적용된다 */
    @Transactional
    public GachaTicketRule updateRule(TicketEventType eventType, boolean enabled, int validityDays) {
        if (validityDays < 1) {
            throw new IllegalArgumentException("유효기간은 1일 이상이어야 합니다.");
        }
        GachaTicketRule rule = ruleRepository.findById(eventType).orElseGet(() -> GachaTicketRule.builder().eventType(eventType).build());
        rule.setEnabled(enabled);
        rule.setValidityDays(validityDays);
        return ruleRepository.save(rule);
    }

    static TicketStatus statusOf(GachaTicket ticket, boolean prizeLinked, LocalDateTime now) {
        if (prizeLinked) {
            return TicketStatus.PRIZE_RECEIVED;
        }
        if (ticket.getUsedAt() != null) {
            return TicketStatus.USED;
        }
        return ticket.getExpiresAt().isAfter(now) ? TicketStatus.USABLE : TicketStatus.EXPIRED;
    }

    static void requireReason(String reason) {
        if (!StringUtils.hasText(reason)) {
            throw new IllegalArgumentException("취소 사유는 필수입니다.");
        }
    }
}
