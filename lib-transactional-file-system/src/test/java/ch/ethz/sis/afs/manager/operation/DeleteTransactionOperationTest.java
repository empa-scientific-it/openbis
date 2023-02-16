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
package ch.ethz.sis.afs.manager.operation;

import ch.ethz.sis.afs.api.dto.File;
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
