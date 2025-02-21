package co.edu.eci;

import static org.junit.jupiter.api.Assertions.*;

import co.edu.eci.model.Pokemon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PokemonTest {

    private Pokemon pokemon;

    @BeforeEach
    public void setUp() {
        // Crear una instancia de Pokemon antes de cada prueba
        pokemon = new Pokemon("Pikachu", 5);
    }

    @Test
    public void testConstructor() {
        // Arrange: Crear un Pokémon con nombre y nivel
        Pokemon pokemon = new Pokemon("Pikachu", 5);

        // Assert: Verificar que los campos se inicializaron correctamente
        assertEquals("Pikachu", pokemon.getName(), "El nombre debe ser Pikachu");
        assertEquals(5, pokemon.getLevel(), "El nivel debe ser 5");
    }

    @Test
    public void testGetName() {
        // Act y Assert: Verificar que el nombre es el esperado
        assertEquals("Pikachu", pokemon.getName(), "El nombre debe ser Pikachu");
    }

    @Test
    public void testSetName() {
        // Act: Cambiar el nombre
        pokemon.setName("Charmander");

        // Assert: Verificar que el nombre se actualizó correctamente
        assertEquals("Charmander", pokemon.getName(), "El nombre debe ser Charmander");
    }

    @Test
    public void testGetLevel() {
        // Act y Assert: Verificar que el nivel es el esperado
        assertEquals(5, pokemon.getLevel(), "El nivel debe ser 5");
    }

    @Test
    public void testSetLevel() {
        // Act: Cambiar el nivel
        pokemon.setLevel(10);

        // Assert: Verificar que el nivel se actualizó correctamente
        assertEquals(10, pokemon.getLevel(), "El nivel debe ser 10");
    }
}