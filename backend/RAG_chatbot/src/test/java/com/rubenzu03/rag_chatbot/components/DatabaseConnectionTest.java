package com.rubenzu03.rag_chatbot.components;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class DatabaseConnectionTest {

    @Autowired
    private DataSource dataSource;


    @Test
    public void testConnection() {
        try (Connection connection = dataSource.getConnection()){
            assertNotNull(connection);
            assertTrue(connection.isValid(10));
            assertFalse(connection.isClosed());
        }
        catch (Exception e){
            fail(e);
        }

    }
}
