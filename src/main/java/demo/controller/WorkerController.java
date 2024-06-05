package demo.controller;

import demo.model.Worker;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestClient;
import org.springframework.scheduling.annotation.Scheduled;

@Controller
public class WorkerController {
    private String hostname;
    private Worker self;
    private String service;

    // Méthode exécutée après le démarrage de l'application
    @EventListener(ApplicationReadyEvent.class)
    public void afterStartup(){
        // Récupère le type d'application à partir des variables d'environnement
        String appType = System.getenv().get("APP_TYPE");
        System.out.println("APP_TYPE: " + appType); 
        
        // Si APP_TYPE n'est pas "worker", ne pas enregistrer le worker
        if (appType == null || !appType.equals("worker")) {
            System.out.println("ATTENTIONNN!!! Je suis " + appType + " et je ne remplie pas la fonction de worker");
            return;
        }

        // Récupère le hostname et le service à partir des variables d'environnement
        this.hostname = System.getenv().get("HOSTNAME");
        service = System.getenv().get("SERVICE");
        System.out.println("HOSTNAME: " + this.hostname); 

        // Si le hostname est non null, crée un Worker et l'enregistre auprès du registre
        if (this.hostname != null){
            this.self = new Worker(hostname, service);
            RestClient restClient = RestClient.create();
            restClient.post()
                    .uri("http://registery:8081/workers/manifest")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(this.self)
                    .retrieve();
            System.out.println("Worker enregistré: " + this.self.getHostname()); 
        } else {
            System.out.println("Hostname null, il n'y a pas eu de registration");
        }
    }

    // Méthode programmée pour enregistrer périodiquement le worker toutes les 60 secondes
    @Scheduled(fixedRate = 60000)
    public void registerWorker() {
        // Ne fait rien si APP_TYPE n'est pas "worker"
        if (!System.getenv().get("APP_TYPE").equals("worker")) {
            return;
        }
        System.out.println("test");
        RestClient restClient = RestClient.create();
        restClient.post()
                .uri("http://registery:8081/workers/manifest")
                .contentType(MediaType.APPLICATION_JSON)
                .body(this.self).retrieve();
        System.out.println("Worker numéro '" + this.hostname + "' registered.");
    }

    // Endpoint pour gérer les requêtes POST à /hello
    @PostMapping("/hello")
    public ResponseEntity<String> hello(@RequestBody String name) {
        // Ne fait rien si APP_TYPE n'est pas "worker"
        if (!System.getenv().get("APP_TYPE").equals("worker")) {
            return null;
        }
        // Retourne un message de salutation avec le nom reçu en entrée
        String response = "Bonjour mon cher " + name + ", je suis " + hostname + " du service /hello ";
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Endpoint pour gérer les requêtes GET à /chat
    @GetMapping("/chat")
    public ResponseEntity<String> chat() {
        // Retourne un message de chat
        return new ResponseEntity<>("Bonjour je suis le " + hostname + ". Voici le service chat ;)", HttpStatus.OK);
    }
}
