package io.jenkins.plugins.sample;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

public class HelloLlm {
    public String talkToLLM(String query) {
        ChatLanguageModel model = OpenAiChatModel.builder()
                .baseUrl("https://api.groq.com/openai/v1")
                .apiKey("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")
                .modelName("llama3-8b-8192")
                .build();
        String answer = model.generate(query);
        return answer;
    }
}
