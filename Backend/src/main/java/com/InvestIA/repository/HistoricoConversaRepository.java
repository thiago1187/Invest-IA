package com.InvestIA.repository;

import com.InvestIA.entity.HistoricoConversa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface HistoricoConversaRepository extends JpaRepository<HistoricoConversa, UUID> {
    
    Page<HistoricoConversa> findByUsuarioIdOrderByCriadoEmDesc(UUID usuarioId, Pageable pageable);
    
    List<HistoricoConversa> findTop10ByUsuarioIdOrderByCriadoEmDesc(UUID usuarioId);
    
    @Query("SELECT h FROM HistoricoConversa h WHERE h.usuario.id = :usuarioId " +
           "AND h.criadoEm >= :dataInicio ORDER BY h.criadoEm DESC")
    List<HistoricoConversa> findRecentesByUsuario(@Param("usuarioId") UUID usuarioId, 
                                                  @Param("dataInicio") LocalDateTime dataInicio);
    
    @Query("SELECT h FROM HistoricoConversa h WHERE h.usuario.id = :usuarioId " +
           "AND h.tipo = :tipo ORDER BY h.criadoEm DESC")
    List<HistoricoConversa> findByUsuarioAndTipo(@Param("usuarioId") UUID usuarioId, 
                                                 @Param("tipo") HistoricoConversa.TipoConversa tipo,
                                                 Pageable pageable);
    
    @Query("SELECT COUNT(h) FROM HistoricoConversa h WHERE h.usuario.id = :usuarioId " +
           "AND h.criadoEm >= :dataInicio")
    Long countConversasPorPeriodo(@Param("usuarioId") UUID usuarioId, 
                                  @Param("dataInicio") LocalDateTime dataInicio);
    
    @Query("SELECT AVG(h.avaliacaoUsuario) FROM HistoricoConversa h WHERE h.usuario.id = :usuarioId " +
           "AND h.avaliacaoUsuario IS NOT NULL")
    Double calcularMediaAvaliacoes(@Param("usuarioId") UUID usuarioId);
}