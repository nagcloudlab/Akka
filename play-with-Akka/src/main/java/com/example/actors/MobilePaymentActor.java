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

public class MobilePaymentActor extends AbstractActor {


    private final ActorRef paymentActor;

    public MobilePaymentActor(ActorRef paymentActor){
        this.paymentActor=paymentActor;
    }

    public static Props props(ActorRef paymentActor){
        return Props.create(MobilePaymentActor.class,paymentActor);
    }


    @Override
    public Receive createReceive() {
        return receiveBuilder()
              .match(PaymentRequestMsg.class, this::processMobilePayment)
              .build();
    }

    private void processMobilePayment(PaymentRequestMsg msg) throws InterruptedException, TimeoutException {
        FiniteDuration timeOut= Duration.create(10, TimeUnit.SECONDS);
        Future<Object> ask=Patterns.ask(paymentActor,msg,timeOut.toMillis());
        PaymentResponseMsg response=(PaymentResponseMsg) Await.result(ask,timeOut);
        boolean isPaymentConfirmed=response.isResult();
        String  description=response.getText();
        if(isPaymentConfirmed){
            System.out.println("Payment confirmed(Mobile)");
        }
        else{
            System.out.println("Payment rejected(Mobile) :"+description);
        }
    }
}
