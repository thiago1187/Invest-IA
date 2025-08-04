package com.InvestIA.controller;

import com.InvestIA.entity.Usuario;
import com.InvestIA.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user-update")
@RequiredArgsConstructor
public class UserUpdateController {
    
    private final UsuarioRepository usuarioRepository;
    
    @PostMapping("/update-test-user-name")
    public ResponseEntity<?> updateTestUserName(@RequestBody Map<String, String> request) {
        try {
            String novoNome = request.get("nome");
            if (novoNome == null || novoNome.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Nome não pode estar vazio");
            }
            
            Usuario usuarioTeste = usuarioRepository.findByEmail("teste@investia.com")
                    .orElseThrow(() -> new RuntimeException("Usuário de teste não encontrado"));
            
            usuarioTeste.setNome(novoNome);
            usuarioRepository.save(usuarioTeste);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Nome do usuário de teste atualizado para: " + novoNome,
                    "usuario", Map.of(
                            "id", usuarioTeste.getId(),
                            "nome", usuarioTeste.getNome(),
                            "email", usuarioTeste.getEmail()
                    )
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Erro ao atualizar nome: " + e.getMessage()
            ));
        }
    }
}