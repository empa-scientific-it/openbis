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
import static ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind.MASTER_DATA_EXPORTABLE_KINDS;
import static ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind.PROJECT;
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
import java.io.InputStream;
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

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.log4j.Logger;
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
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.TextNode;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.ObjectIdentifier;
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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetKind;
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
import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.DataSetFile;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.download.DataSetFileDownload;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.download.DataSetFileDownloadOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.download.DataSetFileDownloadReader;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.fetchoptions.DataSetFileFetchOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.id.DataSetFilePermId;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.search.DataSetFileSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.sharedapi.v3.json.ObjectMapperResource;
import ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind;
import ch.ethz.sis.openbis.generic.server.xls.export.ExportablePermId;
import ch.ethz.sis.openbis.generic.server.xls.export.XLSExport;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
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

    public static final String META_FILE_NAME = "meta.json";

    public static final String SHARED_SAMPLES_DIRECTORY = "(shared)";

    public static final String HTML_EXTENSION = ".html";

    public static final String PDF_EXTENSION = ".pdf";

    public static final String JSON_EXTENSION = ".json";

    static final String NAME_PROPERTY_NAME = "$NAME";

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

    private static final Logger OPERATION_LOG = LogFactory.getLogger(LogCategory.OPERATION, ExportExecutor.class);

    /** All characters except the ones we consider safe as a folder name. */
    private static final String UNSAFE_CHARACTERS_REGEXP = "[^\\w $!#%'()+,\\-.;=@\\[\\]^{}_~]";

    @Autowired
    private ISessionWorkspaceProvider sessionWorkspaceProvider;

    @Resource(name = ObjectMapperResource.NAME)
    private ObjectMapper objectMapper;

    @Resource(name = ExposablePropertyPlaceholderConfigurer.PROPERTY_CONFIGURER_BEAN_NAME)
    private ExposablePropertyPlaceholderConfigurer configurer;

    private ObjectWriter objectWriter;

    @PostConstruct
    private void postConstruct()
    {
        objectWriter = objectMapper.writerWithDefaultPrettyPrinter();
    }

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
        final String fullFileName = String.format("%s.%s%s", EXPORT_FILE_PREFIX, new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS").format(new Date()),
                ZIP_EXTENSION);
        final Collection<String> warnings = new ArrayList<>();

        final boolean hasXlsxFormat = exportFormats.contains(ExportFormat.XLSX);
        final boolean hasHtmlFormat = exportFormats.contains(ExportFormat.HTML);
        final boolean hasPdfFormat = exportFormats.contains(ExportFormat.PDF);
        final boolean hasDataFormat = exportFormats.contains(ExportFormat.DATA);

        if (hasXlsxFormat)
        {
            exportXlsx(api, sessionToken, exportablePermIds, exportReferredMasterData, exportFields, textFormatting, compatibleWithImport, warnings);
        }

        if (hasHtmlFormat || hasPdfFormat || hasDataFormat)
        {
            final EntitiesVo entitiesVo = new EntitiesVo(sessionToken, exportablePermIds);

            if (hasPdfFormat || hasHtmlFormat)
            {
                final ISessionWorkspaceProvider sessionWorkspaceProvider = CommonServiceProvider.getSessionWorkspaceProvider();
                final File sessionWorkspaceDirectory = sessionWorkspaceProvider.getSessionWorkspace(sessionToken).getCanonicalFile();
                final File docDirectory = new File(sessionWorkspaceDirectory, PDF_DIRECTORY);
                mkdir(docDirectory);

                exportSpacesDoc(sessionToken, exportFields, entitiesVo, exportFormats, docDirectory);
                exportProjectsDoc(sessionToken, docDirectory, entitiesVo, exportFields, exportFormats);
                exportExperimentsDoc(sessionToken, docDirectory, entitiesVo, exportFields, exportFormats);
                exportSamplesDoc(sessionToken, docDirectory, entitiesVo, exportFields, exportFormats);
                exportDataSetsDoc(sessionToken, docDirectory, entitiesVo, exportFields, exportFormats);
            }

            if (hasDataFormat)
            {
                final Set<String> existingZipEntries = new HashSet<>();
                exportData(sessionToken, entitiesVo, existingZipEntries);
            }
        }

        return new ExportResult(fullFileName, warnings);
    }

    private static void exportXlsx(final IApplicationServerApi api, final String sessionToken, final List<ExportablePermId> exportablePermIds,
            final boolean exportReferredMasterData, final Map<String, Map<String, List<Map<String, String>>>> exportFields,
            final TextFormatting textFormatting, final boolean compatibleWithImport, final Collection<String> warnings) throws IOException
    {
        final ISessionWorkspaceProvider sessionWorkspaceProvider = CommonServiceProvider.getSessionWorkspaceProvider();
        final File sessionWorkspaceDirectory = sessionWorkspaceProvider.getSessionWorkspace(sessionToken).getCanonicalFile();
        final XLSExport.PrepareWorkbookResult xlsExportResult = XLSExport.prepareWorkbook(api, sessionToken, exportablePermIds,
                exportReferredMasterData, exportFields, textFormatting, compatibleWithImport);

        final File xlsxDirectory = new File(sessionWorkspaceDirectory, XLSX_DIRECTORY);
        mkdir(xlsxDirectory);

        final File scriptsDirectory = new File(xlsxDirectory, SCRIPTS_DIRECTORY);

        final Map<String, String> xlsExportScripts = xlsExportResult.getScripts();
        if (!xlsExportScripts.isEmpty())
        {
            mkdir(scriptsDirectory);

            for (final Map.Entry<String, String> script : xlsExportScripts.entrySet())
            {
                final File scriptFile = new File(scriptsDirectory, script.getKey() + PYTHON_EXTENSION);
                try (final BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(scriptFile), BUFFER_SIZE))
                {
                    bos.write(script.getValue().getBytes());
                    bos.flush();
                }
            }
        }

        try (
                final Workbook wb = xlsExportResult.getWorkbook();
                final BufferedOutputStream bos = new BufferedOutputStream(
                        new FileOutputStream(new File(xlsxDirectory, METADATA_FILE_NAME)), BUFFER_SIZE);
        )
        {
            wb.write(bos);
        }

        warnings.addAll(xlsExportResult.getWarnings());
    }

//    private void exportData(final ZipOutputStream zos, final OutputStream os, final String sessionToken,
//            final EntitiesVo entitiesVo, final Set<String> existingZipEntries,
//            final Map<String, Map<String, List<Map<String, String>>>> exportFields) throws IOException
//    {
//        final Collection<Sample> samples = entitiesVo.getSamples();
//        for (final Sample sample : samples)
//        {
//            exportDatasetsData(zos, os, sessionToken, existingZipEntries, 'O', sample.getDataSets(), sample, sample.getContainer());
//        }
//
//        final Collection<Experiment> experiments = entitiesVo.getExperiments();
//        for (final Experiment experiment : experiments)
//        {
//            exportDatasetsData(zos, os, sessionToken, existingZipEntries, 'E', experiment.getDataSets(), experiment, null);
//        }
//    }

    private void exportData(final String sessionToken, final EntitiesVo entitiesVo, final Set<String> existingZipEntries) throws IOException
    {
        final Collection<Sample> samples = entitiesVo.getSamples();
        for (final Sample sample : samples)
        {
            exportDatasetsData(sessionToken, existingZipEntries, 'O', sample.getDataSets(), sample, sample.getContainer());
        }

        final Collection<Experiment> experiments = entitiesVo.getExperiments();
        for (final Experiment experiment : experiments)
        {
            exportDatasetsData(sessionToken, existingZipEntries, 'E', experiment.getDataSets(), experiment, null);
        }
    }

    private void exportDatasetsData(final String sessionToken,
            final Set<String> existingZipEntries, final char prefix, final List<DataSet> dataSets, final ICodeHolder codeHolder,
            final Sample container) throws IOException
    {
        final ISessionWorkspaceProvider sessionWorkspaceProvider = CommonServiceProvider.getSessionWorkspaceProvider();
        final File sessionWorkspaceDirectory = sessionWorkspaceProvider.getSessionWorkspace(sessionToken).getCanonicalFile();

        final String spaceCode = getSpaceCode(codeHolder);
        final String projectCode = getProjectCode(codeHolder);
        final String containerCode = container == null ? null : container.getCode();
        final String code = codeHolder.getCode();
        final String codeHolderJson = objectWriter.writeValueAsString(codeHolder);
        final IDataStoreServerApi v3Dss = CommonServiceProvider.getDataStoreServerApi();

        for (final DataSet dataSet : dataSets)
        {
            final String dataSetPermId = dataSet.getPermId().getPermId();
            final String dataSetCode = dataSet.getCode();
            final String dataSetTypeCode = dataSet.getType().getCode();
            final String dataSetName = getEntityName(dataSet);

            createMetadataJsonFile(sessionToken, prefix, spaceCode, projectCode, containerCode, code, dataSetTypeCode,
                    dataSetCode, dataSetName, codeHolderJson);

            if (dataSet.getKind() != DataSetKind.LINK)
            {
                final DataSetFileSearchCriteria criteria = new DataSetFileSearchCriteria();
                criteria.withDataSet().withPermId().thatEquals(dataSetPermId);

                final SearchResult<DataSetFile> results = v3Dss.searchFiles(sessionToken, criteria, new DataSetFileFetchOptions());

                OPERATION_LOG.info(String.format("Found: %d files", results.getTotalCount()));

                final List<DataSetFile> dataSetFiles = results.getObjects();
                final List<DataSetFilePermId> fileIds = dataSetFiles.stream().map(DataSetFile::getPermId).collect(Collectors.toList());

                final DataSetFileDownloadOptions options = new DataSetFileDownloadOptions();
                options.setRecursive(true);

                try (final InputStream is = v3Dss.downloadFiles(sessionToken, fileIds, options))
                {
                    final DataSetFileDownloadReader reader = new DataSetFileDownloadReader(is);
                    DataSetFileDownload file;
                    while ((file = reader.read()) != null)
                    {
                        createNextDataFile(sessionToken, prefix, spaceCode, projectCode,
                                containerCode, code, dataSetTypeCode, dataSetCode, dataSetName, file);
                    }
                }
            } else
            {
                OPERATION_LOG.info(String.format("Omitted data export for link dataset with permId: %s", dataSetPermId));
            }
        }
    }

    private static void createMetadataJsonFile(final String sessionToken, final char prefix, final String spaceCode, final String projectCode,
            final String containerCode, final String code, final String dataSetTypeCode, final String dataSetCode, final String dataSetName,
            final String codeHolderJson) throws IOException
    {
        final ISessionWorkspaceProvider sessionWorkspaceProvider = CommonServiceProvider.getSessionWorkspaceProvider();
        final File sessionWorkspaceDirectory = sessionWorkspaceProvider.getSessionWorkspace(sessionToken).getCanonicalFile();
        final File dataDirectory = new File(sessionWorkspaceDirectory, DATA_DIRECTORY);
        mkdir(dataDirectory);

        final File metadataFile = new File(dataDirectory,
                getDataDirectoryName(prefix, spaceCode, projectCode, containerCode, code, dataSetTypeCode, dataSetCode, dataSetName, META_FILE_NAME));

        final File dataSubdirectory = metadataFile.getParentFile();
        mkdir(dataSubdirectory);

        try (final OutputStream os = new BufferedOutputStream(new FileOutputStream(metadataFile)))
        {
            writeInChunks(os, codeHolderJson.getBytes(StandardCharsets.UTF_8));
        }
    }

    private void exportSpacesDoc(final String sessionToken, final Map<String, Map<String, List<Map<String, String>>>> exportFields,
            final EntitiesVo entitiesVo, final Set<ExportFormat> exportFormats, final File docDirectory) throws IOException
    {
        createFilesAndFoldersForSpacesOfEntities(sessionToken, docDirectory, entitiesVo.getSpaces(), exportFields, exportFormats);
        createFilesAndFoldersForSpacesOfEntities(sessionToken, docDirectory, entitiesVo.getProjects(), exportFields, exportFormats);
        createFilesAndFoldersForSpacesOfEntities(sessionToken, docDirectory, entitiesVo.getExperiments(), exportFields, exportFormats);
        createFilesAndFoldersForSpacesOfEntities(sessionToken, docDirectory, entitiesVo.getSamples(), exportFields, exportFormats);
    }

    private void createFilesAndFoldersForSpacesOfEntities(final String sessionToken, final File docDirectory, final Collection<?> entities,
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
                    final File htmlFile = createNextDocFile(docDirectory, space.getCode(), null, null, null, null, null, null, null, HTML_EXTENSION);
                    try (final BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(htmlFile), BUFFER_SIZE))
                    {
                        writeInChunks(bos, htmlBytes);
                        bos.flush();
                    }
                }

                if (hasPdfFormat)
                {
                    final File pdfFile = createNextDocFile(docDirectory, space.getCode(), null, null, null, null, null, null, null, PDF_EXTENSION);
                    try (final BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(pdfFile), BUFFER_SIZE))
                    {
                        final PdfRendererBuilder builder = new PdfRendererBuilder();
                        builder.withHtmlContent(html, null);
                        builder.toStream(bos);
                        builder.run();
                    }
                }
            } else
            {
                final String spaceCode = getSpaceCode(entity);
                final String folderName = spaceCode == null && entity instanceof Sample ? SHARED_SAMPLES_DIRECTORY : spaceCode;
                final File space = createNextDocFile(docDirectory, folderName, null, null, null, null, null, null, null, null);
                mkdir(space);
            }
        }
    }

    private void exportProjectsDoc(final String sessionToken, final File docDirectory, final EntitiesVo entitiesVo,
            final Map<String, Map<String, List<Map<String, String>>>> exportFields, final Set<ExportFormat> exportFormats) throws IOException
    {
        createFilesAndFoldersForProjectsOfEntities(sessionToken, docDirectory, entitiesVo.getProjects(), exportFields, exportFormats);
        createFilesAndFoldersForProjectsOfEntities(sessionToken, docDirectory, entitiesVo.getExperiments(), exportFields, exportFormats);
        createFilesAndFoldersForProjectsOfEntities(sessionToken, docDirectory, entitiesVo.getSamples(), exportFields, exportFormats);
    }

    private void createFilesAndFoldersForProjectsOfEntities(final String sessionToken, final File docDirectory, final Collection<?> entities,
            final Map<String, Map<String, List<Map<String, String>>>> exportFields, final Set<ExportFormat> exportFormats) throws IOException
    {
        for (final Object entity : entities)
        {
            if (entity instanceof Project)
            {
                final Project project = (Project) entity;

                createDocFilesForEntity(sessionToken, docDirectory, exportFields, project,
                        project.getSpace().getCode(), project.getCode(), null, null, null, null, null, null,
                        exportFormats);
            } else
            {
                final String projectCode = getProjectCode(entity);
                if (projectCode != null)
                {
                    final File space = createNextDocFile(docDirectory, getSpaceCode(entity), projectCode, null, null, null, null, null, null, null);
                    mkdir(space);
                }
            }
        }
    }

    private void exportExperimentsDoc(final String sessionToken, final File docDirectory, final EntitiesVo entitiesVo,
            final Map<String, Map<String, List<Map<String, String>>>> exportFields, final Set<ExportFormat> exportFormats) throws IOException
    {
        createFilesAndFoldersForExperimentsOfEntities(sessionToken, docDirectory, entitiesVo.getExperiments(), exportFields, exportFormats);
        createFilesAndFoldersForExperimentsOfEntities(sessionToken, docDirectory, entitiesVo.getSamples(), exportFields, exportFormats);
        createFilesAndFoldersForExperimentsOfEntities(sessionToken, docDirectory, entitiesVo.getDataSets(), exportFields, exportFormats);
    }

    private void createFilesAndFoldersForExperimentsOfEntities(final String sessionToken, final File docDirectory,
            final Collection<?> entities, final Map<String, Map<String, List<Map<String, String>>>> exportFields,
            final Set<ExportFormat> exportFormats) throws IOException
    {
        for (final Object entity : entities)
        {
            if (entity instanceof IExperimentHolder)
            {
                final Experiment experiment = ((IExperimentHolder) entity).getExperiment();
                if (experiment != null)
                {
                    final Project project = experiment.getProject();
                    final File docFile = createNextDocFile(docDirectory, project.getSpace().getCode(), project.getCode(), experiment.getCode(),
                            getEntityName(experiment), null, null, null, null, null);
                    mkdir(docFile);
                }
            }

            if (entity instanceof Experiment)
            {
                final Experiment experiment = (Experiment) entity;
                final Project project = experiment.getProject();

                createDocFilesForEntity(sessionToken, docDirectory, exportFields, project,
                        project.getSpace().getCode(), project.getCode(), experiment.getCode(), getEntityName(experiment), null, null, null, null,
                        exportFormats);
            }
        }
    }

    private void exportSamplesDoc(final String sessionToken, final File docDirectory, final EntitiesVo entitiesVo,
            final Map<String, Map<String, List<Map<String, String>>>> exportFields, final Set<ExportFormat> exportFormats)
            throws IOException
    {
        createFilesAndFoldersForSamplesOfEntities(sessionToken, docDirectory, entitiesVo.getSamples(), exportFields, exportFormats);
    }

    private static Map<String, List<Map<String, String>>> getEntityTypeExportFieldsMap(
            final Map<String, Map<String, List<Map<String, String>>>> exportFields, final ExportableKind exportableKind)
    {
        return exportFields == null
                ? null
                : exportFields.get(MASTER_DATA_EXPORTABLE_KINDS.contains(exportableKind) || exportableKind == SPACE || exportableKind == PROJECT
                ? TYPE_EXPORT_FIELD_KEY : exportableKind.toString());
    }

    private void createFilesAndFoldersForSamplesOfEntities(final String sessionToken, final File docDirectory,
            final Collection<?> entities, final Map<String, Map<String, List<Map<String, String>>>> exportFields,
            final Set<ExportFormat> exportFormats) throws IOException
    {
        for (final Object entity : entities)
        {
            if (entity instanceof ISampleHolder)
            {
                final Sample sample = ((ISampleHolder) entity).getSample();
                final Experiment experiment = sample.getExperiment();
                final File docFile;

                if (experiment != null)
                {
                    final Project project = experiment.getProject();
                    docFile = createNextDocFile(docDirectory, project.getSpace().getCode(), project.getCode(), experiment.getCode(),
                            getEntityName(experiment), null, null, null, null, null);
                } else
                {
                    final Project project = sample.getProject();
                    if (project != null)
                    {
                        docFile = createNextDocFile(docDirectory, project.getSpace().getCode(), project.getCode(), null,
                                null, null, null, null, null, null);
                    } else
                    {
                        final Space space = sample.getSpace();
                        docFile = createNextDocFile(docDirectory, space != null ? space.getCode() : SHARED_SAMPLES_DIRECTORY, null, null,
                                null, null, null, null, null, null);
                    }
                }

                mkdir(docFile);
            }

            if (entity instanceof Sample)
            {
                final Sample sample = (Sample) entity;
                final Experiment experiment = sample.getExperiment();
                final Sample container = sample.getContainer();

                createDocFilesForEntity(sessionToken, docDirectory, exportFields, sample,
                        getSpaceCode(sample), getProjectCode(sample), experiment != null ? experiment.getCode() : null,
                        experiment != null ? getEntityName(experiment) : null, container != null ? container.getCode() : null, sample.getCode(),
                        getEntityName(sample), null, exportFormats);
            }
        }
    }

    private void exportDataSetsDoc(final String sessionToken, final File docDirectory, final EntitiesVo entitiesVo,
            final Map<String, Map<String, List<Map<String, String>>>> exportFields, final Set<ExportFormat> exportFormats) throws IOException
    {
        createFilesAndFoldersForDataSetsOfEntities(sessionToken, docDirectory, entitiesVo.getDataSets(), exportFields, exportFormats);
    }

    private void createFilesAndFoldersForDataSetsOfEntities(final String sessionToken, final File docDirectory,
            final Collection<?> entities, final Map<String, Map<String, List<Map<String, String>>>> exportFields,
            final Set<ExportFormat> exportFormats) throws IOException
    {
        for (final Object entity : entities)
        {
            if (entity instanceof DataSet)
            {
                final DataSet dataSet = (DataSet) entity;
                final Sample sample = dataSet.getSample();
                final Sample container = sample != null ? sample.getContainer() : null;
                final Experiment experiment = sample != null ? sample.getExperiment() : dataSet.getExperiment();

                createDocFilesForEntity(sessionToken, docDirectory, exportFields, dataSet,
                        getSpaceCode(entity), getProjectCode(entity), experiment != null ? experiment.getCode() : null,
                        experiment != null ? getEntityName(experiment) : null, container != null ? container.getCode() : null,
                        sample != null ? sample.getCode() : null, sample != null ? getEntityName(sample) : null, dataSet.getCode(), exportFormats);
            }
        }
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
                final Project project = sample.getProject();
                if (experiment != null)
                {
                    return experiment.getProject().getSpace().getCode();
                } else if (project != null)
                {
                    return project.getSpace().getCode();
                } else
                {
                    return null;
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

    private static void writeInChunks(final OutputStream os, final InputStream is) throws IOException
    {
        final byte[] buffer = new byte[BUFFER_SIZE];
        int length;
        while ((length = is.read(buffer)) > 0)
        {
            os.write(buffer, 0, length);
        }
        os.flush();
    }

    private static File createNextDocFile(final File docDirectory, final String spaceCode, final String projectCode, final String experimentCode,
            final String experimentName, final String containerCode, final String sampleCode, final String sampleName, final String dataSetCode,
            final String extension)
    {
        return new File(docDirectory, getNextDocDirectoryName(spaceCode, projectCode, experimentCode, experimentName, containerCode, sampleCode,
                sampleName, dataSetCode, extension));
    }

    private void createDocFilesForEntity(final String sessionToken, final File docDirectory,
            final Map<String, Map<String, List<Map<String, String>>>> exportFields,
            final ICodeHolder entity, final String spaceCode, final String projectCode, final String experimentCode,
            final String experimentName, final String containerCode, final String sampleCode, final String sampleName, final String dataSetCode,
            final Set<ExportFormat> exportFormats) throws IOException
    {
        final boolean hasHtmlFormat = exportFormats.contains(ExportFormat.HTML);
        final boolean hasPdfFormat = exportFormats.contains(ExportFormat.PDF);
        final Map<String, List<Map<String, String>>> entityTypeExportFieldsMap = getEntityTypeExportFieldsMap(exportFields, PROJECT);
        final String html = getHtml(sessionToken, entity, entityTypeExportFieldsMap);
        final byte[] htmlBytes = html.getBytes(StandardCharsets.UTF_8);

        if (hasHtmlFormat)
        {
            final File htmlFile = createNextDocFile(docDirectory, spaceCode, projectCode, experimentCode, experimentName, containerCode, sampleCode,
                    sampleName, dataSetCode, HTML_EXTENSION);
            try (final BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(htmlFile), BUFFER_SIZE))
            {
                writeInChunks(bos, htmlBytes);
                bos.flush();
            }
        }

        if (hasPdfFormat)
        {
            final File pdfFile = createNextDocFile(docDirectory, spaceCode, projectCode, experimentCode, experimentName, containerCode, sampleCode,
                    sampleName, dataSetCode, PDF_EXTENSION);
            try (final BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(pdfFile), BUFFER_SIZE))
            {
                final PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.withHtmlContent(html, null);
                builder.toStream(bos);
                builder.run();
            }
        }
    }

    static String getNextDocDirectoryName(final String spaceCode, final String projectCode, final String experimentCode, final String experimentName,
            final String containerCode, final String sampleCode, final String sampleName, final String dataSetCode, final String extension)
    {
        final StringBuilder entryBuilder = new StringBuilder();

        if (spaceCode == null && (projectCode != null || experimentCode != null || dataSetCode != null || (sampleCode == null && extension != null)))
        {
            throw new IllegalArgumentException();
        } else if (spaceCode != null)
        {
            entryBuilder.append(spaceCode);
        }

        if (projectCode != null)
        {
            entryBuilder.append('/').append(projectCode);
            if (experimentCode != null)
            {
                entryBuilder.append('/');
                addFullEntityName(entryBuilder, null, experimentCode, experimentName);

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
            if (spaceCode != null)
            {
                entryBuilder.append('/');
            }
            addFullEntityName(entryBuilder, containerCode, sampleCode, sampleName);

            if (dataSetCode != null)
            {
                // Sample data set
                entryBuilder.append('/').append(dataSetCode);
            }
        }

        entryBuilder.append(extension != null ? extension : '/');
        return entryBuilder.toString();
    }

    private static void createNextDataFile(final String sessionToken, final char prefix, final String spaceCode, final String projectCode,
            final String containerCode, final String entityCode, final String dataSetTypeCode, final String dataSetCode,
            final String dataSetName, final DataSetFileDownload dataSetFileDownload) throws IOException
    {
        final ISessionWorkspaceProvider sessionWorkspaceProvider = CommonServiceProvider.getSessionWorkspaceProvider();
        final File sessionWorkspaceDirectory = sessionWorkspaceProvider.getSessionWorkspace(sessionToken).getCanonicalFile();

        final DataSetFile dataSetFile = dataSetFileDownload.getDataSetFile();
        final String filePath = dataSetFile.getPath();
        final boolean isDirectory = dataSetFile.isDirectory();

        final File dataDirectory = new File(sessionWorkspaceDirectory, DATA_DIRECTORY);
        mkdir(dataDirectory);

        final File dataSetFsEntry = new File(dataDirectory, getDataDirectoryName(prefix, spaceCode, projectCode, containerCode, entityCode,
                dataSetTypeCode, dataSetCode, dataSetName, filePath) + (isDirectory ? "/" : ""));

        final File dataSubdirectory = dataSetFsEntry.getParentFile();
        mkdir(dataSubdirectory);

        if (!isDirectory)
        {
            try (
                    final InputStream is = dataSetFileDownload.getInputStream();
                    final BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(dataSetFsEntry))
            )
            {
                writeInChunks(os, is);
            }
        } else
        {
            mkdir(dataSetFsEntry);
        }
    }

    static String getDataDirectoryName(final char prefix, final String spaceCode, final String projectCode,
            final String containerCode, final String entityCode, final String dataSetTypeCode,
            final String dataSetCode, final String dataSetName, final String fileName)
    {
        if (prefix != 'O' && prefix != 'E')
        {
            throw new IllegalArgumentException(String.format("Only 'O' and 'E' can be used as prefix got '%c' instead.", prefix));
        }

        if (containerCode != null && prefix != 'O')
        {
            throw new IllegalArgumentException("Only objects can have containers.");
        }

        final StringBuilder entryBuilder = new StringBuilder(String.valueOf(prefix));

        if (spaceCode != null)
        {
            entryBuilder.append('+').append(spaceCode);
        } else if (prefix == 'E')
        {
            throw new IllegalArgumentException("Space code cannot be null for experiments.");
        } else if (projectCode != null)
        {
            throw new IllegalArgumentException("If space code is null project code should be also null.");
        }

        if (projectCode != null)
        {
            entryBuilder.append('+').append(projectCode);
        } else if (prefix == 'E')
        {
            throw new IllegalArgumentException("Project code cannot be null for experiments.");
        }

        if (entityCode != null)
        {
            entryBuilder.append('+');
            addFullEntityCode(entryBuilder, containerCode, entityCode);
        } else
        {
            throw new IllegalArgumentException("Entity code is mandatory");
        }

        if (dataSetTypeCode != null)
        {
            entryBuilder.append('+').append(dataSetTypeCode);
        } else
        {
            throw new IllegalArgumentException("Data set type code is mandatory");
        }

        if (dataSetCode != null)
        {
            entryBuilder.append('+');
            addFullEntityName(entryBuilder, null, dataSetCode, dataSetName);
        } else
        {
            throw new IllegalArgumentException("Data set code is mandatory");
        }

        if (fileName != null)
        {
            entryBuilder.append('/').append(fileName);
        }

        return entryBuilder.toString();
    }

    private static void addFullEntityName(final StringBuilder entryBuilder, final String containerCode, final String entityCode,
            final String entityName)
    {
        if (entityName == null || entityName.isEmpty())
        {
            addFullEntityCode(entryBuilder, containerCode, entityCode);
        } else
        {
            entryBuilder.append(entityName).append(" (");
            addFullEntityCode(entryBuilder, containerCode, entityCode);
            entryBuilder.append(")");
        }
    }

    private static void addFullEntityCode(final StringBuilder entryBuilder, final String containerCode, final String entityCode)
    {
        if (containerCode != null)
        {
            entryBuilder.append(containerCode).append('*');
        }

        entryBuilder.append(entityCode);
    }

    private static String getEntityName(final IPropertiesHolder entity)
    {
        try
        {
            return escapeUnsafeCharacters(entity.getVarcharProperty(NAME_PROPERTY_NAME));
        } catch (final NotFetchedException e)
        {
            return null;
        }
    }

    static String escapeUnsafeCharacters(final String name)
    {
        return name != null ? name.replaceAll(UNSAFE_CHARACTERS_REGEXP, "_") : null;
    }

    private static void exportXls(final ZipOutputStream zos, final OutputStream os, final XLSExport.PrepareWorkbookResult xlsExportResult,
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
            os.write(script.getValue().getBytes());
            os.flush();
            zos.closeEntry();
        }

        zos.putNextEntry(new ZipEntry(String.format("%s/%s", XLSX_DIRECTORY, METADATA_FILE_NAME)));
        wb.write(os);
        zos.closeEntry();

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
            final ObjectIdentifier identifier = ((IIdentifierHolder) entityObj).getIdentifier();
            if (identifier != null)
            {
                documentBuilder.addProperty("Identifier", identifier.getIdentifier());
            }
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
                    final String name = getEntityName((IPropertiesHolder) parent);
                    documentBuilder.addParagraph(relCodeName + (name != null ? " (" + properties.get("NAME") + ")" : ""));
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
                    final String name = getEntityName((IPropertiesHolder) child);
                    documentBuilder.addParagraph(relCodeName + (name != null ? " (" + properties.get("NAME") + ")" : ""));
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

    /**
     * Safely tries to create a directory if it does not exist. If it could not be created throws an exception.
     *
     * @param dir the directory to be created.
     */
    private static void mkdir(final File dir)
    {
        if (!dir.isDirectory())
        {
            final boolean created = dir.mkdir();
            if (!created)
            {
                throw new RuntimeException(String.format("Cannot create directory '%s'.", dir.getPath()));
            }
        }
    }

    private static class EntitiesVo
    {

        private final String sessionToken;

        private final Map<ExportableKind, List<String>> groupedExportablePermIds;

        private Collection<Space> spaces;

        private Collection<Project> projects;

        private Collection<Experiment> experiments;

        private Collection<Sample> samples;

        private Collection<DataSet> dataSets;

        private EntitiesVo(final String sessionToken, final List<ExportablePermId> exportablePermIds)
        {
            this.sessionToken = sessionToken;
            groupedExportablePermIds = getGroupedExportablePermIds(exportablePermIds);
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
            if (spaces == null)
            {
                spaces = EntitiesFinder.getSpaces(sessionToken, groupedExportablePermIds.getOrDefault(ExportableKind.SPACE, List.of()));
            }
            return spaces;
        }

        public Collection<Project> getProjects()
        {
            if (projects == null)
            {
                projects = EntitiesFinder.getProjects(sessionToken, groupedExportablePermIds.getOrDefault(ExportableKind.PROJECT, List.of()));
            }

            return projects;
        }

        public Collection<Experiment> getExperiments()
        {
            if (experiments == null)
            {
                experiments = EntitiesFinder.getExperiments(sessionToken, groupedExportablePermIds.getOrDefault(ExportableKind.EXPERIMENT, List.of()));
            }
            return experiments;
        }

        public Collection<Sample> getSamples()
        {
            if (samples == null)
            {
                samples = EntitiesFinder.getSamples(sessionToken, groupedExportablePermIds.getOrDefault(ExportableKind.SAMPLE, List.of()));
            }
            return samples;
        }

        public Collection<DataSet> getDataSets()
        {
            if (dataSets == null)
            {
                dataSets = EntitiesFinder.getDataSets(sessionToken, groupedExportablePermIds.getOrDefault(DATASET, List.of()));
            }
            return dataSets;
        }

    }

}
