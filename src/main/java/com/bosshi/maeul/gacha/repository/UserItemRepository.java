package com.bosshi.maeul.gacha.repository;

import com.bosshi.maeul.gacha.entity.UserItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserItemRepository extends JpaRepository<UserItem, Long> {

    Optional<UserItem> findByUserIdAndItemId(Long userId, Long itemId);

    List<UserItem> findByUserIdOrderByIdAsc(Long userId);
}
