package demo.controller;

import demo.model.Worker;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Controller
public class RegistryController {
    @Autowired
    private WorkerRepository workersRepo;

    @Transactional
    @GetMapping("/registryWorkers")
    public ResponseEntity<Object> getWorkers() {
        Stream<Worker> s = workersRepo.streamAllBy();
        return new ResponseEntity<>(s.toList(), HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<Worker> registerWorker(@RequestBody Worker worker) {
        workersRepo.save(worker);
        System.out.println("Ca marche !!! Worker : " + worker.getHostname());
        return new ResponseEntity<>(worker, HttpStatus.OK);
    }

    @Scheduled(fixedDelay = 30000)
    public ResponseEntity<String> sendListToLoadBalancer() {
        System.out.println("Workers dispo : " + workersRepo.toString());
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "http://loadBalancer:8081/setWorkers";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<List<Worker>> request = new HttpEntity<>(this.workersRepo.streamAllBy().toList(), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request,
                    String.class);

            workersRepo.deleteAll();
            return new ResponseEntity<>(response.getStatusCode());
        } catch (Exception e) {
            System.out.println("Failed to register worker.");
        }
        return null;
    }

}
