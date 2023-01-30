package ch.ethz.sis.afsserver.worker.proxy;

import ch.ethz.sis.afs.api.dto.File;
import ch.ethz.sis.afsserver.exception.FSExceptions;
import ch.ethz.sis.afsserver.worker.AbstractProxy;

import java.util.List;

public class ValidationProxy extends AbstractProxy {

    private int maxReadSizeInBytes;

    public ValidationProxy(AbstractProxy nextProxy, int maxReadSizeInBytes) {
        super(nextProxy);
        this.maxReadSizeInBytes = maxReadSizeInBytes;
    }


    @Override
    public List<File> list(String owner, String source, Boolean recursively) throws Exception {
        return nextProxy.list(owner, source, recursively);
    }

    @Override
    public byte[] read(String owner, String source, Long offset, Integer limit) throws Exception {
        validateReadSize(source, limit);
        return nextProxy.read(owner, source, offset, limit);
    }

    @Override
    public Boolean write(String owner, String source, Long offset, byte[] data, byte[] md5Hash) throws Exception {
        return nextProxy.write(owner, source, offset, data, md5Hash);
    }

    @Override
    public Boolean delete(String owner, String source) throws Exception {
        return nextProxy.delete(owner, source);
    }

    @Override
    public Boolean copy(String sourceOwner, String source, String targetOwner, String target) throws Exception {
        return nextProxy.copy(sourceOwner, source, targetOwner, target);
    }

    @Override
    public Boolean move(String sourceOwner, String source, String targetOwner, String target) throws Exception {
        return nextProxy.move(sourceOwner, source, targetOwner, target);
    }

    private void validateReadSize(String source, Integer limit) {
        if (limit > maxReadSizeInBytes) {
            throw FSExceptions.MAX_READ_SIZE_EXCEEDED.getInstance(workerContext.getSessionToken(), limit, source, maxReadSizeInBytes);
        }
    }
}
