/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.filesystem;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;

/**
 * Class which adds a prefix to a path. Which prefix is added depends on whether the path starts with '/' (absolute path) or not (relative path).
 * 
 * @author Franz-Josef Elmer
 */
public final class PathPrefixPrepender
{
    private final File absolutePathsDirectoryOrNull;

    private final File relativePathsDirectoryOrNull;

    private final String prefixForAbsolutePaths;

    private final String prefixForRelativePaths;

    /**
     * Creates an instances for the specified prefixes. <code>null</code> arguments are handled as empty strings.
     */
    public PathPrefixPrepender(final String prefixForAbsolutePathsOrNull,
            final String prefixForRelativePathsOrNull) throws ConfigurationFailureException
    {
        this.prefixForAbsolutePaths = defaultString(prefixForAbsolutePathsOrNull);
        absolutePathsDirectoryOrNull = tryCreateFile(this.prefixForAbsolutePaths, "absolute");
        this.prefixForRelativePaths = preparePrefix(defaultString(prefixForRelativePathsOrNull));
        relativePathsDirectoryOrNull = tryCreateFile(this.prefixForRelativePaths, "relative");
    }

    private final static String defaultString(final String path)
    {
        return StringUtils.defaultString(path).trim();
    }

    private final static String preparePrefix(final String pathPrefix)
    {
        assert pathPrefix != null : "Unspecified path prefix.";
        if (pathPrefix.length() == 0)
        {
            return "";
        }
        if (pathPrefix.endsWith("/"))
        {
            return pathPrefix;
        }
        return pathPrefix + "/";
    }

    private final static File tryCreateFile(final String prefix, final String type)
            throws ConfigurationFailureException
    {
        assert prefix != null : "Unspecified path prefix.";
        if (prefix.length() > 0)
        {
            final File file = new File(prefix);
            final String response =
                    FileUtilities.checkDirectoryFullyAccessible(file, type + " prefix path");
            if (response != null)
            {
                throw new ConfigurationFailureException(response);
            }
            return file;
        }
        return null;
    }

    /**
     * Returns the directory for absolute paths.
     */
    public final File tryGetDirectoryForAbsolutePaths()
    {
        return absolutePathsDirectoryOrNull;
    }

    /**
     * Returns the directory for relative paths.
     */
    public final File tryGetDirectoryForRelativePaths()
    {
        return relativePathsDirectoryOrNull;
    }

    /**
     * Returns the specified path with the appropriated prefix.
     */
    public final String addPrefixTo(final String path)
    {
        assert path != null : "Undefined path.";
        return (FilenameUtils.getPrefixLength(path) > 0 ? prefixForAbsolutePaths
                : prefixForRelativePaths)
                + path;
    }
}
