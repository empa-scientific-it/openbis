/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.generic.server;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.AbstractDAOTest;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentWithContent;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetBatchUpdateDetails;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentBatchUpdateDetails;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewBasicExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewDataSetsWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperimentsWithType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSamplesWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleParentWithDerived;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.UpdatedBasicExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.UpdatedDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.UpdatedExperimentsWithType;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.util.RelationshipUtils;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;
import ch.systemsx.cisd.openbis.plugin.generic.shared.ResourceNames;
import ch.systemsx.cisd.openbis.systemtest.authorization.ProjectAuthorizationUser;

import junit.framework.Assert;

/**
 * @author pkupczyk
 */
public class GenericServerDatabaseTest extends AbstractDAOTest
{
    private static final String REUSE_EXPERIMENT_PERMID = "200811050940555-1032";

    private static final String TEST_EXPERIMENT_PERMID = "200902091255058-1035";

    private static final String REUSE_EXPERIMENT_SAMPLE_PERMID = "200811050929940-1018";

    private static final String TEST_EXPERIMENT_SAMPLE_PERMID = "200902091225616-1027";

    private static final String REUSE_EXPERIMENT_CONTAINER_DATA_SET_CODE = "20110509092359990-10";

    private static final String REUSE_EXPERIMENT_CONTAINED_DATA_SET_CODE = "20110509092359990-11";

    private static final String TEST_EXPERIMENT_CONTAINED_DATA_SET_CODE = "20110805092359990-17";

    private static final String PASSWORD = "password";

    @Resource(name = ResourceNames.GENERIC_PLUGIN_SERVER)
    IGenericServer server;

    @Autowired
    ICommonServer commonServer;

    private SessionContextDTO session;

    @BeforeClass(alwaysRun = true)
    public void init() throws SQLException
    {
        session = server.tryAuthenticate("test", "password");
    }

    @Test
    public void testChangingSampleShouldSetSampleAndExperiment()
    {
        DataPE dataset = findData(TEST_EXPERIMENT_CONTAINED_DATA_SET_CODE);

        Assert.assertEquals(TEST_EXPERIMENT_SAMPLE_PERMID, dataset.tryGetSample().getPermId());
        Assert.assertEquals(TEST_EXPERIMENT_PERMID, dataset.getExperiment().getPermId());

        SamplePE newSample = findSample(REUSE_EXPERIMENT_SAMPLE_PERMID);

        NewDataSet newDataset = new NewDataSet();
        newDataset.setCode(dataset.getCode());
        newDataset.setSampleIdentifierOrNull(newSample.getIdentifier());

        DataSetBatchUpdateDetails updateDetails = new DataSetBatchUpdateDetails();
        updateDetails.setSampleUpdateRequested(true);

        update(dataset, newDataset, updateDetails);

        Assert.assertEquals(REUSE_EXPERIMENT_SAMPLE_PERMID, dataset.tryGetSample().getPermId());
        Assert.assertEquals(REUSE_EXPERIMENT_PERMID, dataset.getExperiment().getPermId());
    }

    @Test
    public void testChangingExperimentShouldClearSampleAndSetExperiment()
    {
        DataPE dataset = findData(TEST_EXPERIMENT_CONTAINED_DATA_SET_CODE);

        Assert.assertEquals(TEST_EXPERIMENT_SAMPLE_PERMID, dataset.tryGetSample().getPermId());
        Assert.assertEquals(TEST_EXPERIMENT_PERMID, dataset.getExperiment().getPermId());

        ExperimentPE newExperiment = findExperiment(REUSE_EXPERIMENT_PERMID);

        NewDataSet newDataset = new NewDataSet();
        newDataset.setCode(dataset.getCode());
        newDataset.setExperimentIdentifier(newExperiment.getIdentifier());

        DataSetBatchUpdateDetails updateDetails = new DataSetBatchUpdateDetails();
        updateDetails.setExperimentUpdateRequested(true);

        update(dataset, newDataset, updateDetails);

        Assert.assertNull(dataset.tryGetSample());
        Assert.assertEquals(REUSE_EXPERIMENT_PERMID, dataset.getExperiment().getPermId());
    }

    @Test
    public void testChangingContainerToDataSetThatIsContainerShouldBeAllowed()
    {
        DataPE dataset = findData(TEST_EXPERIMENT_CONTAINED_DATA_SET_CODE);
        assertEquals(0, RelationshipUtils.getContainerComponentRelationships(dataset.getParentRelationships()).size());
        DataPE newContainer = findData(REUSE_EXPERIMENT_CONTAINER_DATA_SET_CODE);
        NewDataSet newDataset = new NewDataSet();
        newDataset.setCode(dataset.getCode());
        newDataset.setContainerIdentifierOrNull(newContainer.getIdentifier());

        DataSetBatchUpdateDetails updateDetails = new DataSetBatchUpdateDetails();
        updateDetails.setContainerUpdateRequested(true);

        update(dataset, newDataset, updateDetails);

        assertEquals(REUSE_EXPERIMENT_CONTAINER_DATA_SET_CODE,
                RelationshipUtils.getContainerComponentRelationships(dataset.getParentRelationships())
                        .get(0).getParentDataSet().getCode());
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testChangingContainerToDataSetThatIsNotContainerShouldNotBeAllowed()
    {
        DataPE dataset = findData(REUSE_EXPERIMENT_CONTAINER_DATA_SET_CODE);
        assertEquals(0, RelationshipUtils.getContainerComponentRelationships(dataset.getParentRelationships()).size());
        DataPE newContainer = findData(REUSE_EXPERIMENT_CONTAINED_DATA_SET_CODE);
        NewDataSet newDataset = new NewDataSet();
        newDataset.setCode(dataset.getCode());
        newDataset.setContainerIdentifierOrNull(newContainer.getIdentifier());

        DataSetBatchUpdateDetails updateDetails = new DataSetBatchUpdateDetails();
        updateDetails.setContainerUpdateRequested(true);

        update(dataset, newDataset, updateDetails);
    }

    @Test
    public void testClearingSampleShouldClearSampleAndLeaveExperiment()
    {
        DataPE dataset = findData(TEST_EXPERIMENT_CONTAINED_DATA_SET_CODE);

        Assert.assertEquals(TEST_EXPERIMENT_SAMPLE_PERMID, dataset.tryGetSample().getPermId());
        Assert.assertEquals(TEST_EXPERIMENT_PERMID, dataset.getExperiment().getPermId());

        NewDataSet newDataset = new NewDataSet();
        newDataset.setCode(dataset.getCode());
        newDataset.setSampleIdentifierOrNull(null);
        newDataset.setExperimentIdentifier(dataset.getExperiment().getIdentifier());

        DataSetBatchUpdateDetails updateDetails = new DataSetBatchUpdateDetails();
        updateDetails.setSampleUpdateRequested(true);

        update(dataset, newDataset, updateDetails);

        Assert.assertNull(dataset.tryGetSample());
        Assert.assertEquals(TEST_EXPERIMENT_PERMID, dataset.getExperiment().getPermId());
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListExperimentAttachmentsWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO sessionDTO = server.tryAuthenticate(user.getUserId(), PASSWORD);

        TechId experimentId = new TechId(23L); // /TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST
        String fileName = "testExperiment.txt";
        Integer version = 1;

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            AttachmentWithContent attachment = server.getExperimentFileAttachment(sessionDTO.getSessionToken(), experimentId, fileName, version);
            Assert.assertNotNull(attachment);
            Assert.assertEquals(fileName, attachment.getFileName());
        } else
        {
            try
            {
                server.getExperimentFileAttachment(sessionDTO.getSessionToken(), experimentId, fileName, version);
                Assert.fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListProjectAttachmentWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO sessionDTO = server.tryAuthenticate(user.getUserId(), PASSWORD);

        TechId projectId = new TechId(5); // /TEST_SPACE/TEST_PROJECT
        String fileName = "testProject.txt";
        Integer version = 1;

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            AttachmentWithContent attachment = server.getProjectFileAttachment(sessionDTO.getSessionToken(), projectId, fileName, version);
            Assert.assertNotNull(attachment);
            Assert.assertEquals(fileName, attachment.getFileName());
        } else
        {
            try
            {
                server.getProjectFileAttachment(sessionDTO.getSessionToken(), projectId, fileName, version);
                Assert.fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testRegisterExperimentWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO sessionDTO = server.tryAuthenticate(user.getUserId(), PASSWORD);

        IEntityProperty property = new EntityProperty();
        property.setValue("test description");
        PropertyType propertyType = new PropertyType();
        propertyType.setCode("DESCRIPTION");
        property.setPropertyType(propertyType);

        NewExperiment newExperiment = new NewExperiment("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST-2", "SIRNA_HCS");
        newExperiment.setProperties(new IEntityProperty[] { property });

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            Experiment experiment = server.registerExperiment(sessionDTO.getSessionToken(), newExperiment, Collections.emptyList());
            Assert.assertNotNull(experiment);
            Assert.assertEquals(experiment.getIdentifier(), newExperiment.getIdentifier());
        } else
        {
            try
            {
                server.registerExperiment(sessionDTO.getSessionToken(), newExperiment, Collections.emptyList());
                Assert.fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testRegisterExperimentsWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO sessionDTO = server.tryAuthenticate(user.getUserId(), PASSWORD);

        IEntityProperty property = new EntityProperty();
        property.setValue("test description");
        PropertyType propertyType = new PropertyType();
        propertyType.setCode("DESCRIPTION");
        property.setPropertyType(propertyType);

        NewBasicExperiment newExperiment = new NewBasicExperiment("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST-2");
        newExperiment.setProperties(new IEntityProperty[] { property });

        NewExperimentsWithType newExperiments = new NewExperimentsWithType("SIRNA_HCS", Arrays.asList(newExperiment));

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            server.registerExperiments(sessionDTO.getSessionToken(), newExperiments);

            Experiment experiment =
                    commonServer.getExperimentInfo(sessionDTO.getSessionToken(), ExperimentIdentifierFactory.parse(newExperiment.getIdentifier()));
            assertEquals(experiment.getProperties().get(0).getValue(), property.getValue());
        } else
        {
            try
            {
                server.registerExperiments(sessionDTO.getSessionToken(), newExperiments);
                Assert.fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testUpdateExperimentWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO sessionDTO = server.tryAuthenticate(user.getUserId(), PASSWORD);

        IEntityProperty property = new EntityProperty();
        property.setValue("test description");
        PropertyType propertyType = new PropertyType();
        propertyType.setCode("DESCRIPTION");
        property.setPropertyType(propertyType);

        ExperimentUpdatesDTO updates = new ExperimentUpdatesDTO();
        updates.setExperimentId(new TechId(23L)); // /TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST
        updates.setProperties(Arrays.asList(new IEntityProperty[] { property }));
        updates.setAttachments(new ArrayList<NewAttachment>());

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            ExperimentUpdateResult result = server.updateExperiment(sessionDTO.getSessionToken(), updates);
            assertNotNull(result);

            Experiment experiment = commonServer.getExperimentInfo(sessionDTO.getSessionToken(), updates.getExperimentId());
            assertEquals(experiment.getProperties().get(0).getValue(), property.getValue());
        } else
        {
            try
            {
                server.updateExperiment(sessionDTO.getSessionToken(), updates);
                Assert.fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testUpdateExperimentsWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO sessionDTO = server.tryAuthenticate(user.getUserId(), PASSWORD);

        IEntityProperty property = new EntityProperty();
        property.setValue("test description");
        PropertyType propertyType = new PropertyType();
        propertyType.setCode("DESCRIPTION");
        property.setPropertyType(propertyType);

        UpdatedBasicExperiment update = new UpdatedBasicExperiment();
        update.setIdentifier("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");
        update.setProperties(new IEntityProperty[] { property });
        update.setBatchUpdateDetails(new ExperimentBatchUpdateDetails(Collections.singleton("DESCRIPTION")));

        ExperimentType type = new ExperimentType();
        type.setCode("SIRNA_HCS");

        UpdatedExperimentsWithType updates = new UpdatedExperimentsWithType(type, Arrays.asList(update));

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            server.updateExperiments(sessionDTO.getSessionToken(), updates);

            Experiment experiment =
                    commonServer.getExperimentInfo(sessionDTO.getSessionToken(), ExperimentIdentifierFactory.parse(update.getIdentifier()));
            assertEquals(experiment.getProperties().get(0).getValue(), property.getValue());
        } else
        {
            try
            {
                server.updateExperiments(sessionDTO.getSessionToken(), updates);
                Assert.fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testGetSampleInfoWithTechIdWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO sessionDTO = server.tryAuthenticate(user.getUserId(), PASSWORD);

        TechId sampleId = new TechId(1054L); // /TEST-SPACE/FV-TEST

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            SampleParentWithDerived sample = server.getSampleInfo(sessionDTO.getSessionToken(), sampleId);
            assertEquals(sample.getParent().getCode(), "FV-TEST");
        } else
        {
            try
            {
                server.getSampleInfo(sessionDTO.getSessionToken(), sampleId);
                Assert.fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testRegisterSampleWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO sessionDTO = server.tryAuthenticate(user.getUserId(), PASSWORD);

        NewSample newSample = createNewSample("/TEST-SPACE/TEST-PROJECT/TEST-SAMPLE", "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            Sample sample = server.registerSample(sessionDTO.getSessionToken(), newSample, Collections.emptyList());
            assertEquals(sample.getIdentifier(), newSample.getIdentifier());
        } else
        {
            try
            {
                server.registerSample(sessionDTO.getSessionToken(), newSample, Collections.emptyList());
                Assert.fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testRegisterOrUpdateSamplesWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO sessionDTO = server.tryAuthenticate(user.getUserId(), PASSWORD);

        NewSample newSample = createNewSample("/TEST-SPACE/TEST-PROJECT/TEST-SAMPLE", "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");
        NewSamplesWithTypes newSamples = new NewSamplesWithTypes(newSample.getSampleType(), Arrays.asList(newSample));

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            server.registerOrUpdateSamples(sessionDTO.getSessionToken(), Arrays.asList(newSamples));
        } else
        {
            try
            {
                server.registerOrUpdateSamples(sessionDTO.getSessionToken(), Arrays.asList(newSamples));
                Assert.fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testRegisterSamplesWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO sessionDTO = server.tryAuthenticate(user.getUserId(), PASSWORD);

        NewSample newSample = createNewSample("/TEST-SPACE/TEST-SAMPLE", "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");
        NewSamplesWithTypes newSamples = new NewSamplesWithTypes(newSample.getSampleType(), Arrays.asList(newSample));

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            server.registerSamples(sessionDTO.getSessionToken(), Arrays.asList(newSamples));
        } else
        {
            try
            {
                server.registerSamples(sessionDTO.getSessionToken(), Arrays.asList(newSamples));
                Assert.fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testUpdateSampleWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO sessionDTO = server.tryAuthenticate(user.getUserId(), PASSWORD);

        SampleUpdatesDTO updates = createSampleUpdates(1055, "/TEST-SPACE/TEST-PROJECT/EV-TEST",
                "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST", "COMMENT", "updated comment");

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            server.updateSample(sessionDTO.getSessionToken(), updates);
        } else
        {
            try
            {
                server.updateSample(sessionDTO.getSessionToken(), updates);
                Assert.fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testRegisterOrUpdateSamplesAndMaterialsWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO sessionDTO = server.tryAuthenticate(user.getUserId(), PASSWORD);

        NewSample newSample = createNewSample("/TEST-SPACE/TEST-PROJECT/EV-TEST2", "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");
        NewSamplesWithTypes newSamples = new NewSamplesWithTypes(newSample.getSampleType(), Arrays.asList(newSample));

        if (user.isInstanceUser())
        {
            server.registerOrUpdateSamplesAndMaterials(sessionDTO.getSessionToken(), Arrays.asList(newSamples), Collections.emptyList());
        } else
        {
            try
            {
                server.registerOrUpdateSamplesAndMaterials(sessionDTO.getSessionToken(), Arrays.asList(newSamples), Collections.emptyList());
                Assert.fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testUpdateSamplesWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO sessionDTO = server.tryAuthenticate(user.getUserId(), PASSWORD);

        NewSample newSample = createNewSample("/TEST-SPACE/TEST-PROJECT/EV-TEST", "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");
        NewSamplesWithTypes newSamples = new NewSamplesWithTypes(newSample.getSampleType(), Arrays.asList(newSample));

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            server.updateSamples(sessionDTO.getSessionToken(), Arrays.asList(newSamples));
        } else
        {
            try
            {
                server.updateSamples(sessionDTO.getSessionToken(), Arrays.asList(newSamples));
                Assert.fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testGetSamplesFileAttachmentWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO sessionDTO = server.tryAuthenticate(user.getUserId(), PASSWORD);

        TechId sampleId = new TechId(1054L);
        String fileName = "testSample.txt";
        Integer version = 1;

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            AttachmentWithContent attachment = server.getSampleFileAttachment(sessionDTO.getSessionToken(), sampleId, fileName, version);
            assertEquals(attachment.getDescription(), "Test sample description");
        } else
        {
            try
            {
                server.getSampleFileAttachment(sessionDTO.getSessionToken(), sampleId, fileName, version);
                Assert.fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testGetDataSetInfoWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO sessionDTO = server.tryAuthenticate(user.getUserId(), PASSWORD);

        TechId dataSetId = new TechId(41L); // 20120628092259000-41

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            AbstractExternalData dataSet = server.getDataSetInfo(sessionDTO.getSessionToken(), dataSetId);
            assertEquals(dataSet.getCode(), "20120628092259000-41");
        } else
        {
            try
            {
                server.getDataSetInfo(sessionDTO.getSessionToken(), dataSetId);
                Assert.fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testUpdateDataSetWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO sessionDTO = server.tryAuthenticate(user.getUserId(), PASSWORD);

        IEntityProperty property = new EntityProperty();
        property.setValue("test comment");
        PropertyType propertyType = new PropertyType();
        propertyType.setCode("COMMENT");
        property.setPropertyType(propertyType);

        DataSetUpdatesDTO updates = new DataSetUpdatesDTO();
        updates.setDatasetId(new TechId(22L)); // 20120619092259000-22
        updates.setProperties(Arrays.asList(new IEntityProperty[] { property }));
        updates.setFileFormatTypeCode("XML");

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            DataSetUpdateResult result = server.updateDataSet(sessionDTO.getSessionToken(), updates);
            assertNotNull(result);

            AbstractExternalData dataSet = server.getDataSetInfo(sessionDTO.getSessionToken(), updates.getDatasetId());
            assertEquals(dataSet.getProperties().get(0).getValue(), property.getValue());
        } else
        {
            try
            {
                server.updateDataSet(sessionDTO.getSessionToken(), updates);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testUpdateDataSetsWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO sessionDTO = server.tryAuthenticate(user.getUserId(), PASSWORD);

        IEntityProperty property = new EntityProperty();
        property.setValue("test comment");
        PropertyType propertyType = new PropertyType();
        propertyType.setCode("COMMENT");
        property.setPropertyType(propertyType);

        NewDataSet newDataSet = new NewDataSet();
        newDataSet.setCode("20120619092259000-22");
        newDataSet.setProperties(new IEntityProperty[] { property });

        TechId dataSetId = new TechId(22L);
        DataSetType dataSetType = new DataSetType("UNKNOWN");

        NewDataSetsWithTypes newDataSets = new NewDataSetsWithTypes(dataSetType, Arrays.asList(newDataSet));

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            server.updateDataSets(sessionDTO.getSessionToken(), newDataSets);

            AbstractExternalData dataSet = server.getDataSetInfo(sessionDTO.getSessionToken(), dataSetId);
            assertEquals(dataSet.getProperties().get(0).getValue(), property.getValue());
        } else
        {
            try
            {
                server.updateDataSets(sessionDTO.getSessionToken(), newDataSets);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    private void update(DataPE data, NewDataSet newDataset, DataSetBatchUpdateDetails updateDetails)
    {
        List<NewDataSet> newDatasets = new ArrayList<NewDataSet>();
        newDatasets.add(new UpdatedDataSet(newDataset, updateDetails));

        NewDataSetsWithTypes newDatasetsWithType = new NewDataSetsWithTypes();
        newDatasetsWithType.setDataSetType(new DataSetType(data.getDataSetType().getCode()));
        newDatasetsWithType.setNewDataSets(newDatasets);

        server.updateDataSets(session.getSessionToken(), newDatasetsWithType);
        sessionFactory.getCurrentSession().update(data);
    }

    private NewSample createNewSample(String sampleIdentifier, String experimentIdentifier)
    {
        SampleType type = new SampleType();
        type.setCode("CELL_PLATE");

        NewSample newSample = new NewSample();
        newSample.setSampleType(type);
        newSample.setIdentifier(sampleIdentifier);
        newSample.setExperimentIdentifier(experimentIdentifier);
        return newSample;
    }

    private SampleUpdatesDTO createSampleUpdates(long sampleId, String sampleIdentifier,
            String experimentIdentifier, String propertyCode, String propertyValue)
    {
        SampleUpdatesDTO updates =
                new SampleUpdatesDTO(new TechId(sampleId), null,
                        ExperimentIdentifierFactory.parse(experimentIdentifier),
                        null, Collections.emptyList(), 0,
                        SampleIdentifierFactory.parse(sampleIdentifier), null, null);
        updates.setProperties(Arrays.asList(createEntityProperty(propertyCode, propertyValue)));
        updates.setUpdateExperimentLink(false);
        return updates;
    }

    private IEntityProperty createEntityProperty(String propertyCode, String propertyValue)
    {
        IEntityProperty property = new EntityProperty();
        property.setValue(propertyValue);
        PropertyType propertyType = new PropertyType();
        propertyType.setCode(propertyCode);
        property.setPropertyType(propertyType);
        return property;
    }

}
