/*
 * Copyright ETH 2022 - 2023 Zürich, Scientific IT Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.ethz.sis.shared.io;

import ch.ethz.sis.afs.api.dto.File;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipal;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;

public class IOUtils {

    //
    // Reusable maps with permissions
    //

    public static final Set<FilePermission> readPermissions = Set.of(FilePermission.Read);
    public static final Set<FilePermission> writePermissions = Set.of(FilePermission.Write);
    public static final Set<FilePermission> readWritePermissions = Set.of(FilePermission.Read, FilePermission.Write);

    public static final Set<FilePermission> noPermissions = Set.of();

    private static final FileAttribute<Set<PosixFilePermission>> defaultPosixPermissions = PosixFilePermissions.asFileAttribute(Set.of(
            PosixFilePermission.OWNER_READ,
            PosixFilePermission.OWNER_WRITE,
            PosixFilePermission.OWNER_EXECUTE));

    //
    // Initial File Attributes for file systems that support it
    //

    private static final FileAttribute<List<AclEntry>> defaultAclPermissions;

    static {
        List<AclEntryPermission> aclEntryPermissions = List.of(
                AclEntryPermission.READ_DATA,
                AclEntryPermission.READ_ATTRIBUTES,
                AclEntryPermission.READ_NAMED_ATTRS,
                AclEntryPermission.READ_ACL,
                AclEntryPermission.SYNCHRONIZE,
                AclEntryPermission.EXECUTE,
                AclEntryPermission.WRITE_DATA,
                AclEntryPermission.APPEND_DATA,
                AclEntryPermission.WRITE_ATTRIBUTES,
                AclEntryPermission.WRITE_NAMED_ATTRS,
                AclEntryPermission.WRITE_ACL,
                AclEntryPermission.WRITE_OWNER,
                AclEntryPermission.DELETE,
                AclEntryPermission.DELETE_CHILD);

        AclEntry aclEntry = AclEntry.newBuilder()
                .setType(AclEntryType.ALLOW)
                .setPrincipal(getUserPrincipal())
                .setPermissions(aclEntryPermissions.toArray(new AclEntryPermission[0]))
                .build();

        List<AclEntry> aclFilePermissions = List.of(aclEntry);

        defaultAclPermissions = new FileAttribute<List<AclEntry>>() {
            @Override
            public String name() {
                return "acl:acl";
            }

            @Override
            public List<AclEntry> value() {
                return aclFilePermissions;
            }
        };
    }

    //
    // String -> Java NIO2 Path
    //

    public static String getParentPath(String source) {
        Path sourcePath = getPathObject(source);
        Path sourcePathParent = sourcePath.getParent();
        if (sourcePathParent != null) {
            return sourcePathParent.toString().replace(WINDOWS_PATH_SEPARATOR, PATH_SEPARATOR);
        } else {
            return null;
        }
    }

    public static String getPath(String source, String... more) {
        StringBuilder buffer = new StringBuilder(source);
        for (String morePart : more) {
            buffer.append('/').append(morePart);
        }
        return buffer.toString();
    }

    private static Path getPathObject(String source, String... more) {
        return Paths.get(source, more);
    }

    //
    // File
    //

    public static final long NEW_FILE_SIZE = 0;
    private static final char WINDOWS_PATH_SEPARATOR = '\\';
    public static final char PATH_SEPARATOR = '/';
    public static final String PATH_SEPARATOR_AS_STRING = String.valueOf(IOUtils.PATH_SEPARATOR);
    public static final String RELATIVE_PATH_ROOT = "./";
    public static final String ABSOLUTE_PATH_ROOT = PATH_SEPARATOR_AS_STRING;

    public static File getFile(String path) throws IOException {
        return getFile(getPathObject(path));
    }

    private static File getFile(Path path) throws IOException {
        String absolutePath = path.toString().replace(WINDOWS_PATH_SEPARATOR, PATH_SEPARATOR);
        String name = path.getFileName().toString();

        Long size;
        boolean isDirectory = isDirectory(path);

        if (isDirectory) {
            size = null;
        } else {
            size = size(path);
        }

        BasicFileAttributes fileAttributes = Files.readAttributes(path, BasicFileAttributes.class);

        FileTime lastModifiedTime = fileAttributes.lastModifiedTime();
        OffsetDateTime lastModified = OffsetDateTime.ofInstant(lastModifiedTime.toInstant(), ZoneId.systemDefault());

        FileTime creationTime = fileAttributes.creationTime();
        OffsetDateTime creation = OffsetDateTime.ofInstant(creationTime.toInstant(), ZoneId.systemDefault());

        FileTime lastAccessTime = fileAttributes.lastAccessTime();
        OffsetDateTime lastAccess = OffsetDateTime.ofInstant(lastAccessTime.toInstant(), ZoneId.systemDefault());

        return new File(absolutePath, name, isDirectory, size, lastModified, creation, lastAccess);
    }

    //
    // List
    //

    public static List<File> list(String dir, boolean recursively) throws IOException {
        Path dirAsPath = getPathObject(dir);
        if (!isDirectory(dirAsPath)) {
            throw new IOException("Only directories can be listed, '" + dir + "' is not a directory.");
        }
        if (recursively) {
            return listRecursively(dirAsPath);
        } else {
            return list(dirAsPath);
        }
    }

    private static List<File> list(Path dir) throws IOException {
        List<File> files = new ArrayList<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(dir)) {
            for (Path path : directoryStream) {
                files.add(getFile(path));
            }
        }
        return files;
    }

    private static List<File> listRecursively(Path sourceDir) throws IOException {
        List<File> contents = new ArrayList<>();
        Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                contents.add(getFile(file));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (!sourceDir.equals(dir)) {
                    contents.add(getFile(dir));
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return contents;
    }

    //
    // Create
    //

    private static FileAttribute<?> getDefaultPermissions() {
        if (isPosix()) {
            return defaultPosixPermissions;
        } else if (isAcl()) {
            return defaultAclPermissions;
        } else {
            return null;
        }
    }

    public static void createDirectory(String source) throws IOException {
        FileAttribute<?> defaultPermissions = getDefaultPermissions();
        if (defaultPermissions != null) {
            Files.createDirectory(getPathObject(source), defaultPermissions);
        } else {
            Files.createDirectory(getPathObject(source));
        }

    }

    public static void createDirectories(String source) throws IOException {
        FileAttribute<?> defaultPermissions = getDefaultPermissions();
        if (defaultPermissions != null) {
            Files.createDirectories(getPathObject(source), defaultPermissions);
        } else {
            Files.createDirectories(getPathObject(source));
        }
    }

    public static void createFile(String source) throws IOException {
        createFile(getPathObject(source));
    }

    private static void createFile(Path source) throws IOException {
        FileAttribute<?> defaultPermissions = getDefaultPermissions();
        if (defaultPermissions != null) {
            Files.createFile(source, defaultPermissions);
        } else {
            Files.createFile(source);
        }
    }

    //
    // Write
    //

    public static void write(String source, long offset, byte[] data) throws IOException {
        Path sourceAsPath = getPathObject(source);
        boolean canModify = hasPermissions(sourceAsPath, writePermissions);
        if (!canModify) {
            throw new IOException("Can't be written: '" + source + "'.");
        }
        ByteBuffer dataBuffer = ByteBuffer.wrap(data);
        try (FileChannel fileChannel = (FileChannel.open(sourceAsPath, StandardOpenOption.WRITE))) {
            fileChannel.position(offset);
            fileChannel.write(dataBuffer);
        }
    }

    //
    // Read
    //

    public static byte[] readFully(String source) throws IOException {
        return Files.readAllBytes(getPathObject(source));
    }

    public static byte[] read(String source, long offset, int length) throws IOException {
        ByteBuffer dataBuffer = ByteBuffer.allocate(length);
        try (FileChannel fileChannel = (FileChannel.open(getPathObject(source), StandardOpenOption.READ))) {
            fileChannel.position(offset);
            int read = fileChannel.read(dataBuffer);
            if (read < length) {
                throw new IOException("Expected to read " + length + " bytes but was only " + read + ".");
            }
        }
        return dataBuffer.array();
    }

    //
    // Delete
    //

    public static void delete(String source) throws IOException {
        Path sourceAsPath = getPathObject(source);
        boolean canModify = hasPermissions(sourceAsPath, writePermissions);
        if (!canModify) {
            throw new IOException("Can't be modified: '" + source + "'.");
        }
        Files.walkFileTree(sourceAsPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    //
    // Move / Copy
    //

    public static void move(String source, String target) throws IOException {
        try {
            Files.move(getPathObject(source), getPathObject(target), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException ex) {
            Files.move(getPathObject(source), getPathObject(target), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static void copy(String source, String target) throws IOException {
        Path sourceP = getPathObject(source);
        Path targetP = getPathObject(target);

        Files.walkFileTree(sourceP, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path targetPath = targetP.resolve(sourceP.relativize(dir));
                if (!Files.exists(targetPath)) {
                    Files.createDirectory(targetPath);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.copy(file, targetP.resolve(sourceP.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static void copyFile(String source, long offset, int length, String target, long targetOffset) throws IOException {
        byte[] toCopy = read(source, offset, length);
        createFile(target);
        write(target, targetOffset, toCopy);
    }

    //
    // File Permissions
    //

    private static final String POSIX = "posix";
    private static final boolean isPosix = FileSystems.getDefault().supportedFileAttributeViews().contains(POSIX);
    private static final String ACL = "acl";
    private static final boolean isAcl = FileSystems.getDefault().supportedFileAttributeViews().contains(ACL);
    private static final String DOS = "dos";
    private static final boolean isDos = FileSystems.getDefault().supportedFileAttributeViews().contains(DOS);

    public static boolean isFileSystemSupported() {
        return isPosix() || isAcl();
    }

    private static boolean isPosix() {
        return isPosix;
    }

    private static boolean isAcl() {
        return isAcl;
    }

    private static boolean isDos() {
        return isDos;
    }

    public static Set<FilePermission> getFilePermissions(String source) throws IOException {
        return getFilePermissions(getPathObject(source));
    }

    private static Set<FilePermission> getFilePermissions(Path source) throws IOException {
        if (isPosix()) {
            return getPosixFilePermissions(source);
        } else if (isAcl()) {
            return getAclFilePermissions(source);
        } else if (isDos()) {
            return getDosFilePermissions(source);
        } else {
            throw new IOException("Can't get file permissions: '" + source.toString() + "'.");
        }
    }

    public static void setFilePermissions(String source, Set<FilePermission> filePermissions) throws IOException {
        setFilePermissions(getPathObject(source), filePermissions);
    }

    private static void setFilePermissions(Path source, Set<FilePermission> filePermissions) throws IOException {
        if (isPosix()) {
            setPosixFilePermissions(source, filePermissions);
        } else if (isAcl()) {
            setAclFilePermissions(source, filePermissions);
        } else if (isDos()) {
            setDosFilePermissions(source, filePermissions);
        } else {
            throw new IOException("Can't set file permissions: '" + source.toString() + "'.");
        }
    }

    private static String getCurrentUser() {
        return System.getProperty("user.name"); //platform independent
    }

    private static UserPrincipal currentUserPrincipal = null;

    private static UserPrincipal getUserPrincipal() {
        if (currentUserPrincipal == null) {
            try {
                currentUserPrincipal = getPathObject("/").getFileSystem().getUserPrincipalLookupService().lookupPrincipalByName(getCurrentUser());
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        return currentUserPrincipal;
    }

    private static Set<FilePermission> getAclFilePermissions(Path source) throws IOException {
        Set<FilePermission> filePermissions = new HashSet<>();
        UserPrincipal currentUser = getUserPrincipal();

        AclFileAttributeView view = Files.getFileAttributeView(source, AclFileAttributeView.class);
        List<AclEntry> acls = view.getAcl();
        for (AclEntry acl : acls) {
            if (acl.principal().equals(currentUser)) {
                if (acl.type() == AclEntryType.ALLOW && acl.permissions().contains(AclEntryPermission.READ_DATA)) {
                    filePermissions.add(FilePermission.Read);
                }

                if (acl.type() == AclEntryType.ALLOW && acl.permissions().contains(AclEntryPermission.WRITE_DATA)) {
                    filePermissions.add(FilePermission.Write);
                }
            }
        }
        return filePermissions;
    }

    private static void setAclFilePermissions(Path source, Set<FilePermission> filePermissions) throws IOException {
        UserPrincipal userPrincipal = getUserPrincipal();
        List<AclEntryPermission> aclEntryPermissions = new ArrayList<>();
        if (filePermissions.contains(FilePermission.Read)) {
            aclEntryPermissions.add(AclEntryPermission.READ_DATA);
            aclEntryPermissions.add(AclEntryPermission.READ_ATTRIBUTES);
            aclEntryPermissions.add(AclEntryPermission.READ_NAMED_ATTRS);
            aclEntryPermissions.add(AclEntryPermission.READ_ACL);
            aclEntryPermissions.add(AclEntryPermission.SYNCHRONIZE);
            aclEntryPermissions.add(AclEntryPermission.EXECUTE);
        }

        if (filePermissions.contains(FilePermission.Write)) {
            aclEntryPermissions.add(AclEntryPermission.WRITE_DATA);
            aclEntryPermissions.add(AclEntryPermission.APPEND_DATA);
            aclEntryPermissions.add(AclEntryPermission.WRITE_ATTRIBUTES);
            aclEntryPermissions.add(AclEntryPermission.WRITE_NAMED_ATTRS);
            aclEntryPermissions.add(AclEntryPermission.WRITE_ACL);
            aclEntryPermissions.add(AclEntryPermission.WRITE_OWNER);
            aclEntryPermissions.add(AclEntryPermission.DELETE);
            aclEntryPermissions.add(AclEntryPermission.DELETE_CHILD);
        }

        AclEntry aclEntry = AclEntry.newBuilder()
                .setType(AclEntryType.ALLOW)
                .setPrincipal(userPrincipal)
                .setPermissions(aclEntryPermissions.toArray(new AclEntryPermission[0]))
                .build();
        List<AclEntry> aclFilePermissions = new ArrayList<>();

        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
            private void setAcl(Path source) throws IOException {
                AclFileAttributeView view = Files.getFileAttributeView(source, AclFileAttributeView.class);
                view.setAcl(aclFilePermissions);
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                setAcl(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                setAcl(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static Set<FilePermission> getDosFilePermissions(Path source) throws IOException {
        DosFileAttributes dosFilePermissions = Files.readAttributes(source, DosFileAttributes.class);
        Set<FilePermission> filePermissions;
        if (dosFilePermissions.isReadOnly()) {
            filePermissions = readPermissions;
        } else {
            filePermissions = readWritePermissions;
        }
        return filePermissions;
    }

    private static void setDosFilePermissions(Path source, Set<FilePermission> filePermissions) throws IOException {
        boolean isReadOnly = !filePermissions.contains(FilePermission.Write);

        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
            private void setReadOnly(Path path) throws IOException {
                DosFileAttributeView dosFileAttributeView = Files.getFileAttributeView(path, DosFileAttributeView.class);
                dosFileAttributeView.setReadOnly(isReadOnly);
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                setReadOnly(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                setReadOnly(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static Set<FilePermission> getPosixFilePermissions(Path source) throws IOException {
        Set<PosixFilePermission> posixFilePermissions = Files.getPosixFilePermissions(source);
        Set<FilePermission> filePermissions;
        if (posixFilePermissions.contains(PosixFilePermission.OWNER_READ) && posixFilePermissions.contains(PosixFilePermission.OWNER_WRITE)) {
            filePermissions = readWritePermissions;
        } else if (posixFilePermissions.contains(PosixFilePermission.OWNER_READ)) {
            filePermissions = readPermissions;
        } else if (posixFilePermissions.contains(PosixFilePermission.OWNER_WRITE)) {
            filePermissions = writePermissions;
        } else {
            filePermissions = noPermissions;
        }
        return filePermissions;
    }

    private static void setPosixFilePermissions(Path source, Set<FilePermission> filePermissions) throws IOException {
        Set<PosixFilePermission> posixFilePermissions = new HashSet<>();
        if (filePermissions.contains(FilePermission.Read)) {
            posixFilePermissions.add(PosixFilePermission.OWNER_READ);
            posixFilePermissions.add(PosixFilePermission.OWNER_EXECUTE);
        }
        if (filePermissions.contains(FilePermission.Write)) {
            posixFilePermissions.add(PosixFilePermission.OWNER_WRITE);
        }

        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.setPosixFilePermissions(file, posixFilePermissions);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.setPosixFilePermissions(dir, posixFilePermissions);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    //
    // Helpers
    //

    public static boolean isSameVolume(String pathA, String pathB) throws IOException {
        Path pathAo = getPathObject(pathA);
        FileStore pathAStore = Files.getFileStore(pathAo);
        Path pathBo = getPathObject(pathB);
        FileStore pathBStore = Files.getFileStore(pathBo);
        return pathAStore.equals(pathBStore);
    }

    private static final String MD5 = "MD5";

    public static byte[] getMD5(byte[] data) {
        try {
            return MessageDigest.getInstance(MD5).digest(data);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    public static String encodeBase64(byte[] input) {
        return Base64.getEncoder().encodeToString(input);
    }

    public static byte[] decodeBase64(String input) {
        return Base64.getDecoder().decode(input);
    }

    public static boolean exists(String source) {
        Path sourcePath = getPathObject(source);
        return Files.exists(sourcePath);
    }

    private static boolean isDirectory(Path source) {
        return Files.isDirectory(source);
    }

    private static long size(Path source) throws IOException {
        return Files.size(source);
    }

    public static boolean hasReadWritePermissions(String source) throws IOException {
        return hasPermissions(getPathObject(source), readWritePermissions);
    }

    public static boolean hasReadPermissions(String source) throws IOException {
        return hasPermissions(getPathObject(source), readPermissions);
    }

    public static boolean hasWritePermissions(String source) throws IOException {
        return hasPermissions(getPathObject(source), writePermissions);
    }

    private static boolean hasPermissions(String source, Set<FilePermission> permissions) throws IOException {
        return hasPermissions(getPathObject(source), permissions);
    }

    private static boolean hasPermissions(Path source, Set<FilePermission> permissions) throws IOException {
        boolean[] havePermissionsResult = {true};
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {

            private FileVisitResult havePermissions(Path path) throws IOException {
                boolean havePermissions = getFilePermissions(path).containsAll(permissions);
                if (havePermissions) {
                    return FileVisitResult.CONTINUE;
                } else {
                    havePermissionsResult[0] = false;
                    return FileVisitResult.TERMINATE;
                }
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                return havePermissions(file);
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                return havePermissions(dir);
            }
        });
        return havePermissionsResult[0];
    }
}
