/*
 * Copyright ETH 2009 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ICriteriaProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.IDataRefreshCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * The provider which is able to load and reload property types. When property types are loaded the specified callback is executed.<br>
 */
public class PropertyTypesCriteriaProvider implements ICriteriaProvider<PropertyTypesCriteria>
{
    private final IViewContext<?> viewContext;

    private final PropertyTypesCriteria criteria;

    // if not null filters only property types assigned to specified entity
    private final EntityKind propertiesFilterOrNull;

    public PropertyTypesCriteriaProvider(IViewContext<?> viewContext,
            EntityKind propertiesFilterOrNull)
    {
        this.viewContext = viewContext;
        this.criteria = new PropertyTypesCriteria();
        this.propertiesFilterOrNull = propertiesFilterOrNull;
    }

    private void loadPropertyTypes(IDataRefreshCallback dataRefreshCallback)
    {
        DefaultResultSetConfig<String, TableModelRowWithObject<PropertyType>> config =
                DefaultResultSetConfig.createFetchAll();
        viewContext.getCommonService().listPropertyTypes(config,
                new ListPropertyTypesCallback(viewContext, dataRefreshCallback));
    }

    private class ListPropertyTypesCallback extends AbstractAsyncCallback<TypedTableResultSet<PropertyType>>
    {
        private final IDataRefreshCallback dataRefreshCallback;

        public ListPropertyTypesCallback(IViewContext<?> viewContext,
                IDataRefreshCallback dataRefreshCallback)
        {
            super(viewContext);
            this.dataRefreshCallback = dataRefreshCallback;
        }

        @Override
        protected void process(TypedTableResultSet<PropertyType> result)
        {
            List<TableModelRowWithObject<PropertyType>> rows =
                    result.getResultSet().getList().extractOriginalObjects();
            List<PropertyType> propertyTypes = new ArrayList<PropertyType>();
            for (TableModelRowWithObject<PropertyType> row : rows)
            {
                propertyTypes.add(row.getObjectOrNull());
            }
            if (propertiesFilterOrNull != null)
            {
                propertyTypes =
                        PropertyTypesFilterUtil.filterPropertyTypesForEntityKind(propertyTypes,
                                propertiesFilterOrNull);
            }
            criteria.setPropertyTypes(propertyTypes);
            dataRefreshCallback.postRefresh(true);
        }
    }

    @Override
    public PropertyTypesCriteria tryGetCriteria()
    {
        if (criteria.tryGetPropertyTypes() == null)
        {
            return null;
        } else
        {
            return criteria;
        }
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return DatabaseModificationKind.any(ObjectKind.PROPERTY_TYPE_ASSIGNMENT);
    }

    @Override
    public void update(Set<DatabaseModificationKind> observedModifications,
            IDataRefreshCallback dataRefreshCallback)
    {
        loadPropertyTypes(dataRefreshCallback);
    }
}
