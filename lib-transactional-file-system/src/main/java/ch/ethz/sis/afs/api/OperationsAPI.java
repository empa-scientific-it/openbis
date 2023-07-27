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
package ch.ethz.sis.afs.api;

import java.util.List;

import ch.ethz.sis.afs.api.dto.File;
import lombok.NonNull;

public interface OperationsAPI {

    @NonNull
    List<File> list(@NonNull String source, boolean recursively) throws Exception;

    @NonNull
    byte[] read(@NonNull String source, @NonNull long offset, @NonNull int limit) throws Exception;

    boolean write(@NonNull String source, @NonNull long offset, @NonNull byte[] data, @NonNull byte[] md5Hash) throws Exception;

    boolean delete(@NonNull String source) throws Exception;

    boolean copy(@NonNull String source, @NonNull String target) throws Exception;

    boolean move(@NonNull String source, @NonNull String target) throws Exception;

    boolean create(@NonNull String source, boolean directory) throws Exception;

}
