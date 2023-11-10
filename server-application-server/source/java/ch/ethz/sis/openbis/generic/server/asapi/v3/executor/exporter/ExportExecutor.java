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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.poi.ss.usermodel.Workbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ICodeHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IEntityType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IEntityTypeHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IModificationDateHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IModifierHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IParentChildrenHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPropertiesHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IRegistrationDateHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IRegistratorHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.ExportOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.ExportData;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.IExportableFields;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.SelectedFields;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.options.ExportFormat;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.options.ExportOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.IPropertyTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.server.FileServiceServlet;
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

    public static final String PDF_DIRECTORY = "pdf";

    private static final String TYPE_EXPORT_FIELD_KEY = "TYPE";

    private static final JsonFactory JSON_FACTORY = new JsonFactory();

    private static final Map<ExportableKind, IExportFieldsFinder> FIELDS_FINDER_BY_EXPORTABLE_KIND =
            Map.of(ExportableKind.SAMPLE, new SampleExportFieldsFinder(),
                    ExportableKind.EXPERIMENT, new ExperimentExportFieldsFinder(),
                    ExportableKind.DATASET, new DataSetExportFieldsFinder());

    private static final Set<ExportableKind> TYPE_EXPORTABLE_KINDS = EnumSet.of(ExportableKind.SAMPLE_TYPE, ExportableKind.EXPERIMENT_TYPE,
            ExportableKind.DATASET_TYPE, ExportableKind.VOCABULARY_TYPE, ExportableKind.SPACE, ExportableKind.PROJECT);

    private static final String PYTHON_EXTENSION = ".py";

    private static final String COMMON_STYLE = "border: 1px solid black;";

    private static final String TABLE_STYLE = COMMON_STYLE + " border-collapse: collapse;";

    private static final String DATA_TAG_START = "<DATA>";

    private static final int DATA_TAG_START_LENGTH = DATA_TAG_START.length();

    private static final String DATA_TAG_END = "</DATA>";

    private static final int DATA_TAG_END_LENGTH = DATA_TAG_END.length();

    private static final String PNG_MEDIA_TYPE = "image/png";

    private static final String JPEG_MEDIA_TYPE = "image/jpeg";

    /** Buffer size for the buffer stream for Base64 encoding. Should be a multiple of 3. */
    private static final int BUFFER_SIZE = 3 * 1024;

    private static final Map<String, String> MEDIA_TYPE_BY_EXTENSION = Map.of(
            ".png", PNG_MEDIA_TYPE,
            ".jpg", JPEG_MEDIA_TYPE,
            ".jpeg", JPEG_MEDIA_TYPE,
            ".jfif", JPEG_MEDIA_TYPE,
            ".pjpeg", JPEG_MEDIA_TYPE,
            ".pjp", JPEG_MEDIA_TYPE,
            ".gif", "image/gif",
            ".bmp", "image/bmp",
            ".webp", "image/webp",
            ".tiff", "image/tiff");

    private static final String DEFAULT_MEDIA_TYPE = JPEG_MEDIA_TYPE;

    private static final String DATA_PREFIX_TEMPLATE = "data:%s;base64,";

    @Autowired
    private ISessionWorkspaceProvider sessionWorkspaceProvider;

    @Autowired
    private FileServiceServlet fileServiceServlet;

    @Override
    public ExportResult doExport(final IOperationContext context, final ExportOperation operation)
    {
        try {
            final ExportData exportData = operation.getExportData();
            final ExportOptions exportOptions = operation.getExportOptions();
            final String sessionToken = context.getSession().getSessionToken();

            return doExport(sessionToken, exportData, exportOptions);
        } catch (final IOException e)
        {
            throw UserFailureException.fromTemplate(e, "IO exception exporting.");
        }
    }

    private ExportResult doExport(final String sessionToken, final ExportData exportData, final ExportOptions exportOptions)
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

        final ExportResult exportResult = doExport(applicationServerApi, sessionToken,
                exportablePermIds, exportOptions.isWithReferredTypes(), exportFields,
                TextFormatting.valueOf(exportOptions.getXlsTextFormat().name()), exportOptions.isWithImportCompatibility(),
                exportOptions.getFormats());

        return exportResult;
    }

    private ExportResult doExport(final IApplicationServerApi api,
            final String sessionToken, final List<ExportablePermId> exportablePermIds,
            final boolean exportReferredMasterData,
            final Map<String, Map<String, List<Map<String, String>>>> exportFields,
            final TextFormatting textFormatting, final boolean compatibleWithImport,
            final Set<ExportFormat> exportFormats) throws IOException
    {
        final XLSExport.PrepareWorkbookResult xlsExportResult = exportFormats.contains(ExportFormat.XLSX)
                ? XLSExport.prepareWorkbook(api, sessionToken, exportablePermIds, exportReferredMasterData, exportFields, textFormatting,
                        compatibleWithImport)
                : null;

        final ISessionWorkspaceProvider sessionWorkspaceProvider = CommonServiceProvider.getSessionWorkspaceProvider();
        final String fullFileName = String.format("%s.%s%s", EXPORT_FILE_PREFIX, new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS").format(new Date()),
                ZIP_EXTENSION);
        final Collection<String> warnings = new ArrayList<>();
        final Set<String> existingZipEntries = new HashSet<>();
        try
                (
                        final Workbook wb = xlsExportResult != null ? xlsExportResult.getWorkbook() : null;
                        final FileOutputStream os = sessionWorkspaceProvider.getFileOutputStream(sessionToken, fullFileName);
                        final ZipOutputStream zos = new ZipOutputStream(os);
                        final BufferedOutputStream bos = new BufferedOutputStream(zos, BUFFER_SIZE)
                )
        {
            if (xlsExportResult != null)
            {
                exportXls(zos, bos, xlsExportResult, wb, warnings);
            }

            final boolean hasHtmlFormat = exportFormats.contains(ExportFormat.HTML);
            final boolean hasPdfFormat = exportFormats.contains(ExportFormat.PDF);
            if (hasPdfFormat || hasHtmlFormat)
            {
                putNextZipEntry(existingZipEntries, zos, "%s/", PDF_DIRECTORY);

                final Collector<ExportablePermId, List<String>, List<String>> downstreamCollector = Collector.of(ArrayList::new,
                        (stringPermIds, exportablePermId) -> stringPermIds.add(exportablePermId.getPermId().getPermId()),
                        (left, right) ->
                        {
                            left.addAll(right);
                            return left;
                        });
                final Map<ExportableKind, List<String>> groupedExportablePermIds =
                        exportablePermIds.stream().collect(Collectors.groupingBy(ExportablePermId::getExportableKind, downstreamCollector));

                exportSpaces(zos, bos, api, sessionToken, groupedExportablePermIds, existingZipEntries);
            }
        }

        return new ExportResult(fullFileName, warnings);
    }

    private void exportSpaces(final ZipOutputStream zos, final BufferedOutputStream bos, final IApplicationServerApi api, final String sessionToken,
            final Map<ExportableKind, List<String>> groupedExportablePermIds, final Set<String> existingZipEntries)
            throws IOException
    {
        final Collection<Space> spaces = EntitiesFinder.getSpaces(api, sessionToken, groupedExportablePermIds.get(ExportableKind.SPACE));
        for (final Space space : spaces)
        {
            putNextZipEntry(existingZipEntries, zos, "%s/%s/", PDF_DIRECTORY, space.getCode());

            final byte[] htmlBytes = getHtml(sessionToken, space).getBytes(StandardCharsets.UTF_8);
            writeInChunks(bos, htmlBytes);

            zos.closeEntry();
        }
    }

    private static void writeInChunks(final OutputStream os, final byte[] bytes) throws IOException
    {
        for (int pos = 0; pos < bytes.length; pos += BUFFER_SIZE)
        {
            os.write(Arrays.copyOfRange(bytes, pos, pos + BUFFER_SIZE));
        }
        os.flush();
    }

    /**
     * Adds an entry only if it is needed.
     *
     * @param existingZipEntries a set of existing entries
     * @param zos zip output stream to write to
     * @param entryFormat a format string
     * @param args arguments referenced by the format specifiers in the format string
     * @throws IOException if an I/O error has occurred
     */
    private static void putNextZipEntry(final Set<String> existingZipEntries, final ZipOutputStream zos, final String entryFormat,
            final String... args) throws IOException
    {
        final String entry = String.format(entryFormat, (Object[]) args);
        if (!existingZipEntries.contains(entry))
        {
            zos.putNextEntry(new ZipEntry(entry));
            existingZipEntries.add(entry);
        }
    }

    private static void exportXls(final ZipOutputStream zos, final BufferedOutputStream bos, final XLSExport.PrepareWorkbookResult xlsExportResult,
            final Workbook wb, final Collection<String> warnings) throws IOException
    {
        zos.putNextEntry(new ZipEntry(String.format("%s/", XLSX_DIRECTORY)));

        final Map<String, String> xlsExportScripts = xlsExportResult.getScripts();
        if (!xlsExportScripts.isEmpty())
        {
            zos.putNextEntry(new ZipEntry(String.format("%s/%s/", XLSX_DIRECTORY, SCRIPTS_DIRECTORY)));
        }

        for (final Map.Entry<String, String> script : xlsExportScripts.entrySet())
        {
            zos.putNextEntry(new ZipEntry(String.format("%s/%s/%s%s", XLSX_DIRECTORY, SCRIPTS_DIRECTORY, script.getKey(), PYTHON_EXTENSION)));
            bos.write(script.getValue().getBytes());
            bos.flush();
            zos.closeEntry();
        }

        zos.putNextEntry(new ZipEntry(String.format("%s/%s", XLSX_DIRECTORY, METADATA_FILE_NAME)));
        wb.write(bos);

        warnings.addAll(xlsExportResult.getWarnings());
    }

    private String getHtml(final String sessionToken, final ICodeHolder entityObj) throws IOException
    {
        final IApplicationServerInternalApi v3 = CommonServiceProvider.getApplicationServerApi();

        final DocumentBuilder documentBuilder = new DocumentBuilder();
        documentBuilder.addTitle(entityObj.getCode());
        documentBuilder.addHeader("Identification Info");

        final IEntityType typeObj;
        if (entityObj instanceof Experiment)
        {
            documentBuilder.addProperty("Kind", "Experiment");
            final ExperimentTypeSearchCriteria searchCriteria = new ExperimentTypeSearchCriteria();
            searchCriteria.withCode().thatEquals(((Experiment) entityObj).getType().getCode());
            final ExperimentTypeFetchOptions fetchOptions = new ExperimentTypeFetchOptions();
            fetchOptions.withPropertyAssignments().withPropertyType();
            final SearchResult<ExperimentType> results = v3.searchExperimentTypes(sessionToken, searchCriteria, fetchOptions);
            typeObj = results.getObjects().get(0);
        } else if (entityObj instanceof Sample)
        {
            documentBuilder.addProperty("Kind", "Sample");
            final SampleTypeSearchCriteria searchCriteria = new SampleTypeSearchCriteria();
            searchCriteria.withCode().thatEquals(((Sample) entityObj).getType().getCode());
            final SampleTypeFetchOptions fetchOptions = new SampleTypeFetchOptions();
            fetchOptions.withPropertyAssignments().withPropertyType();
            final SearchResult<SampleType> results = v3.searchSampleTypes(sessionToken, searchCriteria, fetchOptions);
            typeObj = results.getObjects().get(0);
        } else if (entityObj instanceof DataSet)
        {
            final DataSet dataSet = (DataSet) entityObj;
            documentBuilder.addProperty("Kind", "DataSet");
            final DataSetTypeSearchCriteria searchCriteria = new DataSetTypeSearchCriteria();
            searchCriteria.withCode().thatEquals(dataSet.getType().getCode());
            final DataSetTypeFetchOptions fetchOptions = new DataSetTypeFetchOptions();
            fetchOptions.withPropertyAssignments().withPropertyType();
            final SearchResult<DataSetType> results = v3.searchDataSetTypes(sessionToken, searchCriteria, fetchOptions);
            typeObj = results.getObjects().get(0);
        } else
        {
            typeObj = null;
        }

        if (entityObj instanceof Project)
        {
            documentBuilder.addProperty("Kind", "Project");
        } else
        {
            documentBuilder.addProperty("Type", ((IEntityTypeHolder) entityObj).getType().getCode());
        }

        if (entityObj instanceof IRegistratorHolder)
        {
            documentBuilder.addProperty("Registrator", ((IRegistratorHolder) entityObj).getRegistrator().getUserId());
        }

        if (entityObj instanceof IRegistrationDateHolder)
        {
            documentBuilder.addProperty("Registration Date", String.valueOf(((IRegistrationDateHolder) entityObj).getRegistrationDate()));
        }

        if (entityObj instanceof IModifierHolder)
        {
            documentBuilder.addProperty("Modifier", ((IModifierHolder) entityObj).getModifier().getUserId());
        }

        if (entityObj instanceof IModificationDateHolder)
        {
            documentBuilder.addProperty("Modification Date", String.valueOf(((IModificationDateHolder) entityObj).getModificationDate()));
        }

        if (entityObj instanceof Project)
        {
            final String description = ((Project) entityObj).getDescription();
            if (description != null)
            {
                documentBuilder.addHeader("Description");
                documentBuilder.addParagraph(description);
            }
        }

        if (entityObj instanceof IParentChildrenHolder<?>)
        {
            final IParentChildrenHolder<?> parentChildrenHolder = (IParentChildrenHolder<?>) entityObj;
            documentBuilder.addHeader("Parents");
            final List<?> parents = parentChildrenHolder.getParents();
            for (final Object parent : parents)
            {
                final String relCodeName = ((ICodeHolder) parent).getCode();
                final Map<String, Serializable> properties = ((IPropertiesHolder) parent).getProperties();
                if (properties.containsKey("NAME"))
                {
                    documentBuilder.addParagraph(relCodeName + " (" + properties.get("NAME") + ")");
                }
            }

            documentBuilder.addHeader("Children");
            final List<?> children = parentChildrenHolder.getChildren();
            for (final Object child : children)
            {
                final String relCodeName = ((ICodeHolder) child).getCode();
                final Map<String, Serializable> properties = ((IPropertiesHolder) child).getProperties();
                if (properties.containsKey("NAME"))
                {
                    documentBuilder.addParagraph(relCodeName + " (" + properties.get("NAME") + ")");
                }
            }
        }

        if (entityObj instanceof IPropertiesHolder)
        {
            documentBuilder.addHeader("Properties");
            if (typeObj != null)
            {
                final List<PropertyAssignment> propertyAssignments = typeObj.getPropertyAssignments();
                if (propertyAssignments != null)
                {
                    final Map<String, Serializable> properties = ((IPropertiesHolder) entityObj).getProperties();
                    for (final PropertyAssignment propertyAssignment : propertyAssignments)
                    {
                        final PropertyType propertyType = propertyAssignment.getPropertyType();
                        if (properties.containsKey(propertyType.getCode()))
                        {
                            final String propertyValueString = String.valueOf(properties.get(propertyType.getCode()));
                            if (propertyType.getDataType() == DataType.MULTILINE_VARCHAR &&
                                    Objects.equals(propertyType.getMetaData().get("custom_widget"), "Word Processor"))
                            {
                                final StringBuilder propertyValue = new StringBuilder(propertyValueString);
                                final Document doc = Jsoup.parse(propertyValueString);
                                final Elements imageElements = doc.select("img");
                                for (final Element imageElement : imageElements)
                                {
                                    final String imageSrc = imageElement.attr("src");
                                    replaceAll(propertyValue, imageSrc, encodeImageContentToString(imageSrc));
                                }
                            } else if (propertyType.getDataType() == DataType.XML
                                    && Objects.equals(propertyType.getMetaData().get("custom_widget"), "Spreadsheet")
                                    && propertyValueString.toUpperCase().startsWith(DATA_TAG_START) && propertyValueString.toUpperCase()
                                    .endsWith(DATA_TAG_END))
                            {
                                final String subString = propertyValueString.substring(DATA_TAG_START_LENGTH,
                                        propertyValueString.length() - DATA_TAG_END_LENGTH);
                                final String decodedString = new String(Base64.getDecoder().decode(subString), StandardCharsets.UTF_8);

                                try (final JsonParser jsonParser = JSON_FACTORY.createParser(decodedString))
                                {
                                    final String htmlValue = convertJsonToHtml(jsonParser.readValueAsTree());

                                    if (!Objects.equals(htmlValue, "\uFFFD(undefined)"))
                                    {
                                        documentBuilder.addProperty(propertyType.getLabel(), htmlValue);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return documentBuilder.getHtml();
    }

    private String encodeImageContentToString(final String imageSrc) throws IOException
    {
        final Base64.Encoder encoder = Base64.getEncoder();
        final String extension = imageSrc.substring(imageSrc.lastIndexOf('.'));
        final String mediaType = MEDIA_TYPE_BY_EXTENSION.getOrDefault(extension, DEFAULT_MEDIA_TYPE);
        final String dataPrefix = String.format(DATA_PREFIX_TEMPLATE, mediaType);
        final String filePath = fileServiceServlet.getFilesRepository().getPath() + imageSrc;

        final StringBuilder result = new StringBuilder(dataPrefix);
        final FileInputStream fileInputStream = new FileInputStream(filePath);
        try (final BufferedInputStream in = new BufferedInputStream(fileInputStream, BUFFER_SIZE))
        {
            byte[] chunk = new byte[BUFFER_SIZE];
            int len;
            while ((len = in.read(chunk)) == BUFFER_SIZE) {
                result.append(encoder.encodeToString(chunk));
            }

            if (len > 0) {
                chunk = Arrays.copyOf(chunk, len);
                result.append(encoder.encodeToString(chunk));
            }
        }

        return result.toString();
    }

    private static String convertJsonToHtml(final TreeNode node) throws IOException
    {
        final TreeNode data = node.get("data");
        final TreeNode styles = node.get("style");

        final StringBuilder tableBody = new StringBuilder();
        for (int i = 0; i < data.size(); i++)
        {
            final TreeNode dataRow = data.get(i);
            tableBody.append("<tr>\n");
            for (int j = 0; j < dataRow.size(); j++)
            {
                final String stylesKey = convertNumericToAlphanumeric(i, j);
                final String style = ((TextNode) styles.get(stylesKey)).text();
                final TextNode cell = (TextNode) dataRow.get(j);
                tableBody.append("  <td style='").append(COMMON_STYLE).append(" ").append(style).append("'> ").append(cell.text()).append(" </td>\n");
            }
            tableBody.append("</tr>\n");
        }
        return String.format("<table style='%s'>\n%s\n%s", TABLE_STYLE, tableBody, "</table>");
    }

    private static String convertNumericToAlphanumeric(final int row, final int col)
    {
        final int aCharCode = (int) 'A';
        final int ord0 = col % 26;
        final int ord1 = col / 26;
        final char char0 = (char) (aCharCode + ord0);
        final char char1 = (char) (aCharCode + ord1 - 1);
        return String.valueOf(ord1 > 0 ? char1 : "") + char0 + (row + 1);
    }

    private static void replaceAll(final StringBuilder sb, final String target, final String replacement)
    {
        // Start index for the first search
        int startIndex = sb.indexOf(target);
        while (startIndex != -1)
        {
            final int endIndex = startIndex + target.length();
            sb.replace(startIndex, endIndex, replacement);
            // Update the start index for the next search
            startIndex = sb.indexOf(target, startIndex + replacement.length());
        }
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
