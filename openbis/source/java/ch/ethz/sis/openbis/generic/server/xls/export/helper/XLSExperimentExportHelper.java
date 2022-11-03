package ch.ethz.sis.openbis.generic.server.xls.export.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Workbook;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;

public class XLSExperimentExportHelper extends AbstractXLSExportHelper
{

    @Override
    public int add(final IApplicationServerApi api, final String sessionToken, final Workbook wb,
            final Collection<String> permIds, int rowNumber)
    {
        final Collection<Experiment> experiments = getExperiments(api, sessionToken, permIds);

        // Sorting after grouping is needed only to make sure that the tests pass, because entrySet() can have elements
        // in arbitrary order.
        final Collection<Map.Entry<ExperimentType, List<Experiment>>> groupedExperiments =
                experiments.stream().collect(Collectors.groupingBy(Experiment::getType)).entrySet().stream()
                        .sorted(Comparator.comparing(e -> e.getKey().getPermId().getPermId()))
                        .collect(Collectors.toList());

        for (final Map.Entry<ExperimentType, List<Experiment>> entry : groupedExperiments)
        {
            addRow(wb, rowNumber++, true, "EXPERIMENT");
            addRow(wb, rowNumber++, true, "Experiment type");
            addRow(wb, rowNumber++, false, entry.getKey().getPermId().getPermId());

            final List<String> headers = new ArrayList<>(List.of("Identifier", "Code", "Project"));
            final List<String> propertyNames = entry.getKey().getPropertyAssignments().stream().map(
                    assignment -> assignment.getPropertyType().getLabel()).collect(Collectors.toList());
            final List<String> propertyCodes = entry.getKey().getPropertyAssignments().stream().map(
                    assignment -> assignment.getPropertyType().getCode()).collect(Collectors.toList());
            headers.addAll(propertyNames);

            addRow(wb, rowNumber++, true, headers.toArray(String[]::new));

            for (final Experiment experiment : entry.getValue())
            {
                final List<String> experimentValues = new ArrayList<>(
                        List.of(experiment.getIdentifier().getIdentifier(), experiment.getCode(),
                                experiment.getProject().getIdentifier().getIdentifier()));

                final Map<String, String> properties = experiment.getProperties();
                experimentValues.addAll(propertyCodes.stream().map(properties::get).collect(Collectors.toList()));
                
                addRow(wb, rowNumber++, false, experimentValues.toArray(String[]::new));
            }

            rowNumber++;
        }

        return rowNumber;
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
