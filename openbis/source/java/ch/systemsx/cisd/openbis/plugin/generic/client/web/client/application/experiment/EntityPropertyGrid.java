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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.PropertyValueRenderers;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property.IPropertyValueRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property.PropertyGrid;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GenericEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ManagedEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermEntityProperty;

/**
 * @author Izabela Adamczyk
 */
public class EntityPropertyGrid<T extends EntityType, S extends EntityTypePropertyType<T>, P extends IEntityProperty>
{
    private PropertyGrid grid;

    private IViewContext<?> viewContext;

    private List<P> properties;

    public EntityPropertyGrid(IViewContext<?> viewContext, List<P> initialProperties)
    {
        assert initialProperties != null : "Initial properties undefined";
        this.viewContext = viewContext;
        this.properties = initialProperties;
        grid = new PropertyGrid(viewContext, properties.size());
        registerRenderers();
        setProperties(properties);
    }

    private void registerRenderers()
    {
        final IPropertyValueRenderer<IEntityProperty> renderer =
                PropertyValueRenderers.createEntityPropertyPropertyValueRenderer(viewContext);
        grid.registerPropertyValueRenderer(EntityProperty.class, renderer);
        grid.registerPropertyValueRenderer(GenericEntityProperty.class, renderer);
        grid.registerPropertyValueRenderer(VocabularyTermEntityProperty.class, renderer);
        grid.registerPropertyValueRenderer(MaterialEntityProperty.class, renderer);
        grid.registerPropertyValueRenderer(ManagedEntityProperty.class, renderer);
    }

    private final Map<String, Object> createProperties(List<P> list)
    {
        final Map<String, Object> map = new LinkedHashMap<String, Object>();
        for (final P property : list)
        {
            final String simpleCode = property.getPropertyType().getLabel();
            map.put(simpleCode, property);
        }
        return map;
    }

    /**
     * Updates the grid with new list of properties.
     */
    public void setProperties(List<P> list)
    {
        properties = list;
        grid.resizeRows(properties.size());
        grid.setProperties(createProperties(properties));
    }

    /**
     * Returns currently used properties.
     */
    public List<P> getProperties()
    {
        return properties;
    }

    public PropertyGrid getWidget()
    {
        return grid;
    }

}
