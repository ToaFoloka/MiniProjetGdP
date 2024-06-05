package demo.controller;

import demo.model.Worker;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Stream;

@RestController
@RequestMapping("/workers")
public class RegisteryController {
    @Autowired
    private WorkerRepository workersRepo;
    private ArrayList<Worker> workersAvailable = new ArrayList<Worker>();

    @Transactional
    @GetMapping()
    public ResponseEntity<Object> getWorkers() {
        Stream<Worker> s = workersRepo.streamAllBy();
        return new ResponseEntity<>(s.toList(), HttpStatus.OK);
    }

    @PostMapping()
    public ResponseEntity<Worker> put(@RequestBody Worker user) {
        workersRepo.save(user);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<Worker> workerIsAvailable(@RequestBody Worker worker) {
        workersRepo.save(worker);
        System.out.println("Ca marche !!! Worker : " + worker.getHostname());
        return new ResponseEntity<>(worker, HttpStatus.OK);
    }

    @Scheduled(fixedDelay = 120000)
    public void sendListToLoadBalancer() {
        System.out.println("Workers dispo : " + workersRepo.toString());
        workersRepo.deleteAll();
        ;
    }

}
