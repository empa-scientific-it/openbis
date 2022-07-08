package ch.ethz.sis.openbis.generic.server.xls.export.helper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Workbook;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPropertyAssignmentsHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.IPropertyTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.Vocabulary;

public class XLSPropertyTypeExportHelper extends AbstractXLSExportHelper
{

    @Override
    public int add(final IApplicationServerApi api, final String sessionToken, final Workbook wb,
            final Collection<String> permIds, int rowNumber)
    {
        final PropertyTypeFetchOptions fetchOptions = new PropertyTypeFetchOptions();
        fetchOptions.withVocabulary();

        final List<PropertyTypePermId> propertyTypePermIds = permIds.stream().map(PropertyTypePermId::new)
                .collect(Collectors.toList());
        final Collection<PropertyType> propertyTypes = api.getPropertyTypes(sessionToken,
                propertyTypePermIds, fetchOptions).values();

        addRow(wb, rowNumber++, true, "PROPERTY_TYPE");
        addRow(wb, rowNumber++, true, "Version", "Code", "Mandatory", "Show in edit views", "Section",
                "Property label", "Data type", "Vocabulary code", "Description", "Metadata", "Dynamic script");

        for (final PropertyType propertyType : propertyTypes)
        {
            final Vocabulary vocabulary = propertyType.getVocabulary();
            addRow(wb, rowNumber++, false, "1", propertyType.getCode(),
//                        String.valueOf(propertyAssignment.isMandatory()).toUpperCase(),
//                        String.valueOf(propertyAssignment.isShowInEditView()).toUpperCase(),
//                        propertyAssignment.getSection(),
                    // TODO: where to take this information from?
                    "FALSE", "FALSE", "", propertyType.getLabel(), String.valueOf(propertyType.getDataType()),
                    String.valueOf(vocabulary != null ? vocabulary.getCode() : ""), propertyType.getDescription(),
                    mapToJSON(propertyType.getMetaData()),
                    // TODO: where to take this information from?
//                        plugin != null ? (plugin.getScript() != null ? plugin.getScript() : "") : ""
                    ""
            );
        }
        return rowNumber + 1;
    }

    @Override
    public IPropertyAssignmentsHolder getPropertyAssignmentsHolder(final IApplicationServerApi api,
            final String sessionToken, final String permId)
    {
        return null;
    }
}
