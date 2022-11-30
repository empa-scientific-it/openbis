package ch.ethz.sis.afsserver.api;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;
import java.util.UUID;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class File {
    private String shareName;
    private UUID owner;
    private String path;
    private String name;
    private Boolean directory;
    private Long size; // Size in bytes
    private OffsetDateTime lastModifiedTime;
    private OffsetDateTime creationTime;
    private OffsetDateTime lastAccessTime;
}