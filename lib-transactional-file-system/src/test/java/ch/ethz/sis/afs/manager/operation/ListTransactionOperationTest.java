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
        assertEquals(6, list.size());
        assertEquals(0, getTransaction().getOperations().size());
    }

    @Test(expected = RuntimeException.class)
    public void operation_list_after_delete_exception() throws Exception {
        begin();
        delete(DIR_B_PATH);
        List<File> list = list(DIR_BC_PATH, true);
    }
}
