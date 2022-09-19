package ch.ethz.sis.afs.api;

import ch.ethz.sis.shared.io.File;
import lombok.NonNull;

import java.util.List;

public interface OperationsAPI {

    @NonNull
    List<File> list(@NonNull String source, boolean recursively) throws Exception;

    @NonNull
    byte[] read(@NonNull String source, @NonNull long offset, @NonNull int limit) throws Exception;

    @NonNull
    boolean write(@NonNull String source, @NonNull long offset, @NonNull byte[] data, @NonNull byte[] md5Hash) throws Exception;

    @NonNull
    boolean delete(@NonNull String source) throws Exception;

    @NonNull
    boolean copy(@NonNull String source, @NonNull String target) throws Exception;

    @NonNull
    boolean move(@NonNull String source, @NonNull String target) throws Exception;

}
