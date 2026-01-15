package com.rubenzu03.rag_chatbot.persistence;


import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.Map;

@Service
public class VectorDatabaseLoader {

    private final VectorStore vectorStore;

    @Autowired
    public VectorDatabaseLoader(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public VectorStore getVectorStore() {
        return vectorStore;
    }



}



