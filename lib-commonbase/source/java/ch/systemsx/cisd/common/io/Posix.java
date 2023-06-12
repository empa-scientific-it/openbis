/*
 * Copyright 2007 - 2018 ETH Zuerich, CISD and SIS.
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

package ch.systemsx.cisd.common.io;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.unix.FileLinkType;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class Posix
{
    // Make constructor private to make clear is a utility class
    private Posix() {

    }

    public static boolean isOperational() {
        return File.separatorChar == '/'; //On Posix systems the value of this field is '/'
    }

    //
    // User related methods
    //

    private static Integer gid = null;

    public static int getGid()
    {
        if (gid == null)
        {
            try
            {
                Process process = Runtime.getRuntime().exec("id -g -r");
                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(process.getInputStream()));
                String output = reader.readLine();
                reader.close();
                gid = Integer.parseInt(output);
            } catch (IOException e)
            {
                throw new IOExceptionUnchecked(e);
            }
        }
        return gid;
    }

    private static Integer uid = null;

    public static int getUid() throws IOExceptionUnchecked
    {
        if (uid == null)
        {
            try
            {
                Process process = Runtime.getRuntime().exec("id -u -r");
                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(process.getInputStream()));
                String output = reader.readLine();
                reader.close();
                uid = Integer.parseInt(output);
            } catch (IOException e)
            {
                throw new IOExceptionUnchecked(e);
            }
        }
        return uid;
    }

    private static Map<String, Integer> uidByUserName = new HashMap<>();

    /**
     * Returns the uid of the <var>userName</var>, or <code>-1</code>, if no user with this name exists.
     */
    public static final int getUidForUserName(String userName)
    {
        if (userName == null)
        {
            throw new NullPointerException("userName");
        }

        if (uidByUserName.get(userName) == null)
        {
            try
            {
                Process process = Runtime.getRuntime().exec("id -u " + userName);
                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(process.getInputStream()));
                String output = reader.readLine();
                reader.close();
                int uid = Integer.parseInt(output);
                uidByUserName.put(userName, uid);
            } catch (IOException e)
            {
                throw new IOExceptionUnchecked(e);
            }
        }

        return uidByUserName.get(userName);
    }

    private static Map<String, Integer> gidByGroupName = new HashMap<>();

    /**
     * Returns the gid of the <var>groupName</var>, or <code>-1</code>, if no group with this name exists.
     */
    public static final int getGidForGroupName(String groupName)
    {
        if (groupName == null)
        {
            throw new NullPointerException("groupName");
        }

        if (gidByGroupName.get(groupName) == null)
        {
            try
            {
                Process process = Runtime.getRuntime().exec("id -g " + groupName);
                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(process.getInputStream()));
                String output = reader.readLine();
                reader.close();
                int uid = Integer.parseInt(output);
                gidByGroupName.put(groupName, uid);
            } catch (IOException e)
            {
                throw new IOExceptionUnchecked(e);
            }
        }

        return gidByGroupName.get(groupName);
    }

    private static Integer euid = null;

    public static int getEuid()
    {
        if (euid == null)
        {
            try
            {
                Process process = Runtime.getRuntime().exec("id -u");
                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(process.getInputStream()));
                String output = reader.readLine();
                reader.close();
                euid = Integer.parseInt(output);
            } catch (IOException e)
            {
                throw new IOExceptionUnchecked(e);
            }
        }
        return euid;
    }

    /**
     * Returns the effective gid that determines the permissions of this process.
     */
    public static final int getEgid()
    {
        return getGid();
    }

    /**
     * Sets the owner of <var>fileName</var> to the specified <var>uid</var> and <var>gid</var> values.
     * Dereferences a symbolic link.
     */
    public static void setOwner(String path, int userId, int groupId)
    {
        try
        {
            Files.setAttribute(Path.of(path), "unix:uid", userId);
            Files.setAttribute(Path.of(path), "unix:gid", groupId);
        } catch (IOException e)
        {
            throw new IOExceptionUnchecked(e);
        }
    }

    public static int getUid(String path, boolean followLinks)
    {
        try
        {
            if (followLinks)
            {
                return (int) Files.getAttribute(Path.of(path), "unix:uid");
            } else {
                return (int) Files.getAttribute(Path.of(path), "unix:uid", LinkOption.NOFOLLOW_LINKS);
            }
        } catch (IOException e)
        {
            throw new IOExceptionUnchecked(e);
        }
    }

    public static int getUid(String path)
    {
        return getUid(path, true);
    }

    public static int getGid(String path, boolean followLinks)
    {
        try
        {
            if (followLinks)
            {
                return (int) Files.getAttribute(Path.of(path), "unix:gid");
            } else {
                return (int) Files.getAttribute(Path.of(path), "unix:gid", LinkOption.NOFOLLOW_LINKS);
            }
        } catch (IOException e)
        {
            throw new IOExceptionUnchecked(e);
        }
    }

    public static int getGid(String path)
    {
        return getGid(path, true);
    }

    private static Map<Integer, String> userNameByUid = new HashMap<>();

    public static String tryGetUserNameForUid(int uid)
    {
        if (userNameByUid.get(uid) == null)
        {
            try
            {
                Process process = Runtime.getRuntime().exec("id -un " + uid);
                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(process.getInputStream()));
                String output = reader.readLine();
                reader.close();
                userNameByUid.put(uid, output);
                uidByUserName.put(output, uid);
            } catch (IOException e)
            {
                throw new IOExceptionUnchecked(e);
            }
        }
        return userNameByUid.get(uid);
    }

    private static Map<Integer, String> groupNameByGid = new HashMap<>();

    public static String tryGetGroupNameForGid(int gid)
    {
        if (groupNameByGid.get(gid) == null)
        {
            try
            {
                Process process = Runtime.getRuntime().exec("id -gn " + gid);
                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(process.getInputStream()));
                String output = reader.readLine();
                reader.close();
                groupNameByGid.put(gid, output);
                gidByGroupName.put(output, gid);
            } catch (IOException e)
            {
                throw new IOExceptionUnchecked(e);
            }
        }
        return groupNameByGid.get(gid);
    }

    public static Time getSystemTime()
    {
        return Time.getInstance();
    }

    public static String tryReadSymbolicLink(String absolutePath)
    {
        Stat stat = tryGetLinkInfo(absolutePath);
        return stat.isSymbolicLink() ? stat.tryGetSymbolicLink() : null;
    }

//    /**
//     * Change link timestamps of a file, directory or link. Does not dereference a symbolic link.
//     *
//     * @param fileName The name of the file or link to change the timestamp of.
//     * @param accessTimeSecs The new access time in seconds since start of the epoch.
//     * @param accessTimeMicroSecs The micro-second part of the new access time.
//     * @param modificationTimeSecs The new modification time in seconds since start of the epoch.
//     * @param modificationTimeMicroSecs The micro-second part of the new modification time.
//     */
//    public static void setLinkTimestamps(String fileName,
//            long accessTimeSecs,
//            long accessTimeMicroSecs,
//            long modificationTimeSecs,
//            long modificationTimeMicroSecs)
//    {
//        try
//        {
//            Instant accessTimeInstant = Instant.ofEpochSecond(accessTimeSecs).plus(accessTimeMicroSecs, ChronoUnit.MICROS);
//            FileTime accessTime = FileTime.from(accessTimeInstant);
//            Instant modifiedTimeInstant = Instant.ofEpochSecond(modificationTimeSecs).plus(modificationTimeMicroSecs, ChronoUnit.MICROS);
//            FileTime modifiedTime = FileTime.from(modifiedTimeInstant);
//            Files.getFileAttributeView(Path.of(fileName), PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS).setTimes(modifiedTime, accessTime, null);
//            Files.getFileAttributeView(Path.of(fileName), PosixFileAttributeView.class).setTimes(modifiedTime, accessTime, null);
//        } catch (IOException e)
//        {
//            throw new IOExceptionUnchecked(e);
//        }
//    }
//
//    /**
//     * Change file timestamps of a file, directory or link to the current time. Does not dereference a symbolic link.
//     *
//     * @param fileName The name of the file or link to change the timestamp of.
//     */
//    public static void setLinkTimestamps(String fileName) throws IOExceptionUnchecked
//    {
//        Time time = Time.getInstance();
//        setLinkTimestamps(fileName, time.getSecs(), time.getMicroSecPart(), time.getSecs(), time.getMicroSecPart());
//    }
//
//    /**
//     * Change file timestamps of a file, directory or link. Dereferences a symbolic link.
//     *
//     * @param fileName The name of the file or link to change the timestamp of.
//     * @param accessTimeSecs The new access time in seconds since start of the epoch.
//     * @param accessTimeMicroSecs The micro-second part of the new access time.
//     * @param modificationTimeSecs The new modification time in seconds since start of the epoch.
//     * @param modificationTimeMicroSecs The micro-second part of the new modification time.
//     */
//    public static void setFileTimestamps(String fileName,
//            long accessTimeSecs, long accessTimeMicroSecs,
//            long modificationTimeSecs, long modificationTimeMicroSecs) throws IOExceptionUnchecked
//    {
//        try
//        {
//            Instant accessTimeInstant = Instant.ofEpochSecond(accessTimeSecs).plus(accessTimeMicroSecs, ChronoUnit.MICROS);
//            FileTime accessTime = FileTime.from(accessTimeInstant);
//            Instant modifiedTimeInstant = Instant.ofEpochSecond(modificationTimeSecs).plus(modificationTimeMicroSecs, ChronoUnit.MICROS);
//            FileTime modifiedTime = FileTime.from(modifiedTimeInstant);
//            Files.getFileAttributeView(Path.of(fileName), PosixFileAttributeView.class).setTimes(modifiedTime, accessTime, null);
//        } catch (IOException e)
//        {
//            throw new IOExceptionUnchecked(e);
//        }
//    }

    //
    // File related methods
    //

    /**
     * A class to represent a Unix <code>struct timespec</code> that holds a system time in nano-second resolution.
     */
    public static final class Time
    {
        private final long secs;

        private final long nanos;

        public static Time getInstance() {
            Instant now = Instant.now();
            return new Time(now);
        }

        private Time(Instant now) {
            this(now.getEpochSecond(), now.getNano());
        }

        private Time(FileTime fileTime)
        {
            this(fileTime.toInstant().getEpochSecond(), fileTime.toInstant().getNano());
        }

        private Time(long secs, long nanos)
        {
            this.secs = secs;
            this.nanos = nanos;
        }

        public long getSecs()
        {
            return secs;
        }

        public long getNanoSecPart()
        {
            return nanos;
        }

        public long getMicroSecPart()
        {
            if (nanos % 1000 >= 500)
            {
                return nanos / 1_000 + 1;
            } else
            {
                return nanos / 1_000;
            }
        }

        public long getMilliSecPart()
        {
            if (nanos % 1000000 >= 500000)
            {
                return nanos / 1_000_000 + 1;
            } else
            {
                return nanos / 1_000_000;
            }
        }

        public long getMillis()
        {
            return secs * 1_000 + getMilliSecPart();
        }

        @Override
        public String toString()
        {
            return "Time [secs=" + secs + ", nanos=" + nanos + "]";
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + (int) (nanos ^ (nanos >>> 32));
            result = prime * result + (int) (secs ^ (secs >>> 32));
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            final Posix.Time other = (Posix.Time) obj;
            if (nanos != other.nanos)
            {
                return false;
            }
            if (secs != other.secs)
            {
                return false;
            }
            return true;
        }

    }

    public static class Stat
    {
        private final Path path;

        private final short permissions;

        private final FileLinkType linkType;

        private final Time lastModified;

        private final Time lastAccessed;

        private final int uid;

        private final int gid;

        private final String symbolicLinkOrNull;

        private final long size;

        public Stat(Path path, short permissions, FileLinkType linkType, FileTime lastModified, FileTime lastAccessed, int uid, int gid,
                String symbolicLinkOrNull, long size)
        {
            this.path = path;
            this.permissions = permissions;
            this.linkType = linkType;
            this.lastModified = new Time(lastModified);
            this.lastAccessed = new Time(lastAccessed);
            this.uid = uid;
            this.gid = gid;
            this.symbolicLinkOrNull = symbolicLinkOrNull;
            this.size = size;
        }

        public short getPermissions()
        {
            return permissions;
        }

        public FileLinkType getLinkType()
        {
            return linkType;
        }

        public long getLastAccess()
        {
            return lastAccessed.getSecs();
        }

        public Time getLastAccessTime()
        {
            return lastAccessed;
        }

        public long getLastModified()
        {
            return lastModified.getSecs();
        }

        public Time getLastModifiedTime()
        {
            return lastModified;
        }

        public int getUid()
        {
            return uid;
        }

        public int getGid()
        {
            return gid;
        }

        public String tryGetSymbolicLink()
        {
            return symbolicLinkOrNull;
        }

        public long getSize() {
            return size;
        }

        public boolean isSymbolicLink()
        {
            return symbolicLinkOrNull != null;
        }

        /**
         * Returns the number of hard links for the <var>linkName</var>. Does not dereference a symbolic link.
         *
         * @throws IOExceptionUnchecked If the information could not be obtained, e.g. because the link does not exist.
         */
        public int getNumberOfHardLinks() throws IOException
        {
            Number count = (Number) Files.getAttribute(path, "unix:nlink", LinkOption.NOFOLLOW_LINKS);
            return count.intValue();
        }
    }

    private static short getNumericAccessMode(Set<PosixFilePermission> permissions) {
        short posixPermissions = 0;

        for (PosixFilePermission permission : permissions) {
            switch (permission) {
                case OWNER_READ:
                    posixPermissions |= 0400;
                    break;
                case OWNER_WRITE:
                    posixPermissions |= 0200;
                    break;
                case OWNER_EXECUTE:
                    posixPermissions |= 0100;
                    break;
                case GROUP_READ:
                    posixPermissions |= 0040;
                    break;
                case GROUP_WRITE:
                    posixPermissions |= 0020;
                    break;
                case GROUP_EXECUTE:
                    posixPermissions |= 0010;
                    break;
                case OTHERS_READ:
                    posixPermissions |= 0004;
                    break;
                case OTHERS_WRITE:
                    posixPermissions |= 0002;
                    break;
                case OTHERS_EXECUTE:
                    posixPermissions |= 0001;
                    break;
            }
        }

        return posixPermissions;
    }

    private static Set<PosixFilePermission> getFilePermissionsMode(short permissions) {
        Set<PosixFilePermission> posixPermissions = new HashSet<>();

        if ((permissions & 0400) != 0) {
            posixPermissions.add(PosixFilePermission.OWNER_READ);
        }
        if ((permissions & 0200) != 0) {
            posixPermissions.add(PosixFilePermission.OWNER_WRITE);
        }
        if ((permissions & 0100) != 0) {
            posixPermissions.add(PosixFilePermission.OWNER_EXECUTE);
        }
        if ((permissions & 0040) != 0) {
            posixPermissions.add(PosixFilePermission.GROUP_READ);
        }
        if ((permissions & 0020) != 0) {
            posixPermissions.add(PosixFilePermission.GROUP_WRITE);
        }
        if ((permissions & 0010) != 0) {
            posixPermissions.add(PosixFilePermission.GROUP_EXECUTE);
        }
        if ((permissions & 0004) != 0) {
            posixPermissions.add(PosixFilePermission.OTHERS_READ);
        }
        if ((permissions & 0002) != 0) {
            posixPermissions.add(PosixFilePermission.OTHERS_WRITE);
        }
        if ((permissions & 0001) != 0) {
            posixPermissions.add(PosixFilePermission.OTHERS_EXECUTE);
        }

        return posixPermissions;
    }

    /**
     * Returns the information about <var>linkName</var>.
     *
     * @throws IOExceptionUnchecked If the information could not be obtained, e.g. because the link does not exist.
     */
    public static Stat getLinkInfo(String absolutePath)
    {
        return getLinkInfo(absolutePath, true);
    }

    /**
     * Returns the information about <var>linkName</var>. If <code>readSymbolicLinkTarget == true</code>, then the symbolic link target is read when
     * <var>linkName</var> is a symbolic link.
     *
     * @throws IOExceptionUnchecked If the information could not be obtained, e.g. because the link does not exist.
     */
    public static Stat getLinkInfo(String pathAsString, boolean readSymbolicLinkTarget)
    {
        try {
            if (pathAsString == null)
            {
                throw new NullPointerException("linkName");
            }

            Path path = Path.of(pathAsString);
            if (Files.exists(path, LinkOption.NOFOLLOW_LINKS) == false)
            {
                return null;
            }


            PosixFileAttributes attrs = null;
            FileLinkType linkType;
            short permissions;
            int uid;
            int gid;
            if(readSymbolicLinkTarget && Files.exists(path))
            {
                permissions = getNumericAccessMode(Files.getPosixFilePermissions(path));
                attrs = Files.readAttributes(path, PosixFileAttributes.class);
                if (Files.isSymbolicLink(path)) {
                    linkType = FileLinkType.SYMLINK;
                } else if (Files.isDirectory(path)) {
                    linkType = FileLinkType.DIRECTORY;
                } else if (Files.isRegularFile(path)) {
                    linkType = FileLinkType.REGULAR_FILE;
                } else {
                    linkType = FileLinkType.OTHER;
                }
                uid = getUid(pathAsString);
                gid = getGid(pathAsString);
            } else {
                permissions = getNumericAccessMode(Files.getPosixFilePermissions(path, LinkOption.NOFOLLOW_LINKS));
                attrs = Files.readAttributes(path, PosixFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
                if (Files.isSymbolicLink(path)) {
                    linkType = FileLinkType.SYMLINK;
                } else if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
                    linkType = FileLinkType.DIRECTORY;
                } else if (Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) {
                    linkType = FileLinkType.REGULAR_FILE;
                } else {
                    linkType = FileLinkType.OTHER;
                }
                uid = getUid(pathAsString, false);
                gid = getGid(pathAsString, false);
            }

            FileTime lastModified = attrs.lastModifiedTime();
            FileTime lastAccessed = attrs.lastAccessTime();

            String symbolicLinkOrNull = null;
            if (linkType == FileLinkType.SYMLINK && readSymbolicLinkTarget) {
                symbolicLinkOrNull = Files.readSymbolicLink(path).toString();
            }
            long size = attrs.size();
            return new Stat(path, permissions, linkType, lastModified, lastAccessed, uid, gid, symbolicLinkOrNull, size);
        } catch (IOException e)
        {
            throw new IOExceptionUnchecked(e);
        }
    }

    /**
     * Returns the information about <var>linkName</var>, or {@link NullPointerException}, if the information could not be obtained, e.g. because the
     * link does not exist.
     */
    public static Stat tryGetLinkInfo(String pathAsString){
        return getLinkInfo(pathAsString, true);
    }

    /**
     * Returns the information about <var>fileName</var>, or {@link NullPointerException}, if the information could not be obtained, e.g. because the
     * file does not exist.
     */
    public static Stat tryGetFileInfo(String absolutePath)
    {
        return getFileInfo(absolutePath, true);
    }

    /**
     * Returns the information about <var>fileName</var>.
     *
     * @throws IOExceptionUnchecked If the information could not be obtained, e.g. because the file does not exist.
     */
    public static Stat getFileInfo(String pathAsString)
    {
        return getFileInfo(pathAsString, true);
    }

    /**
     * Returns the information about <var>fileName</var>.
     *
     * @throws IOExceptionUnchecked If the information could not be obtained, e.g. because the file does not exist.
     */
    public static Stat getFileInfo(String pathAsString, boolean readSymbolicLinkTarget)
            throws IOExceptionUnchecked
    {
        try {
            if (pathAsString == null)
            {
                throw new NullPointerException("linkName");
            }

            Path path = Path.of(pathAsString);
            if (Files.exists(path) == false)
            {
                return null;
            }

            short permissions = getNumericAccessMode(Files.getPosixFilePermissions(path));
            PosixFileAttributes attrs = Files.readAttributes(path, PosixFileAttributes.class);

            FileLinkType linkType;
            if (attrs.isSymbolicLink()) {
                linkType = FileLinkType.SYMLINK;
            } else if (attrs.isDirectory()) {
                linkType = FileLinkType.DIRECTORY;
            } else if (attrs.isRegularFile()) {
                linkType = FileLinkType.REGULAR_FILE;
            } else {
                linkType = FileLinkType.OTHER;
            }

            FileTime lastModified = attrs.lastModifiedTime();
            FileTime lastAccessed = attrs.lastAccessTime();
            int uid = getUid(pathAsString);
            int gid = getGid(pathAsString);
            String symbolicLinkOrNull = null;
            if (linkType == FileLinkType.SYMLINK && readSymbolicLinkTarget) {
                symbolicLinkOrNull = Files.readSymbolicLink(path).toString();
            }
            long size = attrs.size();
            return new Stat(path, permissions, linkType, lastModified, lastAccessed, uid, gid, symbolicLinkOrNull, size);
        } catch (IOException e)
        {
            throw new IOExceptionUnchecked(e);
        }
    }
    
    public static Set<PosixFilePermission> getPermissions(String path) throws IOExceptionUnchecked {
        try
        {
            return Files.getPosixFilePermissions(Path.of(path));
        } catch (IOException e)
        {
            throw new IOExceptionUnchecked(e);
        }
    }

    public static void setAccessMode(String path, Set<PosixFilePermission> permissions) throws IOExceptionUnchecked {
        try {
            Files.setPosixFilePermissions(Path.of(path), permissions);
        } catch (IOException e) {
            throw new IOExceptionUnchecked(e);
        }
    }

    /**
     * Sets the access mode of <var>filename</var> to the specified <var>mode</var> value.
     * Dereferences a symbolic link.
     */
    public static void setAccessMode(String path, short mode) throws IOExceptionUnchecked {
        Set<PosixFilePermission> permissions = getFilePermissionsMode(mode);
        setAccessMode(path, permissions);
    }


    public static void setAccessMode777(String path) throws IOExceptionUnchecked {
        try {
            Files.setPosixFilePermissions(Path.of(path), Set.of(PosixFilePermission.values()));
        } catch (IOException e) {
            throw new IOExceptionUnchecked(e);
        }
    }

    public static boolean isSymbolicLink(String absolutePath) {
        return Files.isSymbolicLink(Path.of(absolutePath));
    }

    /**
     * Creates a hard link <var>linkName</var> that points to <var>fileName</var>.
     *
     * @throws IOExceptionUnchecked If the underlying system call fails, e.g. because <var>linkName</var> already exists or <var>fileName</var> does
     *             not exist.
     */
    public static final void createHardLink(String fileName, String linkName)
            throws IOExceptionUnchecked
    {
        if (fileName == null)
        {
            throw new NullPointerException("fileName");
        }
        if (linkName == null)
        {
            throw new NullPointerException("linkName");
        }
        link(fileName, linkName);
    }

    /**
     * Creates a symbolic link <var>linkName</var> that points to <var>fileName</var>.
     *
     * @throws IOExceptionUnchecked If the underlying system call fails, e.g. because <var>linkName</var> already exists.
     */
    public static final void createSymbolicLink(String fileName, String linkName)
            throws IOExceptionUnchecked
    {
        if (fileName == null)
        {
            throw new NullPointerException("fileName");
        }
        if (linkName == null)
        {
            throw new NullPointerException("linkName");
        }

        symlink(fileName, linkName);
    }

    /*
     * This method manages symbolic link creation using NIO API.
     */
    public static final void symlink(String fileName, String linkName) throws IOExceptionUnchecked {
        try {
            Path file = Path.of(fileName);
            Path link = Path.of(linkName);
            Files.createSymbolicLink(link, file);// Creates the link
        } catch (IOException exception) {
            throw new IOExceptionUnchecked(exception);
        }
    }

    /*
     * This method manages link creation using NIO API.
     */
    public static final void link(String fileName, String linkName) throws IOExceptionUnchecked {
        try {
            Path file = Path.of(fileName);
            Path link = Path.of(linkName);
            Files.createLink(link, file); // Creates the link
        } catch (IOException exception) {
            throw new IOExceptionUnchecked(exception);
        }
    }

}
