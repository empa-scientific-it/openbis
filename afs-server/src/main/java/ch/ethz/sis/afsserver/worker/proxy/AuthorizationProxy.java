package ch.ethz.sis.afsserver.worker.proxy;

import ch.ethz.sis.afs.api.dto.File;
import ch.ethz.sis.afs.dto.operation.OperationName;
import ch.ethz.sis.afsserver.exception.FSExceptions;
import ch.ethz.sis.afsserver.worker.AbstractProxy;
import ch.ethz.sis.afsserver.worker.providers.AuthorizationInfoProvider;
import ch.ethz.sis.shared.io.FilePermission;
import ch.ethz.sis.shared.io.IOUtils;

import java.util.List;
import java.util.Set;

public class AuthorizationProxy extends AbstractProxy {

    AuthorizationInfoProvider authorizationInfoProvider;

    public AuthorizationProxy(AbstractProxy nextProxy,
                              AuthorizationInfoProvider authorizationInfoProvider) {
        super(nextProxy);
        this.authorizationInfoProvider = authorizationInfoProvider;
    }

    private void validateUserRights(String owner, String source, Set<FilePermission> permissions, OperationName operationName) throws Exception {
        boolean doesSessionHaveRights = authorizationInfoProvider.doesSessionHaveRights(workerContext.getSessionToken(),
                owner,
                permissions);
        if (!doesSessionHaveRights) {
            throw FSExceptions.USER_NO_ACL_RIGHTS.getInstance(workerContext.getSessionToken(), permissions, owner, source, operationName);
        }
    }

    @Override
    public List<File> list(String owner, String source, Boolean recursively) throws Exception {
        validateUserRights(owner, source, IOUtils.readPermissions, OperationName.List);
        return nextProxy.list(owner, source, recursively);
    }

    @Override
    public byte[] read(String owner, String source, Long offset, Integer limit) throws Exception {
        validateUserRights(owner, source, IOUtils.readPermissions, OperationName.Read);
        return nextProxy.read(owner, source, offset, limit);
    }

    @Override
    public Boolean write(String owner, String source, Long offset, byte[] data, byte[] md5Hash) throws Exception {
        validateUserRights(owner, source, IOUtils.writePermissions, OperationName.Write);
        return nextProxy.write(owner, source, offset, data, md5Hash);
    }

    @Override
    public Boolean delete(String owner, String source) throws Exception {
        validateUserRights(owner, source, IOUtils.writePermissions, OperationName.Delete);
        return nextProxy.delete(owner, source);
    }

    @Override
    public Boolean copy(String sourceOwner, String source, String targetOwner, String target) throws Exception {
        validateUserRights(sourceOwner, source, IOUtils.readPermissions, OperationName.Copy);
        validateUserRights(targetOwner, target, IOUtils.writePermissions, OperationName.Copy);
        return nextProxy.copy(sourceOwner, source, targetOwner, target);
    }

    @Override
    public Boolean move(String sourceOwner, String source, String targetOwner, String target) throws Exception {
        validateUserRights(sourceOwner, source, IOUtils.readWritePermissions, OperationName.Move);
        validateUserRights(targetOwner, target, IOUtils.writePermissions, OperationName.Move);
        return nextProxy.move(sourceOwner, source, targetOwner, target);
    }
}
