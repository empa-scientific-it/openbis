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

package ch.ethz.sis.openbis.generic.dss.systemtest.api.v3;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.PhysicalDataCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.delete.DataSetDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.FileFormatTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.ProprietaryStorageFormatPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.RelativeLocationLocatorTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.id.DataStorePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.service.CustomDSSServiceExecutionOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.service.id.CustomDssServiceCode;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.service.id.ICustomDSSServiceId;
import ch.ethz.sis.openbis.generic.server.dss.plugins.imaging.container.ImagingPreviewContainer;
import org.testng.annotations.Test;

import java.util.*;


public class ImagingServiceTest extends AbstractFileTest
{

    @Test
    public void testImagingService() throws Exception
    {
        String sessionToken = as.login(TEST_USER, PASSWORD);

        DataSetPermId dataSetPermId = new DataSetPermId("TEST-IMAGING-" + UUID.randomUUID().toString());
        createDirectories(workingDirectory, new HashSet<>(List.of("dss-root", "dss-root/store", "dss-root/store/1",
                "dss-root/store/1/" + dataSetPermId.getPermId())));
        createFiles(workingDirectory, List.of("dss-root/store/1/" + dataSetPermId.getPermId() + "/file1.txt"));

        try
        {
            PhysicalDataCreation physicalCreation = new PhysicalDataCreation();
            physicalCreation.setLocation(dataSetPermId.getPermId());
            physicalCreation.setFileFormatTypeId(new FileFormatTypePermId("TIFF"));
            physicalCreation.setLocatorTypeId(new RelativeLocationLocatorTypePermId());
            physicalCreation.setStorageFormatId(new ProprietaryStorageFormatPermId());

            DataSetCreation creation = new DataSetCreation();
            creation.setCode(dataSetPermId.getPermId());
            creation.setDataSetKind(DataSetKind.PHYSICAL);
            creation.setTypeId(new EntityTypePermId("IMAGING_DATA"));
            creation.setExperimentId(new ExperimentIdentifier("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST"));
            creation.setDataStoreId(new DataStorePermId("STANDARD"));
            creation.setPhysicalData(physicalCreation);
            creation.setProperty("$IMAGING_DATA_CONFIG", "{\"config\" : { \"@type\" : \"dss.dto.imaging.ImagingDataSetConfig\", \"adaptor\" : \"ch.ethz.sis.openbis.generic.server.dss.plugins.imaging.adaptor.ImagingDataSetExampleAdaptor\" } }");


            List<DataSetPermId> ds = as.createDataSets(sessionToken, Arrays.asList(creation));
            DataSetPermId pds = ds.get(0);

            ICustomDSSServiceId serviceId = new CustomDssServiceCode("imaging");

            Map<String, Object> map = Map.of("@type", "dss.dto.imaging.ImagingDataSetPreview",
                    "config", Map.of("Dimension 1", new Integer[] {1,2}));

            CustomDSSServiceExecutionOptions options = new CustomDSSServiceExecutionOptions();
            options.withParameter("type", "preview");
            options.withParameter("permId", pds.getPermId());
            options.withParameter("index", 0);
            options.withParameter("preview", map);
            Object executionResult = dss.executeCustomDSSService(sessionToken, serviceId, options);

            assertTrue(executionResult instanceof ImagingPreviewContainer);
            ImagingPreviewContainer result = (ImagingPreviewContainer) executionResult;

            assertNotNull(result.getPreview().getBytes());
        }
        finally
        {
            DataSetDeletionOptions options = new DataSetDeletionOptions();
            options.setReason("cleanup");
            as.deleteDataSets(sessionToken, Arrays.asList(dataSetPermId), options);
            as.logout(sessionToken);
        }
    }












}
