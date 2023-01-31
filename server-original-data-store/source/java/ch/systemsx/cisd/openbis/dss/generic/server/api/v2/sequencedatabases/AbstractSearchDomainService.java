/*
 * Copyright 2014 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.dss.generic.server.api.v2.sequencedatabases;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.PluginTaskFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.ISearchDomainService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchDomainSearchOption;

/**
 * Abstract super class of all {@link ISearchDomainService} implementations which get the label from the property
 * {@link PluginTaskFactory#LABEL_PROPERTY_NAME}.
 *
 * @author Franz-Josef Elmer
 */
public abstract class AbstractSearchDomainService implements ISearchDomainService
{
    private final String label;

    protected String name;

    protected AbstractSearchDomainService(Properties properties, File storeRoot)
    {
        label = properties.getProperty(PluginTaskFactory.LABEL_PROPERTY_NAME);
    }

    @Override
    public String getLabel()
    {
        return label;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public String getPossibleSearchOptionsKey()
    {
        return null;
    }

    @Override
    public List<SearchDomainSearchOption> getPossibleSearchOptions()
    {
        return Collections.emptyList();
    }
    

}
