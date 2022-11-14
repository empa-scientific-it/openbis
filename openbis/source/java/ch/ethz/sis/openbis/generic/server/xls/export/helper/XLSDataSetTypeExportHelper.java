package ch.ethz.sis.openbis.generic.server.xls.export.helper;

import java.util.ArrayList;
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
import ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind;
import ch.ethz.sis.openbis.generic.server.xls.export.XLSExport;

public class XLSDataSetTypeExportHelper extends AbstractXLSExportHelper
{

    public XLSDataSetTypeExportHelper(final Workbook wb)
    {
        super(wb);
    }

    @Override
    public AdditionResult add(final IApplicationServerApi api, final String sessionToken, final Workbook wb,
            final Collection<String> permIds, int rowNumber,
            final Map<String, Collection<String>> entityTypeExportPropertiesMap,
            final XLSExport.TextFormatting textFormatting)
    {
        assert permIds.size() == 1;
        final DataSetType dataSetType = getDataSetType(api, sessionToken, permIds.iterator().next());
        final Collection<String> warnings = new ArrayList<>();

        if (dataSetType != null)
        {
            final String permId = dataSetType.getPermId().getPermId();
            warnings.addAll(addRow(rowNumber++, true, ExportableKind.DATASET_TYPE, permId, "DATASET_TYPE"));
            warnings.addAll(addRow(rowNumber++, true, ExportableKind.DATASET_TYPE, permId, "Version", "Code",
                    "Description", "Validation script"));

            final Plugin validationPlugin = dataSetType.getValidationPlugin();
            final String script = validationPlugin != null
                    ? (validationPlugin.getName() != null ? validationPlugin.getName() + ".py" : "") : "";

            warnings.addAll(addRow(rowNumber++, false, ExportableKind.DATASET_TYPE, permId, "1",
                    dataSetType.getCode(), dataSetType.getDescription(), script));

            final AdditionResult additionResult = addEntityTypePropertyAssignments(rowNumber,
                    dataSetType.getPropertyAssignments(), ExportableKind.DATASET_TYPE, permId);
            warnings.addAll(additionResult.getWarnings());

            rowNumber = additionResult.getRowNumber();
            return new AdditionResult(rowNumber + 1, warnings);
        } else
        {
            return new AdditionResult(rowNumber, warnings);
        }
    }

    private DataSetType getDataSetType(final IApplicationServerApi api, final String sessionToken, final String permId)
    {
        final DataSetTypeFetchOptions fetchOptions = new DataSetTypeFetchOptions();
        fetchOptions.withValidationPlugin().withScript();
        final PropertyAssignmentFetchOptions propertyAssignmentFetchOptions = fetchOptions.withPropertyAssignments();
        propertyAssignmentFetchOptions.withPropertyType().withVocabulary();
        propertyAssignmentFetchOptions.withPropertyType().withSampleType();
        propertyAssignmentFetchOptions.withPropertyType().withMaterialType();
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
