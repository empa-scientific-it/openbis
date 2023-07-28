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

import ch.ethz.sis.afs.api.dto.File;
import ch.ethz.sis.shared.io.IOUtils;
import ch.ethz.sis.afsjson.JsonObjectMapper;
import ch.ethz.sis.afs.AFSEnvironment;
import ch.ethz.sis.afs.AbstractTest;
import ch.ethz.sis.afs.dto.Transaction;
import ch.ethz.sis.afs.startup.AtomicFileSystemParameter;
import org.junit.After;
import org.junit.Before;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static ch.ethz.sis.shared.io.IOUtils.createDirectories;
import static ch.ethz.sis.shared.io.IOUtils.createDirectory;
import static ch.ethz.sis.shared.io.IOUtils.createFile;
import static ch.ethz.sis.shared.io.IOUtils.getPath;
import static ch.ethz.sis.shared.io.IOUtils.readWritePermissions;
import static ch.ethz.sis.shared.io.IOUtils.setFilePermissions;

public abstract class AbstractTransactionConnectionTest extends AbstractTest {

    private JsonObjectMapper jsonObjectMapper;
    private LockManager<UUID, String> lockManager;
    private TransactionConnection transaction;

    public static final String ROOT = IOUtils.PATH_SEPARATOR_AS_STRING;
    public static final String DIR_A = "A";
    public static final String DIR_B = "B";
    public static final String FILE_A = "A.txt";
    public static final byte[] DATA = "ABCD".getBytes();
    public static final String FILE_B = "B.txt";
    public static final String DIR_BC = "B/C";
    public static final String FILE_C = "C.txt";
    public static final String DIR_A_PATH = IOUtils.PATH_SEPARATOR + getPath(DIR_A);
    public static final String DIR_B_PATH = IOUtils.PATH_SEPARATOR + getPath(DIR_B);
    public static final String DIR_BC_PATH = IOUtils.PATH_SEPARATOR + getPath(DIR_BC);
    public static final String FILE_A_PATH = getPath(DIR_A_PATH, FILE_A);
    public static final String FILE_B_PATH = getPath(DIR_B_PATH, FILE_B);
    public static final String FILE_C_PATH = getPath(DIR_BC_PATH, FILE_C);

    @Before
    public void setupTransaction() throws Exception {
        String writeAheadLogRoot = AFSEnvironment.getDefaultAFSConfig().getStringProperty(AtomicFileSystemParameter.writeAheadLogRoot);
        String storageRoot = AFSEnvironment.getDefaultAFSConfig().getStringProperty(AtomicFileSystemParameter.storageRoot);
        jsonObjectMapper = AFSEnvironment.getDefaultAFSConfig().getSharableInstance(AtomicFileSystemParameter.jsonObjectMapperClass);
        lockManager = new LockManager<>(new PathLockFinder());
        transaction = new TransactionConnection(lockManager, jsonObjectMapper, writeAheadLogRoot, storageRoot, new RecoveredTransactions());
    }

    @Before
    public void createTestData() throws Exception {
        String baseDir = AFSEnvironment.getDefaultAFSConfig().getStringProperty(AtomicFileSystemParameter.storageRoot);
        createDirectories(baseDir);
        createDirectory(getPath(baseDir, DIR_A));
        createFile(getPath(baseDir, DIR_A, FILE_A));
        IOUtils.write(getPath(baseDir, DIR_A, FILE_A), 0, DATA);
        createDirectory(getPath(baseDir, DIR_B));
        createFile(getPath(baseDir, DIR_B, FILE_B));
        createDirectory(getPath(baseDir, DIR_BC));
        createFile(getPath(baseDir, DIR_BC, FILE_C));
        IOUtils.write(getPath(baseDir, DIR_BC, FILE_C), 0, DATA);
    }


    @After
    public void cleanTransaction() throws Exception {
        if (transaction.getState() != State.Commit &&
                transaction.getState() != State.Executed &&
                transaction.getState() != State.Rollback) {
            transaction.rollback();
        }
        jsonObjectMapper = null;
        lockManager = null;
        transaction = null;
    }

    @After
    public void deleteTestData() throws Exception {
        String storageRoot = AFSEnvironment.getDefaultAFSConfig().getStringProperty(AtomicFileSystemParameter.storageRoot);
        deleteIfExists(storageRoot);
        String writeAheadLogRoot = AFSEnvironment.getDefaultAFSConfig().getStringProperty(AtomicFileSystemParameter.writeAheadLogRoot);
        deleteIfExists(writeAheadLogRoot);
    }

    private void deleteIfExists(String source) throws IOException {
        if (IOUtils.exists(source)) {
            setFilePermissions(source, readWritePermissions);
            boolean canDeleteSafely = IOUtils.hasWritePermissions(source);
            if (canDeleteSafely) {
                IOUtils.delete(source);
            } else {
                throw new RuntimeException("Can't be deleted safely? WTF!");
            }
        }
    }

    public Transaction getTransaction() {
        return transaction.getTransaction();
    }

    public TransactionConnection getTransactionConnection() {
        return transaction;
    }

    public void begin() throws Exception {
        transaction.begin(UUID.randomUUID());
    }

    public void prepare() throws Exception {
        transaction.prepare();
    }

    public void rollback() throws Exception {
        transaction.rollback();
    }

    public void commit() throws Exception {
        transaction.commit();
    }

    public List<File> list(String source, boolean recursively) throws Exception {
        return transaction.list(source, recursively);
    }

    public byte[] read(String source, long offset, int limit) throws Exception {
        return transaction.read(source, offset, limit);
    }

    public boolean write(String source, long offset, byte[] data, byte[] md5Hash) throws Exception {
        return transaction.write(source, offset, data, md5Hash);
    }

    public boolean delete(String source) throws Exception {
        return transaction.delete(source);
    }

    public boolean copy(String source, String target) throws Exception {
        return transaction.copy(source, target);
    }

    public boolean move(String source, String target) throws Exception {
        return transaction.move(source, target);
    }

    public boolean create(final String source, final boolean directory) throws Exception {
        return transaction.create(source, directory);
    }

}
