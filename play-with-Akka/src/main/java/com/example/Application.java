package com.example;


import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.routing.FromConfig;
import akka.routing.RoundRobinPool;
import com.example.actors.*;
import com.example.db.FakeDB;
import com.example.messages.BalanceRequestMsg;
import com.example.messages.PaymentRequestMsg;
import com.example.strategy.SupervisionStrategyMain;

public class Application {
    public static void main(String[] args) throws InterruptedException {

        FakeDB.balanceSheet.put(1, 100.0);
        FakeDB.balanceSheet.put(2, 250.0);
        FakeDB.balanceSheet.put(3, 300.0);
        FakeDB.balanceSheet.put(4, 400.0);
        FakeDB.balanceSheet.put(5, 500.0);
        FakeDB.balanceSheet.put(6, 600.0);
        FakeDB.balanceSheet.put(7, 700.0);
        FakeDB.balanceSheet.put(8, 800.0);
        FakeDB.balanceSheet.put(9, 900.0);

        ActorSystem system = ActorSystem.create("system-1");

        //ActorRef balanceActor=system.actorOf(BalanceActor.props().withRouter(new RoundRobinPool(9)), "balanceActor");
        //ActorRef balanceActor=system.actorOf(BalanceActor.props().withRouter(FromConfig.getInstance()),"balanceActor");

//        for (int i=1;i<=9;i++){
//            balanceActor.tell(new BalanceRequestMsg(i,"Customer-"+i), ActorRef.noSender());
//        }

//        ActorRef paymentActor = system.actorOf(PaymentActor.props().withRouter(new RoundRobinPool(1)), "paymentActor");
//        ActorRef mobilePaymentActor = system.actorOf(MobilePaymentActor.props(paymentActor).withRouter(new RoundRobinPool(4)), "mobilePaymentActor");
//        ActorRef onlinePaymentActor = system.actorOf(OnlinePaymentActor.props(paymentActor).withRouter(new RoundRobinPool(4)), "onlinePaymentActor");
//
//        for (int i = 1; i <= 10; i++) {
//            if (i % 2 == 0) {
//                //0,2,4,6,8
//                onlinePaymentActor.tell(new PaymentRequestMsg(1, 25.0), ActorRef.noSender());
//            } else {
//                //1,3,5,7,9
//                mobilePaymentActor.tell(new PaymentRequestMsg(1, 25.0), ActorRef.noSender());
//            }
//        }


        ActorRef bornToFailActor = system.actorOf(BornToFailActor.props()
                        .withRouter(new RoundRobinPool(1).withSupervisorStrategy(new SupervisionStrategyMain().create()))
                , "failingActor");


        // 1. String message as the actor expects
        bornToFailActor.tell("Hey1",ActorRef.noSender());
        // 2. int message , will  throw an  exception
        bornToFailActor.tell(1,ActorRef.noSender());
        Thread.sleep(3*1000);
        // 3. String message ,to see how supervision strategy acted
        bornToFailActor.tell("Hey2",ActorRef.noSender());

    }
}
