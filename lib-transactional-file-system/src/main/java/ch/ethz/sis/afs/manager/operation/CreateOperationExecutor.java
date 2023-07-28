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

import static ch.ethz.sis.afs.exception.AFSExceptions.MD5NotMatch;
import static ch.ethz.sis.afs.exception.AFSExceptions.PathInStore;
import static ch.ethz.sis.afs.exception.AFSExceptions.PathIsDirectory;

import java.util.Arrays;

import ch.ethz.sis.afs.api.dto.File;
import ch.ethz.sis.afs.dto.Transaction;
import ch.ethz.sis.afs.dto.operation.CreateOperation;
import ch.ethz.sis.afs.dto.operation.OperationName;
import ch.ethz.sis.afs.dto.operation.WriteOperation;
import ch.ethz.sis.afs.exception.AFSExceptions;
import ch.ethz.sis.shared.io.IOUtils;
import lombok.NonNull;

public class CreateOperationExecutor implements OperationExecutor<CreateOperation> {

    //
    // Singleton
    //

    private static final CreateOperationExecutor INSTANCE;

    static
    {
        INSTANCE = new CreateOperationExecutor();
    }

    private CreateOperationExecutor()
    {
    }

    public static CreateOperationExecutor getInstance() {
        return INSTANCE;
    }

    //
    // Operation
    //

    @Override
    public boolean prepare(final @NonNull Transaction transaction, final CreateOperation operation) throws Exception {
        // Check that file/directory does not exist
        if (IOUtils.exists(operation.getSource()))
        {
            AFSExceptions.throwInstance(PathInStore, OperationName.Create.name(), operation.getSource());
        }
        return true;
    }

    @Override
    public boolean commit(final @NonNull Transaction transaction, final CreateOperation operation) throws Exception {
        final String directoriesToCreate = operation.isDirectory() ? operation.getSource() : IOUtils.getParentPath(operation.getSource());
        IOUtils.createDirectories(directoriesToCreate);
        if (!operation.isDirectory())
        {
            IOUtils.createFile(operation.getSource());
        }
        return true;
    }

}
