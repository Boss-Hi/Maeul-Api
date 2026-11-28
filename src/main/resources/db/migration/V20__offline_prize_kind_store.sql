-- 상품 종류를 한 매장에서만 지급하게 묶는다. NULL 이면 어느 매장에서나 지급할 수 있다.
ALTER TABLE `offline_prize_kinds`
    ADD COLUMN `store_id` BIGINT NULL COMMENT '이 매장에서만 지급 (NULL 이면 모든 매장)' AFTER `name`,
    ADD CONSTRAINT `fk_offline_prize_kinds_store` FOREIGN KEY (`store_id`) REFERENCES `stores` (`id`);

-- 이미 등록된 더온전히 정조효공원점 상품 5종을 그 매장 전용으로 묶는다 (매장이 아직 없으면 아무것도 바뀌지 않는다)
UPDATE `offline_prize_kinds`
SET `store_id` = (SELECT MIN(`id`) FROM `stores` WHERE `name` = '더온전히 정조효공원점' AND `deleted_at` IS NULL)
WHERE `name` IN ('아메리카노 1잔', '물고기 친구 분양', 'Maeul 병아리 스티커', '디저트 1개: 쌀 휘낭시에', '디저트 1개: 쌀 소금빵')
  AND `deleted_at` IS NULL;
