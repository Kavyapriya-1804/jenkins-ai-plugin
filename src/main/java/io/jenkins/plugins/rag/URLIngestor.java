package io.jenkins.plugins.rag;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.UrlDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.segment.TextSegment;
import java.util.List;
import org.jsoup.Jsoup;

/**
 * Class that encompasses useful methods for URL ingestion
 */
public class URLIngestor {
    /**
     * Loads, Extracts and Returns chunks of data from the given URL
     * @param url
     * @return
     * @throws Exception
     */
    public List<TextSegment> ingestAndExtractText(String url) throws Exception {
        // Load the raw HTML content using TextDocumentParser
        DocumentParser textParser = new TextDocumentParser();
        Document rawHtmlDocument = UrlDocumentLoader.load(url, textParser);
        System.out.println("Raw HTML loaded. Length: " + rawHtmlDocument.text().length());

        // Use Jsoup to parse the HTML and extract clean text
        String rawHtml = rawHtmlDocument.text();
        String cleanText = Jsoup.parse(rawHtml).text();
        System.out.println("Clean text extracted using Jsoup. Length: " + cleanText.length());
        System.out.println("Clean text start: '"
                + cleanText.substring(0, Math.min(200, cleanText.length())).replace('\n', ' ') + "...'");

        // Create a new Document containing only the clean text
        //    but preserving the original metadata (like the URL)
        Document cleanDocument = Document.from(cleanText, rawHtmlDocument.metadata());

        // Split the clean document
        int chunkSize = 500;
        DocumentSplitter splitter = new SimpleCharacterSplitter(chunkSize);
        List<TextSegment> segments = splitter.split(cleanDocument);

        return segments;
    }
}
