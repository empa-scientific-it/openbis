package ch.ethz.sis.afs.dto.operation;

import ch.ethz.sis.shared.io.IOUtils;
import ch.ethz.sis.afs.dto.Lock;
import ch.ethz.sis.afs.dto.LockType;
import lombok.Value;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Value
public class CopyOperation implements Operation {

    private UUID owner;
    private List<Lock<UUID, String>> locks;
    private String source;
    private String target;
    private OperationName name;

    public CopyOperation(UUID owner, String source, String target) throws IOException {
        this.owner = owner;

        LockType sourceLockType = null;
        LockType targetLockType = null;
        if (IOUtils.getFile(source).getDirectory()) {
            sourceLockType = LockType.HierarchicallyExclusive;
            targetLockType = LockType.HierarchicallyExclusive;
        } else {
            sourceLockType = LockType.Shared;
            targetLockType = LockType.Exclusive;
        }

        this.locks = List.of(new Lock<>(owner, source, sourceLockType), new Lock<>(owner, target, targetLockType));
        this.source = source;
        this.target = target;
        this.name = OperationName.Copy;
    }
}
