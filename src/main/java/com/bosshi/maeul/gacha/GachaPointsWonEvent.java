package com.bosshi.maeul.gacha;

/** 가챠에서 포인트가 나왔을 때 보낸다. 포인트 모듈이 같은 트랜잭션에서 적립한다 */
public record GachaPointsWonEvent(Long userId, Long gachaResultId, long amount) {
}
