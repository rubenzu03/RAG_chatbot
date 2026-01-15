package com.rubenzu03.rag_chatbot.persistence;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
public class VectorDatabaseInserter {

    private List<Document> documents;
    private VectorStore vectorStore;

    public List<Document> getAllDocuments(String directoryPath) {
        Path base = Paths.get(directoryPath);
        if (!Files.exists(base)) {
            throw new IllegalArgumentException("Path does not exist: " + directoryPath);
        }

        List<Document> documents = new ArrayList<>();
        for(Path path : listFilesRecursively(base)) {
            try {
                String content = Files.readString(path);
                Document doc = new Document(content, Map.of("source", path.toString(), "filename", path.getFileName().toString()));
                documents.add(doc);
            } catch (Exception e) {
                System.err.println("Failed to read file: " + path + " due to " + e.getMessage());
            }
        }

        this.documents = documents;
        return documents;
    }

    private List<Path> listFilesRecursively(Path directory) {
        List<Path> files = new ArrayList<>();
        try (Stream<Path> stream = Files.walk(directory)) {
            stream.filter(Files::isRegularFile)
                    .forEach(files::add);
        } catch (IOException e) {
            throw new RuntimeException("Failed to list files in directory: " + directory, e);
        }
        return files;
    }
}
