package com.example.iii.fault;

import akka.actor.typed.*;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;

public class Application2 {



    //------------------------------------------------------------------
    // Protocols(s)
    //------------------------------------------------------------------

    interface Resource { }

    interface Request { }

    interface Response{}

    @AllArgsConstructor
    @Getter
    final static class Get implements Request {
        private String path;
        private ActorRef<Response> replyTo;
    }

    @AllArgsConstructor
    @Getter
    final static class Ok implements Response {
        private String path;
        private Resource resource;
    }

    @AllArgsConstructor
    @Getter
    final static class NotFound implements Response {
        private String path;
    }

    @AllArgsConstructor
    @Getter
    final static class BadRequest implements Response {
        private String path;
    }

    @AllArgsConstructor
    @Getter
    final static class InternalServerError implements Response {
        private String path;
        private String error;
    }

    @AllArgsConstructor
    @Getter
    final static class AdaptedHitResponse implements Request {
        private String path;
        private Resource resource;
        private ActorRef<Response> replyTo;
    }

    @AllArgsConstructor
    @Getter
    final static class AdaptedMissResponse implements Request {
        private String path;
        private ActorRef<Response> replyTo;
    }

    @AllArgsConstructor
    @Getter
    final static class AdaptedErrorResponse implements Request {
        private String path;
        private String error;
        private ActorRef<Response> replyTo;
    }

    @AllArgsConstructor
    @Getter
    final static class File implements Resource {
        private int id;
        private String mimeType;
        private byte[] content;
    }


    interface FileSystemRequest { }

    interface FileSystemResponse{}


    @AllArgsConstructor
    @Getter
    final static class FsFind implements FileSystemRequest{
        private String path;
        private ActorRef<FileSystemResponse> replyTo;
    }

    @AllArgsConstructor
    @Getter
    final static class FsFound implements FileSystemResponse {
        private Resource resource;
    }

    final static class FsNotFound implements FileSystemResponse {
    }




    //------------------------------------------------------------------

    //------------------------------------------------------------------

    public static void main(String[] args) {

            ActorRef<Main.Start> actorRef= ActorSystem.create(Main.create(),"main");
            actorRef.tell(new Main.Start());

    }


    //------------------------------------------------------------------
    // Actor(s)
    //------------------------------------------------------------------


    final static class Main extends AbstractBehavior<Main.Start> {

        final static class Start{}

        public static Behavior<Main.Start> create() {
            return Behaviors.setup(Main::new);
        }

        public Main(ActorContext<Start> context) {
            super(context);
        }

        @Override
        public Receive<Start> createReceive() {
            return newReceiveBuilder()
                    .onMessage(Start.class, message -> {
                        ActorRef<Response> responseHandler = getContext().spawn(ResponseHandler.create(),"responseHandler");
                        ActorRef<Request> webServer = getContext().spawn(WebServer.create(),"webServer");
                        webServer.tell(new Get("restart",responseHandler));
                        return Behaviors.same();
                    })
                    .build();
        }

    }

    final static class ResponseHandler extends AbstractBehavior<Response> {

            public ResponseHandler(ActorContext<Response> context) {
                super(context);
            }

            public static Behavior<Response> create() {
                return Behaviors.setup(ResponseHandler::new);
            }

            @Override
            public Receive<Response> createReceive() {
                return newReceiveBuilder()
                        .onMessage(Ok.class, message -> {
                            getContext().getLog().info("Response: {}", message);
                            return Behaviors.same();
                        })
                        .onMessage(NotFound.class, message -> {
                            getContext().getLog().info("Response: {}", message);
                            return Behaviors.same();
                        })
                        .onMessage(BadRequest.class, message -> {
                            getContext().getLog().info("Response: {}", message);
                            return Behaviors.same();
                        })
                        .onMessage(InternalServerError.class, message -> {
                            getContext().getLog().info("Response: {}", message);
                            return Behaviors.same();
                        })
                        .build();
            }
    }

    final static class WebServer extends AbstractBehavior<Request>{

        public WebServer(ActorContext<Request> context) {
            super(context);
        }

        public static Behavior<Request> create() {
            return Behaviors.setup(context->{

                ActorRef<FsFind> fileSystem = context.spawn(FileSystem.create(),"fileSystem");
                ActorRef<Cache.Request> cache = context.spawn(Cache.create(fileSystem),"cache");

                return Behaviors.supervise(new WebServer(context).onStart(fileSystem,cache))
                        .onFailure(IllegalArgumentException.class,SupervisorStrategy.restart().withStopChildren(false));
            });
        }

        private Behavior<Request> onStart(ActorRef<FsFind> fileSystem, ActorRef<Cache.Request> cache) {
        return newReceiveBuilder()
                .onMessage(Get.class, message -> {
                    // Validation Errors
                    if (message.getPath().equals("")) {
                        message.replyTo.tell(new BadRequest(message.getPath()));
                        return Behaviors.same();
                    }
                    tryToMakeTheServerRestart(message.getPath());
                    tryToMakeTheServerStop(message.getPath());
                    findInCache(cache, getContext(), message.getReplyTo(), message.getPath());
                    return Behaviors.same();
                })
                .onMessage(AdaptedHitResponse.class, message -> {
                    message.getReplyTo().tell(new Ok(message.getPath(),message.getResource()));
                    return Behaviors.same();
                })
                .onMessage(AdaptedMissResponse.class, message -> {
                    message.getReplyTo().tell(new NotFound(message.getPath()));
                    return Behaviors.same();
                })
                .onMessage(AdaptedErrorResponse.class, message -> {
                    message.getReplyTo().tell(new InternalServerError(message.getPath(), message.getError()));
                    return Behaviors.same();
                })
                .build();
        }

        public void tryToMakeTheServerRestart(String path) {
            if (path.contains("restart")) {
                throw new IllegalArgumentException();
            }
        }

        public void tryToMakeTheServerStop(String path) {
            if (path.contains("stop")) {
                getContext().getSystem().terminate();
            }
        }


        public void findInCache(ActorRef<Cache.Request> cache, ActorContext<Request> context, ActorRef<Response> replyTo, String path) {
            context.ask(Cache.Response.class, cache, Duration.ofSeconds(3), (ref) -> new Cache.Find(path, ref), (response, error) -> {
                if (response instanceof Cache.Hit) {
                    return new AdaptedHitResponse(path, ((Cache.Hit) response).getResource(),replyTo);
                } else if (response instanceof Cache.Miss) {
                    return new AdaptedMissResponse(path, replyTo);
                } else {
                    return new AdaptedErrorResponse(path,error.getMessage(),replyTo);
                }
            });
        }

        @Override
        public Receive<Request> createReceive() {
            return null;
        }

    }

    final static class Cache extends AbstractBehavior<Cache.Request> {

        interface Request { }
        interface Response{}

        @AllArgsConstructor
        @Getter
        final static class Find implements Request{
            private String path;
            private ActorRef<Response> replyTo;
        }
        @AllArgsConstructor
        @Getter
        final static class AdaptedFsFound implements Request{
            private String path;
            private Resource resource;
            private ActorRef<Response> replyTo;
        }
        @AllArgsConstructor
        @Getter
        final static class AdaptedFsMiss implements Request {
            private String path;
            private ActorRef<Response> replyTo;
        }
        @AllArgsConstructor
        @Getter
        final static class Hit implements Response {
            private Resource resource;
        }
        @AllArgsConstructor
        @Getter
        final static class Miss implements Response {
        }



        public Cache(ActorContext<Request> context) {
            super(context);
        }

        public static Behavior<Request> create(ActorRef<FsFind> fileSystem) {
            return Behaviors.setup(context->{
                return Behaviors.supervise(new Cache(context).cache(fileSystem,new HashMap<>()))
                        .onFailure(SupervisorStrategy.resume());
            });
        }

        public Receive<Request> cache(ActorRef<FsFind> fileSystem,HashMap<String, Resource> cacheMap) {
            return newReceiveBuilder()
                    .onMessage(Find.class, m->this.onFind(m,fileSystem,cacheMap))
                    .onMessage(AdaptedFsFound.class, m->this.onAdaptedFsFound(m,cacheMap))
                    .onMessage(AdaptedFsMiss.class, m->this.onAdaptedFsMiss(m,cacheMap))
                    .build();
        }

        @Override
        public Receive<Request> createReceive() {
            return null;
        }

        private Behavior<Request> onAdaptedFsMiss(AdaptedFsMiss m, HashMap<String, Resource> cacheMap) {
            m.replyTo.tell(new Miss());
            return Behaviors.same();
        }

        private Behavior<Request> onAdaptedFsFound(AdaptedFsFound m, HashMap<String, Resource> cacheMap) {
            // update cache..
            m.replyTo.tell(new Hit(m.getResource()));
            return Behaviors.same();
        }

        private Behavior<Request> onFind(Find message,ActorRef<FsFind> fileSystem,HashMap<String, Resource> cacheMap) {

//            if (message.getPath().contains("resume"))
//                throw new RuntimeException();

            Resource maybeAnHit = cacheMap.get(message.getPath());
            if(maybeAnHit!=null){
                message.getReplyTo().tell(new Hit(maybeAnHit));
            }
            else{
                askFilesystemForResource(fileSystem,getContext(), message.getPath(),message.getReplyTo());
            }
            return Behaviors.same();
        }

        private void askFilesystemForResource(ActorRef<FsFind> fileSystem,ActorContext<Request> context, String path, ActorRef<Response> replyTo) {
            context.ask(FileSystemResponse.class, fileSystem, Duration.ofSeconds(3), me -> new FsFind(path, me), (response, failed) -> {
                if (response instanceof FsFound) {
                    return new AdaptedFsFound(path, ((FsFound) response).getResource(), replyTo);
                } else {
                    return new AdaptedFsMiss(path, replyTo);
                }
            });
        }


    }

    final static class FileSystem extends AbstractBehavior<FsFind>{

        public FileSystem(ActorContext<FsFind> context) {
            super(context);
        }

        public static Behavior<FsFind> create() {
            return Behaviors.supervise(
                    Behaviors.supervise(Behaviors.setup(FileSystem::new)).onFailure(IOException.class,SupervisorStrategy.restart())
            ).onFailure(Exception.class,SupervisorStrategy.restart().withLimit(3, Duration.ofSeconds(10)));
        }

        @Override
        public Receive<FsFind> createReceive() {
            return newReceiveBuilder()
                    .onMessage(FsFind.class, this::onFind)
                    .onSignal(PreRestart.class, signal -> {
                        getContext().getLog().info("FileSystem restarting");
                        return Behaviors.same();
                    })
                    .onSignal(PostStop.class, signal -> {
                        getContext().getLog().info("FileSystem stopped");
                        return Behaviors.same();
                    })
                    .build();
        }

        private Behavior<FsFind> onFind(FsFind message) {
            if(message.getPath().equals("/file1.txt")) {
                message.getReplyTo().tell(new FsFound(new File(1,"text/plain",new byte[]{})));
            } else {
                message.getReplyTo().tell(new FsNotFound());
            }
            return Behaviors.same();
        }

    }

}
