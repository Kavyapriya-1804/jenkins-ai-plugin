package io.jenkins.plugins.mongodb;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Indexes;
import java.util.List;
import java.util.stream.Collectors;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that contains useful MongoDB methods
 */
public class MongoDbHelper {

    private static final Logger log = LoggerFactory.getLogger(MongoDbHelper.class);
    private static MongoClient client;
    private static MongoCollection<Document> collection;

    public static MongoClient getClient() {
        return client;
    }

    public static final String FIELD_URL = "url";
    public static final String FIELD_SEGMENT_TEXT = "segment_text";
    public static final String FIELD_EMBEDDING = "embedding";
    public static final String FIELD_SCORE = "vectorSearchScore";
    private static final String VECTOR_INDEX_NAME = "vector_index";

    /**
     * Initializes MongoDB connection and instantiates the collection
     * @param connectionString
     * @param databaseName
     * @param collectionName
     */
    public static void init(String connectionString, String databaseName, String collectionName) {
        log.info("Initializing MongoDB connection...");
        try {
            client = MongoClients.create(connectionString);
            MongoDatabase database = client.getDatabase(databaseName);
            collection = database.getCollection(collectionName);
            log.info("Connected to MongoDB. Database: '{}', Collection: '{}'", databaseName, collectionName);

            // Optional: Create a standard index on the URL field if you query by it often
            try {
                log.debug("Ensuring standard index exists on '{}' field...", FIELD_URL);
                collection.createIndex(Indexes.ascending(FIELD_URL));
                log.debug("Standard index on '{}' ensured.", FIELD_URL);
            } catch (Exception e) {
                log.warn(
                        "Could not create standard index on '{}'. It might already exist or there's a permission issue. "
                                + "Note: This is separate from the required Atlas Vector Search index.",
                        FIELD_URL,
                        e);
            }
            // **IMPORTANT**: Atlas Vector Search index (e.g., "vector_index") MUST be created MANUALLY
            // in the MongoDB Atlas UI or via Atlas Admin API / CLI before using findRelevantSegmentsUsingAtlasSearch.
            // Example index definition (adjust dimensions, similarity):
            // {
            //   "name": "vector_index",
            //   "type": "vectorSearch",
            //   "fields": [
            //     {
            //       "type": "vector",
            //       "path": "embedding",
            //       "numDimensions": 384, // MATCH YOUR EMBEDDING MODEL DIMENSIONS
            //       "similarity": "cosine" // or "dotProduct" / "euclidean"
            //     }
            //   ]
            // }
            log.info(
                    "Ensure the Atlas Vector Search index named '{}' is created on the '{}' collection for field '{}'.",
                    VECTOR_INDEX_NAME,
                    collectionName,
                    FIELD_EMBEDDING);

        } catch (Exception e) {
            log.error("Failed to initialize MongoDB connection", e);
            throw new RuntimeException("Failed to initialize MongoDB connection", e);
        }
    }

    /**
     * Wrapper method for initializing mongoDB with default credentials
     */
    public static void initWithDefault() {
        String connectionString = System.getenv("MONGODB_CONNECTION_STRING");
        if (connectionString == null || connectionString.isEmpty()) {
            connectionString =
                    "mongodb+srv://kavyajg1804:T3LLeXzJNnVFyHPa@kcluster.kmbuu.mongodb.net/?retryWrites=true&w=majority&appName=KCluster";
            log.warn("MONGODB_CONNECTION_STRING environment variable not set. Using fallback connection string.");
        } else {
            log.info("Using MongoDB connection string from environment variable.");
        }
        String databaseName = "jenkins_ai_plugin";
        String collectionName = "webpage_embeddings";
        init(connectionString, databaseName, collectionName);
    }

    /**
     * Useful util function formatting float type of the passed embedding to double type
     * @param embedding
     * @return
     */
    public static List<Double> formatEmbeddingsToDouble(List<Float> embedding) {
        List<Double> embeddingAsDouble = embedding.stream()
                .map(f -> (f == null ? null : f.doubleValue()))
                .collect(Collectors.toList());
        return embeddingAsDouble;
    }

    /**
     * Inserts the chunk wise embeddings into MongoDB collection
     * @param url
     * @param segmentText
     * @param embedding
     */
    public static void insertSegment(String url, String segmentText, List<Float> embedding) {
        if (collection == null) {
            log.error("MongoDB collection is not initialized. Call init() first.");
            return;
        }
        if (embedding == null || embedding.isEmpty()) {
            log.error("Cannot insert segment with null or empty embedding for URL: {}", url);
            return;
        }
        try {
            // Storing embeddings as List<Double> for better compatibility with MongoDB BSON types
            List<Double> embeddingAsDouble = formatEmbeddingsToDouble(embedding);

            if (embeddingAsDouble.contains(null)) {
                log.warn("Embedding for URL {} contains null values. Check the embedding generation process.", url);
            }

            Document doc = new Document(FIELD_URL, url)
                    .append(FIELD_SEGMENT_TEXT, segmentText)
                    .append(FIELD_EMBEDDING, embeddingAsDouble);
            collection.insertOne(doc);
        } catch (Exception e) {
            log.error("Failed to insert segment for URL: {}", url, e);
        }
    }

    /**
     * Used to close the resources
     */
    public static void close() {
        if (client != null) {
            try {
                client.close();
                log.info("MongoDB client closed.");
            } catch (Exception e) {
                log.error("Error closing MongoDB client", e);
            } finally {
                client = null;
                collection = null;
            }
        }
    }
}
