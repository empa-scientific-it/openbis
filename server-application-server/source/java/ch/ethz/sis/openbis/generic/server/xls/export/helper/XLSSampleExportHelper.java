package ch.ethz.sis.openbis.generic.server.xls.export.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Workbook;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind;
import ch.ethz.sis.openbis.generic.server.xls.export.XLSExport;

public class XLSSampleExportHelper extends AbstractXLSExportHelper
{

    public XLSSampleExportHelper(final Workbook wb)
    {
        super(wb);
    }

    @Override
    public AdditionResult add(final IApplicationServerApi api, final String sessionToken, final Workbook wb,
            final Collection<String> permIds, int rowNumber,
            final Map<String, Collection<String>> entityTypeExportPropertiesMap,
            final XLSExport.TextFormatting textFormatting)
    {
        final Collection<Sample> samples = getSamples(api, sessionToken, permIds);
        final Collection<String> warnings = new ArrayList<>();

        // Sorting after grouping is needed only to make sure that the tests pass, because entrySet() can have elements
        // in arbitrary order.
        final Collection<Map.Entry<SampleType, List<Sample>>> groupedSamples =
                samples.stream().collect(Collectors.groupingBy(Sample::getType)).entrySet().stream()
                        .sorted(Comparator.comparing(e -> e.getKey().getPermId().getPermId()))
                        .collect(Collectors.toList());

        for (final Map.Entry<SampleType, List<Sample>> entry : groupedSamples)
        {
            final String typePermId = entry.getKey().getPermId().getPermId();
            final Collection<String> propertiesToInclude = entityTypeExportPropertiesMap == null
                    ? null
                    : entityTypeExportPropertiesMap.get(typePermId);
            final Predicate<PropertyType> propertiesFilterFunction = getPropertiesFilterFunction(propertiesToInclude);

            warnings.addAll(addRow(rowNumber++, true, ExportableKind.SAMPLE_TYPE, typePermId, "SAMPLE"));
            warnings.addAll(addRow(rowNumber++, true, ExportableKind.SAMPLE_TYPE, typePermId, "Sample type"));
            warnings.addAll(addRow(rowNumber++, false, ExportableKind.SAMPLE_TYPE, typePermId, typePermId));

            final List<String> headers = new ArrayList<>(List.of("$", "Identifier", "Code", "Space", "Project",
                    "Experiment", "Auto generate code", "Parents", "Children"));
            final List<PropertyType> propertyTypes = entry.getKey().getPropertyAssignments().stream()
                    .map(PropertyAssignment::getPropertyType).collect(Collectors.toList());
            final List<String> propertyNames = propertyTypes.stream().filter(propertiesFilterFunction)
                    .map(PropertyType::getLabel).collect(Collectors.toList());

            headers.addAll(propertyNames);

            warnings.addAll(addRow(rowNumber++, true, ExportableKind.SAMPLE_TYPE, typePermId,
                    headers.toArray(String[]::new)));

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
                                sample.getSpace() != null ? sample.getSpace().getPermId().getPermId() : "",
                                sample.getProject() != null ? sample.getProject().getIdentifier().getIdentifier() : "",
                                sample.getExperiment() != null ? sample.getExperiment().getIdentifier().getIdentifier()
                                        : "",
                                "FALSE", parents, children));

                final Map<String, String> properties = sample.getProperties();
                sampleValues.addAll(propertyTypes.stream()
                        .filter(propertiesFilterFunction)
                        .map(getPropertiesMappingFunction(textFormatting, properties))
                        .collect(Collectors.toList()));
                
                warnings.addAll(addRow(rowNumber++, false, ExportableKind.SAMPLE, sample.getIdentifier().getIdentifier(),
                        sampleValues.toArray(String[]::new)));
            }

            rowNumber++;
        }

        return new AdditionResult(rowNumber, warnings);
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
