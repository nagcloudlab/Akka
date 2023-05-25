package com.example.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.pattern.Patterns;
import com.example.messages.PaymentRequestMsg;
import com.example.messages.PaymentResponseMsg;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class OnlinePaymentActor extends AbstractActor {


    private final ActorRef paymentActor;

    public OnlinePaymentActor(ActorRef paymentActor){
        this.paymentActor=paymentActor;
    }

    public static Props props(ActorRef paymentActor){
        return Props.create(OnlinePaymentActor.class,paymentActor);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
              .match(PaymentRequestMsg.class, this::processOnlinePayment)
              .build();
    }

    private void processOnlinePayment(PaymentRequestMsg msg) throws InterruptedException, TimeoutException {
        FiniteDuration timeOut= Duration.create(10, TimeUnit.SECONDS);
        Future<Object> ask= Patterns.ask(paymentActor,msg,timeOut.toMillis());
        PaymentResponseMsg response=(PaymentResponseMsg) Await.result(ask,timeOut);
        boolean isPaymentConfirmed=response.isResult();
        String  description=response.getText();
        if(isPaymentConfirmed){
            System.out.println("Payment confirmed(Online)");
        }
        else{
            System.out.println("Payment rejected(Online) :"+description);
        }
    }
}
