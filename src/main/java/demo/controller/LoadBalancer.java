package demo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import demo.model.Worker;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@CrossOrigin
@Controller
public class LoadBalancer {
    private List<Worker> hello_workers = new ArrayList<>();
    private List<Worker> chat_workers = new ArrayList<>();
    private int index = 0;

    // Endpoint pour gérer les requêtes GET à /service/hello/{name}
    @GetMapping("/service/hello/{name}")
    public ResponseEntity<String> hello(@PathVariable String name) throws JsonMappingException, JsonProcessingException {
        // Vérifie si l'application fonctionne comme un load balancer
        if (!isLoadBalancer()) {
            return null;
        }

        // Vérifie la disponibilité des workers pour le service hello
        if (hello_workers == null || hello_workers.isEmpty()) {
            return new ResponseEntity<>("Pas de workers disponibles en ce moment", HttpStatus.SERVICE_UNAVAILABLE);
        }

        // Réinitialise l'index si nécessaire et sélectionne un worker au hasard
        if (index >= hello_workers.size()) {
            index = 0;
        }
        Worker worker = hello_workers.get(index);
        Random rand = new Random();
        index = rand.nextInt(hello_workers.size());

        // Envoie une requête POST au worker sélectionné
        RestClient restClient = RestClient.create();
        String result = restClient.post()
                .uri("http://" + worker.getHostname() + ":8081/hello")
                .contentType(MediaType.APPLICATION_JSON)
                .body(name)
                .retrieve()
                .body(String.class);
        System.out.println("Envoyé à " + worker.getHostname());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    // Endpoint pour gérer les requêtes GET à /service/chat
    @GetMapping("/service/chat")
    public ResponseEntity<String> chat() {
        // Vérifie si l'application fonctionne comme un load balancer
        if (!isLoadBalancer()) {
            return null;
        }

        // Vérifie la disponibilité des workers pour le service chat
        if (chat_workers == null || chat_workers.isEmpty()) {
            return new ResponseEntity<>("Pas de workers disponibles en ce moment", HttpStatus.SERVICE_UNAVAILABLE);
        }

        // Réinitialise l'index si nécessaire et sélectionne un worker au hasard
        if (index >= chat_workers.size()) {
            index = 0;
        }
        Worker worker = chat_workers.get(index);
        Random rand = new Random();
        index = rand.nextInt(chat_workers.size());

        // Envoie une requête GET au worker sélectionné
        RestClient restClient = RestClient.create();
        String result = restClient.get()
                .uri("http://" + worker.getHostname() + ":8081/chat")
                .retrieve()
                .body(String.class);
        System.out.println("Envoyé à " + worker.getHostname());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    // Endpoint pour gérer les requêtes POST à /postworkers
    @PostMapping("/postworkers")
    public ResponseEntity<String> postWorkers(@RequestBody List<Worker> workers) {
        // Vérifie si l'application fonctionne comme un load balancer
        if (!isLoadBalancer()) {
            return null;
        }

        // Met à jour les listes de workers pour hello et chat
        hello_workers.clear();
        chat_workers.clear();
        for (Worker w : workers) {
            if (w.getService().equals("hello")) {
                hello_workers.add(w);
            } else if (w.getService().equals("chat")) {
                chat_workers.add(w);
            }
        }
        System.out.println("Les workers ont été mis à jour");
        return new ResponseEntity<>("Les workers ont été mis à jour", HttpStatus.OK);
    }

    // Vérifie si l'application est configurée pour fonctionner comme un load balancer
    public boolean isLoadBalancer() {
        return System.getenv().get("APP_TYPE").equals("loadbalancer");
    }
}
