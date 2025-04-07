package io.jenkins.plugins.rag;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import io.jenkins.plugins.llms.LLM;
import io.jenkins.plugins.mongodb.MongoDbHelper;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;

/**
 * Class that holds methods useful for RAG Retrieval pipeline
 */
public class Retriever {

    /**
     * Retrieves text segments from MongoDB that are similar to the query.
     *
     * @param query The query text.
     * @return A list of text segments with similarity above a threshold.
     * @throws Exception If an error occurs.
     */
    public static List<TextSegment> retrieveRagResults(String query) throws Exception {
        MongoDbHelper.initWithDefault();
        MongoDatabase database = MongoDbHelper.getClient().getDatabase("jenkins_ai_plugin");
        MongoCollection<Document> collection = database.getCollection("webpage_embeddings");

        // Create an embedding model using your LLM wrapper
        LLM llm = new LLM();
        EmbeddingModel embeddingModel = llm.createTextEmbeddingModel();

        // Compute the embedding for the query
        Embedding queryEmbedding = embeddingModel.embed(query).content();
        List<Double> queryVector = MongoDbHelper.formatEmbeddingsToDouble(queryEmbedding.vectorAsList());

        // Retrieve stored segments and compute similarity
        List<TextSegment> results = new ArrayList<>();
        FindIterable<Document> docs = collection.find();
        for (Document doc : docs) {
            @SuppressWarnings("unchecked")
            List<Double> storedVector = (List<Double>) doc.get("embedding");
            String text = doc.getString("segment_text");

            // Compute cosine similarity between query and stored embedding
            double similarity = cosineSimilarity(queryVector, storedVector);

            // Use a threshold to filter results (adjust as needed)
            if (similarity > 0.5) {
                results.add(new TextSegment(text, new Metadata()));
            }
        }
        return results;
    }

    /**
     * Computes cosine similarity between two vectors.
     *
     * @param vectorA The first vector.
     * @param vectorB The second vector.
     * @return The cosine similarity value.
     */
    private static double cosineSimilarity(List<Double> vectorA, List<Double> vectorB) {
        if (vectorA.size() != vectorB.size()) {
            throw new IllegalArgumentException("Vectors must be the same size.");
        }
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vectorA.size(); i++) {
            dotProduct += vectorA.get(i) * vectorB.get(i);
            normA += Math.pow(vectorA.get(i), 2);
            normB += Math.pow(vectorB.get(i), 2);
        }
        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    public static void main(String[] args) {
        String query = "There are two locations where a settings.xml file may live ?";
        // String query = "Where is tajmahal ?";
        try {
            List<TextSegment> segments = Retriever.retrieveRagResults(query);
            segments.stream().forEach(segment -> System.out.println(segment.text()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
