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

package ch.systemsx.cisd.base.unix;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import com.sun.security.auth.module.UnixSystem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.Set;

public final class Posix
{
    // Make constructor private to make clear is a utility class
    private Posix() {

    }

    public static boolean isOperational() {
        return true;
    }

    // Available on unix systems JDK 11 onwards
    private static UnixSystem unixSystem = new UnixSystem();

    //
    // User Functions
    //

    public static int getEuid() {
        try {
            String processId = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
            String command = "id -u " + processId;

            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String output = reader.readLine();
            reader.close();

            if (output != null) {
                return Integer.parseInt(output);
            } else {
                throw new IOExceptionUnchecked("Failed to retrieve user ID.");
            }
        } catch (IOException | NumberFormatException ex) {
            throw new IOExceptionUnchecked(ex);
        }
    }

    public static int getUid()
    {
        return (int) unixSystem.getUid();
    }

    public static int getGid()
    {
        return (int) unixSystem.getGid();
    }

    public static String tryGetUserNameForUid(int uid) throws IOExceptionUnchecked {
        try
        {
            FileSystem fileSystem = FileSystems.getDefault();
            UserPrincipalLookupService service = fileSystem.getUserPrincipalLookupService();
            UserPrincipal groupPrincipal = service.lookupPrincipalByName(Integer.toString(uid));
            return groupPrincipal.getName();
        } catch (IOException ex) {
            throw new IOExceptionUnchecked(ex);
        }
    }

    public static String tryGetGroupNameForGid(int gid) throws IOExceptionUnchecked {
        try
        {
            FileSystem fileSystem = FileSystems.getDefault();
            UserPrincipalLookupService service = fileSystem.getUserPrincipalLookupService();
            GroupPrincipal groupPrincipal = service.lookupPrincipalByGroupName(Integer.toString(gid));
            return groupPrincipal.getName();
        } catch (IOException ex) {
            throw new IOExceptionUnchecked(ex);
        }
    }

    //
    // File functions
    //

    public static void setOwner(String pathAsString, int uid, int gid) throws IOExceptionUnchecked
    {
        try
        {
            FileSystem fileSystem = FileSystems.getDefault();
            UserPrincipalLookupService service = fileSystem.getUserPrincipalLookupService();
            Path path = Path.of(pathAsString);
            UserPrincipal user = service.lookupPrincipalByName(Integer.toString(uid));
            Files.setOwner(path, user);
            UserPrincipal group = service.lookupPrincipalByGroupName(Integer.toString(gid));
            Files.setOwner(path, group);
        } catch (IOException ex) {
            throw new IOExceptionUnchecked(ex);
        }
    }

    public static boolean isSymbolicLink(String path) {
        return Files.isSymbolicLink(Path.of(path));
    }

    private static Set<PosixFilePermission> allPermissionsMode = Set.of(PosixFilePermission.OWNER_READ,
            PosixFilePermission.OWNER_WRITE,
            PosixFilePermission.OWNER_EXECUTE,
            PosixFilePermission.GROUP_READ,
            PosixFilePermission.GROUP_WRITE,
            PosixFilePermission.GROUP_EXECUTE,
            PosixFilePermission.OTHERS_READ,
            PosixFilePermission.OTHERS_WRITE,
            PosixFilePermission.OTHERS_EXECUTE);

    public static final void setAccessMode(String fileName, short mode) throws IOExceptionUnchecked
    {
        if (fileName == null)
        {
            throw new NullPointerException("fileName");
        }

        if (mode != 777) {
            throw new IOExceptionUnchecked("Unsupported unix file permission mode: " + mode);
        }

        try
        {
            Files.setPosixFilePermissions(Path.of(fileName), allPermissionsMode);
        } catch (IOException e)
        {
            throw new IOExceptionUnchecked(e);
        }
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
            Path relativeFilePath = link.getParent().relativize(file); // Relative path to the file from the link
            Files.createDirectories(link.getParent()); // Create any missing folder on the directory hierarchy leading to folder that will contain the link
            Files.createSymbolicLink(link, relativeFilePath); // Creates the link
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
            Path relativeFilePath = link.getParent().relativize(file); // Relative path to the file from the link
            Files.createDirectories(link.getParent()); // Create any missing folder on the directory hierarchy leading to folder that will contain the link
            Files.createLink(link, relativeFilePath); // Creates the link
        } catch (IOException exception) {
            throw new IOExceptionUnchecked(exception);
        }
    }

}
