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

import java.util.EnumSet;
import java.util.List;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.AllFields;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.ExportData;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.ExportableKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.ExportablePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.options.ExportFormat;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.options.ExportOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.options.XlsTextFormat;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;

public class XlsExportTest extends AbstractTest
{

    private static final String EXPORT_DATA_PROVIDER = "xlsExportData";

    private static final Object[][] EXPORT_DATA = {
            {
                    XlsTextFormat.PLAIN,
                    false,
                    false
            }
    };

    protected String sessionToken;

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
    public void testDataExportAllFields(final XlsTextFormat xlsTextFormat,
            final boolean withReferredTypes, final boolean withImportCompatibility)
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

        final ExportData exportData = new ExportData(List.of(
                new ExportablePermId(ExportableKind.SAMPLE, new SamplePermId("/DEFAULT/DEFAULT/DEFAULT"))), new AllFields());
        final ExportOptions exportOptions = new ExportOptions(EnumSet.of(ExportFormat.XLS), xlsTextFormat, withReferredTypes, withImportCompatibility);
        v3api.executeExport(sessionToken, exportData, exportOptions);
    }

}
