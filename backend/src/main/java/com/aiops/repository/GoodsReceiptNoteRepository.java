package com.aiops.repository;

import com.aiops.domain.GoodsReceiptNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoodsReceiptNoteRepository extends JpaRepository<GoodsReceiptNote, String> {
    List<GoodsReceiptNote> findByTenantId(String tenantId);
    Optional<GoodsReceiptNote> findByTenantIdAndId(String tenantId, String id);
    Optional<GoodsReceiptNote> findByTenantIdAndPoNumber(String tenantId, String poNumber);
    Optional<GoodsReceiptNote> findByTenantIdAndGrnNumber(String tenantId, String grnNumber);
}
