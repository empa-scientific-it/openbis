/*
 * Copyright ETH 2023 Zürich, Scientific IT Services
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
 */

package ch.ethz.sis.afs.dto.operation;

import java.util.List;
import java.util.UUID;

import ch.ethz.sis.afs.dto.Lock;
import ch.ethz.sis.afs.dto.LockType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class CreateOperation implements Operation
{
    UUID owner;

    List<Lock<UUID, String>> locks;

    OperationName name;

    String source;

    boolean directory;

    public CreateOperation(final UUID owner, final String source, final boolean directory)
    {
        this.owner = owner;
        this.locks = List.of(new Lock<>(owner, source, LockType.Exclusive));
        this.name = OperationName.Create;
        this.source = source;
        this.directory = directory;
    }

}
