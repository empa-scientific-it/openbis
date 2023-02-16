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
package ch.ethz.sis.afsserver.worker.providers;

import ch.ethz.sis.shared.io.FilePermission;
import ch.ethz.sis.shared.startup.Configuration;

import java.util.Set;

public interface AuthorizationInfoProvider {
    void init(Configuration initParameter) throws Exception;

    boolean doesSessionHaveRights(String sessionToken, String owner, Set<FilePermission> permissions);
}
