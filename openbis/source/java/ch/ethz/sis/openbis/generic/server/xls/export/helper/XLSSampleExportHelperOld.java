package ch.ethz.sis.openbis.generic.server.xls.export.helper;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.poi.ss.usermodel.Workbook;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPropertyAssignmentsHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;

public class XLSSampleExportHelperOld extends AbstractXLSExportHelper
{

    @Override
    public int add(final IApplicationServerApi api, final String sessionToken, final Workbook wb,
            final Collection<String> permIds, int rowNumber)
    {
        final List<Sample> samples = getSamples(api, sessionToken, permIds).stream()
                .sorted(Comparator.comparing(sample -> sample.getType().getPermId().getPermId())).collect(Collectors.toList());
        final Collection<Collection<Sample>> groupedSamples = groupByType(samples);

        for (final Collection<Sample> sampleGroup : groupedSamples)
        {
            addRow(wb, rowNumber++, true, "SAMPLE");
            addRow(wb, rowNumber++, true, "Sample type");

            final Sample firstSample = sampleGroup.iterator().next();
            final SampleType sampleType = firstSample.getType();
            addRow(wb, rowNumber++, false, sampleType.getCode());

            final Collection<PropertyType> propertyTypes = getSamplePropertyTypes(api, sessionToken,
                    firstSample.getProperties().keySet());

            final String[] headers = Stream.concat(
                    Stream.of("$", "Identifier", "Code", "Space", "Project", "Experiment", "Auto generate code",
                            "Parents", "Children"),
                    propertyTypes.stream().map(PropertyType::getLabel)
            ).toArray(String[]::new);

            addRow(wb, rowNumber++, true, headers);
            
            for (final Sample sample : sampleGroup)
            {
                final Experiment experiment = sample.getExperiment();
                // TODO: what to put to auto generate codes, parents and children?
                final String[] values = Stream.concat(
                        Stream.of(sample.getIdentifier().getIdentifier(), sample.getCode(), sample.getSpace().getCode(),
                                sample.getProject().getIdentifier().getIdentifier(),
                                experiment != null ? experiment.getIdentifier().getIdentifier() : "",
                                "FALSE"),
                        sample.getProperties().values().stream()
                ).toArray(String[]::new);
                addRow(wb, rowNumber++, false, values);
            }
        }

        return rowNumber + 1;
    }

    private Collection<PropertyType> getSamplePropertyTypes(final IApplicationServerApi api, final String sessionToken,
            final Collection<String> permIds)
    {
        final List<PropertyTypePermId> propertyTypePermIds = permIds.stream().map(PropertyTypePermId::new)
                .collect(Collectors.toList());

        return api.getPropertyTypes(sessionToken, propertyTypePermIds, new PropertyTypeFetchOptions()).values();
    }

    private Collection<Sample> getSamples(final IApplicationServerApi api, final String sessionToken,
            final Collection<String> permIds)
    {
        final List<SamplePermId> samplePermIds = permIds.stream().map(SamplePermId::new).collect(Collectors.toList());

        final SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withType();
        fetchOptions.withProperties();
        fetchOptions.withSpace();
        fetchOptions.withProject();
        fetchOptions.withExperiment();
        fetchOptions.withParents();
        fetchOptions.withChildren();

        return api.getSamples(sessionToken, samplePermIds, fetchOptions).values();
    }

    @Override
    public IPropertyAssignmentsHolder getPropertyAssignmentsHolder(final IApplicationServerApi api,
            final String sessionToken, final String permId)
    {
        return null;
    }

}
