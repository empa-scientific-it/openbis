/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.shared.utils;

import java.io.File;
import java.util.Properties;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IPluginTaskInfoProvider;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.PluginTaskInfoProvider;

/**
 * @author Tomasz Pylak
 */
@Friend(toClasses = { DssPropertyParametersUtil.class, PluginTaskInfoProvider.class })
public class PluginUtilTest
{
    public static IPluginTaskInfoProvider createPluginTaskProviders(File storeRoot)
    {
        Properties serviceProperties = new Properties();
        serviceProperties.put(DssPropertyParametersUtil.DSS_CODE_KEY, "dss");
        return new PluginTaskInfoProvider(serviceProperties, null, storeRoot, new File("workspace"));
    }
}
