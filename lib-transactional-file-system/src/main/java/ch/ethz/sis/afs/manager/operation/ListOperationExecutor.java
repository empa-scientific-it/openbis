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
import ch.ethz.sis.afs.dto.Transaction;
import ch.ethz.sis.afs.dto.operation.ListOperation;
import ch.ethz.sis.shared.io.IOUtils;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

public class ListOperationExecutor implements NonModifyingOperationExecutor<ListOperation> {
    //
    // Singleton
    //

    private static final ListOperationExecutor instance;

    static {
        instance = new ListOperationExecutor();
    }

    private ListOperationExecutor() {
    }

    public static ListOperationExecutor getInstance() {
        return instance;
    }

    @Override
    public List<File> executeOperation(@NonNull Transaction transaction, @NonNull ListOperation operation) throws Exception {
        List<File> files = IOUtils.list(operation.getSource(), operation.isRecursively());
        List<File> filesFromRoot = new ArrayList<>();
        for (File file : files) {
            filesFromRoot.add(file.toBuilder().path(OperationExecutor.getStoragePath(transaction, file.getPath())).build());
        }
        return filesFromRoot;
    }
}
