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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IIdentifierHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind;
import ch.ethz.sis.openbis.generic.server.xls.export.XLSExport;

public class XLSDataSetExportHelper extends AbstractXLSExportHelper
{

    public XLSDataSetExportHelper(final Workbook wb)
    {
        super(wb);
    }

    @Override
    public AdditionResult add(final IApplicationServerApi api, final String sessionToken, final Workbook wb,
            final Collection<String> permIds, int rowNumber,
            final Map<String, Collection<String>> entityTypeExportPropertiesMap,
            final XLSExport.TextFormatting textFormatting)
    {
        final Collection<DataSet> dataSets = getDataSets(api, sessionToken, permIds);
        final Collection<String> warnings = new ArrayList<>();

        // Sorting after grouping is needed only to make sure that the tests pass, because entrySet() can have elements
        // in arbitrary order.
        final Collection<Map.Entry<DataSetType, List<DataSet>>> groupedDataSets =
                dataSets.stream().collect(Collectors.groupingBy(DataSet::getType)).entrySet().stream()
                        .sorted(Comparator.comparing(e -> e.getKey().getPermId().getPermId()))
                        .collect(Collectors.toList());

        for (final Map.Entry<DataSetType, List<DataSet>> entry : groupedDataSets)
        {
            final String typePermId = entry.getKey().getPermId().getPermId();
            final Collection<String> propertiesToInclude = entityTypeExportPropertiesMap == null
                    ? null
                    : entityTypeExportPropertiesMap.get(typePermId);
            final Predicate<PropertyType> propertiesFilterFunction = getPropertiesFilterFunction(propertiesToInclude);

            warnings.addAll(addRow(rowNumber++, true, ExportableKind.DATASET_TYPE, typePermId, "DATASET"));
            warnings.addAll(addRow(rowNumber++, true, ExportableKind.DATASET_TYPE, typePermId, "Dataset type"));
            warnings.addAll(addRow(rowNumber++, false, ExportableKind.DATASET_TYPE, typePermId, typePermId));

            final List<String> headers = new ArrayList<>(List.of("Code",
                    entry.getValue().get(0).getSample() != null ? "Sample" : "Experiment"));
            final List<PropertyType> propertyTypes = entry.getKey().getPropertyAssignments().stream()
                    .map(PropertyAssignment::getPropertyType).collect(Collectors.toList());
            final List<String> propertyNames = propertyTypes.stream().filter(propertiesFilterFunction)
                    .map(PropertyType::getLabel).collect(Collectors.toList());

            headers.addAll(propertyNames);

            warnings.addAll(addRow(rowNumber++, true, ExportableKind.DATASET_TYPE, typePermId,
                    headers.toArray(String[]::new)));

            for (final DataSet dataSet : entry.getValue())
            {
                final IIdentifierHolder identifierHolder = dataSet.getSample() != null
                        ? dataSet.getSample()
                        : dataSet.getExperiment();
                final List<String> dataSetValues = new ArrayList<>(
                        List.of(dataSet.getCode(), identifierHolder.getIdentifier().getIdentifier()));

                final Map<String, String> properties = dataSet.getProperties();
                dataSetValues.addAll(propertyTypes.stream()
                        .filter(propertiesFilterFunction)
                        .map(getPropertiesMappingFunction(textFormatting, properties))
                        .collect(Collectors.toList()));
                
                warnings.addAll(addRow(rowNumber++, false, ExportableKind.DATASET, dataSet.getPermId().getPermId(),
                        dataSetValues.toArray(String[]::new)));
            }

            rowNumber++;
        }

        return new AdditionResult(rowNumber, warnings);
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
