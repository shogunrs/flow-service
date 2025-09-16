package com.docheck.flow.api;

import com.docheck.flow.api.dto.FaceRecognitionDTO;
import com.docheck.flow.application.service.UserService;
import com.docheck.flow.domain.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/face-recognition")
public class FaceRecognitionController {
    private final UserService userService;

    public FaceRecognitionController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register/{userId}")
    public ResponseEntity<?> registerFace(@PathVariable("userId") String userId, @Valid @RequestBody FaceRecognitionDTO dto, HttpServletRequest request) {
        try {
            String clientIp = getClientIpAddress(request);

            // Atualizar o embedding facial do usuário
            userService.updateFaceEmbedding(userId, dto.faceEmbedding());

            // Atualizar informações de acesso
            userService.updateAccessInfo(userId, clientIp, dto.location());

            return ResponseEntity.ok(Map.of("success", true, "message", "Face registrada com sucesso"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Erro ao registrar face: " + e.getMessage()));
        }
    }

    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticateByFace(@Valid @RequestBody FaceRecognitionDTO dto, HttpServletRequest request) {
        try {
            String clientIp = getClientIpAddress(request);

            // Em uma implementação real, aqui você compararia o faceEmbedding
            // com os embeddings armazenados no banco de dados usando algoritmos
            // de similaridade (ex: cosine similarity)

            List<User> allUsers = userService.list();
            for (User user : allUsers) {
                if (user.getFaceEmbedding() != null &&
                    isSimilarFace(user.getFaceEmbedding(), dto.faceEmbedding())) {

                    // Atualizar informações de acesso
                    userService.updateAccessInfo(user.getId(), clientIp, dto.location());

                    return ResponseEntity.ok(Map.of(
                        "success", true,
                        "userId", user.getId(),
                        "name", user.getName(),
                        "email", user.getEmail()
                    ));
                }
            }

            return ResponseEntity.status(401).body(Map.of("error", "Face não reconhecida"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Erro na autenticação: " + e.getMessage()));
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader == null) {
            return request.getRemoteAddr();
        } else {
            return xForwardedForHeader.split(",")[0];
        }
    }

    private boolean isSimilarFace(String storedEmbedding, String providedEmbedding) {
        // Implementação simplificada - em produção, use bibliotecas especializadas
        // para comparar embeddings faciais (ex: cosine similarity)

        // Por enquanto, apenas verifica se são exatamente iguais (para demonstração)
        return storedEmbedding.equals(providedEmbedding);
    }
}