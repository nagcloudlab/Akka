package com.example.ex1;

import akka.Done;
import akka.NotUsed;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import akka.http.javadsl.Http;
import akka.http.javadsl.IncomingConnection;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.japi.function.Function;
import akka.stream.Materializer;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

import java.util.concurrent.CompletionStage;

public class SimpleSyncHttpServer {
    public  void run() {

        ActorSystem actorSystem = ActorSystem.create(Behaviors.empty(), "simpleServer");

        Source<IncomingConnection, CompletionStage<ServerBinding>> source =
                Http.get(actorSystem).newServerAt("localhost", 8080).connectionSource();

        Flow<IncomingConnection, IncomingConnection, NotUsed> flow =
                Flow.of(IncomingConnection.class).map(connection -> {
                    System.out.println("Incoming connection from " + connection.remoteAddress().toString());
                    connection.handleWithSyncHandler(httpRequest -> {
                        System.out.println("The headers are ");
                        httpRequest.getHeaders()
                                .forEach(header -> System.out.println(header.name() + " : " + header.value()));
                        System.out.println("The URI was " + httpRequest.getUri().getPathString());
                        if (httpRequest.getUri().rawQueryString().isPresent()) {
                            System.out.println("The query string was " + httpRequest.getUri().rawQueryString().get());
                        }
                        httpRequest.discardEntityBytes(actorSystem);
                        return HttpResponse.create()
                                .withStatus(StatusCodes.OK).withEntity("The data was received");
                    }, Materializer.createMaterializer(actorSystem));
                    return connection;
                });

        Sink<IncomingConnection, CompletionStage<Done>> sink = Sink.ignore();

        CompletionStage<ServerBinding> server = source.via(flow).to(sink).run(actorSystem);
        server.whenComplete((binding, throwable) -> {
            if (throwable != null) {
                System.out.println("Something went wrong " + throwable);
            } else {
                System.out.println("The server is running at " + binding.localAddress());
            }
        });

    }
}
