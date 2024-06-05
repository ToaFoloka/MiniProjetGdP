package demo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import demo.model.Worker;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Controller
public class LoadBalancer {
    private List<Worker> workers = new ArrayList<>();

    private int index = 0;

    @GetMapping("/hello")
    public ResponseEntity<String> hello() throws JsonMappingException, JsonProcessingException {
        RestClient restClient = RestClient.create();
        String r = restClient.get().uri("http://registry:8081/registryWorkers")
                .retrieve().body(String.class);
        ObjectMapper mapper = new ObjectMapper();
        this.workers = mapper.readValue(r, new TypeReference<List<Worker>>() {
        });

        this.index = (this.index + 1) % this.workers.size();
        String uri = "http://" + this.workers.get(this.index).getHostname() + ":8081/hello1";
        String rw = restClient.get().uri(uri).retrieve().body(String.class);

        return new ResponseEntity<>(rw, HttpStatus.OK);
    }

    @PostMapping("/setWorkers")
    public ResponseEntity<List<Worker>> postWorkersAvailable(@RequestBody List<Worker> workersRegistered) {
        workers = workersRegistered;
        return new ResponseEntity<>(workers, HttpStatus.OK);
    }

    @GetMapping("/workers")
    public ResponseEntity<List<Worker>> getWorkers() {
        return new ResponseEntity<>(workers, HttpStatus.OK);
    }

}
