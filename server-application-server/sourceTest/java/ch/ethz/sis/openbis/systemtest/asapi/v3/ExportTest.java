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

package ch.ethz.sis.openbis.systemtest.asapi.v3;

import static ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.Attribute.CODE;
import static ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.Attribute.DESCRIPTION;
import static ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.Attribute.IDENTIFIER;
import static ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.Attribute.REGISTRATOR;
import static ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.Attribute.SPACE;
import static ch.ethz.sis.openbis.generic.server.FileServiceServlet.REPO_PATH_KEY;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.executor.exporter.ExportExecutor.HTML_EXTENSION;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.executor.exporter.ExportExecutor.JSON_EXTENSION;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.executor.exporter.ExportExecutor.METADATA_FILE_NAME;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.executor.exporter.ExportExecutor.PDF_EXTENSION;
import static ch.ethz.sis.openbis.generic.server.xls.export.XLSExport.XLSX_EXTENSION;
import static ch.ethz.sis.openbis.generic.server.xls.export.XLSExport.ZIP_EXTENSION;
import static ch.ethz.sis.openbis.systemtest.asapi.v3.ExportData.RICH_TEXT_PROPERTY_NAME;
import static ch.ethz.sis.openbis.systemtest.asapi.v3.ExportData.RICH_TEXT_WITH_IMAGE_PROPERTY_NAME;
import static ch.ethz.sis.openbis.systemtest.asapi.v3.ExportData.RICH_TEXT_WITH_SPREADSHEET_PROPERTY_NAME;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.annotation.Resource;

import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.IDeletionId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.ExportResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.AllFields;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.ExportData;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.ExportableKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.ExportablePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.IExportableFields;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.SelectedFields;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.options.ExportFormat;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.options.ExportOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.options.XlsTextFormat;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyAssignmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.delete.PropertyTypeDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.delete.SampleDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.delete.SampleTypeDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.DataSetFile;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.download.DataSetFileDownloadOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.fetchoptions.DataSetFileFetchOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.id.DataSetFilePermId;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.id.IDataSetFileId;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.search.DataSetFileSearchCriteria;
import ch.ethz.sis.openbis.generic.server.xls.export.XLSExportTest;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;

public class ExportTest extends AbstractTest
{

    private static final String EXPORT_DATA_PROVIDER = "exportData";

    private static final String PLAIN_TEXT_VALUE = "Rich format test";

    private static final String RICH_TEXT_VALUE = String.format("<b>%s</b>", PLAIN_TEXT_VALUE);

    private static final String XLS_EXPORT_RESOURCES_PATH = "ch/ethz/sis/openbis/systemtest/asapi/v3/test_files/export/";

    private static final String RICH_TEXT_WITH_IMAGE_VALUE = String.format("<b>Here goes the image: <figure class=\"image\">\n"
            + "<img src=\"%snature.png\"/></figure></b>", XLS_EXPORT_RESOURCES_PATH);

    private static final String RICH_TEXT_SAMPLE_CODE = "RICH_TEXT";

    private static final String BIG_CELL_SAMPLE_CODE = "BIG_CELL";

    private static final String JAVA_FOLDER_PATH = "./sourceTest/java/";

    protected String sessionToken;

    private PropertyTypePermId richTextPropertyTypePermId;

    private PropertyTypePermId richTextWithImagePropertyTypePermId;

    private PropertyTypePermId richTextWithSpreadsheetPropertyTypePermId;

    private EntityTypePermId richTextSampleTypePermId;

    private EntityTypePermId bigCellSampleTypePermId;

    private SamplePermId richTextSamplePermId;

    private SamplePermId bigCellSamplePermId;

    @Resource(name = ExposablePropertyPlaceholderConfigurer.PROPERTY_CONFIGURER_BEAN_NAME)
    private ExposablePropertyPlaceholderConfigurer configurer;

    private Mockery mockery;

    private IDataStoreServerApi v3Dss;

    @DataProvider
    protected Object[][] exportData()
    {
        return ch.ethz.sis.openbis.systemtest.asapi.v3.ExportData.EXPORT_DATA;
    }

    @BeforeClass
    public void beforeClass()
    {
        mockery = new Mockery();

        configurer.getResolvedProps().setProperty(REPO_PATH_KEY, JAVA_FOLDER_PATH);

        sessionToken = v3api.login(TEST_USER, PASSWORD);

        final PropertyTypeCreation richTextPropertyTypeCreation = new PropertyTypeCreation();
        richTextPropertyTypeCreation.setCode(RICH_TEXT_PROPERTY_NAME);
        richTextPropertyTypeCreation.setLabel("Multiline");
        richTextPropertyTypeCreation.setDescription("Property type with multiline text value");
        richTextPropertyTypeCreation.setDataType(DataType.MULTILINE_VARCHAR);

        final PropertyTypeCreation richTextWithImagePropertyTypeCreation = new PropertyTypeCreation();
        richTextWithImagePropertyTypeCreation.setCode(RICH_TEXT_WITH_IMAGE_PROPERTY_NAME);
        richTextWithImagePropertyTypeCreation.setLabel("Image");
        richTextWithImagePropertyTypeCreation.setDescription("Property type with image");
        richTextWithImagePropertyTypeCreation.setMetaData(Map.of("custom_widget", "Word Processor"));
        richTextWithImagePropertyTypeCreation.setDataType(DataType.MULTILINE_VARCHAR);

        final PropertyTypeCreation richTextWithSpreadsheetPropertyTypeCreation = new PropertyTypeCreation();
        richTextWithSpreadsheetPropertyTypeCreation.setCode(RICH_TEXT_WITH_SPREADSHEET_PROPERTY_NAME);
        richTextWithSpreadsheetPropertyTypeCreation.setLabel("Spreadsheet");
        richTextWithSpreadsheetPropertyTypeCreation.setDescription("Property type with spreadsheet");
        richTextWithSpreadsheetPropertyTypeCreation.setMetaData(Map.of("custom_widget", "Spreadsheet"));
        richTextWithSpreadsheetPropertyTypeCreation.setDataType(DataType.XML);

        final List<PropertyTypePermId> propertyTypes =
                v3api.createPropertyTypes(sessionToken, List.of(richTextPropertyTypeCreation, richTextWithImagePropertyTypeCreation,
                        richTextWithSpreadsheetPropertyTypeCreation));
        richTextPropertyTypePermId = propertyTypes.get(0);
        richTextWithImagePropertyTypePermId = propertyTypes.get(1);
        richTextWithSpreadsheetPropertyTypePermId = propertyTypes.get(2);

        final SampleCreation richTextSampleCreation = getRichTextSampleCreation();

        final SampleCreation bigCellSampleCreation = getBigCellSampleCreation();

        final List<SamplePermId> samplePermsIds = v3api.createSamples(sessionToken, List.of(richTextSampleCreation, bigCellSampleCreation));
        richTextSamplePermId = samplePermsIds.get(0);
        bigCellSamplePermId = samplePermsIds.get(1);

        registerDss();

        v3api.logout(sessionToken);
    }

    private SampleCreation getRichTextSampleCreation()
    {
        final SampleTypeCreation sampleTypeCreation = new SampleTypeCreation();
        sampleTypeCreation.setCode("MULTI_LINE_VALUE_SAMPLE_TYPE");

        final PropertyAssignmentCreation richTextPropertyAssignmentCreation = new PropertyAssignmentCreation();
        richTextPropertyAssignmentCreation.setPropertyTypeId(richTextPropertyTypePermId);

        final PropertyAssignmentCreation richTextWithImagePropertyAssignmentCreation = new PropertyAssignmentCreation();
        richTextWithImagePropertyAssignmentCreation.setPropertyTypeId(richTextWithImagePropertyTypePermId);

        final PropertyAssignmentCreation richTextWithSpreadsheetPropertyAssignmentCreation = new PropertyAssignmentCreation();
        richTextWithSpreadsheetPropertyAssignmentCreation.setPropertyTypeId(richTextWithSpreadsheetPropertyTypePermId);

        sampleTypeCreation.setPropertyAssignments(List.of(richTextPropertyAssignmentCreation, richTextWithImagePropertyAssignmentCreation,
                richTextWithSpreadsheetPropertyAssignmentCreation));
        richTextSampleTypePermId = v3api.createSampleTypes(sessionToken, List.of(sampleTypeCreation)).get(0);

        final SampleCreation richTextSampleCreation = new SampleCreation();
        richTextSampleCreation.setSpaceId(new SpacePermId("TEST-SPACE"));
        richTextSampleCreation.setCode(RICH_TEXT_SAMPLE_CODE);
        richTextSampleCreation.setTypeId(richTextSampleTypePermId);
        richTextSampleCreation.setProperty(RICH_TEXT_PROPERTY_NAME, RICH_TEXT_VALUE);
        richTextSampleCreation.setProperty(RICH_TEXT_WITH_IMAGE_PROPERTY_NAME, RICH_TEXT_WITH_IMAGE_VALUE);
        richTextSampleCreation.setProperty(RICH_TEXT_WITH_SPREADSHEET_PROPERTY_NAME, SpreadsheetData.BASE64_SPREADSHEET_DATA);

        return richTextSampleCreation;
    }

    private SampleCreation getBigCellSampleCreation()
    {
        final SampleTypeCreation sampleTypeCreation = new SampleTypeCreation();
        sampleTypeCreation.setCode("BIG_CELL_SAMPLE_TYPE");

        // Big cell reuses the rich text property type
        final PropertyAssignmentCreation bigCellPropertyAssignmentCreation = new PropertyAssignmentCreation();
        bigCellPropertyAssignmentCreation.setPropertyTypeId(richTextPropertyTypePermId);

        sampleTypeCreation.setPropertyAssignments(List.of(bigCellPropertyAssignmentCreation));
        bigCellSampleTypePermId = v3api.createSampleTypes(sessionToken, List.of(sampleTypeCreation)).get(0);

        final SampleCreation bigCellSampleCreation = new SampleCreation();
        bigCellSampleCreation.setSpaceId(new SpacePermId("TEST-SPACE"));
        bigCellSampleCreation.setCode(BIG_CELL_SAMPLE_CODE);
        bigCellSampleCreation.setTypeId(bigCellSampleTypePermId);
        bigCellSampleCreation.setProperty(RICH_TEXT_PROPERTY_NAME, getResourceFileContent(XLS_EXPORT_RESOURCES_PATH + "lorem-ipsum.txt"));

        return bigCellSampleCreation;
    }

    @SuppressWarnings("SameParameterValue")
    private static String getResourceFileContent(final String filePath)
    {
        try (final InputStream exampleTextInputStream = ExportTest.class.getClassLoader().getResourceAsStream(filePath))
        {
            Objects.requireNonNull(exampleTextInputStream);
            return new String(exampleTextInputStream.readAllBytes());
        } catch (final IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void registerDss()
    {
        v3Dss = mockery.mock(IDataStoreServerApi.class);
        CommonServiceProvider.setDataStoreServerApi(v3Dss);
    }


    @AfterClass
    public void afterClass()
    {
        sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleDeletionOptions sampleDeletionOptions = new SampleDeletionOptions();
        sampleDeletionOptions.setReason("Test");
        final IDeletionId deletionId = v3api.deleteSamples(sessionToken, List.of(richTextSamplePermId, bigCellSamplePermId), sampleDeletionOptions);
        v3api.confirmDeletions(systemSessionToken, List.of(deletionId));

        final SampleTypeDeletionOptions sampleTypeDeletionOptions = new SampleTypeDeletionOptions();
        sampleTypeDeletionOptions.setReason("Test");
        v3api.deleteSampleTypes(sessionToken, List.of(richTextSampleTypePermId, bigCellSampleTypePermId), sampleTypeDeletionOptions);

        final PropertyTypeDeletionOptions propertyTypeDeletionOptions = new PropertyTypeDeletionOptions();
        propertyTypeDeletionOptions.setReason("Test");
        v3api.deletePropertyTypes(sessionToken,
                List.of(richTextPropertyTypePermId, richTextWithImagePropertyTypePermId, richTextWithSpreadsheetPropertyTypePermId),
                propertyTypeDeletionOptions);

        v3api.logout(sessionToken);
    }

    @BeforeMethod
    public void beforeMethod()
    {
        sessionToken = v3api.login(TEST_USER, PASSWORD);
    }

    @AfterMethod
    public void afterMethod()
    {
        v3api.logout(sessionToken);
    }

    @SuppressWarnings({ "unchecked", "resource" })
    @Test(dataProvider = EXPORT_DATA_PROVIDER)
    public void testDataExport(final String expectedResultFileName, final Set<ExportFormat> formats, final List<ExportablePermId> permIds,
            final IExportableFields fields, final XlsTextFormat xlsTextFormat, final boolean withReferredTypes,
            final boolean withImportCompatibility, final boolean zipSingleFiles) throws Exception
    {
        processPermIds(permIds);

        if (formats.contains(ExportFormat.DATA))
        {
            // export-sample-data.zip refers to a test sample with more 2 datasets
            final boolean with2FileContents = expectedResultFileName.startsWith("export-sample-data");

            final String fileContent1 = "This is some test data.";
            final DataSetFile dataSetFile1 = createDataSetFile("default/data1.txt", fileContent1.length());
            final SearchResult<DataSetFile> results1 = new SearchResult<>(List.of(dataSetFile1), 1);
            final InputStream is1 = objectAndDataToStream(dataSetFile1, fileContent1);

            final DataSetFile dataSetFile2;
            final SearchResult<DataSetFile> results2;
            final InputStream is2;
            if (with2FileContents)
            {
                final String fileContent2 = "This is some other test data.";
                dataSetFile2 = createDataSetFile("my-folder/data2.txt", fileContent2.length());
                results2 = new SearchResult<>(List.of(dataSetFile2), 1);
                is2 = objectAndDataToStream(dataSetFile2, fileContent2);
            } else
            {
                dataSetFile2 = null;
                results2 = null;
                is2 = null;
            }

            mockery.checking(new Expectations()
            {{
                atLeast(1).of(v3Dss).searchFiles(with(equal(sessionToken)), with(any(DataSetFileSearchCriteria.class)),
                        with(any(DataSetFileFetchOptions.class)));
                if (with2FileContents)
                {
                    will(onConsecutiveCalls(returnValue(results1), returnValue(results2), returnValue(results1), returnValue(results2)));
                } else
                {
                    will(onConsecutiveCalls(returnValue(results1), returnValue(results1)));
                }

                atLeast(1).of(v3Dss).downloadFiles(with(equal(sessionToken)), with(equal(List.<IDataSetFileId>of(dataSetFile1.getPermId()))),
                        with(any(DataSetFileDownloadOptions.class)));
                will(returnValue(is1));

                if (with2FileContents)
                {
                    atLeast(1).of(v3Dss).downloadFiles(with(equal(sessionToken)), with(equal(List.<IDataSetFileId>of(dataSetFile2.getPermId()))),
                            with(any(DataSetFileDownloadOptions.class)));
                    will(returnValue(is2));
                }
            }});
        }

        final ExportData exportData = new ExportData(permIds, fields);
        final ExportOptions exportOptions = new ExportOptions(formats, xlsTextFormat, withReferredTypes, withImportCompatibility, zipSingleFiles);
        final ExportResult exportResult = v3api.executeExport(sessionToken, exportData, exportOptions);

        compareFiles(XLS_EXPORT_RESOURCES_PATH + expectedResultFileName, getBareFileName(exportResult.getDownloadURL()));
        mockery.assertIsSatisfied();
    }

    @Test(expectedExceptions = UserFailureException.class,
            expectedExceptionsMessageRegExp = "Total data size 10485762 is larger than the data limit 10485760\\..*")
    public void testTooLargeDataExport()
    {
        final Set<ExportFormat> formats = EnumSet.of(ExportFormat.DATA);
        final List<ExportablePermId> permIds = List.of(new ExportablePermId(ExportableKind.SAMPLE, new SamplePermId("200902091225616-1027")));
        final IExportableFields fields = new AllFields();
        final XlsTextFormat xlsTextFormat = XlsTextFormat.PLAIN;
        final boolean withReferredTypes = true;
        final boolean withImportCompatibility = false;

        processPermIds(permIds);

        if (formats.contains(ExportFormat.DATA))
        {
            // The following test data are returned twice because the sample has 2 datasets.
            final DataSetFile dataSetFile1 = createDataSetFile("default/data1.txt", 5242881); // 1 byte over 5000MB
            final SearchResult<DataSetFile> results1 = new SearchResult<>(List.of(dataSetFile1), 1);

            mockery.checking(new Expectations()
            {{
                atLeast(1).of(v3Dss).searchFiles(with(equal(sessionToken)), with(any(DataSetFileSearchCriteria.class)),
                        with(any(DataSetFileFetchOptions.class)));
                will(onConsecutiveCalls(returnValue(results1), returnValue(results1)));

                atMost(0).of(v3Dss).downloadFiles(with(any(String.class)), with(any(List.class)),
                        with(any(DataSetFileDownloadOptions.class)));
            }});
        }

        final ExportData exportData = new ExportData(permIds, fields);
        final ExportOptions exportOptions = new ExportOptions(formats, xlsTextFormat, withReferredTypes, withImportCompatibility, true);
        v3api.executeExport(sessionToken, exportData, exportOptions);
    }

    @Test()
    public void testNotTooLargeDataExport()
    {
        final Set<ExportFormat> formats = EnumSet.of(ExportFormat.DATA);
        final List<ExportablePermId> permIds = List.of(new ExportablePermId(ExportableKind.SAMPLE, new SamplePermId("200902091225616-1027")));
        final IExportableFields fields = new AllFields();
        final XlsTextFormat xlsTextFormat = XlsTextFormat.PLAIN;
        final boolean withReferredTypes = true;
        final boolean withImportCompatibility = false;

        processPermIds(permIds);

        if (formats.contains(ExportFormat.DATA))
        {
            // The following test data are returned twice because the sample has 2 datasets.
            final DataSetFile dataSetFile1 = createDataSetFile("default/data1.txt", 5242880); // Exactly 5000MB
            final SearchResult<DataSetFile> results1 = new SearchResult<>(List.of(dataSetFile1), 1);

            mockery.checking(new Expectations()
            {{
                atLeast(1).of(v3Dss).searchFiles(with(equal(sessionToken)), with(any(DataSetFileSearchCriteria.class)),
                        with(any(DataSetFileFetchOptions.class)));
                will(onConsecutiveCalls(returnValue(results1), returnValue(results1), returnValue(results1), returnValue(results1)));

                atLeast(1).of(v3Dss).downloadFiles(with(equal(sessionToken)), with(equal(List.<IDataSetFileId>of(dataSetFile1.getPermId()))),
                        with(any(DataSetFileDownloadOptions.class)));
                will(returnValue(new InputStream()
                {
                    @Override
                    public int read()
                    {
                        return -1;
                    }

                }));
            }});
        }

        final ExportData exportData = new ExportData(permIds, fields);
        final ExportOptions exportOptions = new ExportOptions(formats, xlsTextFormat, withReferredTypes, withImportCompatibility, true);
        v3api.executeExport(sessionToken, exportData, exportOptions);

        mockery.assertIsSatisfied();
    }

    /**
     * Tests export of cells larger than 32k.
     */
    @Test
    public void testLargeCellXlsExport() throws IOException
    {
        final ExportData exportData = new ExportData(List.of(new ExportablePermId(ExportableKind.SAMPLE, bigCellSamplePermId)), new SelectedFields(
                List.of(REGISTRATOR, CODE, IDENTIFIER, SPACE, DESCRIPTION),
                List.of(richTextPropertyTypePermId)));
        final ExportOptions exportOptions = new ExportOptions(EnumSet.of(ExportFormat.XLSX), XlsTextFormat.RICH, false, false, false);
        final ExportResult exportResult = v3api.executeExport(sessionToken, exportData, exportOptions);

        compareFiles(XLS_EXPORT_RESOURCES_PATH + "export-large-cell.zip", getBareFileName(exportResult.getDownloadURL()));
    }

    private static String getBareFileName(final String url)
    {
        return url.substring(url.lastIndexOf("=") + 1);
    }

    private static DataSetFile createDataSetFile(final String filePath, final int fileLength)
    {
        final DataSetFilePermId dataSetFilePermId = new DataSetFilePermId(new DataSetPermId("20230904175944612-1"), filePath);
        final DataSetFile dataSetFile = new DataSetFile();
        dataSetFile.setPermId(dataSetFilePermId);
        dataSetFile.setFileLength(fileLength);
        dataSetFile.setPath(filePath);
        return dataSetFile;
    }

    public static InputStream objectAndDataToStream(final Object obj, final String data) throws IOException {
        final byte[] objectBytes = getObjectBytes(obj);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final DataOutputStream dos = new DataOutputStream(baos);

        // Write the size of the object
        dos.writeLong(objectBytes.length);

        // Write the object again
        dos.write(objectBytes);

        // Convert the string to bytes and write its size followed by the data
        final byte[] stringBytes = data.getBytes();
        final long stringSize = stringBytes.length;
        dos.writeLong(stringSize);
        dos.write(stringBytes);

        dos.close();

        // Return a ByteArrayInputStream containing the sequence
        return new ByteArrayInputStream(baos.toByteArray());
    }

    private static byte[] getObjectBytes(final Object obj) throws IOException
    {
        try (
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final ObjectOutputStream oos = new ObjectOutputStream(baos)
        )
        {
            oos.writeObject(obj);
            return baos.toByteArray();
        }
    }

    /**
     * Searches for ExportablePermIds with null perm IDs, which should indicate that it should be replaced with {@link #richTextSamplePermId}.
     *
     * @param permIds the list of ExportablePermId values to be processed.
     */
    private void processPermIds(final List<ExportablePermId> permIds)
    {
        permIds.forEach(exportablePermId ->
        {
            if (exportablePermId.getPermId() == null)
            {
                exportablePermId.setPermId(richTextSamplePermId);
            }
        });
    }

    private void compareFiles(final String expectedResultFilePath, final String actualResultFilePath) throws IOException
    {
        final URL expectedResultUrl = getClass().getClassLoader().getResource(expectedResultFilePath);
        assertNotNull(expectedResultUrl, String.format("Expected result file path produces null URL: %s.", expectedResultFilePath));

        final File expectedResultFile;
        try
        {
            expectedResultFile = new File(expectedResultUrl.toURI());
        } catch (final URISyntaxException e)
        {
            throw new RuntimeException(e);
        }
        assertTrue(expectedResultFile.exists(), String.format("Expected result file does not exist: %s.", expectedResultUrl.getPath()));

        if (expectedResultFile.isDirectory())
        {
            final File actualResultFile = getActualFile(actualResultFilePath);
            assertTrue(actualResultFile.exists(), String.format("Actual result file does not exist: %s.", actualResultFilePath));

            assertTrue(actualResultFile.isDirectory());
            compareDirectories(expectedResultFile, actualResultFile);
        } else if (expectedResultFilePath.endsWith(XLSX_EXTENSION) && actualResultFilePath.endsWith(XLSX_EXTENSION))
        {
            compareXlsxFiles(expectedResultFilePath, actualResultFilePath);
        } else if (expectedResultFilePath.endsWith(ZIP_EXTENSION) && actualResultFilePath.endsWith(ZIP_EXTENSION))
        {
            compareZipFiles(expectedResultFilePath, actualResultFilePath);
        } else if (expectedResultFilePath.endsWith(HTML_EXTENSION) && actualResultFilePath.endsWith(HTML_EXTENSION))
        {
            try (
                    final InputStream expectedResultInputStream = getClass().getClassLoader().getResourceAsStream(expectedResultFilePath);
                    final FileInputStream actualResultInputStream = new FileInputStream(getActualFile(actualResultFilePath));
            )
            {
                compareStreams(expectedResultInputStream, actualResultInputStream);
            }
        } else
        {
            throw new IllegalArgumentException(String.format("Expected ('%s') and actual ('%s') files have different formats.",
                    expectedResultFilePath, actualResultFilePath));
        }
    }

    private void compareXlsxFiles(final String expectedResultFilePath, final String actualResultFilePath) throws IOException
    {
        try(final InputStream expectedResultInputStream = getClass().getClassLoader().getResourceAsStream(expectedResultFilePath))
        {
            if (expectedResultInputStream == null)
            {
                throw new IllegalArgumentException(String.format("Expected result file '%s' not found.", expectedResultFilePath));
            }

            try (final FileInputStream actualResultInputStream = new FileInputStream(getActualFile(actualResultFilePath)))
            {
                compareXlsxStreams(expectedResultInputStream, actualResultInputStream);
            }
        }
    }

    private void compareZipFiles(final String expectedResultFilePath, final String actualResultFilePath) throws IOException
    {
        final URL expectedResultUrl = getClass().getClassLoader().getResource(expectedResultFilePath);
        assertNotNull(expectedResultUrl);

        final File actualFile = getActualFile(actualResultFilePath);

        try (
                final ZipFile extectedZipFile = new ZipFile(expectedResultUrl.getPath());
                final ZipFile actualZipFile = new ZipFile(actualFile);
        )
        {
            final Set<String> expectedZipEntries = extectedZipFile.stream().map(ZipEntry::getName).collect(Collectors.toSet());
            final Set<String> actualZipEntries = actualZipFile.stream().map(ZipEntry::getName).collect(Collectors.toSet());
            assertEquals(actualZipEntries, expectedZipEntries);

            for (final String expectedZipEntry : expectedZipEntries)
            {
                if (expectedZipEntry.endsWith(XLSX_EXTENSION))
                {
                    try (
                            final InputStream expectedInputStream = extectedZipFile.getInputStream(extectedZipFile.getEntry(expectedZipEntry));
                            final InputStream actualInputStream = actualZipFile.getInputStream(actualZipFile.getEntry(expectedZipEntry));
                    )
                    {
                        compareXlsxStreams(expectedInputStream, actualInputStream);
                    }
                } else if (!expectedZipEntry.endsWith(PDF_EXTENSION) && !expectedZipEntry.endsWith(JSON_EXTENSION))
                {
                    // We ignore PDF and JSON files in comparison
                    try (
                            final InputStream expectedInputStream = extectedZipFile.getInputStream(extectedZipFile.getEntry(expectedZipEntry));
                            final InputStream actualInputStream = actualZipFile.getInputStream(actualZipFile.getEntry(expectedZipEntry));
                    )
                    {
                        compareStreams(expectedInputStream, actualInputStream);
                    }
                }
            }
        }
    }

    private void compareDirectories(final File expectedDirectory, final File actualDirectory) throws IOException
    {
        final File[] expectedFiles = expectedDirectory.listFiles();
        final File[] actualFiles = actualDirectory.listFiles();
        final Set<String> expectedFileNames = Stream.of(Objects.requireNonNull(expectedFiles)).map(File::getName)
                .collect(Collectors.toSet());
        final Set<String> actualFileNames = Stream.of(Objects.requireNonNull(actualFiles)).map(File::getName)
                .collect(Collectors.toSet());

        assertEquals(actualFileNames, expectedFileNames);

        final Map<String, File> actualFileByName = Stream.of(Objects.requireNonNull(actualFiles))
                .collect(Collectors.toMap(File::getName, Function.identity()));

        for (final File expectedFile : expectedFiles)
        {
            final String expectedFileName = expectedFile.getName();
            final File actualFile = actualFileByName.get(expectedFileName);
            final boolean expectedFileIsDirectory = expectedFile.isDirectory();
            final boolean actualFileIsDirectory = actualFile.isDirectory();

            assertEquals(actualFileIsDirectory, expectedFileIsDirectory, "One of the compared files is a directory whereas the other is not.");
            if (expectedFileIsDirectory)
            {
                compareDirectories(expectedFile, actualFile);
            } else
            {
                try (
                        final InputStream expectedInputStream = new BufferedInputStream(new FileInputStream(expectedFile));
                        final InputStream actualInputStream = new BufferedInputStream(new FileInputStream(actualFile));
                )
                {
                    if (expectedFileName.equals(METADATA_FILE_NAME))
                    {
                        compareXlsxStreams(expectedInputStream, actualInputStream);
                    } else
                    {
                        compareStreams(expectedInputStream, actualInputStream);
                    }
                }
            }
        }
    }

    private static void compareXlsxStreams(final InputStream expectedResultInputStream, final InputStream actualResultInputStream) throws IOException
    {
        final Workbook expectedResult = new XSSFWorkbook(expectedResultInputStream);
        final Workbook actualResult = new XSSFWorkbook(actualResultInputStream);

        XLSExportTest.assertWorkbooksEqual(actualResult, expectedResult);
    }

    private static void compareStreams(final InputStream expectedResultInputStream, final InputStream actualResultInputStream) throws IOException
    {
        try (
                final BufferedReader expectedReader = new BufferedReader(new InputStreamReader(expectedResultInputStream, StandardCharsets.UTF_8));
                final BufferedReader actualReader = new BufferedReader(new InputStreamReader(actualResultInputStream, StandardCharsets.UTF_8));
        )
        {
            String expectedLine;
            while ((expectedLine = expectedReader.readLine()) != null)
            {
                final String actualLine = actualReader.readLine();
                assertEquals(actualLine, expectedLine);
            }
            assertNull(actualReader.readLine());
        }
    }

    private File getActualFile(final String actualResultFilePath)
    {
        final int pathSeparatorLocation = actualResultFilePath.lastIndexOf("/");
        final String intermediatePath = pathSeparatorLocation > 0 ? actualResultFilePath.substring(0, pathSeparatorLocation) : "";
        final String actualResultFileName = pathSeparatorLocation > 0
                ? actualResultFilePath.substring(pathSeparatorLocation + 1) : actualResultFilePath;
        final File sessionWorkspace = sessionWorkspaceProvider.getSessionWorkspace(sessionToken);
        final File sessionWorkspaceSubdirectory = new File(sessionWorkspace, intermediatePath);
        final File[] files = sessionWorkspaceSubdirectory.listFiles((FilenameFilter) new NameFileFilter(actualResultFileName));

        assertNotNull(files);
        assertEquals(1, files.length,
                String.format("Session workspace should contain only one file with the download URL '%s' but in contains the following files: %s",
                actualResultFilePath, Arrays.toString(files)));

        return files[0];
    }

}
