package com.bosshi.maeul.gacha.service;

import com.bosshi.maeul.gacha.dto.PrizeKindStock;
import com.bosshi.maeul.gacha.entity.GachaTicket;
import com.bosshi.maeul.gacha.entity.OfflineGachaLog;
import com.bosshi.maeul.gacha.entity.OfflinePrizeKind;
import com.bosshi.maeul.gacha.entity.PrizeQr;
import com.bosshi.maeul.gacha.entity.Store;
import com.bosshi.maeul.gacha.repository.GachaTicketRepository;
import com.bosshi.maeul.gacha.repository.OfflineGachaLogRepository;
import com.bosshi.maeul.gacha.repository.OfflinePrizeKindRepository;
import com.bosshi.maeul.gacha.repository.PrizeQrRepository;
import com.bosshi.maeul.gacha.type.OfflineGachaAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 오프라인 상품 QR (ADR 0016). 운영자가 미리 발급하고, 사장님이 사용된 가챠권과 함께 스캔하면 지급 완료가 된다.
 * 재고는 발급한 상품 QR 개수이며 서버 가챠의 상품(Prize)과 별개다.
 */
@Service
@Transactional(readOnly = true)
public class PrizeQrService {

    static final int MAX_ISSUE_PER_REQUEST = 1000;

    private final PrizeQrRepository prizeQrRepository;
    private final OfflinePrizeKindRepository kindRepository;
    private final GachaTicketRepository ticketRepository;
    private final OfflineGachaLogRepository logRepository;
    private final StoreService storeService;
    private final Clock clock;

    @Autowired
    public PrizeQrService(PrizeQrRepository prizeQrRepository, OfflinePrizeKindRepository kindRepository,
                          GachaTicketRepository ticketRepository, OfflineGachaLogRepository logRepository,
                          StoreService storeService) {
        this(prizeQrRepository, kindRepository, ticketRepository, logRepository, storeService, Clock.systemDefaultZone());
    }

    PrizeQrService(PrizeQrRepository prizeQrRepository, OfflinePrizeKindRepository kindRepository,
                   GachaTicketRepository ticketRepository, OfflineGachaLogRepository logRepository,
                   StoreService storeService, Clock clock) {
        this.prizeQrRepository = prizeQrRepository;
        this.kindRepository = kindRepository;
        this.ticketRepository = ticketRepository;
        this.logRepository = logRepository;
        this.storeService = storeService;
        this.clock = clock;
    }

    @Transactional
    public OfflinePrizeKind createKind(String name) {
        return kindRepository.save(OfflinePrizeKind.builder().name(name).build());
    }

    public List<PrizeKindStock> kinds() {
        return kindRepository.findByDeletedAtIsNullOrderByIdAsc().stream()
                .map(kind -> new PrizeKindStock(kind,
                        prizeQrRepository.countByKindIdAndDeletedAtIsNull(kind.getId()),
                        prizeQrRepository.countByKindIdAndDeliveredAtIsNotNullAndDeletedAtIsNull(kind.getId())))
                .toList();
    }

    /** 상품 QR 을 count 개 한꺼번에 발급한다. 코드는 서버가 만든 고유 값이다 */
    @Transactional
    public List<PrizeQr> issue(Long kindId, int count) {
        if (count < 1 || count > MAX_ISSUE_PER_REQUEST) {
            throw new IllegalArgumentException("한 번에 발급할 수 있는 개수는 1~" + MAX_ISSUE_PER_REQUEST + " 입니다.");
        }
        if (!kindRepository.existsById(kindId)) {
            throw new IllegalArgumentException("상품 종류를 찾을 수 없습니다: " + kindId);
        }
        List<PrizeQr> qrs = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            qrs.add(PrizeQr.builder().kindId(kindId).code(QrCodes.newCode(QrCodes.PRIZE_PREFIX)).build());
        }
        return prizeQrRepository.saveAll(qrs);
    }

    public Page<PrizeQr> qrs(Long kindId, Pageable pageable) {
        return prizeQrRepository.findByKindIdAndDeletedAtIsNullOrderByIdAsc(kindId, pageable);
    }

    public List<PrizeQr> allQrs(Long kindId) {
        return prizeQrRepository.findByKindIdAndDeletedAtIsNullOrderByIdAsc(kindId);
    }

    public PrizeQr qr(Long prizeQrId) {
        return prizeQrRepository.findById(prizeQrId)
                .filter(qr -> !qr.isDeleted())
                .orElseThrow(() -> new IllegalArgumentException("상품 QR 을 찾을 수 없습니다: " + prizeQrId));
    }

    /**
     * 사장님이 사용된 가챠권과 상품 QR 을 함께 스캔해 연결하고 지급 완료로 바꾼다.
     * 가챠권은 이 매장에서 사용됐고 아직 상품이 연결되지 않았어야 하며, 상품 QR 은 한 번만 쓸 수 있다.
     */
    @Transactional
    public PrizeQr deliver(Long ownerUserId, Long ticketId, String code) {
        Store store = storeService.requireStoreOf(ownerUserId);
        if (!QrCodes.hasPrefix(code, QrCodes.PRIZE_PREFIX)) {
            throw new IllegalArgumentException("상품 QR 이 아닙니다.");
        }
        GachaTicket ticket = ticketRepository.findById(ticketId)
                .filter(t -> !t.isDeleted())
                .orElseThrow(() -> new IllegalArgumentException("가챠권을 찾을 수 없습니다: " + ticketId));
        if (!store.getId().equals(ticket.getUsedStoreId())) {
            throw new IllegalArgumentException("이 매장에서 사용된 가챠권이 아닙니다.");
        }
        if (prizeQrRepository.existsByTicketId(ticketId)) {
            throw new IllegalArgumentException("이미 상품이 연결된 가챠권입니다.");
        }
        PrizeQr qr = prizeQrRepository.findByCodeAndDeletedAtIsNull(code)
                .orElseThrow(() -> new IllegalArgumentException("상품 QR 을 찾을 수 없습니다."));
        if (prizeQrRepository.markDelivered(qr.getId(), store.getId(), ownerUserId, ticketId, LocalDateTime.now(clock)) == 0) {
            throw new IllegalArgumentException("이미 지급된 상품 QR 입니다.");
        }
        logRepository.save(OfflineGachaLog.builder().action(OfflineGachaAction.PRIZE_DELIVERED)
                .ticketId(ticketId).prizeQrId(qr.getId()).storeId(store.getId()).actorUserId(ownerUserId).build());
        return prizeQrRepository.findById(qr.getId()).orElseThrow();
    }

    /** 운영자가 지급을 취소한다. 그 상품 QR 은 다시 쓸 수 있고 가챠권과의 연결이 끊어진다 */
    @Transactional
    public void cancelDelivery(Long prizeQrId, Long adminUserId, String reason) {
        GachaTicketService.requireReason(reason);
        PrizeQr qr = qr(prizeQrId);
        if (qr.getDeliveredAt() == null) {
            throw new IllegalArgumentException("지급되지 않은 상품 QR 입니다.");
        }
        Long ticketId = qr.getTicketId();
        Long storeId = qr.getDeliveredStoreId();
        if (prizeQrRepository.clearDelivery(prizeQrId) == 0) {
            throw new IllegalArgumentException("지급되지 않은 상품 QR 입니다.");
        }
        logRepository.save(OfflineGachaLog.builder().action(OfflineGachaAction.PRIZE_DELIVERY_CANCELLED)
                .ticketId(ticketId).prizeQrId(prizeQrId).storeId(storeId).actorUserId(adminUserId).reason(reason).build());
    }
}
