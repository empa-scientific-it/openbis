/*
 *  Copyright ETH 2023 Zürich, Scientific IT Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package ch.ethz.sis.afs.manager.operation;

import static ch.ethz.sis.shared.io.IOUtils.getPath;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ch.ethz.sis.afs.api.dto.File;
import ch.ethz.sis.shared.io.IOUtils;

public class CreateTransactionOperationTest extends AbstractTransactionOperationTest
{

    public static final String DIR_C = "C";

    public static final String FILE_C = "C.txt";

    public static final String DIR_C_PATH = IOUtils.PATH_SEPARATOR + getPath(DIR_C);

    public static final String FILE_C_PATH = IOUtils.PATH_SEPARATOR + getPath(DIR_C, FILE_C);

    @Override
    public void operation() throws Exception {
        create(FILE_C_PATH, false);
    }

    @Test
    public void operation_createFile_succeed() throws Exception {
        begin();
        final String realPathC = OperationExecutor.getRealPath(getTransaction(), FILE_C_PATH);
        create(FILE_C_PATH, false);
        assertEquals(1, getTransaction().getOperations().size());
        assertFalse(IOUtils.exists(realPathC));
        prepare();
        commit();
        assertTrue(IOUtils.exists(realPathC));

        final File file = IOUtils.getFile(realPathC);
        assertFalse(file.getDirectory());
    }

    @Test
    public void operation_createDirectory_succeed() throws Exception {
        begin();
        final String realPathC = OperationExecutor.getRealPath(getTransaction(), DIR_C_PATH);
        create(DIR_C_PATH, true);
        assertEquals(1, getTransaction().getOperations().size());
        assertFalse(IOUtils.exists(realPathC));
        prepare();
        commit();
        assertTrue(IOUtils.exists(realPathC));

        final File file = IOUtils.getFile(realPathC);
        assertTrue(file.getDirectory());
    }

}
