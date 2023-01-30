package ch.ethz.sis.afsserver.core;

import ch.ethz.sis.afs.api.dto.File;
import ch.ethz.sis.afsserver.AbstractTest;
import ch.ethz.sis.afsserver.ServerClientEnvironmentFS;
import ch.ethz.sis.afsserver.api.PublicAPI;
import ch.ethz.sis.afsserver.startup.AtomicFileSystemServerParameter;
import ch.ethz.sis.shared.exception.ThrowableReason;
import ch.ethz.sis.shared.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public abstract class PublicApiTest extends AbstractTest {

    public abstract PublicAPI getPublicAPI() throws Exception;

    public static final String ROOT = IOUtils.PATH_SEPARATOR_AS_STRING;
//    public static final String DIR_A = "A";
//    public static final String DIR_B = "B";
    public static final String FILE_A = "A.txt";
    public static final byte[] DATA = "ABCD".getBytes();
    public static final String FILE_B = "B.txt";
//    public static final String DIR_A_PATH = IOUtils.PATH_SEPARATOR + getPath(DIR_A);
//    public static final String DIR_B_PATH = IOUtils.PATH_SEPARATOR + getPath(DIR_B);
//    public static final String FILE_A_PATH = getPath(DIR_A_PATH, FILE_A);
//    public static final String FILE_B_PATH = getPath(DIR_B_PATH, FILE_B);

    public String owner = UUID.randomUUID().toString();

    @Before
    public void createTestData() throws IOException {
        String storageRoot = ServerClientEnvironmentFS.getInstance()
                .getDefaultServerConfiguration().getStringProperty(AtomicFileSystemServerParameter.storageRoot);
        String testDataRoot = IOUtils.getPath(storageRoot, owner.toString());
        IOUtils.createDirectories(testDataRoot);
        String testDataFile = IOUtils.getPath(testDataRoot, FILE_A);
        IOUtils.createFile(testDataFile);
        IOUtils.write(testDataFile, 0, DATA);
    }

    @After
    public void deleteTestData() throws IOException {
        String storageRoot = ServerClientEnvironmentFS.getInstance()
                .getDefaultServerConfiguration().getStringProperty(AtomicFileSystemServerParameter.storageRoot);
        IOUtils.delete(storageRoot);
        String writeAheadLogRoot = ServerClientEnvironmentFS.getInstance()
                .getDefaultServerConfiguration().getStringProperty(AtomicFileSystemServerParameter.writeAheadLogRoot);
        IOUtils.delete(writeAheadLogRoot);
    }

    @Test
    public void list() throws Exception {
        List<File> list = getPublicAPI().list(owner, ROOT, Boolean.TRUE);
        assertEquals(1, list.size());
        assertEquals(FILE_A, list.get(0).getName());
    }

    @Test
    public void read() throws Exception {
        byte[] bytes = getPublicAPI().read(owner, "/" + FILE_A, 0L, DATA.length);
        assertArrayEquals(DATA, bytes);
    }

    @Test(expected = RuntimeException.class)
    public void read_big_failure() throws Exception {
        byte[] bytes = getPublicAPI().read(owner, "/" + FILE_A, 0L, Integer.MAX_VALUE);
        assertArrayEquals(DATA, bytes);
    }

    @Test
    public void write() throws Exception {
        getPublicAPI().write(owner, "/" + FILE_B, 0L, DATA, IOUtils.getMD5(DATA));
        byte[] bytes = getPublicAPI().read(owner, "/" + FILE_B, 0L, DATA.length);
        assertArrayEquals(DATA, bytes);
    }

    @Test
    public void delete() throws Exception {
        Boolean deleted = getPublicAPI().delete(owner, "/" + FILE_A);
        assertTrue(deleted);
        List<File> list = getPublicAPI().list(owner, ROOT, Boolean.TRUE);
        assertEquals(0, list.size());
    }

    @Test
    public void copy() throws Exception {
        getPublicAPI().copy(owner, "/" + FILE_A, owner, "/" + FILE_B);
        byte[] bytes = getPublicAPI().read(owner, "/" + FILE_B, 0L, DATA.length);
        assertArrayEquals(DATA, bytes);
    }

    @Test
    public void move() throws Exception {
        getPublicAPI().move(owner, "/" + FILE_A, owner, "/" + FILE_B);
        List<File> list = getPublicAPI().list(owner, ROOT, Boolean.TRUE);
        assertEquals(1, list.size());
        assertEquals(FILE_B, list.get(0).getName());
    }
}