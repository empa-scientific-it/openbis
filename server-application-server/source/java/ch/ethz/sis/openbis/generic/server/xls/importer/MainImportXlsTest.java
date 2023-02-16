/*
 * Copyright ETH 2022 - 2023 Zürich, Scientific IT Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.ethz.sis.openbis.generic.server.xls.importer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.apache.log4j.Logger;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.server.xls.importer.enums.ImportModes;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.impl.MasterDataRegistrationHelper;

public class MainImportXlsTest
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, MainImportXlsTest.class);

    private static final int TIMEOUT = 10000;

    private static final String URL =
            "http://localhost:8888/openbis/openbis" + IApplicationServerApi.SERVICE_URL;

    private static final File ELN_MASTER_DATA_PATH =
            new File("../core-plugin-openbis/dist/core-plugins/eln-lims/1/as");

    private static final File LIFE_SCIENCES_MASTER_DATA_PATH =
            new File("../core-plugin-openbis/dist/core-plugins/eln-lims-life-sciences/1/as");

    // used only for development!
    public static void main(String[] args)
    {
        IApplicationServerApi v3 =
                HttpInvokerUtils.createServiceStub(IApplicationServerApi.class, URL, TIMEOUT);
        String sessionToken = v3.login("system", "a");

        Map<String, String> scripts = getListScripts(ELN_MASTER_DATA_PATH.getPath());
        ImportOptions options = new ImportOptions();

        XLSImport importXls =
                new XLSImport(sessionToken, v3, scripts, ImportModes.UPDATE_IF_EXISTS, options,
                        "ELN-LIMS");
        Collection<IObjectId> importedIds = new ArrayList<>();

        File commonDataModel = new File(ELN_MASTER_DATA_PATH, "/master-data/common-data-model.xls");
        importedIds.addAll(importXls.importXLS(FileUtilities.loadToByteArray(commonDataModel)));
        File singleGroupDataModel =
                new File(ELN_MASTER_DATA_PATH, "/master-data/single-group-data-model.xls");
        importedIds.addAll(
                importXls.importXLS(FileUtilities.loadToByteArray(singleGroupDataModel)));

        Map<String, String> lifeSciencesImporterScripts =
                getListScripts(LIFE_SCIENCES_MASTER_DATA_PATH.getPath());
        XLSImport lifeSciencesImporter =
                new XLSImport(sessionToken, v3, lifeSciencesImporterScripts,
                        ImportModes.UPDATE_IF_EXISTS, options, "ELN-LIMS-LIFE-SCIENCES");
        File lifeSciencesDataModel =
                new File(LIFE_SCIENCES_MASTER_DATA_PATH, "/master-data/data-model.xls");
        importedIds.addAll(lifeSciencesImporter.importXLS(
                FileUtilities.loadToByteArray(lifeSciencesDataModel)));
    }

    private static Map<String, String> getListScripts(String path)
    {
        MasterDataRegistrationHelper helper = new MasterDataRegistrationHelper(Arrays.asList(path));
        return helper.getAllScripts();
    }
}
