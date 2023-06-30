package ex2.server;

import ex2.http.*;

public class Main {
    public static void main(String[] args) {
        HttpServer httpServer = new HttpServer();
        httpServer.run();
    }
}
