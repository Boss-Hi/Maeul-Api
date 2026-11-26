package com.bosshi.maeul.gacha.repository;

import com.bosshi.maeul.gacha.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
}
