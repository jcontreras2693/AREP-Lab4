package co.edu.eci.controller;

import co.edu.eci.annotations.GetMapping;
import co.edu.eci.annotations.PostMapping;
import co.edu.eci.annotations.RequestBody;
import co.edu.eci.annotations.RestController;
import co.edu.eci.model.Pokemon;

import java.util.*;

@RestController
public class PokemonController {
    private final List<Pokemon> pokemonTeam = new ArrayList<>();

    public PokemonController() {
        // Se agrega un Pokémon inicial
        pokemonTeam.add(new Pokemon("Pikachu", 5));
    }

    @GetMapping("/api/pokemon")
    public List<Pokemon> getPokemonTeam() {
        return pokemonTeam;
    }

    /**
     *  Código tomado y adaptado de Ana María Durán.
     *
     */
    @PostMapping("/api/pokemon")
    public Map<String, String> addPokemon(@RequestBody Map<String, String> data) {
        if (pokemonTeam.size() >= 6) {
            return Map.of("error", "Equipo completo");
        }

        if (data.containsKey("name") && data.containsKey("level")) {
            try {
                int level = Integer.parseInt(data.get("level"));
                pokemonTeam.add(new Pokemon(data.get("name"), level));
                return Map.of("status", "success");
            } catch (NumberFormatException e) {
                return Map.of("error", "Nivel inválido");
            }
        }

        return Map.of("error", "Faltan campos");
    }
}
