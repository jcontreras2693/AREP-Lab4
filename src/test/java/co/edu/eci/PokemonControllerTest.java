package co.edu.eci;

import static org.junit.jupiter.api.Assertions.*;

import co.edu.eci.controller.PokemonController;
import co.edu.eci.model.Pokemon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

public class PokemonControllerTest {

    private PokemonController pokemonController;

    @BeforeEach
    public void setUp() {
        pokemonController = new PokemonController();
    }

    @Test
    public void testGetPokemonTeam() {
        List<Pokemon> team = pokemonController.getPokemonTeam();

        assertNotNull(team, "La lista de Pokémon no debe ser nula");
        assertEquals(1, team.size(), "El equipo debe tener un Pokémon inicial");
        assertEquals("Pikachu", team.get(0).getName(), "El Pokémon inicial debe ser Pikachu");
        assertEquals(5, team.get(0).getLevel(), "El nivel de Pikachu debe ser 5");
    }

    @Test
    public void testAddPokemonValid() {
        Map<String, String> data = Map.of("name", "Charmander", "level", "10");

        Map<String, String> response = pokemonController.addPokemon(data);

        assertEquals("success", response.get("status"), "El Pokémon debe añadirse correctamente");
        assertEquals(2, pokemonController.getPokemonTeam().size(), "El equipo debe tener 2 Pokémon");
    }

    @Test
    public void testAddPokemonInvalidLevel() {
        Map<String, String> data = Map.of("name", "Squirtle", "level", "not-a-number");

        Map<String, String> response = pokemonController.addPokemon(data);

        assertEquals("Nivel inválido", response.get("error"), "Debe devolver un error por nivel inválido");
        assertEquals(1, pokemonController.getPokemonTeam().size(), "El equipo no debe cambiar");
    }

    @Test
    public void testAddPokemonMissingFields() {
        Map<String, String> data = Map.of("name", "Bulbasaur");

        Map<String, String> response = pokemonController.addPokemon(data);

        assertEquals("Faltan campos", response.get("error"), "Debe devolver un error por campos faltantes");
        assertEquals(1, pokemonController.getPokemonTeam().size(), "El equipo no debe cambiar");
    }

    @Test
    public void testAddPokemonTeamFull() {
        for (int i = 0; i < 5; i++) {
            pokemonController.addPokemon(Map.of("name", "Pokemon" + i, "level", "5"));
        }

        Map<String, String> response = pokemonController.addPokemon(Map.of("name", "Mewtwo", "level", "70"));

        assertEquals("Equipo completo", response.get("error"), "Debe devolver un error por equipo lleno");
        assertEquals(6, pokemonController.getPokemonTeam().size(), "El equipo debe tener 6 Pokémon");
    }
}