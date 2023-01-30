/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetBatchUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialUpdateDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class AtomicEntityOperationDetailsTest extends AssertJUnit
{

    @Test
    public void testToString()
    {
        // Given
        ArrayList<NewSpace> spaceRegistrations = new ArrayList<NewSpace>();
        for (int i = 0; i < 110; i++)
        {
            spaceRegistrations.add(new NewSpace("S" + i, "description", "adminUser" + i));
        }

        Map<String, List<NewMaterial>> materialRegistrations =
                new HashMap<String, List<NewMaterial>>();
        List<NewMaterial> newMaterials1 = new ArrayList<NewMaterial>();
        newMaterials1.add(new NewMaterial("material-one"));
        newMaterials1.add(new NewMaterial("material-two"));
        List<NewMaterial> newMaterials2 = new ArrayList<NewMaterial>();
        newMaterials2.add(new NewMaterial("material-three"));
        materialRegistrations.put("material-type-1", newMaterials1);
        materialRegistrations.put("material-type-2", newMaterials2);

        List<MaterialUpdateDTO> materialUpdates = new ArrayList<MaterialUpdateDTO>();

        ArrayList<NewProject> projectRegistrations = new ArrayList<NewProject>();
        projectRegistrations.add(new NewProject("/SPACE/P1", "description"));
        projectRegistrations.add(new NewProject("/SPACE/P2", "description"));
        ArrayList<ProjectUpdatesDTO> projectUpdates = new ArrayList<ProjectUpdatesDTO>();

        ArrayList<NewExperiment> experimentRegistrations = new ArrayList<NewExperiment>();
        experimentRegistrations.add(new NewExperiment("/SPACE/PROJECT/EXP-ID1", "EXP-TYPE"));
        experimentRegistrations.add(new NewExperiment("/SPACE/PROJECT/EXP-ID2", "EXP-TYPE"));

        List<ExperimentUpdatesDTO> experimentUpdates = new ArrayList<ExperimentUpdatesDTO>();

        ArrayList<SampleUpdatesDTO> sampleUpdates = new ArrayList<SampleUpdatesDTO>();

        ArrayList<NewSample> sampleRegistrations = new ArrayList<NewSample>();
        sampleRegistrations.add(new NewSample("/SPACE/SAMPLE-ID1", new SampleType(), null, null,
                "/SPACE/PROJECT/EXP-ID1", null, null, new IEntityProperty[0],
                new ArrayList<NewAttachment>()));
        sampleRegistrations.add(new NewSample("/SPACE/SAMPLE-ID2", new SampleType(), null, null,
                "/SPACE/PROJECT/EXP-ID1", null, null, new IEntityProperty[0],
                new ArrayList<NewAttachment>()));

        ArrayList<NewExternalData> dataSetRegistrations = new ArrayList<NewExternalData>();
        NewExternalData newExternalData = new NewExternalData();
        newExternalData.setCode("DATA-SET-CODE");
        newExternalData.setSampleIdentifierOrNull(new SampleIdentifierFactory("/SPACE/SAMPLE-ID1")
                .createIdentifier());
        dataSetRegistrations.add(newExternalData);

        List<DataSetBatchUpdatesDTO> dataSetUpdates = new ArrayList<DataSetBatchUpdatesDTO>();
        DataSetBatchUpdatesDTO dataSetUpdate = new DataSetBatchUpdatesDTO();
        dataSetUpdate.setDatasetId(new TechId(1L));
        dataSetUpdates.add(dataSetUpdate);

        List<NewMetaproject> metaprojectRegistrations =
                Collections.singletonList(new NewMetaproject("TEST-AEOD-TAG", "short description",
                        "test"));

        List<MetaprojectUpdatesDTO> metaprojectUpdates = new ArrayList<MetaprojectUpdatesDTO>();

        List<VocabularyUpdatesDTO> vocabularyUpdates = Collections.emptyList();

        AtomicEntityOperationDetails details =
                new AtomicEntityOperationDetails(new TechId(42), null, spaceRegistrations,
                        projectRegistrations, projectUpdates, experimentRegistrations,
                        experimentUpdates, sampleUpdates, sampleRegistrations,
                        materialRegistrations, materialUpdates,
                        dataSetRegistrations, dataSetUpdates, metaprojectRegistrations,
                        metaprojectUpdates, vocabularyUpdates);

        // When
        String detailsAsString = details.toString();

        // Then
        assertEquals(
                "AtomicEntityOperationDetails[registrationIdOrNull=42,"
                        + "spaceRegistrations=[S0, S1, S2, S3, S4, S5, S6, S7, S8, S9, S10, S11, "
                        + "S12, S13, S14, S15, S16, S17, S18, S19, S20, S21, S22, S23, S24, S25, S26, S27, S28, S29, "
                        + "S30, S31, S32, S33, S34, S35, S36, S37, S38, S39, S40, S41, S42, S43, S44, S45, S46, S47, "
                        + "S48, S49, S50, S51, S52, S53, S54, S55, S56, S57, S58, S59, S60, S61, S62, S63, S64, S65, "
                        + "S66, S67, S68, S69, S70, S71, S72, S73, S74, S75, S76, S77, S78, S79, S80, S81, S82, S83, "
                        + "S84, S85, S86, S87, S88, S89, S90, S91, S92, S93, S94, S95, S96, S97, S98, S99, ... (10 left)]"
                        + ",projectRegistrations=[/SPACE/P1, /SPACE/P2]"
                        + ",experimentRegistrations=[/SPACE/PROJECT/EXP-ID1, /SPACE/PROJECT/EXP-ID2]"
                        + ",sampleRegistrations=[/SPACE/SAMPLE-ID1, /SPACE/SAMPLE-ID2]"
                        + ",materialRegistrations=[material-type-1=[material-one, material-two], material-type-2=[material-three]]"
                        + ",dataSetRegistrations=[NewExternalData[code=DATA-SET-CODE,type=<null>,kind=<null>,fileFormat=<null>,properties=[]]]"
                        + ",dataSetUpdates=[1]"
                        + ",metaprojectRegistrations=[NewMetaproject[name=TEST-AEOD-TAG,description=short description,ownerId=test]]]",
                detailsAsString);

    }
}
