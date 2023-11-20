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

import static ch.ethz.sis.openbis.generic.server.FileServiceServlet.REPO_PATH_KEY;
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.IDeletionId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.ExportResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.ExportData;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.ExportablePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.IExportableFields;
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
import ch.ethz.sis.openbis.generic.server.xls.export.XLSExportTest;
import ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer;

public class ExportTest extends AbstractTest
{

    private static final String EXPORT_DATA_PROVIDER = "xlsExportData";

    private static final String PLAIN_TEXT_VALUE = "Rich format test";

    private static final String RICH_TEXT_VALUE = String.format("<b>%s</b>", PLAIN_TEXT_VALUE);

    private static final String XLS_EXPORT_RESOURCES_PATH = "ch/ethz/sis/openbis/systemtest/asapi/v3/test_files/export/";

    private static final String RICH_TEXT_WITH_IMAGE_VALUE = String.format("<b>Here goes the image: <figure class=\"image\">\n"
            + "<img src=\"%snature.png\"/></figure></b>", XLS_EXPORT_RESOURCES_PATH);

    private static final String RICH_TEXT_SAMPLE_CODE = "RICH_TEXT";

    private static final String JAVA_FOLDER_PATH = "./sourceTest/java/";

    protected String sessionToken;

    private PropertyTypePermId richTextPropertyTypePermId;

    private PropertyTypePermId richTextWithImagePropertyTypePermId;

    private PropertyTypePermId richTextWithSpreadsheetPropertyTypePermId;

    private EntityTypePermId sampleTypePermId;

    private SamplePermId samplePermId;

    @Resource(name = ExposablePropertyPlaceholderConfigurer.PROPERTY_CONFIGURER_BEAN_NAME)
    private ExposablePropertyPlaceholderConfigurer configurer;

    @DataProvider
    protected Object[][] xlsExportData()
    {
        return ch.ethz.sis.openbis.systemtest.asapi.v3.ExportData.EXPORT_DATA;
    }

    @BeforeClass
    public void beforeClass()
    {
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
        sampleTypePermId = v3api.createSampleTypes(sessionToken, List.of(sampleTypeCreation)).get(0);

        final SampleCreation richTextSampleCreation = new SampleCreation();
        richTextSampleCreation.setSpaceId(new SpacePermId("TEST-SPACE"));
        richTextSampleCreation.setCode(RICH_TEXT_SAMPLE_CODE);
        richTextSampleCreation.setTypeId(sampleTypePermId);
        richTextSampleCreation.setProperty(RICH_TEXT_PROPERTY_NAME, RICH_TEXT_VALUE);
        richTextSampleCreation.setProperty(RICH_TEXT_WITH_IMAGE_PROPERTY_NAME, RICH_TEXT_WITH_IMAGE_VALUE);
        richTextSampleCreation.setProperty(RICH_TEXT_WITH_SPREADSHEET_PROPERTY_NAME, SpreadsheetData.BASE64_SPREADSHEET_DATA);

        samplePermId = v3api.createSamples(sessionToken, List.of(richTextSampleCreation)).get(0);

        v3api.logout(sessionToken);
    }

    @AfterClass
    public void afterClass()
    {
        sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleDeletionOptions sampleDeletionOptions = new SampleDeletionOptions();
        sampleDeletionOptions.setReason("Test");
        final IDeletionId deletionId = v3api.deleteSamples(sessionToken, List.of(samplePermId), sampleDeletionOptions);
        v3api.confirmDeletions(systemSessionToken, List.of(deletionId));

        final SampleTypeDeletionOptions sampleTypeDeletionOptions = new SampleTypeDeletionOptions();
        sampleTypeDeletionOptions.setReason("Test");
        v3api.deleteSampleTypes(sessionToken, List.of(sampleTypePermId), sampleTypeDeletionOptions);

        final PropertyTypeDeletionOptions propertyTypeDeletionOptions = new PropertyTypeDeletionOptions();
        propertyTypeDeletionOptions.setReason("Test");
        v3api.deletePropertyTypes(sessionToken, List.of(richTextPropertyTypePermId), propertyTypeDeletionOptions);

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

    @Test(dataProvider = EXPORT_DATA_PROVIDER)
    public void testDataExport(final String expectedResultFileName, final Set<ExportFormat> formats, final List<ExportablePermId> permIds,
            final IExportableFields fields, final XlsTextFormat xlsTextFormat, final boolean withReferredTypes,
            final boolean withImportCompatibility) throws Exception
    {
        processPermIds(permIds);

        final ExportData exportData = new ExportData(permIds, fields);
        final ExportOptions exportOptions = new ExportOptions(formats, xlsTextFormat, withReferredTypes, withImportCompatibility);
        final ExportResult exportResult = v3api.executeExport(sessionToken, exportData, exportOptions);

        compareFiles(XLS_EXPORT_RESOURCES_PATH + expectedResultFileName, exportResult.getDownloadURL());
    }

    /**
     * Searches for ExportablePermIds with null perm IDs, which should indicate that it should be replaced with {@link #samplePermId}.
     *
     * @param permIds the list of ExportablePermId values to be processed.
     */
    private void processPermIds(final List<ExportablePermId> permIds)
    {
        permIds.forEach(exportablePermId ->
        {
            if (exportablePermId.getPermId() == null)
            {
                exportablePermId.setPermId(samplePermId);
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
        } else
        {
            throw new IllegalArgumentException(String.format("Expected ('%s') and actual ('%s') files have different formats.",
                    expectedResultFilePath, actualResultFilePath));
        }
    }

    private void compareXlsxFiles(final String expectedResultFilePath, final String actualResultFilePath) throws IOException
    {
        final InputStream expectedResultStream = getClass().getClassLoader().getResourceAsStream(expectedResultFilePath);
        if (expectedResultStream == null)
        {
            throw new IllegalArgumentException(String.format("Expected result file '%s' not found.", expectedResultFilePath));
        }
        compareXlsxStreams(expectedResultStream, new FileInputStream(getActualFile(actualResultFilePath)));
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
                } else if (!expectedZipEntry.endsWith(PDF_EXTENSION)) // We ignore PDF files in comparison
                {
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
        final File sessionWorkspaceSubfolder = new File(sessionWorkspace, intermediatePath);
        final File[] files = sessionWorkspaceSubfolder.listFiles((FilenameFilter) new NameFileFilter(actualResultFileName));

        assertNotNull(files);
        assertEquals(1, files.length, String.format("Session workspace should contain only one file with the download URL '%s'.",
                actualResultFilePath));

        return files[0];
    }

}
