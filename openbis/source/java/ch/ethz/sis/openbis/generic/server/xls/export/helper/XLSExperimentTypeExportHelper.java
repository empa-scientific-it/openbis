package ch.ethz.sis.openbis.generic.server.xls.export.helper;

import java.util.Collections;
import java.util.Map;

import org.apache.poi.ss.usermodel.Workbook;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.Plugin;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyAssignmentFetchOptions;

public class XLSExperimentTypeExportHelper extends AbstractXLSExportHelper
{

    @Override
    public int add(final IApplicationServerApi api, final String sessionToken, final Workbook wb,
            final String permId, int rowNumber)
    {
        final ExperimentTypeFetchOptions fetchOptions = new ExperimentTypeFetchOptions();
        fetchOptions.withValidationPlugin().withScript();
        final PropertyAssignmentFetchOptions propertyAssignmentFetchOptions = fetchOptions.withPropertyAssignments();
        propertyAssignmentFetchOptions.withPropertyType().withVocabulary();
        propertyAssignmentFetchOptions.withPlugin().withScript();
        final Map<IEntityTypeId, ExperimentType> experimentTypes = api.getExperimentTypes(sessionToken,
                Collections.singletonList(new EntityTypePermId(permId, EntityKind.EXPERIMENT)), fetchOptions);

        assert experimentTypes.size() <= 1;

        if (experimentTypes.size() > 0)
        {
            final ExperimentType experimentType = experimentTypes.values().iterator().next();

            addRow(wb, rowNumber++, true, "EXPERIMENT_TYPE");
            addRow(wb, rowNumber++, true, "Version", "Code", "Validation script");

            final Plugin validationPlugin = experimentType.getValidationPlugin();
            final String script = validationPlugin != null
                    ? (validationPlugin.getScript() != null ? validationPlugin.getScript() : "") : "";

            addRow(wb, rowNumber++, false, "1", experimentType.getCode(), script != null ? script : "");

            rowNumber = addEntityTypePropertyAssignments(wb, rowNumber, experimentType.getPropertyAssignments());
            return rowNumber + 1;
        } else
        {
            return rowNumber;
        }
    }

}
