package ch.ethz.sis.openbis.generic.server.xls.export.helper;

import org.apache.poi.ss.usermodel.Workbook;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPropertyAssignmentsHolder;

public class XLSPropertyTypeExportHelper extends AbstractXLSExportHelper
{

    @Override
    public int add(final IApplicationServerApi api, final String sessionToken, final Workbook wb,
            final String permId, int rowNumber)
    {
        // TODO: this is a special case, should be treated separately.
        return 0;
//        final PropertyTypeFetchOptions fetchOptions = new PropertyTypeFetchOptions();
//        final Map<IPropertyTypeId, PropertyType> propertyTypes = api.getPropertyTypes(sessionToken,
//                Collections.singletonList(new PropertyTypePermId(permId)), fetchOptions);
//
//        assert propertyTypes.size() <= 1;
//
//        if (propertyTypes.size() > 0)
//        {
//            final PropertyType propertyType = propertyTypes.values().iterator().next();
//
//            addRow(wb, rowNumber++, true, "PROPERTY_TYPE");
//
//            final Plugin validationPlugin = propertyType.getValidationPlugin();
//            final String script = validationPlugin != null
//                    ? (validationPlugin.getScript() != null ? validationPlugin.getScript() : "") : "";
//
//            int rowNumber1 = rowNumber;
//            addRow(wb, rowNumber1++, true, ENTITY_ASSIGNMENT_COLUMNS);
//            for (final PropertyAssignment propertyAssignment : propertyType.getPropertyAssignments())
//            {
//                final PropertyType propertyType1 = propertyAssignment.getPropertyType();
//                final Plugin plugin = propertyAssignment.getPlugin();
//                final Vocabulary vocabulary = propertyType1.getVocabulary();
//                addRow(wb, rowNumber1++, false, "1", propertyType1.getCode(),
//                        String.valueOf(propertyAssignment.isMandatory()).toUpperCase(),
//                        String.valueOf(propertyAssignment.isShowInEditView()).toUpperCase(),
//                        propertyAssignment.getSection(),
//                        propertyType1.getLabel(), String.valueOf(propertyType1.getDataType()),
//                        String.valueOf(vocabulary != null ? vocabulary.getCode() : ""), propertyType1.getDescription(),
//                        mapToJSON(propertyType1.getMetaData()),
//                        plugin != null ? (plugin.getScript() != null ? plugin.getScript() : "") : "");
//            }
//            rowNumber = rowNumber1;
//            return rowNumber + 1;
//        } else
//        {
//            return rowNumber;
//        }
    }

    @Override
    public IPropertyAssignmentsHolder getPropertyAssignmentsHolder(final IApplicationServerApi api,
            final String sessionToken, final String permId)
    {
        // TODO: implement.
        return null;
    }
}
