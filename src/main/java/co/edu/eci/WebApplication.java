package co.edu.eci;

import co.edu.eci.annotations.GetMapping;
import co.edu.eci.annotations.PostMapping;
import co.edu.eci.annotations.RestController;
import org.reflections.Reflections;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import co.edu.eci.http.PokemonServer;
import co.edu.eci.http.Request;
import java.lang.reflect.Parameter;
import java.lang.reflect.InvocationTargetException;

import co.edu.eci.annotations.RequestBody;
import co.edu.eci.annotations.RequestParam;

public class WebApplication {
    public static Map<String, Method> services = new HashMap();
    public static Map<String, Object> controllers = new HashMap<>();

    public static void main(String[] args) throws Exception {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Deteniendo elegantemente...");
            PokemonServer.stop();
        }));
        System.out.println("Iniciando WebApplication en forma de Mini SpringBoot...");
        loadComponents();
        PokemonServer.staticFiles("src/main/resources/static");
        PokemonServer.start(35000);
    }

    /**
     * Código tomado y adaptado de Cristian Javier Alvarez.
     */
    public static void loadComponents() {
        try {
            // Explorar el classpath en busca de clases anotadas con @RestController
            for (Class<?> clazz : findAllClasses()) {
                if (clazz.isAnnotationPresent(RestController.class)) {
                    System.out.println("RestController found: " + clazz.getName());
                    Object controllerInstance = clazz.getDeclaredConstructor().newInstance();

                    for (Method method : clazz.getDeclaredMethods()) {
                        if (method.isAnnotationPresent(GetMapping.class)) {
                            GetMapping mapping = method.getAnnotation(GetMapping.class);
                            String path = mapping.value();
                            PokemonServer.get(path, (req, res) -> invokeMethod(method, controllerInstance, req));
                            System.out.println("Registering GET route: " + mapping.value());
                            services.put(mapping.value(), method);
                            controllers.put(mapping.value(), controllerInstance);
                        }
                        if (method.isAnnotationPresent(PostMapping.class)) {
                            PostMapping mapping = method.getAnnotation(PostMapping.class);
                            String path = mapping.value();
                            PokemonServer.post(path, (req, res) -> invokeMethod(method, controllerInstance, req));
                            System.out.println("Registering POST route: " + mapping.value());
                            services.put(mapping.value(), method);
                            controllers.put(mapping.value(), controllerInstance);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error loading components", e);
        }
    }

    private static Iterable<Class<?>> findAllClasses() {
        Reflections reflections = new Reflections("co.edu.eci");
        return reflections.getTypesAnnotatedWith(RestController.class);
    }

    private static String invokeMethod(Method method, Object instance, Request req) {
        try {
            Parameter[] parameters = method.getParameters();
            Object[] argsForMethod = new Object[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];
                if (parameter.isAnnotationPresent(RequestBody.class)) {
                    if (Map.class.isAssignableFrom(parameter.getType())) {
                        argsForMethod[i] = PokemonServer.parseJson(req.getBody());
                    } else {
                        argsForMethod[i] = req.getBody();
                    }
                } else if (parameter.isAnnotationPresent(RequestParam.class)) {
                    RequestParam reqParam = parameter.getAnnotation(RequestParam.class);
                    String paramName = reqParam.value();
                    String value = req.getHeaders().get(paramName);
                    if (value == null || value.isEmpty()) {
                        value = reqParam.defaultValue();
                    }
                    argsForMethod[i] = value;
                } else {
                    argsForMethod[i] = null;
                }
            }
            Object result = method.invoke(instance, argsForMethod);
            return result != null ? result.toString() : "";
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return "{\"error\": \"Internal Server Error\"}";
        }
    }
}
