/*
 * Copyright ETH 2012 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.server.coreplugin;

import static ch.systemsx.cisd.openbis.generic.server.coreplugin.CorePluginsInjectingPropertyPlaceholderConfigurer.PLUGIN_TYPE_WEBAPPS;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import ch.systemsx.cisd.common.io.Posix;
import org.apache.log4j.Logger;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.openbis.generic.shared.WebClientConfigurationProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CorePlugin;
import ch.systemsx.cisd.openbis.generic.shared.coreplugin.CorePluginScanner;
import ch.systemsx.cisd.openbis.generic.shared.coreplugin.CorePluginScanner.ScannerType;
import ch.systemsx.cisd.openbis.generic.shared.coreplugin.CorePluginsUtils;

/**
 * A class that injects web apps into jetty.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class JettyWebAppPluginInjector
{
    private static final String WEBAPP_FOLDER = "webapp";

    private static final String START_PAGE = "start-page";

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            JettyWebAppPluginInjector.class);

    private final List<String> webapps;

    private final Map<String, Properties> webappProperties;

    public static final String WEB_APP_FOLDER_PROPERTY = "webapp-folder";

    private Map<String, File> webappToFoldersMap;

    private static Map<String, Properties> extractWebappProperties(Properties props,
            List<String> webapps)
    {
        HashMap<String, Properties> map = new HashMap<String, Properties>();

        for (String webapp : webapps)
        {
            Properties webappProperties =
                    PropertyParametersUtil.extractSingleSectionProperties(props, webapp, false)
                            .getProperties();
            map.put(webapp, webappProperties);
        }
        return map;
    }

    public JettyWebAppPluginInjector(Properties props)
    {
        List<String> appList =
                PropertyUtils.tryGetListInOriginalCase(props, BasicConstant.WEB_APPS_PROPERTY);
        webapps = (null == appList) ? Collections.<String> emptyList() : appList;
        webappProperties = extractWebappProperties(props, webapps);
        webappToFoldersMap = new HashMap<String, File>();
        String corePluginsFolder = CorePluginsUtils.getCorePluginsFolder(props, ScannerType.AS);
        CorePluginScanner scanner = new CorePluginScanner(corePluginsFolder, ScannerType.AS);
        List<CorePlugin> plugins = scanner.scanForPlugins();
        for (CorePlugin plugin : plugins)
        {
            File webappsFolder =
                    new File(corePluginsFolder, CorePluginScanner.constructPath(plugin,
                            ScannerType.AS, PLUGIN_TYPE_WEBAPPS));
            if (webappsFolder.isDirectory())
            {
                File[] pluginFolders = webappsFolder.listFiles();
                for (File folder : pluginFolders)
                {
                    String webappName = folder.getName();
                    if (webappName.startsWith(".") == false)
                    {
                        Properties webappProps = webappProperties.get(webappName);
                        if (webappProps == null)
                        {
                            break;
                        }
                        String f = webappProps.getProperty(WEB_APP_FOLDER_PROPERTY);
                        webappToFoldersMap.put(webappName, new File(f));
                    }
                }
            }
        }
    }

    public void injectWebApps()
    {
        List<String> remainingWebapps = replaceDefaultStartPageByWebApp();
        // Leave if there is nothing to do
        if (remainingWebapps.isEmpty())
        {
            return;
        }
        operationLog.info("Inject the following web apps: " + remainingWebapps);
        List<File> targets = WebClientConfigurationProvider.findInjectionTargets();
        for (File target : targets)
        {
            File webappFolder = new File(target, WEBAPP_FOLDER);
            FileUtilities.deleteRecursively(webappFolder);
        }
        for (String webapp : remainingWebapps)
        {
            File folder = webappToFoldersMap.get(webapp);
            if (folder == null)
            {
                continue;
            }
            String path = folder.getAbsolutePath();
            for (File target : targets)
            {
                File webappFolder = new File(target, WEBAPP_FOLDER);
                webappFolder.mkdirs();
                File link = new File(webappFolder, webapp);
                if (link.exists())
                {
                    try
                    {
                        if (!folder.getCanonicalPath().equals(link.getCanonicalPath())) // Verifies they are pointing to the same version
                        {
                            link.delete();
                        }
                    } catch (Exception e)
                    {
                        operationLog.error("WebApp '" + webapp + "': Problem managing Symbolic link ", e);
                    }
                }
                if (link.exists() == false)
                {
                    String linkPath = link.getAbsolutePath();
                    Posix.createSymbolicLink(path, linkPath);
                    operationLog.info("WebApp '" + webapp + "': Symbolic link " + linkPath + " -> " + path);
                }
            }
        }
        return;
    }

    private List<String> replaceDefaultStartPageByWebApp()
    {
        List<String> remainingWebApps = new ArrayList<String>(webapps);
        if (remainingWebApps.contains(START_PAGE) == false)
        {
            return remainingWebApps;
        }
        remainingWebApps.remove(START_PAGE);
        operationLog.info("Replace default start page by web app '" + START_PAGE + "'.");
        File webappFolder = webappToFoldersMap.get(START_PAGE);
        if (webappFolder != null)
        {
            for (File target : WebClientConfigurationProvider.findInjectionTargets())
            {
                target.mkdirs();
                File folderCustom = new File(target, "custom");
                if (folderCustom.exists())
                {
                    boolean success = FileUtilities.deleteRecursively(folderCustom);
                    if (success)
                    {
                        operationLog.info(folderCustom.getAbsoluteFile() + " deleted.");
                    } else
                    {
                        operationLog.error("Couldn't delete folder " + folderCustom.getAbsolutePath() + ".");
                    }
                }
                Posix.createSymbolicLink(webappFolder.getAbsolutePath(), folderCustom.getAbsolutePath());
                operationLog.info("Symbolic link: " + folderCustom.getAbsolutePath() + " -> " + webappFolder.getAbsolutePath());
                File webappIndexFile = new File(webappFolder, "index.html");
                if (webappIndexFile.exists())
                {
                    File indexFile = new File(target, "index.html");
                    if (indexFile.exists())
                    {
                        boolean success = FileUtilities.delete(indexFile);
                        if (success)
                        {
                            operationLog.info(indexFile.getAbsolutePath() + " deleted.");
                        } else
                        {
                            operationLog.error("Couldn't delete file " + indexFile.getAbsolutePath() + ".");
                        }
                    }
                    Posix.createSymbolicLink(webappIndexFile.getAbsolutePath(), indexFile.getAbsolutePath());
                    operationLog.info("Symbolic link: " + indexFile.getAbsolutePath() + " -> " + webappIndexFile.getAbsolutePath());
                }
            }
        }
        return remainingWebApps;
    }

}
