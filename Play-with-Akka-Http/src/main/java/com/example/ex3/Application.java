package com.example.ex3;

import akka.NotUsed;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;
import akka.actor.typed.javadsl.Behaviors;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.*;
import akka.http.javadsl.server.Route;
import akka.stream.*;
import akka.stream.javadsl.*;
import akka.stream.typed.javadsl.*;
import akka.util.*;

import java.math.BigInteger;
import java.time.Duration;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static akka.http.javadsl.server.Directives.*;
import static akka.http.javadsl.server.PathMatchers.*;

public class Application {


    ActorSystem<PrimeDatabaseActor.Command> actorSystem=
            actorSystem = ActorSystem.create(PrimeDatabaseActor.create(), "actorSystem");

    private CompletionStage<Integer> newPrimeNumberRequest() {
        CompletionStage<Integer> requestId = AskPattern.ask(actorSystem,
                me -> new PrimeDatabaseActor.NewRequestCommand(me),
                Duration.ofSeconds(5),
                actorSystem.scheduler());
        return requestId;
    }

    private CompletableFuture<HttpResponse> newUpdateRequest(String requestId) {
        CompletionStage<BigInteger> resultValue = AskPattern.ask(actorSystem,
                me -> new PrimeDatabaseActor
                        .GetResultCommand(Integer.parseInt(requestId), me),
                Duration.ofSeconds(5),
                actorSystem.scheduler());
        CompletableFuture<HttpResponse> response = new CompletableFuture<>();
        resultValue.whenComplete((value, throwable) -> {
            response.complete(HttpResponse.create()
                    .withStatus(200).withEntity(value.toString()));
        });
        return response;
    }

    Flow<Integer, Integer, NotUsed> loggingFlow = Flow.of(Integer.class).map ( id -> {
        System.out.println("Received progress update request for Id " + id);
        return id;
    });

    Flow<Integer, BigInteger, NotUsed> getProgressFlow =
            ActorFlow.ask(actorSystem, Duration.ofSeconds(5),
                    (id, me) -> new PrimeDatabaseActor.GetResultCommand(id, me) );

    private Route newUpdateRequestV2(String requestId) {
        Graph<FlowShape<Integer,ByteString>,NotUsed> partialGraph=GraphDSL.create(builder -> {
            FlowShape<Integer,Integer> loggingFlowShape=builder.add(loggingFlow);
            FlowShape<Integer,BigInteger> getProgressShape = builder.add(getProgressFlow);
            UniformFanOutShape<BigInteger, BigInteger> broadcast = builder.add(Broadcast.create(2));
            FlowShape<BigInteger, BigInteger> filterShape = builder.add(Flow
                    .of(BigInteger.class).filter (i -> !i.equals(BigInteger.ZERO)));
            SinkShape<BigInteger> sinkShape = builder.add(Sink.foreach(System.out::println));
            FlowShape<BigInteger, ByteString> byteStringShape = builder.add(
                    Flow.of(BigInteger.class).map ( i -> ByteString.fromString(i.toString())));
            builder.from(loggingFlowShape).via(getProgressShape)
                    .viaFanOut(broadcast).via(byteStringShape);
            builder.from(broadcast).via(filterShape).to(sinkShape);
            return  FlowShape.of(loggingFlowShape.in(),byteStringShape.out());
        });
        Source<ByteString, NotUsed> source = Source.single(1).map (i -> Integer.parseInt(requestId)).via(partialGraph);
        return complete(HttpEntities.create(ContentTypes.APPLICATION_JSON, source));
    }


    public Route createRoute() {
        return get(() ->
                concat(
                        pathEndOrSingleSlash(() -> {
                                    System.out.println("Received new request");
                                    return onComplete(newPrimeNumberRequest(),
                                            requestId -> complete(requestId.get().toString()));
                                }
                        ),
                        path(segment("result").slash(remaining()), requestId -> {
                            return newUpdateRequestV2(requestId);
                        })
                )
        );
    }


    public void run() {

        Http.get(actorSystem).newServerAt("localhost", 8080).bind(createRoute());

    }
}
