package com.InvestIA.repository;

import com.InvestIA.entity.Meta;
import com.InvestIA.enums.StatusMeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MetaRepository extends JpaRepository<Meta, UUID> {
    List<Meta> findByUsuarioId(UUID usuarioId);
    List<Meta> findByUsuarioIdAndStatus(UUID usuarioId, StatusMeta status);
}