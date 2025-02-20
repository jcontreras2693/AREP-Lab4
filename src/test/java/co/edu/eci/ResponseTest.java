package co.edu.eci;

import co.edu.eci.http.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ResponseTest {
    private Response response;

    @BeforeEach
    public void setup() {
        response = new Response();
    }

    @Test
    public void shouldReturnDefaultStatus() {
        assertEquals(200, response.getStatus());
    }

    @Test
    public void shouldSetCustomStatus() {
        response.status(404);
        assertEquals(404, response.getStatus());
    }

    @Test
    public void shouldReturnDefaultContentType() {
        assertEquals("text/plain", response.getContentType());
    }

    @Test
    public void shouldSetCustomContentType() {
        response.contentType("application/json");
        assertEquals("application/json", response.getContentType());
    }
}
