package ch.ethz.sis.openbis.generic.server.xls.export.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.apache.poi.ss.usermodel.Workbook;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IEntityType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.Plugin;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind;
import ch.ethz.sis.openbis.generic.server.xls.export.XLSExport;

public class XLSExperimentTypeExportHelper extends AbstractXLSExportHelper
{

    public XLSExperimentTypeExportHelper(final Workbook wb)
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
        final ExperimentType experimentType = getExperimentType(api, sessionToken, permIds.iterator().next());
        final Collection<String> warnings = new ArrayList<>();

        if (experimentType != null)
        {
            final String permId = experimentType.getPermId().getPermId();
            warnings.addAll(addRow(rowNumber++, true, ExportableKind.EXPERIMENT_TYPE, permId, "EXPERIMENT_TYPE"));
            warnings.addAll(addRow(rowNumber++, true, ExportableKind.EXPERIMENT_TYPE, permId, "Version", "Code",
                    "Description", "Validation script"));

            final Plugin validationPlugin = experimentType.getValidationPlugin();
            final String script = validationPlugin != null
                    ? (validationPlugin.getName() != null ? validationPlugin.getName() + ".py" : "") : "";

            warnings.addAll(addRow(rowNumber++, false, ExportableKind.EXPERIMENT_TYPE, permId, "1",
                    experimentType.getCode(), experimentType.getDescription(), script));

            final AdditionResult additionResult = addEntityTypePropertyAssignments(rowNumber,
                    experimentType.getPropertyAssignments(), ExportableKind.EXPERIMENT_TYPE, permId);
            warnings.addAll(additionResult.getWarnings());
            rowNumber = additionResult.getRowNumber();

            return new AdditionResult(rowNumber + 1, warnings);
        } else
        {
            return new AdditionResult(rowNumber, warnings);
        }
    }

    private ExperimentType getExperimentType(final IApplicationServerApi api, final String sessionToken,
            final String permId)
    {
        final ExperimentTypeFetchOptions fetchOptions = new ExperimentTypeFetchOptions();
        fetchOptions.withValidationPlugin().withScript();
        final PropertyAssignmentFetchOptions propertyAssignmentFetchOptions = fetchOptions.withPropertyAssignments();
        propertyAssignmentFetchOptions.withPropertyType().withVocabulary();
        propertyAssignmentFetchOptions.withPropertyType().withSampleType();
        propertyAssignmentFetchOptions.withPropertyType().withMaterialType();
        propertyAssignmentFetchOptions.withPlugin().withScript();
        final Map<IEntityTypeId, ExperimentType> experimentTypes = api.getExperimentTypes(sessionToken,
                Collections.singletonList(new EntityTypePermId(permId, EntityKind.EXPERIMENT)), fetchOptions);

        assert experimentTypes.size() <= 1;

        final Iterator<ExperimentType> iterator = experimentTypes.values().iterator();
        return iterator.hasNext() ? iterator.next() : null;
    }

    @Override
    public IEntityType getEntityType(final IApplicationServerApi api, final String sessionToken,
            final String permId)
    {
        return getExperimentType(api, sessionToken, permId);
    }

}
