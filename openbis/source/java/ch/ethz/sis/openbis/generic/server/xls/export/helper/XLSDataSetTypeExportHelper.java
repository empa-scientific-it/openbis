package ch.ethz.sis.openbis.generic.server.xls.export.helper;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.apache.poi.ss.usermodel.Workbook;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IEntityType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.Plugin;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyAssignmentFetchOptions;

public class XLSDataSetTypeExportHelper extends AbstractXLSExportHelper
{

    @Override
    public int add(final IApplicationServerApi api, final String sessionToken, final Workbook wb,
            final Collection<String> permIds, int rowNumber)
    {
        assert permIds.size() == 1;
        final DataSetType dataSetType = getDataSetType(api, sessionToken, permIds.iterator().next());

        if (dataSetType != null)
        {

            addRow(wb, rowNumber++, true, "DATA_SET_TYPE");
            addRow(wb, rowNumber++, true, "Version", "Code", "Validation script");

            final Plugin validationPlugin = dataSetType.getValidationPlugin();
            final String script = validationPlugin != null
                    ? (validationPlugin.getName() != null ? validationPlugin.getName() + ".py" : "") : "";

            addRow(wb, rowNumber++, false, "1", dataSetType.getCode(), script);

            rowNumber = addEntityTypePropertyAssignments(wb, rowNumber, dataSetType.getPropertyAssignments());
            return rowNumber + 1;
        } else
        {
            return rowNumber;
        }
    }

    private DataSetType getDataSetType(final IApplicationServerApi api, final String sessionToken, final String permId)
    {
        final DataSetTypeFetchOptions fetchOptions = new DataSetTypeFetchOptions();
        fetchOptions.withValidationPlugin().withScript();
        final PropertyAssignmentFetchOptions propertyAssignmentFetchOptions = fetchOptions.withPropertyAssignments();
        propertyAssignmentFetchOptions.withPropertyType().withVocabulary();
        propertyAssignmentFetchOptions.withPlugin().withScript();
        final Map<IEntityTypeId, DataSetType> dataSetTypes = api.getDataSetTypes(sessionToken,
                Collections.singletonList(new EntityTypePermId(permId, EntityKind.DATA_SET)), fetchOptions);

        assert dataSetTypes.size() <= 1;

        final Iterator<DataSetType> iterator = dataSetTypes.values().iterator();
        return iterator.hasNext() ? iterator.next() : null;
    }

    @Override
    public IEntityType getEntityType(final IApplicationServerApi api, final String sessionToken,
            final String permId)
    {
        return getDataSetType(api, sessionToken, permId);
    }

}
