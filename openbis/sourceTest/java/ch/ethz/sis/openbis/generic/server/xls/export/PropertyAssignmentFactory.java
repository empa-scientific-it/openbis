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
