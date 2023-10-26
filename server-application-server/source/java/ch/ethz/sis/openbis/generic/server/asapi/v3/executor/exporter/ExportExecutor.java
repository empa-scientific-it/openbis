/*
 *  Copyright ETH 2023 Zürich, Scientific IT Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.exporter;

import static ch.ethz.sis.openbis.generic.server.xls.export.XLSExport.ExportResult;
import static ch.ethz.sis.openbis.generic.server.xls.export.XLSExport.TextFormatting;
import static ch.ethz.sis.openbis.generic.server.xls.export.XLSExport.export;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.ExportOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.Attribute;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.ExportData;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.IExportableFields;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.SelectedFields;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.options.ExportFormat;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.options.ExportOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.IPropertyTypeId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind;
import ch.ethz.sis.openbis.generic.server.xls.export.ExportablePermId;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;

@SuppressWarnings("SizeReplaceableByIsEmpty")
@Component
public class ExportExecutor implements IExportExecutor
{

    public static final String METADATA_FILE_PREFIX = "metadata";

//    private static final String TYPE = "type";
//
//    private static final String ID = "id";

    private static final Map<ExportableKind, IExportFieldsFinder> FIELDS_FINDER_BY_EXPORTABLE_KIND =
            Map.of(ExportableKind.SAMPLE, new SampleExportFieldsFinder());

    @Override
    public ExportResult doExport(final IOperationContext context, final ExportOperation operation)
    {
        try {
            final ExportData exportData = operation.getExportData();
            final ExportOptions exportOptions = operation.getExportOptions();
            final Set<ExportFormat> formats = exportOptions.getFormats();
            final String sessionToken = context.getSession().getSessionToken();

            if (formats.contains(ExportFormat.XLS))
            {
                // TODO: extract to a method

                final IApplicationServerInternalApi applicationServerApi = CommonServiceProvider.getApplicationServerApi();

                final List<ExportablePermId> exportablePermIds = exportData.getPermIds().stream().map(exportablePermIdDto ->
                        new ExportablePermId(
                                ExportableKind.valueOf(exportablePermIdDto.getExportableKind().name()), exportablePermIdDto.getPermId())).collect(
                        Collectors.toList());
                final Set<ExportableKind> exportableKinds = exportablePermIds.stream().map(ExportablePermId::getExportableKind)
                        .collect(Collectors.toSet());

                final IExportableFields fields = exportData.getFields();
                final Map<String, Map<String, List<Map<String, String>>>> exportFields;
                if (fields instanceof SelectedFields)
                {
                    final SelectedFields selectedFields = (SelectedFields) fields;
                    exportFields = new HashMap<>();

                    final List<Attribute> attributes = selectedFields.getAttributes();
                    final Set<IPropertyTypeId> properties = new HashSet<>(selectedFields.getProperties());


                    // TODO: Do something similar for experiments and datasets
                    exportableKinds.forEach(exportableKind ->
                    {
                        final IExportFieldsFinder fieldsFinder = FIELDS_FINDER_BY_EXPORTABLE_KIND.get(exportableKind);
                        if (fieldsFinder != null)
                        {
                            final Map<String, List<Map<String, String>>> sampleSelectedFieldMap =
                                    fieldsFinder.findExportFields(properties, applicationServerApi, sessionToken, selectedFields, attributes);
                            exportFields.put(exportableKind.name(), sampleSelectedFieldMap);
                        }
                    }); // TODO: can be rewritten using mapping.
                } else
                {
                    exportFields = null;
                }

                return export(METADATA_FILE_PREFIX, applicationServerApi, sessionToken,
                        exportablePermIds, exportOptions.isWithReferredTypes(), exportFields,
                        TextFormatting.valueOf(exportOptions.getXlsTextFormat().name()), exportOptions.isWithImportCompatibility());
            } else
            {
                // TODO: implement other formats.
                return null;
            }
        } catch (final IOException e)
        {
            throw UserFailureException.fromTemplate(e, "IO exception exporting.");
        }
    }

//    private static Map<String, List<Map<String, String>>> findExportFields(final Set<IPropertyTypeId> properties,
//            final IApplicationServerInternalApi applicationServerApi, final String sessionToken, final SelectedFields selectedFields,
//            final Collection<Attribute> attributes)
//    {
//        final SampleTypeSearchCriteria typeSearchCriteria = new SampleTypeSearchCriteria();
//        typeSearchCriteria.withPropertyAssignments().withPropertyType().withIds().thatIn(properties);
//        final SampleTypeFetchOptions fetchOptions = new SampleTypeFetchOptions();
//        fetchOptions.withPropertyAssignments().withPropertyType();
//        final SearchResult<SampleType> entityTypeSearchResult =
//                applicationServerApi.searchSampleTypes(sessionToken, typeSearchCriteria, fetchOptions);
//
//        final List<SampleType> sampleTypes = entityTypeSearchResult.getObjects();
//        final Map<String, Map<PropertyTypePermId, String>> propertyTypePermIdsBySampleType =
//                sampleTypes.stream().collect(Collectors.toMap(sampleType -> sampleType.getPermId().getPermId(),
//                        sampleType -> sampleType.getPropertyAssignments().stream()
//                                .map(PropertyAssignment::getPropertyType)
//                                .collect(Collectors.toMap(PropertyType::getPermId, PropertyType::getCode))));
//
//        final Collector<SampleType, ?, Map<String, List<Map<String, String>>>> sampleTypeToMapCollector =
//                Collectors.toMap(sampleType -> sampleType.getPermId().getPermId(),
//                        sampleType ->
//                        {
//                            final Map<PropertyTypePermId, String> propertyTypePermIds =
//                                    propertyTypePermIdsBySampleType.get(sampleType.getPermId().getPermId());
//                            final List<String> selectedPropertyTypeCodes =
//                                    selectedFields.getProperties().stream().flatMap(
//                                            propertyTypePermId ->
//                                            {
//                                                final String propertyTypeCode = propertyTypePermIds.get(propertyTypePermId);
//                                                return propertyTypeCode != null ? Stream.of(propertyTypeCode) : Stream.empty();
//                                            })
//                                            .collect(Collectors.toList());
//                            return getPropertyAssignmentList(sampleType, selectedPropertyTypeCodes, attributes);
//                        });
//        final Map<String, List<Map<String, String>>> sampleSelectedFieldMap = sampleTypes.stream().collect(sampleTypeToMapCollector);
//        return sampleSelectedFieldMap;
//    }

//    private static List<Map<String, String>> getPropertyAssignmentList(final SampleType sampleType,
//            final List<String> selectedPropertyTypeCodes,
//            final Collection<Attribute> attributes)
//    {
//        final Stream<Map<String, String>> attributesStream = attributes.stream()
//                .map(attribute -> Map.of(TYPE, FieldType.ATTRIBUTE.name(), ID, attribute.name()));
//
//        final Stream<Map<String, String>> propertiesStream = selectedPropertyTypeCodes.stream()
//                .map(propertyTypeCode -> Map.of(TYPE, FieldType.PROPERTY.name(), ID, propertyTypeCode));
//
//        return Stream.concat(attributesStream, propertiesStream).collect(Collectors.toList());
//    }

//    private List<Map<String, String>> convertAttributeToMap(final Attribute attribute)
//    {
//        final List<Map<String, String>> result = new ArrayList<>();
//        result.add(Map.of(TYPE, FieldType.ATTRIBUTE.name(), ID, ch.ethz.sis.openbis.generic.server.xls.export.Attribute.CODE.getName()));
//        result.add(Map.of(TYPE, FieldType.ATTRIBUTE.name(), ID, ch.ethz.sis.openbis.generic.server.xls.export.Attribute.DESCRIPTION.getName()));
//        switch (attribute)
//        {
//            case SPACE:
//            {
//                result.add(Map.of(TYPE, FieldType.ATTRIBUTE.name(), ID,
//                        ch.ethz.sis.openbis.generic.server.xls.export.Attribute.REGISTRATOR.getName()));
//                result.add(Map.of(TYPE, FieldType.ATTRIBUTE.name(), ID,
//                        ch.ethz.sis.openbis.generic.server.xls.export.Attribute.REGISTRATION_DATE.getName()));
//                break;
//            }
//            case SAMPLE_TYPE:
//            {
//                result.add(Map.of(TYPE, FieldType.ATTRIBUTE.name(), ID,
//                        ch.ethz.sis.openbis.generic.server.xls.export.Attribute.AUTO_GENERATE_CODES.getName()));
//                result.add(Map.of(TYPE, FieldType.ATTRIBUTE.name(),
//                        ID, ch.ethz.sis.openbis.generic.server.xls.export.Attribute.VALIDATION_SCRIPT.getName()));
//                result.add(Map.of(TYPE, FieldType.ATTRIBUTE.name(),
//                        ID, ch.ethz.sis.openbis.generic.server.xls.export.Attribute.GENERATED_CODE_PREFIX.getName()));
//                result.add(Map.of(TYPE, FieldType.ATTRIBUTE.name(),
//                        ID, ch.ethz.sis.openbis.generic.server.xls.export.Attribute.UNIQUE_SUBCODES.getName()));
//                result.add(Map.of(TYPE, FieldType.ATTRIBUTE.name(),
//                        ID, ch.ethz.sis.openbis.generic.server.xls.export.Attribute.MODIFICATION_DATE.getName()));
//                break;
//            }
//            case EXPERIMENT_TYPE:
//            {
//                result.add(Map.of(TYPE, FieldType.ATTRIBUTE.name(),
//                        ID, ch.ethz.sis.openbis.generic.server.xls.export.Attribute.VALIDATION_SCRIPT.getName()));
//                result.add(Map.of(TYPE, FieldType.ATTRIBUTE.name(),
//                        ID, ch.ethz.sis.openbis.generic.server.xls.export.Attribute.MODIFICATION_DATE.getName()));
//                break;
//            }
//        }
//        return result;
//    }

}
