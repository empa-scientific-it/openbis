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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui;

import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;

/**
 * A <i>static</i> class for creating {@link ColumnConfig}.
 * 
 * @author Christian Ribeaud
 */
public final class ColumnConfigFactory
{
    public static final int DEFAULT_COLUMN_WIDTH = 100;

    private ColumnConfigFactory()
    {
        // Can not be instantiated.
    }

    public final static ColumnConfig createRegistratorColumnConfig(
            final IMessageProvider messageProvider)
    {
        return createRegistratorColumnConfig(messageProvider, ModelDataPropertyNames.REGISTRATOR);
    }

    private final static ColumnConfig createRegistratorColumnConfig(
            final IMessageProvider messageProvider, String id)
    {
        return createDefaultColumnConfig(messageProvider.getMessage(Dict.REGISTRATOR), id);
    }

    public final static ColumnConfig createRegistrationDateColumnConfig(
            final IMessageProvider messageProvider)
    {
        return createRegistrationDateColumnConfig(messageProvider,
                ModelDataPropertyNames.REGISTRATION_DATE);
    }

    private final static ColumnConfig createRegistrationDateColumnConfig(
            final IMessageProvider messageProvider, String id)
    {
        final ColumnConfig registrationDateColumnConfig =
                createDefaultColumnConfig(messageProvider.getMessage(Dict.REGISTRATION_DATE), id);
        registrationDateColumnConfig.setWidth(200);
        return registrationDateColumnConfig;
    }

    //
    // Default ColumnConfig
    //

    private final static ColumnConfig createDefaultColumnConfig(final String id)
    {
        final ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setId(id);
        columnConfig.setSortable(true);
        columnConfig.setHidden(false);
        columnConfig.setMenuDisabled(false);
        columnConfig.setResizable(true);
        return columnConfig;
    }

    public final static ColumnConfig createDefaultColumnConfig(final String header, final String id)
    {
        final ColumnConfig columnConfig = createDefaultColumnConfig(id);
        columnConfig.setHeader(header);
        columnConfig.setWidth(DEFAULT_COLUMN_WIDTH);
        return columnConfig;
    }

}
