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

import static ch.ethz.sis.openbis.generic.server.asapi.v3.executor.exporter.ExportExecutor.METADATA_FILE_PREFIX;
import static ch.ethz.sis.openbis.generic.server.xls.export.XLSExport.XLSX_EXTENSION;
import static ch.ethz.sis.openbis.generic.server.xls.export.XLSExport.ZIP_EXTENSION;
import static ch.ethz.sis.openbis.systemtest.asapi.v3.ExportData.RICH_TEXT_PROPERTY_NAME;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
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
import ch.ethz.sis.openbis.generic.server.xls.export.XLSExportTest;
import ch.systemsx.cisd.openbis.generic.shared.ISessionWorkspaceProvider;

public class ExportTest extends AbstractTest
{

    private static final String EXPORT_DATA_PROVIDER = "xlsExportData";

    private static final String ZIPPED_EXPORT_FILE_NAME = METADATA_FILE_PREFIX + XLSX_EXTENSION;

    private static final String PLAIN_TEXT_VALUE = "Rich format test";

    private static final String RICH_TEXT_VALUE = String.format("<b>%s</b>", PLAIN_TEXT_VALUE);

    private static final String RICH_TEXT_SAMPLE_CODE = "RICH_TEXT";

    protected String sessionToken;

    private PropertyTypePermId propertyTypePermId;

    private EntityTypePermId sampleTypePermId;

    private SamplePermId samplePermId;

    @Autowired
    private ISessionWorkspaceProvider sessionWorkspaceProvider;

    @DataProvider
    protected Object[][] xlsExportData()
    {
        return ch.ethz.sis.openbis.systemtest.asapi.v3.ExportData.EXPORT_DATA;
    }

    @BeforeClass
    public void beforeClass()
    {
        sessionToken = v3api.login(TEST_USER, PASSWORD);

        final PropertyTypeCreation propertyTypeCreation = new PropertyTypeCreation();
        propertyTypeCreation.setCode(RICH_TEXT_PROPERTY_NAME);
        propertyTypeCreation.setLabel("Multiline");
        propertyTypeCreation.setDescription("Property type with multiline text value");
        propertyTypeCreation.setDataType(DataType.MULTILINE_VARCHAR);
        propertyTypePermId = v3api.createPropertyTypes(sessionToken, List.of(propertyTypeCreation)).get(0);

        final SampleTypeCreation sampleTypeCreation = new SampleTypeCreation();
        final PropertyAssignmentCreation propertyAssignmentCreation = new PropertyAssignmentCreation();
        propertyAssignmentCreation.setPropertyTypeId(propertyTypePermId);
        sampleTypeCreation.setCode("MULTI_LINE_VALUE_SAMPLE_TYPE");
        sampleTypeCreation.setPropertyAssignments(List.of(propertyAssignmentCreation));
        sampleTypePermId = v3api.createSampleTypes(sessionToken, List.of(sampleTypeCreation)).get(0);

        final SampleCreation sampleCreation = new SampleCreation();
        sampleCreation.setCode(RICH_TEXT_SAMPLE_CODE);
        sampleCreation.setTypeId(sampleTypePermId);
        sampleCreation.setProperty(RICH_TEXT_PROPERTY_NAME, RICH_TEXT_VALUE);

        samplePermId = v3api.createSamples(sessionToken, List.of(sampleCreation)).get(0);

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
        v3api.deletePropertyTypes(sessionToken, List.of(propertyTypePermId), propertyTypeDeletionOptions);

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
    public void testXlsDataExport(final String expectedResultFileName, final List<ExportablePermId> permIds, final IExportableFields fields,
            final XlsTextFormat xlsTextFormat, final boolean withReferredTypes, final boolean withImportCompatibility) throws Exception
    {
        processPermIds(permIds);

        final ExportData exportData = new ExportData(permIds, fields);
        final ExportOptions exportOptions = new ExportOptions(EnumSet.of(ExportFormat.XLS), xlsTextFormat, withReferredTypes, withImportCompatibility);
        final ExportResult exportResult = v3api.executeExport(sessionToken, exportData, exportOptions);
        final String downloadUrl = exportResult.getDownloadURL();

        compareFiles("ch/ethz/sis/openbis/systemtest/asapi/v3/test_files/xls/export/" + expectedResultFileName, downloadUrl);
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
        if (expectedResultFilePath.endsWith(XLSX_EXTENSION) && actualResultFilePath.endsWith(XLSX_EXTENSION))
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
                if (expectedZipEntry.equals(ZIPPED_EXPORT_FILE_NAME))
                {
                    try (
                            final InputStream expectedInputStream = extectedZipFile.getInputStream(extectedZipFile.getEntry(ZIPPED_EXPORT_FILE_NAME));
                            final InputStream actualInputStream = actualZipFile.getInputStream(actualZipFile.getEntry(ZIPPED_EXPORT_FILE_NAME));
                    )
                    {
                        compareXlsxStreams(expectedInputStream, actualInputStream);
                    }
                } else
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

        final File file = files[0];

        assertTrue(file.getName().startsWith(METADATA_FILE_PREFIX + "."));
        return file;
    }

}
