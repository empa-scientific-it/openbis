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
import static ch.ethz.sis.openbis.generic.server.xls.export.XLSExport.SCRIPTS_DIRECTORY;
import static ch.ethz.sis.openbis.generic.server.xls.export.XLSExport.TextFormatting;
import static ch.ethz.sis.openbis.generic.server.xls.export.XLSExport.ZIP_EXTENSION;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
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
import ch.ethz.sis.openbis.generic.server.xls.export.XLSExport;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.ISessionWorkspaceProvider;

@SuppressWarnings("SizeReplaceableByIsEmpty")
@Component
public class ExportExecutor implements IExportExecutor
{

    public static final String METADATA_FILE_PREFIX = "metadata";

    public static final String EXPORT_FILE_PREFIX = "export";

    public static final String METADATA_FILE_NAME = "metadata" + XLSExport.XLSX_EXTENSION;

    public static final String XLSX_DIRECTORY = "xlsx";

    private static final String TYPE_EXPORT_FIELD_KEY = "TYPE";

    private static final Map<ExportableKind, IExportFieldsFinder> FIELDS_FINDER_BY_EXPORTABLE_KIND =
            Map.of(ExportableKind.SAMPLE, new SampleExportFieldsFinder(),
                    ExportableKind.EXPERIMENT, new ExperimentExportFieldsFinder(),
                    ExportableKind.DATASET, new DataSetExportFieldsFinder());

    private static final Set<ExportableKind> TYPE_EXPORTABLE_KINDS = EnumSet.of(ExportableKind.SAMPLE_TYPE, ExportableKind.EXPERIMENT_TYPE,
            ExportableKind.DATASET_TYPE, ExportableKind.VOCABULARY_TYPE, ExportableKind.SPACE, ExportableKind.PROJECT);

    private static final String PYTHON_EXTENSION = ".py";

    @Autowired
    private ISessionWorkspaceProvider sessionWorkspaceProvider;

    @Override
    public ExportResult doExport(final IOperationContext context, final ExportOperation operation)
    {
        try {
            final ExportData exportData = operation.getExportData();
            final ExportOptions exportOptions = operation.getExportOptions();
            final Set<ExportFormat> formats = exportOptions.getFormats();
            final String sessionToken = context.getSession().getSessionToken();

            // TODO: combination of results is also possible, so combine them afterwards
            if (formats.contains(ExportFormat.XLSX))
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

    private ExportResult doXlsExport(final String sessionToken, final ExportData exportData, final ExportOptions exportOptions)
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
            final Set<IPropertyTypeId> properties = new HashSet<>(selectedFields.getProperties());

            exportFields = exportableKinds.stream().flatMap(exportableKind ->
            {
                final IExportFieldsFinder fieldsFinder = FIELDS_FINDER_BY_EXPORTABLE_KIND.get(exportableKind);
                if (fieldsFinder != null)
                {
                    final Map<String, List<Map<String, String>>> selectedFieldMap =
                            fieldsFinder.findExportFields(properties, applicationServerApi, sessionToken, selectedFields);
                    return Stream.of(new AbstractMap.SimpleEntry<>(exportableKind.name(), selectedFieldMap));
                } else if (TYPE_EXPORTABLE_KINDS.contains(exportableKind))
                {
                    final Map<String, List<Map<String, String>>> selectedAttributesMap = findExportAttributes(exportableKind, selectedFields);
                    return Stream.of(new AbstractMap.SimpleEntry<>(TYPE_EXPORT_FIELD_KEY, selectedAttributesMap));
                } else
                {
                    return Stream.empty();
                }
            }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        } else
        {
            exportFields = null;
        }

        final ExportResult exportResult = exportZipped(applicationServerApi, sessionToken,
                exportablePermIds, exportOptions.isWithReferredTypes(), exportFields,
                TextFormatting.valueOf(exportOptions.getXlsTextFormat().name()), exportOptions.isWithImportCompatibility());

        return exportResult;
    }

    private static ExportResult exportZipped(final IApplicationServerApi api,
            final String sessionToken, final List<ExportablePermId> exportablePermIds,
            final boolean exportReferredMasterData,
            final Map<String, Map<String, List<Map<String, String>>>> exportFields,
            final TextFormatting textFormatting, final boolean compatibleWithImport) throws IOException
    {
        final XLSExport.PrepareWorkbookResult exportResult = XLSExport.prepareWorkbook(api, sessionToken, exportablePermIds,
                exportReferredMasterData, exportFields, textFormatting, compatibleWithImport);
        final Map<String, String> scripts = exportResult.getScripts();
        final ISessionWorkspaceProvider sessionWorkspaceProvider = CommonServiceProvider.getSessionWorkspaceProvider();

        final String fullFileName = String.format("%s.%s%s", EXPORT_FILE_PREFIX, new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS").format(new Date()),
                ZIP_EXTENSION);

        try
                (
                        final FileOutputStream os = sessionWorkspaceProvider.getFileOutputStream(sessionToken, fullFileName);
                        final Workbook wb = exportResult.getWorkbook();
                        final ZipOutputStream zos = new ZipOutputStream(os);
                        final BufferedOutputStream bos = new BufferedOutputStream(zos)
                )
        {
            zos.putNextEntry(new ZipEntry(String.format("%s/", XLSX_DIRECTORY)));

            if (!scripts.isEmpty())
            {
                zos.putNextEntry(new ZipEntry(String.format("%s/%s/", XLSX_DIRECTORY, SCRIPTS_DIRECTORY)));
            }

            for (final Map.Entry<String, String> script : scripts.entrySet())
            {
                zos.putNextEntry(new ZipEntry(String.format("%s/%s/%s%s", XLSX_DIRECTORY, SCRIPTS_DIRECTORY, script.getKey(), PYTHON_EXTENSION)));
                bos.write(script.getValue().getBytes());
                bos.flush();
                zos.closeEntry();
            }

            zos.putNextEntry(new ZipEntry(String.format("%s/%s", XLSX_DIRECTORY, METADATA_FILE_NAME)));
            wb.write(bos);
        }

        return new ExportResult(fullFileName, exportResult.getWarnings());
    }

    private File getActualFile(final String sessionToken, final String actualResultFilePath)
    {
        final File sessionWorkspace = sessionWorkspaceProvider.getSessionWorkspace(sessionToken);
        final File[] files = sessionWorkspace.listFiles((FilenameFilter) new NameFileFilter(actualResultFilePath));

        assertNotNull(files);
        assertEquals(1, files.length, String.format("Session workspace should contain only one file with the download URL '%s'.",
                actualResultFilePath));

        final File file = files[0];

        assertTrue(file.getName().startsWith(METADATA_FILE_PREFIX + "."));
        return file;
    }

    private static Map<String, List<Map<String, String>>> findExportAttributes(final ExportableKind exportableKind, final SelectedFields selectedFields)
    {
        final List<Map<String, String>> attributes = selectedFields.getAttributes().stream()
                .map(attribute -> Map.of(IExportFieldsFinder.TYPE, FieldType.ATTRIBUTE.name(), IExportFieldsFinder.ID, attribute.name()))
                .collect(Collectors.toList());
        return Map.of(exportableKind.name(), attributes);
    }

}
