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

package ch.ethz.sis.afs.dto.operation;

import ch.ethz.sis.afs.dto.Lock;
import ch.ethz.sis.shared.io.IOUtils;
import ch.ethz.sis.afs.dto.LockType;
import lombok.Value;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Value
public class DeleteOperation implements Operation {

    private UUID owner;
    private List<Lock<UUID, String>> locks;
    private String source;
    private OperationName name;

    public DeleteOperation(UUID owner, String source) throws IOException {
        this.owner = owner;

        LockType sourceLockType = null;
        if (IOUtils.getFile(source).getDirectory()) {
            sourceLockType = LockType.HierarchicallyExclusive;
        } else {
            sourceLockType = LockType.Exclusive;
        }

        this.locks = List.of(new Lock<>(owner, source, sourceLockType));
        this.source = source;
        this.name = OperationName.Delete;
    }

}
