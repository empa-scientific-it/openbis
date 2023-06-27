package ch.ethz.sis.afsapi.api;

import lombok.NonNull;

import java.nio.file.Path;

public interface ClientAPI
{
    void resumeRead(@NonNull String owner, @NonNull String source, @NonNull Path destination, @NonNull Long offset) throws Exception;

    @NonNull
    Boolean resumeWrite(@NonNull String owner, @NonNull String destination, @NonNull Path source, @NonNull Long offset) throws Exception;
}
