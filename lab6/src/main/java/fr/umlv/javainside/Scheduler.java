package fr.umlv.javainside;

import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Collectors;

public class Scheduler {
    private enum SCHEDULER_POLICY{
        STACK,FIFO,RANDOM
    }
    private SCHEDULER_POLICY policy = SCHEDULER_POLICY.STACK;

    private final ArrayList<Continuation> list = new ArrayList();

    public void enqueue( ContinuationScope scope ) {
        var cont = Continuation.getCurrentContinuation(scope);
        if( cont == null ){
            throw new IllegalStateException("No running continuation");
        }
        list.add(cont);
        Continuation.yield(scope);
    }

    public void runLoop() {
        while( !list.stream().allMatch(Continuation::isDone) ){
            switch ( policy ){
                case STACK:{
                    var conts = list.stream()
                                                    .filter( e -> !e.isDone() )
                                                    .collect(Collectors.toList());
                    var cont = conts.get( conts.size()-1 );
                    cont.run();
                }
                case FIFO: {
                    var cont = list.get( 0 );
                    cont.run();
                }
                case RANDOM:{
                    var rand = new Random();
                    var cont = list.get( rand.nextInt(list.size()) );
                    cont.run();
                }
            }

        }
    }

}
