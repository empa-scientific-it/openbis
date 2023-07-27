/*
 *  Copyright ETH 2022 - 2023 Zürich, Scientific IT Services
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

package ch.ethz.sis.afsapi.api;

import java.util.List;

import ch.ethz.sis.afsapi.dto.File;
import lombok.NonNull;

public interface OperationsAPI
{

    @NonNull
    List<File> list(@NonNull String owner, @NonNull String source, @NonNull Boolean recursively)
            throws Exception;

    @NonNull
    byte[] read(@NonNull String owner, @NonNull String source, @NonNull Long offset,
            @NonNull Integer limit) throws Exception;

    @NonNull
    Boolean write(@NonNull String owner, @NonNull String source, @NonNull Long offset,
            @NonNull byte[] data, @NonNull byte[] md5Hash) throws Exception;

    @NonNull
    Boolean delete(@NonNull String owner, @NonNull String source) throws Exception;

    @NonNull
    Boolean copy(@NonNull String sourceOwner, @NonNull String source, @NonNull String targetOwner,
            @NonNull String target) throws Exception;

    @NonNull
    Boolean move(@NonNull String sourceOwner, @NonNull String source, @NonNull String targetOwner,
            @NonNull String target) throws Exception;

    @NonNull
    Boolean create(@NonNull String owner, @NonNull String source, @NonNull Boolean directory) throws Exception;

}