/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.ftp;

import java.util.Properties;

/**
 * A builder for {@link FtpServerConfig} objects.
 * 
 * @author Kaloyan Enimanev
 */
public class ResolverConfigBuilder
{
    private Properties props = new Properties();

    public FtpPathResolverConfig getConfig()
    {
        return new FtpPathResolverConfig(props);
    }

    public ResolverConfigBuilder()
    {
    }

    public ResolverConfigBuilder showParentsAndChildren()
    {
        props.setProperty(FtpPathResolverConfig.SHOW_PARENTS_AND_CHILDREN_KEY, Boolean.TRUE.toString());
        return this;

    }

    public ResolverConfigBuilder withTemplate(String template)
    {
        props.setProperty(FtpPathResolverConfig.DATASET_DISPLAY_TEMPLATE_KEY, template);
        return this;

    }

    public ResolverConfigBuilder withFileListFilter(String dataSetType, String filterPattern)
    {
        String key = FtpPathResolverConfig.DATASET_FILELIST_FILTER_KEY + dataSetType;
        props.setProperty(key, filterPattern);
        return this;
    }

    public ResolverConfigBuilder withFileListSubPath(String dataSetType, String subPathPattern)
    {
        String key = FtpPathResolverConfig.DATASET_FILELIST_SUBPATH_KEY + dataSetType;
        props.setProperty(key, subPathPattern);
        return this;
    }
}
