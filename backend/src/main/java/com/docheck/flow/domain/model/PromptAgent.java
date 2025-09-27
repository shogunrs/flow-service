package com.docheck.flow.domain.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "prompt_agents")
public class PromptAgent {
    @Id
    private String id;
    private String name;
    private String prompt;
    private List<String> knowledgeFileIds = new ArrayList<>();
}
