/*
 * Copyright ETH 2022 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.xls.export;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.Plugin;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.fetchoptions.PluginFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyTypeFetchOptions;

class PropertyAssignmentFactory
{

    private PropertyAssignmentFactory()
    {
        throw new UnsupportedOperationException();
    }

    static PropertyAssignment createPropertyAssignment(
            final PropertyAssignmentFetchOptions propertyAssignmentFetchOptions)
    {
        final PropertyTypeFetchOptions propertyTypeFetchOptions =
                propertyAssignmentFetchOptions.withPropertyType();
        propertyTypeFetchOptions.withVocabulary();

        final PluginFetchOptions pluginFetchOptions = propertyAssignmentFetchOptions.withPlugin();
        pluginFetchOptions.withScript();

        final PropertyAssignment propertyAssignment = new PropertyAssignment();
        propertyAssignment.setFetchOptions(propertyAssignmentFetchOptions);
        propertyAssignment.setPropertyType(new PropertyType());
        propertyAssignment.getPropertyType().setFetchOptions(propertyTypeFetchOptions);
        propertyAssignment.setPlugin(new Plugin());
        propertyAssignment.getPlugin().setFetchOptions(pluginFetchOptions);
        return propertyAssignment;
    }

}
