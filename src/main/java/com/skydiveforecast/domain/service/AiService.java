package com.skydiveforecast.domain.service;

import com.skydiveforecast.infrastructure.adapter.in.web.dto.AnalysisRequest;
import com.skydiveforecast.infrastructure.adapter.in.web.dto.AnalysisResponse;
import org.springframework.stereotype.Service;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class AiService {

    private final ChatClient chatClient;

    @Autowired
    public AiService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public AnalysisResponse analyze(AnalysisRequest request) {
        String userData = null;

        // TODO get prompt template from database, add data about weather, and airports

        Prompt prompt = new Prompt(
            "Data: " + userData + "\n" +
            "Please analyze forecast."
        );

        String aiResult = chatClient.prompt(prompt)
                .call()
                .content();

        return new AnalysisResponse(aiResult);
    }
}