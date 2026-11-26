package com.bosshi.maeul.gacha.repository;

import com.bosshi.maeul.gacha.entity.GachaTicketRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GachaTicketRuleRepository extends JpaRepository<GachaTicketRule, com.bosshi.maeul.gacha.TicketEventType> {

}
