package demo.controller;

import demo.model.Worker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@RestController
@RequestMapping("/workers")
public class RegisteryController {

    @Autowired
    private WorkerRepository workersRepo;

    // Endpoint pour obtenir la liste des workers enregistrés
    @GetMapping()
    public ResponseEntity<Object> getUsers() {
        // Vérifie si l'application est configurée comme un registre
        if (!isRegistry()) {
            return null;
        }
        // Récupère tous les workers enregistrés dans la base de données et les retourne
        Stream<Worker> s = workersRepo.streamAllBy();
        return new ResponseEntity<>(s.toList(), HttpStatus.OK);
    }

    // Endpoint pour recevoir le manifeste d'un worker
    @Transactional
    @PostMapping("/manifest")
    public ResponseEntity<String> manifest(@RequestBody Worker worker) {
        // Vérifie si l'application est configurée comme un registre
        if (!isRegistry()) {
            return null;
        }
        System.out.println("Manifestation reçue de '" + worker.getHostname() + "'.");
        // Vérifie si le worker est déjà enregistré dans la base de données
        Optional<Worker> existingWorker = workersRepo.findById(worker.getHostname());
        if (existingWorker.isPresent()) {
            // Met à jour la date de la dernière manifestation s'il existe déjà
            existingWorker.get().setLastManifestTime(LocalDateTime.now());
            workersRepo.save(existingWorker.get());
            System.out.println("Worker déjà enregistré. Mise à jour de la date de la dernière manifestation.");
        } else {
            // Ajoute le worker à la base de données s'il n'existe pas déjà
            worker.setLastManifestTime(LocalDateTime.now());
            workersRepo.save(worker);
            System.out.println(worker.getHostname() + " enregistré dans la base de données.");
        }
        // Envoie la liste mise à jour des workers au load balancer
        sendWorkersList();
        return new ResponseEntity<>("Manifestation reçue", HttpStatus.OK);
    }

    // Tâche planifiée pour envoyer périodiquement la liste des workers au load balancer
    @Transactional
    @Scheduled(fixedRate = 120000)
    public void sendWorkersList() {
        // Vérifie si l'application est configurée comme un registre
        if (!isRegistry()) {
            return;
        }
        // Récupère la liste des workers enregistrés dans la base de données
        List<Worker> workers = workersRepo.streamAllBy().toList();
        // Vérifie les workers non réactifs et les supprime de la base de données
        checkUnresponsiveWorkers(workers);
        // Envoie la liste mise à jour des workers au load balancer
        RestClient restClient = RestClient.create();
        restClient.post()
                .uri("http://loadbalancer:8081/postworkers")
                .contentType(MediaType.APPLICATION_JSON)
                .body(workers)
                .retrieve();
        System.out.println("Liste des workers envoyée au load balancer.");
    }

    // Méthode pour vérifier les workers non réactifs
    public void checkUnresponsiveWorkers(List<Worker> allWorkers) {
        for (Worker worker : allWorkers) {
            if (isUnresponsive(worker)) {
                // Supprime les workers non réactifs de la base de données
                workersRepo.delete(worker);
                System.out.println("Worker '" + worker.getHostname() + "' non réactif. Supprimé de la base de données.");
            }
        }
    }

    // Méthode pour déterminer si un worker est non réactif
    private boolean isUnresponsive(Worker worker) {
        LocalDateTime lastManifestTime = worker.getLastManifestTime();
        return lastManifestTime.isBefore(LocalDateTime.now().minusMinutes(2));
    }

    // Méthode pour vérifier si l'application est configurée comme un registre
    public boolean isRegistry() {
        return System.getenv().get("APP_TYPE").equals("registery");
    }
}
