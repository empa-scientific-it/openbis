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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.metaproject.viewer;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property.PropertyGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MetaprojectAssignmentsCount;

/**
 * @author pkupczyk
 */
public class MetaprojectPropertiesPanel extends ContentPanel implements IDisposableComponent
{
    public static final String PROPERTIES_ID_PREFIX = GenericConstants.ID_PREFIX
            + "metaproject-properties-section_";

    private IViewContext<?> viewContext;

    private Long metaprojectId;

    private PropertyGrid propertyGrid;

    public MetaprojectPropertiesPanel(IViewContext<?> viewContext, Long metaprojectId)
    {
        super(new BorderLayout());

        this.viewContext = viewContext;
        this.metaprojectId = metaprojectId;

        setHeading(viewContext.getMessage(Dict.METAPROJECT_PROPERTIES));

        load();
    }

    private void load()
    {
        viewContext.getCommonService().getMetaprojectAssignmentsCount(metaprojectId,
                new AbstractAsyncCallback<MetaprojectAssignmentsCount>(viewContext)
                    {
                        @Override
                        protected void process(MetaprojectAssignmentsCount count)
                        {
                            Map<String, Object> properties = createProperties(count);

                            if (propertyGrid == null)
                            {
                                createPropertyGrid(properties);
                            } else
                            {
                                updatePropertyGrid(properties);
                            }
                        }

                    });
    }

    private Map<String, Object> createProperties(MetaprojectAssignmentsCount count)
    {
        Metaproject metaproject = count.getMetaproject();
        Map<String, Object> properties = new LinkedHashMap<String, Object>();
        properties.put(viewContext.getMessage(Dict.METAPROJECT), metaproject.getIdentifier());
        properties.put(viewContext.getMessage(Dict.NAME), metaproject.getName());
        properties.put(viewContext.getMessage(Dict.DESCRIPTION), metaproject.getDescription());
        if (count.getExperimentCount() > 0)
        {
            properties.put(viewContext.getMessage(Dict.METAPROJECT_ENTITIES_EXPERIMENTS),
                    count.getExperimentCount());
        }
        if (count.getSampleCount() > 0)
        {
            properties.put(viewContext.getMessage(Dict.METAPROJECT_ENTITIES_SAMPLES),
                    count.getSampleCount());
        }
        if (count.getDataSetCount() > 0)
        {
            properties.put(viewContext.getMessage(Dict.METAPROJECT_ENTITIES_DATA_SETS),
                    count.getDataSetCount());
        }
        if (count.getMaterialCount() > 0)
        {
            properties.put(viewContext.getMessage(Dict.METAPROJECT_ENTITIES_MATERIALS),
                    count.getMaterialCount());
        }
        properties.put(viewContext.getMessage(Dict.CREATION_DATE), metaproject.getCreationDate());
        return properties;
    }

    private void createPropertyGrid(Map<String, Object> properties)
    {
        propertyGrid = new PropertyGrid(viewContext, properties.size());
        propertyGrid.getElement().setId(PROPERTIES_ID_PREFIX + metaprojectId);
        propertyGrid.setProperties(properties);

        ContentPanel panel = new ContentPanel();
        panel.setHeaderVisible(false);
        panel.setScrollMode(Scroll.AUTOY);
        panel.add(propertyGrid);
        add(panel, new BorderLayoutData(LayoutRegion.CENTER));

        layout();
    }

    private void updatePropertyGrid(Map<String, Object> properties)
    {
        propertyGrid.resizeRows(properties.size());
        propertyGrid.setProperties(properties);
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        Set<DatabaseModificationKind> result = new HashSet<DatabaseModificationKind>();
        DatabaseModificationKind.addAny(result, ObjectKind.METAPROJECT);
        DatabaseModificationKind.addAny(result, ObjectKind.EXPERIMENT);
        DatabaseModificationKind.addAny(result, ObjectKind.SAMPLE);
        DatabaseModificationKind.addAny(result, ObjectKind.DATA_SET);
        DatabaseModificationKind.addAny(result, ObjectKind.MATERIAL);
        return result.toArray(DatabaseModificationKind.EMPTY_ARRAY);
    }

    @Override
    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        load();
    }

    @Override
    public Component getComponent()
    {
        return this;
    }

    @Override
    public void dispose()
    {

    }

}
