package ch.ethz.sis.afsserver.worker.proxy;

import ch.ethz.sis.afs.api.dto.File;
import ch.ethz.sis.afsserver.worker.AbstractProxy;
import ch.ethz.sis.shared.io.IOUtils;

import java.util.List;
import java.util.UUID;

public class ExecutorProxy extends AbstractProxy {

    public ExecutorProxy() {
        super(null);
    }

    //
    // Transaction Management
    //

    @Override
    public void begin(UUID transactionId) throws Exception {
        workerContext.setTransactionId(transactionId);
        workerContext.getConnection().begin(transactionId);
    }

    @Override
    public Boolean prepare() throws Exception {
        return workerContext.getConnection().prepare();
    }

    @Override
    public void commit() throws Exception {
        workerContext.getConnection().commit();
    }

    @Override
    public void rollback() throws Exception {
        workerContext.getConnection().rollback();
    }

    @Override
    public List<UUID> recover() throws Exception {
        return workerContext.getConnection().recover();
    }

    //
    // File System Operations
    //

    public String getPath(String owner, String source) {
        return IOUtils.PATH_SEPARATOR + owner.toString() + source;
    }

    @Override
    public List<File> list(String owner, String source, Boolean recursively) throws Exception {
        return workerContext.getConnection().list(getPath(owner, source), recursively);
    }

    @Override
    public byte[] read(String owner, String source, Long offset, Integer limit) throws Exception {
        return workerContext.getConnection().read(getPath(owner, source), offset, limit);
    }

    @Override
    public Boolean write(String owner, String source, Long offset, byte[] data, byte[] md5Hash) throws Exception {
        return workerContext.getConnection().write(getPath(owner, source), offset, data, md5Hash);
    }

    @Override
    public Boolean delete(String owner, String source) throws Exception {
        return workerContext.getConnection().delete(getPath(owner, source));
    }

    @Override
    public Boolean copy(String sourceOwner, String source, String targetOwner, String target) throws Exception {
        return workerContext.getConnection().copy(getPath(sourceOwner, source), getPath(targetOwner, target));
    }

    @Override
    public Boolean move(String sourceOwner, String source, String targetOwner, String target) throws Exception {
        return workerContext.getConnection().move(getPath(sourceOwner, source), getPath(targetOwner, target));
    }

}
