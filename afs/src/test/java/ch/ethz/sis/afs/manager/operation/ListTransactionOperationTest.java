package ch.ethz.sis.afs.manager.operation;

import ch.ethz.sis.shared.io.File;
import org.junit.Test;

import java.nio.file.NoSuchFileException;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class ListTransactionOperationTest extends AbstractTransactionOperationTest {

    @Override
    public void operation() throws Exception {
        list(ROOT, false);
    }

    @Test
    public void operation_list_succeed() throws Exception {
        begin();
        List<File> list = list(ROOT, false);
        assertEquals(2, list.size());
        assertEquals(0, getTransaction().getOperations().size());
    }

    @Test(expected = NoSuchFileException.class)
    public void operation_list_exception() throws Exception {
        begin();
        list(ROOT + UUID.randomUUID().toString(), false);
    }

    @Test
    public void operation_list_recursively_succeed() throws Exception {
        begin();
        List<File> list = list(ROOT, true);
        assertEquals(4, list.size());
        assertEquals(0, getTransaction().getOperations().size());
    }
}
