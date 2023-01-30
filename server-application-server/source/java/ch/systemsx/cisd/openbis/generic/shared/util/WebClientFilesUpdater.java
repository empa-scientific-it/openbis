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

package ch.systemsx.cisd.openbis.generic.shared.util;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.DefaultClientPluginFactoryProvider;

/**
 * Class which updates <code>OpenBIS.gwt.xml</code> and {@link DefaultClientPluginFactoryProvider}.
 * <p>
 * Usage: <tt>java {@link WebClientFilesUpdater} [<working directory> [<technology 1> <technology 2> ...]]</tt> Without technology arguments all
 * technologies found in the code base are available. If at least one technology is specified only the specified technologies will be available.
 * 
 * @author Christian Ribeaud
 */
public final class WebClientFilesUpdater
{
    @Private
    static final String SOURCE_TAG_TEMPLATE =
            "<source path=\"plugin/%1$s/client/web/client\"/>\n"
                    + "    <source path=\"plugin/%1$s/shared/basic\"/>";

    @Private
    static final String SCRIPT_TAG_TEMPLATE = "<script src=\"%s-dictionary.js\"/>";

    @Private
    static final String PUBLIC_TAG_TEMPLATE = "<public path=\"plugin/%s/client/web/public\"/>";

    @Private
    static final String JAVA_MARKER_START = "// Automatically generated part - START";

    @Private
    static final String XML_MARKER_START = "<!-- Automatically generated part - START -->";

    @Private
    static final String JAVA_MARKER_END = "// Automatically generated part - END";

    @Private
    static final String XML_MARKER_END = "<!-- Automatically generated part - END -->";

    @Private
    static final String PLUGIN_FACTORY_CLASS_NAME_TEMPLATE =
            "ch.systemsx.cisd.openbis.plugin.%s.client.web.client.application.ClientPluginFactory";

    @Private
    static final String PLUGIN_FACTORY_REGISTRATION_TEMPLATE =
            "registerPluginFactory(new " + PLUGIN_FACTORY_CLASS_NAME_TEMPLATE
                    + "(originalViewContext));";

    @Private
    static final String PLUGIN_PACKAGE_NAME = "plugin";

    @Private
    static final String CLIENT_PLUGIN_PROVIDER_CLASS =
            "ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.DefaultClientPluginFactoryProvider";

    @Private
    static final String OPENBIS_GWT_XML_FILE_NAME = "OpenBIS-without-entry-point.gwt.xml";

    @Private
    static final String OPENBIS_PACKAGE_NAME = "ch/systemsx/cisd/openbis";

    private final String[] allTechnologies;

    private final String[] technologies;

    private final File workingDirectory;

    @Private
    WebClientFilesUpdater(final String workingDirectory, final String... technologies)
    {
        assert workingDirectory != null : "Unspecified working directory.";
        this.workingDirectory = getWorkingDirectory(workingDirectory);
        this.allTechnologies = scanAllTechnologies(this.workingDirectory);
        this.technologies = getTechnologies(allTechnologies, technologies);
        Arrays.sort(this.technologies);
    }

    private final static void checkTechnologies(final String[] allTechnologies,
            final String[] technologies)
    {
        for (final String technology : technologies)
        {
            if (ArrayUtils.indexOf(allTechnologies, technology) < 0)
            {
                throw new IllegalArgumentException(String.format(
                        "Technology '%s' must be one of '%s'.", technology, Arrays
                                .toString(allTechnologies)));
            }
        }
    }

    private final static File getWorkingDirectory(final String workingDirectory)
    {
        return workingDirectory == null ? new File(".") : new File(workingDirectory);
    }

    private final static String[] getTechnologies(final String[] allTechnologies,
            final String[] technologies)
    {
        if (technologies == null || technologies.length == 0)
        {
            return allTechnologies;
        } else
        {
            checkTechnologies(allTechnologies, technologies);
            return technologies;
        }
    }

    private final static String[] scanAllTechnologies(final File rootDirectory)
    {
        final File pluginRootDirectory = getPluginRootDirectory(rootDirectory);
        final File[] pluginDirs =
                pluginRootDirectory.listFiles((FileFilter) FileFilterUtils
                        .makeSVNAware(FileFilterUtils.directoryFileFilter()));
        return FileUtilities.toFileNames(pluginDirs);
    }

    private final static File getPluginRootDirectory(final File rootDirectory)
    {
        final File openBISPackage =
                new File(rootDirectory, OPENBIS_PACKAGE_NAME + "/" + PLUGIN_PACKAGE_NAME);
        final String response =
                FileUtilities.checkDirectoryFullyAccessible(openBISPackage, "openBIS package");
        if (response != null)
        {
            throw new RuntimeException(response);
        }
        return openBISPackage;
    }

    private final static StringBuilder createTag(final String template, final String indent,
            final String technology)
    {
        final StringBuilder builder = new StringBuilder();
        builder.append(indent);
        builder.append(String.format(template, technology));
        builder.append("\n");
        return builder;
    }

    /**
     * Updates the <code>OpenBIS.gwt.xml</code> up to the technologies found.
     */
    public final void updateOpenBISGwtXmlFile()
    {
        final File openBISGwtXmlFile =
                new File(workingDirectory, OPENBIS_PACKAGE_NAME + "/" + OPENBIS_GWT_XML_FILE_NAME);
        final String response = FileUtilities.checkFileFullyAccessible(openBISGwtXmlFile, "xml");
        if (response != null)
        {
            throw new RuntimeException(response);
        }
        final StringBuilder builder = new StringBuilder(XML_MARKER_START);
        final String sep = "\n";
        builder.append(sep);
        final String indent = StringUtils.repeat(" ", 4);
        boolean first = true;
        for (final String technology : technologies)
        {
            if (first == false)
            {
                builder.append(sep);
            }
            first = false;
            builder.append(indent);
            builder.append(String.format("<!-- %s plugin -->", StringUtils.capitalize(technology)));
            builder.append(sep);
            // <script>-tag
            builder.append(createTag(SCRIPT_TAG_TEMPLATE, indent, technology));
            // <public>-tag
            builder.append(createTag(PUBLIC_TAG_TEMPLATE, indent, technology));
            // <source>-tag
            builder.append(createTag(SOURCE_TAG_TEMPLATE, indent, technology));
        }
        builder.append(indent);
        String content = FileUtilities.loadToString(openBISGwtXmlFile);
        content =
                content.substring(0, content.indexOf(XML_MARKER_START)) + builder.toString()
                        + content.substring(content.indexOf(XML_MARKER_END), content.length());
        FileUtilities.writeToFile(openBISGwtXmlFile, content);
    }

    /**
     * Updates {@link DefaultClientPluginFactoryProvider} class up to the technologies found.
     */
    public final void updateClientPluginProvider()
    {
        final File clientPluginProviderJavaFile =
                new File(workingDirectory, CLIENT_PLUGIN_PROVIDER_CLASS.replace(".", "/") + ".java");
        final String response =
                FileUtilities.checkFileFullyAccessible(clientPluginProviderJavaFile, "java");
        if (response != null)
        {
            throw new RuntimeException(response);
        }
        final StringBuilder builder = new StringBuilder(JAVA_MARKER_START);
        builder.append("\n");
        final String indent = StringUtils.repeat(" ", 8);
        for (final String technology : technologies)
        {
            if (technology.equals("generic"))
            {
                continue;
            }
            try
            {
                Class.forName(String.format(PLUGIN_FACTORY_CLASS_NAME_TEMPLATE, technology));
            } catch (final ClassNotFoundException ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
            builder.append(indent);
            builder.append(String.format(PLUGIN_FACTORY_REGISTRATION_TEMPLATE, technology));
            builder.append("\n");
        }
        builder.append(indent);
        String content = FileUtilities.loadToString(clientPluginProviderJavaFile);
        content =
                content.substring(0, content.indexOf(JAVA_MARKER_START)) + builder.toString()
                        + content.substring(content.indexOf(JAVA_MARKER_END), content.length());
        FileUtilities.writeToFile(clientPluginProviderJavaFile, content);
    }

    //
    // Main method
    //

    public static void main(final String[] args)
    {
        final int len = args.length;
        final String workingDirectory;
        final String[] technologies;
        switch (len)
        {
            case 0:
                workingDirectory = null;
                technologies = null;
                break;
            default:
                workingDirectory = args[0];
                technologies = new String[len - 1];
                for (int i = 1; i < len; i++)
                {
                    technologies[i - 0] = args[i];
                }
                break;
        }
        final WebClientFilesUpdater webClientFilesUpdater =
                new WebClientFilesUpdater(workingDirectory, technologies);
        webClientFilesUpdater.updateOpenBISGwtXmlFile();
        System.out.println(String.format("'%s' has been updated.", OPENBIS_GWT_XML_FILE_NAME));
        webClientFilesUpdater.updateClientPluginProvider();
        System.out.println(String.format("'%s' has been updated.", CLIENT_PLUGIN_PROVIDER_CLASS));
    }
}
