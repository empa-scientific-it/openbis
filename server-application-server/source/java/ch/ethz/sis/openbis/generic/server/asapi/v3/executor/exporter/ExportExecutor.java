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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.ExportOperation;
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
import ch.ethz.sis.openbis.generic.server.xls.export.FieldType;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;

@SuppressWarnings("SizeReplaceableByIsEmpty")
@Component
public class ExportExecutor implements IExportExecutor
{

    public static final String METADATA_FILE_PREFIX = "metadata";

    private static final String TYPE_EXPORT_FIELD_KEY = "TYPE";

    private static final Map<ExportableKind, IExportFieldsFinder> FIELDS_FINDER_BY_EXPORTABLE_KIND =
            Map.of(ExportableKind.SAMPLE, new SampleExportFieldsFinder(),
                    ExportableKind.EXPERIMENT, new ExperimentExportFieldsFinder(),
                    ExportableKind.DATASET, new DataSetExportFieldsFinder());

    private static final Set<ExportableKind> TYPE_EXPORTABLE_KINDS = EnumSet.of(ExportableKind.SAMPLE_TYPE, ExportableKind.EXPERIMENT_TYPE,
            ExportableKind.DATASET_TYPE, ExportableKind.VOCABULARY_TYPE, ExportableKind.SPACE, ExportableKind.PROJECT);

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
                return doXlsExport(sessionToken, exportData, exportOptions);
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

    private static ExportResult doXlsExport(final String sessionToken, final ExportData exportData, final ExportOptions exportOptions)
            throws IOException
    {
        final IApplicationServerInternalApi applicationServerApi = CommonServiceProvider.getApplicationServerApi();

        final List<ExportablePermId> exportablePermIds = exportData.getPermIds().stream()
                .map(exportablePermIdDto -> new ExportablePermId(
                        ExportableKind.valueOf(exportablePermIdDto.getExportableKind().name()), exportablePermIdDto.getPermId()))
                .collect(Collectors.toList());
        final Set<ExportableKind> exportableKinds = exportablePermIds.stream()
                .map(ExportablePermId::getExportableKind)
                .collect(Collectors.toSet());

        final IExportableFields fields = exportData.getFields();
        final Map<String, Map<String, List<Map<String, String>>>> exportFields;
        if (fields instanceof SelectedFields)
        {
            final SelectedFields selectedFields = (SelectedFields) fields;
            exportFields = new HashMap<>();

            final Set<IPropertyTypeId> properties = new HashSet<>(selectedFields.getProperties());

            // TODO: can be rewritten using mapping.
            exportableKinds.forEach(exportableKind ->
            {
                final IExportFieldsFinder fieldsFinder = FIELDS_FINDER_BY_EXPORTABLE_KIND.get(exportableKind);
                if (fieldsFinder != null)
                {
                    final Map<String, List<Map<String, String>>> selectedFieldMap =
                            fieldsFinder.findExportFields(properties, applicationServerApi, sessionToken, selectedFields);
                    exportFields.put(exportableKind.name(), selectedFieldMap);
                } else if (TYPE_EXPORTABLE_KINDS.contains(exportableKind))
                {
                    final Map<String, List<Map<String, String>>> selectedAttributesMap = findExportAttributes(exportableKind, selectedFields);
                    exportFields.put(TYPE_EXPORT_FIELD_KEY, selectedAttributesMap);
                }
            });
        } else
        {
            exportFields = null;
        }

        return export(METADATA_FILE_PREFIX, applicationServerApi, sessionToken,
                exportablePermIds, exportOptions.isWithReferredTypes(), exportFields,
                TextFormatting.valueOf(exportOptions.getXlsTextFormat().name()), exportOptions.isWithImportCompatibility());
    }

    private static Map<String, List<Map<String, String>>> findExportAttributes(final ExportableKind exportableKind, final SelectedFields selectedFields)
    {
        final List<Map<String, String>> attributes = selectedFields.getAttributes().stream()
                .map(attribute -> Map.of(IExportFieldsFinder.TYPE, FieldType.ATTRIBUTE.name(), IExportFieldsFinder.ID, attribute.name()))
                .collect(Collectors.toList());
        return Map.of(exportableKind.name(), attributes);
    }

}
