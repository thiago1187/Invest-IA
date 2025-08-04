package com.InvestIA.repository;

import com.InvestIA.entity.Ativo;
import com.InvestIA.enums.TipoAtivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AtivoRepository extends JpaRepository<Ativo, UUID> {
    Optional<Ativo> findByTicker(String ticker);
    Optional<Ativo> findBySimbolo(String simbolo);
    List<Ativo> findByTipoAtivo(TipoAtivo tipoAtivo);
    List<Ativo> findByStatusTrue();
    boolean existsByTicker(String ticker);
}