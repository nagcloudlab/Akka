package com.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Scheduler;
import akka.actor.typed.javadsl.AskPattern;
import akka.actor.typed.javadsl.GroupRouter;
import akka.actor.typed.javadsl.PoolRouter;
import akka.actor.typed.javadsl.Routers;
import akka.actor.typed.receptionist.Receptionist;
import akka.actor.typed.receptionist.ServiceKey;
import com.example.actor.PaymentActor;
import com.example.actor.PrintActor;
import com.example.actor.VerificationActor;
import com.example.db.FakeDB;
import com.example.msg.*;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

public class Application {
    public static void main(String[] args) {

//        final ActorSystem<PrintMsg> printActorRef = ActorSystem.create(PrintActor.create(),"printActor");
//        printActorRef.tell(new PrintStringMsg("hello"));
//        printActorRef.tell(new PrintIntegerMsg(123));

        FakeDB.balanceSheet.put(1, 100.0);

        ActorRef<VerificationQuery> verificationActorWorker=ActorSystem.create(VerificationActor.create(),"verificationActorWorker");
        ServiceKey<VerificationQuery> serviceKey=ServiceKey.create(VerificationQuery.class,"verification-key");

        // GroupRouter
        GroupRouter<VerificationQuery> groupRouter=Routers.group(serviceKey)
                .withConsistentHashingRouting(10, VerificationQuery::consistentHashKey);

        // Verification  Actor ,  Handling  the race condition, single actor
        final ActorSystem<VerificationQuery> verificationActor = ActorSystem.create(groupRouter,"verificationActor");
        verificationActor.receptionist().tell(Receptionist.register(serviceKey,verificationActorWorker));

        // PoolRouter
        PoolRouter<PaymentCommand> poolRouter2= Routers.pool(10,PaymentActor.create(verificationActor));

        // Payment  Actor ( single actor )
        //final ActorSystem<PaymentCommand> paymentActor = ActorSystem.create(PaymentActor.create(verificationActor),"paymentActor");
        final ActorSystem<PaymentCommand> paymentActor =
                 ActorSystem.create(poolRouter2.withRoundRobinRouting(),"paymentActor");
                //ActorSystem.create(poolRouter.withRandomRouting(),"paymentActor");
                //ActorSystem.create(poolRouter.withBroadcastPredicate(msg->msg instanceof MobilePaymentRequest),"paymentActor");
//
//        paymentActor.tell(new OnlinePaymentRequest(1,10.00));
//        paymentActor.tell(new MobilePaymentRequest(1,10.00));
//        paymentActor.tell(new MobilePaymentRequest(1,10.00));

        for (int i = 0; i < 10; i++) {
            paymentActor.tell(new MobilePaymentRequest(1,25.00));
        }
        // askPatternSample(verificationActor,paymentActor.scheduler(),1,1000.00);
    }


    // Ask Pattern
    public static void askPatternSample(ActorRef<VerificationQuery> verificationActor, Scheduler scheduler,int userId, double amount) {
        CompletionStage<PaymentCommand> ask=AskPattern.ask(
                verificationActor, // 1st param : target actor
                sender-> new VerificationQuery(sender,userId,amount), // 2nd param : message
                Duration.ofSeconds(3), // 3rd param : timeout
                scheduler // 4th param : scheduler
        );

        ask.whenComplete((reply,failure)->{
            VerificationResponse response=(VerificationResponse) reply;
            if(response.isVerified()) {
                System.out.println("Payment verified "+String.valueOf(response.getAmount()));
            }
            else{
                System.out.println("Payment rejected "+String.valueOf(response.getText()));
            }
        });
    }



}
