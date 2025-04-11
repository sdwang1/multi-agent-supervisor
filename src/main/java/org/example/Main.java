package org.example;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.openai.OpenAiChatModel;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;

import org.bsc.langgraph4j.StateGraph;
import org.example.graph.*;

import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;


public class Main {
    public static void main(String[] args) throws Exception {
        var supervisor = new SupervisorAgent(
                OpenAiChatModel.builder()
                        .apiKey(System.getenv("OPENAI_API_KEY"))
                        .modelName(GPT_4_O_MINI)
                        .responseFormat("json_schema")
                        .temperature(0.0)
                        .build()
        );
        var researcher = new ResearchAgent(
                OpenAiChatModel.builder()
                        .apiKey(System.getenv("OPENAI_API_KEY"))
                        .modelName(GPT_4_O_MINI)
                        .responseFormat("json_schema")
                        .temperature(0.5)
                        .strictTools(true)
                        .build()
        );
        var charter = new ChartAgent(
                OpenAiChatModel.builder()
                        .apiKey(System.getenv("OPENAI_API_KEY"))
                        .modelName(GPT_4_O_MINI)
                        .responseFormat("json_schema")
                        .temperature(0.0)
                        .strictTools(true)
                        .build()
        );

//        var toolNode = LC4jToolService.builder()
//                .specification( new WebSearchTool() )
//                .specification( new PythonRunnerTool() )
//                .build();
        var workflow = new StateGraph<>( State.SCHEMA, new StateSerializer() )
                .addNode( "supervisor", node_async(supervisor))
                .addNode( "researcher", node_async(researcher) )
                .addNode( "chartGenerator", node_async(charter) )
//                .addNode( "toolExecutor", node_async(state -> {
//                    var message = state.lastMessage().orElseThrow();
//                    var result = toolNode.execute( ((AiMessage) message).toolExecutionRequests() );
//                    return Map.of( "messages", result, "sender", state.sender() );
//                }) )
                .addEdge( START, "supervisor")
                .addConditionalEdges( "supervisor",
                        edge_async( state ->
                                state.next().orElseThrow()
                        ), Map.of(
                                "FINISH", END,
                                "chartGenerator", "chartGenerator",
                                "researcher", "researcher"
                        ))
                .addEdge( "chartGenerator", "supervisor")
                .addEdge( "researcher", "supervisor")
                ;

        var graph = workflow.compile();

        for( var event : graph.stream( Map.of( "messages", UserMessage.from(
                "Obtain the GDP of the United States from 2000 to 2020, and then plot a line chart with Python. End the task after generating the chart。"
        ))) ) {
            System.out.println(event);
        }
    }
}