package ch.ethz.sis.afs.dto.operation;

import ch.ethz.sis.afs.dto.Lock;
import ch.ethz.sis.shared.io.IOUtils;
import ch.ethz.sis.afs.dto.LockType;
import lombok.Value;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Value
public class DeleteOperation implements Operation {

    private UUID owner;
    private List<Lock<UUID, String>> locks;
    private String source;
    private OperationName name;

    public DeleteOperation(UUID owner, String source) throws IOException {
        this.owner = owner;

        LockType sourceLockType = null;
        if (IOUtils.getFile(source).getDirectory()) {
            sourceLockType = LockType.HierarchicallyExclusive;
        } else {
            sourceLockType = LockType.Exclusive;
        }

        this.locks = List.of(new Lock<>(owner, source, sourceLockType));
        this.source = source;
        this.name = OperationName.Delete;
    }

}
