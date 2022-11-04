package ch.ethz.sis.openbis.generic.server.xls.export.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Workbook;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.server.xls.export.XLSExport;

public class XLSSampleExportHelper extends AbstractXLSExportHelper
{

    @Override
    public int add(final IApplicationServerApi api, final String sessionToken, final Workbook wb,
            final Collection<String> permIds, int rowNumber, final XLSExport.TextFormatting textFormatting)
    {
        final Collection<Sample> samples = getSamples(api, sessionToken, permIds);

        // Sorting after grouping is needed only to make sure that the tests pass, because entrySet() can have elements
        // in arbitrary order.
        final Collection<Map.Entry<SampleType, List<Sample>>> groupedSamples =
                samples.stream().collect(Collectors.groupingBy(Sample::getType)).entrySet().stream()
                        .sorted(Comparator.comparing(e -> e.getKey().getPermId().getPermId()))
                        .collect(Collectors.toList());

        for (final Map.Entry<SampleType, List<Sample>> entry : groupedSamples)
        {
            addRow(wb, rowNumber++, true, "SAMPLE");
            addRow(wb, rowNumber++, true, "Sample type");
            addRow(wb, rowNumber++, false, entry.getKey().getPermId().getPermId());

            final List<String> headers = new ArrayList<>(List.of("$", "Identifier", "Code", "Space", "Project",
                    "Experiment", "Auto generate code", "Parents", "Children"));
            final List<PropertyAssignment> propertyAssignments = entry.getKey().getPropertyAssignments();
            final List<String> propertyNames = propertyAssignments.stream().map(
                    assignment -> assignment.getPropertyType().getLabel()).collect(Collectors.toList());
            final Map<String, DataType> propertyCodeToTypeMap = propertyAssignments.stream()
                    .map(PropertyAssignment::getPropertyType)
                    .collect(Collectors.toMap(PropertyType::getCode, PropertyType::getDataType));

            headers.addAll(propertyNames);

            addRow(wb, rowNumber++, true, headers.toArray(String[]::new));

            for (final Sample sample : entry.getValue())
            {
                final String parents = sample.getParents() == null ? "" : sample.getParents().stream()
                        .map(parent -> parent.getIdentifier().getIdentifier())
                        .collect(Collectors.joining("\n"));
                final String children = sample.getChildren() == null ? "" : sample.getChildren().stream()
                        .map(child -> child.getIdentifier().getIdentifier())
                        .collect(Collectors.joining("\n"));
                final List<String> sampleValues = new ArrayList<>(
                        List.of("", sample.getIdentifier().getIdentifier(), sample.getCode(),
                                sample.getSpace().getPermId().getPermId(),
                                sample.getProject().getIdentifier().getIdentifier(),
                                sample.getExperiment().getIdentifier().getIdentifier(),
                                "FALSE", parents, children));

                final Map<String, String> properties = sample.getProperties();
                sampleValues.addAll(propertyCodeToTypeMap.entrySet().stream()
                        .map(getPropertiesMappingFunction(textFormatting, properties))
                        .collect(Collectors.toList()));
                
                addRow(wb, rowNumber++, false, sampleValues.toArray(String[]::new));
            }

            rowNumber++;
        }

        return rowNumber;
    }

    private Collection<Sample> getSamples(final IApplicationServerApi api, final String sessionToken,
            final Collection<String> permIds)
    {
        final List<SamplePermId> samplePermIds = permIds.stream().map(SamplePermId::new)
                .collect(Collectors.toList());
        final SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withSpace();
        fetchOptions.withProject();
        fetchOptions.withExperiment();
        fetchOptions.withParents();
        fetchOptions.withChildren();
        fetchOptions.withType().withPropertyAssignments().withPropertyType();
        fetchOptions.withProperties();
        return api.getSamples(sessionToken, samplePermIds, fetchOptions).values();
    }

}
