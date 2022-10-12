package ch.ethz.sis.afs.manager.operation;

import ch.ethz.sis.shared.io.File;
import ch.ethz.sis.shared.io.IOUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DeleteTransactionOperationTest extends AbstractTransactionOperationTest {

    @Override
    public void operation() throws Exception {
        delete(FILE_B_PATH);
    }

    @Test
    public void operation_delete_succeed() throws Exception {
        begin();
        String realPath = OperationExecutor.getRealPath(getTransaction(), FILE_B_PATH);
        File before = IOUtils.getFile(realPath);
        delete(FILE_B_PATH);
        File after = IOUtils.getFile(realPath);
        assertEquals(1, getTransaction().getOperations().size());
        assertEquals(before, after);
        boolean existsTrue = IOUtils.exists(realPath);
        assertTrue(existsTrue);
        prepare();
        commit();
        boolean existsFalse = IOUtils.exists(realPath);
        assertFalse(existsFalse);
    }

    @Test(expected = RuntimeException.class)
    public void operation_deleteTwice_exception() throws Exception {
        begin();
        delete(FILE_B_PATH);
        delete(FILE_B_PATH);
    }
}
