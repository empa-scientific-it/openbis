package ch.systemsx.cisd.openbis.generic.server.dataaccess;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.collection.ExtendedLinkedBlockingQueue;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

public class DynamicPropertyEvaluationSchedulerTest extends AssertJUnit
{
    public DynamicPropertyEvaluationScheduler scheduler;

    private long counter;

    private DynamicPropertyEvaluationOperation updateSingle()
    {
        counter++;
        return DynamicPropertyEvaluationOperation.evaluate(SamplePE.class, Collections.singletonList(counter));
    }

    private DynamicPropertyEvaluationOperation updateAll()
    {
        return DynamicPropertyEvaluationOperation.evaluateAll(SamplePE.class);
    }

    @BeforeMethod
    public void clear()
    {
        scheduler = new DynamicPropertyEvaluationScheduler(new ExtendedLinkedBlockingQueue<DynamicPropertyEvaluationOperation>());
        counter = 0;
    }

    @Test
    public void singleEntityUpdatesArePrioritizedOverEntityTypeUpdates() throws Exception
    {
        DynamicPropertyEvaluationOperation updateSingle = updateSingle();
        DynamicPropertyEvaluationOperation updateAll = updateAll();

        scheduler.scheduleUpdate(updateAll);
        scheduler.scheduleUpdate(updateSingle);
        scheduler.synchronizeThreadQueue();

        assertEquals(updateSingle, scheduler.peekWait());
        scheduler.take();
        assertEquals(updateAll, scheduler.peekWait());
    }

    @Test
    public void singleEntityUpdatesKeepTheirOrder() throws Exception
    {
        DynamicPropertyEvaluationOperation first = updateSingle();
        DynamicPropertyEvaluationOperation second = updateSingle();
        DynamicPropertyEvaluationOperation third = updateSingle();

        scheduler.scheduleUpdate(first);
        scheduler.scheduleUpdate(second);
        scheduler.scheduleUpdate(third);
        scheduler.synchronizeThreadQueue();

        assertEquals(first, scheduler.peekWait());
        assertEquals(first, scheduler.take());
        assertEquals(second, scheduler.peekWait());
        assertEquals(second, scheduler.take());
        assertEquals(third, scheduler.peekWait());
        assertEquals(third, scheduler.take());
    }

    @Test
    public void scheduleCanContainOnlyOneEntityTypeUpdateOperation() throws Exception
    {
        scheduler.scheduleUpdate(updateAll());
        scheduler.scheduleUpdate(updateAll());
        scheduler.synchronizeThreadQueue();
        scheduler.scheduleUpdate(updateAll());
        scheduler.scheduleUpdate(updateAll());
        scheduler.synchronizeThreadQueue();

        assertEquals(scheduler.getEvaluatorQueue().size(), 1);
    }

    private static AtomicLong atomicId = new AtomicLong(-5);

    private class WriteAllWorker implements Runnable
    {

        private BlockingQueue<DynamicPropertyEvaluationOperation> operationQueue;

        public WriteAllWorker(BlockingQueue<DynamicPropertyEvaluationOperation> operationQueue)
        {
            this.operationQueue = operationQueue;

        }

        @Override
        public void run()
        {
            while (true)
            {
                DynamicPropertyEvaluationOperation operation = operationQueue.poll();
                if (operation == null)
                {

                    scheduler.scheduleUpdate(DynamicPropertyEvaluationOperation.evaluate(SamplePE.class,
                            Collections.singletonList(atomicId.incrementAndGet())));
                    scheduler.synchronizeThreadQueue();
                    System.out.println("END WRITE");
                    break;
                } else
                {
                    scheduler.scheduleUpdate(operation);
                    scheduler.synchronizeThreadQueue();
                    // System.out.println("WRITE");
                    Thread.yield();
                }
            }
        }

    }

    private class ReadAllWorker implements Callable<Set<DynamicPropertyEvaluationOperation>>
    {

        private final HashSet<DynamicPropertyEvaluationOperation> results;

        private int counter;

        public ReadAllWorker()
        {
            results = new HashSet<DynamicPropertyEvaluationOperation>();
            counter = 0;
        }

        @Override
        public Set<DynamicPropertyEvaluationOperation> call() throws Exception
        {
            while (true)
            {
                scheduler.peekWait();
                DynamicPropertyEvaluationOperation value = scheduler.take();
                if (value.getIds() != null && value.getIds().toArray(new Long[0])[0] < 0)
                {
                    counter++;
                    System.out.println("counter is now " + counter);
                    if (counter == 3)
                    {
                        break;
                    } else
                    {
                        continue;
                    }
                }
                results.add(value);
                Thread.yield();
                // System.out.println("READ");
            }
            System.out.println("END READ");
            return results;

        }
    }

    @Test
    public void everythingThatGoesInComesOut() throws Exception
    {

        List<DynamicPropertyEvaluationOperation> allOperations = new ArrayList<DynamicPropertyEvaluationOperation>();
        for (int i = 0; i < 10000; i++)
        {
            DynamicPropertyEvaluationOperation operation = Math.random() < 0.9 ? updateSingle() : updateAll();
            allOperations.add(operation);
        }
        Collections.shuffle(allOperations);

        BlockingQueue<DynamicPropertyEvaluationOperation> operationQueue = new LinkedBlockingQueue<DynamicPropertyEvaluationOperation>();
        for (DynamicPropertyEvaluationOperation operation : allOperations)
        {
            operationQueue.add(operation);
        }

        ExecutorService tp = Executors.newFixedThreadPool(4);
        tp.execute(new WriteAllWorker(operationQueue));
        Future<Set<DynamicPropertyEvaluationOperation>> resultF = tp.submit(new ReadAllWorker());
        tp.execute(new WriteAllWorker(operationQueue));
        tp.execute(new WriteAllWorker(operationQueue));

        Set<DynamicPropertyEvaluationOperation> result = resultF.get();

        assertTrue(allOperations.containsAll(result));

        Iterator<DynamicPropertyEvaluationOperation> iterator = allOperations.iterator();
        while (iterator.hasNext())
        {
            if (iterator.next().getIds() == null)
                iterator.remove();
        }

        ArrayList<DynamicPropertyEvaluationOperation> log = new ArrayList<DynamicPropertyEvaluationOperation>(allOperations);
        log.removeAll(result);
        System.out.println("Expecting empty: " + log);

        assertTrue(result.containsAll(allOperations));
    }
}
