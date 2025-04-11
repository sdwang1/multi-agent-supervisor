package org.example.graph;

import dev.langchain4j.data.message.ChatMessage;
import org.bsc.langgraph4j.langchain4j.serializer.std.ChatMesssageSerializer;
import org.bsc.langgraph4j.langchain4j.serializer.std.ToolExecutionRequestSerializer;
import org.bsc.langgraph4j.serializer.std.ObjectStreamStateSerializer;
import dev.langchain4j.agent.tool.ToolExecutionRequest;

public class StateSerializer extends ObjectStreamStateSerializer<State> {
    public StateSerializer() {
        super(State::new);

        mapper().register(ToolExecutionRequest.class, new ToolExecutionRequestSerializer());
        mapper().register(ChatMessage.class, new ChatMesssageSerializer());
    }
}
