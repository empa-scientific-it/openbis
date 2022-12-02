package ch.ethz.sis.shared.pool;

import lombok.Value;

import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Semaphore;

public class Pool<I, E> {
    private final Deque<E> availableElements;
    private final Map<E, Status> inUseElements;
    private final Semaphore available;
    private final int maxSize;
    private final I factoryParameters;
    private final Factory<I, I, E> factory;

    @Value
    public static class Status {
        long acquisitionTime;
        Thread owner;
    }

    public Pool(int maxSize, I factoryParameters, Factory<I, I, E> factory) throws Exception {
        this.maxSize = maxSize;
        this.factoryParameters = factoryParameters;
        this.factory = factory;
        this.available = new Semaphore(maxSize, true);
        this.availableElements = new ConcurrentLinkedDeque<>();
        this.inUseElements = new ConcurrentHashMap<>();
    }

    public E checkOut() throws Exception {
        available.acquire();

        if (availableElements.isEmpty() && inUseElements.size() < maxSize) {
            availableElements.add(factory.create(factoryParameters));
        }

        E element = availableElements.pollFirst();

        Status stats = new Status(System.currentTimeMillis(), Thread.currentThread());
        inUseElements.put(element, stats);

        return element;
    }

    public void checkIn(E element) {
        inUseElements.remove(element);
        availableElements.addLast(element);
        available.release();
    }

    public void shutdown() throws Exception {
        for (E e:inUseElements.keySet()) {
            factory.destroy(e);
        }
        for (E e:availableElements) {
            factory.destroy(e);
        }
    }
}
