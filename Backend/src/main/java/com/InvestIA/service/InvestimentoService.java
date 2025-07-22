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
        
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        // Buscar ou criar o ativo
        Ativo ativo = buscarOuCriarAtivo(request.getTicker());
        
        // Criar o investimento
        Investimento investimento = Investimento.builder()
                .usuario(usuario)
                .ativo(ativo)
                .quantidade(request.getQuantidade())
                .valorTotalInvestido(request.getValorCompra().multiply(BigDecimal.valueOf(request.getQuantidade())))
                .valorMedioCompra(request.getValorCompra())
                .dataCompra(request.getDataCompra())
                .ativoStatus(true)
                .build();
        
        investimento = investimentoRepository.save(investimento);
        log.info("Investimento criado com ID: {}", investimento.getId());
        
        return toInvestimentoResponse(investimento);
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
        Optional<Ativo> ativoExistente = ativoRepository.findByTicker(ticker);
        
        if (ativoExistente.isPresent()) {
            return ativoExistente.get();
        }
        
        // Criar novo ativo
        log.info("Criando novo ativo: {}", ticker);
        
        Ativo novoAtivo = Ativo.builder()
                .ticker(ticker)
                .simbolo(ticker) // Usar o mesmo valor do ticker
                .nome(obterNomeAtivo(ticker))
                .tipoAtivo(determinarTipoAtivo(ticker))
                .setor(null) // Pode ser definido manualmente depois
                .status(true)
                .criadoEm(LocalDateTime.now())
                .ultimaAtualizacao(LocalDateTime.now())
                .build();
        
        return ativoRepository.save(novoAtivo);
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
        
        // Calcular valores
        BigDecimal valorAtual = cotacaoAtual.multiply(BigDecimal.valueOf(investimento.getQuantidade()));
        BigDecimal ganhoPerda = valorAtual.subtract(investimento.getValorTotalInvestido());
        BigDecimal percentualGanhoPerda = ganhoPerda.divide(investimento.getValorTotalInvestido(), 4, BigDecimal.ROUND_HALF_UP)
                                                     .multiply(BigDecimal.valueOf(100));
        
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
                .valorTotalAtual(valorAtual)
                .lucroPreju(ganhoPerda)
                .percentualLucroPreju(percentualGanhoPerda)
                .dataCompra(investimento.getDataCompra())
                .build();
    }
}