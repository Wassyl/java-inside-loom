package fr.umlv.javainside;

import java.util.ArrayDeque;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

public class Scheduler {

    private interface PolicyImpl {
        void add(Continuation continuation);
        Continuation remove();
        boolean isEmpty();
    }

    public enum Policy{
        STACK   {
            @Override
            PolicyImpl createImpl(){
                return new PolicyImpl() {
                    private final ArrayDeque<Continuation> queue = new ArrayDeque<>();

                    @Override
                    public void add(Continuation continuation) {
                        queue.offerLast(continuation);
                    }

                    @Override
                    public Continuation remove() {
                        return queue.removeLast();
                    }

                    @Override
                    public boolean isEmpty() {
                        return queue.isEmpty();
                    }
                };
            }
        }
        ,FIFO   {
            @Override
            PolicyImpl createImpl(){
                return new PolicyImpl() {
                    private final ArrayDeque<Continuation> queue = new ArrayDeque<>();

                    @Override
                    public void add(Continuation continuation) {
                        queue.offerLast(continuation);
                    }

                    @Override
                    public Continuation remove() {
                        return queue.removeFirst();
                    }

                    @Override
                    public boolean isEmpty() {
                        return queue.isEmpty();
                    }
                };
            }
        }
        ,RANDOM {
            @Override
            PolicyImpl createImpl(){
                return new PolicyImpl() {
                    private final TreeMap<Integer, ArrayDeque<Continuation>> tree = new TreeMap<>();

                    @Override
                    public void add(Continuation continuation) {
                        var random = ThreadLocalRandom.current().nextInt();
                        tree.computeIfAbsent(random, __ -> new ArrayDeque<>()).offer(continuation);
                    }

                    @Override
                    public Continuation remove() {
                        var random = ThreadLocalRandom.current().nextInt();
                        var key = tree.floorKey(random);
                        if ( key == null ){
                            key = tree.firstKey();
                        }
                        var queue =  tree.get(key);
                        var continuation = queue.poll();
                        if( queue.isEmpty() ){
                            tree.remove(key);
                        }
                        return continuation;
                    }

                    @Override
                    public boolean isEmpty() {
                        return tree.isEmpty();
                    }
                };
            }
        };

        abstract PolicyImpl createImpl();
    }

    private final PolicyImpl policyImpl;
    public Scheduler( Policy policy ){
        this.policyImpl = policy.createImpl();
    }

    public void enqueue( ContinuationScope scope ) {
        var cont = Continuation.getCurrentContinuation(scope);
        if( cont == null ){
            throw new IllegalStateException("No running continuation");
        }
        policyImpl.add(cont);
        Continuation.yield(scope);
    }

    public void runLoop() {
        while( !policyImpl.isEmpty() ) {
            var cont = policyImpl.remove();
            cont.run();
        }
    }

}
