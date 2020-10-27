package fr.umlv.javainside;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class Example1 {
    public static void continuationTest(String[] args) {
        var lock = new Object();
        var lockRe = new ReentrantLock();

        var oldScope = new ContinuationScope("hello1");
        var cont = new Continuation( oldScope, () -> {

            //Continuation.yield(scope);
            System.out.println("Hello continuation");
            /*
            // Synchronized est fait en assembleur : difficile à update
            synchronized(lockRe) {
                Continuation.yield(scope);
            }
            */
            // ReentrantLock est fait en Java : OK !
            lockRe.lock();
            try{
                Continuation.yield(oldScope);
            } finally {
                lockRe.unlock();
            }

            var test = Continuation.getCurrentContinuation(oldScope);
            System.out.println("Hello continuation");

        });
        /*
        cont.run();
        Continuation.getCurrentContinuation(scope);
        cont.run();
        */


        var scope = new ContinuationScope("scope");
        var continuation1 = new Continuation(scope, () -> {
            System.out.println("start 1");
            Continuation.yield(scope);
            System.out.println("middle 1");
            Continuation.yield(scope);
            System.out.println("end 1");
        });
        var continuation2 = new Continuation(scope, () -> {
            System.out.println("start 2");
            Continuation.yield(scope);
            System.out.println("middle 2");
            Continuation.yield(scope);
            System.out.println("end 2");
        });
        var list = List.of(continuation1, continuation2);
        /*
        list.forEach( e -> {
                while( !e.isDone() ){
                    e.run();
                }
            } );
        */

        while( !list.stream().allMatch(Continuation::isDone) ){
            list.forEach( Continuation::run );
        }

    }

    public static void main(String[] args) {
        var scope = new ContinuationScope("scope");
        var scheduler = new Scheduler(Scheduler.SCHEDULER_POLICY.STACK);
        var continuation1 = new Continuation(scope, () -> {
            System.out.println("start 1");
            scheduler.enqueue(scope);
            System.out.println("middle 1");
            scheduler.enqueue(scope);
            System.out.println("end 1");
        });
        var continuation2 = new Continuation(scope, () -> {
            System.out.println("start 2");
            scheduler.enqueue(scope);
            System.out.println("middle 2");
            scheduler.enqueue(scope);
            System.out.println("end 2");
        });
        var list = List.of(continuation1, continuation2);
        list.forEach(Continuation::run);
        scheduler.runLoop();
    }
}
