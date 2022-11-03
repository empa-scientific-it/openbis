package ch.ethz.sis.openbis.generic.server.xls.export.helper;

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
import ch.ethz.sis.openbis.generic.server.xls.export.XLSExport;

public class XLSExperimentTypeExportHelper extends AbstractXLSExportHelper
{

    @Override
    public int add(final IApplicationServerApi api, final String sessionToken, final Workbook wb,
            final Collection<String> permIds, int rowNumber, final XLSExport.TextFormatting textFormatting)
    {
        assert permIds.size() == 1;
        final ExperimentType experimentType = getExperimentType(api, sessionToken, permIds.iterator().next());

        if (experimentType != null)
        {

            addRow(wb, rowNumber++, true, "EXPERIMENT_TYPE");
            addRow(wb, rowNumber++, true, "Version", "Code", "Validation script");

            final Plugin validationPlugin = experimentType.getValidationPlugin();
            final String script = validationPlugin != null
                    ? (validationPlugin.getName() != null ? validationPlugin.getName() + ".py" : "") : "";

            addRow(wb, rowNumber++, false, "1", experimentType.getCode(), script);

            rowNumber = addEntityTypePropertyAssignments(wb, rowNumber, experimentType.getPropertyAssignments());
            return rowNumber + 1;
        } else
        {
            return rowNumber;
        }
    }

    private ExperimentType getExperimentType(final IApplicationServerApi api, final String sessionToken,
            final String permId)
    {
        final ExperimentTypeFetchOptions fetchOptions = new ExperimentTypeFetchOptions();
        fetchOptions.withValidationPlugin().withScript();
        final PropertyAssignmentFetchOptions propertyAssignmentFetchOptions = fetchOptions.withPropertyAssignments();
        propertyAssignmentFetchOptions.withPropertyType().withVocabulary();
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
