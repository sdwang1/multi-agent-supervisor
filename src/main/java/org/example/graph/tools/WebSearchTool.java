package org.example.graph.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.WebSearchContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.web.search.WebSearchEngine;
import dev.langchain4j.web.search.tavily.TavilyWebSearchEngine;

import java.util.List;

public class WebSearchTool {
    @Tool("Useful for when you need to retrieve up-to-date information, answer questions about current events, or find real-time data from the internet. " +
            "This tool enables access to the latest news, facts, and online resources to provide accurate and timely responses.")
    List<Content> webSearch(@P("The query to use in your search.") String query) {
        WebSearchEngine webSearchEngine = TavilyWebSearchEngine.builder()
                .apiKey(System.getenv("TAVILY_API_KEY")) // get a free key: https://app.tavily.com/sign-in
                .build();

        ContentRetriever webSearchContentRetriever = WebSearchContentRetriever.builder()
                .webSearchEngine(webSearchEngine)
                .maxResults(5)
                .build();

        return webSearchContentRetriever.retrieve( new Query( query ) );
    }
}
