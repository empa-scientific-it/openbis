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

import static ch.ethz.sis.openbis.generic.server.FileServiceServlet.DEFAULT_REPO_PATH;
import static ch.ethz.sis.openbis.generic.server.FileServiceServlet.REPO_PATH_KEY;
import static ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind.DATASET;
import static ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind.EXPERIMENT;
import static ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind.MASTER_DATA_EXPORTABLE_KINDS;
import static ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind.PROJECT;
import static ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind.SAMPLE;
import static ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind.SPACE;
import static ch.ethz.sis.openbis.generic.server.xls.export.FieldType.ATTRIBUTE;
import static ch.ethz.sis.openbis.generic.server.xls.export.FieldType.PROPERTY;
import static ch.ethz.sis.openbis.generic.server.xls.export.XLSExport.ExportResult;
import static ch.ethz.sis.openbis.generic.server.xls.export.XLSExport.SCRIPTS_DIRECTORY;
import static ch.ethz.sis.openbis.generic.server.xls.export.XLSExport.TextFormatting;
import static ch.ethz.sis.openbis.generic.server.xls.export.XLSExport.ZIP_EXTENSION;
import static ch.ethz.sis.openbis.generic.server.xls.export.helper.AbstractXLSExportHelper.FIELD_ID_KEY;
import static ch.ethz.sis.openbis.generic.server.xls.export.helper.AbstractXLSExportHelper.FIELD_TYPE_KEY;
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

import javax.annotation.Resource;

import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.poi.ss.usermodel.Workbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ICodeHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IDescriptionHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IEntityType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IEntityTypeHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IExperimentHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IIdentifierHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IModificationDateHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IModifierHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IParentChildrenHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPermIdHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPropertiesHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IRegistrationDateHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IRegistratorHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ISampleHolder;
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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.Attribute;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.ExportData;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.IExportableFields;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.SelectedFields;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.options.ExportFormat;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.options.ExportOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
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
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.NotFetchedException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind;
import ch.ethz.sis.openbis.generic.server.xls.export.ExportablePermId;
import ch.ethz.sis.openbis.generic.server.xls.export.XLSExport;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer;
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

    public static final String DATA_DIRECTORY = "data";

    public static final String HTML_EXTENSION = ".html";

    public static final String PDF_EXTENSION = ".pdf";

    private static final String TYPE_EXPORT_FIELD_KEY = "TYPE";

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

    private static final String KIND_DOCUMENT_PROPERTY_ID = "Kind";

    static final String NAME_PROPERTY_NAME = "$NAME";

    @Autowired
    private ISessionWorkspaceProvider sessionWorkspaceProvider;

    @Resource(name = ExposablePropertyPlaceholderConfigurer.PROPERTY_CONFIGURER_BEAN_NAME)
    private ExposablePropertyPlaceholderConfigurer configurer;

    @Override
    public ExportResult doExport(final IOperationContext context, final ExportOperation operation)
    {
        try
        {
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

        return doExport(applicationServerApi, sessionToken,
                exportablePermIds, exportOptions.isWithReferredTypes(), exportFields,
                TextFormatting.valueOf(exportOptions.getXlsTextFormat().name()), exportOptions.isWithImportCompatibility(),
                exportOptions.getFormats());
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
            final boolean hasDataFormat = exportFormats.contains(ExportFormat.DATA);

            if (hasHtmlFormat || hasPdfFormat || hasDataFormat)
            {
                final EntitiesVo entitiesVo = new EntitiesVo(sessionToken, exportablePermIds);

                if (hasPdfFormat || hasHtmlFormat)
                {
                    final Set<String> existingZipEntries = new HashSet<>();
                    putNextDocZipEntry(existingZipEntries, zos, null, null, null, null, null, null, null, null);
                    exportSpacesDoc(zos, bos, sessionToken, entitiesVo, existingZipEntries, exportFields, exportFormats);
                    exportProjectsDoc(zos, bos, sessionToken, entitiesVo, existingZipEntries, exportFields, exportFormats);
                    exportExperimentsDoc(zos, bos, sessionToken, entitiesVo, existingZipEntries, exportFields, exportFormats);
                    exportSamplesDoc(zos, bos, sessionToken, entitiesVo, existingZipEntries, exportFields, exportFormats);
                    exportDataSetsDoc(zos, bos, sessionToken, entitiesVo, existingZipEntries, exportFields, exportFormats);
                }

//                if (hasDataFormat)
//                {
//                    final Set<String> existingZipEntries = new HashSet<>();
//                    exportData(zos, bos, sessionToken, entitiesVo, existingZipEntries, exportFields);
//                }
            }
        }

        return new ExportResult(fullFileName, warnings);
    }

//    private void exportData(final ZipOutputStream zos, final BufferedOutputStream bos, final String sessionToken,
//            final EntitiesVo entitiesVo, final Set<String> existingZipEntries,
//            final Map<String, Map<String, List<Map<String, String>>>> exportFields)
//    {
//        final List<String> sampleExportablePermIds = groupedExportablePermIds.get(SAMPLE);
//
//
//
//        final List<String> experimentExportablePermIds = groupedExportablePermIds.get(EXPERIMENT);
//    }

    private void exportSpacesDoc(final ZipOutputStream zos, final BufferedOutputStream bos, final String sessionToken,
            final EntitiesVo entitiesVo, final Set<String> existingZipEntries,
            final Map<String, Map<String, List<Map<String, String>>>> exportFields, final Set<ExportFormat> exportFormats)
            throws IOException
    {
        putZipEntriesForSpacesOfEntities(zos, bos, sessionToken, existingZipEntries, entitiesVo.getSpaces(), exportFields, exportFormats);
        putZipEntriesForSpacesOfEntities(zos, bos, sessionToken, existingZipEntries, entitiesVo.getProjects(), exportFields, exportFormats);
        putZipEntriesForSpacesOfEntities(zos, bos, sessionToken, existingZipEntries, entitiesVo.getExperiments(), exportFields, exportFormats);
        putZipEntriesForSpacesOfEntities(zos, bos, sessionToken, existingZipEntries, entitiesVo.getSamples(), exportFields, exportFormats);
    }

    private void putZipEntriesForSpacesOfEntities(final ZipOutputStream zos, final BufferedOutputStream bos, final String sessionToken,
            final Set<String> existingZipEntries, final Collection<?> entities,
            final Map<String, Map<String, List<Map<String, String>>>> exportFields, final Set<ExportFormat> exportFormats) throws IOException
    {
        final boolean hasHtmlFormat = exportFormats.contains(ExportFormat.HTML);
        final boolean hasPdfFormat = exportFormats.contains(ExportFormat.PDF);

        for (final Object entity : entities)
        {
            if (entity instanceof Space)
            {
                final Space space = (Space) entity;

                final Map<String, List<Map<String, String>>> entityTypeExportFieldsMap = getEntityTypeExportFieldsMap(exportFields, SPACE);
                final String html = getHtml(sessionToken, space, entityTypeExportFieldsMap);
                final byte[] htmlBytes = html.getBytes(StandardCharsets.UTF_8);

                if (hasHtmlFormat)
                {
                    putNextDocZipEntry(existingZipEntries, zos, space.getCode(), null, null, null, null, null, null, HTML_EXTENSION);
                    writeInChunks(bos, htmlBytes);
                    zos.closeEntry();
                }

                if (hasPdfFormat)
                {
                    putNextDocZipEntry(existingZipEntries, zos, space.getCode(), null, null, null, null, null, null, PDF_EXTENSION);

                    final PdfRendererBuilder builder = new PdfRendererBuilder();

                    builder.withHtmlContent(html, null);
                    builder.toStream(bos);
                    builder.run(); // zos is closed here, closing it later throws an exception
                }
            } else
            {
                putNextDocZipEntry(existingZipEntries, zos, getSpaceCode(entity), null, null, null, null, null, null, null);
                zos.closeEntry();
            }
        }
    }

    private void exportProjectsDoc(final ZipOutputStream zos, final BufferedOutputStream bos, final String sessionToken,
            final EntitiesVo entitiesVo, final Set<String> existingZipEntries,
            final Map<String, Map<String, List<Map<String, String>>>> exportFields, final Set<ExportFormat> exportFormats)
            throws IOException
    {
        putZipEntriesForProjectsOfEntities(zos, bos, sessionToken, existingZipEntries, entitiesVo.getProjects(), exportFields, exportFormats);
        putZipEntriesForProjectsOfEntities(zos, bos, sessionToken, existingZipEntries, entitiesVo.getExperiments(), exportFields, exportFormats);
        putZipEntriesForProjectsOfEntities(zos, bos, sessionToken, existingZipEntries, entitiesVo.getSamples(), exportFields, exportFormats);
    }

    private void putZipEntriesForProjectsOfEntities(final ZipOutputStream zos, final BufferedOutputStream bos,
            final String sessionToken, final Set<String> existingZipEntries,
            final Collection<?> entities, final Map<String, Map<String, List<Map<String, String>>>> exportFields,
            final Set<ExportFormat> exportFormats) throws IOException
    {
        final boolean hasHtmlFormat = exportFormats.contains(ExportFormat.HTML);
        final boolean hasPdfFormat = exportFormats.contains(ExportFormat.PDF);

        for (final Object entity : entities)
        {
            if (entity instanceof Project)
            {
                final Project project = (Project) entity;

                final Map<String, List<Map<String, String>>> entityTypeExportFieldsMap = getEntityTypeExportFieldsMap(exportFields, PROJECT);
                final String html = getHtml(sessionToken, project, entityTypeExportFieldsMap);
                final byte[] htmlBytes = html.getBytes(StandardCharsets.UTF_8);

                if (hasHtmlFormat)
                {
                    putNextDocZipEntry(existingZipEntries, zos, project.getSpace().getCode(), project.getCode(), null, null, null, null, null,
                            HTML_EXTENSION);
                    writeInChunks(bos, htmlBytes);
                    zos.closeEntry();
                }

                if (hasPdfFormat)
                {
                    putNextDocZipEntry(existingZipEntries, zos, project.getSpace().getCode(), project.getCode(), null, null, null, null, null,
                            PDF_EXTENSION);

                    final PdfRendererBuilder builder = new PdfRendererBuilder();

                    builder.withHtmlContent(html, null);
                    builder.toStream(bos);
                    builder.run(); // zos is closed here, closing it later throws an exception
                }
            } else
            {
                final String projectCode = getProjectCode(entity);
                if (projectCode != null)
                {
                    putNextDocZipEntry(existingZipEntries, zos, getSpaceCode(entity), projectCode, null, null, null, null, null, null);
                    zos.closeEntry();
                }
            }
        }
    }

    private void exportExperimentsDoc(final ZipOutputStream zos, final BufferedOutputStream bos,
            final String sessionToken, final EntitiesVo entitiesVo, final Set<String> existingZipEntries,
            final Map<String, Map<String, List<Map<String, String>>>> exportFields, final Set<ExportFormat> exportFormats) throws IOException
    {
        putZipEntriesForExperimentsOfEntities(zos, bos, sessionToken, existingZipEntries, entitiesVo.getExperiments(), exportFields, exportFormats);
        putZipEntriesForExperimentsOfEntities(zos, bos, sessionToken, existingZipEntries, entitiesVo.getSamples(), exportFields, exportFormats);
        putZipEntriesForExperimentsOfEntities(zos, bos, sessionToken, existingZipEntries, entitiesVo.getDataSets(), exportFields, exportFormats);
    }

    private void putZipEntriesForExperimentsOfEntities(final ZipOutputStream zos, final BufferedOutputStream bos, final String sessionToken,
            final Set<String> existingZipEntries, final Collection<?> entities,
            final Map<String, Map<String, List<Map<String, String>>>> exportFields, final Set<ExportFormat> exportFormats) throws IOException
    {
        for (final Object entity : entities)
        {
            if (entity instanceof IExperimentHolder)
            {
                final Experiment experiment = ((IExperimentHolder) entity).getExperiment();
                if (experiment != null)
                {
                    final Project project = experiment.getProject();
                    putNextDocZipEntry(existingZipEntries, zos, project.getSpace().getCode(), project.getCode(),
                            experiment.getCode(), getEntityName(experiment), null, null, null, null);
                    zos.closeEntry();
                }
            }

            final boolean hasHtmlFormat = exportFormats.contains(ExportFormat.HTML);
            final boolean hasPdfFormat = exportFormats.contains(ExportFormat.PDF);

            if (entity instanceof Experiment)
            {
                final Experiment experiment = (Experiment) entity;
                final Project project = experiment.getProject();

                final Map<String, List<Map<String, String>>> entityTypeExportFieldsMap = getEntityTypeExportFieldsMap(exportFields, EXPERIMENT);
                final String html = getHtml(sessionToken, (Experiment) entity, entityTypeExportFieldsMap);
                final byte[] htmlBytes = html.getBytes(StandardCharsets.UTF_8);

                if (hasHtmlFormat)
                {
                    putNextDocZipEntry(existingZipEntries, zos, project.getSpace().getCode(), project.getCode(), experiment.getCode(),
                            getEntityName(experiment), null, null, null, HTML_EXTENSION);
                    writeInChunks(bos, htmlBytes);
                    zos.closeEntry();
                }

                if (hasPdfFormat)
                {
                    putNextDocZipEntry(existingZipEntries, zos, project.getSpace().getCode(), project.getCode(), experiment.getCode(),
                            getEntityName(experiment), null, null, null, PDF_EXTENSION);

                    final PdfRendererBuilder builder = new PdfRendererBuilder();

                    builder.withHtmlContent(html, null);
                    builder.toStream(bos);
                    builder.run(); // zos is closed here, closing it later throws an exception
                }
            }
        }
    }

    private void exportSamplesDoc(final ZipOutputStream zos, final BufferedOutputStream bos,
            final String sessionToken, final EntitiesVo entitiesVo, final Set<String> existingZipEntries,
            final Map<String, Map<String, List<Map<String, String>>>> exportFields, final Set<ExportFormat> exportFormats)
            throws IOException
    {
        final Map<String, List<Map<String, String>>> entityTypeExportFieldsMap = getEntityTypeExportFieldsMap(exportFields, SAMPLE);
        putZipEntriesForSamplesOfEntities(zos, bos, sessionToken, existingZipEntries, entitiesVo.getSamples(), entityTypeExportFieldsMap, exportFormats);
    }

    private static Map<String, List<Map<String, String>>> getEntityTypeExportFieldsMap(
            final Map<String, Map<String, List<Map<String, String>>>> exportFields, final ExportableKind exportableKind)
    {
        return exportFields == null
                ? null
                : exportFields.get(MASTER_DATA_EXPORTABLE_KINDS.contains(exportableKind) || exportableKind == SPACE || exportableKind == PROJECT
                ? TYPE_EXPORT_FIELD_KEY : exportableKind.toString());
    }

    private void putZipEntriesForSamplesOfEntities(final ZipOutputStream zos, final BufferedOutputStream bos, final String sessionToken,
            final Set<String> existingZipEntries, final Collection<?> entities,
            final Map<String, List<Map<String, String>>> entityTypeExportFieldsMap, final Set<ExportFormat> exportFormats) throws IOException
    {
        for (final Object entity : entities)
        {
            if (entity instanceof ISampleHolder)
            {
                final Sample sample = ((ISampleHolder) entity).getSample();
                putNextDocZipEntryForSample(zos, existingZipEntries, sample, null);
            }

            if (entity instanceof Sample)
            {
                final Sample sample = (Sample) entity;

                if (exportFormats.contains(ExportFormat.HTML))
                {
                    final byte[] htmlBytes = getHtmlEntryForSample(zos, bos, sessionToken, existingZipEntries, entityTypeExportFieldsMap, sample,
                            HTML_EXTENSION).getBytes(StandardCharsets.UTF_8);

                    writeInChunks(bos, htmlBytes);

                    zos.closeEntry();
                }

                if (exportFormats.contains(ExportFormat.PDF))
                {
                    final String html =
                            getHtmlEntryForSample(zos, bos, sessionToken, existingZipEntries, entityTypeExportFieldsMap, sample, PDF_EXTENSION);
                    final PdfRendererBuilder builder = new PdfRendererBuilder();

                    builder.withHtmlContent(html, null);
                    builder.toStream(bos);
                    builder.run(); // zos is closed here, closing it later throws an exception
                }
            }
        }
    }

    private String getHtmlEntryForSample(final ZipOutputStream zos, final BufferedOutputStream bos, final String sessionToken,
            final Set<String> existingZipEntries, final Map<String, List<Map<String, String>>> entityTypeExportFieldsMap, final Sample sample,
            final String extension) throws IOException
    {
        putNextDocZipEntryForSample(zos, existingZipEntries, sample, extension);
        return getHtml(sessionToken, sample, entityTypeExportFieldsMap);
    }

    private static void putNextDocZipEntryForSample(final ZipOutputStream zos, final Set<String> existingZipEntries, final Sample sample,
            final String extension) throws IOException
    {
        final Experiment experiment = sample.getExperiment();
        if (experiment != null)
        {
            final Project project = experiment.getProject();
            putNextDocZipEntry(existingZipEntries, zos, project.getSpace().getCode(), project.getCode(), experiment.getCode(),
                    getEntityName(experiment), sample.getCode(), getEntityName(sample), null, extension);
        } else
        {
            final Project project = sample.getProject();
            if (project != null)
            {
                putNextDocZipEntry(existingZipEntries, zos, project.getSpace().getCode(), project.getCode(), null, null,
                        sample.getCode(), getEntityName(sample), null, extension);
            } else
            {
                final Space space = sample.getSpace();
                if (space != null)
                {
                    putNextDocZipEntry(existingZipEntries, zos, space.getCode(), null, null, null, sample.getCode(), getEntityName(sample),
                            null, extension);
                }
            }
        }
    }

    private void exportDataSetsDoc(final ZipOutputStream zos, final BufferedOutputStream bos,
            final String sessionToken, final EntitiesVo entitiesVo, final Set<String> existingZipEntries,
            final Map<String, Map<String, List<Map<String, String>>>> exportFields, final Set<ExportFormat> exportFormats)
            throws IOException
    {
        final Map<String, List<Map<String, String>>> entityTypeExportFieldsMap = getEntityTypeExportFieldsMap(exportFields, DATASET);
        putZipEntriesForDataSets(zos, bos, sessionToken, existingZipEntries, entitiesVo.getDataSets(), entityTypeExportFieldsMap, exportFormats);
    }

    private void putZipEntriesForDataSets(final ZipOutputStream zos, final BufferedOutputStream bos, final String sessionToken,
            final Set<String> existingZipEntries, final Collection<?> entities,
            final Map<String, List<Map<String, String>>> entityTypeExportFieldsMap, final Set<ExportFormat> exportFormats) throws IOException
    {
        for (final Object entity : entities)
        {
            if (entity instanceof DataSet)
            {
                final DataSet dataSet = (DataSet) entity;
                final boolean hasHtmlFormat = exportFormats.contains(ExportFormat.HTML);
                final boolean hasPdfFormat = exportFormats.contains(ExportFormat.PDF);

                if (hasHtmlFormat)
                {
                    final byte[] htmlBytes = getHtmlEntryForDataSet(zos, bos, sessionToken, existingZipEntries, entityTypeExportFieldsMap, dataSet,
                            HTML_EXTENSION).getBytes(StandardCharsets.UTF_8);

                    writeInChunks(bos, htmlBytes);

                    zos.closeEntry();
                }

                if (hasPdfFormat)
                {
                    final String html =
                            getHtmlEntryForDataSet(zos, bos, sessionToken, existingZipEntries, entityTypeExportFieldsMap, dataSet, PDF_EXTENSION);
                    final PdfRendererBuilder builder = new PdfRendererBuilder();

                    builder.withHtmlContent(html, null);
                    builder.toStream(bos);
                    builder.run(); // zos is closed here, closing it later throws an exception
                }
            }
        }
    }

    private String getHtmlEntryForDataSet(final ZipOutputStream zos, final BufferedOutputStream bos, final String sessionToken,
            final Set<String> existingZipEntries, final Map<String, List<Map<String, String>>> entityTypeExportFieldsMap, final DataSet dataSet,
            final String extension) throws IOException
    {
        final Sample sample = dataSet.getSample();
        if (sample != null)
        {
            final Experiment experiment = sample.getExperiment();
            final Project project = getProjectForSample(sample);
            putNextDocZipEntry(existingZipEntries, zos, project.getSpace().getCode(), project.getCode(), experiment.getCode(),
                    getEntityName(experiment), dataSet.getCode(), getEntityName(dataSet), null, extension);
        } else
        {
            final Experiment experiment = dataSet.getExperiment();
            if (experiment != null)
            {
                final Project project = experiment.getProject();
                putNextDocZipEntry(existingZipEntries, zos, project.getSpace().getCode(), project.getCode(),
                        experiment.getCode(), getEntityName(experiment), null, null, dataSet.getCode(), extension);
            }
        }

        return getHtml(sessionToken, dataSet, entityTypeExportFieldsMap);
    }

    private static String getSpaceCode(final Object entity)
    {
        if (entity instanceof Space)
        {
            return ((Space) entity).getCode();
        } else if (entity instanceof Project)
        {
            return ((Project) entity).getSpace().getCode();
        } else if (entity instanceof Experiment)
        {
            return ((Experiment) entity).getProject().getSpace().getCode();
        } else if (entity instanceof Sample)
        {
            final Sample sample = (Sample) entity;
            final Space space = sample.getSpace();
            if (space != null)
            {
                return sample.getSpace().getCode();
            } else
            {
                final Experiment experiment = sample.getExperiment();
                if (experiment != null)
                {
                    return experiment.getProject().getSpace().getCode();
                } else
                {
                    return sample.getProject().getSpace().getCode();
                }
            }
        } else
        {
            throw new IllegalArgumentException();
        }
    }

    private static String getProjectCode(final Object entity)
    {
        if (entity instanceof Project)
        {
            return ((Project) entity).getCode();
        } else if (entity instanceof Experiment)
        {
            return ((Experiment) entity).getProject().getCode();
        } else if (entity instanceof Sample)
        {
            final Project project = getProjectForSample((Sample) entity);
            return project != null ? project.getCode() : null;
        } else
        {
            throw new IllegalArgumentException();
        }
    }

    private static Project getProjectForSample(final Sample sample)
    {
        final Experiment experiment = sample.getExperiment();
        if (experiment != null)
        {
            return experiment.getProject();
        } else
        {
            return sample.getProject();
        }
    }

    private static void writeInChunks(final OutputStream os, final byte[] bytes) throws IOException
    {
        final int length = bytes.length;
        for (int pos = 0; pos < length; pos += BUFFER_SIZE)
        {
            os.write(Arrays.copyOfRange(bytes, pos, Math.min(pos + BUFFER_SIZE, length)));
        }
        os.flush();
    }

    /**
     * Adds an entry only if it is needed.
     *
     * @param existingZipEntries a set of existing entries
     * @param zos zip output stream to write to
     * @throws IOException if an I/O error has occurred
     */
    private static void putNextDocZipEntry(final Set<String> existingZipEntries, final ZipOutputStream zos,
            final String spaceCode, final String projectCode, final String experimentCode, final String experimentName,
            final String sampleCode, final String sampleName, final String dataSetCode, final String extension)
            throws IOException
    {
        final String entry = getNextDocZipEntry(spaceCode, projectCode, experimentCode, experimentName, sampleCode, sampleName, dataSetCode, extension);
        if (!existingZipEntries.contains(entry))
        {
            zos.putNextEntry(new ZipEntry(entry));
            existingZipEntries.add(entry);
        }
    }

    static String getNextDocZipEntry(final String spaceCode, final String projectCode, final String experimentCode, final String experimentName,
            final String sampleCode, final String sampleName, final String dataSetCode, final String extension)
    {
        final StringBuilder entryBuilder = new StringBuilder(PDF_DIRECTORY);

        if (spaceCode == null && (projectCode != null || experimentCode != null || sampleCode != null || dataSetCode != null || extension != null))
        {
            throw new IllegalArgumentException();
        } else if (spaceCode != null)
        {
            entryBuilder.append('/').append(spaceCode);
        }

        if (projectCode != null)
        {
            entryBuilder.append('/').append(projectCode);
            if (experimentCode != null)
            {
                addFullEntityName(entryBuilder, experimentCode, experimentName);

                if (sampleCode == null && dataSetCode != null)
                {
                    // Experiment data set
                    entryBuilder.append('/').append(dataSetCode);
                }
            } else if (sampleCode == null && dataSetCode != null)
            {
                throw new IllegalArgumentException();
            }
        } else if (experimentCode != null || (dataSetCode != null && sampleCode == null))
        {
            throw new IllegalArgumentException();
        }

        if (sampleCode != null)
        {
            addFullEntityName(entryBuilder, sampleCode, sampleName);

            if (dataSetCode != null)
            {
                // Sample data set
                entryBuilder.append('/').append(dataSetCode);
            }
        }

        entryBuilder.append(extension != null ? extension : '/');
        return entryBuilder.toString();
    }

    private static void addFullEntityName(final StringBuilder entryBuilder, final String entityCode, final String entityName)
    {
        if (entityName == null || entityName.isEmpty())
        {
            entryBuilder.append("/").append(entityCode);
        } else
        {
            entryBuilder.append("/").append(entityName).append(" (").append(entityCode).append(")");
        }
    }

    private static <T extends IPropertiesHolder & ICodeHolder> String getEntityName(final T entity)
    {
        try
        {
            return entity.getVarcharProperty(NAME_PROPERTY_NAME);
        } catch (final NotFetchedException e)
        {
            return null;
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

    private String getHtml(final String sessionToken, final ICodeHolder entityObj,
            final Map<String, List<Map<String, String>>> entityTypeExportFieldsMap) throws IOException
    {
        final IApplicationServerInternalApi v3 = CommonServiceProvider.getApplicationServerApi();

        final DocumentBuilder documentBuilder = new DocumentBuilder();
        documentBuilder.addTitle(entityObj.getCode());
        documentBuilder.addHeader("Identification Info");

        final IEntityType typeObj;
        if (entityObj instanceof Experiment)
        {
            documentBuilder.addProperty(KIND_DOCUMENT_PROPERTY_ID, "Experiment");
            final ExperimentTypeSearchCriteria searchCriteria = new ExperimentTypeSearchCriteria();
            searchCriteria.withCode().thatEquals(((Experiment) entityObj).getType().getCode());
            final ExperimentTypeFetchOptions fetchOptions = new ExperimentTypeFetchOptions();
            fetchOptions.withPropertyAssignments().withPropertyType();
            final SearchResult<ExperimentType> results = v3.searchExperimentTypes(sessionToken, searchCriteria, fetchOptions);
            typeObj = results.getObjects().get(0);
        } else if (entityObj instanceof Sample)
        {
            documentBuilder.addProperty(KIND_DOCUMENT_PROPERTY_ID, "Sample");
            final SampleTypeSearchCriteria searchCriteria = new SampleTypeSearchCriteria();
            searchCriteria.withCode().thatEquals(((Sample) entityObj).getType().getCode());
            final SampleTypeFetchOptions fetchOptions = new SampleTypeFetchOptions();
            fetchOptions.withPropertyAssignments().withPropertyType();
            final SearchResult<SampleType> results = v3.searchSampleTypes(sessionToken, searchCriteria, fetchOptions);
            typeObj = results.getObjects().get(0);
        } else if (entityObj instanceof DataSet)
        {
            final DataSet dataSet = (DataSet) entityObj;
            documentBuilder.addProperty(KIND_DOCUMENT_PROPERTY_ID, "DataSet");
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
            documentBuilder.addProperty(KIND_DOCUMENT_PROPERTY_ID, "Project");
        } else if (entityObj instanceof Space)
        {
            documentBuilder.addProperty(KIND_DOCUMENT_PROPERTY_ID, "Space");
        } else
        {
            documentBuilder.addProperty("Type", ((IEntityTypeHolder) entityObj).getType().getCode());
        }

        // TODO: what to do when typeObj is null?
        final List<Map<String, String>> selectedExportFields;
        if (entityTypeExportFieldsMap == null || entityTypeExportFieldsMap.isEmpty())
        {
            selectedExportFields = null;
        } else if (typeObj != null)
        {
            selectedExportFields = entityTypeExportFieldsMap.get(typeObj.getCode());
        } else if (entityObj instanceof Space)
        {
            selectedExportFields = entityTypeExportFieldsMap.get(SPACE.name());
        } else if (entityObj instanceof Project)
        {
            selectedExportFields = entityTypeExportFieldsMap.get(PROJECT.name());
        } else
        {
            selectedExportFields = null;
        }

        final Set<String> selectedExportAttributes = selectedExportFields != null
                ? selectedExportFields.stream().filter(map -> Objects.equals(map.get(FIELD_TYPE_KEY), ATTRIBUTE.name()))
                .map(map -> map.get(FIELD_ID_KEY)).collect(Collectors.toSet())
                : null;

        final Set<String> selectedExportProperties = selectedExportFields != null
                ? selectedExportFields.stream().filter(map -> Objects.equals(map.get(FIELD_TYPE_KEY), PROPERTY.name()))
                .map(map -> map.get(FIELD_ID_KEY)).collect(Collectors.toSet())
                : null;

        if (allowsValue(selectedExportAttributes, Attribute.CODE.name()))
        {
            documentBuilder.addProperty("Code", entityObj.getCode());
        }

        if (entityObj instanceof IPermIdHolder && allowsValue(selectedExportAttributes, Attribute.PERM_ID.name()))
        {
            documentBuilder.addProperty("Perm ID", ((IPermIdHolder) entityObj).getPermId().toString());
        }

        if (entityObj instanceof IIdentifierHolder && allowsValue(selectedExportAttributes, Attribute.IDENTIFIER.name()))
        {
            documentBuilder.addProperty("Identifier", entityObj.getCode());
        }

        if (entityObj instanceof IRegistratorHolder && allowsValue(selectedExportAttributes, Attribute.REGISTRATOR.name()))
        {
            final Person registrator = ((IRegistratorHolder) entityObj).getRegistrator();
            if (registrator != null)
            {
                documentBuilder.addProperty("Registrator", registrator.getUserId());
            }
        }

        if (entityObj instanceof IRegistrationDateHolder && allowsValue(selectedExportAttributes, Attribute.REGISTRATION_DATE.name()))
        {
            final Date registrationDate = ((IRegistrationDateHolder) entityObj).getRegistrationDate();
            if (registrationDate != null)
            {
                documentBuilder.addProperty("Registration Date", String.valueOf(registrationDate));
            }
        }

        if (entityObj instanceof IModifierHolder && allowsValue(selectedExportAttributes, Attribute.MODIFIER.name()))
        {
            final Person modifier = ((IModifierHolder) entityObj).getModifier();
            if (modifier != null)
            {
                documentBuilder.addProperty("Modifier", modifier.getUserId());
            }
        }

        if (entityObj instanceof IModificationDateHolder && allowsValue(selectedExportAttributes, Attribute.MODIFICATION_DATE.name()))
        {
            final Date modificationDate = ((IModificationDateHolder) entityObj).getModificationDate();
            if (modificationDate != null)
            {
                documentBuilder.addProperty("Modification Date", String.valueOf(modificationDate));
            }
        }

        if (entityObj instanceof IDescriptionHolder && allowsValue(selectedExportAttributes, Attribute.DESCRIPTION.name()))
        {
            final String description = ((IDescriptionHolder) entityObj).getDescription();
            if (description != null)
            {
                documentBuilder.addHeader("Description");
                documentBuilder.addParagraph(description);
            }
        }

        if (entityObj instanceof IParentChildrenHolder<?>)
        {
            final IParentChildrenHolder<?> parentChildrenHolder = (IParentChildrenHolder<?>) entityObj;
            if (allowsValue(selectedExportAttributes, Attribute.PARENTS.name()))
            {
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
            }

            if (allowsValue(selectedExportAttributes, Attribute.CHILDREN.name()))
            {
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
                        System.out.println(selectedExportFields);

                        final PropertyType propertyType = propertyAssignment.getPropertyType();
                        final String propertyTypeCode = propertyType.getCode();
                        final Object rawPropertyValue = properties.get(propertyTypeCode);

                        if (rawPropertyValue != null && allowsValue(selectedExportProperties, propertyTypeCode))
                        {
                            final String initialPropertyValue = String.valueOf(rawPropertyValue);
                            final String propertyValue;

                            if (propertyType.getDataType() == DataType.MULTILINE_VARCHAR &&
                                    Objects.equals(propertyType.getMetaData().get("custom_widget"), "Word Processor"))
                            {
                                final StringBuilder propertyValueBuilder = new StringBuilder(initialPropertyValue);
                                final Document doc = Jsoup.parse(initialPropertyValue);
                                final Elements imageElements = doc.select("img");
                                for (final Element imageElement : imageElements)
                                {
                                    final String imageSrc = imageElement.attr("src");
                                    replaceAll(propertyValueBuilder, imageSrc, encodeImageContentToString(imageSrc));
                                }
                                propertyValue = propertyValueBuilder.toString();
                            } else if (propertyType.getDataType() == DataType.XML
                                    && Objects.equals(propertyType.getMetaData().get("custom_widget"), "Spreadsheet")
                                    && initialPropertyValue.toUpperCase().startsWith(DATA_TAG_START) && initialPropertyValue.toUpperCase()
                                    .endsWith(DATA_TAG_END))
                            {
                                final String subString = initialPropertyValue.substring(DATA_TAG_START_LENGTH,
                                        initialPropertyValue.length() - DATA_TAG_END_LENGTH);
                                final String decodedString = new String(Base64.getDecoder().decode(subString), StandardCharsets.UTF_8);
                                final ObjectMapper objectMapper = new ObjectMapper();
                                final JsonNode jsonNode = objectMapper.readTree(decodedString);
                                propertyValue = convertJsonToHtml(jsonNode);
                            } else
                            {
                                propertyValue = initialPropertyValue;
                            }

                            if (!Objects.equals(propertyValue, "\uFFFD(undefined)"))
                            {
                                documentBuilder.addProperty(propertyType.getLabel(), propertyValue);
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
        final String filePath = getFilesRepository().getCanonicalPath() + "/" + imageSrc;

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

    /**
     * Whether the set does not forbid a value.
     *
     * @param set the set to look in
     * @param value the value to be found
     * @return <code>true</code> if set is <code>null</code> or value is in the set
     */
    private boolean allowsValue(final Set<String> set, final String value)
    {
        return set == null || set.contains(value);
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
                final String style = ((TextNode) styles.get(stylesKey)).textValue();
                final TextNode cell = (TextNode) dataRow.get(j);
                tableBody.append("  <td style='").append(COMMON_STYLE).append(" ").append(style).append("'> ").append(cell.textValue())
                        .append(" </td>\n");
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
                .map(attribute -> Map.of(IExportFieldsFinder.TYPE, ATTRIBUTE.name(), IExportFieldsFinder.ID, attribute.name()))
                .collect(Collectors.toList());
        return Map.of(exportableKind.name(), attributes);
    }

    private File getFilesRepository()
    {
        return new File(configurer.getResolvedProps().getProperty(REPO_PATH_KEY, DEFAULT_REPO_PATH));
    }

    private static class EntitiesVo
    {

        final Collection<Space> spaces;

        final Collection<Project> projects;

        final Collection<Experiment> experiments;

        final Collection<Sample> samples;

        final Collection<DataSet> dataSets;

        private EntitiesVo(final String sessionToken, final List<ExportablePermId> exportablePermIds)
        {
            final Map<ExportableKind, List<String>> groupedExportablePermIds = getGroupedExportablePermIds(exportablePermIds);

            spaces = EntitiesFinder.getSpaces(sessionToken, groupedExportablePermIds.getOrDefault(ExportableKind.SPACE, List.of()));
            projects = EntitiesFinder.getProjects(sessionToken, groupedExportablePermIds.getOrDefault(ExportableKind.PROJECT, List.of()));
            experiments = EntitiesFinder.getExperiments(sessionToken, groupedExportablePermIds.getOrDefault(ExportableKind.EXPERIMENT, List.of()));
            samples = EntitiesFinder.getSamples(sessionToken, groupedExportablePermIds.getOrDefault(ExportableKind.SAMPLE, List.of()));
            dataSets = EntitiesFinder.getDataSets(sessionToken, groupedExportablePermIds.getOrDefault(DATASET, List.of()));
        }

        private static Map<ExportableKind, List<String>> getGroupedExportablePermIds(final List<ExportablePermId> exportablePermIds)
        {
            final Collector<ExportablePermId, List<String>, List<String>> downstreamCollector = Collector.of(ArrayList::new,
                    (stringPermIds, exportablePermId) -> stringPermIds.add(exportablePermId.getPermId().getPermId()),
                    (left, right) ->
                    {
                        left.addAll(right);
                        return left;
                    });

            return exportablePermIds.stream().collect(Collectors.groupingBy(ExportablePermId::getExportableKind, downstreamCollector));
        }

        public Collection<Space> getSpaces()
        {
            return spaces;
        }

        public Collection<Project> getProjects()
        {
            return projects;
        }

        public Collection<Experiment> getExperiments()
        {
            return experiments;
        }

        public Collection<Sample> getSamples()
        {
            return samples;
        }

        public Collection<DataSet> getDataSets()
        {
            return dataSets;
        }

    }

}
