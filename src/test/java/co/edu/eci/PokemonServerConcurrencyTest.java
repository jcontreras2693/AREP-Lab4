package co.edu.eci;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import co.edu.eci.http.PokemonServer;

public class PokemonServerConcurrencyTest {
    @BeforeAll
    static void startServer() {
        new Thread(() -> {
            try {
                WebApplication.main(new String[]{});
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {}
    }

    @AfterAll
    static void stopServer() {
        PokemonServer.stop();
    }

    private String sendHttpRequest(String request) throws IOException {
        try (Socket socket = new Socket("localhost", 35000);
             OutputStream out = socket.getOutputStream();
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.write(request.getBytes(StandardCharsets.UTF_8));
            out.flush();

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line).append("\r\n");
            }

            return response.toString();
        }
    }

    @Test
    void testGetPokemon() throws IOException {
        String response = sendHttpRequest("GET /api/pokemon HTTP/1.1\r\nHost: localhost\r\n\r\n");
        //System.out.println("Respuesta recibida: " + response);
        assertTrue(response.contains("200 OK"));
        assertTrue(response.contains("Pikachu"));
    }

    @Test
    void testInvalidPostPokemon() throws IOException {
        String requestBody = "{\"name\": \"Grookey\", \"level\": \"5\"}";
        String request = "POST /api/pokemon HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "Content-Type: application/json\r\n" +
                "Content-Length: " + requestBody.length() + "\r\n\r\n" +
                requestBody;

        String response = sendHttpRequest(request);
        //System.out.println("Respuesta recibida: " + response);
        assertTrue(response.contains("400 Bad Request"));
        assertTrue(response.contains("{\"error\":\"Invalid JSON\"}"));
    }

    @Test
    void testInvalidRoute() throws IOException {
        String response = sendHttpRequest("GET /invalid HTTP/1.1\r\nHost: localhost\r\n\r\n");
        assertTrue(response.contains("404 Not Found"));
    }

    @Test
    void testConcurrentRequests() throws InterruptedException {
        int numThreads = 10;
        Thread[] threads = new Thread[numThreads];

        for (int i = 0; i < numThreads; i++) {
            int finalI = i;
            threads[i] = new Thread(() -> {
                try {
                    String response = sendHttpRequest("GET /greeting?name=Test" + finalI + " HTTP/1.1\r\nHost: localhost\r\n\r\n");

                    //System.out.println("Respuesta recibida: " + response);
                    assertTrue(response.contains("200 OK"));
                    assertTrue(response.contains("Hola, Test" + finalI + "!"));
                } catch (IOException e) {
                    e.printStackTrace();
                    fail("Request failed");
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }
    }
}