/*
 * Copyright ETH 2022 - 2023 Zürich, Scientific IT Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.ethz.sis.afs.manager;

import ch.ethz.sis.afs.dto.Lock;
import ch.ethz.sis.afs.dto.LockType;
import ch.ethz.sis.afs.exception.AFSExceptions;

import java.util.*;

import static ch.ethz.sis.afs.exception.AFSExceptions.DeadlockDetected;

class LockManager<O, E> {

    private HierarchicalLockFinder<O, E> hierarchicalLockFinder;
    private Map<E, Lock<O, E>> exclusiveLocks;
    private Map<E, Set<Lock<O, E>>> sharedLocks;
    private Map<O, Map<O, Set<E>>> waitingFor;
    private Map<O, Map<O, Set<E>>> waitedBy;

    LockManager(HierarchicalLockFinder<O, E> hierarchicalLockFinder) {
        if (hierarchicalLockFinder == null) {
            // Dummy lock finder when hierarchical locks are unnecessary
            this.hierarchicalLockFinder = new HierarchicalLockFinder<O, E>() {
                @Override
                public Map<E, Lock<O, E>> getHierarchicallyExclusiveLocks() {
                    return Map.of();
                }

                @Override
                public Lock<O, E> getHierarchicallyExclusiveLock(Lock lock) {
                    return null;
                }

                @Override
                public void add(Lock lock) {

                }

                @Override
                public void remove(Lock lock) {

                }
            };
        } else {
            this.hierarchicalLockFinder = hierarchicalLockFinder;
        }

        exclusiveLocks = new HashMap<>();
        sharedLocks = new HashMap<>();
        waitingFor = new HashMap<>();
        waitedBy = new HashMap<>();
    }

    Map<E, Lock<O, E>> getHierarchicallyExclusiveLocks() {
        return this.hierarchicalLockFinder.getHierarchicallyExclusiveLocks();
    }


    Map<E, Lock<O, E>> getExclusiveLocks() {
        return exclusiveLocks;
    }

    Map<E, Set<Lock<O, E>>> getSharedLocks() {
        return sharedLocks;
    }

    Map<O, Map<O, Set<E>>> getWaitingFor() {
        return waitingFor;
    }

    Map<O, Map<O, Set<E>>> getWaitedBy() {
        return waitedBy;
    }

    synchronized boolean add(List<Lock<O, E>> locks) {
        // Check for exclusive locks
        // if you need one and a single one exists for sure you can't execute this transaction now.
        List<Lock<O, E>> locksToAdd = new ArrayList<>();

        for (Lock<O, E> lock : locks) {
            Lock<O, E> foundExclusive = hierarchicalLockFinder.getHierarchicallyExclusiveLock(lock);
            if (foundExclusive == null) {
                foundExclusive = exclusiveLocks.get(lock.getResource());
            }

            Set<Lock<O, E>> foundShared = sharedLocks.get(lock.getResource());

            switch (lock.getType()) {
                case HierarchicallyExclusive:
                case Exclusive:
                    if (foundExclusive != null && foundExclusive.getOwner().equals(lock.getOwner())) {
                        //Do nothing, its just a repetition
                    } else if ((foundExclusive != null && !foundExclusive.getOwner().equals(lock.getOwner())) ||
                            (foundShared != null && !foundShared.contains(new Lock<>(lock.getOwner(), lock.getResource(), LockType.Shared)))) {
                        checkForDeadlock(lock, foundExclusive, foundShared);
                        addWait(lock, foundExclusive, foundShared);
                        return Boolean.FALSE;
                    } else {
                        locksToAdd.add(lock);
                    }
                    break;
                case Shared:
                    if (foundShared != null && foundShared.contains(lock)) {
                        //Do nothing, its just a repetition
                    } else if (foundExclusive != null && !foundExclusive.getOwner().equals(lock.getOwner())) {
                        checkForDeadlock(lock, foundExclusive, null);
                        addWait(lock, foundExclusive, null);
                        return Boolean.FALSE;
                    } else {
                        locksToAdd.add(lock);
                    }
                    break;
            }
        }

        // If none is found, is possible to add locks
        // When they are repeated you just override the ones that are there already
        for (Lock<O, E> lock : locksToAdd) {
            switch (lock.getType()) {
                case HierarchicallyExclusive:
                    hierarchicalLockFinder.add(lock);
                    break;
                case Exclusive:
                    exclusiveLocks.put(lock.getResource(), lock);
                    break;
                case Shared:
                    Set<Lock<O, E>> current = sharedLocks.get(lock.getResource());
                    if (current == null) {
                        current = new HashSet<>();
                        sharedLocks.put(lock.getResource(), current);
                    }
                    current.add(lock);
                    break;
            }
        }

        return Boolean.TRUE;
    }

    synchronized boolean remove(List<Lock<O, E>> locks) {
        for (Lock<O, E> lock : locks) {
            switch (lock.getType()) {
                case HierarchicallyExclusive:
                    hierarchicalLockFinder.remove(lock);
                    break;
                case Exclusive:
                    exclusiveLocks.remove(lock.getResource());
                    break;
                case Shared:
                    Set<Lock<O, E>> current = sharedLocks.get(lock.getResource());
                    if (current != null) {
                        current.remove(lock);
                        if (current.isEmpty()) {
                            sharedLocks.remove(lock.getResource());
                        }
                    }
                    break;
            }
            removeWait(lock);
        }
        return Boolean.TRUE;
    }


    /*
     * Used to detect deadlocks
     *
     * If an owner is waiting for a second owner to free a lock
     * The second owner can't request a lock own by the first one
     * If this happens a RuntimeException is thrown
     *
     * On adding A:
     * If (A needs to wait for B) AND (B is waiting for A):
     * throw a RuntimeException
     * else:
     * Insert
     * waitingFor: A -> [B]
     * waitedBy: B -> [A]
     *
     *
     * On remove A:
     * Check if A was been waited by and remove it from both structures
     *
     * This structure needs to be maintained in both directions to make deadlock resolution time constant
     */
    private void checkForDeadlock(Lock<O, E> waitingLock, Lock<O, E> foundExclusive, Set<Lock<O, E>> foundShared) {
        if (foundExclusive != null) {
            if (waitingFor.get(foundExclusive.getOwner()) != null &&
                    waitingFor.get(foundExclusive.getOwner()).containsKey(waitingLock.getOwner())) {
                AFSExceptions.throwInstance(DeadlockDetected, foundExclusive.getOwner(), waitingLock.getResource(), waitingLock.getOwner());
            }
        }
        if (foundShared != null) {
            for (Lock<O, E> shared : foundShared) {
                if (waitingFor.get(shared.getOwner()) != null &&
                        waitingFor.get(shared.getOwner()).containsKey(waitingLock.getOwner())) {
                    AFSExceptions.throwInstance(DeadlockDetected, shared.getOwner(), waitingLock.getResource(), waitingLock.getOwner());
                }
            }
        }
    }

    private void addWait(Lock<O, E> waitingLock, Lock<O, E> foundExclusive, Set<Lock<O, E>> foundShared) {
        // Obtain Waiting For Update
        Set<Lock<O, E>> waitingOnLocks = new HashSet<>();
        if (foundExclusive != null) {
            waitingOnLocks.add(foundExclusive);
        }
        if (foundShared != null) {
            waitingOnLocks.addAll(foundShared);
        }

        // Set Waiting For
        Map<O, Set<E>> lockWaitingFor = waitingFor.get(waitingLock.getOwner());
        if (lockWaitingFor == null) {
            lockWaitingFor = new HashMap<>();
            waitingFor.put(waitingLock.getOwner(), lockWaitingFor);
        }
        for (Lock<O, E> waitingOnLock : waitingOnLocks) {
            Set<E> resources = lockWaitingFor.get(waitingOnLock.getOwner());
            if (resources == null) {
                resources = new HashSet<>();
                lockWaitingFor.put(waitingOnLock.getOwner(), resources);
            }
            resources.add(waitingOnLock.getResource());
        }


        // Set Waited By
        for (Lock<O, E> waitingOnLock : waitingOnLocks) {
            Map<O, Set<E>> lockWaitedBy = waitedBy.get(waitingOnLock.getOwner());
            if (lockWaitedBy == null) {
                lockWaitedBy = new HashMap<>();
                waitedBy.put(waitingOnLock.getOwner(), lockWaitedBy);
            }
            Set<E> resources = lockWaitedBy.get(waitingLock.getOwner());
            if (resources == null) {
                resources = new HashSet<>();
                lockWaitedBy.put(waitingLock.getOwner(), resources);
            }
            resources.add(waitingLock.getResource());
        }
    }

    private void removeWait(Lock<O, E> releasedLock) {
        List<O> waitingForOs = new ArrayList<>();
        Map<O, Set<E>> waitingForReleaseOwner = waitedBy.get(releasedLock.getOwner());
        if (waitingForReleaseOwner != null) {
            for (O ownerWaitingForReleaseOwner : waitingForReleaseOwner.keySet()) {
                Set<E> resources = waitingForReleaseOwner.get(ownerWaitingForReleaseOwner);
                if (resources != null && resources.contains(releasedLock.getResource())) {
                    waitingForOs.add(ownerWaitingForReleaseOwner);
                    resources.remove(releasedLock.getResource());
                    if (resources.isEmpty()) {
                        waitingForReleaseOwner.remove(ownerWaitingForReleaseOwner);
                    }
                }
            }
            if (waitingForReleaseOwner.isEmpty()) {
                waitedBy.remove(releasedLock.getOwner());
            }
        }

        for (O waitingForO : waitingForOs) {
            Map<O, Set<E>> waitedByO = waitingFor.get(waitingForO);
            if (waitedByO.get(releasedLock.getOwner()) != null) {
                Set<E> resources = waitedByO.get(releasedLock.getOwner());
                if (resources != null && resources.contains(releasedLock.getResource())) {
                    resources.remove(releasedLock.getResource());
                    if (resources.isEmpty()) {
                        waitedByO.remove(releasedLock.getOwner());
                    }
                }
            }
            if (waitedByO.isEmpty()) {
                waitingFor.remove(waitingForO);
            }
        }

    }

}
