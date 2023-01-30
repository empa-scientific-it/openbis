/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.PropertyValueRenderers;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GenericEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ManagedEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermEntityProperty;

/**
 * A <code>HTMLTable</code> that displays a couple of properties.
 * 
 * @author Christian Ribeaud
 */
public final class PropertyGrid extends Grid
{
    private final IPropertyValueRenderer<Object> defaultPropertyValueRenderer;

    private final Map<Class<?>, IPropertyValueRenderer<?>> propertyValueRenderers =
            new HashMap<Class<?>, IPropertyValueRenderer<?>>();

    private Map<String, Object> properties;

    private final IViewContext<?> viewContext;

    private final IMessageProvider messageProvider;

    public PropertyGrid(final IViewContext<?> viewContext, final int rows)
    {
        this(viewContext, rows, "30%");
    }

    public PropertyGrid(final IViewContext<?> viewContext, final int rows, String widthOfFirstColumn)
    {
        super(rows, 2);
        this.messageProvider = viewContext;
        this.viewContext = viewContext;
        setStyleName("property-grid");
        getColumnFormatter().addStyleName(0, "header");
        getColumnFormatter().setWidth(0, widthOfFirstColumn);
        defaultPropertyValueRenderer = new ObjectPropertyValueRenderer(messageProvider);
        registerDefaultPropertyValueRenderers();
    }

    /** Registers default <code>PropertyValueRenderer</code>. */
    private final void registerDefaultPropertyValueRenderers()
    {
        registerPropertyValueRenderer(Date.class, new DatePropertyValueRenderer(messageProvider,
                BasicConstant.DATE_HOURS_MINUTES_SECONDS_PATTERN));
        // Common
        registerPropertyValueRenderer(Person.class,
                PropertyValueRenderers.createPersonPropertyValueRenderer(messageProvider));

        // Entities
        registerPropertyValueRenderer(Deletion.class,
                PropertyValueRenderers.createDeletionPropertyValueRenderer(messageProvider));

        registerPropertyValueRenderer(ContainerDataSet.class,
                PropertyValueRenderers.createExternalDataPropertyValueRenderer(viewContext));
        registerPropertyValueRenderer(PhysicalDataSet.class,
                PropertyValueRenderers.createExternalDataPropertyValueRenderer(viewContext));
        registerPropertyValueRenderer(Experiment.class,
                PropertyValueRenderers.createExperimentPropertyValueRenderer(viewContext));
        registerPropertyValueRenderer(Sample.class,
                PropertyValueRenderers.createSamplePropertyValueRenderer(viewContext, true));
        registerPropertyValueRenderer(Project.class,
                PropertyValueRenderers.createProjectPropertyValueRenderer(viewContext));

        // Properties
        final IPropertyValueRenderer<IEntityProperty> propertyValueRenderer =
                PropertyValueRenderers.createEntityPropertyPropertyValueRenderer(viewContext);
        registerPropertyValueRenderer(EntityProperty.class, propertyValueRenderer);
        registerPropertyValueRenderer(GenericEntityProperty.class, propertyValueRenderer);
        registerPropertyValueRenderer(ManagedEntityProperty.class, propertyValueRenderer);
        registerPropertyValueRenderer(MaterialEntityProperty.class, propertyValueRenderer);
        registerPropertyValueRenderer(SampleEntityProperty.class, propertyValueRenderer);
        registerPropertyValueRenderer(VocabularyTermEntityProperty.class, propertyValueRenderer);
    }

    /**
     * For given <var>className</var> returns the corresponding <code>PropertyValueRenderer</code> or {@link #defaultPropertyValueRenderer} if:
     * <ul>
     * <li>none is found</li>
     * <li>given <var>clazz</var> is <code>null</code></li>
     * </ul>
     */
    @SuppressWarnings(
    { "unchecked", "rawtypes" })
    private final <T> IPropertyValueRenderer<? super T> getPropertyValueRenderer(final T value)
    {
        if (value == null)
        {
            return defaultPropertyValueRenderer;
        }
        final Class<T> clazz = (Class<T>) value.getClass();
        final IPropertyValueRenderer<T> renderer =
                (IPropertyValueRenderer<T>) propertyValueRenderers.get(clazz);
        if (renderer == null)
        {
            if (clazz.isArray())
            {
                final T[] array = (T[]) value;
                if (array.length > 0)
                {
                    return new ObjectArrayPropertyValueRenderer(messageProvider,
                            getPropertyValueRenderer(array[0]));
                } else
                {
                    return new ObjectArrayPropertyValueRenderer(messageProvider,
                            defaultPropertyValueRenderer);
                }
            } else
            {
                return defaultPropertyValueRenderer;
            }
        }
        return renderer;
    }

    /** For given <var>clazz</var> registers given <var>propertyValueRenderer</var>. */
    public final <V, T extends V> void registerPropertyValueRenderer(final Class<T> clazz,
            final IPropertyValueRenderer<V> propertyValueRenderer)
    {
        propertyValueRenderers.put(clazz, propertyValueRenderer);
    }

    /** Unregisters <code>PropertyValueRenderer</code> for given <var>clazz</var>. */
    public final <T> void unregisterPropertyValueRenderer(final Class<T> clazz)
    {
        propertyValueRenderers.remove(clazz);
    }

    /**
     * Sets the properties that are going to be displayed here.
     */
    public final void setProperties(final Map<String, Object> properties)
    {
        this.properties = properties;
        assert properties != null : "Unspecified properties.";
        resizeRows(properties.size());
        addPropertiesToTable(properties, 0);
    }

    public Map<String, Object> getProperties()
    {
        return properties;
    }

    private final <T> Widget getAsWidget(final T value)
    {
        final IPropertyValueRenderer<? super T> propertyValueRenderer =
                getPropertyValueRenderer(value);
        return propertyValueRenderer.getAsWidget(value);
    }

    public void addAdditionalProperties(String title, Map<String, Object> props)
    {
        int row = getRowCount();
        resizeRows(row + 1 + props.size());
        setHTML(row, 0, title);
        Element element = getCellFormatter().getElement(row, 0);
        element.setAttribute("colspan", "2");
        element.setAttribute("class", "properties-sub-section");
        getCellFormatter().getElement(row, 1).setAttribute("style", "display: none");
        addPropertiesToTable(props, row + 1);
    }

    private void addPropertiesToTable(Map<String, Object> props, int initRow)
    {
        int row = initRow;
        for (Entry<String, Object> entry : props.entrySet())
        {
            String key = entry.getKey();
            Object value = entry.getValue();
            Widget widget = getAsWidget(value);
            setHTML(row, 0, key);
            setWidget(row++, 1, widget);
        }
    }

}
