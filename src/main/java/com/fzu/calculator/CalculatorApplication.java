package com.fzu.calculator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the calculator backend service.
 */
@SpringBootApplication
public class CalculatorApplication {

    private static final Logger log = LoggerFactory.getLogger(CalculatorApplication.class);

    /** Fallback used when neither the command line nor application.yml defines a database path. */
    private static final String DEFAULT_DATABASE_PATH = "./data/calculator.db";

    public static void main(String[] args) {
        createDatabaseDirectoryIfNeeded(args);
        SpringApplication.run(CalculatorApplication.class, args);
    }

    /**
     * SQLite creates the database file itself but never creates missing directories,
     * therefore the parent directory of the configured database path is prepared before
     * the DataSource is created.
     *
     * <p>The path is resolved from the sources Spring Boot itself would use, in decreasing
     * priority: the {@code --app.database.path=...} command line argument, the
     * {@code -Dapp.database.path=...} system property, and finally the value declared in
     * {@code application.yml}. Reading only the system property would be a bug, because the
     * shipped configuration is expressed in {@code application.yml}.</p>
     */
    private static void createDatabaseDirectoryIfNeeded(String[] args) {
        String path = resolveDatabasePath(args);
        try {
            Path parent = Paths.get(path).toAbsolutePath().getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
                log.info("Created database directory {}", parent);
            }
        } catch (Exception ex) {
            log.warn("Could not pre-create the database directory: {}", ex.getMessage());
        }
    }

    /**
     * Resolves the SQLite file path the same way Spring Boot would.
     *
     * @param args raw command line arguments
     * @return the database path, never {@code null}
     */
    private static String resolveDatabasePath(String[] args) {
        if (args != null) {
            for (String arg : args) {
                String value = readAssignment(arg, "--app.database.path=");
                if (value != null) {
                    return value;
                }
            }
        }
        String fromSystemProperty = System.getProperty("app.database.path");
        if (fromSystemProperty != null && !fromSystemProperty.trim().isEmpty()) {
            return fromSystemProperty.trim();
        }
        String fromConfiguration = readPathFromApplicationYml();
        return fromConfiguration != null ? fromConfiguration : DEFAULT_DATABASE_PATH;
    }

    private static String readAssignment(String arg, String prefix) {
        if (arg == null || !arg.startsWith(prefix)) {
            return null;
        }
        String value = arg.substring(prefix.length()).trim();
        return value.isEmpty() ? null : value;
    }

    /**
     * Minimal reader for the {@code app.database.path} entry of the packaged
     * {@code application.yml}; it avoids starting a Spring context just to learn one string.
     */
    private static String readPathFromApplicationYml() {
        InputStream stream = CalculatorApplication.class.getResourceAsStream("/application.yml");
        if (stream == null) {
            return null;
        }
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            int databaseIndent = -1;
            int pathIndent = -1;
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                int indent = line.indexOf(trimmed.charAt(0));

                if (trimmed.equals("database:")) {
                    databaseIndent = indent;
                    pathIndent = -1;
                    continue;
                }
                if (databaseIndent >= 0 && trimmed.startsWith("path:")) {
                    pathIndent = indent;
                    String value = stripQuotes(trimmed.substring("path:".length()).trim());
                    if (!value.isEmpty()) {
                        return value;
                    }
                    continue;
                }
                // Leaving the "database:" block: any key at the same or lower indentation.
                if (databaseIndent >= 0 && indent <= databaseIndent
                        && (pathIndent < 0 || indent <= pathIndent)) {
                    databaseIndent = -1;
                }
            }
        } catch (IOException ex) {
            log.warn("Could not read app.database.path from application.yml: {}", ex.getMessage());
        }
        return null;
    }

    /** Removes the optional surrounding quotes that YAML allows around a scalar value. */
    private static String stripQuotes(String value) {
        if (value.length() >= 2) {
            char first = value.charAt(0);
            char last = value.charAt(value.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return value.substring(1, value.length() - 1).trim();
            }
        }
        return value;
    }
}
