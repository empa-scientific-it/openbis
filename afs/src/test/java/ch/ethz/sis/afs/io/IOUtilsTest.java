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

package ch.ethz.sis.afs.io;

import ch.ethz.sis.afs.api.dto.File;
import ch.ethz.sis.shared.io.IOUtils;
import ch.ethz.sis.afs.AFSEnvironment;
import ch.ethz.sis.afs.AbstractTest;
import ch.ethz.sis.afs.startup.AtomicFileSystemParameter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static ch.ethz.sis.shared.io.IOUtils.NEW_FILE_SIZE;
import static ch.ethz.sis.shared.io.IOUtils.copy;
import static ch.ethz.sis.shared.io.IOUtils.copyFile;
import static ch.ethz.sis.shared.io.IOUtils.createDirectories;
import static ch.ethz.sis.shared.io.IOUtils.createDirectory;
import static ch.ethz.sis.shared.io.IOUtils.createFile;
import static ch.ethz.sis.shared.io.IOUtils.delete;
import static ch.ethz.sis.shared.io.IOUtils.exists;
import static ch.ethz.sis.shared.io.IOUtils.getFile;
import static ch.ethz.sis.shared.io.IOUtils.getMD5;
import static ch.ethz.sis.shared.io.IOUtils.getParentPath;
import static ch.ethz.sis.shared.io.IOUtils.getPath;
import static ch.ethz.sis.shared.io.IOUtils.isFileSystemSupported;
import static ch.ethz.sis.shared.io.IOUtils.isSameVolume;
import static ch.ethz.sis.shared.io.IOUtils.list;
import static ch.ethz.sis.shared.io.IOUtils.move;
import static ch.ethz.sis.shared.io.IOUtils.noPermissions;
import static ch.ethz.sis.shared.io.IOUtils.read;
import static ch.ethz.sis.shared.io.IOUtils.readFully;
import static ch.ethz.sis.shared.io.IOUtils.readPermissions;
import static ch.ethz.sis.shared.io.IOUtils.readWritePermissions;
import static ch.ethz.sis.shared.io.IOUtils.setFilePermissions;
import static ch.ethz.sis.shared.io.IOUtils.write;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class IOUtilsTest extends AbstractTest {

    private static final String DIR_A = "A";
    private static final String DIR_B = "B";
    private static final String FILE_A = "A.txt";
    private static final String FILE_B = "B.txt";
    private static final char NULL = '\u0000';
    private static final String DATA = "ABCD";
    private static final byte[] DATA_BYTES = DATA.getBytes();

    private String baseDir;

    private void deleteIfExists(String source) throws IOException {
        if (IOUtils.exists(source)) {
            setFilePermissions(source, readWritePermissions);
            boolean canDeleteSafely = IOUtils.hasWritePermissions(source);
            if (canDeleteSafely) {
                delete(source);
            } else {
                throw new RuntimeException("Can't be deleted safely? WTF!");
            }
        }
    }

    @Before
    public void createBaseDir() throws Exception {
        baseDir = AFSEnvironment.getDefaultAFSConfig().getStringProperty(AtomicFileSystemParameter.storageRoot);
        deleteIfExists(baseDir);
        createDirectories(baseDir);
    }

    @After
    public void deleteBaseDir() throws Exception {
        deleteIfExists(baseDir);
        baseDir = null;
    }

    @Test
    public void createDirectory_success() throws IOException {
        String toCreate = getPath(baseDir, DIR_A);
        createDirectory(toCreate);
        assertTrue(exists(toCreate));
        assertTrue(IOUtils.getFile(toCreate).getDirectory());
    }

    @Test(expected = IOException.class)
    public void createDirectory_exception() throws IOException {
        String toCreate = getPath(baseDir, DIR_A, DIR_B);
        createDirectory(toCreate);
    }

    @Test
    public void createDirectories_success() throws IOException {
        String toCreate = getPath(baseDir, DIR_A, DIR_B);
        createDirectories(toCreate);
        assertTrue(exists(toCreate));
        assertTrue(IOUtils.getFile(toCreate).getDirectory());
    }

    @Test
    public void createFile_success() throws IOException {
        String toCreate = getPath(baseDir, FILE_A);
        createFile(toCreate);
        assertTrue(exists(toCreate));
        assertTrue(!IOUtils.getFile(toCreate).getDirectory());
    }

    @Test(expected = IOException.class)
    public void createFile_exception() throws IOException {
        String toCreate = getPath(baseDir, DIR_A, FILE_A);
        createFile(toCreate);
    }

    @Test
    public void exists_true_success() throws IOException {
        assertTrue(exists(baseDir));
    }

    @Test
    public void exists_false_success() throws IOException {
        String idontexist = getPath(baseDir, DIR_A);
        assertFalse(exists(idontexist));
    }

    @Test
    public void list_success() throws IOException {
        String toCreateDirA = getPath(baseDir, DIR_A);
        createDirectory(toCreateDirA);

        String toCreateFileA = getPath(baseDir, FILE_A);
        createFile(toCreateFileA);

        List<File> files = list(baseDir, false);
        assertTrue(files.size() == 2);
    }

    @Test
    public void list_recursively_success() throws IOException {
        String toCreateDirA = getPath(baseDir, DIR_A);
        createDirectory(toCreateDirA);

        String toCreateFileA = getPath(baseDir, FILE_A);
        createFile(toCreateFileA);

        List<File> files = list(baseDir, true);
        assertTrue(files.size() == 2);
    }

    @Test(expected = IOException.class)
    public void list_directoryDontExists_exception() throws IOException {
        String idontexist = getPath(baseDir, DIR_A, DIR_B);
        assertFalse(exists(idontexist));
        list(idontexist, false);
    }

    @Test(expected = IOException.class)
    public void list_recursively_directoryDontExists_exception() throws IOException {
        String idontexist = getPath(baseDir, DIR_A, DIR_B);
        assertFalse(exists(idontexist));
        list(idontexist, true);
    }

    @Test(expected = IOException.class)
    public void list_file_exception() throws IOException {
        String toCreateFileA = getPath(baseDir, FILE_A);
        createFile(toCreateFileA);
        list(toCreateFileA, false);
    }

    @Test(expected = IOException.class)
    public void list_file_recursively_exception() throws IOException {
        String toCreateFileA = getPath(baseDir, FILE_A);
        createFile(toCreateFileA);
        list(toCreateFileA, true);
    }

    @Test
    public void getFile_directoryProperties_success() throws IOException {
        String toCreateDirA = getPath(baseDir, DIR_A);
        createDirectory(toCreateDirA);

        File dirA = getFile(toCreateDirA);

        // Testing expected directory results
        assertTrue(dirA.getPath().endsWith(toCreateDirA));
        assertTrue(dirA.getName().equals(DIR_A));
        assertTrue(dirA.getDirectory() == Boolean.TRUE);
        assertTrue(dirA.getSize() == null);
    }

    @Test
    public void getFile_fileProperties_success() throws IOException {
        String toCreateFileA = getPath(baseDir, FILE_A);
        createFile(toCreateFileA);

        File fileA = getFile(toCreateFileA);

        // Testing expected file results
        assertTrue(fileA.getPath().endsWith(toCreateFileA));
        assertTrue(fileA.getName().equals(FILE_A));
        assertTrue(fileA.getDirectory() == Boolean.FALSE);
        assertTrue(fileA.getSize() == NEW_FILE_SIZE);
    }

    @Test(expected = IOException.class)
    public void delete_dontexists_exception() throws IOException {
        String idontexist = getPath(baseDir, DIR_A, DIR_B);
        assertFalse(exists(idontexist));
        delete(idontexist);
    }

    @Test
    public void delete_file_success() throws IOException {
        String toCreateFileA = getPath(baseDir, FILE_A);
        createFile(toCreateFileA);

        assertTrue(exists(toCreateFileA));
        delete(toCreateFileA);
        assertFalse(exists(toCreateFileA));
    }

    @Test
    public void delete_directory_success() throws IOException {
        String toCreateDirA = getPath(baseDir, DIR_A);
        createDirectory(toCreateDirA);

        assertTrue(exists(toCreateDirA));
        delete(toCreateDirA);
        assertFalse(exists(toCreateDirA));
    }

    @Test(expected = IOException.class)
    public void write_dontexists_exception() throws IOException {
        String dontexists = getPath(baseDir, FILE_A);
        write(dontexists, 0, DATA_BYTES);
    }

    @Test
    public void write_fromStart_success() throws IOException {
        String toCreateFileA = getPath(baseDir, FILE_A);
        createFile(toCreateFileA);

        write(toCreateFileA, 0, DATA_BYTES);
        byte[] dataReaded = read(toCreateFileA, 0, DATA_BYTES.length);
        assertArrayEquals(DATA_BYTES, dataReaded);
        assertEquals(DATA_BYTES.length, (long) getFile(toCreateFileA).getSize());
    }

    @Test
    public void write_fromStartEmpty_success() throws IOException {
        String toCreateFileA = getPath(baseDir, FILE_A);
        createFile(toCreateFileA);
        byte[] empty = new byte[0];
        write(toCreateFileA, 0, empty);
        byte[] dataReaded = read(toCreateFileA, 0, empty.length);
        assertArrayEquals(empty, dataReaded);
        assertEquals(empty.length, (long) getFile(toCreateFileA).getSize());
    }

    @Test
    public void write_append_success() throws IOException {
        write_fromStart_success();

        String toCreateFileA = getPath(baseDir, FILE_A);
        File fileA = getFile(toCreateFileA);

        write(toCreateFileA, fileA.getSize(), DATA_BYTES);
        assertEquals(DATA_BYTES.length * 2, (long) getFile(toCreateFileA).getSize());
    }

    @Test
    public void write_appendRandomMiddle_success() throws IOException {
        write_fromStart_success();

        String toCreateFileA = getPath(baseDir, FILE_A);

        write(toCreateFileA, 2, DATA_BYTES);
        File fileA = getFile(toCreateFileA);
        assertEquals(6, (long) fileA.getSize());
        byte[] dataReaded = read(toCreateFileA, 0, 6);
        assertEquals("AB" + DATA, new String(dataReaded));
    }

    @Test
    public void write_appendRandomOverEnd_success() throws IOException {
        write_fromStart_success();

        String toCreateFileA = getPath(baseDir, FILE_A);

        write(toCreateFileA, 5, DATA_BYTES);
        File fileA = getFile(toCreateFileA);
        assertEquals(9, (long) fileA.getSize());
        byte[] dataReaded = read(toCreateFileA, 0, 9);
        assertEquals(DATA + NULL + DATA, new String(dataReaded));
    }

    @Test
    public void write_readFully_success() throws IOException {
        String toCreateFileA = getPath(baseDir, FILE_A);
        write_fromStart_success();
        byte[] dataReaded = readFully(toCreateFileA);
        assertArrayEquals(DATA_BYTES, dataReaded);
        assertEquals(DATA_BYTES.length, (long) getFile(toCreateFileA).getSize());
    }

    @Test
    public void write_readPartiallyA_success() throws IOException {
        String toCreateFileA = getPath(baseDir, FILE_A);
        write_fromStart_success();
        byte[] dataReaded = read(toCreateFileA, 0, 2);
        assertEquals("AB", new String(dataReaded));
    }

    @Test
    public void write_readPartiallyB_success() throws IOException {
        String toCreateFileA = getPath(baseDir, FILE_A);
        write_fromStart_success();
        byte[] dataReaded = read(toCreateFileA, 2, 2);
        assertEquals("CD", new String(dataReaded));
    }

    @Test(expected = IOException.class)
    public void write_readOver_exception() throws IOException {
        String toCreateFileA = getPath(baseDir, FILE_A);
        write_fromStart_success();
        read(toCreateFileA, 0, 5);
    }

    @Test
    public void move_file_success() throws IOException {
        write_fromStart_success();
        String toCreateFileA = getPath(baseDir, FILE_A);
        String toCreateFileB = getPath(baseDir, FILE_B);
        move(toCreateFileA, toCreateFileB);
        assertFalse(exists(toCreateFileA));
        assertTrue(exists(toCreateFileB));
        assertEquals(4, (long) getFile(toCreateFileB).getSize());
    }

    @Test
    public void move_fileOverride_success() throws IOException {
        write_fromStart_success();
        String toCreateFileA = getPath(baseDir, FILE_A);
        String toCreateFileB = getPath(baseDir, FILE_B);
        createFile(toCreateFileB);
        move(toCreateFileA, toCreateFileB);
        assertFalse(exists(toCreateFileA));
        assertTrue(exists(toCreateFileB));
        assertEquals(4, (long) getFile(toCreateFileB).getSize());
    }

    @Test
    public void move_directory_success() throws IOException {
        String toCreateDirA = getPath(baseDir, DIR_A);
        String toCreateDirB = getPath(baseDir, DIR_B);
        createDirectory(toCreateDirA);
        assertTrue(IOUtils.getFile(toCreateDirA).getDirectory());
        move(toCreateDirA, toCreateDirB);
        assertFalse(exists(toCreateDirA));
        assertTrue(exists(toCreateDirB));
    }

    @Test(expected = IOException.class)
    public void move_dontexists_exception() throws IOException {
        String toCreateFileA = getPath(baseDir, FILE_A);
        String toCreateFileB = getPath(baseDir, FILE_B);
        move(toCreateFileA, toCreateFileB);
    }

    @Test
    public void copy_file_success() throws IOException {
        write_fromStart_success();
        String toCreateFileA = getPath(baseDir, FILE_A);
        String toCreateFileB = getPath(baseDir, FILE_B);
        copy(toCreateFileA, toCreateFileB);
        assertTrue(exists(toCreateFileA));
        assertTrue(exists(toCreateFileB));
        assertEquals(getFile(toCreateFileB).getSize(),
                getFile(toCreateFileA).getSize());
    }

    @Test
    public void copy_directory_success() throws IOException {
        String toCreateDirA = getPath(baseDir, DIR_A);
        createDirectory(toCreateDirA);
        String toCreateFileA = getPath(baseDir, DIR_A, FILE_A);
        createFile(toCreateFileA);
        String toCreateDirB = getPath(baseDir, DIR_B);
        createDirectory(toCreateDirB);

        assertTrue(IOUtils.getFile(toCreateDirA).getDirectory());
        copy(toCreateDirA, toCreateDirB);
        assertTrue(exists(toCreateDirA));
        assertTrue(exists(toCreateDirB));
    }

    @Test
    public void copyFile_Success() throws IOException {
        write_fromStart_success();
        String toCreateFileA = getPath(baseDir, FILE_A);
        String toCreateFileB = getPath(baseDir, FILE_B);
        copyFile(toCreateFileA, 0, 2, toCreateFileB, 0);
        assertTrue(exists(toCreateFileA));
        assertTrue(exists(toCreateFileB));
        assertEquals(4,
                (long) getFile(toCreateFileA).getSize());
        assertEquals(2,
                (long) getFile(toCreateFileB).getSize());
        byte[] dataReaded = readFully(toCreateFileB);
        assertEquals("AB", new String(dataReaded));
    }

    @Test(expected = IOException.class)
    public void file_noRightsFile_read_exception() throws IOException {
        String toCreate = getPath(baseDir, FILE_A);
        createFile(toCreate);
        write(toCreate, 0, DATA_BYTES);
        File file = IOUtils.getFile(toCreate);
        assertEquals(DATA_BYTES.length, (long) file.getSize());
        setFilePermissions(toCreate, noPermissions);
        boolean canModify = IOUtils.hasWritePermissions(toCreate);
        assertFalse(canModify);
        read(toCreate, 0, DATA_BYTES.length);
    }

    @Test(expected = IOException.class)
    public void file_readOnlyFile_write_exception() throws IOException {
        String toCreate = getPath(baseDir, FILE_A);
        createFile(toCreate);
        setFilePermissions(toCreate, readPermissions);
        boolean canModify = IOUtils.hasWritePermissions(toCreate);
        assertFalse(canModify);
        write(toCreate, 0, DATA_BYTES);
    }

    @Test
    public void folder_readOnlyFolder_canModify() throws IOException {
        String toCreate = getPath(baseDir, DIR_A);
        createDirectory(toCreate);
        setFilePermissions(toCreate, readPermissions);
        boolean canModify = IOUtils.hasWritePermissions(toCreate);
        assertFalse(canModify);
        // Will Fail in Posix but work in DOS
        // createFile(getPath(baseDir, DIR_A, FILE_A));
    }

    @Test(expected = IOException.class)
    public void file_readOnlyFolder_delete_exception() throws IOException {
        String toCreate = getPath(baseDir, FILE_A);
        createFile(toCreate);
        setFilePermissions(getPath(baseDir), readPermissions);
        boolean canModify = IOUtils.hasWritePermissions(getPath(baseDir));
        assertFalse(canModify);
        delete(toCreate);
    }

    @Test(expected = IOException.class)
    public void file_readOnlyFile_delete_exception() throws IOException {
        String toCreate = getPath(baseDir, FILE_A);
        createFile(toCreate);
        setFilePermissions(toCreate, readPermissions);
        boolean canModify = IOUtils.hasWritePermissions(toCreate);
        assertFalse(canModify);
        delete(toCreate);
    }

    @Test(expected = IOException.class)
    public void folder_readOnlyFolder_delete_exception() throws IOException {
        String toCreate = getPath(baseDir, DIR_A);
        createDirectory(toCreate);
        setFilePermissions(toCreate, readPermissions);
        boolean canModify = IOUtils.hasWritePermissions(toCreate);
        assertFalse(canModify);
        delete(getPath(baseDir));
    }

    @Test
    public void folder_parentPath_success() {
        String dir = getPath(baseDir, DIR_A);
        String parent = getParentPath(dir);
        assertEquals(getPath(baseDir), parent);
    }

    @Test
    public void file_parentPath_success() {
        String file = getPath(baseDir, FILE_A);
        String parent = getParentPath(file);
        assertEquals(getPath(baseDir), parent);
    }

    @Test
    public void md5_success() {
        byte[] md5 = getMD5(DATA_BYTES);
    }

    @Test(expected = RuntimeException.class)
    public void md5_exception() {
        getMD5(null);
    }

    @Test
    public void isFileSystemSupported_success() {
        assertTrue(isFileSystemSupported());
    }

    @Test
    public void isSameVolume_success() throws IOException {
        String dirA = getPath(baseDir, DIR_A);
        createDirectory(dirA);
        String dirB = getPath(baseDir, DIR_B);
        createDirectory(dirB);
        assertTrue(isSameVolume(dirA, dirB));
    }
}
