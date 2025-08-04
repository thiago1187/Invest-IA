package com.InvestIA.service;

import com.InvestIA.dto.investimento.*;
import com.InvestIA.entity.*;
import com.InvestIA.enums.TipoAtivo;
import com.InvestIA.enums.TipoInvestimento;
import com.InvestIA.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvestimentoService {
    
    private final InvestimentoRepository investimentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final AtivoRepository ativoRepository;
    private final FinanceAPIService financeAPIService;
    
    @Transactional(readOnly = true)
    public Page<InvestimentoResponse> listarPorUsuario(UUID usuarioId, Pageable pageable) {
        log.info("Listando investimentos para usuário: {}", usuarioId);
        
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        Page<Investimento> investimentos = investimentoRepository.findByUsuarioIdOrderByCriadoEmDesc(usuarioId, pageable);
        
        List<InvestimentoResponse> responses = investimentos.stream()
                .map(this::toInvestimentoResponse)
                .collect(Collectors.toList());
        
        return new PageImpl<>(responses, pageable, investimentos.getTotalElements());
    }
    
    @Transactional
    public InvestimentoResponse criar(UUID usuarioId, CriarInvestimentoRequest request) {
        log.info("Criando investimento para usuário {}: {}", usuarioId, request.getTicker());
        
        try {
            // Validações adicionais
            if (request.getTicker() == null || request.getTicker().trim().isEmpty()) {
                throw new RuntimeException("Ticker não pode ser vazio");
            }
            
            if (request.getQuantidade() == null || request.getQuantidade() <= 0) {
                throw new RuntimeException("Quantidade deve ser maior que zero");
            }
            
            if (request.getValorCompra() == null || request.getValorCompra().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("Valor de compra deve ser maior que zero");
            }
            
            if (request.getDataCompra() == null) {
                throw new RuntimeException("Data de compra é obrigatória");
            }
            
            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + usuarioId));
            
            // Buscar ou criar o ativo
            Ativo ativo = buscarOuCriarAtivo(request.getTicker().trim().toUpperCase());
            
            // Criar o investimento
            Investimento investimento = Investimento.builder()
                    .usuario(usuario)
                    .ativo(ativo)
                    .quantidade(request.getQuantidade())
                    .valorTotalInvestido(request.getValorCompra().multiply(BigDecimal.valueOf(request.getQuantidade())))
                    .valorMedioCompra(request.getValorCompra())
                    .dataCompra(request.getDataCompra())
                    .ativoStatus(true)
                    .criadoEm(LocalDateTime.now())
                    .atualizadoEm(LocalDateTime.now())
                    .build();
            
            investimento = investimentoRepository.save(investimento);
            log.info("Investimento criado com sucesso. ID: {}", investimento.getId());
            
            return toInvestimentoResponse(investimento);
            
        } catch (Exception e) {
            log.error("Erro ao criar investimento para usuário {}: {}", usuarioId, e.getMessage(), e);
            throw new RuntimeException("Erro ao criar investimento: " + e.getMessage());
        }
    }
    
    @Transactional
    public InvestimentoResponse atualizar(UUID usuarioId, UUID investimentoId, AtualizarInvestimentoRequest request) {
        log.info("Atualizando investimento {} para usuário {}", investimentoId, usuarioId);
        
        Investimento investimento = investimentoRepository.findByIdAndUsuarioId(investimentoId, usuarioId)
                .orElseThrow(() -> new RuntimeException("Investimento não encontrado"));
        
        if (request.getQuantidade() != null) {
            investimento.setQuantidade(request.getQuantidade());
            investimento.setValorTotalInvestido(investimento.getValorMedioCompra().multiply(BigDecimal.valueOf(request.getQuantidade())));
        }
        
        if (request.getValorMedioCompra() != null) {
            investimento.setValorMedioCompra(request.getValorMedioCompra());
            investimento.setValorTotalInvestido(request.getValorMedioCompra().multiply(BigDecimal.valueOf(investimento.getQuantidade())));
        }
        
        investimento = investimentoRepository.save(investimento);
        log.info("Investimento atualizado: {}", investimento.getId());
        
        return toInvestimentoResponse(investimento);
    }
    
    @Transactional
    public void remover(UUID usuarioId, UUID investimentoId) {
        log.info("Removendo investimento {} do usuário {}", investimentoId, usuarioId);
        
        Investimento investimento = investimentoRepository.findByIdAndUsuarioId(investimentoId, usuarioId)
                .orElseThrow(() -> new RuntimeException("Investimento não encontrado"));
        
        investimentoRepository.delete(investimento);
        log.info("Investimento removido: {}", investimentoId);
    }
    
    private Ativo buscarOuCriarAtivo(String ticker) {
        try {
            // Primeiro tenta buscar pelo ticker exato
            Optional<Ativo> ativoExistente = ativoRepository.findByTicker(ticker);
            
            if (ativoExistente.isPresent()) {
                log.info("Ativo encontrado: {}", ticker);
                return ativoExistente.get();
            }
            
            // Se não encontrar, tenta buscar pelo símbolo
            ativoExistente = ativoRepository.findBySimbolo(ticker);
            if (ativoExistente.isPresent()) {
                log.info("Ativo encontrado por símbolo: {}", ticker);
                return ativoExistente.get();
            }
            
            // Criar novo ativo
            log.info("Criando novo ativo: {}", ticker);
            
            String simboloCompleto = ticker.contains(".SA") ? ticker : ticker;
            
            Ativo novoAtivo = Ativo.builder()
                    .ticker(ticker)
                    .simbolo(simboloCompleto)
                    .nome(obterNomeAtivo(ticker))
                    .tipoAtivo(determinarTipoAtivo(ticker))
                    .setor(null)
                    .status(true)
                    .criadoEm(LocalDateTime.now())
                    .ultimaAtualizacao(LocalDateTime.now())
                    .build();
            
            Ativo ativoSalvo = ativoRepository.save(novoAtivo);
            log.info("Novo ativo criado com ID: {}", ativoSalvo.getId());
            return ativoSalvo;
            
        } catch (Exception e) {
            log.error("Erro ao buscar ou criar ativo {}: {}", ticker, e.getMessage(), e);
            throw new RuntimeException("Erro ao processar ativo: " + e.getMessage());
        }
    }
    
    private String obterNomeAtivo(String ticker) {
        // Nomes conhecidos dos ativos principais
        switch (ticker.toUpperCase()) {
            case "PETR4.SA": return "PETROBRAS PN";
            case "VALE3.SA": return "VALE ON";
            case "BBAS3.SA": return "BANCO DO BRASIL ON";
            case "AAPL": return "Apple Inc";
            case "TSLA": return "Tesla Inc";
            case "BTC-USD": return "Bitcoin USD";
            case "ETH-USD": return "Ethereum USD";
            default: return ticker + " - Ativo";
        }
    }
    
    private TipoAtivo determinarTipoAtivo(String ticker) {
        if (ticker.contains(".SA")) {
            return TipoAtivo.ACAO;
        } else if (ticker.contains("BTC") || ticker.contains("ETH") || ticker.contains("CRYPTO")) {
            return TipoAtivo.CRIPTO;
        } else {
            return TipoAtivo.ACAO; // Ações internacionais também são ACAO
        }
    }
    
    private InvestimentoResponse toInvestimentoResponse(Investimento investimento) {
        // Buscar cotação atual
        BigDecimal cotacaoAtual = financeAPIService.getCurrentPrice(investimento.getAtivo().getTicker())
                .orElse(investimento.getValorMedioCompra());
        
        // Atualizar valor atual na entidade (preço por ação)
        investimento.setValorAtual(cotacaoAtual);
        investimento.setAtualizadoEm(LocalDateTime.now());
        investimentoRepository.save(investimento);
        
        // Calcular valores
        BigDecimal valorTotalAtual = cotacaoAtual.multiply(BigDecimal.valueOf(investimento.getQuantidade()));
        BigDecimal lucroPreju = valorTotalAtual.subtract(investimento.getValorTotalInvestido());
        BigDecimal percentualLucroPreju = investimento.getValorTotalInvestido().compareTo(BigDecimal.ZERO) > 0 ?
                lucroPreju.divide(investimento.getValorTotalInvestido(), 4, RoundingMode.HALF_UP)
                         .multiply(BigDecimal.valueOf(100)) :
                BigDecimal.ZERO;
        
        return InvestimentoResponse.builder()
                .id(investimento.getId())
                .ativo(AtivoResponse.builder()
                        .id(investimento.getAtivo().getId())
                        .ticker(investimento.getAtivo().getTicker())
                        .nome(investimento.getAtivo().getNome())
                        .tipoAtivo(investimento.getAtivo().getTipoAtivo())
                        .build())
                .quantidade(investimento.getQuantidade())
                .valorMedioCompra(investimento.getValorMedioCompra())
                .valorAtual(cotacaoAtual)
                .valorTotalInvestido(investimento.getValorTotalInvestido())
                .valorTotalAtual(valorTotalAtual)
                .lucroPreju(lucroPreju)
                .percentualLucroPreju(percentualLucroPreju)
                .dataCompra(investimento.getDataCompra())
                .build();
    }
}