package com.docheck.flow.application.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class OllamaCommandService {

    private static final String OLLAMA_PORT = "11434";

    public boolean isOllamaServerRunning() {
        // This command works on macOS and Linux. It might need adjustment for Windows.
        return executeCommand("lsof -i :" + OLLAMA_PORT).getExitCode() == 0;
    }

    public CommandResult startOllamaServer() {
        log.info("Received request to start Ollama server.");
        if (isOllamaServerRunning()) {
            log.info("Ollama server is already running.");
            CommandResult result = new CommandResult(0);
            result.getStdout().add("Ollama server appears to be already running.");
            return result;
        }
        log.info("Starting Ollama server...");
        // Using "nohup ... &" to detach the process, making it more robust.
        return executeCommand("nohup ollama serve > /dev/null 2>&1 &");
    }

    public CommandResult listOllamaModels() {
        log.info("Executing 'ollama list' command.");
        return executeCommand("ollama list");
    }

    private CommandResult executeCommand(String command) {
        CommandResult commandResult = new CommandResult();
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            // Using sh -c to handle complex commands with pipes or background processes
            processBuilder.command("sh", "-c", command);

            // We don't want to show the user's home directory or other sensitive paths
            processBuilder.environment().put("HOME", "~");
            processBuilder.environment().put("USER", "user");

            Process process = processBuilder.start();

            BufferedReader stdInputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader stdErrorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            String line;
            while ((line = stdInputReader.readLine()) != null) {
                commandResult.getStdout().add(line);
            }
            while ((line = stdErrorReader.readLine()) != null) {
                commandResult.getStderr().add(line);
            }

            // Wait for the process to complete, but with a timeout for safety
            if (process.waitFor(10, TimeUnit.SECONDS)) {
                commandResult.setExitCode(process.exitValue());
            } else {
                commandResult.getStderr().add("Command timed out.");
                commandResult.setExitCode(-1);
                process.destroyForcibly();
            }

        } catch (IOException | InterruptedException e) {
            log.error("Failed to execute command: {}", command, e);
            commandResult.getStderr().add(e.getMessage());
            commandResult.setExitCode(-1);
        }
        return commandResult;
    }

    // Inner class to hold command results
    public static class CommandResult {
        private int exitCode = -1;
        private final List<String> stdout = new ArrayList<>();
        private final List<String> stderr = new ArrayList<>();

        public CommandResult(int exitCode) {
            this.exitCode = exitCode;
        }

        public CommandResult() {}

        // Getters and Setters
        public int getExitCode() { return exitCode; }
        public void setExitCode(int exitCode) { this.exitCode = exitCode; }
        public List<String> getStdout() { return stdout; }
        public List<String> getStderr() { return stderr; }
    }
}
