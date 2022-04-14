package ch.ethz.sis.openbis.generic.server.xls.export.helper;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.apache.poi.ss.usermodel.Workbook;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPropertyAssignmentsHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.Plugin;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;

public class XLSSampleTypeExportHelper extends AbstractXLSExportHelper
{

    @Override
    public int add(final IApplicationServerApi api, final String sessionToken, final Workbook wb,
            final String permId, int rowNumber)
    {
        final SampleType sampleType = getSampleType(api, sessionToken, permId);
        if (sampleType != null)
        {
            addRow(wb, rowNumber++, true, "SAMPLE_TYPE");
            addRow(wb, rowNumber++, true, "Version", "Code", "Auto generate codes", "Validation script",
                    "Generated Code Prefix");

            // TODO: what to put to validation script?
            final Plugin validationPlugin = sampleType.getValidationPlugin();
            final String script = validationPlugin != null
                    ? (validationPlugin.getScript() != null ? validationPlugin.getScript() : "") : "";

            addRow(wb, rowNumber++, false, "1", sampleType.getCode(),
                    String.valueOf(sampleType.isAutoGeneratedCode()).toUpperCase(),
                    script != null ? script : "",
                    sampleType.getGeneratedCodePrefix());

            rowNumber = addEntityTypePropertyAssignments(wb, rowNumber, sampleType.getPropertyAssignments());

            return rowNumber + 1;
        } else
        {
            return rowNumber;
        }
    }

    private SampleType getSampleType(final IApplicationServerApi api, final String sessionToken, final String permId)
    {
        final SampleTypeFetchOptions fetchOptions = new SampleTypeFetchOptions();
        fetchOptions.withValidationPlugin().withScript();
        final PropertyAssignmentFetchOptions propertyAssignmentFetchOptions = fetchOptions.withPropertyAssignments();
        propertyAssignmentFetchOptions.withPropertyType().withVocabulary();
        propertyAssignmentFetchOptions.withPropertyType().withSampleType();
        propertyAssignmentFetchOptions.withPropertyType().withMaterialType();
        propertyAssignmentFetchOptions.withPlugin().withScript();
        final Map<IEntityTypeId, SampleType> sampleTypes = api.getSampleTypes(sessionToken,
                Collections.singletonList(new EntityTypePermId(permId, EntityKind.SAMPLE)), fetchOptions);

        assert sampleTypes.size() <= 1;

        final Iterator<SampleType> iterator = sampleTypes.values().iterator();
        return iterator.hasNext() ? iterator.next() : null;
    }

    public IPropertyAssignmentsHolder getPropertyAssignmentsHolder(final IApplicationServerApi api,
            final String sessionToken, final String permId)
    {
        return getSampleType(api, sessionToken, permId);
    }

}
