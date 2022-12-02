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

package ch.ethz.sis.afs.manager.operation;

import ch.ethz.sis.afs.api.dto.File;
import ch.ethz.sis.shared.io.IOUtils;
import org.junit.Test;

import static ch.ethz.sis.shared.io.IOUtils.getPath;
import static org.junit.Assert.*;

public class CopyTransactionOperationTest extends AbstractTransactionOperationTest {

    public static final String DIR_C = "C";
    public static final String FILE_C = "C.txt";
    public static final String DIR_C_PATH = IOUtils.PATH_SEPARATOR + getPath(DIR_C);
    public static final String FILE_C_PATH = IOUtils.PATH_SEPARATOR + getPath(DIR_C, FILE_C);

    @Override
    public void operation() throws Exception {
        copy(FILE_A_PATH, FILE_C_PATH);
    }

    @Test
    public void operation_copyFile_succeed() throws Exception {
        begin();
        String realPathA = OperationExecutor.getRealPath(getTransaction(), FILE_A_PATH);
        String realPathC = OperationExecutor.getRealPath(getTransaction(), FILE_C_PATH);
        File before = IOUtils.getFile(realPathA);
        copy(FILE_A_PATH, FILE_C_PATH);
        File after = IOUtils.getFile(realPathA);
        assertEquals(1, getTransaction().getOperations().size());
        assertTrue(after.getLastAccessTime().isAfter(before.getLastAccessTime()));
        assertTrue(IOUtils.exists(realPathA));
        assertFalse(IOUtils.exists(realPathC));
        prepare();
        commit();
        assertTrue(IOUtils.exists(realPathA));
        assertTrue(IOUtils.exists(realPathC));
    }

    @Test
    public void operation_copyDirectory_succeed() throws Exception {
        begin();
        String realPathA = OperationExecutor.getRealPath(getTransaction(), DIR_A_PATH);
        String realPathC = OperationExecutor.getRealPath(getTransaction(), DIR_C_PATH);
        File before = IOUtils.getFile(realPathA);
        copy(DIR_A_PATH, DIR_C_PATH);
        File after = IOUtils.getFile(realPathA);
        assertEquals(1, getTransaction().getOperations().size());
        assertTrue(after.getLastAccessTime().isAfter(before.getLastAccessTime()));
        assertTrue(IOUtils.exists(realPathA));
        assertFalse(IOUtils.exists(realPathC));
        prepare();
        commit();
        assertTrue(IOUtils.exists(realPathA));
        assertTrue(IOUtils.exists(realPathC));
    }

    @Test
    public void operation_copyTwice_exception() throws Exception {
        begin();
        copy(FILE_A_PATH, FILE_C_PATH);
        copy(FILE_A_PATH, FILE_C_PATH);
    }
}
