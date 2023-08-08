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
package ch.ethz.sis.afsserver.core;

import ch.ethz.sis.afsapi.dto.File;
import ch.ethz.sis.afsserver.AbstractTest;
import ch.ethz.sis.afsserver.ServerClientEnvironmentFS;
import ch.ethz.sis.afsapi.api.PublicAPI;
import ch.ethz.sis.afsserver.startup.AtomicFileSystemServerParameter;
import ch.ethz.sis.shared.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public abstract class PublicApiTest extends AbstractTest
{

    public abstract PublicAPI getPublicAPI() throws Exception;

    public abstract PublicAPI getPublicAPI(String interactiveSessionKey, String transactionManagerKey) throws Exception;

    public static final String ROOT = IOUtils.PATH_SEPARATOR_AS_STRING;

    public static final String FILE_A = "A.txt";

    public static final byte[] DATA = "ABCD".getBytes();

    public static final String FILE_B = "B.txt";

    public String owner = UUID.randomUUID().toString();

    @Before
    public void createTestData() throws IOException
    {
        String storageRoot = ServerClientEnvironmentFS.getInstance()
                .getDefaultServerConfiguration()
                .getStringProperty(AtomicFileSystemServerParameter.storageRoot);
        String testDataRoot = IOUtils.getPath(storageRoot, owner.toString());
        IOUtils.createDirectories(testDataRoot);
        String testDataFile = IOUtils.getPath(testDataRoot, FILE_A);
        IOUtils.createFile(testDataFile);
        IOUtils.write(testDataFile, 0, DATA);
    }

    @After
    public void deleteTestData() throws IOException
    {
        String storageRoot = ServerClientEnvironmentFS.getInstance()
                .getDefaultServerConfiguration()
                .getStringProperty(AtomicFileSystemServerParameter.storageRoot);
        IOUtils.delete(storageRoot);
        String writeAheadLogRoot = ServerClientEnvironmentFS.getInstance()
                .getDefaultServerConfiguration()
                .getStringProperty(AtomicFileSystemServerParameter.writeAheadLogRoot);
        IOUtils.delete(writeAheadLogRoot);
    }

    @Test
    public void list() throws Exception
    {
        List<File> list = getPublicAPI().list(owner, ROOT, Boolean.TRUE);
        assertEquals(1, list.size());
        assertEquals(FILE_A, list.get(0).getName());
    }

    @Test
    public void read() throws Exception
    {
        byte[] bytes = getPublicAPI().read(owner, FILE_A, 0L, DATA.length);
        assertArrayEquals(DATA, bytes);
    }

    @Test(expected = RuntimeException.class)
    public void read_big_failure() throws Exception
    {
        byte[] bytes = getPublicAPI().read(owner, FILE_A, 0L, Integer.MAX_VALUE);
        assertArrayEquals(DATA, bytes);
    }

    @Test
    public void write() throws Exception
    {
        getPublicAPI().write(owner,  FILE_B, 0L, DATA, IOUtils.getMD5(DATA));
        byte[] bytes = getPublicAPI().read(owner, FILE_B, 0L, DATA.length);
        assertArrayEquals(DATA, bytes);
    }

    @Test
    public void delete() throws Exception
    {
        Boolean deleted = getPublicAPI().delete(owner, FILE_A);
        assertTrue(deleted);
        List<File> list = getPublicAPI().list(owner, ROOT, Boolean.TRUE);
        assertEquals(0, list.size());
    }

    @Test
    public void copy() throws Exception
    {
        getPublicAPI().copy(owner, FILE_A, owner, FILE_B);
        byte[] bytes = getPublicAPI().read(owner, FILE_B, 0L, DATA.length);
        assertArrayEquals(DATA, bytes);
    }

    @Test
    public void move() throws Exception
    {
        getPublicAPI().move(owner, FILE_A, owner, FILE_B);
        List<File> list = getPublicAPI().list(owner, ROOT, Boolean.TRUE);
        assertEquals(1, list.size());
        assertEquals(FILE_B, list.get(0).getName());
    }

    @Test
    public void create_directory() throws Exception
    {
        getPublicAPI().create(owner, FILE_B, Boolean.TRUE);

        final List<File> list = getPublicAPI().list(owner, ROOT, Boolean.TRUE);
        assertEquals(2, list.size());

        final List<File> matchedFiles = list.stream().filter(file -> file.getName().equals(FILE_B)).collect(Collectors.toList());
        assertEquals(1, matchedFiles.size());
        assertTrue(matchedFiles.get(0).getDirectory());
    }

    @Test
    public void create_file() throws Exception
    {
        getPublicAPI().create(owner, FILE_B, Boolean.FALSE);

        final List<File> list = getPublicAPI().list(owner, ROOT, Boolean.TRUE);

        final List<File> matchedFiles = list.stream().filter(file -> file.getName().equals(FILE_B)).collect(Collectors.toList());
        assertEquals(1, matchedFiles.size());
        assertFalse(matchedFiles.get(0).getDirectory());

        byte[] bytes = getPublicAPI().read(owner, FILE_B, 0L, 0);
        assertEquals(0, bytes.length);
    }


    @Test
    public void operation_state_begin_succeed() throws Exception {
        UUID sessionToken = UUID.randomUUID();
        PublicAPI publicAPI = getPublicAPI("1234", null);
        publicAPI.begin(sessionToken);
    }

    @Test
    public void operation_state_prepare_succeed() throws Exception {
        UUID sessionToken = UUID.randomUUID();
        PublicAPI publicAPI = getPublicAPI("1234", "5678");
        publicAPI.begin(sessionToken);
        publicAPI.prepare();
    }

    @Test
    public void operation_state_rollback_succeed() throws Exception {
        UUID sessionToken = UUID.randomUUID();
        PublicAPI publicAPI = getPublicAPI("1234", "5678");
        publicAPI.begin(sessionToken);
        publicAPI.prepare();
        publicAPI.rollback();
    }

    @Test
    public void operation_state_commit_succeed() throws Exception {
        UUID sessionToken = UUID.randomUUID();
        PublicAPI publicAPI = getPublicAPI("1234", null);
        publicAPI.begin(sessionToken);
        publicAPI.commit();
    }

    @Test
    public void operation_state_commitPrepared_succeed() throws Exception {
        UUID sessionToken = UUID.randomUUID();
        PublicAPI publicAPI = getPublicAPI("1234", "5678");
        publicAPI.begin(sessionToken);
        publicAPI.prepare();
        publicAPI.commit();
    }

    @Test
    public void operation_state_commit_reuse_succeed() throws Exception {
        UUID sessionToken = UUID.randomUUID();
        PublicAPI publicAPI = getPublicAPI("1234", "5678");
        publicAPI.begin(sessionToken);
        publicAPI.prepare();
        publicAPI.commit();
        publicAPI.begin(sessionToken);
    }

    @Test
    public void operation_state_rollback_reuse_succeed() throws Exception {
        UUID sessionToken = UUID.randomUUID();
        PublicAPI publicAPI = getPublicAPI("1234", "5678");
        publicAPI.begin(sessionToken);
        publicAPI.prepare();
        publicAPI.rollback();
        publicAPI.begin(sessionToken);
    }

    @Test(expected = RuntimeException.class)
    public void operation_state_begin_reuse_fails() throws Exception {
        UUID sessionToken = UUID.randomUUID();
        PublicAPI publicAPI = getPublicAPI("1234", null);
        publicAPI.begin(sessionToken);
        publicAPI.begin(sessionToken);
    }

    @Test
    public void operation_state_prepare_reuse_succeed() throws Exception {
        UUID sessionToken = UUID.randomUUID();
        PublicAPI publicAPI = getPublicAPI("1234", "5678");
        publicAPI.begin(sessionToken);
        publicAPI.prepare();
        publicAPI.begin(sessionToken);
    }
}