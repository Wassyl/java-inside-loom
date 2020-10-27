package fr.umlv.javainside;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SchedulerTest {

    @Test
    public void testStack() {
        var scheduler = new Scheduler(Scheduler.Policy.STACK);
        var scope = new ContinuationScope("scope");
        var list = new ArrayList<String>();
        var continuation1 = new Continuation(scope, () -> {
            list.add("start 1");
            scheduler.enqueue(scope);
            list.add("middle 1");
            scheduler.enqueue(scope);
            list.add("end 1");
        });
        var continuation2 = new Continuation(scope, () -> {
            list.add("start 2");
            scheduler.enqueue(scope);
            list.add("middle 2");
            scheduler.enqueue(scope);
            list.add("end 2");
        });
        var listConts = List.of(continuation1, continuation2);
        listConts.forEach(Continuation::run);
        scheduler.runLoop();
        assertEquals(List.of("start 1", "start 2", "middle 2", "end 2", "middle 1", "end 1"), list);
        assertNotEquals(List.of("middle 1", "start 2", "start 1", "end 1", "middle 2", "end 2"), list);
    }

    @Test
    public void testFifo() {
        var scheduler = new Scheduler(Scheduler.Policy.FIFO);
        var scope = new ContinuationScope("scope");
        var list = new ArrayList<String>();
        var continuation1 = new Continuation(scope, () -> {
            list.add("start 1");
            scheduler.enqueue(scope);
            list.add("middle 1");
            scheduler.enqueue(scope);
            list.add("end 1");
        });
        var continuation2 = new Continuation(scope, () -> {
            list.add("start 2");
            scheduler.enqueue(scope);
            list.add("middle 2");
            scheduler.enqueue(scope);
            list.add("end 2");
        });
        var listConts = List.of(continuation1, continuation2);
        listConts.forEach(Continuation::run);
        scheduler.runLoop();
        assertEquals(List.of("start 1", "start 2", "middle 1", "middle 2", "end 1", "end 2"), list);
        assertNotEquals(List.of("start 2", "start 1", "middle 1", "middle 2", "end 1", "end 2"), list);
    }

}
