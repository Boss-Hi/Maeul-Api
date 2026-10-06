package com.bosshi.maeul.auth.repository;

import com.bosshi.maeul.auth.entity.AuthType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthTypeRepository extends JpaRepository<AuthType, String> {
}
