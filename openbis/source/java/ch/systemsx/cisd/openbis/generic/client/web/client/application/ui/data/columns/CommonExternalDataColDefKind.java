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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.columns;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalData;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public enum CommonExternalDataColDefKind implements IColumnDefinitionKind<ExternalData>
{
    CODE(new AbstractColumnDefinitionKind<ExternalData>(ModelDataPropertyNames.CODE, Dict.CODE)
        {
            public String tryGetValue(ExternalData entity)
            {
                return entity.getCode();
            }
        }),

    REGISTRATION_DATE(new AbstractColumnDefinitionKind<ExternalData>(
            ModelDataPropertyNames.REGISTRATION_DATE, Dict.REGISTRATION_DATE, 200, false)
        {
            public String tryGetValue(ExternalData entity)
            {
                return renderRegistrationDate(entity);
            }
        }),

    REGISTRATOR(new AbstractColumnDefinitionKind<ExternalData>(ModelDataPropertyNames.REGISTRATOR,
            Dict.REGISTRATOR)
        {
            public String tryGetValue(ExternalData entity)
            {
                return renderRegistrator(entity);
            }
        }),

    LOCATION(new AbstractColumnDefinitionKind<ExternalData>(ModelDataPropertyNames.LOCATION,
            Dict.LOCATION)
        {
            public String tryGetValue(ExternalData entity)
            {
                return entity.getLocation();
            }
        }),

    FILE_FORMAT_TYPE(new AbstractColumnDefinitionKind<ExternalData>(
            ModelDataPropertyNames.FILE_FORMAT_TYPE, Dict.FILE_FORMAT_TYPE)
        {
            public String tryGetValue(ExternalData entity)
            {
                return entity.getFileFormatType().getCode();
            }
        });

    private final IColumnDefinitionKind<ExternalData> columnDefinitionKind;

    private CommonExternalDataColDefKind(IColumnDefinitionKind<ExternalData> columnDefinitionKind)
    {
        this.columnDefinitionKind = columnDefinitionKind;
    }

    public String getHeaderMsgKey()
    {
        return columnDefinitionKind.getHeaderMsgKey();
    }

    public int getWidth()
    {
        return columnDefinitionKind.getWidth();
    }

    public String id()
    {
        return columnDefinitionKind.id();
    }

    public boolean isHidden()
    {
        return columnDefinitionKind.isHidden();
    }

    public String tryGetValue(ExternalData entity)
    {
        return columnDefinitionKind.tryGetValue(entity);
    }

}
