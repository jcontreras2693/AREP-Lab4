package co.edu.eci.controller;

import co.edu.eci.annotations.GetMapping;
import co.edu.eci.annotations.RequestParam;
import co.edu.eci.annotations.RestController;

@RestController
public class ServerController {
    @GetMapping("/hello")
    public String hello() {return "Hello World!";}

    @GetMapping("/greeting")
    public String greeting(@RequestParam(value = "name", defaultValue = "World") String name){
        return "Hello " + name;
    }

    @GetMapping("/pi")
    public String pi() {return Double.toString(Math.PI);}

    @GetMapping("/e")
    public String e() {return Double.toString(Math.E);}
}
