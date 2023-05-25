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

public final class Posix
{
    // Make constructor private to make clear is a utility class
    private Posix() {

    }

    public static boolean isOperational() {
        return true;
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
            Path file = new File(fileName).getCanonicalFile().toPath();
            Path link = new File(linkName).getCanonicalFile().toPath();
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
            Path file = new File(fileName).getCanonicalFile().toPath();
            Path link = new File(linkName).getCanonicalFile().toPath();
            Files.createDirectories(link.getParent()); // Create any missing folder on the directory hierarchy leading to folder that will contain the link
            Files.createLink(link, file); // Creates the link
        } catch (IOException exception) {
            throw new IOExceptionUnchecked(exception);
        }
    }

}
