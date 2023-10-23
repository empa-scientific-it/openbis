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

import static ch.ethz.sis.openbis.generic.server.asapi.v3.executor.exporter.ExportExecutor.EXPORT_FILE_PREFIX;
import static ch.ethz.sis.openbis.generic.server.xls.export.XLSExport.XLSX_EXTENSION;
import static ch.ethz.sis.openbis.generic.server.xls.export.XLSExport.ZIP_EXTENSION;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.ExportResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.AllFields;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.ExportData;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.ExportableKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.ExportablePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.options.ExportFormat;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.options.ExportOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.options.XlsTextFormat;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.server.xls.export.XLSExportTest;
import ch.systemsx.cisd.openbis.generic.shared.ISessionWorkspaceProvider;

public class XlsExportTest extends AbstractTest
{

    private static final String EXPORT_DATA_PROVIDER = "xlsExportData";

    private static final Object[][] EXPORT_DATA = {
            {
                    // Non-existing sample
                    "empty.xlsx",
                    List.of(new ExportablePermId(ExportableKind.SAMPLE, new SamplePermId("WrongPermId"))),
                    XlsTextFormat.PLAIN,
                    true, // withReferredTypes
                    false // withImportCompatibility
            },
            {
                    // Sample: /TEST-SPACE/TEST-PROJECT/FV-TEST
                    "export-sample.xlsx",
                    List.of(new ExportablePermId(ExportableKind.SAMPLE, new SamplePermId("201206191219327-1054"))),
                    XlsTextFormat.PLAIN,
                    true, // withReferredTypes
                    false // withImportCompatibility
            },
            {
                    // Sample: /TEST-SPACE/TEST-PROJECT/FV-TEST
                    "export-sample-compatible-with-import.xlsx",
                    List.of(new ExportablePermId(ExportableKind.SAMPLE, new SamplePermId("201206191219327-1054"))),
                    XlsTextFormat.PLAIN,
                    true, // withReferredTypes
                    true // withImportCompatibility
            },
            {
                    // Sample Type: CELL_PLATE
                    "export-sample-type-with-referred-types.zip",
                    List.of(new ExportablePermId(ExportableKind.SAMPLE_TYPE, new EntityTypePermId("CELL_PLATE", EntityKind.SAMPLE))),
                    XlsTextFormat.PLAIN,
                    true, // withReferredTypes
                    false // withImportCompatibility
            },
            {
                    // Experiment Type: SIRNA_HCS
                    "export-experiment-type.xlsx",
                    List.of(new ExportablePermId(ExportableKind.EXPERIMENT_TYPE, new EntityTypePermId("SIRNA_HCS", EntityKind.EXPERIMENT))),
                    XlsTextFormat.PLAIN,
                    false, // withReferredTypes
                    false // withImportCompatibility
            },
    };

    private static final String ZIPPED_EXPORT_FILE_NAME = EXPORT_FILE_PREFIX + XLSX_EXTENSION;

    protected String sessionToken;

    @Autowired
    private ISessionWorkspaceProvider sessionWorkspaceProvider;

    @DataProvider
    protected Object[][] xlsExportData()
    {
        return EXPORT_DATA;
    }

    @BeforeMethod
    public void beforeTest()
    {
        sessionToken = v3api.login(TEST_USER, PASSWORD);
    }

    @AfterMethod
    public void afterTest()
    {
        v3api.logout(sessionToken);
    }

    @Test(dataProvider = EXPORT_DATA_PROVIDER)
    public void testDataExportAllFields(final String expectedResultFileName, final List<ExportablePermId> permIds, final XlsTextFormat xlsTextFormat,
            final boolean withReferredTypes, final boolean withImportCompatibility) throws Exception
    {
//        // TODO: specify values here.
//        final String expectedResultFileName;
//        final Map<String, String> expectedScripts;
//        final Class<IApplicationServerApi> expectationsClass;
//        final List<ExportablePermId> exportablePermIds;
//        final boolean exportReferred;
//        final Map<String, Map<String, List<Map<String, String>>>> exportFields;
//        final XLSExport.TextFormatting textFormatting;
//        final List<String> expectedWarnings;
//        final boolean compatibleWithImport;
//
//        final Expectations expectations = (Expectations) expectationsClass.getConstructor(IApplicationServerApi.class,
//                boolean.class).newInstance(api, exportReferred);
//        mockery.checking(expectations);

        final ExportData exportData = new ExportData(permIds, new AllFields());
        final ExportOptions exportOptions = new ExportOptions(EnumSet.of(ExportFormat.XLS), xlsTextFormat, withReferredTypes, withImportCompatibility);
        final ExportResult exportResult = v3api.executeExport(sessionToken, exportData, exportOptions);
        final String downloadUrl = exportResult.getDownloadURL();

        compareFiles("ch/ethz/sis/openbis/systemtest/asapi/v3/test_files/xls/export/" + expectedResultFileName, downloadUrl);
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
        }
    }

    private File getActualFile(final String actualResultFilePath) throws FileNotFoundException
    {
        final File sessionWorkspace = sessionWorkspaceProvider.getSessionWorkspace(sessionToken);
        final File[] files = sessionWorkspace.listFiles((FilenameFilter) new NameFileFilter(actualResultFilePath));

        assertNotNull(files);
        assertEquals(1, files.length, String.format("Session workspace should contain only one file with the download URL '%s'.",
                actualResultFilePath));

        final File file = files[0];

        assertTrue(file.getName().startsWith("export."));
        return file;
    }

}
