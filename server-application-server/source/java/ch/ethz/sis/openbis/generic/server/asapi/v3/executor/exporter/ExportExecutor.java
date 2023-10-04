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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.ExportOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.Attribute;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.ExportData;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.IExportableFields;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.SelectedFields;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.options.ExportFormat;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.options.ExportOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.ImportOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.options.ImportOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.IPropertyTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind;
import ch.ethz.sis.openbis.generic.server.xls.export.ExportablePermId;
import ch.ethz.sis.openbis.generic.server.xls.export.FieldType;
import ch.ethz.sis.openbis.generic.server.xls.export.XLSExport;
import ch.ethz.sis.openbis.generic.server.xls.importer.XLSImport;
import ch.ethz.sis.openbis.generic.server.xls.importer.enums.ImportModes;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;

@SuppressWarnings("SizeReplaceableByIsEmpty")
@Component
public class ExportExecutor implements IExportExecutor
{

    @Override
    public void doExport(final IOperationContext context, final ExportOperation operation)
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

//                final ch.ethz.sis.openbis.generic.server.xls.export.ImportOptions importerImportOptions =
//                        new ch.ethz.sis.openbis.generic.server.xls.importer.ImportOptions();
//
//                final boolean projectSamplesEnabled = Boolean.parseBoolean(applicationServerApi.getServerInformation(context.getSession().getSessionToken())
//                        .get("project-samples-enabled"));
//                importerImportOptions.setAllowProjectSamples(projectSamplesEnabled);

                final List<ExportablePermId> exportablePermIds = exportData.getPermIds().stream().map(exportablePermIdDto ->
                        new ExportablePermId(
                                ExportableKind.valueOf(exportablePermIdDto.getExportableKind().name()), exportablePermIdDto.getPermId())).collect(
                        Collectors.toList());

                final IExportableFields fields = exportData.getFields();
                final Map<String, Map<String, List<Map<String, String>>>> exportFields;
                if (fields instanceof SelectedFields)
                {
                    final SelectedFields selectedFields = (SelectedFields) fields;
                    exportFields = new HashMap<>();

                    // TODO: are all attributes exported?
                    final Collection<Attribute> attributes = EnumSet.copyOf(selectedFields.getAttributes());
                    if (attributes.size() > 0)
                    {
                        final Map<String, List<Map<String, String>>> selectedAttributes =
                                attributes.stream().collect(Collectors.toMap(Attribute::name, this::convertAttributeToMap));

                        exportFields.put("TYPE", selectedAttributes);
                    }

                    final Collection<IPropertyTypeId> properties = new ArrayList<>(selectedFields.getProperties());

                    // Do similar for experiments and datasets
                    final SampleTypeSearchCriteria searchCriteria = new SampleTypeSearchCriteria();
                    searchCriteria.withPropertyAssignments().withPropertyType().withIds().thatIn(properties);
                    final SearchResult<SampleType> sampleTypeSearchResult =
                            applicationServerApi.searchSampleTypes(sessionToken, searchCriteria, new SampleTypeFetchOptions());
                    final List<SampleType> sampleTypes = sampleTypeSearchResult.getObjects();
                    selectedFields.getProperties().get(0).

                    // TODO: how to map PropertyTypePermId to entity kind?
                    properties.stream().collect(Collectors.toMap(propertyTypePermId -> propertyTypePermId.))
                } else
                {
                    exportFields = null;
                }

                // TODO: file prefix probably should be configurable
                final XLSExport.ExportResult exportResult = XLSExport.export("export", applicationServerApi, sessionToken,
                        exportablePermIds, exportOptions.isWithReferredTypes(), exportFields,
                        XLSExport.TextFormatting.valueOf(exportOptions.getXlsTextFormat().name()), exportOptions.isWithImportCompatibility());
            }
        } catch (final IOException e)
        {
            throw UserFailureException.fromTemplate(e, "IO exception exporting.");
        }
    }

    private List<Map<String, String>> convertAttributeToMap(final Attribute attribute)
    {
        final List<Map<String, String>> result = new ArrayList<>();
        result.add(Map.of("type", FieldType.ATTRIBUTE.name(), "id", ch.ethz.sis.openbis.generic.server.xls.export.Attribute.CODE.getName()));
        result.add(Map.of("type", FieldType.ATTRIBUTE.name(), "id", ch.ethz.sis.openbis.generic.server.xls.export.Attribute.DESCRIPTION.getName()));
        switch (attribute)
        {
            case SPACE:
            {
                result.add(Map.of("type", FieldType.ATTRIBUTE.name(), "id",
                        ch.ethz.sis.openbis.generic.server.xls.export.Attribute.REGISTRATOR.getName()));
                result.add(Map.of("type", FieldType.ATTRIBUTE.name(), "id",
                        ch.ethz.sis.openbis.generic.server.xls.export.Attribute.REGISTRATION_DATE.getName()));
                break;
            }
            case SAMPLE_TYPE:
            {
                result.add(Map.of("type", FieldType.ATTRIBUTE.name(), "id",
                        ch.ethz.sis.openbis.generic.server.xls.export.Attribute.AUTO_GENERATE_CODES.getName()));
                result.add(Map.of("type", FieldType.ATTRIBUTE.name(),
                        "id", ch.ethz.sis.openbis.generic.server.xls.export.Attribute.VALIDATION_SCRIPT.getName()));
                result.add(Map.of("type", FieldType.ATTRIBUTE.name(),
                        "id", ch.ethz.sis.openbis.generic.server.xls.export.Attribute.GENERATED_CODE_PREFIX.getName()));
                result.add(Map.of("type", FieldType.ATTRIBUTE.name(),
                        "id", ch.ethz.sis.openbis.generic.server.xls.export.Attribute.UNIQUE_SUBCODES.getName()));
                result.add(Map.of("type", FieldType.ATTRIBUTE.name(),
                        "id", ch.ethz.sis.openbis.generic.server.xls.export.Attribute.MODIFICATION_DATE.getName()));
                break;
            }
            case EXPERIMENT_TYPE:
            {
                result.add(Map.of("type", FieldType.ATTRIBUTE.name(),
                        "id", ch.ethz.sis.openbis.generic.server.xls.export.Attribute.VALIDATION_SCRIPT.getName()));
                result.add(Map.of("type", FieldType.ATTRIBUTE.name(),
                        "id", ch.ethz.sis.openbis.generic.server.xls.export.Attribute.MODIFICATION_DATE.getName()));
                break;
            }
        }
    }

    private static void importXls(final IOperationContext context, final ImportOperation operation, final Map<String, String> scripts,
            final byte[] xlsContent)
    {
        final IApplicationServerInternalApi applicationServerApi = CommonServiceProvider.getApplicationServerApi();
        final ImportOptions importOptions = operation.getImportOptions();

        final ch.ethz.sis.openbis.generic.server.xls.importer.ImportOptions importerImportOptions =
                new ch.ethz.sis.openbis.generic.server.xls.importer.ImportOptions();

        final boolean projectSamplesEnabled = Boolean.parseBoolean(applicationServerApi.getServerInformation(context.getSession().getSessionToken())
                .get("project-samples-enabled"));
        importerImportOptions.setAllowProjectSamples(projectSamplesEnabled);

        final XLSImport xlsImport = new XLSImport(context.getSession().getSessionToken(), applicationServerApi, scripts,
                ImportModes.valueOf(importOptions.getMode().name()), importerImportOptions, "DEFAULT");

        xlsImport.importXLS(xlsContent);
    }

}
