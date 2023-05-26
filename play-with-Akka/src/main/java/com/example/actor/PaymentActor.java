package com.example.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.example.msg.*;

import java.util.concurrent.TimeUnit;

public class PaymentActor extends AbstractBehavior<PaymentCommand> {

    private ActorRef<VerificationQuery> verificationActor;

    // Constructor
    public PaymentActor(ActorContext<PaymentCommand> context, ActorRef<VerificationQuery> verificationActor) {
        super(context);
        this.verificationActor = verificationActor;
    }

    //  Behavior
    public static Behavior<PaymentCommand> create(ActorRef<VerificationQuery> verificationActor) {
        return Behaviors.setup(
                context -> new PaymentActor(context, verificationActor)
        );
    }

    @Override
    public Receive<PaymentCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(OnlinePaymentRequest.class, this::onlinePayment)     //  Online payment requests
                .onMessage(MobilePaymentRequest.class, this::mobilePayment)     // Mobile payment requests
                .onMessage(VerificationResponse.class, this::responseReceived)  //  Responses from  Verification actor
                .build();
    }

    //  Online payment requests
    private Behavior<PaymentCommand> onlinePayment(OnlinePaymentRequest request) {
        verificationActor.tell(new VerificationQuery(getContext().getSelf(), request.getUserId(), request.getAmount()));
        return this;
    }

    //  Mobile payment requests
    private Behavior<PaymentCommand> mobilePayment(MobilePaymentRequest request) {
        verificationActor.tell(new VerificationQuery(getContext().getSelf(), request.getUserId(), request.getAmount()));
        return this;
    }

    //  Responses from  Verification actor
    private Behavior<PaymentCommand> responseReceived(VerificationResponse response) throws InterruptedException {
        TimeUnit.SECONDS.sleep(3);
        System.out.println("{" +
                "User Id:" + String.valueOf(response.getUserId()) + "," +
                "Amount:" + String.valueOf(response.getAmount()) + "," +
                "Is Confirmed:" + String.valueOf(response.isVerified()) + "," +
                "Text:" + String.valueOf(response.getText()) +
                "}");

        return this;
    }

}
