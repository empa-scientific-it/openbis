package ch.ethz.sis.afs.manager.operation;

import ch.ethz.sis.shared.io.File;
import ch.ethz.sis.shared.io.IOUtils;
import org.junit.Test;

import static ch.ethz.sis.shared.io.IOUtils.getMD5;
import static org.junit.Assert.assertEquals;

public class WriteTransactionOperationTest extends AbstractTransactionOperationTest {

    @Override
    public void operation() throws Exception {
        write(FILE_B_PATH, 0, DATA, getMD5(DATA));
    }

    @Test
    public void operation_write_succeed() throws Exception {
        begin();
        String realPath = OperationExecutor.getRealPath(getTransaction(), FILE_B_PATH);
        File beforeWrite = IOUtils.getFile(realPath);
        write(FILE_B_PATH, 0, DATA, getMD5(DATA));
        File afterWrite = IOUtils.getFile(realPath);
        assertEquals(1, getTransaction().getOperations().size());
        assertEquals(beforeWrite, afterWrite);
        prepare();
        commit();
        File afterCommit = IOUtils.getFile(realPath);
        assertEquals(DATA.length, (long) afterCommit.getSize());
    }

    @Test(expected = RuntimeException.class)
    public void operation_writeDirectory_exception() throws Exception {
        begin();
        write(DIR_B_PATH, 0, DATA, getMD5(DATA));
    }

    @Test
    public void operation_writeTwice_succeed() throws Exception {
        begin();
        String realPath = OperationExecutor.getRealPath(getTransaction(), FILE_B_PATH);
        File beforeWrite = IOUtils.getFile(realPath);
        write(FILE_B_PATH, 0, DATA, getMD5(DATA));
        write(FILE_B_PATH, DATA.length, DATA, getMD5(DATA));
        File afterWrite = IOUtils.getFile(realPath);
        assertEquals(2, getTransaction().getOperations().size());
        assertEquals(beforeWrite, afterWrite);
        prepare();
        commit();
        File afterCommit = IOUtils.getFile(realPath);
        assertEquals(DATA.length * 2, (long) afterCommit.getSize());
    }


    @Test(expected = RuntimeException.class)
    public void operation_writeWrongMD5_exception() throws Exception {
        begin();
        write(FILE_B_PATH, 0, DATA, "WRONG_MD5".getBytes());
    }

    @Test
    public void operation_writeEmpty_succeed() throws Exception {
        begin();
        String realPath = OperationExecutor.getRealPath(getTransaction(), FILE_B_PATH);
        File beforeWrite = IOUtils.getFile(realPath);
        byte[] empty = new byte[0];
        write(FILE_B_PATH, 0, empty, getMD5(empty));
        File afterWrite = IOUtils.getFile(realPath);
        assertEquals(1, getTransaction().getOperations().size());
        assertEquals(beforeWrite, afterWrite);
        prepare();
        commit();
        File afterCommit = IOUtils.getFile(realPath);
        assertEquals(empty.length, (long) afterCommit.getSize());
    }

    @Test
    public void operation_writeOver_succeed() throws Exception {
        begin();
        String realPath = OperationExecutor.getRealPath(getTransaction(), FILE_B_PATH);
        File beforeWrite = IOUtils.getFile(realPath);
        write(FILE_B_PATH, 4, DATA, getMD5(DATA));
        File afterWrite = IOUtils.getFile(realPath);
        assertEquals(1, getTransaction().getOperations().size());
        assertEquals(beforeWrite.getPath(), afterWrite.getPath());
        assertEquals(beforeWrite.getName(), afterWrite.getName());
        assertEquals(beforeWrite.getDirectory(), afterWrite.getDirectory());
        assertEquals(beforeWrite.getSize(), afterWrite.getSize());
        prepare();
        commit();
        File afterCommit = IOUtils.getFile(realPath);
        assertEquals(4 + DATA.length, (long) afterCommit.getSize());
    }
}
