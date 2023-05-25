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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

public final class Posix
{
    // Make constructor private to make clear is a utility class
    private Posix() {

    }

    public static boolean isOperational() {
        return true;
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
        if (mode != (short) 0777) {
            throw new IOExceptionUnchecked("Failure to set file permissions for '" + path + "', mode 777 is supported.");
        }
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
