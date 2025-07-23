package com.InvestIA.util;

import com.InvestIA.entity.Usuario;
import org.springframework.security.core.Authentication;

import java.util.UUID;

/**
 * Utilitários para extração de dados de autenticação
 */
public class AuthenticationUtils {
    
    /**
     * Extrai o ID do usuário autenticado do contexto de autenticação
     * 
     * @param authentication O objeto de autenticação do Spring Security
     * @return UUID do usuário autenticado
     * @throws RuntimeException se o usuário não estiver autenticado ou o principal for inválido
     */
    public static UUID obterUsuarioIdAutenticado(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof Usuario) {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            return usuario.getId();
        }
        throw new RuntimeException("Usuário não autenticado ou principal inválido");
    }
    
    /**
     * Extrai o usuário completo do contexto de autenticação
     * 
     * @param authentication O objeto de autenticação do Spring Security
     * @return Usuário autenticado
     * @throws RuntimeException se o usuário não estiver autenticado
     */
    public static Usuario obterUsuarioAutenticado(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof Usuario) {
            return (Usuario) authentication.getPrincipal();
        }
        throw new RuntimeException("Usuário não autenticado ou principal inválido");
    }
}