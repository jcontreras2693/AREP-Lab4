package co.edu.eci.http;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import co.edu.eci.model.Pokemon;

public class PokemonServer {
    private static final Map<String, BiFunction<Request, Response, String>> getRoutes = new HashMap<>();
    private static final Map<String, BiFunction<Request, Response, String>> postRoutes = new HashMap<>();

    private static String staticFilesDir = "src/main/resources/web";

    private static final ConcurrentLinkedQueue<Pokemon> pokemonTeam = new ConcurrentLinkedQueue<>();

    private static final int THREADS = 10;
    private static ExecutorService threadPool = Executors.newFixedThreadPool(THREADS);
    private static boolean running = true;

    public static void get(String route, BiFunction<Request, Response, String> handler) {
        if (!route.startsWith("/api/pokemon"))
            getRoutes.put(route, handler);
    }

    public static void post(String route, BiFunction<Request, Response, String> handler) {
        if (!route.startsWith("/api/pokemon"))
            postRoutes.put(route, handler);
    }

    public static void staticFiles(String path) {
        staticFilesDir = path;
    }

    public static void start(int port) throws IOException {
        // Pokémon inicial
        Pokemon pikachu = new Pokemon("Pikachu", 25);
        pokemonTeam.add(pikachu);

        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Servidor iniciado en el puerto " + port);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            handleClient(clientSocket);
            clientSocket.close();
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             OutputStream out = clientSocket.getOutputStream()) {

            String requestLine = in.readLine();
            if (requestLine == null) return;
            String[] requestParts = requestLine.split(" ");
            String method = requestParts[0];
            String path = requestParts[1];

            Map<String, String> headers = new HashMap<>();
            String line;
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                String[] headerParts = line.split(":", 2);
                if (headerParts.length == 2)
                    headers.put(headerParts[0].trim().toLowerCase(), headerParts[1].trim());
            }

            String body = "";
            if ("POST".equalsIgnoreCase(method)) {
                int contentLength = 0;
                if (headers.containsKey("content-length")) {
                    try {
                        contentLength = Integer.parseInt(headers.get("content-length"));
                    } catch (NumberFormatException e) {
                        contentLength = 0;
                    }
                }
                if (contentLength > 0) {
                    char[] bodyChars = new char[contentLength];
                    in.read(bodyChars, 0, contentLength);
                    body = new String(bodyChars);
                }
            }

            Request request = new Request(method, path, headers, body);
            Response response = new Response();

            // Si la ruta es de la API (empieza por /api/pokemon), se procesa internamente
            if (path.startsWith("/api/pokemon")) {
                handleApiRequest(request, response, out);
            } else if ("GET".equalsIgnoreCase(method)) {
                if (getRoutes.containsKey(path)) {
                    String respBody = getRoutes.get(path).apply(request, response);
                    sendResponse(out, response, respBody);
                } else {
                    serveStaticFile(path, out);
                }
            } else if ("POST".equalsIgnoreCase(method)) {
                if (postRoutes.containsKey(path)) {
                    String respBody = postRoutes.get(path).apply(request, response);
                    sendResponse(out, response, respBody);
                } else {
                    sendError(out, 404, "Not Found");
                }
            } else {
                sendError(out, 405, "Method Not Allowed");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleApiRequest(Request request, Response response, OutputStream out) throws IOException {
        String method = request.getMethod();
        if ("GET".equalsIgnoreCase(method)) {
            StringBuilder json = new StringBuilder("[");
            for (Pokemon p : pokemonTeam) {
                json.append(String.format("{\"name\":\"%s\",\"level\":%d},", p.getName(), p.getLevel()));
            }
            if (!pokemonTeam.isEmpty())
                json.deleteCharAt(json.length() - 1);
            json.append("]");
            response.contentType("application/json");
            sendResponse(out, response, json.toString());
        } else if ("POST".equalsIgnoreCase(method)) {
            // Agrega un nuevo Pokémon al equipo
            if (pokemonTeam.size() >= 6) {
                response.status(400);
                response.contentType("application/json");
                sendResponse(out, response, "{\"error\":\"Equipo completo\"}");
                return;
            }
            String jsonBody = request.getBody();
            try {
                // Se espera un JSON con "name" y "level"
                String name = jsonBody.split("\"name\":\"")[1].split("\"")[0];
                String levelStr = jsonBody.split("\"level\":")[1].split("[^0-9]")[0];
                int level = Integer.parseInt(levelStr);
                Pokemon newPokemon = new Pokemon(name, level);
                pokemonTeam.add(newPokemon);
                response.status(201);
                response.contentType("application/json");
                sendResponse(out, response, "{\"status\":\"success\"}");
            } catch (Exception e) {
                response.status(400);
                response.contentType("application/json");
                sendResponse(out, response, "{\"error\":\"Invalid JSON\"}");
            }
        } else {
            sendError(out, 405, "Method Not Allowed");
        }
    }

    private static void sendResponse(OutputStream out, Response response, String body) throws IOException {
        byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
        String header = "HTTP/1.1 " + response.getStatus() + " " + getStatusMessage(response.getStatus()) + "\r\n" +
                        "Content-Type: " + response.getContentType() + "\r\n" +
                        "Content-Length: " + bodyBytes.length + "\r\n" +
                        "\r\n";
        out.write(header.getBytes(StandardCharsets.UTF_8));
        out.write(bodyBytes);
    }

    // Envía una respuesta de error simple
    private static void sendError(OutputStream out, int status, String message) throws IOException {
        String body = "Error: " + message;
        String header = "HTTP/1.1 " + status + " " + getStatusMessage(status) + "\r\n" +
                        "Content-Type: text/plain\r\n" +
                        "Content-Length: " + body.length() + "\r\n" +
                        "\r\n";
        out.write(header.getBytes(StandardCharsets.UTF_8));
        out.write(body.getBytes(StandardCharsets.UTF_8));
    }

    private static String getStatusMessage(int status) {
        switch(status) {
            case 200: return "OK";
            case 201: return "Created";
            case 400: return "Bad Request";
            case 404: return "Not Found";
            case 405: return "Method Not Allowed";
            case 500: return "Internal Server Error";
            default: return "";
        }
    }

    private static void serveStaticFile(String path, OutputStream out) throws IOException {
        if (path.equals("/")) path = "/index.html";
        File file = new File(staticFilesDir + path);
        if (file.exists() && !file.isDirectory()) {
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            String contentType = getMimeType(path);
            String header = "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + contentType + "\r\n" +
                            "Content-Length: " + fileBytes.length + "\r\n" +
                            "\r\n";
            out.write(header.getBytes(StandardCharsets.UTF_8));
            out.write(fileBytes);
        } else {
            sendError(out, 404, "Not Found");
        }
    }

    private static String getMimeType(String path) {
        if (path.endsWith(".html")) return "text/html";
        if (path.endsWith(".css")) return "text/css";
        if (path.endsWith(".js")) return "application/javascript";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        if (path.endsWith(".gif")) return "image/gif";
        return "application/octet-stream";
    }

    public static Map<String, String> parseJson(String json) {
        Map<String, String> map = new HashMap<>();

        if (json == null || json.isEmpty()) {
            return map;
        }

        json = json.trim();

        if (json.startsWith("{") && json.endsWith("}")) {
            json = json.substring(1, json.length() - 1); // Elimina llaves solo si existen
        } else {
            return map;
        }

        String[] pairs = json.split(",");

        for (String pair : pairs) {
            String[] keyValue = pair.split(":", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0].trim().replace("", "");
                String value = keyValue[1].trim().replace("", "");
                map.put(key, value);
            }
        }
        return map;
    }

    public static void stop() {
        running = false;
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
        }
        System.out.println("Server stopped");
    }
}
