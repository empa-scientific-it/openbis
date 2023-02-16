/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.common.filesystem.highwatermark;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.HostAwareFile;
import ch.systemsx.cisd.common.properties.PropertyUtils;

/**
 * A {@link File} with a <i>high water mark</i> attached to it.
 * <p>
 * This file could be located on an external host.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class HostAwareFileWithHighwaterMark extends HostAwareFile
{
    private static final long serialVersionUID = 1L;

    static final String SEP = "-";

    public static final int DEFAULT_HIGHWATER_MARK = -1;

    /**
     * The property name under which you must find a <code>long</code> for the high water mark (in <i>kilobytes</i>).
     */
    public static final String HIGHWATER_MARK_PROPERTY_KEY = "highwater-mark";

    private final long highwaterMarkInKb;

    /**
     * @param hostOrNull the host on which given <var>file</var> is located.
     * @param path the file path.
     * @param rsyncModuleOrNull The name of the module on the rsync server or <code>null</code>, if no rsync server should be used.
     * @param highwaterMarkInKb the high water mark in <i>kilobytes</i>. <code>-1</code> means that the system will not be watching.
     */
    public HostAwareFileWithHighwaterMark(final String hostOrNull, final String path,
            final String rsyncModuleOrNull, final long highwaterMarkInKb)
    {
        super(hostOrNull, path, rsyncModuleOrNull);
        this.highwaterMarkInKb = highwaterMarkInKb;
    }

    /**
     * @param path the file path.
     * @param highwaterMarkInKb the high water mark in <i>kilobytes</i>. <code>-1</code> means that the system will not be watching.
     */
    public HostAwareFileWithHighwaterMark(final String path, final long highwaterMarkInKb)
    {
        this(null, path, null, highwaterMarkInKb);
    }

    /**
     * @param path the file path.
     */
    public HostAwareFileWithHighwaterMark(final String path)
    {
        this(path, DEFAULT_HIGHWATER_MARK);
    }

    /**
     * @param hostOrNull the host on which given <var>file</var> is located.
     * @param path the file path.
     * @param rsyncModuleOrNull The name of the module on the rsync server or <code>null</code>, if no rsync server should be used.
     */
    public HostAwareFileWithHighwaterMark(final String hostOrNull, final String path,
            final String rsyncModuleOrNull)
    {
        this(hostOrNull, path, rsyncModuleOrNull, DEFAULT_HIGHWATER_MARK);
    }

    /**
     * Instantiates a new <code>FileWithHighwaterMark</code> from given <var>properties</var>. Some examples:
     * 
     * <pre>
     * &lt;host-file-property-key&gt; = /temp
     * &lt;host-file-property-key&gt; = localhost:/temp
     * &lt;host-file-property-key&gt;-highwater-mark = 123456
     * </pre>
     * 
     * @param hostFilePropertyKey the property key under which the host/file path can be found.
     * @throws ConfigurationFailureException if given <var>hostFilePropertyKey</var> could not be found in given <var>properties</var>.
     */
    public final static HostAwareFileWithHighwaterMark fromProperties(final Properties properties,
            final String hostFilePropertyKey) throws ConfigurationFailureException
    {
        assert properties != null : "Unspecified properties";
        assert StringUtils.isNotBlank(hostFilePropertyKey) : "Host-file property key is blank";
        final String hostFile = PropertyUtils.getMandatoryProperty(properties, hostFilePropertyKey);
        final long highwaterMarkInKb =
                PropertyUtils.getLong(properties, hostFilePropertyKey.concat(SEP).concat(
                        HIGHWATER_MARK_PROPERTY_KEY), -1L);
        return create(hostFile, highwaterMarkInKb);
    }

    /**
     * Instantiates a new <code>FileWithHighwaterMark</code> from specified host file and high-water mark.
     * 
     * @param hostFile Either a local file or remote file in SSH notation (i.e. <host>:<path>).
     * @param highwaterMarkInKb -1 means no checking for high water.
     */
    public static HostAwareFileWithHighwaterMark create(final String hostFile,
            final long highwaterMarkInKb)
    {
        String path;
        String hostNameOrNull = null;
        final int index = getHostFileIndex(hostFile);
        final String rsyncModuleOrNull;
        if (index > -1)
        {
            hostNameOrNull = hostFile.substring(0, index);
            final int index2 = hostFile.indexOf(HOST_FILE_SEP, index + 1);
            if (index2 > -1)
            {
                rsyncModuleOrNull = hostFile.substring(index + 1, index2);
                path = hostFile.substring(index2 + 1);
            } else
            {
                rsyncModuleOrNull = null;
                path = hostFile.substring(index + 1);
            }
        } else
        {
            rsyncModuleOrNull = null;
            path = getCanonicalFile(hostFile);
        }
        return new HostAwareFileWithHighwaterMark(hostNameOrNull, path, rsyncModuleOrNull,
                highwaterMarkInKb);
    }

    private static String getCanonicalFile(final String hostFile)
    {
        File file = new File(hostFile);
        try
        {
            return file.getCanonicalFile().getPath();
        } catch (IOException ex)
        {
            throw new ConfigurationFailureException("Unknown file " + file.getAbsolutePath(), ex);
        }
    }

    /**
     * Returns the high water mark for this file.
     */
    public final long getHighwaterMark()
    {
        return highwaterMarkInKb;
    }

    //
    // Object
    //

    @Override
    public final String toString()
    {
        final StringBuilder builder = new StringBuilder(getPathDescription());
        builder.append(" ").append(
                String.format("[high water mark: %s]", HighwaterMarkWatcher
                        .displayKilobyteValue(getHighwaterMark())));
        return builder.toString();
    }
}
