package io.jenkins.plugins.rag;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.segment.TextSegment;
import java.util.ArrayList;
import java.util.List;

/**
 * A very basic DocumentSplitter implementation that splits text
 * into chunks of a fixed maximum character length.
 * Does not handle overlap or semantic boundaries.
 */
public class SimpleCharacterSplitter implements DocumentSplitter {

    private final int maxChunkSizeChars;

    public SimpleCharacterSplitter(int maxChunkSizeChars) {
        if (maxChunkSizeChars <= 0) {
            throw new IllegalArgumentException("maxChunkSizeChars must be positive");
        }
        this.maxChunkSizeChars = maxChunkSizeChars;
    }

    @Override
    public List<TextSegment> split(Document document) {
        List<TextSegment> segments = new ArrayList<>();
        String text = document.text();
        if (text == null || text.isEmpty()) {
            return segments;
        }

        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + maxChunkSizeChars, text.length());
            String chunkText = text.substring(start, end);
            // Create a TextSegment, inheriting metadata from the original document
            TextSegment segment = TextSegment.from(chunkText, document.metadata());
            segments.add(segment);
            start = end;
        }

        return segments;
    }
}
