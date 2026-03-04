package com.gaiotti.zenith.repository;

import com.gaiotti.zenith.model.LedgerMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LedgerMemberRepository extends JpaRepository<LedgerMember, Long> {
    boolean existsByLedgerIdAndUserId(Long ledgerId, Long userId);
    boolean existsByUserId(Long userId);
    Optional<LedgerMember> findFirstByUserId(Long userId);
    long countByLedgerId(Long ledgerId);
    List<LedgerMember> findByLedgerId(Long ledgerId);
}
