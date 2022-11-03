package ch.ethz.sis.openbis.generic.server.xls.export.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Workbook;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IIdentifierHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.server.xls.export.XLSExport;

public class XLSDataSetExportHelper extends AbstractXLSExportHelper
{

    @Override
    public int add(final IApplicationServerApi api, final String sessionToken, final Workbook wb,
            final Collection<String> permIds, int rowNumber, final XLSExport.TextFormatting textFormatting)
    {
        final Collection<DataSet> dataSets = getDataSets(api, sessionToken, permIds);

        // Sorting after grouping is needed only to make sure that the tests pass, because entrySet() can have elements
        // in arbitrary order.
        final Collection<Map.Entry<DataSetType, List<DataSet>>> groupedDataSets =
                dataSets.stream().collect(Collectors.groupingBy(DataSet::getType)).entrySet().stream()
                        .sorted(Comparator.comparing(e -> e.getKey().getPermId().getPermId()))
                        .collect(Collectors.toList());

        for (final Map.Entry<DataSetType, List<DataSet>> entry : groupedDataSets)
        {
            addRow(wb, rowNumber++, true, "DATASET");
            addRow(wb, rowNumber++, true, "Dataset type");
            addRow(wb, rowNumber++, false, entry.getKey().getPermId().getPermId());

            final List<String> headers = new ArrayList<>(List.of("Code",
                    entry.getValue().get(0).getSample() != null ? "Sample" : "Experiment"));
            final List<String> propertyNames = entry.getKey().getPropertyAssignments().stream().map(
                    assignment -> assignment.getPropertyType().getLabel()).collect(Collectors.toList());
            final List<String> propertyCodes = entry.getKey().getPropertyAssignments().stream().map(
                    assignment -> assignment.getPropertyType().getCode()).collect(Collectors.toList());
            headers.addAll(propertyNames);

            addRow(wb, rowNumber++, true, headers.toArray(String[]::new));

            for (final DataSet dataSet : entry.getValue())
            {
                final IIdentifierHolder identifierHolder = dataSet.getSample() != null
                        ? dataSet.getSample()
                        : dataSet.getExperiment();
                final List<String> dataSetValues = new ArrayList<>(
                        List.of(dataSet.getCode(), identifierHolder.getIdentifier().getIdentifier()));

                final Map<String, String> properties = dataSet.getProperties();
                dataSetValues.addAll(propertyCodes.stream()
                        .map(getPropertiesMappingFunction(textFormatting, properties))
                        .collect(Collectors.toList()));
                
                addRow(wb, rowNumber++, false, dataSetValues.toArray(String[]::new));
            }

            rowNumber++;
        }

        return rowNumber;
    }

    private Collection<DataSet> getDataSets(final IApplicationServerApi api, final String sessionToken,
            final Collection<String> permIds)
    {
        final List<DataSetPermId> dataSetPermIds = permIds.stream().map(DataSetPermId::new)
                .collect(Collectors.toList());
        final DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withSample();
        fetchOptions.withExperiment();
        fetchOptions.withType().withPropertyAssignments().withPropertyType();
        fetchOptions.withProperties();
        return api.getDataSets(sessionToken, dataSetPermIds, fetchOptions).values();
    }

}
