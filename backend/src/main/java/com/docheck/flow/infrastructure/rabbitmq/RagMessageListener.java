package com.docheck.flow.infrastructure.rabbitmq;

import com.docheck.flow.domain.model.KnowledgeDocument;
import com.docheck.flow.infrastructure.mongo.KnowledgeDocumentRepository;
import com.docheck.flow.infrastructure.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class RagMessageListener {

    private final KnowledgeDocumentRepository knowledgeDocumentRepository;
    private final FileStorageService fileStorageService;
    private final Tika tika = new Tika();

    private static final String RAG_QUEUE_NAME = "rag.processing.queue";

    @RabbitListener(queues = RAG_QUEUE_NAME)
    public void receiveRagMessage(String knowledgeDocumentId) {
        log.info("Received message for KnowledgeDocument ID: {}", knowledgeDocumentId);

        Optional<KnowledgeDocument> optionalDoc = knowledgeDocumentRepository.findById(knowledgeDocumentId);

        if (optionalDoc.isEmpty()) {
            log.warn("KnowledgeDocument with ID {} not found. Skipping processing.", knowledgeDocumentId);
            return;
        }

        KnowledgeDocument doc = optionalDoc.get();
        doc.setStatus(KnowledgeDocument.ProcessingStatus.PROCESSING);
        knowledgeDocumentRepository.save(doc);

        try {
            // Step 1: Retrieve the original file from the FileStorageService
            log.info("Retrieving file {} from storage for document ID: {}", doc.getOriginalFileName(), doc.getId());
            // CORREÇÃO: Usando getFileStream em vez de downloadFile
            InputStream fileStream = fileStorageService.getFileStream(doc.getStoredFilePath());

            // Step 2: Extract content from the file using Apache Tika
            log.info("Starting content extraction for document ID: {}", doc.getId());
            String content = tika.parseToString(fileStream);

            // Step 3: Update the document with the content and final status
            doc.setMarkdownContent(content);
            doc.setStatus(KnowledgeDocument.ProcessingStatus.COMPLETED);
            knowledgeDocumentRepository.save(doc);
            log.info("Successfully extracted content and updated document ID: {}", doc.getId());

        } catch (Exception e) {
            log.error("Failed to process file for document ID: {}", doc.getId(), e);
            doc.setStatus(KnowledgeDocument.ProcessingStatus.FAILED);
            knowledgeDocumentRepository.save(doc);
        }
    }
}
