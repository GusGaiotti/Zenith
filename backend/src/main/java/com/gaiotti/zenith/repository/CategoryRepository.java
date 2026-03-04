package com.gaiotti.zenith.repository;

import com.gaiotti.zenith.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByLedgerId(Long ledgerId);

    Optional<Category> findByIdAndLedgerId(Long id, Long ledgerId);

    boolean existsByIdAndLedgerId(Long id, Long ledgerId);
}
