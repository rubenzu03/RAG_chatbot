package com.rubenzu03.rag_chatbot.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnvFileEnvironmentPostProcessor implements EnvironmentPostProcessor {
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Logger logger = LoggerFactory.getLogger(EnvFileEnvironmentPostProcessor.class);

        Map<String, Object> propertyMap = new HashMap<>();

        // Try multiple locations for .env file
        Path[] possiblePaths = {
            Paths.get(".env"),                           // Current directory
            Paths.get("../../.env"),                     // Two levels up (for tests)
            Paths.get("../../../.env"),                  // Three levels up
            Paths.get(System.getProperty("user.dir"), ".env")  // User directory
        };

        Path envPath = null;
        for (Path path : possiblePaths) {
            if (Files.exists(path)) {
                envPath = path;
                logger.debug("Found .env file at: {}", path.toAbsolutePath());
                break;
            }
        }

        if (envPath != null) {
            try {
                List<String> lines = Files.readAllLines(envPath);
                for (String rawLine : lines) {
                    String line = rawLine.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    if (line.startsWith("export ")) {
                        line = line.substring(7).trim();
                    }

                    int idx = line.indexOf('=');
                    if (idx <= 0) {
                        continue;
                    }

                    String key = line.substring(0, idx).trim();
                    String value = line.substring(idx + 1).trim();

                    // strip optional surrounding quotes
                    if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
                        value = value.substring(1, value.length() - 1);
                    }

                    propertyMap.put(key, value);
                    logger.info("Loaded env variable: {}=****", key);
                }
            } catch (IOException e) {
                logger.warn("Failed to read .env file: {}", e.getMessage());
            }
        } else {
            logger.debug(".env file not found in any expected location, skipping");
        }

        MapPropertySource propertySource = new MapPropertySource("dotenvProperties", propertyMap);
        environment.getPropertySources().addLast(propertySource);
    }
}