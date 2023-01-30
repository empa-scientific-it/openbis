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

package ch.systemsx.cisd.openbis.generic.client.web.client.application;

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

import java.util.LinkedHashMap;
import java.util.Map;

import com.extjs.gxt.ui.client.widget.ContentPanel;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.PropertyValueRenderers;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property.PropertyGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.MultilineHTML;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;

/**
 * {@link ContentPanel} containing project properties.
 * 
 * @author Piotr Buczek
 */
public class ProjectPropertiesPanel extends ContentPanel
{
    public static final String PROPERTIES_ID_PREFIX = GenericConstants.ID_PREFIX
            + "project-properties-section_";

    private final Project project;

    private final IViewContext<?> viewContext;

    public ProjectPropertiesPanel(final Project project, final IViewContext<?> viewContext)
    {
        setHeading("Project Properties");
        this.project = project;
        this.viewContext = viewContext;
        final PropertyGrid propertyGrid = createPropertyGrid();
        add(propertyGrid);
    }

    private final PropertyGrid createPropertyGrid()
    {
        final Map<String, Object> properties = createProperties(viewContext);
        final PropertyGrid propertyGrid = new PropertyGrid(viewContext, properties.size());
        propertyGrid.getElement().setId(PROPERTIES_ID_PREFIX + project.getIdentifier());
        propertyGrid.registerPropertyValueRenderer(Person.class,
                PropertyValueRenderers.createPersonPropertyValueRenderer(viewContext));
        propertyGrid.setProperties(properties);
        return propertyGrid;
    }

    private final Map<String, Object> createProperties(final IMessageProvider messageProvider)
    {
        final Map<String, Object> properties = new LinkedHashMap<String, Object>();

        properties.put(messageProvider.getMessage(Dict.PROJECT), project.getIdentifier());
        properties.put(messageProvider.getMessage(Dict.PERM_ID), project.getPermId());
        properties.put(messageProvider.getMessage(Dict.REGISTRATOR), project.getRegistrator());
        properties.put(messageProvider.getMessage(Dict.REGISTRATION_DATE),
                project.getRegistrationDate());
        // show description in multiple lines (renderer would need to be assigned to String.class)
        final String description =
                project.getDescription() == null ? null : new MultilineHTML(
                        project.getDescription()).toString();
        properties.put(messageProvider.getMessage(Dict.DESCRIPTION), description);

        return properties;
    }
}
