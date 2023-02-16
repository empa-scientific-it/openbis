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
package ch.ethz.sis.afs.dto.operation;

import ch.ethz.sis.shared.io.IOUtils;
import ch.ethz.sis.afs.dto.Lock;
import ch.ethz.sis.afs.dto.LockType;
import lombok.Value;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Value
public class CopyOperation implements Operation {

    private UUID owner;
    private List<Lock<UUID, String>> locks;
    private String source;
    private String target;
    private OperationName name;

    public CopyOperation(UUID owner, String source, String target) throws IOException {
        this.owner = owner;

        LockType sourceLockType = null;
        LockType targetLockType = null;
        if (IOUtils.getFile(source).getDirectory()) {
            sourceLockType = LockType.HierarchicallyExclusive;
            targetLockType = LockType.HierarchicallyExclusive;
        } else {
            sourceLockType = LockType.Shared;
            targetLockType = LockType.Exclusive;
        }

        this.locks = List.of(new Lock<>(owner, source, sourceLockType), new Lock<>(owner, target, targetLockType));
        this.source = source;
        this.target = target;
        this.name = OperationName.Copy;
    }
}
