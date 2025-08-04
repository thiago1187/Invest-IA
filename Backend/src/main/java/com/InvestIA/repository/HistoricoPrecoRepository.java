package com.InvestIA.repository;

import com.InvestIA.entity.Ativo;
import com.InvestIA.entity.HistoricoPreco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HistoricoPrecoRepository extends JpaRepository<HistoricoPreco, UUID> {
    
    // Buscar histórico por ativo e período
    @Query("SELECT hp FROM HistoricoPreco hp WHERE hp.ativo = :ativo AND hp.data BETWEEN :dataInicio AND :dataFim ORDER BY hp.data ASC")
    List<HistoricoPreco> findByAtivoAndDataBetween(@Param("ativo") Ativo ativo, 
                                                   @Param("dataInicio") LocalDate dataInicio, 
                                                   @Param("dataFim") LocalDate dataFim);
    
    // Buscar últimos N dias de histórico para um ativo
    @Query("SELECT hp FROM HistoricoPreco hp WHERE hp.ativo = :ativo AND hp.data >= :dataInicio ORDER BY hp.data ASC")
    List<HistoricoPreco> findByAtivoAndDataAfter(@Param("ativo") Ativo ativo, @Param("dataInicio") LocalDate dataInicio);
    
    // Buscar preço mais recente de um ativo
    @Query("SELECT hp FROM HistoricoPreco hp WHERE hp.ativo = :ativo ORDER BY hp.data DESC LIMIT 1")
    Optional<HistoricoPreco> findLatestByAtivo(@Param("ativo") Ativo ativo);
    
    // Buscar se já existe histórico para uma data específica
    @Query("SELECT hp FROM HistoricoPreco hp WHERE hp.ativo = :ativo AND hp.data = :data")
    Optional<HistoricoPreco> findByAtivoAndData(@Param("ativo") Ativo ativo, @Param("data") LocalDate data);
    
    // Buscar histórico dos últimos N dias para múltiplos ativos
    @Query("SELECT hp FROM HistoricoPreco hp WHERE hp.ativo IN :ativos AND hp.data >= :dataInicio ORDER BY hp.ativo.id, hp.data ASC")
    List<HistoricoPreco> findByAtivosAndDataAfter(@Param("ativos") List<Ativo> ativos, @Param("dataInicio") LocalDate dataInicio);
    
    // Contar quantos dias de histórico temos para um ativo
    @Query("SELECT COUNT(hp) FROM HistoricoPreco hp WHERE hp.ativo = :ativo")
    Long countByAtivo(@Param("ativo") Ativo ativo);
    
    // Buscar datas que não têm histórico para um ativo (para preencher gaps)
    @Query("SELECT DISTINCT hp.data FROM HistoricoPreco hp WHERE hp.ativo = :ativo AND hp.data BETWEEN :dataInicio AND :dataFim ORDER BY hp.data")
    List<LocalDate> findDatasByAtivoAndDateRange(@Param("ativo") Ativo ativo, 
                                                @Param("dataInicio") LocalDate dataInicio, 
                                                @Param("dataFim") LocalDate dataFim);
}