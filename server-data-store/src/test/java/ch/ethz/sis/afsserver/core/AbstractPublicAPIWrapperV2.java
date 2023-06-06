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
package ch.ethz.sis.afsserver.core;

import ch.ethz.sis.afsapi.api.PublicAPI;
import ch.ethz.sis.afsapi.dto.File;
import lombok.NonNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class AbstractPublicAPIWrapperV2 implements PublicAPI
{

    public abstract <E> E process(Class<E> responseType, String method, Map<String, Object> params);

    @Override
    public List<File> list(@NonNull String owner, @NonNull String source,
            @NonNull Boolean recursively) throws Exception
    {
        Map<String, Object> args = Map.of(
                "owner", owner,
                "source", source,
                "recursively", recursively);
        return process(List.class, "list", args);
    }

    @Override
    public byte[] read(@NonNull String owner, @NonNull String source, @NonNull Long offset,
            @NonNull Integer limit) throws Exception
    {
        Map<String, Object> args = Map.of(
                "owner", owner,
                "source", source,
                "offset", offset,
                "limit", limit);
        return process(byte[].class,"read", args);
    }

    @Override
    public Boolean write(@NonNull String owner, @NonNull String source, @NonNull Long offset,
            @NonNull byte[] data, @NonNull byte[] md5Hash) throws Exception
    {
        Map<String, Object> args = Map.of(
                "owner", owner,
                "source", source,
                "offset", offset,
                "data", data,
                "md5Hash", md5Hash);
        return process(Boolean.class, "write", args);
    }

    @Override
    public Boolean delete(@NonNull String owner, @NonNull String source) throws Exception
    {
        Map<String, Object> args = Map.of(
                "owner", owner,
                "source", source);
        return process(Boolean.class, "delete", args);
    }

    @Override
    public Boolean copy(@NonNull String sourceOwner, @NonNull String source,
            @NonNull String targetOwner, @NonNull String target) throws Exception
    {
        Map<String, Object> args = Map.of(
                "sourceOwner", sourceOwner,
                "source", source,
                "targetOwner", targetOwner,
                "target", target);
        return process(Boolean.class,"copy", args);
    }

    @Override
    public Boolean move(@NonNull String sourceOwner, @NonNull String source,
            @NonNull String targetOwner, @NonNull String target) throws Exception
    {
        Map<String, Object> args = Map.of(
                "sourceOwner", sourceOwner,
                "source", source,
                "targetOwner", targetOwner,
                "target", target);
        return process(Boolean.class, "move", args);
    }

    @Override
    public void begin(UUID transactionId) throws Exception
    {
        //TODO: Unused
    }

    @Override
    public Boolean prepare() throws Exception
    {
        //TODO: Unused
        return true;
    }

    @Override
    public void commit() throws Exception
    {
        //TODO: Unused
    }

    @Override
    public void rollback() throws Exception
    {
        //TODO: Unused
    }

    @Override
    public List<UUID> recover() throws Exception
    {
        //TODO: Unused
        return null;
    }

    @Override
    public String login(String userId, String password) throws Exception
    {
        //TODO: Unused
        return null;
    }

    @Override
    public Boolean isSessionValid() throws Exception
    {
        //TODO: Unused
        return null;
    }

    @Override
    public Boolean logout() throws Exception
    {
        //TODO: Unused
        return null;
    }
}
