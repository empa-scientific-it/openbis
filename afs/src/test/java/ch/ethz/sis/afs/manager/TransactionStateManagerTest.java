package ch.ethz.sis.afs.manager;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TransactionStateManagerTest extends AbstractTransactionConnectionTest {

    @Test
    public void operation_state_new_succeed() throws Exception {
        assertEquals(getTransactionConnection().getState(), State.New);
    }

    @Test
    public void operation_state_begin_succeed() throws Exception {
        begin();
        assertEquals(getTransactionConnection().getState(), State.Begin);
    }

    @Test
    public void operation_state_prepare_succeed() throws Exception {
        begin();
        prepare();
        assertEquals(getTransactionConnection().getState(), State.Prepare);
    }

    @Test
    public void operation_state_rollback_succeed() throws Exception {
        begin();
        prepare();
        rollback();
        assertEquals(getTransactionConnection().getState(), State.Rollback);
    }

    @Test
    public void operation_state_commit_succeed() throws Exception {
        begin();
        commit();
        assertEquals(getTransactionConnection().getState(), State.Executed);
    }

    @Test
    public void operation_state_commitPrepared_succeed() throws Exception {
        begin();
        prepare();
        commit();
        assertEquals(getTransactionConnection().getState(), State.Executed);
    }

    @Test
    public void operation_state_commit_reuse_succeed() throws Exception {
        begin();
        prepare();
        commit();
        begin();
        assertEquals(getTransactionConnection().getState(), State.Begin);
    }

    @Test
    public void operation_state_rollback_reuse_succeed() throws Exception {
        begin();
        prepare();
        rollback();
        begin();
        assertEquals(getTransactionConnection().getState(), State.Begin);
    }

    @Test(expected = RuntimeException.class)
    public void operation_state_begin_reuse_fails() throws Exception {
        begin();
        begin();
    }

    @Test
    public void operation_state_prepare_reuse_succeed() throws Exception {
        begin();
        prepare();
        begin();
    }
}
