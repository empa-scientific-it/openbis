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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.common.shared.basic.string.IgnoreCaseComparator;
import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.PropertyTypeRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.EntityPropertyUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;

/**
 * @author Piotr Buczek
 */
public class PropertiesPanelUtils
{

    public static void addEntityProperties(final IViewContext<?> viewContext,
            final Map<String, Object> properties, final List<IEntityProperty> entityProperties)
    {
        Collections.sort(entityProperties);
        List<PropertyType> types = EntityPropertyUtils.extractTypes(entityProperties);
        boolean isDebuggingEnabled =
                viewContext.getDisplaySettingsManager().isDebuggingModeEnabled();
        for (final IEntityProperty property : entityProperties)
        {
            if (property.isManaged() == false || isDebuggingEnabled)
            {
                final String label =
                        PropertyTypeRenderer.getDisplayName(property.getPropertyType(), types);
                properties.put(label, property);
            }
        }
    }

    public static void addMetaprojects(final IViewContext<?> viewContext,
            final Map<String, Object> properties, final Collection<Metaproject> metaprojects)
    {
        if (metaprojects != null && metaprojects.size() > 0)
        {
            String[] names = new String[metaprojects.size()];
            int index = 0;

            for (Metaproject metaproject : metaprojects)
            {
                names[index++] = metaproject.getName();
            }

            Arrays.sort(names, new IgnoreCaseComparator());

            properties
                    .put(viewContext.getMessage(Dict.METAPROJECTS), StringUtils.join(names, ", "));
        }

    }
}
