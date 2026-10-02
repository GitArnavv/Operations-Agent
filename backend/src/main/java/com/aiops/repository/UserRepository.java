package com.aiops.repository;

import com.aiops.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    List<User> findByTenantId(String tenantId);
    Optional<User> findByTenantIdAndId(String tenantId, String id);
}
