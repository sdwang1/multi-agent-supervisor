package org.example.graph.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class PythonRunnerTool {
    @Tool("Use this to execute python code. If you want to see the output of a value," +
            "you should print it out with `print(...)`. This is visible to the user.")
    void pythonRepl(@P("The python code to execute to generate your chart.") String code) {
        ProcessBuilder pb = new ProcessBuilder("python3", "-c", code);

        try {
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                System.out.println("Error: " + errorLine);
            }
            int exitCode = process.waitFor();
            System.out.println("Exited with code: " + exitCode);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
