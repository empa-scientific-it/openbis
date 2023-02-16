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

import ch.ethz.sis.afs.AbstractTest;
import ch.ethz.sis.afs.dto.Lock;
import ch.ethz.sis.afs.dto.LockType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LockManagerTest extends AbstractTest {

    private enum Resource {ResA, ResB}

    private enum Owner {A, B}

    private LockManager<Owner, Resource> lockManager;

    @Before
    public void setup() {
        lockManager = new LockManager<>(null);
    }

    @After
    public void clean() {
        lockManager = null;
    }

    private void addSharedLock(Owner o, Resource r, Boolean expectedAdd) {
        Lock<Owner, Resource> lock = new Lock<>(o, r, LockType.Shared);
        Boolean actualAdd = lockManager.add(List.of(lock));
        assertEquals(expectedAdd, actualAdd);
        assertEquals(expectedAdd, lockManager.getSharedLocks().get(r).contains(lock));
    }

    private void removeSharedLock(Owner o, Resource r, Boolean expectedRemove) {
        Lock<Owner, Resource> lock = new Lock<>(o, r, LockType.Shared);
        Boolean actualRemove = lockManager.remove(List.of(lock));
        assertEquals(expectedRemove, actualRemove);
    }

    private void addExclusiveLock(Owner o, Resource r, Boolean expectedAdd) {
        Lock<Owner, Resource> lock = new Lock<>(o, r, LockType.Exclusive);
        Boolean actualAdd = lockManager.add(List.of(lock));
        assertEquals(expectedAdd, actualAdd);
        assertEquals(expectedAdd, lockManager.getExclusiveLocks().get(r).equals(lock));
    }

    private void removeExclusiveLock(Owner o, Resource r, Boolean expectedRemove) {
        Lock<Owner, Resource> lock = new Lock<>(o, r, LockType.Exclusive);
        Boolean actualRemove = lockManager.remove(List.of(lock));
        assertEquals(expectedRemove, actualRemove);
    }

    private void assertSize(int sharedLocks, int exclusiveLocks, int waitingFor, int waitedBy) {
        assertEquals(sharedLocks, lockManager.getSharedLocks().size());
        assertEquals(exclusiveLocks, lockManager.getExclusiveLocks().size());
        assertEquals(waitingFor, lockManager.getWaitingFor().size());
        assertEquals(waitedBy, lockManager.getWaitedBy().size());
    }

    @Test
    public void addSharedLock() {
        addSharedLock(Owner.A, Resource.ResA, Boolean.TRUE);
        assertSize(1, 0, 0, 0);
    }

    @Test
    public void removeSharedLock() {
        addSharedLock();
        removeSharedLock(Owner.A, Resource.ResA, Boolean.TRUE);
        assertSize(0, 0, 0, 0);
    }

    @Test
    public void addSharedLockSameOwnerTwice() {
        addSharedLock(Owner.A, Resource.ResA, Boolean.TRUE);
        addSharedLock(Owner.A, Resource.ResA, Boolean.TRUE);
        assertEquals(1, lockManager.getSharedLocks().get(Resource.ResA).size());
        assertSize(1, 0, 0, 0);
    }

    @Test
    public void addExclusiveAfterSharedSameOwner() {
        addSharedLock(Owner.A, Resource.ResA, Boolean.TRUE);
        addExclusiveLock(Owner.A, Resource.ResA, Boolean.TRUE);
        assertSize(1, 1, 0, 0);
    }

    @Test
    public void addSharedLockDifferentOwnersTwice() {
        addSharedLock(Owner.A, Resource.ResA, Boolean.TRUE);
        addSharedLock(Owner.B, Resource.ResA, Boolean.TRUE);
        assertSize(1, 0, 0, 0);
    }

    @Test
    public void addExclusiveLock() {
        addExclusiveLock(Owner.A, Resource.ResA, Boolean.TRUE);
        assertSize(0, 1, 0, 0);
    }

    @Test
    public void removeExclusiveLock() {
        addExclusiveLock();
        removeExclusiveLock(Owner.A, Resource.ResA, Boolean.TRUE);
        assertSize(0, 0, 0, 0);
    }

    @Test
    public void addExclusiveLockSameOwnerTwice() {
        addExclusiveLock(Owner.A, Resource.ResA, Boolean.TRUE);
        addExclusiveLock(Owner.A, Resource.ResA, Boolean.TRUE);
        assertSize(0, 1, 0, 0);
    }

    @Test
    public void addExclusiveLockDifferentOwnersTwice() {
        addExclusiveLock(Owner.A, Resource.ResA, Boolean.TRUE);
        addExclusiveLock(Owner.B, Resource.ResA, Boolean.FALSE);
        assertSize(0, 1, 1, 1);
        assertTrue(lockManager.getWaitingFor().containsKey(Owner.B));
        assertTrue(lockManager.getWaitingFor().get(Owner.B).containsKey(Owner.A));
        assertTrue(lockManager.getWaitingFor().get(Owner.B).get(Owner.A).contains(Resource.ResA));
        assertTrue(lockManager.getWaitedBy().containsKey(Owner.A));
        assertTrue(lockManager.getWaitedBy().get(Owner.A).containsKey(Owner.B));
        assertTrue(lockManager.getWaitedBy().get(Owner.A).get(Owner.B).contains(Resource.ResA));
    }

    @Test
    public void removeExclusiveLockDifferentOwnersTwice() {
        addExclusiveLockDifferentOwnersTwice();
        removeExclusiveLock(Owner.A, Resource.ResA, Boolean.TRUE);
        assertSize(0, 0, 0, 0);
    }

    @Test(expected = RuntimeException.class)
    public void deadlockWithExclusiveLocks() {
        addExclusiveLock(Owner.A, Resource.ResA, Boolean.TRUE);
        addExclusiveLock(Owner.B, Resource.ResB, Boolean.TRUE);
        addExclusiveLock(Owner.A, Resource.ResB, Boolean.FALSE);
        addExclusiveLock(Owner.B, Resource.ResA, Boolean.FALSE);
    }

    @Test(expected = RuntimeException.class)
    public void deadlockWithSharedExclusiveLocks() {
        addSharedLock(Owner.A, Resource.ResA, Boolean.TRUE);
        addSharedLock(Owner.B, Resource.ResB, Boolean.TRUE);
        addExclusiveLock(Owner.A, Resource.ResB, Boolean.FALSE);
        addExclusiveLock(Owner.B, Resource.ResA, Boolean.FALSE);
    }

}
