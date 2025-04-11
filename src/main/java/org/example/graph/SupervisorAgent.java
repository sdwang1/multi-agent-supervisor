package org.example.graph;

import dev.langchain4j.model.output.structured.Description;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.bsc.langgraph4j.action.NodeAction;
import static java.lang.String.format;

import java.util.Map;

public class SupervisorAgent implements NodeAction<State> {
    private final Service service;

    static class Router {
        @Description("Worker to route to next. If no workers needed, route to FINISH.")
        String next;

        @Override
        public String toString() {
            return format( "Router[next: %s]",next);
        }
    }

    interface Service {
        @SystemMessage( """
                You are a supervisor tasked with managing a conversation between the following workers: researcher, chartGenerator.
                Given the following user request, respond with the worker to act next.
                Each worker will perform a task and respond with their results and status.
                When finished, respond with FINISH.
                """)
        Router evaluate(@dev.langchain4j.service.UserMessage String userMessage);
    }

    public SupervisorAgent(ChatLanguageModel model ) {
        service = AiServices.create( Service.class, model );
    }

    @Override
    public Map<String, Object> apply(State state) throws Exception {

        var message = state.lastMessage().orElseThrow();

        var text = switch( message.type() ) {
            case USER -> ((UserMessage)message).singleText();
            case AI -> ((AiMessage)message).text();
            default -> throw new IllegalStateException("unexpected message type: " + message.type() );
        };

        var result = service.evaluate(text );

        return Map.of( "next", result.next );
    }
}
