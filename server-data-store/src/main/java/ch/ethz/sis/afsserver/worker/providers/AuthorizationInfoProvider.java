package ch.ethz.sis.afsserver.worker.providers;

import ch.ethz.sis.shared.io.FilePermission;
import ch.ethz.sis.shared.startup.Configuration;

import java.util.Set;

public interface AuthorizationInfoProvider {
    void init(Configuration initParameter) throws Exception;

    boolean doesSessionHaveRights(String sessionToken, String owner, Set<FilePermission> permissions);
}
