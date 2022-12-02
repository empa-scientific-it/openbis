package ch.ethz.sis.afsserver.worker.providers.dummy;

import ch.ethz.sis.afsserver.worker.providers.AuthorizationInfoProvider;
import ch.ethz.sis.shared.io.FilePermission;
import ch.ethz.sis.shared.startup.Configuration;

import java.util.Set;

public class DummyAuthorizationInfoProvider implements AuthorizationInfoProvider {
    @Override
    public void init(Configuration initParameter) throws Exception {
        // Do nothing
    }

    @Override
    public boolean doesSessionHaveRights(String sessionToken, String owner, Set<FilePermission> permissions) {
        return true;
    }
}
