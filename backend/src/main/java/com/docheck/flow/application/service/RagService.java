package com.docheck.flow.application.service;

import com.docheck.flow.domain.model.KnowledgeDocument;
import com.docheck.flow.domain.model.PromptAgent;
import com.docheck.flow.infrastructure.mongo.KnowledgeDocumentRepository;
import com.docheck.flow.infrastructure.mongo.PromptAgentRepository;
import com.docheck.flow.infrastructure.rabbitmq.RabbitMQConfig;
import com.docheck.flow.infrastructure.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Slf4j
@RequiredArgsConstructor
public class RagService {

    private final PromptAgentRepository promptAgentRepository;
    private final KnowledgeDocumentRepository knowledgeDocumentRepository;
    private final FileStorageService fileStorageService; // Injetando o serviço de armazenamento
    private final RabbitTemplate rabbitTemplate; // Injetando o RabbitTemplate

    public void processAndStoreFile(String agentId, MultipartFile file) {
        log.info("Received file '{}' for agent ID '{}'. Size: {} bytes.",
                file.getOriginalFilename(), agentId, file.getSize());

        // Step 1: Validate agent existence
        PromptAgent agent = promptAgentRepository.findById(agentId)
                .orElseThrow(() -> new NoSuchElementException("PromptAgent not found with id: " + agentId));

        // Step 2: Save the original file to the File Storage Service
        String storedFilePath;
        try {
            // Assumindo um método uploadFile que retorna o caminho/ID do arquivo salvo
            storedFilePath = fileStorageService.uploadFile(file, "rag-knowledge");
            log.info("File '{}' uploaded to storage at: {}", file.getOriginalFilename(), storedFilePath);
        } catch (Exception e) {
            log.error("Failed to upload file '{}' to storage.", file.getOriginalFilename(), e);
            throw new RuntimeException("Failed to upload file to storage", e);
        }

        // Step 3: Create and save the initial knowledge document with QUEUED status
        KnowledgeDocument doc = new KnowledgeDocument();
        doc.setAgentId(agentId);
        doc.setOriginalFileName(file.getOriginalFilename());
        doc.setStoredFilePath(storedFilePath); // Salva o caminho do arquivo no storage
        doc.setStatus(KnowledgeDocument.ProcessingStatus.QUEUED);
        KnowledgeDocument savedDoc = knowledgeDocumentRepository.save(doc);
        log.info("Created knowledge document with ID: {} and status QUEUED.", savedDoc.getId());

        // Step 4: Publish a message to RabbitMQ to trigger async processing
        rabbitTemplate.convertAndSend(RabbitMQConfig.RAG_EXCHANGE_NAME, RabbitMQConfig.RAG_ROUTING_KEY, savedDoc.getId());
        log.info("Message for KnowledgeDocument ID {} sent to RabbitMQ queue {}.", savedDoc.getId(), RabbitMQConfig.RAG_QUEUE_NAME);

        // Step 5: Update the agent with the new knowledge document ID (optional, can be done by listener)
        // For now, we'll keep it here for immediate association feedback.
        agent.getKnowledgeFileIds().add(savedDoc.getId());
        promptAgentRepository.save(agent);

        log.info("File ingestion for agent '{}' completed. Document ID: {}. Processing will continue asynchronously.", agent.getName(), savedDoc.getId());
    }

    public List<KnowledgeDocument> getProcessingDocuments() {
        return knowledgeDocumentRepository.findByStatusIn(
                List.of(KnowledgeDocument.ProcessingStatus.QUEUED,
                        KnowledgeDocument.ProcessingStatus.PROCESSING,
                        KnowledgeDocument.ProcessingStatus.FAILED)
        );
    }
}
