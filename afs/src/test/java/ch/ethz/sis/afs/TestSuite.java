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