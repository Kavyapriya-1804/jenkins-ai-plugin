package io.jenkins.plugins.rag;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import io.jenkins.plugins.llms.LLM;
import io.jenkins.plugins.mongodb.MongoDbHelper;
import java.util.List;

/**
 * Class that holds useful methods for RAG Ingestion pipeline
 */
public class Ingestor {
    /**
     * Runs the ingestion pipeline for the passed URL
     * @param url
     * @throws Exception
     */
    public static void runIngestionPipeline(String url) throws Exception {
        URLIngestor urlIngestor = new URLIngestor();
        List<TextSegment> segments = urlIngestor.ingestAndExtractText(url);

        MongoDbHelper.initWithDefault();
        LLM llm = new LLM();
        EmbeddingModel embeddingModel = llm.createTextEmbeddingModel();

        for (TextSegment segment : segments) {
            if (segment.text() == null || segment.text().trim().isEmpty()) {
                continue;
            }
            try {
                Embedding embedding = embeddingModel.embed(segment).content();

                MongoDbHelper.initWithDefault();
                MongoDbHelper.insertSegment(url, segment.text(), embedding.vectorAsList());

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        try {
            String url = "https://maven.apache.org/settings.html";
            runIngestionPipeline(url);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
