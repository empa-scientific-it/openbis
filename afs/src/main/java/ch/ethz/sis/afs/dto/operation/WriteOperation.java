package ch.ethz.sis.afs.dto.operation;

import ch.ethz.sis.afs.dto.Lock;
import ch.ethz.sis.afs.dto.LockType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.UUID;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class WriteOperation implements Operation {

    private UUID owner;
    private List<Lock<UUID, String>> locks;
    private String source;
    private long offset;
    private byte[] data;
    private byte[] md5Hash;
    private OperationName name;

    public WriteOperation(UUID owner, String source, long offset, byte[] data, byte[] md5Hash) {
        this.owner = owner;
        this.locks = List.of(new Lock<>(owner, source, LockType.Exclusive));
        this.source = source;
        this.offset = offset;
        this.data = data;
        this.md5Hash = md5Hash;
        this.name = OperationName.Write;
    }
}
