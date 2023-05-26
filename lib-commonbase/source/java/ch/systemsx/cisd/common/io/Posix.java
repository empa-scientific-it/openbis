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
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.HashSet;
import java.util.Set;

public final class Posix
{
    // Make constructor private to make clear is a utility class
    private Posix() {

    }

    public static boolean isOperational() {
        return true;
    }

    //
    // User related methods
    //

    public static int getGid()
    {
        try
        {
            Process process = Runtime.getRuntime().exec("id -g -r");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String output = reader.readLine();
            reader.close();
            return Integer.parseInt(output);
        } catch (IOException e)
        {
            throw new IOExceptionUnchecked(e);
        }
    }

    public static int getUid() throws IOExceptionUnchecked
    {
        try
        {
            Process process = Runtime.getRuntime().exec("id -u -r");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String output = reader.readLine();
            reader.close();
            return Integer.parseInt(output);
        } catch (IOException e)
        {
            throw new IOExceptionUnchecked(e);
        }
    }

    public static int getEuid()
    {
        try
        {
            Process process = Runtime.getRuntime().exec("id -u");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String output = reader.readLine();
            reader.close();
            return Integer.parseInt(output);
        } catch (IOException e)
        {
            throw new IOExceptionUnchecked(e);
        }
    }

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

    public static String tryGetUserNameForUid(int uid)
    {
        try
        {
            UserPrincipalLookupService service = FileSystems.getDefault().getUserPrincipalLookupService();
            UserPrincipal user = service.lookupPrincipalByName(Integer.toString(uid));
            return user.getName();
        } catch (IOException e)
        {
            throw new IOExceptionUnchecked(e);
        }
    }

    public static String tryGetGroupNameForGid(int gid)
    {
        try
        {
            UserPrincipalLookupService service = FileSystems.getDefault().getUserPrincipalLookupService();
            GroupPrincipal group = service.lookupPrincipalByGroupName(Integer.toString(gid));
            return group.getName();
        } catch (IOException e)
        {
            throw new IOExceptionUnchecked(e);
        }
    }

    //
    // File related methods
    //

    public static class Stat
    {
        private final short permissions;

        private final FileLinkType linkType;

        private final long lastModified;

        private final int uid;

        private final int gid;

        private final String symbolicLinkOrNull;

        private final long size;

        public Stat(short permissions, FileLinkType linkType, long lastModified, int uid, int gid,
                String symbolicLinkOrNull, long size)
        {
            this.permissions = permissions;
            this.linkType = linkType;
            this.lastModified = lastModified;
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

        public long getLastModified()
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

        public String getSymbolicLinkOrNull()
        {
            return symbolicLinkOrNull;
        }

        public long getSize() {
            return size;
        }
    }

    private static short getNumericAccessMode(Set<PosixFilePermission> permissions) {
        short mode = 0;
        if (permissions.contains(PosixFilePermission.OWNER_READ)) {
            mode |= 0400;
        }
        if (permissions.contains(PosixFilePermission.OWNER_WRITE)) {
            mode |= 0200;
        }
        if (permissions.contains(PosixFilePermission.OWNER_EXECUTE)) {
            mode |= 0100;
        }
        if (permissions.contains(PosixFilePermission.GROUP_READ)) {
            mode |= 040;
        }
        if (permissions.contains(PosixFilePermission.GROUP_WRITE)) {
            mode |= 020;
        }
        if (permissions.contains(PosixFilePermission.GROUP_EXECUTE)) {
            mode |= 010;
        }
        if (permissions.contains(PosixFilePermission.OTHERS_READ)) {
            mode |= 04;
        }
        if (permissions.contains(PosixFilePermission.OTHERS_WRITE)) {
            mode |= 02;
        }
        if (permissions.contains(PosixFilePermission.OTHERS_EXECUTE)) {
            mode |= 01;
        }
        return mode;
    }

    private static Set<PosixFilePermission> getFilePermissionsMode(short mode) {
        Set<PosixFilePermission> permissions = new HashSet<>();
        if ((0400 & mode) == 0400) {
            permissions.add(PosixFilePermission.OWNER_READ);
        }
        if ((0200 & mode) == 0200) {
            permissions.add(PosixFilePermission.OWNER_WRITE);
        }
        if ((0100 & mode) == 0100) {
            permissions.add(PosixFilePermission.OWNER_EXECUTE);
        }
        if ((040 & mode) == 040) {
            permissions.add(PosixFilePermission.GROUP_READ);
        }
        if ((020 & mode) == 020) {
            permissions.add(PosixFilePermission.GROUP_WRITE);
        }
        if ((010 & mode) == 010) {
            permissions.add(PosixFilePermission.GROUP_EXECUTE);
        }
        if ((04 & mode) == 04) {
            permissions.add(PosixFilePermission.OTHERS_READ);
        }
        if ((02 & mode) == 02) {
            permissions.add(PosixFilePermission.OWNER_WRITE);
        }
        if ((01 & mode) == 01) {
            permissions.add(PosixFilePermission.OTHERS_EXECUTE);
        }
        return permissions;
    }

    public static Stat tryGetLinkInfo(String pathAsString) {
        try
        {
            Path path = Path.of(pathAsString);
            short permissions = getNumericAccessMode(Files.getPosixFilePermissions(path));
            FileLinkType linkType;
            if (Files.isSymbolicLink(path)) {
                linkType = FileLinkType.SYMLINK;
            } else if (Files.isDirectory(path)) {
                linkType = FileLinkType.DIRECTORY;
            } else {
                linkType = FileLinkType.OTHER;
            }
            long lastModified = Files.getLastModifiedTime(path).toMillis();
            int uid = (int) Files.getAttribute(path, "unix:uid");
            int gid = (int) Files.getAttribute(path, "unix:gid");
            String symbolicLinkOrNull = null;
            if (linkType == FileLinkType.SYMLINK) {
                symbolicLinkOrNull = Files.readSymbolicLink(path).toString();
            }

            long size = Files.size(path);
            return new Stat(permissions, linkType, lastModified, uid, gid, symbolicLinkOrNull, size);
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

    public static void setAccessMode(String path, short mode) throws IOExceptionUnchecked {
        try {
            Set<PosixFilePermission> permissions = getFilePermissionsMode(mode);
            Files.setPosixFilePermissions(Path.of(path), permissions);
        } catch (IOException e) {
            throw new IOExceptionUnchecked(e);
        }
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
            Files.createDirectories(link.getParent()); // Create any missing folder on the directory hierarchy leading to folder that will contain the link
            Files.createSymbolicLink(link, file); // Creates the link
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
            Files.createDirectories(link.getParent()); // Create any missing folder on the directory hierarchy leading to folder that will contain the link
            Files.createLink(link, file); // Creates the link
        } catch (IOException exception) {
            throw new IOExceptionUnchecked(exception);
        }
    }

}
