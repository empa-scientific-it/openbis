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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.EnumSet;
import java.util.List;

import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

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
                    // "/TEST-SPACE/TEST-PROJECT/FV-TEST"
                    "export-sample.xlsx",
                    List.of(new ExportablePermId(ExportableKind.SAMPLE, new SamplePermId("201206191219327-1054"))),
                    XlsTextFormat.PLAIN,
                    true,
                    false
            }
    };

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
        final File sessionWorkspace = sessionWorkspaceProvider.getSessionWorkspace(sessionToken);
        final File[] files = sessionWorkspace.listFiles((FilenameFilter) new NameFileFilter(downloadUrl));

        assertNotNull(files);
        assertEquals(1, files.length);

        final File file = files[0];

        assertTrue(file.getName().startsWith("export."));

        System.out.println(sessionWorkspace);

        final InputStream expectedResultStream = getClass().getClassLoader().getResourceAsStream(
                "ch/ethz/sis/openbis/systemtest/asapi/v3/test_files/xls/export/" + expectedResultFileName);
        if (expectedResultStream == null)
        {
            throw new IllegalArgumentException("File not found.");
        }
        final Workbook expectedResult = new XSSFWorkbook(expectedResultStream);
        final Workbook actualResult = new XSSFWorkbook(new FileInputStream(file));

        XLSExportTest.assertWorkbooksEqual(actualResult, expectedResult);
    }

}
