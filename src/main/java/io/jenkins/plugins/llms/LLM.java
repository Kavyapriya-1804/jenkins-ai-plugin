package io.jenkins.plugins.llms;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.huggingface.HuggingFaceEmbeddingModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.openai.OpenAiChatModel;
import io.jenkins.plugins.rag.Retriever;
import io.jenkins.plugins.utils.CredentialUtils;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *  Class that encompasses methods to create different LLM models and useful LLM methods
 */
public class LLM {
    /**
     * Creates and returns a text embedding model
     * @return HuggingFaceEmbeddingModel
     */
    public EmbeddingModel createTextEmbeddingModel() {
        String hfApiKey = CredentialUtils.getAPIKey("HF_API_KEY");

        return HuggingFaceEmbeddingModel.builder()
                .accessToken(hfApiKey)
                .modelId("sentence-transformers/all-MiniLM-L6-v2")
                .waitForModel(true) // Wait for the model to be ready on the server (if applicable)
                .timeout(Duration.ofSeconds(60))
                .build();
    }

    /**
     * Creates and returns a Chat model
     * @return OpenAiChatModel
     */
    public ChatLanguageModel createChatModel() {
        String groqApiKey = CredentialUtils.getAPIKey("GROQ_API_KEY");

        return OpenAiChatModel.builder()
                .baseUrl("https://api.groq.com/openai/v1")
                .apiKey(groqApiKey)
                .modelName("llama3-8b-8192")
                .build();
    }

    /**
     * Returns LLM generated response for the given query
     * @param query
     * @return
     */
    public String talkToLLM(String query) {
        ChatLanguageModel model = createChatModel();
        return model.generate(query);
    }

    /**
     * Returns LLM generated response having the retrieved text from the URL fed as content to the prompt
     * @param query
     * @return
     * @throws Exception
     */
    public String ragRetrievalWithLLM(String query) throws Exception {
        List<TextSegment> segments = Retriever.retrieveRagResults(query);

        PromptTemplate promptTemplate =
                PromptTemplate.from("You are a helpful Jenkins assistant. Answer the following query\n"
                        + "Query: {{query}}\n\n" + "Answer:");
        Map<String, Object> variables = new HashMap<>();
        variables.put("query", query);

        if (!segments.isEmpty()) {
            String context = segments.stream().map(TextSegment::text).collect(Collectors.joining("\n---\n"));
            if (!context.isEmpty()) {
                promptTemplate = PromptTemplate.from(
                        "You are a helpful Jenkins assistant. Answer the following query based ONLY on the provided context.\n"
                                + "If the context does not contain the answer, state that the context doesn't provide the information.\n\n"
                                + "Context:\n"
                                + "---\n"
                                + "{{context}}\n"
                                + "---\n\n"
                                + "Query: {{query}}\n\n"
                                + "Answer:");
                variables.put("context", context);
            }
        }

        Prompt prompt = promptTemplate.apply(variables);

        return talkToLLM(prompt.text());
    }

    public static void main(String[] args) throws Exception {
        LLM llm = new LLM();
        String query = "There are two locations where a settings.xml file may live ?";
        // String query = "Where is tajmahal ?";

        String response = llm.ragRetrievalWithLLM(query);
        System.out.println(response);
    }
}
