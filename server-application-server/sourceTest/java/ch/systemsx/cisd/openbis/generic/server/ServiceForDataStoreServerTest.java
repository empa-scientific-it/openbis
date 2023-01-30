/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetBatchUpdateDetails;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentFetchOptions;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocationNode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LocatorType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewProject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSamplesWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationResult;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetBatchUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ListSamplesByPropertyCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewVocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.dto.PermId;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.StorageFormat;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.systemtest.SystemTestCase;
import ch.systemsx.cisd.openbis.systemtest.authorization.ProjectAuthorizationUser;
import junit.framework.Assert;

/**
 * @author pkupczyk
 */
public class ServiceForDataStoreServerTest extends SystemTestCase
{

    private static final ProjectIdentifier TEST_PROJECT_IDENTIFIER = new ProjectIdentifier("TEST-SPACE", "TEST-PROJECT");

    @Test()
    public void testListPhysicalDataSetsWithUnknownSize()
    {
        String sessionToken = authenticateAs("test");
        List<SimpleDataSetInformationDTO> dataSetsWithUnknownSize = etlService.listPhysicalDataSetsWithUnknownSize(sessionToken, "STANDARD", 3, null);

        Assert.assertEquals(3, dataSetsWithUnknownSize.size());
        Assert.assertEquals("20081105092159188-3", dataSetsWithUnknownSize.get(0).getDataSetCode());
        Assert.assertEquals("20081105092159222-2", dataSetsWithUnknownSize.get(1).getDataSetCode());
        Assert.assertEquals("20081105092159333-3", dataSetsWithUnknownSize.get(2).getDataSetCode());
    }

    @Test()
    public void testListPhysicalDataSetsWithUnknownSizeAndDataSetCodeLimit()
    {
        String sessionToken = authenticateAs("test");
        List<SimpleDataSetInformationDTO> dataSetsWithUnknownSize =
                etlService.listPhysicalDataSetsWithUnknownSize(sessionToken, "STANDARD", 3, "20081105092159188-3");

        Assert.assertEquals(3, dataSetsWithUnknownSize.size());
        Assert.assertEquals("20081105092159222-2", dataSetsWithUnknownSize.get(0).getDataSetCode());
        Assert.assertEquals("20081105092159333-3", dataSetsWithUnknownSize.get(1).getDataSetCode());
        Assert.assertEquals("20081105092259000-18", dataSetsWithUnknownSize.get(2).getDataSetCode());
    }

    @Test(dependsOnMethods = "testListPhysicalDataSetsWithUnknownSize")
    public void testUpdatePhysicalDataSetsWithUnknownSize()
    {
        String sessionToken = authenticateAs("test");

        Map<String, Long> sizeMap = new HashMap<String, Long>();
        sizeMap.put("20081105092159188-3", 123L);

        etlService.updatePhysicalDataSetsSize(sessionToken, sizeMap);

        int unknownSizeCount = 0;
        List<SimpleDataSetInformationDTO> physicalDataSets = etlService.listPhysicalDataSets(sessionToken, "STANDARD");

        for (SimpleDataSetInformationDTO physicalDataSet : physicalDataSets)
        {
            if (physicalDataSet.getDataSetSize() == null)
            {
                unknownSizeCount++;
            }
        }

        List<SimpleDataSetInformationDTO> dataSetsWithUnknownSize =
                etlService.listPhysicalDataSetsWithUnknownSize(sessionToken, "STANDARD", 100, null);
        List<AbstractExternalData> updatedDataSets = etlService.listDataSetsByCode(sessionToken, Arrays.asList("20081105092159188-3"));

        Assert.assertEquals(unknownSizeCount, dataSetsWithUnknownSize.size());
        Assert.assertEquals("20081105092159222-2", dataSetsWithUnknownSize.get(0).getDataSetCode());
        Assert.assertEquals("VALIDATIONS_PARENT-28", dataSetsWithUnknownSize.get(dataSetsWithUnknownSize.size() - 1).getDataSetCode());

        Assert.assertEquals(1, updatedDataSets.size());
        Assert.assertEquals("20081105092159188-3", updatedDataSets.get(0).getCode());
        Assert.assertEquals(Long.valueOf(123L), updatedDataSets.get(0).getSize());
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testListExperimentsForProjectsWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = etlService.tryAuthenticate(user.getUserId(), PASSWORD);

        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            List<Experiment> experiments =
                    etlService.listExperimentsForProjects(session.getSessionToken(), Arrays.asList(TEST_PROJECT_IDENTIFIER), fetchOptions);
            assertEquals(experiments.size(), 1);
            assertEquals(experiments.get(0).getIdentifier(), "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");
        } else
        {
            try
            {
                etlService.listExperimentsForProjects(session.getSessionToken(), Arrays.asList(TEST_PROJECT_IDENTIFIER), fetchOptions);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testListProjectsWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = etlService.tryAuthenticate(user.getUserId(), PASSWORD);

        if (user.isDisabledProjectUser())
        {
            try
            {
                etlService.listProjects(session.getSessionToken());
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        } else
        {
            List<Project> projects = etlService.listProjects(session.getSessionToken());

            if (user.isInstanceUser())
            {
                assertEntities(
                        "[/CISD/DEFAULT, /CISD/NEMO, /CISD/NOE, /TEST-SPACE/NOE, /TEST-SPACE/PROJECT-TO-DELETE, /TEST-SPACE/TEST-PROJECT, /TESTGROUP/TESTPROJ]",
                        projects);
            } else if (user.isTestSpaceUser())
            {
                assertEntities("[/TEST-SPACE/NOE, /TEST-SPACE/PROJECT-TO-DELETE, /TEST-SPACE/TEST-PROJECT]", projects);
            } else if (user.isTestGroupUser())
            {
                assertEntities("[/TESTGROUP/TESTPROJ]", projects);
            } else if (user.isTestProjectUser())
            {
                assertEntities("[/TEST-SPACE/PROJECT-TO-DELETE, /TEST-SPACE/TEST-PROJECT]", projects);
            } else
            {
                assertEntities("[]", projects);
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testTryGetSpaceWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = etlService.tryAuthenticate(user.getUserId(), PASSWORD);

        SpaceIdentifier spaceIdentifier = new SpaceIdentifier("TEST-SPACE");

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            Space space = etlService.tryGetSpace(session.getSessionToken(), spaceIdentifier);
            assertEquals(space.getIdentifier(), "/TEST-SPACE");
        } else
        {
            try
            {
                etlService.tryGetSpace(session.getSessionToken(), spaceIdentifier);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testTryGetProjectWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = etlService.tryAuthenticate(user.getUserId(), PASSWORD);

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            Project project = etlService.tryGetProject(session.getSessionToken(), TEST_PROJECT_IDENTIFIER);
            assertEquals(project.getIdentifier(), "/TEST-SPACE/TEST-PROJECT");
        } else
        {
            try
            {
                etlService.tryGetProject(session.getSessionToken(), TEST_PROJECT_IDENTIFIER);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testTryGetProjectByPermIdWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = etlService.tryAuthenticate(user.getUserId(), PASSWORD);

        PermId projectPermId = new PermId("20120814110011738-105"); // /TEST-SPACE/TEST-PROJECT

        if (user.isInstanceUser() || (user.isETLServerUser() && user.isTestSpaceUser()))
        {
            Project project = etlService.tryGetProjectByPermId(session.getSessionToken(), projectPermId);
            assertEquals(project.getIdentifier(), "/TEST-SPACE/TEST-PROJECT");
        } else
        {
            try
            {
                etlService.tryGetProjectByPermId(session.getSessionToken(), projectPermId);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testTryGetExperimentByPermIdWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = etlService.tryAuthenticate(user.getUserId(), PASSWORD);

        PermId experimentPermId = new PermId("201206190940555-1032"); // /TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            Experiment experiment = etlService.tryGetExperimentByPermId(session.getSessionToken(), experimentPermId);
            assertEquals(experiment.getIdentifier(), "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");
        } else
        {
            try
            {
                etlService.tryGetExperimentByPermId(session.getSessionToken(), experimentPermId);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testListDataSetsByExperimentIDWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = etlService.tryAuthenticate(user.getUserId(), PASSWORD);

        TechId experimentId = new TechId(23L); // /TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            List<AbstractExternalData> dataSets = etlService.listDataSetsByExperimentID(session.getSessionToken(), experimentId);
            assertEquals(dataSets.size(), 9);
        } else
        {
            try
            {
                etlService.listDataSetsByExperimentID(session.getSessionToken(), experimentId);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testListExperimentsByExperimentIdentifierListWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = etlService.tryAuthenticate(user.getUserId(), PASSWORD);

        ExperimentIdentifier experimentIdentifier = new ExperimentIdentifier("TEST-SPACE", "TEST-PROJECT", "EXP-SPACE-TEST");
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            List<Experiment> experiments =
                    etlService.listExperiments(session.getSessionToken(), Arrays.asList(experimentIdentifier), fetchOptions);
            assertEquals(experiments.size(), 1);
            assertEquals(experiments.get(0).getIdentifier(), "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");
        } else
        {
            try
            {
                etlService.listExperiments(session.getSessionToken(), Arrays.asList(experimentIdentifier), fetchOptions);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testListExperimentsByProjectIdentifierWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = etlService.tryAuthenticate(user.getUserId(), PASSWORD);

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            List<Experiment> experiments = etlService.listExperiments(session.getSessionToken(), TEST_PROJECT_IDENTIFIER);
            assertEquals(experiments.size(), 1);
            assertEquals(experiments.get(0).getIdentifier(), "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");
        } else
        {
            try
            {
                etlService.listExperiments(session.getSessionToken(), TEST_PROJECT_IDENTIFIER);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testTryGetExperimentWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = etlService.tryAuthenticate(user.getUserId(), PASSWORD);

        ExperimentIdentifier experimentIdentifier = new ExperimentIdentifier("TEST-SPACE", "TEST-PROJECT", "EXP-SPACE-TEST");

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            Experiment experiment = etlService.tryGetExperiment(session.getSessionToken(), experimentIdentifier);
            assertEquals(experiment.getIdentifier(), "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");
        } else
        {
            try
            {
                etlService.tryGetExperiment(session.getSessionToken(), experimentIdentifier);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testListSamplesWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = etlService.tryAuthenticate(user.getUserId(), PASSWORD);

        ListSampleCriteria criteria = ListSampleCriteria.createForExperiment(new TechId(23L)); // /TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            List<Sample> samples = etlService.listSamples(session.getSessionToken(), criteria);
            assertEntities(
                    "[/TEST-SPACE/TEST-PROJECT/EV-INVALID, /TEST-SPACE/TEST-PROJECT/EV-PARENT, "
                    + "/TEST-SPACE/TEST-PROJECT/EV-PARENT-NORMAL, /TEST-SPACE/TEST-PROJECT/EV-TEST, "
                    + "/TEST-SPACE/TEST-PROJECT/FV-TEST, /TEST-SPACE/TEST-PROJECT/SAMPLE-TO-DELETE]",
                    samples);
        } else
        {
            try
            {
                etlService.listSamples(session.getSessionToken(), criteria);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testListSamplesByCriteriaWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = etlService.tryAuthenticate(user.getUserId(), PASSWORD);

        ListSamplesByPropertyCriteria criteria = new ListSamplesByPropertyCriteria("COMMENT", "test comment", "TEST-SPACE", null);

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            List<Sample> samples = etlService.listSamplesByCriteria(session.getSessionToken(), criteria);
            assertEntities("[/TEST-SPACE/TEST-PROJECT/EV-TEST]", samples);
        } else
        {
            try
            {
                etlService.listSamplesByCriteria(session.getSessionToken(), criteria);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testTryGetSampleWithExperimentWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = etlService.tryAuthenticate(user.getUserId(), PASSWORD);

        SampleIdentifier sampleIdentifier = new SampleIdentifier(TEST_PROJECT_IDENTIFIER, "FV-TEST");

        if (user.isInstanceUser() || (user.isETLServerUser() && user.isTestSpaceUser()))
        {
            Sample sample = etlService.tryGetSampleWithExperiment(session.getSessionToken(), sampleIdentifier);
            assertEquals(sample.getIdentifier(), sampleIdentifier.toString());
        } else
        {
            try
            {
                etlService.tryGetSampleWithExperiment(session.getSessionToken(), sampleIdentifier);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testTryGetSampleByPermIdWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = etlService.tryAuthenticate(user.getUserId(), PASSWORD);

        PermId samplePermId = new PermId("201206191219327-1054"); // /TEST-SPACE/FV-TEST

        if (user.isInstanceUser() || (user.isETLServerUser() && user.isTestSpaceUser()))
        {
            Sample sample = etlService.tryGetSampleByPermId(session.getSessionToken(), samplePermId);
            assertEquals(sample.getIdentifier(), "/TEST-SPACE/TEST-PROJECT/FV-TEST");
        } else
        {
            try
            {
                etlService.tryGetSampleByPermId(session.getSessionToken(), samplePermId);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testTryGetSampleIdentifierWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = etlService.tryAuthenticate(user.getUserId(), PASSWORD);

        String samplePermId = "201206191219327-1054"; // /TEST-SPACE/FV-TEST

        if (user.isInstanceUser() || (user.isETLServerUser() && user.isTestSpaceUser()))
        {
            SampleIdentifier sampleIdentifier = etlService.tryGetSampleIdentifier(session.getSessionToken(), samplePermId);
            assertEquals(sampleIdentifier.toString(), "/TEST-SPACE/TEST-PROJECT/FV-TEST");
        } else
        {
            try
            {
                etlService.tryGetSampleIdentifier(session.getSessionToken(), samplePermId);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testListSampleByPermIdWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = etlService.tryAuthenticate(user.getUserId(), PASSWORD);

        String samplePermId = "201206191219327-1054"; // /TEST-SPACE/FV-TEST

        if (user.isInstanceUser() || (user.isETLServerUser() && user.isTestSpaceUser()))
        {
            Map<String, SampleIdentifier> samples = etlService.listSamplesByPermId(session.getSessionToken(), Arrays.asList(samplePermId));
            assertEquals(samples.get(samplePermId).toString(), "/TEST-SPACE/TEST-PROJECT/FV-TEST");
        } else
        {
            try
            {
                etlService.listSamplesByPermId(session.getSessionToken(), Arrays.asList(samplePermId));
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testListDataSetsBySampleIDWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = etlService.tryAuthenticate(user.getUserId(), PASSWORD);

        TechId sampleTechId = new TechId(1054L); // /TEST-SPACE/FV-TEST

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            List<AbstractExternalData> dataSets = etlService.listDataSetsBySampleID(session.getSessionToken(), sampleTechId, true);
            assertEntities("[20120628092259000-41]", dataSets);
        } else
        {
            try
            {
                etlService.listDataSetsBySampleID(session.getSessionToken(), sampleTechId, true);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testListDataSetsByCodeWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = etlService.tryAuthenticate(user.getUserId(), PASSWORD);

        String dataSetCode = "20120628092259000-41";

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            List<AbstractExternalData> dataSets = etlService.listDataSetsByCode(session.getSessionToken(), Arrays.asList(dataSetCode));
            assertEntities("[20120628092259000-41]", dataSets);
        } else
        {
            try
            {
                etlService.listDataSetsByCode(session.getSessionToken(), Arrays.asList(dataSetCode));
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testTryGetPropertiesOfTopSampleWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = etlService.tryAuthenticate(user.getUserId(), PASSWORD);

        SampleIdentifier sampleIdentifier = new SampleIdentifier(TEST_PROJECT_IDENTIFIER, "FV-TEST");

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            IEntityProperty[] properties = etlService.tryGetPropertiesOfTopSample(session.getSessionToken(), sampleIdentifier);
            assertEquals(properties[0].getMaterial().getCode(), "BACTERIUM-X");
        } else
        {
            try
            {
                etlService.tryGetPropertiesOfTopSample(session.getSessionToken(), sampleIdentifier);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testTryGetPropertiesOfSampleWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = etlService.tryAuthenticate(user.getUserId(), PASSWORD);

        SampleIdentifier sampleIdentifier = new SampleIdentifier(TEST_PROJECT_IDENTIFIER, "FV-TEST");

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            IEntityProperty[] properties = etlService.tryGetPropertiesOfSample(session.getSessionToken(), sampleIdentifier);
            assertEquals(properties[0].getMaterial().getCode(), "BACTERIUM-X");
        } else
        {
            try
            {
                etlService.tryGetPropertiesOfSample(session.getSessionToken(), sampleIdentifier);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testRegisterExperimentWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = etlService.tryAuthenticate(user.getUserId(), PASSWORD);

        NewExperiment newExperiment = createNewExperiment("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST-2");

        if (user.isInstanceUser() || (user.isETLServerUser() && user.isTestSpaceUser()))
        {
            etlService.registerExperiment(session.getSessionToken(), newExperiment);
            Experiment experiment = etlService.tryGetExperiment(session.getSessionToken(),
                    new ExperimentIdentifier("TEST-SPACE", "TEST-PROJECT", "EXP-SPACE-TEST-2"));
            assertEquals(experiment.getExperimentType().getCode(), newExperiment.getExperimentTypeCode());
        } else
        {
            try
            {
                etlService.registerExperiment(session.getSessionToken(), newExperiment);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testRegisterSampleWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = etlService.tryAuthenticate(user.getUserId(), PASSWORD);

        NewSample newSample = createNewSample("/TEST-SPACE/TEST-SAMPLE", "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");

        if (user.isInstanceUser() || (user.isETLServerUser() && user.isTestSpaceUser()))
        {
            etlService.registerSample(session.getSessionToken(), newSample, null);
            Sample sample = etlService.tryGetSampleWithExperiment(session.getSessionToken(),
                    new SampleIdentifier(TEST_PROJECT_IDENTIFIER, "TEST-SAMPLE"));
            assertEquals(sample.getExperiment().getIdentifier(), newSample.getExperimentIdentifier());
        } else
        {
            try
            {
                etlService.registerSample(session.getSessionToken(), newSample, null);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testRegisterSamplesWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = etlService.tryAuthenticate(user.getUserId(), PASSWORD);

        NewSample newSample = createNewSample("/TEST-SPACE/TEST-SAMPLE", "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");
        NewSamplesWithTypes newSamples = new NewSamplesWithTypes(newSample.getSampleType(), Arrays.asList(newSample));

        if (user.isInstanceUser() || (user.isETLServerUser() && user.isTestSpaceUser()))
        {
            etlService.registerSamples(session.getSessionToken(), Arrays.asList(newSamples), null);
            Sample sample = etlService.tryGetSampleWithExperiment(session.getSessionToken(),
                    new SampleIdentifier(TEST_PROJECT_IDENTIFIER, "TEST-SAMPLE"));
            assertEquals(sample.getExperiment().getIdentifier(), newSample.getExperimentIdentifier());
        } else
        {
            try
            {
                etlService.registerSamples(session.getSessionToken(), Arrays.asList(newSamples), null);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testUpdateSampleWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = etlService.tryAuthenticate(user.getUserId(), PASSWORD);

        SampleUpdatesDTO updates = createSampleUpdates(1055, "/TEST-SPACE/TEST-PROJECT/EV-TEST", "COMMENT", "updated comment");

        if (user.isInstanceUser() || (user.isETLServerUser() && user.isTestSpaceUser()))
        {
            etlService.updateSample(session.getSessionToken(), updates);
            IEntityProperty[] properties = etlService.tryGetPropertiesOfSample(session.getSessionToken(),
                    new SampleIdentifier(TEST_PROJECT_IDENTIFIER, "EV-TEST"));
            assertEquals(properties[0].getValue(), updates.getProperties().get(0).getValue());
        } else
        {
            try
            {
                etlService.updateSample(session.getSessionToken(), updates);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testRegisterDataSetWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = etlService.tryAuthenticate(user.getUserId(), PASSWORD);

        NewExternalData newData = createNewDataSet("TEST-DATASET");

        if (user.isInstanceUser() || (user.isETLServerUser() && user.isTestSpaceUser()))
        {
            etlService.registerDataSet(session.getSessionToken(), new ExperimentIdentifier("TEST-SPACE", "TEST-PROJECT", "EXP-SPACE-TEST"), newData);
            AbstractExternalData data = etlService.tryGetDataSet(session.getSessionToken(), newData.getCode());
            assertEquals(data.tryGetAsDataSet().getLocation(), newData.getLocation());
        } else
        {
            try
            {
                etlService.registerDataSet(session.getSessionToken(), new ExperimentIdentifier("TEST-SPACE", "TEST-PROJECT", "EXP-SPACE-TEST"),
                        newData);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testRegisterSampleAndDataSetWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = etlService.tryAuthenticate(user.getUserId(), PASSWORD);

        NewSample newSample = createNewSample("/TEST-SPACE/TEST-SAMPLE", "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");
        NewExternalData newExternalData = createNewDataSet("TEST-DATASET");

        if (user.isInstanceUser() || (user.isETLServerUser() && user.isTestSpaceUser()))
        {
            etlService.registerSampleAndDataSet(session.getSessionToken(), newSample, newExternalData, null);
            Sample sample = etlService.tryGetSampleWithExperiment(session.getSessionToken(),
                    new SampleIdentifier(TEST_PROJECT_IDENTIFIER, "TEST-SAMPLE"));
            assertEquals(sample.getExperiment().getIdentifier(), newSample.getExperimentIdentifier());
        } else
        {
            try
            {
                etlService.registerSampleAndDataSet(session.getSessionToken(), newSample, newExternalData, null);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testUpdateSampleAndRegisterDataSetWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = etlService.tryAuthenticate(user.getUserId(), PASSWORD);

        SampleUpdatesDTO sampleUpdates = createSampleUpdates(1055, "/TEST-SPACE/TEST-PROJECT/EV-TEST", "COMMENT", "updated comment");
        NewExternalData newExternalData = createNewDataSet("TEST-DATASET");

        if (user.isInstanceUser() || (user.isETLServerUser() && user.isTestSpaceUser()))
        {
            etlService.updateSampleAndRegisterDataSet(session.getSessionToken(), sampleUpdates, newExternalData);
            IEntityProperty[] properties = etlService.tryGetPropertiesOfSample(session.getSessionToken(),
                    new SampleIdentifier(TEST_PROJECT_IDENTIFIER, "EV-TEST"));
            assertEquals(properties[0].getValue(), sampleUpdates.getProperties().get(0).getValue());
        } else
        {
            try
            {
                etlService.updateSampleAndRegisterDataSet(session.getSessionToken(), sampleUpdates, newExternalData);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testSearchForExperimentsWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = etlService.tryAuthenticate(user.getUserId(), PASSWORD);

        SearchCriteria criteria = new SearchCriteria();
        criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.PERM_ID, "201206190940555-1032")); // /TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST

        if (user.isInstanceUser() || (user.isETLServerUser() && user.isTestSpaceUser()))
        {
            List<Experiment> experiments = etlService.searchForExperiments(session.getSessionToken(), criteria);
            assertEntities("[/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST]", experiments);
        } else if (user.isETLServerUser() && false == user.isTestSpaceUser())
        {
            List<Experiment> experiments = etlService.searchForExperiments(session.getSessionToken(), criteria);
            assertEntities("[]", experiments);
        } else
        {
            try
            {
                etlService.searchForExperiments(session.getSessionToken(), criteria);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testSearchForSamplesWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = etlService.tryAuthenticate(user.getUserId(), PASSWORD);

        SearchCriteria criteria = new SearchCriteria();
        criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.PERM_ID, "201206191219327-1054")); // /TEST-SPACE/FV-TEST

        if (user.isInstanceUser() || (user.isETLServerUser() && user.isTestSpaceUser()))
        {
            List<Sample> samples = etlService.searchForSamples(session.getSessionToken(), criteria);
            assertEntities("[/TEST-SPACE/TEST-PROJECT/FV-TEST]", samples);
        } else if (user.isETLServerUser() && false == user.isTestSpaceUser())
        {
            List<Sample> samples = etlService.searchForSamples(session.getSessionToken(), criteria);
            assertEntities("[]", samples);
        } else
        {
            try
            {
                etlService.searchForSamples(session.getSessionToken(), criteria);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testFilterToVisibleExperimentsWithMainUserWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = etlService.tryAuthenticate(user.getUserId(), PASSWORD);

        if (user.isInstanceUser() || (user.isETLServerUser() && user.isTestSpaceUser()))
        {
            List<String> experiments = etlService.filterToVisibleExperiments(session.getSessionToken(), TEST_USER,
                    Arrays.asList("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST"));
            assertEquals("[/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST]", experiments.toString());
        } else
        {
            try
            {
                etlService.filterToVisibleExperiments(session.getSessionToken(), TEST_USER, Arrays.asList("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST"));
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testFilterToVisibleExperimentsWithParameterUserWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = etlService.tryAuthenticate(TEST_USER, PASSWORD);

        List<String> experiments = etlService.filterToVisibleExperiments(session.getSessionToken(), user.getUserId(),
                Arrays.asList("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST"));

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            assertEquals("[/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST]", experiments.toString());
        } else
        {
            assertEquals("[]", experiments.toString());
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testFilterToVisibleSamplesWithMainUserWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = etlService.tryAuthenticate(user.getUserId(), PASSWORD);

        if (user.isInstanceUser() || (user.isETLServerUser() && user.isTestSpaceUser()))
        {
            List<String> samples = etlService.filterToVisibleSamples(session.getSessionToken(), TEST_USER, 
                    Arrays.asList("/TEST-SPACE/TEST-PROJECT/FV-TEST"));
            assertEquals(samples.toString(), "[/TEST-SPACE/TEST-PROJECT/FV-TEST]");
        } else
        {
            try
            {
                etlService.filterToVisibleSamples(session.getSessionToken(), TEST_USER, 
                        Arrays.asList("/TEST-SPACE/TEST-PROJECT/FV-TEST"));
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testFilterToVisibleSamplesWithParameterUserWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = etlService.tryAuthenticate(TEST_USER, PASSWORD);

        List<String> samples = etlService.filterToVisibleSamples(session.getSessionToken(), user.getUserId(), 
                Arrays.asList("/TEST-SPACE/TEST-PROJECT/FV-TEST"));

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            assertEquals(samples.toString(), "[/TEST-SPACE/TEST-PROJECT/FV-TEST]");
        } else
        {
            assertEquals(samples.toString(), "[]");
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testPerformEntityOperationsWithNewProjectWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO adminSession = etlService.tryAuthenticate(TEST_USER, PASSWORD);

        NewProject newProject = new NewProject("/TEST-SPACE/PA_TEST", null);

        AtomicEntityOperationDetails operation =
                new AtomicEntityOperationDetails(null, user.getUserId(), Collections.emptyList(), Arrays.asList(newProject), Collections.emptyList(),
                        Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyMap(),
                        Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                        Collections.emptyList());

        if (user.isInstanceUser() || user.isTestSpaceUser())
        {
            AtomicEntityOperationResult result = etlService.performEntityOperations(adminSession.getSessionToken(), operation);
            assertEquals(result.getProjectsCreatedCount(), 1);
        } else
        {
            try
            {
                etlService.performEntityOperations(adminSession.getSessionToken(), operation);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testPerformEntityOperationsWithProjectUpdateWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO adminSession = etlService.tryAuthenticate(TEST_USER, PASSWORD);

        ProjectUpdatesDTO projectUpdate = new ProjectUpdatesDTO();
        projectUpdate.setTechId(new TechId(5L)); // /TEST-SPACE/TEST-PROJECT
        projectUpdate.setAttachments(Collections.<NewAttachment> emptyList());

        AtomicEntityOperationDetails operation =
                new AtomicEntityOperationDetails(null, user.getUserId(), Collections.emptyList(), Collections.emptyList(),
                        Arrays.asList(projectUpdate), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                        Collections.emptyList(), Collections.emptyMap(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                        Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            AtomicEntityOperationResult result = etlService.performEntityOperations(adminSession.getSessionToken(), operation);
            assertEquals(result.getProjectsUpdatedCount(), 1);
        } else
        {
            try
            {
                etlService.performEntityOperations(adminSession.getSessionToken(), operation);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testPerformEntityOperationsWithNewExperimentWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO adminSession = etlService.tryAuthenticate(TEST_USER, PASSWORD);

        NewExperiment newExperiment = new NewExperiment("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST-2", "SIRNA_HCS");
        newExperiment.setProperties(new IEntityProperty[] { createEntityProperty("DESCRIPTION", "test description") });

        AtomicEntityOperationDetails operation =
                new AtomicEntityOperationDetails(null, user.getUserId(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                        Arrays.asList(newExperiment), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                        Collections.emptyMap(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                        Collections.emptyList(), Collections.emptyList());

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            AtomicEntityOperationResult result = etlService.performEntityOperations(adminSession.getSessionToken(), operation);
            assertEquals(result.getExperimentsCreatedCount(), 1);
        } else
        {
            try
            {
                etlService.performEntityOperations(adminSession.getSessionToken(), operation);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testPerformEntityOperationsWithExperimentUpdateWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO adminSession = etlService.tryAuthenticate(TEST_USER, PASSWORD);

        ExperimentUpdatesDTO updates = new ExperimentUpdatesDTO();
        updates.setExperimentId(new TechId(23L)); // /TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST
        updates.setProperties(Arrays.asList(new IEntityProperty[] { createEntityProperty("DESCRIPTION", "test description") }));
        updates.setAttachments(new ArrayList<NewAttachment>());

        AtomicEntityOperationDetails operation =
                new AtomicEntityOperationDetails(null, user.getUserId(), Collections.emptyList(), Collections.emptyList(),
                        Collections.emptyList(), Collections.emptyList(), Arrays.asList(updates), Collections.emptyList(),
                        Collections.emptyList(), Collections.emptyMap(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                        Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            AtomicEntityOperationResult result = etlService.performEntityOperations(adminSession.getSessionToken(), operation);
            assertEquals(result.getExperimentsUpdatedCount(), 1);
        } else
        {
            try
            {
                etlService.performEntityOperations(adminSession.getSessionToken(), operation);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testPerformEntityOperationsWithNewSampleWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO adminSession = etlService.tryAuthenticate(TEST_USER, PASSWORD);

        NewSample newSample = createNewSample("/TEST-SPACE/TEST-SAMPLE", "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");

        AtomicEntityOperationDetails operation =
                new AtomicEntityOperationDetails(null, user.getUserId(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                        Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Arrays.asList(newSample),
                        Collections.emptyMap(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                        Collections.emptyList(), Collections.emptyList());

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            AtomicEntityOperationResult result = etlService.performEntityOperations(adminSession.getSessionToken(), operation);
            assertEquals(result.getSamplesCreatedCount(), 1);
        } else
        {
            try
            {
                etlService.performEntityOperations(adminSession.getSessionToken(), operation);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testPerformEntityOperationsWithSampleUpdateWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO adminSession = etlService.tryAuthenticate(TEST_USER, PASSWORD);

        SampleUpdatesDTO sampleUpdate = createSampleUpdates(1055, "/TEST-SPACE/TEST-PROJECT/EV-TEST", "COMMENT", "updated comment");

        AtomicEntityOperationDetails operation =
                new AtomicEntityOperationDetails(null, user.getUserId(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                        Collections.emptyList(), Collections.emptyList(), Arrays.asList(sampleUpdate), Collections.emptyList(),
                        Collections.emptyMap(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                        Collections.emptyList(), Collections.emptyList());

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            AtomicEntityOperationResult result = etlService.performEntityOperations(adminSession.getSessionToken(), operation);
            assertEquals(result.getSamplesUpdatedCount(), 1);
        } else
        {
            try
            {
                etlService.performEntityOperations(adminSession.getSessionToken(), operation);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testPerformEntityOperationsWithNewDataSetWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO adminSession = etlService.tryAuthenticate(TEST_USER, PASSWORD);

        NewExternalData newDataSet = createNewDataSet("PA_PERFORM_ENTITY_OPERATIONS_DATA_SET");
        newDataSet.setExperimentIdentifierOrNull(new ExperimentIdentifier("TEST-SPACE", "TEST-PROJECT", "EXP-SPACE-TEST"));

        AtomicEntityOperationDetails operation =
                new AtomicEntityOperationDetails(null, user.getUserId(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                        Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                        Collections.emptyMap(), Collections.emptyList(), Arrays.asList(newDataSet), Collections.emptyList(), Collections.emptyList(),
                        Collections.emptyList(), Collections.emptyList());

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            AtomicEntityOperationResult result = etlService.performEntityOperations(adminSession.getSessionToken(), operation);
            assertEquals(result.getDataSetsCreatedCount(), 1);
        } else
        {
            try
            {
                etlService.performEntityOperations(adminSession.getSessionToken(), operation);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testPerformEntityOperationsWithDataSetUpdateWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO adminSession = etlService.tryAuthenticate(TEST_USER, PASSWORD);

        DataSetBatchUpdatesDTO dataSetUpdate = createDataSetUpdates(22, "20120619092259000-22", "COMMENT", "updated comment");

        AtomicEntityOperationDetails operation =
                new AtomicEntityOperationDetails(null, user.getUserId(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                        Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                        Collections.emptyMap(), Collections.emptyList(), Collections.emptyList(), Arrays.asList(dataSetUpdate),
                        Collections.emptyList(),
                        Collections.emptyList(), Collections.emptyList());

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            AtomicEntityOperationResult result = etlService.performEntityOperations(adminSession.getSessionToken(), operation);
            assertEquals(result.getDataSetsUpdatedCount(), 1);
        } else
        {
            try
            {
                etlService.performEntityOperations(adminSession.getSessionToken(), operation);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @DataProvider
    public Object[][] providerTestPerformEntityOperationsWithVocabularyUpdateAttributesAuthorization()
    {
        return new Object[][] {
                { "ORGANISM", TEST_INSTANCE_ETLSERVER, null, null },
                { "ORGANISM", TEST_INSTANCE_ETLSERVER, SYSTEM_USER, null },
                { "ORGANISM", TEST_INSTANCE_ETLSERVER, TEST_USER, null },
                { "ORGANISM", TEST_INSTANCE_ETLSERVER, TEST_GROUP_ADMIN,
                        "None of method roles '[INSTANCE_ADMIN, INSTANCE_ETL_SERVER]' could be found in roles of user 'admin'" },

                { "$PLATE_GEOMETRY", TEST_INSTANCE_ETLSERVER, null, "Internal vocabularies can be managed only by the system user" },
                { "$PLATE_GEOMETRY", TEST_INSTANCE_ETLSERVER, SYSTEM_USER, null },
                { "$PLATE_GEOMETRY", TEST_INSTANCE_ETLSERVER, TEST_USER, "Internal vocabularies can be managed only by the system user" },
                { "$PLATE_GEOMETRY", TEST_INSTANCE_ETLSERVER, TEST_GROUP_ADMIN,
                        "None of method roles '[INSTANCE_ADMIN, INSTANCE_ETL_SERVER]' could be found in roles of user 'admin'" },

                { "ORGANISM", TEST_SPACE_ETLSERVER_TESTSPACE, null,
                        "None of method roles '[INSTANCE_ADMIN, INSTANCE_ETL_SERVER]' could be found in roles of user 'test_space_etl_server'" },
                { "ORGANISM", TEST_SPACE_ETLSERVER_TESTSPACE, SYSTEM_USER,
                        "None of method roles '[INSTANCE_ADMIN, INSTANCE_ETL_SERVER]' could be found in roles of user 'test_space_etl_server'" },
                { "ORGANISM", TEST_SPACE_ETLSERVER_TESTSPACE, TEST_USER,
                        "None of method roles '[INSTANCE_ADMIN, INSTANCE_ETL_SERVER]' could be found in roles of user 'test_space_etl_server'" },
                { "ORGANISM", TEST_SPACE_ETLSERVER_TESTSPACE, TEST_GROUP_ADMIN,
                        "None of method roles '[INSTANCE_ADMIN, INSTANCE_ETL_SERVER]' could be found in roles of user 'test_space_etl_server'" },

                { "$PLATE_GEOMETRY", TEST_SPACE_ETLSERVER_TESTSPACE, null,
                        "None of method roles '[INSTANCE_ADMIN, INSTANCE_ETL_SERVER]' could be found in roles of user 'test_space_etl_server'" },
                { "$PLATE_GEOMETRY", TEST_SPACE_ETLSERVER_TESTSPACE, SYSTEM_USER,
                        "None of method roles '[INSTANCE_ADMIN, INSTANCE_ETL_SERVER]' could be found in roles of user 'test_space_etl_server'" },
                { "$PLATE_GEOMETRY", TEST_SPACE_ETLSERVER_TESTSPACE, TEST_USER,
                        "None of method roles '[INSTANCE_ADMIN, INSTANCE_ETL_SERVER]' could be found in roles of user 'test_space_etl_server'" },
                { "$PLATE_GEOMETRY", TEST_SPACE_ETLSERVER_TESTSPACE, TEST_GROUP_ADMIN,
                        "None of method roles '[INSTANCE_ADMIN, INSTANCE_ETL_SERVER]' could be found in roles of user 'test_space_etl_server'" },
        };
    }

    @Test(dataProvider = "providerTestPerformEntityOperationsWithVocabularyUpdateAttributesAuthorization")
    public void testPerformEntityOperationsWithVocabularyUpdateAttributesAuthorization(String vocabularyCode, String operationsPerformer,
            String vocabularyUpdater, String expectedError)
    {
        SessionContextDTO performerSession = etlService.tryAuthenticate(operationsPerformer, PASSWORD);

        VocabularyPE vocabulary = daoFactory.getVocabularyDAO().tryFindVocabularyByCode(vocabularyCode);

        VocabularyUpdatesDTO updates =
                new VocabularyUpdatesDTO(vocabulary.getId(), vocabulary.getCode(), "new description", vocabulary.isManagedInternally(),
                        vocabulary.isChosenFromList(), vocabulary.getURLTemplate(), Collections.emptyList());

        AtomicEntityOperationDetails operation =
                new AtomicEntityOperationDetails(null, vocabularyUpdater, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                        Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                        Collections.emptyMap(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                        Collections.emptyList(),
                        Collections.emptyList(), Arrays.asList(updates));

        assertExceptionMessage(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    etlService.performEntityOperations(performerSession.getSessionToken(), operation);
                }
            }, expectedError);
    }

    @DataProvider
    public Object[][] providerTestPerformEntityOperationsWithVocabularyUpdateNewTermsAuthorization()
    {
        return new Object[][] {
                { "ORGANISM", TEST_INSTANCE_ETLSERVER, null, null },
                { "ORGANISM", TEST_INSTANCE_ETLSERVER, SYSTEM_USER, null },
                { "ORGANISM", TEST_INSTANCE_ETLSERVER, TEST_USER, null },
                { "ORGANISM", TEST_INSTANCE_ETLSERVER, TEST_GROUP_ADMIN,
                        "None of method roles '[INSTANCE_ADMIN, INSTANCE_ETL_SERVER]' could be found in roles of user 'admin'" },

                { "$PLATE_GEOMETRY", TEST_INSTANCE_ETLSERVER, null, null },
                { "$PLATE_GEOMETRY", TEST_INSTANCE_ETLSERVER, SYSTEM_USER, null },
                { "$PLATE_GEOMETRY", TEST_INSTANCE_ETLSERVER, TEST_USER, null },
                { "$PLATE_GEOMETRY", TEST_INSTANCE_ETLSERVER, TEST_GROUP_ADMIN,
                        "None of method roles '[INSTANCE_ADMIN, INSTANCE_ETL_SERVER]' could be found in roles of user 'admin'" },

                { "ORGANISM", TEST_SPACE_ETLSERVER_TESTSPACE, null,
                        "None of method roles '[INSTANCE_ADMIN, INSTANCE_ETL_SERVER]' could be found in roles of user 'test_space_etl_server'" },
                { "ORGANISM", TEST_SPACE_ETLSERVER_TESTSPACE, SYSTEM_USER,
                        "None of method roles '[INSTANCE_ADMIN, INSTANCE_ETL_SERVER]' could be found in roles of user 'test_space_etl_server'" },
                { "ORGANISM", TEST_SPACE_ETLSERVER_TESTSPACE, TEST_USER,
                        "None of method roles '[INSTANCE_ADMIN, INSTANCE_ETL_SERVER]' could be found in roles of user 'test_space_etl_server'" },
                { "ORGANISM", TEST_SPACE_ETLSERVER_TESTSPACE, TEST_GROUP_ADMIN,
                        "None of method roles '[INSTANCE_ADMIN, INSTANCE_ETL_SERVER]' could be found in roles of user 'test_space_etl_server'" },

                { "$PLATE_GEOMETRY", TEST_SPACE_ETLSERVER_TESTSPACE, null,
                        "None of method roles '[INSTANCE_ADMIN, INSTANCE_ETL_SERVER]' could be found in roles of user 'test_space_etl_server'" },
                { "$PLATE_GEOMETRY", TEST_SPACE_ETLSERVER_TESTSPACE, SYSTEM_USER,
                        "None of method roles '[INSTANCE_ADMIN, INSTANCE_ETL_SERVER]' could be found in roles of user 'test_space_etl_server'" },
                { "$PLATE_GEOMETRY", TEST_SPACE_ETLSERVER_TESTSPACE, TEST_USER,
                        "None of method roles '[INSTANCE_ADMIN, INSTANCE_ETL_SERVER]' could be found in roles of user 'test_space_etl_server'" },
                { "$PLATE_GEOMETRY", TEST_SPACE_ETLSERVER_TESTSPACE, TEST_GROUP_ADMIN,
                        "None of method roles '[INSTANCE_ADMIN, INSTANCE_ETL_SERVER]' could be found in roles of user 'test_space_etl_server'" },
        };
    }

    @Test(dataProvider = "providerTestPerformEntityOperationsWithVocabularyUpdateNewTermsAuthorization")
    public void testPerformEntityOperationsWithVocabularyUpdateNewTermsAuthorization(String vocabularyCode, String operationsPerformer,
            String vocabularyUpdater, String expectedError)
    {
        SessionContextDTO performerSession = etlService.tryAuthenticate(operationsPerformer, PASSWORD);

        VocabularyPE vocabulary = daoFactory.getVocabularyDAO().tryFindVocabularyByCode(vocabularyCode);

        // the new term is official (in VocabuaryBO the flag is always set to true)
        NewVocabularyTerm newTerm = new NewVocabularyTerm("TEST_CODE", "test description", "test label", (long) vocabulary.getTerms().size() + 1);

        VocabularyUpdatesDTO updates =
                new VocabularyUpdatesDTO(vocabulary.getId(), vocabulary.getCode(), vocabulary.getDescription(), vocabulary.isManagedInternally(),
                        vocabulary.isChosenFromList(), vocabulary.getURLTemplate(), Arrays.asList(newTerm));

        AtomicEntityOperationDetails operation =
                new AtomicEntityOperationDetails(null, vocabularyUpdater, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                        Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                        Collections.emptyMap(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                        Collections.emptyList(),
                        Collections.emptyList(), Arrays.asList(updates));

        assertExceptionMessage(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    etlService.performEntityOperations(performerSession.getSessionToken(), operation);
                }
            }, expectedError);
    }

    @DataProvider
    public Object[][] providerTestPerformEntityOperationsWithVocabularyUpdateManagedInternallyAuthorization()
    {
        return new Object[][] {
                { "ORGANISM", TEST_INSTANCE_ETLSERVER, null, "Internal vocabularies can be managed only by the system user" },
                { "ORGANISM", TEST_INSTANCE_ETLSERVER, SYSTEM_USER, null },
                { "ORGANISM", TEST_INSTANCE_ETLSERVER, TEST_USER, "Internal vocabularies can be managed only by the system user" },
                { "ORGANISM", TEST_INSTANCE_ETLSERVER, TEST_GROUP_ADMIN,
                        "None of method roles '[INSTANCE_ADMIN, INSTANCE_ETL_SERVER]' could be found in roles of user 'admin'" },

                { "$PLATE_GEOMETRY", TEST_INSTANCE_ETLSERVER, null, "Internal vocabularies can be managed only by the system user" },
                { "$PLATE_GEOMETRY", TEST_INSTANCE_ETLSERVER, SYSTEM_USER, null },
                { "$PLATE_GEOMETRY", TEST_INSTANCE_ETLSERVER, TEST_USER, "Internal vocabularies can be managed only by the system user" },
                { "$PLATE_GEOMETRY", TEST_INSTANCE_ETLSERVER, TEST_GROUP_ADMIN,
                        "None of method roles '[INSTANCE_ADMIN, INSTANCE_ETL_SERVER]' could be found in roles of user 'admin'" },

                { "ORGANISM", TEST_SPACE_ETLSERVER_TESTSPACE, null,
                        "None of method roles '[INSTANCE_ADMIN, INSTANCE_ETL_SERVER]' could be found in roles of user 'test_space_etl_server'" },
                { "ORGANISM", TEST_SPACE_ETLSERVER_TESTSPACE, SYSTEM_USER,
                        "None of method roles '[INSTANCE_ADMIN, INSTANCE_ETL_SERVER]' could be found in roles of user 'test_space_etl_server'" },
                { "ORGANISM", TEST_SPACE_ETLSERVER_TESTSPACE, TEST_USER,
                        "None of method roles '[INSTANCE_ADMIN, INSTANCE_ETL_SERVER]' could be found in roles of user 'test_space_etl_server'" },
                { "ORGANISM", TEST_SPACE_ETLSERVER_TESTSPACE, TEST_GROUP_ADMIN,
                        "None of method roles '[INSTANCE_ADMIN, INSTANCE_ETL_SERVER]' could be found in roles of user 'test_space_etl_server'" },

                { "$PLATE_GEOMETRY", TEST_SPACE_ETLSERVER_TESTSPACE, null,
                        "None of method roles '[INSTANCE_ADMIN, INSTANCE_ETL_SERVER]' could be found in roles of user 'test_space_etl_server'" },
                { "$PLATE_GEOMETRY", TEST_SPACE_ETLSERVER_TESTSPACE, SYSTEM_USER,
                        "None of method roles '[INSTANCE_ADMIN, INSTANCE_ETL_SERVER]' could be found in roles of user 'test_space_etl_server'" },
                { "$PLATE_GEOMETRY", TEST_SPACE_ETLSERVER_TESTSPACE, TEST_USER,
                        "None of method roles '[INSTANCE_ADMIN, INSTANCE_ETL_SERVER]' could be found in roles of user 'test_space_etl_server'" },
                { "$PLATE_GEOMETRY", TEST_SPACE_ETLSERVER_TESTSPACE, TEST_GROUP_ADMIN,
                        "None of method roles '[INSTANCE_ADMIN, INSTANCE_ETL_SERVER]' could be found in roles of user 'test_space_etl_server'" },
        };
    }

    @Test(dataProvider = "providerTestPerformEntityOperationsWithVocabularyUpdateManagedInternallyAuthorization")
    public void testPerformEntityOperationsWithVocabularyUpdateManagedInternallyAuthorization(String vocabularyCode, String operationsPerformer,
            String vocabularyUpdater, String expectedError)
    {
        SessionContextDTO performerSession = etlService.tryAuthenticate(operationsPerformer, PASSWORD);

        VocabularyPE vocabulary = daoFactory.getVocabularyDAO().tryFindVocabularyByCode(vocabularyCode);

        VocabularyUpdatesDTO updates =
                new VocabularyUpdatesDTO(vocabulary.getId(), vocabulary.getCode(), vocabulary.getDescription(),
                        vocabulary.isManagedInternally() ? false : true,
                        vocabulary.isChosenFromList(), vocabulary.getURLTemplate(), Collections.emptyList());

        AtomicEntityOperationDetails operation =
                new AtomicEntityOperationDetails(null, vocabularyUpdater, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                        Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                        Collections.emptyMap(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                        Collections.emptyList(),
                        Collections.emptyList(), Arrays.asList(updates));

        assertExceptionMessage(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    etlService.performEntityOperations(performerSession.getSessionToken(), operation);
                }
            }, expectedError);
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testTryGetDataSetLocationWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = etlService.tryAuthenticate(user.getUserId(), PASSWORD);

        String dataSetCode = "20120619092259000-22";

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            IDatasetLocationNode location = etlService.tryGetDataSetLocation(session.getSessionToken(), dataSetCode);
            assertEquals(location.getLocation().getDataSetCode(), dataSetCode);
        } else
        {
            try
            {
                etlService.tryGetDataSetLocation(session.getSessionToken(), dataSetCode);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testTryGetLocalDataSetWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = etlService.tryAuthenticate(user.getUserId(), PASSWORD);

        String dataSetCode = "20120619092259000-22";
        String dataStore = "STANDARD";

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            AbstractExternalData dataSet = etlService.tryGetLocalDataSet(session.getSessionToken(), dataSetCode, dataStore);
            assertEquals(dataSet.getCode(), dataSetCode);
        } else
        {
            try
            {
                etlService.tryGetLocalDataSet(session.getSessionToken(), dataSetCode, dataStore);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testTryGetDataSetWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = etlService.tryAuthenticate(user.getUserId(), PASSWORD);

        String dataSetCode = "20120619092259000-22";

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            AbstractExternalData dataSet = etlService.tryGetDataSet(session.getSessionToken(), dataSetCode);
            assertEquals(dataSet.getCode(), dataSetCode);
        } else
        {
            try
            {
                etlService.tryGetDataSet(session.getSessionToken(), dataSetCode);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testTryGetThinDataSetWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = etlService.tryAuthenticate(user.getUserId(), PASSWORD);

        String dataSetCode = "20120619092259000-22";

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            AbstractExternalData dataSet = etlService.tryGetThinDataSet(session.getSessionToken(), dataSetCode);
            assertEquals(dataSet.getCode(), dataSetCode);
        } else
        {
            try
            {
                etlService.tryGetThinDataSet(session.getSessionToken(), dataSetCode);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testCheckDataSetAccessWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = etlService.tryAuthenticate(user.getUserId(), PASSWORD);

        String dataSetCode = "20120619092259000-22";

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            etlService.checkDataSetAccess(session.getSessionToken(), dataSetCode);
        } else
        {
            try
            {
                etlService.checkDataSetAccess(session.getSessionToken(), dataSetCode);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testCheckDataSetCollectionAccessWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = etlService.tryAuthenticate(user.getUserId(), PASSWORD);

        String dataSetCode = "20120619092259000-22";

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            etlService.checkDataSetCollectionAccess(session.getSessionToken(), Arrays.asList(dataSetCode));
        } else
        {
            try
            {
                etlService.checkDataSetCollectionAccess(session.getSessionToken(), Arrays.asList(dataSetCode));
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    private NewExperiment createNewExperiment(String experimentIdentifier)
    {
        NewExperiment newExperiment = new NewExperiment();
        newExperiment.setExperimentTypeCode("SIRNA_HCS");
        newExperiment.setIdentifier(experimentIdentifier);
        newExperiment.setProperties(new IEntityProperty[] { createEntityProperty("DESCRIPTION", "test description") });

        return newExperiment;
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

    private SampleUpdatesDTO createSampleUpdates(long sampleId, String sampleIdentifier, String propertyCode, String propertyValue)
    {
        SampleIdentifier sid = SampleIdentifierFactory.parse(sampleIdentifier);
        SampleUpdatesDTO updates = new SampleUpdatesDTO(new TechId(sampleId), null, null, 
                sid.getProjectLevel(), null, 0, sid, null, null);
        updates.setProperties(Arrays.asList(createEntityProperty(propertyCode, propertyValue)));
        updates.setUpdateExperimentLink(false);
        return updates;
    }

    private NewExternalData createNewDataSet(String dataSetCode)
    {
        DataSetType type = new DataSetType();
        type.setCode("UNKNOWN");

        NewExternalData newData = new NewExternalData();
        newData.setDataSetType(type);
        newData.setDataSetKind(DataSetKind.PHYSICAL);
        newData.setDataStoreCode("STANDARD");
        newData.setCode(dataSetCode);
        newData.setFileFormatType(new FileFormatType(FileFormatType.DEFAULT_FILE_FORMAT_TYPE_CODE));
        newData.setLocation("location/1");
        newData.setStorageFormat(StorageFormat.PROPRIETARY);
        newData.setLocatorType(new LocatorType(LocatorType.DEFAULT_LOCATOR_TYPE_CODE));
        return newData;
    }

    private DataSetBatchUpdatesDTO createDataSetUpdates(long dataSetId, String dataSetCode, String propertyCode, String propertyValue)
    {
        DataSetBatchUpdatesDTO updates = new DataSetBatchUpdatesDTO();
        updates.setDatasetId(new TechId(dataSetId));
        updates.setDatasetCode(dataSetCode);
        updates.setProperties(Arrays.asList(createEntityProperty(propertyCode, propertyValue)));
        updates.setDetails(new DataSetBatchUpdateDetails());
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
