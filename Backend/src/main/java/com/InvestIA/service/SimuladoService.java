package com.InvestIA.service;

import com.InvestIA.dto.simulado.*;
import com.InvestIA.entity.Perfil;
import com.InvestIA.entity.Usuario;
import com.InvestIA.enums.NivelExperiencia;
import com.InvestIA.enums.TipoPerfil;
import com.InvestIA.repository.PerfilRepository;
import com.InvestIA.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SimuladoService {
    
    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final ObjectMapper objectMapper;
    private final IAService iaService;
    
    public SimuladoQuestoesResponse obterPerguntas() {
        List<PerguntaSimulado> perguntas = new ArrayList<>();
        
        // Pergunta 1 - Experiência
        perguntas.add(PerguntaSimulado.builder()
                .id(1)
                .pergunta("Há quanto tempo você investe?")
                .categoria("EXPERIENCIA")
                .opcoes(Arrays.asList(
                        OpcaoResposta.builder().id("A").texto("Nunca investi").pontos(0).build(),
                        OpcaoResposta.builder().id("B").texto("Menos de 1 ano").pontos(1).build(),
                        OpcaoResposta.builder().id("C").texto("Entre 1 e 3 anos").pontos(2).build(),
                        OpcaoResposta.builder().id("D").texto("Mais de 3 anos").pontos(3).build()
                ))
                .build());
        
        // Pergunta 2 - Objetivo
        perguntas.add(PerguntaSimulado.builder()
                .id(2)
                .pergunta("Qual é seu principal objetivo ao investir?")
                .categoria("OBJETIVO")
                .opcoes(Arrays.asList(
                        OpcaoResposta.builder().id("A").texto("Preservar capital").pontos(0).build(),
                        OpcaoResposta.builder().id("B").texto("Renda complementar").pontos(1).build(),
                        OpcaoResposta.builder().id("C").texto("Crescimento moderado").pontos(2).build(),
                        OpcaoResposta.builder().id("D").texto("Alto crescimento").pontos(3).build()
                ))
                .build());
        
        // Pergunta 3 - Prazo
        perguntas.add(PerguntaSimulado.builder()
                .id(3)
                .pergunta("Por quanto tempo pretende manter seus investimentos?")
                .categoria("PRAZO")
                .opcoes(Arrays.asList(
                        OpcaoResposta.builder().id("A").texto("Menos de 1 ano").pontos(0).build(),
                        OpcaoResposta.builder().id("B").texto("1 a 3 anos").pontos(1).build(),
                        OpcaoResposta.builder().id("C").texto("3 a 5 anos").pontos(2).build(),
                        OpcaoResposta.builder().id("D").texto("Mais de 5 anos").pontos(3).build()
                ))
                .build());
        
        // Pergunta 4 - Tolerância a risco
        perguntas.add(PerguntaSimulado.builder()
                .id(4)
                .pergunta("Como você reagiria se seus investimentos caíssem 20% em um mês?")
                .categoria("RISCO")
                .opcoes(Arrays.asList(
                        OpcaoResposta.builder().id("A").texto("Venderia tudo imediatamente").pontos(0).build(),
                        OpcaoResposta.builder().id("B").texto("Ficaria preocupado e venderia parte").pontos(1).build(),
                        OpcaoResposta.builder().id("C").texto("Manteria e esperaria recuperar").pontos(2).build(),
                        OpcaoResposta.builder().id("D").texto("Compraria mais aproveitando a queda").pontos(3).build()
                ))
                .build());
        
        // Pergunta 5 - Conhecimento
        perguntas.add(PerguntaSimulado.builder()
                .id(5)
                .pergunta("Como você avalia seu conhecimento sobre investimentos?")
                .categoria("EXPERIENCIA")
                .opcoes(Arrays.asList(
                        OpcaoResposta.builder().id("A").texto("Nenhum conhecimento").pontos(0).build(),
                        OpcaoResposta.builder().id("B").texto("Conhecimento básico").pontos(1).build(),
                        OpcaoResposta.builder().id("C").texto("Conhecimento intermediário").pontos(2).build(),
                        OpcaoResposta.builder().id("D").texto("Conhecimento avançado").pontos(3).build()
                ))
                .build());
        
        return SimuladoQuestoesResponse.builder()
                .perguntas(perguntas)
                .build();
    }
    
    @Transactional
    @SneakyThrows
    public ResultadoSimuladoResponse processarRespostas(SimuladoRespostasRequest request, String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        // Calcular pontuação
        int pontuacaoTotal = calcularPontuacao(request.getRespostas());
        
        // Determinar perfil
        TipoPerfil tipoPerfil = determinarPerfil(pontuacaoTotal);
        NivelExperiencia nivelExp = determinarNivelExperiencia(request.getRespostas());
        
        // Salvar ou atualizar perfil
        Perfil perfil = usuario.getPerfil();
        if (perfil == null) {
            perfil = new Perfil();
            perfil.setUsuario(usuario);
        }
        
        perfil.setTipoPerfil(tipoPerfil);
        perfil.setNivelExperiencia(nivelExp);
        perfil.setPontuacaoSimulado(pontuacaoTotal);
        perfil.setRespostasSimulado(objectMapper.writeValueAsString(request.getRespostas()));
        perfil.setToleranciaRisco(calcularToleranciaRisco(pontuacaoTotal));
        
        perfilRepository.save(perfil);
        
        // Gerar análise com IA
        String analiseIA = iaService.analisarPerfil(tipoPerfil, nivelExp, request.getRespostas());
        
        return ResultadoSimuladoResponse.builder()
                .perfil(tipoPerfil)
                .nivelExperiencia(nivelExp)
                .pontuacaoTotal(pontuacaoTotal)
                .descricaoPerfil(tipoPerfil.getDescricao())
                .caracteristicas(obterCaracteristicas(tipoPerfil))
                .recomendacoesIniciais(obterRecomendacoes(tipoPerfil, nivelExp))
                .toleranciaRisco(calcularToleranciaRisco(pontuacaoTotal))
                .build();
    }
    
    private int calcularPontuacao(Map<Integer, String> respostas) {
        SimuladoQuestoesResponse questoes = obterPerguntas();
        int pontuacao = 0;
        
        for (Map.Entry<Integer, String> resposta : respostas.entrySet()) {
            PerguntaSimulado pergunta = questoes.getPerguntas().stream()
                    .filter(p -> p.getId().equals(resposta.getKey()))
                    .findFirst()
                    .orElse(null);
            
            if (pergunta != null) {
                OpcaoResposta opcao = pergunta.getOpcoes().stream()
                        .filter(o -> o.getId().equals(resposta.getValue()))
                        .findFirst()
                        .orElse(null);
                
                if (opcao != null) {
                    pontuacao += opcao.getPontos();
                }
            }
        }
        
        return pontuacao;
    }
    
    private TipoPerfil determinarPerfil(int pontuacao) {
        if (pontuacao <= 5) {
            return TipoPerfil.CONSERVADOR;
        } else if (pontuacao <= 10) {
            return TipoPerfil.MODERADO;
        } else {
            return TipoPerfil.AGRESSIVO;
        }
    }
    
    private NivelExperiencia determinarNivelExperiencia(Map<Integer, String> respostas) {
        // Analisar respostas específicas de experiência
        String respostaExp = respostas.get(1); // Pergunta sobre tempo investindo
        String respostaConh = respostas.get(5); // Pergunta sobre conhecimento
        
        if ("A".equals(respostaExp) || "A".equals(respostaConh)) {
            return NivelExperiencia.INICIANTE;
        } else if ("D".equals(respostaExp) && "D".equals(respostaConh)) {
            return NivelExperiencia.EXPERT;
        } else if ("C".equals(respostaExp) || "C".equals(respostaConh)) {
            return NivelExperiencia.AVANCADO;
        } else {
            return NivelExperiencia.INTERMEDIARIO;
        }
    }
    
    private Double calcularToleranciaRisco(int pontuacao) {
        return pontuacao / 15.0; // Normalizado entre 0 e 1
    }
    
    private List<String> obterCaracteristicas(TipoPerfil perfil) {
        switch (perfil) {
            case CONSERVADOR:
                return Arrays.asList(
                        "Prioriza segurança do capital",
                        "Prefere investimentos de baixo risco",
                        "Busca rendimentos estáveis"
                );
            case MODERADO:
                return Arrays.asList(
                        "Busca equilíbrio entre risco e retorno",
                        "Aceita volatilidade moderada",
                        "Diversifica investimentos"
                );
            case AGRESSIVO:
                return Arrays.asList(
                        "Aceita alto risco por maior retorno",
                        "Focado em crescimento de longo prazo",
                        "Tolera alta volatilidade"
                );
            default:
                return Arrays.asList();
        }
    }
    
    private List<String> obterRecomendacoes(TipoPerfil perfil, NivelExperiencia nivel) {
        List<String> recomendacoes = new ArrayList<>();
        
        switch (perfil) {
            case CONSERVADOR:
                recomendacoes.add("Considere Tesouro Direto e CDBs");
                recomendacoes.add("Mantenha reserva de emergência");
                break;
            case MODERADO:
                recomendacoes.add("Diversifique entre renda fixa e variável");
                recomendacoes.add("Considere fundos de investimento");
                break;
            case AGRESSIVO:
                recomendacoes.add("Foque em ações e fundos de ações");
                recomendacoes.add("Considere investimentos internacionais");
                break;
        }
        
        if (nivel == NivelExperiencia.INICIANTE) {
            recomendacoes.add("Estude sobre investimentos antes de aplicar");
            recomendacoes.add("Comece com valores menores");
        }
        
        return recomendacoes;
    }
}