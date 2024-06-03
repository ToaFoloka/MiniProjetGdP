package demo.controller;

import demo.model.Worker;

import java.util.List;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
public class WorkerController {
    private String hostname;
    private Worker self;
    private int test = 0;

    @EventListener(ApplicationReadyEvent.class)
    public void afterStartup() {
        this.hostname = System.getenv().get("HOSTNAME");
        if (this.hostname != null) {
            this.self = new Worker(hostname);
            RestClient restClient = RestClient.create();
            restClient.post()
                    .uri("http://registery:8081/workers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(this.self).retrieve();
        }
    }

    @Scheduled(fixedDelay = 1000)
    public void incrementTest() {
        test++;
    }

    @Scheduled(fixedDelay = 5000)
    public ResponseEntity<String> isAvailable() {
        RestClient restClient = RestClient.create();

        String uri = "http://registery:8081/workers?worker=" + hostname;
        String rw = restClient.post().uri(uri).retrieve().body(String.class);
        System.out.println("Envoie de la donnée au registery");
        return new ResponseEntity<>(rw, HttpStatus.OK);
    }

    @GetMapping("/hello1")
    public ResponseEntity<String> hello1() {
        return new ResponseEntity<>("Hello\n" + hostname + " test = " + test, HttpStatus.OK);
    }

}
