package ch.ethz.sis.afs.manager;

import ch.ethz.sis.afs.dto.Lock;

import java.util.Map;

interface HierarchicalLockFinder<O, E> {
    Map<E, Lock<O, E>> getHierarchicallyExclusiveLocks();

    Lock<O, E> getHierarchicallyExclusiveLock(Lock<O, E> lock);

    void add(Lock<O, E> lock);

    void remove(Lock<O, E> lock);
}
