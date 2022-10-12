package ch.ethz.sis.afs.manager.operation;

import ch.ethz.sis.afs.manager.AbstractTransactionConnectionTest;
import org.junit.Test;

public abstract class AbstractTransactionOperationTest extends AbstractTransactionConnectionTest {

    //
    // Generic Tests
    //

    public abstract void operation() throws Exception;

    @Test(expected = RuntimeException.class)
    public void operation_beforeBegin_exception() throws Exception {
        operation();
    }

    @Test(expected = RuntimeException.class)
    public void operation_afterPrepare_exception() throws Exception {
        begin();
        prepare();
        operation();
    }

    @Test(expected = RuntimeException.class)
    public void operation_afterRollback_exception() throws Exception {
        begin();
        prepare();
        rollback();
        operation();
    }

    @Test(expected = RuntimeException.class)
    public void operation_afterCommit_exception() throws Exception {
        begin();
        prepare();
        commit();
        operation();
    }
}
