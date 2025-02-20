package co.edu.eci;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class PokemonServerTest {

    private final String host = "localhost";
    private final int port = 35000;

    /**
     * Prueba la ruta GET registrada (por ejemplo, /hello).
     * Se espera que la respuesta contenga "Hello World!".
     */
    @Test
    public void testGetHello() throws Exception {
        Socket socket = new Socket(host, port);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

        out.print("GET /hello HTTP/1.1\r\n");
        out.print("Host: " + host + "\r\n");
        out.print("\r\n");
        out.flush();

        String statusLine = in.readLine();
        assertNotNull(statusLine);
        assertTrue(statusLine.contains("200"));

        String line;

        StringBuilder body = new StringBuilder();
        while ((line = in.readLine()) != null) {
            body.append(line);
        }

        assertTrue(body.toString().contains("Hello World!"),
                "La respuesta debería contener 'Hello World!'");
        socket.close();
    }

    /**
     * Prueba la ruta GET /pi.
     * Se espera que la respuesta contenga el valor de π.
     */
    @Test
    public void testGetPi() throws Exception {
        Socket socket = new Socket(host, port);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

        out.print("GET /pi HTTP/1.1\r\n");
        out.print("Host: " + host + "\r\n");
        out.print("\r\n");
        out.flush();

        String statusLine = in.readLine();
        assertNotNull(statusLine);
        assertTrue(statusLine.contains("200"));

        String line;
        while ((line = in.readLine()) != null && !line.isEmpty()) {}

        StringBuilder body = new StringBuilder();
        while ((line = in.readLine()) != null) {
            body.append(line);
        }

        assertEquals(String.valueOf(Math.PI), body.toString(),
                "La respuesta debería contener el valor de π");
        socket.close();
    }

    /**
     * Prueba la ruta GET /e.
     * Se espera que la respuesta contenga el valor de e.
     */
    @Test
    public void testGetE() throws Exception {
        Socket socket = new Socket(host, port);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

        out.print("GET /e HTTP/1.1\r\n");
        out.print("Host: " + host + "\r\n");
        out.print("\r\n");
        out.flush();

        String statusLine = in.readLine();
        assertNotNull(statusLine);
        assertTrue(statusLine.contains("200"));

        String line;
        while ((line = in.readLine()) != null && !line.isEmpty()) {}

        StringBuilder body = new StringBuilder();
        while ((line = in.readLine()) != null) {
            body.append(line);
        }

        assertEquals(String.valueOf(Math.E), body.toString(),
                "El body de la respuesta debería contener el valor de e");
        socket.close();
    }

    /**
     * Prueba la ruta POST para /api/pokemon.
     * Se envía un JSON válido y se espera que la respuesta indique éxito.
     */
    @Test
    public void testPostApiPokemon() throws Exception {
        Socket socket = new Socket(host, port);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

        String json = "{\"name\":\"Bulbasaur\",\"level\":7}";
        int contentLength = json.getBytes(StandardCharsets.UTF_8).length;

        out.print("POST /api/pokemon HTTP/1.1\r\n");
        out.print("Host: " + host + "\r\n");
        out.print("Content-Type: application/json\r\n");
        out.print("Content-Length: " + contentLength + "\r\n");
        out.print("\r\n");
        out.print(json);
        out.flush();

        String statusLine = in.readLine();
        assertNotNull(statusLine);
        assertTrue(statusLine.contains("201") || statusLine.contains("200"),
                "El status de la respuesta debe ser 201 o 200");

        String line;

        StringBuilder body = new StringBuilder();
        while ((line = in.readLine()) != null) {
            body.append(line);
        }

        String responseBody = body.toString();
        assertTrue(responseBody.contains("success") || responseBody.contains("Pokemon recibido"),
                "La respuesta debe indicar que el Pokémon fue recibido");
        socket.close();
    }
}
