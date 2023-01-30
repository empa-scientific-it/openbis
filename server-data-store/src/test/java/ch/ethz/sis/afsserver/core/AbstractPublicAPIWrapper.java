package ch.ethz.sis.afsserver.core;

import ch.ethz.sis.afs.api.dto.File;
import ch.ethz.sis.afsserver.api.PublicAPI;
import lombok.NonNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class AbstractPublicAPIWrapper implements PublicAPI {

    public abstract <E> E process(String method, Map<String, Object> methodParameters);

    @Override
    public List<File> list(@NonNull String owner, @NonNull String source, @NonNull Boolean recursively) throws Exception {
        Map<String, Object> args = Map.of(
                "owner", owner,
                "source", source,
                "recursively", recursively);
        return process("list", args);
    }

    @Override
    public byte[] read(@NonNull String owner, @NonNull String source, @NonNull Long offset, @NonNull Integer limit) throws Exception {
        Map<String, Object> args = Map.of(
                "owner", owner,
                "source", source,
                "offset", offset,
                "limit", limit);
        return process("read", args);
    }

    @Override
    public Boolean write(@NonNull String owner, @NonNull String source, @NonNull Long offset, @NonNull byte[] data, @NonNull byte[] md5Hash) throws Exception {
        Map<String, Object> args = Map.of(
                "owner", owner,
                "source", source,
                "offset", offset,
                "data", data,
                "md5Hash", md5Hash);
        return process("write", args);
    }

    @Override
    public Boolean delete(@NonNull String owner, @NonNull String source) throws Exception {
        Map<String, Object> args = Map.of(
                "owner", owner,
                "source", source);
        return process("delete", args);
    }

    @Override
    public Boolean copy(@NonNull String sourceOwner, @NonNull String source, @NonNull String targetOwner, @NonNull String target) throws Exception {
        Map<String, Object> args = Map.of(
                "sourceOwner", sourceOwner,
                "source", source,
                "targetOwner", targetOwner,
                "target", target);
        return process("copy", args);
    }

    @Override
    public Boolean move(@NonNull String sourceOwner, @NonNull String source, @NonNull String targetOwner, @NonNull String target) throws Exception {
        Map<String, Object> args = Map.of(
                "sourceOwner", sourceOwner,
                "source", source,
                "targetOwner", targetOwner,
                "target", target);
        return process("move", args);
    }

    @Override
    public void begin(UUID transactionId) throws Exception {
        //TODO: Unused
    }

    @Override
    public Boolean prepare() throws Exception {
        //TODO: Unused
        return true;
    }

    @Override
    public void commit() throws Exception {
        //TODO: Unused
    }

    @Override
    public void rollback() throws Exception {
        //TODO: Unused
    }

    @Override
    public List<UUID> recover() throws Exception {
        //TODO: Unused
        return null;
    }

    @Override
    public String login(String userId, String password) throws Exception {
        //TODO: Unused
        return null;
    }

    @Override
    public Boolean isSessionValid() throws Exception {
        //TODO: Unused
        return null;
    }

    @Override
    public Boolean logout() throws Exception {
        //TODO: Unused
        return null;
    }
}
