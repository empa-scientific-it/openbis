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

import ch.ethz.sis.shared.io.IOUtils;
import ch.ethz.sis.afs.AbstractTest;
import ch.ethz.sis.afs.dto.Lock;
import ch.ethz.sis.afs.dto.LockType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class HierarchicalLockManagerTest extends AbstractTest {

    private static final String RES_ROOT = IOUtils.ABSOLUTE_PATH_ROOT;
    private static final String RES_RELATIVE_ROOT = IOUtils.RELATIVE_PATH_ROOT;

    private static final String RES_A = "/A";
    private static final String RES_AB = "/A/B";
    private static final String RES_B = "/B";

    private static final UUID OWNER_A = UUID.randomUUID();
    private static final UUID OWNER_B = UUID.randomUUID();

    private PathLockFinder pathLockFinder;
    private LockManager<UUID, String> lockManager;

    @Before
    public void setup() {
        pathLockFinder = new PathLockFinder();
        lockManager = new LockManager<>(pathLockFinder);
    }

    @After
    public void clean() {
        pathLockFinder = null;
        lockManager = null;
    }

    private void addSharedLock(UUID o, String r, Boolean expectedAdd) {
        Lock<UUID, String> lock = new Lock<>(o, r, LockType.Shared);
        Boolean actualAdd = lockManager.add(List.of(lock));
        assertEquals(expectedAdd, actualAdd);
        assertEquals(expectedAdd, lockManager.getSharedLocks().get(r).contains(lock));
    }

    private void removeSharedLock(UUID o, String r, Boolean expectedRemove) {
        Lock<UUID, String> lock = new Lock<>(o, r, LockType.Shared);
        Boolean actualRemove = lockManager.remove(List.of(lock));
        assertEquals(expectedRemove, actualRemove);
    }

    private void addHierarchicalLock(UUID o, String r, Boolean expectedAdd) {
        Lock<UUID, String> lock = new Lock<>(o, r, LockType.HierarchicallyExclusive);
        Boolean actualAdd = lockManager.add(List.of(lock));
        assertEquals(expectedAdd, actualAdd);
        if (actualAdd && lockManager.getHierarchicallyExclusiveLocks().containsKey(r)) {
            assertEquals(expectedAdd, lockManager.getHierarchicallyExclusiveLocks().get(r).getOwner().equals(o));
        } else if (actualAdd && !lockManager.getHierarchicallyExclusiveLocks().containsKey(r)) {
            Lock<UUID, String> found = pathLockFinder.getHierarchicallyExclusiveLock(lock);
            assertEquals(found.getOwner(), o);
        } else {
            assertFalse(actualAdd);
        }
    }

    private void removeHierarchicalLock(UUID o, String r, Boolean expectedRemove) {
        Lock<UUID, String> lock = new Lock<>(o, r, LockType.HierarchicallyExclusive);
        Boolean actualRemove = lockManager.remove(List.of(lock));
        assertEquals(expectedRemove, actualRemove);
    }

    private void assertSize(int sharedLocks, int exclusiveLocks, int hExclusiveLocks, int waitingFor, int waitedBy) {
        assertEquals(sharedLocks, lockManager.getSharedLocks().size());
        assertEquals(exclusiveLocks, lockManager.getExclusiveLocks().size());
        assertEquals(hExclusiveLocks, lockManager.getHierarchicallyExclusiveLocks().size());
        assertEquals(waitingFor, lockManager.getWaitingFor().size());
        assertEquals(waitedBy, lockManager.getWaitedBy().size());
    }

    @Test
    public void addHierarchicalyExclusiveLock() {
        addHierarchicalLock(OWNER_A, RES_A, Boolean.TRUE);
        assertSize(0, 0, 1, 0, 0);
    }

    @Test
    public void removeHierarchicalyExclusiveLock() {
        addHierarchicalyExclusiveLock();
        removeHierarchicalLock(OWNER_A, RES_A, Boolean.TRUE);
        assertSize(0, 0, 0, 0, 0);
    }

    @Test
    public void addHierarchicalyExclusiveLockSameOwnerTwiceA() {
        addHierarchicalLock(OWNER_A, RES_A, Boolean.TRUE);
        addHierarchicalLock(OWNER_A, RES_A, Boolean.TRUE);
        assertNotNull(lockManager.getHierarchicallyExclusiveLocks().get(RES_A));
        assertSize(0, 0, 1, 0, 0);
    }

    @Test
    public void addHierarchicalyExclusiveLockSameOwnerTwiceB() {
        addHierarchicalLock(OWNER_A, RES_AB, Boolean.TRUE);
        addHierarchicalLock(OWNER_A, RES_A, Boolean.TRUE);
        assertNotNull(lockManager.getHierarchicallyExclusiveLocks().get(RES_AB));
//        assertNotNull(lockManager.getHierarchicallyExclusiveLocks().get(RES_A));
        assertSize(0, 0, 1, 0, 0);
    }

    @Test
    public void addHierarchicalyExclusiveLockSameOwnerTwiceC() {
        addHierarchicalLock(OWNER_A, RES_A, Boolean.TRUE);
        addHierarchicalLock(OWNER_A, RES_AB, Boolean.TRUE);
        assertNotNull(lockManager.getHierarchicallyExclusiveLocks().get(RES_A));
//        assertNotNull(lockManager.getHierarchicallyExclusiveLocks().get(RES_AB));
        assertSize(0, 0, 1, 0, 0);
    }

    @Test
    public void addHierarchicalyExclusiveLockSameOwner() {
        addSharedLock(OWNER_A, RES_A, Boolean.TRUE);
        addHierarchicalLock(OWNER_A, RES_A, Boolean.TRUE);
        assertSize(1, 0, 1, 0, 0);
    }

    @Test
    public void addHierarchicalyExclusiveLockDifferentOwnersTwice() {
        addHierarchicalLock(OWNER_A, RES_A, Boolean.TRUE);
        addHierarchicalLock(OWNER_B, RES_A, Boolean.FALSE);
        assertSize(0, 0, 1, 1, 1);
    }


    @Test
    public void safeWithHierarchicallyExclusiveLocksA() {
        addHierarchicalLock(OWNER_A, RES_A, Boolean.TRUE);
        addHierarchicalLock(OWNER_B, RES_AB, Boolean.FALSE);
        addHierarchicalLock(OWNER_A, RES_AB, Boolean.TRUE);
        addHierarchicalLock(OWNER_B, RES_A, Boolean.FALSE);
    }

    @Test
    public void safeWithHierarchicallyExclusiveLocksB() {
        addHierarchicalLock(OWNER_A, RES_AB, Boolean.TRUE);
        addHierarchicalLock(OWNER_B, RES_A, Boolean.FALSE);
        addHierarchicalLock(OWNER_A, RES_A, Boolean.TRUE);
        addHierarchicalLock(OWNER_B, RES_AB, Boolean.FALSE);
    }

    @Test(expected = RuntimeException.class)
    public void deadlockWithHierarchicallyExclusiveLocks() {
        addHierarchicalLock(OWNER_A, RES_A, Boolean.TRUE);
        addHierarchicalLock(OWNER_B, RES_B, Boolean.TRUE);
        addHierarchicalLock(OWNER_B, RES_A, Boolean.FALSE);
        addHierarchicalLock(OWNER_A, RES_B, Boolean.FALSE);
    }

    @Test
    public void rootWithHierarchicallyExclusiveLocks() {
        addHierarchicalLock(OWNER_A, RES_ROOT, Boolean.TRUE);
        addHierarchicalLock(OWNER_B, RES_ROOT, Boolean.FALSE);
        assertSize(0, 0, 1, 1, 1);
    }

    @Test
    public void relativeRootWithHierarchicallyExclusiveLocks() {
        addHierarchicalLock(OWNER_A, RES_RELATIVE_ROOT, Boolean.TRUE);
        addHierarchicalLock(OWNER_B, RES_RELATIVE_ROOT, Boolean.FALSE);
        assertSize(0, 0, 1, 1, 1);
    }

    @Test
    public void rootMixWithHierarchicallyExclusiveLocks() {
        addHierarchicalLock(OWNER_A, RES_ROOT, Boolean.TRUE);
        addHierarchicalLock(OWNER_B, RES_RELATIVE_ROOT, Boolean.TRUE);
        assertSize(0, 0, 2, 0, 0);
    }

}
