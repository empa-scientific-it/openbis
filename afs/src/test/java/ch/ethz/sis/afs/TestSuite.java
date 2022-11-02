/*
 * Copyright 2022 ETH Zürich, SIS
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

package ch.ethz.sis.afs;

import ch.ethz.sis.afs.io.IOUtilsTest;
import ch.ethz.sis.afs.manager.HierarchicalLockManagerTest;
import ch.ethz.sis.afs.manager.LockManagerTest;
import ch.ethz.sis.afs.manager.PathLockFinderTest;
import ch.ethz.sis.afs.manager.TransactionStateManagerTest;
import ch.ethz.sis.afs.manager.operation.CopyTransactionOperationTest;
import ch.ethz.sis.afs.manager.operation.DeleteTransactionOperationTest;
import ch.ethz.sis.afs.manager.operation.ListTransactionOperationTest;
import ch.ethz.sis.afs.manager.operation.MoveTransactionOperationTest;
import ch.ethz.sis.afs.manager.operation.ReadTransactionOperationTest;
import ch.ethz.sis.afs.manager.operation.WriteTransactionOperationTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        LockManagerTest.class,
        IOUtilsTest.class,
        TransactionStateManagerTest.class,
        ListTransactionOperationTest.class,
        ReadTransactionOperationTest.class,
        WriteTransactionOperationTest.class,
        DeleteTransactionOperationTest.class,
        MoveTransactionOperationTest.class,
        CopyTransactionOperationTest.class,
        PathLockFinderTest.class,
        HierarchicalLockManagerTest.class
})

public class TestSuite {

}