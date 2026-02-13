package com.rubenzu03.rag_chatbot.config;

import io.milvus.client.MilvusClient;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.DataType;
import io.milvus.param.ConnectParam;
import io.milvus.param.collection.CreateCollectionParam;
import io.milvus.param.collection.FieldType;
import io.milvus.param.collection.HasCollectionParam;
import io.milvus.param.collection.CollectionSchemaParam;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import io.milvus.param.index.CreateIndexParam;
import io.milvus.param.collection.LoadCollectionParam;

import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(0)
@ConditionalOnExpression("'${VECTOR_DATABASE_HOST:}' != ''")
public class MilvusInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(MilvusInitializer.class);

    @Value("${VECTOR_DATABASE_HOST}")
    private String host;

    @Value("${VECTOR_DATABASE_PORT}")
    private int port;

    @Value("${VECTOR_DATABASE_USERNAME}")
    private String username;

    @Value("${VECTOR_DATABASE_PASSWORD}")
    private String password;

    @Value("${VECTOR_DATABASE_COLLECTION_NAME:chatbot}")
    private String collectionName;

    @Value("${spring.ai.vectorstore.milvus.embedding-dimension:1024}")
    private int dimension;

    @Override
    public void run(ApplicationArguments args){
        log.info("Initializing Milvus collection '{}' on {}:{}", collectionName, host, port);

        MilvusServiceClient milvusClient = null;
        try {
            milvusClient = new MilvusServiceClient(
                    ConnectParam.newBuilder()
                            .withHost(host)
                            .withPort(port)
                            .withAuthorization(username, password)
                            .build()
            );

            log.info("Creating Milvus collection '{}' with dimension {}", collectionName, dimension);

            createFieldsAndSchema(milvusClient);
            createIndex(milvusClient);
            loadCollection(milvusClient);

            log.info("Successfully created and loaded collection '{}'", collectionName);

        } catch (Exception e) {
            log.error("Failed to initialize Milvus collection '{}': {}", collectionName, e.getMessage(), e);
            throw e;
        } finally {
            if (milvusClient != null) {
                milvusClient.close();
            }
        }
    }

    private void createFieldsAndSchema(MilvusClient milvusClient){
        FieldType idField = FieldType.newBuilder()
                .withName("id")
                .withDataType(DataType.Int64)
                .withPrimaryKey(true)
                .withAutoID(true)
                .build();

        FieldType docIdField = FieldType.newBuilder()
                .withName("doc_id")
                .withDataType(DataType.VarChar)
                .withMaxLength(36)
                .build();

        FieldType embeddingField = FieldType.newBuilder()
                .withName("embedding")
                .withDataType(DataType.FloatVector)
                .withDimension(dimension)
                .build();

        FieldType contentField = FieldType.newBuilder()
                .withName("content")
                .withDataType(DataType.VarChar)
                .withMaxLength(65535)
                .build();

        FieldType metadataField = FieldType.newBuilder()
                .withName("metadata")
                .withDataType(DataType.JSON)
                .build();

        CollectionSchemaParam schema = CollectionSchemaParam.newBuilder()
                .withFieldTypes(Arrays.asList(idField, docIdField, embeddingField, contentField, metadataField))
                .build();

        CreateCollectionParam createCollectionParam = CreateCollectionParam.newBuilder()
                .withCollectionName(collectionName)
                .withDescription("RAG chatbot embeddings")
                .withSchema(schema)
                .build();

        milvusClient.createCollection(createCollectionParam);
    }

    private void createIndex(MilvusClient milvusClient){
        milvusClient.createIndex(CreateIndexParam.newBuilder()
                .withCollectionName(collectionName)
                .withFieldName("embedding")
                .withIndexType(IndexType.IVF_FLAT)
                .withMetricType(MetricType.COSINE)
                .withExtraParam("{\"nlist\":1024}")
                .withSyncMode(Boolean.TRUE)
                .build());
    }

    private void loadCollection(MilvusClient milvusClient){
        milvusClient.loadCollection(LoadCollectionParam.newBuilder()
                .withCollectionName(collectionName)
                .build());
    }
}
