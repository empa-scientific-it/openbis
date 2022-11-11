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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind;
import ch.ethz.sis.openbis.generic.server.xls.export.XLSExport;

public class XLSExperimentExportHelper extends AbstractXLSExportHelper
{

    public XLSExperimentExportHelper(final Workbook wb)
    {
        super(wb);
    }

    @Override
    public AdditionResult add(final IApplicationServerApi api, final String sessionToken, final Workbook wb,
            final Collection<String> permIds, int rowNumber,
            final Map<String, Collection<String>> entityTypeExportPropertiesMap,
            final XLSExport.TextFormatting textFormatting)
    {
        final Collection<Experiment> experiments = getExperiments(api, sessionToken, permIds);
        final Collection<String> warnings = new ArrayList<>();

        // Sorting after grouping is needed only to make sure that the tests pass, because entrySet() can have elements
        // in arbitrary order.
        final Collection<Map.Entry<ExperimentType, List<Experiment>>> groupedExperiments =
                experiments.stream().collect(Collectors.groupingBy(Experiment::getType)).entrySet().stream()
                        .sorted(Comparator.comparing(e -> e.getKey().getPermId().getPermId()))
                        .collect(Collectors.toList());

        for (final Map.Entry<ExperimentType, List<Experiment>> entry : groupedExperiments)
        {
            final String typePermId = entry.getKey().getPermId().getPermId();
            final Collection<String> propertiesToInclude = entityTypeExportPropertiesMap == null
                    ? null
                    : entityTypeExportPropertiesMap.get(typePermId);
            final Predicate<PropertyType> propertiesFilterFunction = getPropertiesFilterFunction(propertiesToInclude);

            warnings.addAll(addRow(rowNumber++, true, ExportableKind.EXPERIMENT_TYPE, typePermId, "EXPERIMENT"));
            warnings.addAll(addRow(rowNumber++, true, ExportableKind.EXPERIMENT_TYPE, typePermId,
                    "Experiment type"));
            warnings.addAll(addRow(rowNumber++, false, ExportableKind.EXPERIMENT_TYPE, typePermId, typePermId));

            final List<String> headers = new ArrayList<>(List.of("Identifier", "Code", "Project"));
            final List<PropertyType> propertyTypes = entry.getKey().getPropertyAssignments().stream()
                    .map(PropertyAssignment::getPropertyType).collect(Collectors.toList());
            final List<String> propertyNames = propertyTypes.stream().filter(propertiesFilterFunction)
                    .map(PropertyType::getLabel).collect(Collectors.toList());

            headers.addAll(propertyNames);

            warnings.addAll(addRow(rowNumber++, true, ExportableKind.EXPERIMENT_TYPE, typePermId,
                    headers.toArray(String[]::new)));

            for (final Experiment experiment : entry.getValue())
            {
                final List<String> experimentValues = new ArrayList<>(
                        List.of(experiment.getIdentifier().getIdentifier(), experiment.getCode(),
                                experiment.getProject().getIdentifier().getIdentifier()));

                final Map<String, String> properties = experiment.getProperties();
                experimentValues.addAll(propertyTypes.stream()
                        .filter(propertiesFilterFunction)
                        .map(getPropertiesMappingFunction(textFormatting, properties))
                        .collect(Collectors.toList()));

                warnings.addAll(addRow(rowNumber++, false, ExportableKind.EXPERIMENT,
                        experiment.getPermId().getPermId(), experimentValues.toArray(String[]::new)));
            }

            rowNumber++;
        }

        return new AdditionResult(rowNumber, warnings);
    }

    private Collection<Experiment> getExperiments(final IApplicationServerApi api, final String sessionToken,
            final Collection<String> permIds)
    {
        final List<ExperimentPermId> experimentPermIds = permIds.stream().map(ExperimentPermId::new)
                .collect(Collectors.toList());
        final ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withProject();
        fetchOptions.withType().withPropertyAssignments().withPropertyType();
        fetchOptions.withProperties();
        return api.getExperiments(sessionToken, experimentPermIds, fetchOptions).values();
    }

}
