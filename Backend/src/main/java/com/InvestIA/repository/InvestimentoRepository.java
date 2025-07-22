package com.InvestIA.repository;

import com.InvestIA.entity.Investimento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvestimentoRepository extends JpaRepository<Investimento, UUID> {
    List<Investimento> findByUsuarioIdAndAtivoStatusTrue(UUID usuarioId);
    List<Investimento> findByUsuarioId(UUID usuarioId);
    Page<Investimento> findByUsuarioIdOrderByCriadoEmDesc(UUID usuarioId, Pageable pageable);
    Optional<Investimento> findByIdAndUsuarioId(UUID id, UUID usuarioId);
}