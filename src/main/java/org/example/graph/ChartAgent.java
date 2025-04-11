package org.example.graph;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import org.bsc.langgraph4j.action.NodeAction;
import org.example.graph.tools.PythonRunnerTool;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChartAgent implements NodeAction<State> {
    private final Service service;
    private final List<ToolSpecification> specifications = new ArrayList<>();

    interface Service {
            @SystemMessage( """
                You are a helpful AI assistant, collaborating with other assistants."
                " Use the provided tools to progress towards answering the question."
                " If you are unable to fully answer, that's OK, another assistant with different tools "
                " will help where you left off. Execute what you can to make progress."
                " If you or any of the other assistants have the final answer or deliverable,"
                " prefix your response with FINISH so the team knows to stop."
                "
                "Notice:
                "
                "If you have completed all tasks, respond with FINISH."
                "
                " You have access to the following tools: pythonRepl.
                "Create clear and user-friendly charts based on the provided data."
                
                
                """)
//        @dev.langchain4j.service.UserMessage("{{messages}}")
            AiMessage execute(@dev.langchain4j.service.UserMessage String code);
    }

    public ChartAgent( ChatLanguageModel model) {
        service = AiServices.builder( Service.class )
                .chatLanguageModel(model)
                .tools(new PythonRunnerTool())
                .build();
    }

    @Override
    public Map<String, Object> apply(State state) {
        var message = state.lastMessage().orElseThrow();

        var text = switch( message.type() ) {
            case USER -> ((UserMessage)message).singleText();
            case AI -> ((AiMessage)message).text();
            default -> throw new IllegalStateException("unexpected message type: " + message.type() );
        };
        var result = service.execute( text );
        return Map.of( "messages", result );
    }
}
