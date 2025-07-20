package com.InvestIA.repository;

import com.InvestIA.entity.Investimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InvestimentoRepository extends JpaRepository<Investimento, UUID> {
    List<Investimento> findByUsuarioIdAndAtivoStatusTrue(UUID usuarioId);
    List<Investimento> findByUsuarioId(UUID usuarioId);
}