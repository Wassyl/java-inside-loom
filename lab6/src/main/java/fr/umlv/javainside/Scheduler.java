package fr.umlv.javainside;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Scheduler {
    public enum SCHEDULER_POLICY{
        STACK,FIFO,RANDOM
    }
    private final SCHEDULER_POLICY policy;
    private final List<Continuation> list = (ArrayList<Continuation>) new ArrayList();

    public Scheduler( SCHEDULER_POLICY policy ){
        this.policy = policy;
    }

    public void enqueue( ContinuationScope scope ) {
        var cont = Continuation.getCurrentContinuation(scope);
        if( cont == null ){
            throw new IllegalStateException("No running continuation");
        }
        list.add(cont);
        Continuation.yield(scope);
    }

    public void runLoop() {
        while( !list.isEmpty() ) {
            Continuation cont;
            switch(policy){
                case STACK  -> cont = list.remove( list.size() -1 );
                case FIFO   -> cont = list.remove( 0 );
                case RANDOM -> cont = list.remove( ThreadLocalRandom.current().nextInt(0, list.size() ));
                default     -> throw new IllegalStateException("hello there ? ");

            }
            cont.run();
        }
    }

}
