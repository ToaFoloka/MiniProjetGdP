package demo.model;

import jakarta.persistence.*;

@Entity
public class Worker {
    @Id
    private String hostname;
    private int port;
    private static int portIncr = 8000;

    public Worker() {
    }

    public Worker(String hostname) {
        this.hostname = hostname;
        port = portIncr;
        portIncr++;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }
}
