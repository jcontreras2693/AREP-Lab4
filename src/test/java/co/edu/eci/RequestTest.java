package co.edu.eci;

import co.edu.eci.http.Request;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class RequestTest {
    private static Request request;

    @BeforeAll
    public static void setup() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        // Creamos un Request con una URL que contiene parámetros (aunque no los extraemos en Request)
        request = new Request("GET", "/app/greeting?name=Juan", headers, null);
    }

    @Test
    public void shouldRetrieveMethod() {
        assertEquals("GET", request.getMethod());
    }

    @Test
    public void shouldRetrieveFullPath() {
        assertEquals("/app/greeting?name=Juan", request.getPath());
    }

    @Test
    public void shouldRetrieveHeaders() {
        assertEquals("application/json", request.getHeaders().get("Content-Type"));
    }

    @Test
    public void shouldReturnNullForBodyWhenNotProvided() {
        assertNull(request.getBody());
    }
}
