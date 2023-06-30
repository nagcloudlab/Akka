package com.example.ex2.server;

import com.example.ex2.http.*;

public class Main {
    public static void main(String[] args) {
        HttpServer httpServer = new HttpServer();
        httpServer.run();
    }
}
