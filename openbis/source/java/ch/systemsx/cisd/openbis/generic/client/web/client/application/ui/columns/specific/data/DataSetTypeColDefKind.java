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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.data;

import static ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.EntityTypeColDefKindFactory.dataSetTypeColDefKindFactory;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.AbstractColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;

/**
 * Columns definition for browsing grid of {@link DataSetType}s.
 * 
 * @author Piotr Buczek
 */
public enum DataSetTypeColDefKind implements IColumnDefinitionKind<DataSetType>
{
    // copy from EntityTypeColDefKind (cannot extend an enum)

    CODE(dataSetTypeColDefKindFactory.createCodeColDefKind()),

    DESCRIPTION(dataSetTypeColDefKindFactory.createDescriptionColDefKind()),

    MAIN_DATA_SET_PATH(new AbstractColumnDefinitionKind<DataSetType>(Dict.MAIN_DATA_SET_PATH, true)
        {
            @Override
            public String tryGetValue(DataSetType entity)
            {
                return entity.getMainDataSetPath();
            }
        }),

    MAIN_DATA_SET_PATTERN(new AbstractColumnDefinitionKind<DataSetType>(Dict.MAIN_DATA_SET_PATTERN,
            true)
        {
            @Override
            public String tryGetValue(DataSetType entity)
            {
                return entity.getMainDataSetPattern();
            }
        }),

    DATABASE_INSTANCE(dataSetTypeColDefKindFactory.createDatabaseInstanceColDefKind());

    // no specific Sample Type columns

    private final AbstractColumnDefinitionKind<DataSetType> columnDefinitionKind;

    private DataSetTypeColDefKind(AbstractColumnDefinitionKind<DataSetType> columnDefinitionKind)
    {
        this.columnDefinitionKind = columnDefinitionKind;
    }

    public String id()
    {
        return name();
    }

    public AbstractColumnDefinitionKind<DataSetType> getDescriptor()
    {
        return columnDefinitionKind;
    }

}
