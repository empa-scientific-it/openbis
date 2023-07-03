/*
 * Copyright ETH 2010 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.systemtest.api.v1;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.fail;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationChangingService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Attachment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet.Connections;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Deletion;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DeletionType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityRegistrationDetails;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityRegistrationDetails.EntityRegistrationDetailsInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment.ExperimentInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.MaterialTypeIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.MetaprojectAssignments;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.PropertyTypeGroup;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample.SampleInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SampleFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.CompareMode;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseTimeAttribute;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.SearchOperator;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchSubCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SpaceWithProjectsAndRoleAssignments;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.experiment.ExperimentIdentifierId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.experiment.ExperimentPermIdId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.experiment.ExperimentTechIdId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.metaproject.MetaprojectIdentifierId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.metaproject.MetaprojectTechIdId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.project.ProjectIdentifierId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.project.ProjectPermIdId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.sample.SampleIdentifierId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.sample.SamplePermIdId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.sample.SampleTechIdId;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Grantee;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.systemtest.SystemTestCase;
import ch.systemsx.cisd.openbis.systemtest.authorization.ProjectAuthorizationUser;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;

import junit.framework.Assert;

/**
 * @author Franz-Josef Elmer
 */
@Test(groups = "system test")
public class GeneralInformationServiceTest extends SystemTestCase
{

    private static final Comparator<Sample> SAMPLE_COMPARATOR = new Comparator<Sample>()
        {
            @Override
            public int compare(Sample s1, Sample s2)
            {
                return s1.getIdentifier().compareTo(s2.getIdentifier());
            }
        };

    @Autowired
    private IGeneralInformationService generalInformationService;

    @Autowired
    private IGeneralInformationChangingService generalInformationChangingService;

    private String sessionToken;

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod()
    {
        sessionToken = generalInformationService.tryToAuthenticateForAllServices("test", "a");
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod()
    {
        generalInformationService.logout(sessionToken);
    }

    @Test
    public void testListSpaces()
    {
        List<SpaceWithProjectsAndRoleAssignments> spaces =
                generalInformationService.listSpacesWithProjectsAndRoleAssignments(sessionToken,
                        null);
        assertEquals(3, spaces.size());
        loginAsObserver();

        spaces =
                generalInformationService.listSpacesWithProjectsAndRoleAssignments(sessionToken,
                        null);

        assertEquals("TESTGROUP", spaces.get(0).getCode());
        assertEquals(1, spaces.size());
    }

    @Test
    public void testListProjects()
    {
        List<Project> projects = generalInformationService.listProjects(sessionToken);
        Collections.sort(projects, new Comparator<Project>()
            {
                @Override
                public int compare(Project p1, Project p2)
                {
                    return p1.getIdentifier().compareTo(p2.getIdentifier());
                }
            });
        assertEquals(
                "[/CISD/DEFAULT, /CISD/NEMO, /CISD/NOE, /TEST-SPACE/NOE, /TEST-SPACE/PROJECT-TO-DELETE, /TEST-SPACE/TEST-PROJECT, /TESTGROUP/TESTPROJ]",
                projects.toString());

        loginAsObserver();

        projects = generalInformationService.listProjects(sessionToken);
        assertEquals("[/TESTGROUP/TESTPROJ]", projects.toString());

        for (Project project : projects)
        {
            assertEquals("test", project.getRegistrationDetails().getModifierUserId());
        }
    }

    @Test
    public void testSearchForSamples()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE,
                "MASTER_PLATE"));

        List<Sample> samples =
                generalInformationService.searchForSamples(sessionToken, searchCriteria);
        assertEntities("[/CISD/EMPTY-MP, /CISD/MP002-1, /CISD/MP1-MIXED, /CISD/MP2-NO-CL, /MP]",
                samples);
        assertEquals(1225873363584L, samples.get(0).getRegistrationDetails().getRegistrationDate()
                .getTime());
        assertEquals(1237369819475L, samples.get(0).getRegistrationDetails().getModificationDate()
                .getTime());

        loginAsObserver();
        samples = generalInformationService.searchForSamples(sessionToken, searchCriteria);

        assertEntities("[/MP]", samples);
    }

    @Test
    public void testSearchForSamplesOnBehalfOfUser()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE,
                "*TEST*"));

        // executed by test
        List<Sample> testResult =
                generalInformationService.searchForSamples(sessionToken, searchCriteria);
        assertEntities(
                "[/CISD/NEMO/CP-TEST-1, /CISD/NEMO/CP-TEST-3, /CISD/NEMO/DYNA-TEST-1, /CISD/NOE/CP-TEST-2, "
                + "/TEST-SPACE/NOE/CP-TEST-4, /TEST-SPACE/TEST-PROJECT/EV-TEST, /TEST-SPACE/TEST-PROJECT/FV-TEST]",
                testResult);

        // executed by test on behalf of test_space
        List<Sample> onBehalfOfTestSpaceResult =
                generalInformationService.searchForSamplesOnBehalfOfUser(sessionToken,
                        searchCriteria, EnumSet.of(SampleFetchOption.BASIC), "test_space");
        assertEntities("[/TEST-SPACE/NOE/CP-TEST-4, /TEST-SPACE/TEST-PROJECT/EV-TEST, /TEST-SPACE/TEST-PROJECT/FV-TEST]", 
                onBehalfOfTestSpaceResult);

        // executed by test_space
        generalInformationService.logout(sessionToken);
        sessionToken =
                generalInformationService.tryToAuthenticateForAllServices("test_space", "password");

        List<Sample> testSpaceResult =
                generalInformationService.searchForSamples(sessionToken, searchCriteria);
        assertEntities("[/TEST-SPACE/NOE/CP-TEST-4, /TEST-SPACE/TEST-PROJECT/EV-TEST, /TEST-SPACE/TEST-PROJECT/FV-TEST]", 
                testSpaceResult);
    }

    @Test(expectedExceptions = AuthorizationFailureException.class)
    public void testSearchForSamplesOnBehalfOfUserExecutedByNotInstanceUser()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE,
                "SOME_CODE"));

        generalInformationService.logout(sessionToken);
        sessionToken =
                generalInformationService.tryToAuthenticateForAllServices("test_role", "password");
        generalInformationService.searchForSamplesOnBehalfOfUser(sessionToken, searchCriteria,
                EnumSet.of(SampleFetchOption.BASIC), "admin");
    }

    @Test
    public void testSearchForSamplesOnBehalfOfUserByMetaprojectName()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(
                MatchClauseAttribute.METAPROJECT, "TEST_METAPROJECTS"));

        // executed by test
        List<Sample> testResult =
                generalInformationService.searchForSamples(sessionToken, searchCriteria);
        assertEntities("[/TEST-SPACE/TEST-PROJECT/EV-TEST]", testResult);

        // executed by test on behalf of test_space
        List<Sample> onBehalfOfTestSpaceResult =
                generalInformationService.searchForSamplesOnBehalfOfUser(sessionToken,
                        searchCriteria, EnumSet.of(SampleFetchOption.BASIC), "test_space");
        assertEntities("[/TEST-SPACE/TEST-PROJECT/EV-TEST, /TEST-SPACE/TEST-PROJECT/FV-TEST]", onBehalfOfTestSpaceResult);

        // executed by test_space
        generalInformationService.logout(sessionToken);
        sessionToken =
                generalInformationService.tryToAuthenticateForAllServices("test_space", "password");

        List<Sample> testSpaceResult =
                generalInformationService.searchForSamples(sessionToken, searchCriteria);
        assertEntities("[/TEST-SPACE/TEST-PROJECT/EV-TEST, /TEST-SPACE/TEST-PROJECT/FV-TEST]", testSpaceResult);
    }

    @Test
    public void testSearchForSamplesByCodeWithWildcard()
    {
        final SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "B1B3*"));
        final List<Sample> samples = generalInformationService.searchForSamples(sessionToken, searchCriteria);
        assertEntities("[/CISD/B1B3, /CISD/B1B3:B01, /CISD/B1B3:B03]", samples);
    }

    @Test
    public void testSearchForSamplesByAnyFieldMatchingCodeOfComponent()
    {
        final SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAnyFieldMatch("B1B3:B01"));
        final List<Sample> samples = generalInformationService.searchForSamples(sessionToken, searchCriteria);
        assertEntities("[/CISD/B1B3:B01]", samples);
    }

    @Test
    public void testSearchForSamplesByPermIdAndCheckModifier()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(
                MatchClauseAttribute.PERM_ID, "201206191219327-1054"));

        List<Sample> samples =
                generalInformationService.searchForSamples(sessionToken, searchCriteria);
        assertEntities("[/TEST-SPACE/TEST-PROJECT/FV-TEST]", samples);
        assertEquals("test_role", samples.get(0).getRegistrationDetails().getModifierUserId());
    }

    @Test
    public void testSearchForSamplesByPermIdByAnyField()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAnyFieldMatch("201206191219327-1054"));

        List<Sample> samples =
                generalInformationService.searchForSamples(sessionToken, searchCriteria);
        assertEntities("[/TEST-SPACE/TEST-PROJECT/FV-TEST]", samples);
    }

    @Test
    public void testSearchForSamplesByRegistratorUserId()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.REGISTRATOR_USER_ID, "etlserver"));

        List<Sample> samples =
                generalInformationService.searchForSamples(sessionToken, searchCriteria);
        assertEntities("[/CISD/DEFAULT/RP1-A2X, /CISD/DEFAULT/RP2-A1X]", samples);
    }

    @Test
    public void testSearchForSamplesByRegistratorFirstName()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.REGISTRATOR_FIRST_NAME, "\"John 2\""));

        List<Sample> samples =
                generalInformationService.searchForSamples(sessionToken, searchCriteria);
        assertEntities("[/CISD/DEFAULT/RP1-A2X, /CISD/DEFAULT/RP2-A1X]", samples);
    }

    @Test
    public void testSearchForSamplesByRegistratorLastName()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.REGISTRATOR_LAST_NAME, "\"ETL Server\""));

        List<Sample> samples =
                generalInformationService.searchForSamples(sessionToken, searchCriteria);
        assertEntities("[/CISD/DEFAULT/RP1-A2X, /CISD/DEFAULT/RP2-A1X]", samples);
    }

    @Test
    public void testSearchForSamplesByRegistratorEmail()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.REGISTRATOR_EMAIL, "\"etlserver@systemsx.ch\""));

        List<Sample> samples =
                generalInformationService.searchForSamples(sessionToken, searchCriteria);
        assertEntities("[/CISD/DEFAULT/RP1-A2X, /CISD/DEFAULT/RP2-A1X]", samples);
    }

    @Test
    public void testSearchForSamplesByModifierUserId()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.MODIFIER_USER_ID, "etlserver"));

        List<Sample> samples =
                generalInformationService.searchForSamples(sessionToken, searchCriteria);
        assertEntities("[/CISD/DEFAULT/RP1-B1X]", samples);
    }

    @Test
    public void testSearchForSamplesByModifierFirstName()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.MODIFIER_FIRST_NAME, "\"John 2\""));

        List<Sample> samples =
                generalInformationService.searchForSamples(sessionToken, searchCriteria);
        assertEntities("[/CISD/DEFAULT/RP1-B1X]", samples);
    }

    @Test
    public void testSearchForSamplesByModifierLastName()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.MODIFIER_LAST_NAME, "\"ETL Server\""));

        List<Sample> samples =
                generalInformationService.searchForSamples(sessionToken, searchCriteria);
        assertEntities("[/CISD/DEFAULT/RP1-B1X]", samples);
    }

    @Test
    public void testSearchForSamplesByModifierEmail()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.MODIFIER_EMAIL, "\"etlserver@systemsx.ch\""));

        List<Sample> samples =
                generalInformationService.searchForSamples(sessionToken, searchCriteria);
        assertEntities("[/CISD/DEFAULT/RP1-B1X]", samples);
    }

    @Test
    public void testSearchForSamplesByMetaprojectName()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(
                MatchClauseAttribute.METAPROJECT, "TEST_METAPROJECTS"));

        List<Sample> samples =
                generalInformationService.searchForSamples(sessionToken, searchCriteria);
        assertEntities("[/TEST-SPACE/TEST-PROJECT/EV-TEST]", samples);
    }

    @Test(enabled = false)
    public void testSearchForSamplesByMetaprojectNameWithDifferentCase()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(
                MatchClauseAttribute.METAPROJECT, "TeSt_MeTaPrOjEcTs"));

        List<Sample> samples =
                generalInformationService.searchForSamples(sessionToken, searchCriteria);
        assertEntities("[/TEST-SPACE/TEST-PROJECT/EV-TEST]", samples);
    }

    @Test
    public void testSearchForSamplesByMetaprojectNameWithWildcards()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(
                MatchClauseAttribute.METAPROJECT, "*TeSt_MeTa*"));

        List<Sample> samples =
                generalInformationService.searchForSamples(sessionToken, searchCriteria);
        assertEntities("[/CISD/3V-125, /TEST-SPACE/TEST-PROJECT/EV-TEST]", samples);
    }

    @Test
    public void testSearchForSamplesByMetaprojectNameOwnedBySomebodyElse()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(
                MatchClauseAttribute.METAPROJECT, "TEST_METAPROJECTS_2"));

        List<Sample> samples =
                generalInformationService.searchForSamples(sessionToken, searchCriteria);
        assertEntities("[]", samples);
    }

    @Test
    public void testSearchForSamplesByMetaprojectIdentifier()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(
                MatchClauseAttribute.METAPROJECT, "/test/TEST_METAPROJECTS"));

        List<Sample> samples =
                generalInformationService.searchForSamples(sessionToken, searchCriteria);
        assertEntities("[/TEST-SPACE/TEST-PROJECT/EV-TEST]", samples);
    }

    @Test(enabled = false)
    public void testSearchForSamplesByMetaprojectIdentifierWithDifferentCase()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(
                MatchClauseAttribute.METAPROJECT, "/test/TeSt_MeTaPrOjEcTs"));

        List<Sample> samples =
                generalInformationService.searchForSamples(sessionToken, searchCriteria);
        assertEntities("[/TEST-SPACE/TEST-PROJECT/EV-TEST]", samples);
    }

    public void testSearchForSamplesByMetaprojectIdentifierWithWildcards()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(
                MatchClauseAttribute.METAPROJECT, "/test/*TeSt_MeTa*"));

        List<Sample> samples =
                generalInformationService.searchForSamples(sessionToken, searchCriteria);
        assertEntities("[/CISD/3V-125, /TEST-SPACE/TEST-PROJECT/EV-TEST]", samples);
    }

    @Test
    public void testSearchForSamplesByMetaprojectIdentifierOwnedBySomebodyElse()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(
                MatchClauseAttribute.METAPROJECT, "/test_role/TEST_METAPROJECTS_2"));

        List<Sample> samples =
                generalInformationService.searchForSamples(sessionToken, searchCriteria);
        assertEntities("[]", samples);
    }

    @Test
    public void testSearchForSamplesWithChildren()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE,
                "DILUTION_PLATE"));

        EnumSet<SampleFetchOption> fetchOptions =
                EnumSet.of(SampleFetchOption.PROPERTIES, SampleFetchOption.CHILDREN);
        List<Sample> samples =
                generalInformationService.searchForSamples(sessionToken, searchCriteria,
                        fetchOptions);

        assertEntities("[/CISD/3V-125, /CISD/3V-126, /CISD/DP1-A, /CISD/DP1-B, /CISD/DP2-A, /DP]",
                samples);
        assertEquals(fetchOptions, samples.get(0).getRetrievedFetchOptions());
        assertEquals("3V-125", samples.get(0).getCode());
        assertEquals("CISD", samples.get(0).getSpaceCode());
        assertEquals(979L, samples.get(0).getId().longValue());
        assertEquals("200811050945092-976", samples.get(0).getPermId());
        assertEquals("DILUTION_PLATE", samples.get(0).getSampleTypeCode());
        assertEquals(2L, samples.get(0).getSampleTypeId().longValue());
        assertEquals("test", samples.get(0).getRegistrationDetails().getUserId());
        assertEquals("franz-josef.elmer@systemsx.ch", samples.get(0).getRegistrationDetails()
                .getUserEmail());
        assertEquals("{OFFSET=49}", samples.get(0).getProperties().toString());
        List<Sample> children = samples.get(0).getChildren();
        assertEntities("[/CISD/3VCP7, /CISD/3VCP8, /CISD/NEMO/3VCP5, /CISD/NEMO/3VCP6]", children);
        assertEquals("3VCP5", children.get(0).getCode());
        assertEquals(EnumSet.of(SampleFetchOption.PROPERTIES), children.get(0)
                .getRetrievedFetchOptions());
        assertEquals("{}", children.get(0).getProperties().toString());

        loginAsObserver();
        samples =
                generalInformationService.searchForSamples(sessionToken, searchCriteria,
                        fetchOptions);

        assertEntities("[/DP]", samples);
    }

    @Test
    public void testSearchForSamplesWithAuthorizationFilteredDescendants()
    {
        NewSample newSample = new NewSample();
        newSample.setIdentifier("/TEST-SPACE/S1");
        newSample.setParents("/CISD/3V-126");
        ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType sampleType =
                new ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType();
        sampleType.setCode("NORMAL");
        newSample.setSampleType(sampleType);
        genericServer.registerSample(systemSessionToken, newSample,
                Collections.<NewAttachment> emptySet());
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE,
                "*V-*"));
        sessionToken = generalInformationService.tryToAuthenticateForAllServices("test", "a");
        List<Sample> samples =
                generalInformationService.searchForSamples(sessionToken, searchCriteria,
                        EnumSet.of(SampleFetchOption.DESCENDANTS));
        Collections.sort(samples, SAMPLE_COMPARATOR);
        assertEquals("Sample[/CISD/3V-126,DILUTION_PLATE,properties=?,parents=?,children={Sample[/TEST-SPACE/S1,"
                + "NORMAL,properties=?,parents=?,children=[]]}]", samples.get(1).toString());
        assertEquals(8, samples.size());
        sessionToken = generalInformationService.tryToAuthenticateForAllServices("test_role", "a");

        samples =
                generalInformationService.searchForSamples(sessionToken, searchCriteria,
                        EnumSet.of(SampleFetchOption.DESCENDANTS));

        Collections.sort(samples, SAMPLE_COMPARATOR);
        assertEquals("Sample[/CISD/3V-126,DILUTION_PLATE,properties=?,parents=?,children=[]]",
                samples.get(1).toString());
        assertEquals(2, samples.size());
    }

    @Test
    public void testSearchForSamplesOnBehalfWithAuthorizationFilteredDescendants()
    {
        NewSample newSample = new NewSample();
        newSample.setIdentifier("/TEST-SPACE/S1");
        newSample.setParents("/CISD/3V-126");
        ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType sampleType =
                new ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType();
        sampleType.setCode("NORMAL");
        newSample.setSampleType(sampleType);
        genericServer.registerSample(systemSessionToken, newSample,
                Collections.<NewAttachment> emptySet());
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE,
                "*V-*"));
        List<Sample> samples =
                generalInformationService.searchForSamples(sessionToken, searchCriteria,
                        EnumSet.of(SampleFetchOption.DESCENDANTS));
        Collections.sort(samples, SAMPLE_COMPARATOR);
        assertEquals("Sample[/CISD/3V-126,DILUTION_PLATE,properties=?,parents=?,children={Sample[/TEST-SPACE/S1,"
                + "NORMAL,properties=?,parents=?,children=[]]}]", samples.get(1).toString());
        assertEquals(8, samples.size());

        samples =
                generalInformationService.searchForSamplesOnBehalfOfUser(systemSessionToken,
                        searchCriteria, EnumSet.of(SampleFetchOption.DESCENDANTS), "test_role");

        Collections.sort(samples, SAMPLE_COMPARATOR);
        assertEquals("Sample[/CISD/3V-126,DILUTION_PLATE,properties=?,parents=?,children=[]]",
                samples.get(1).toString());
        assertEquals(2, samples.size());
    }

    @Test
    public void testSearchForSamplesWithMetaprojects()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE,
                "DILUTION_PLATE"));

        EnumSet<SampleFetchOption> fetchOptions =
                EnumSet.of(SampleFetchOption.PROPERTIES, SampleFetchOption.METAPROJECTS);
        List<Sample> samples =
                generalInformationService.searchForSamples(sessionToken, searchCriteria,
                        fetchOptions);

        assertEntities("[/CISD/3V-125, /CISD/3V-126, /CISD/DP1-A, /CISD/DP1-B, /CISD/DP2-A, /DP]",
                samples);
        assertEquals(fetchOptions, samples.get(0).getRetrievedFetchOptions());
        assertEquals("3V-125", samples.get(0).getCode());
        assertEquals("CISD", samples.get(0).getSpaceCode());
        assertEquals(979L, samples.get(0).getId().longValue());
        assertEquals("200811050945092-976", samples.get(0).getPermId());
        assertEquals("DILUTION_PLATE", samples.get(0).getSampleTypeCode());
        assertEquals(2L, samples.get(0).getSampleTypeId().longValue());
        assertEquals("test", samples.get(0).getRegistrationDetails().getUserId());
        assertEquals("franz-josef.elmer@systemsx.ch", samples.get(0).getRegistrationDetails()
                .getUserEmail());
        assertEquals("{OFFSET=49}", samples.get(0).getProperties().toString());
        assertEquals(1, samples.get(0).getMetaprojects().size());

        Metaproject metaproject = samples.get(0).getMetaprojects().get(0);
        assertEquals(3l, metaproject.getId().longValue());
        assertEquals("ANOTHER_TEST_METAPROJECTS", metaproject.getName());
        assertEquals("Another example metaproject", metaproject.getDescription());
        assertTrue(metaproject.isPrivate());
        assertNotNull(metaproject.getCreationDate());

        loginAsObserver();
        samples =
                generalInformationService.searchForSamples(sessionToken, searchCriteria,
                        fetchOptions);

        assertEntities("[/DP]", samples);
    }

    @Test
    public void testSearchForSamplesWithParents()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE,
                "DILUTION_PLATE"));

        EnumSet<SampleFetchOption> fetchOptions =
                EnumSet.of(SampleFetchOption.PROPERTIES, SampleFetchOption.PARENTS);
        List<Sample> samples =
                generalInformationService.searchForSamples(sessionToken, searchCriteria,
                        fetchOptions);

        assertEntities("[/CISD/3V-125, /CISD/3V-126, /CISD/DP1-A, /CISD/DP1-B, /CISD/DP2-A, /DP]",
                samples);
        assertEquals(fetchOptions, samples.get(0).getRetrievedFetchOptions());
        List<Sample> parents = samples.get(0).getParents();
        assertEntities("[/CISD/MP002-1]", parents);
        assertEquals("MP002-1", parents.get(0).getCode());
        assertEquals(EnumSet.of(SampleFetchOption.PROPERTIES), parents.get(0)
                .getRetrievedFetchOptions());
        assertEquals("{PLATE_GEOMETRY=384_WELLS_16X24}", parents.get(0).getProperties().toString());
    }

    @Test
    public void testSearchForSamplesWithParentsButNoProperties()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE,
                "DILUTION_PLATE"));

        EnumSet<SampleFetchOption> fetchOptions = EnumSet.of(SampleFetchOption.PARENTS);
        List<Sample> samples =
                generalInformationService.searchForSamples(sessionToken, searchCriteria,
                        fetchOptions);

        assertEntities("[/CISD/3V-125, /CISD/3V-126, /CISD/DP1-A, /CISD/DP1-B, /CISD/DP2-A, /DP]",
                samples);
        assertEquals(EnumSet.of(SampleFetchOption.BASIC, SampleFetchOption.PARENTS), samples.get(0)
                .getRetrievedFetchOptions());
        assertEquals(null, samples.get(0).getExperimentIdentifierOrNull());
        try
        {
            samples.get(0).getProperties();
            fail("Get properties should have thrown an illegal argument exception");
        } catch (IllegalArgumentException e)
        {
        }
        List<Sample> parents = samples.get(0).getParents();
        assertEntities("[/CISD/MP002-1]", parents);
        assertEquals("MP002-1", parents.get(0).getCode());
        assertEquals(EnumSet.of(SampleFetchOption.BASIC), parents.get(0).getRetrievedFetchOptions());
        try
        {
            parents.get(0).getProperties();
            fail("Get properties should have thrown an illegal argument exception");
        } catch (IllegalArgumentException e)
        {
        }
    }

    @Test
    public void testSearchForSamplesWithParentsAndDescendentsButNoProperties()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE,
                "DILUTION_PLATE"));

        EnumSet<SampleFetchOption> fetchOptions =
                EnumSet.of(SampleFetchOption.PARENTS, SampleFetchOption.DESCENDANTS);
        List<Sample> samples =
                generalInformationService.searchForSamples(sessionToken, searchCriteria,
                        fetchOptions);

        assertEntities("[/CISD/3V-125, /CISD/3V-126, /CISD/DP1-A, /CISD/DP1-B, /CISD/DP2-A, /DP]",
                samples);
        assertEquals(EnumSet.of(SampleFetchOption.BASIC, SampleFetchOption.PARENTS,
                SampleFetchOption.CHILDREN), samples.get(0).getRetrievedFetchOptions());
        assertEquals(null, samples.get(0).getExperimentIdentifierOrNull());
        try
        {
            samples.get(0).getProperties();
            fail("Get properties should have thrown an illegal argument exception");
        } catch (IllegalArgumentException e)
        {
        }
        List<Sample> parents = samples.get(0).getParents();
        assertEntities("[/CISD/MP002-1]", parents);
        assertEquals("MP002-1", parents.get(0).getCode());
        assertEquals(EnumSet.of(SampleFetchOption.BASIC), parents.get(0).getRetrievedFetchOptions());
        try
        {
            parents.get(0).getProperties();
            fail("Get properties should have thrown an illegal argument exception");
        } catch (IllegalArgumentException e)
        {

        }
        List<Sample> children = samples.get(0).getChildren();
        assertEntities("[/CISD/3VCP7, /CISD/3VCP8, /CISD/NEMO/3VCP5, /CISD/NEMO/3VCP6]", children);
        assertEquals("3VCP5", children.get(0).getCode());
        assertEquals(EnumSet.of(SampleFetchOption.BASIC, SampleFetchOption.CHILDREN),
                children.get(0).getRetrievedFetchOptions());
        try
        {
            children.get(0).getProperties();
            fail("Get properties should have thrown an illegal argument exception");
        } catch (IllegalArgumentException e)
        {
        }
        assertEntities("[]", children.get(0).getChildren());
    }

    @Test
    public void testSearchForSamplesWhichAreParentChildRelatedToTestRecursiveLoop()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE,
                "CELL_PLATE"));

        EnumSet<SampleFetchOption> fetchOptions =
                EnumSet.of(SampleFetchOption.PARENTS, SampleFetchOption.CHILDREN);
        List<Sample> samples =
                generalInformationService.searchForSamples(sessionToken, searchCriteria,
                        fetchOptions);

        assertEntities(
                "[/CISD/3VCP7, /CISD/3VCP8, /CISD/DEFAULT/CP1-A1, /CISD/DEFAULT/CP1-A2, /CISD/DEFAULT/CP1-B1, "
                + "/CISD/DEFAULT/CP2-A1, /CISD/DEFAULT/PLATE_WELLSEARCH, /CISD/NEMO/3VCP5, /CISD/NEMO/3VCP6, "
                + "/CISD/NEMO/CP-TEST-1, /CISD/NEMO/CP-TEST-3, /CISD/NOE/CP-TEST-2, /TEST-SPACE/NOE/CP-TEST-4, "
                + "/TEST-SPACE/TEST-PROJECT/FV-TEST]",
                samples);

        Sample sample0 = samples.get(0);
        assertEquals("3VCP5", sample0.getCode());
        assertEquals(EnumSet.of(SampleFetchOption.BASIC, SampleFetchOption.PARENTS,
                SampleFetchOption.CHILDREN), sample0.getRetrievedFetchOptions());
        assertEntities("[]", sample0.getChildren());
        assertEquals("/CISD/NEMO/EXP10", sample0.getExperimentIdentifierOrNull());
        List<Sample> parents = sample0.getParents();
        assertEntities("[/CISD/3V-125]", parents);
        assertEquals("3V-125", parents.get(0).getCode());
        assertEquals(EnumSet.of(SampleFetchOption.BASIC), parents.get(0).getRetrievedFetchOptions());

        Sample sample9 = samples.get(9);
        assertEquals("CP1-A2", sample9.getCode());
        assertEquals(EnumSet.of(SampleFetchOption.BASIC, SampleFetchOption.PARENTS,
                SampleFetchOption.CHILDREN), sample9.getRetrievedFetchOptions());
        assertEntities("[/CISD/DP1-A]", sample9.getParents());
        List<Sample> children = sample9.getChildren();
        assertEntities("[/CISD/DEFAULT/RP1-A2X]", children);
        assertEquals("RP1-A2X", children.get(0).getCode());
        assertEquals(EnumSet.of(SampleFetchOption.BASIC), children.get(0)
                .getRetrievedFetchOptions());

        Sample sample4 = samples.get(4);
        assertEquals("CP-TEST-1", sample4.getCode());
        parents = sample4.getParents();
        assertEntities("[/CISD/NOE/CP-TEST-2]", parents);
        assertEquals("CP-TEST-2", parents.get(0).getCode());
        assertEquals(EnumSet.of(SampleFetchOption.BASIC, SampleFetchOption.PARENTS,
                SampleFetchOption.CHILDREN), parents.get(0).getRetrievedFetchOptions());
        assertEntities("[/CISD/NEMO/CP-TEST-1]", parents.get(0).getChildren());
        assertEntities("[]", parents.get(0).getParents());

        Sample sample5 = samples.get(5);
        assertEquals("CP-TEST-2", sample5.getCode());
        children = sample5.getChildren();
        assertEntities("[/CISD/NEMO/CP-TEST-1]", children);
        assertEquals("CP-TEST-1", children.get(0).getCode());
        assertEquals(EnumSet.of(SampleFetchOption.BASIC, SampleFetchOption.PARENTS,
                SampleFetchOption.CHILDREN), children.get(0).getRetrievedFetchOptions());
        assertEntities("[/CISD/NOE/CP-TEST-2]", children.get(0).getParents());
        assertEntities("[]", children.get(0).getChildren());
    }

    @Test
    public void testSearchForSamplesWithAncestors()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE,
                "3VCP*"));

        EnumSet<SampleFetchOption> fetchOptions =
                EnumSet.of(SampleFetchOption.PROPERTIES, SampleFetchOption.ANCESTORS);
        List<Sample> samples =
                generalInformationService.searchForSamples(sessionToken, searchCriteria,
                        fetchOptions);

        assertEntities("[/CISD/3VCP7, /CISD/3VCP8, /CISD/NEMO/3VCP5, /CISD/NEMO/3VCP6]", samples);
        assertEquals(EnumSet.of(SampleFetchOption.PROPERTIES, SampleFetchOption.PARENTS), samples
                .get(0).getRetrievedFetchOptions());
        assertEquals("/CISD/NEMO/EXP10", samples.get(0).getExperimentIdentifierOrNull());
        List<Sample> parents = samples.get(0).getParents();
        assertEntities("[/CISD/3V-125]", parents);
        assertEquals("3V-125", parents.get(0).getCode());
        assertEquals(EnumSet.of(SampleFetchOption.PROPERTIES, SampleFetchOption.PARENTS), parents
                .get(0).getRetrievedFetchOptions());
        List<Sample> grandParents = parents.get(0).getParents();
        assertEntities("[/CISD/MP002-1]", grandParents);
        assertEquals("MP002-1", grandParents.get(0).getCode());
        assertEquals(EnumSet.of(SampleFetchOption.PROPERTIES, SampleFetchOption.PARENTS),
                grandParents.get(0).getRetrievedFetchOptions());
        assertEquals("{PLATE_GEOMETRY=384_WELLS_16X24}", grandParents.get(0).getProperties()
                .toString());
        assertEntities("[]", grandParents.get(0).getParents());
    }

    @Test
    public void testSearchForSamplesWithDescendants()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE,
                "MP002-1"));

        EnumSet<SampleFetchOption> fetchOptions =
                EnumSet.of(SampleFetchOption.PROPERTIES, SampleFetchOption.DESCENDANTS);
        List<Sample> samples =
                generalInformationService.searchForSamples(sessionToken, searchCriteria,
                        fetchOptions);

        assertEntities("[/CISD/MP002-1]", samples);
        assertEquals(EnumSet.of(SampleFetchOption.PROPERTIES, SampleFetchOption.CHILDREN), samples
                .get(0).getRetrievedFetchOptions());
        List<Sample> children = samples.get(0).getChildren();
        assertEntities("[/CISD/3V-125, /CISD/3V-126]", children);
        assertEquals(EnumSet.of(SampleFetchOption.PROPERTIES, SampleFetchOption.CHILDREN), children
                .get(0).getRetrievedFetchOptions());
        List<Sample> grandChildren = children.get(0).getChildren();
        assertEntities("[/CISD/3VCP7, /CISD/3VCP8, /CISD/NEMO/3VCP5, /CISD/NEMO/3VCP6]", grandChildren);
        assertEquals("3VCP5", grandChildren.get(0).getCode());
        assertEquals(EnumSet.of(SampleFetchOption.PROPERTIES, SampleFetchOption.CHILDREN),
                grandChildren.get(0).getRetrievedFetchOptions());
        assertEquals("{}", grandChildren.get(0).getProperties().toString());
        assertEntities("[]", grandChildren.get(0).getChildren());
    }

    @Test
    public void testSearchForSamplesByAnyFieldMatchingProperty()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAnyFieldMatch("\"very advanced stuff\""));

        List<Sample> samples =
                generalInformationService.searchForSamples(sessionToken, searchCriteria);
        assertEntities("[/CISD/NEMO/CP-TEST-1]", samples);
    }

    @Test
    public void testSearchForSamplesByAnyFieldWithSubCriteria()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAnyFieldMatch("*stuff*"));

        List<Sample> samples =
                generalInformationService.searchForSamples(sessionToken, searchCriteria);
        assertEntities("[/CISD/NEMO/CP-TEST-1, /CISD/NEMO/CP-TEST-3, /CISD/NOE/CP-TEST-2]", samples);

        SearchCriteria experimentCriteria = new SearchCriteria();
        experimentCriteria.addMatchClause(MatchClause.createAnyFieldMatch("EXP-TEST-1"));
        searchCriteria.addSubCriteria(SearchSubCriteria
                .createExperimentCriteria(experimentCriteria));

        samples = generalInformationService.searchForSamples(sessionToken, searchCriteria);
        assertEntities("[/CISD/NEMO/CP-TEST-1]", samples);
    }

    @Test
    public void testSearchForSamplesByAnyFieldMatchingAttribute()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAnyFieldMatch("\"CP-TEST-2\""));

        List<Sample> samples =
                generalInformationService.searchForSamples(sessionToken, searchCriteria);
        assertEntities("[/CISD/NOE/CP-TEST-2]", samples);
    }

    @Test
    public void testSearchForSamplesByAnyProperty()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria
                .addMatchClause(MatchClause.createAnyPropertyMatch("\"very advanced stuff\""));

        List<Sample> samples =
                generalInformationService.searchForSamples(sessionToken, searchCriteria);
        assertEntities("[/CISD/NEMO/CP-TEST-1]", samples);
    }

    @Test
    public void testSearchForSamplesWithoutMainCriteriaAndWithoutSubcriteria()
    {
        SearchCriteria criteria = new SearchCriteria();
        List<Sample> samples = generalInformationService.searchForSamples(sessionToken, criteria);
        assertEquals(701, samples.size());
    }

    @Test
    public void testSearchForSamplesByParentPermId()
    {

        SearchCriteria parentCriteria = new SearchCriteria();
        parentCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.PERM_ID, "200811050945092-976"));

        SearchCriteria criteria = new SearchCriteria();
        criteria.addSubCriteria(SearchSubCriteria.createSampleParentCriteria(parentCriteria));

        List<Sample> samples =
                generalInformationService.searchForSamples(sessionToken, criteria);
        assertEntities("[/CISD/3VCP7, /CISD/3VCP8, /CISD/NEMO/3VCP5, /CISD/NEMO/3VCP6]", samples);
    }

    @Test
    public void testSearchForSamplesByParentPermIdAndChildPermId()
    {

        SearchCriteria parentCriteria = new SearchCriteria();
        parentCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.PERM_ID, "200811050927630-1003"));

        SearchCriteria childCriteria = new SearchCriteria();
        childCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.PERM_ID, "200811050929940-1017"));

        SearchCriteria criteria = new SearchCriteria();
        criteria.addSubCriteria(SearchSubCriteria.createSampleParentCriteria(parentCriteria));
        criteria.addSubCriteria(SearchSubCriteria.createSampleChildCriteria(childCriteria));

        List<Sample> samples =
                generalInformationService.searchForSamples(sessionToken, criteria);
        assertEntities("[/CISD/DP1-A]", samples);
    }

    @Test
    public void testSearchForSamplesByParentPermIdAndExperimentCode()
    {
        SearchCriteria parentCriteria = new SearchCriteria();
        parentCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.PERM_ID, "200811050945092-976"));

        SearchCriteria experimentCriteria = new SearchCriteria();
        experimentCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "EXP11"));

        SearchCriteria criteria = new SearchCriteria();
        criteria.addSubCriteria(SearchSubCriteria.createSampleParentCriteria(parentCriteria));
        criteria.addSubCriteria(SearchSubCriteria.createExperimentCriteria(experimentCriteria));

        List<Sample> samples =
                generalInformationService.searchForSamples(sessionToken, criteria);
        assertEntities("[/CISD/NEMO/3VCP6]", samples);
    }

    @Test
    public void testSearchForSamplesByChildPermId()
    {
        SearchCriteria childCriteria = new SearchCriteria();
        childCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.PERM_ID, "200811050946559-980"));

        SearchCriteria criteria = new SearchCriteria();
        criteria.addSubCriteria(SearchSubCriteria.createSampleChildCriteria(childCriteria));

        List<Sample> samples =
                generalInformationService.searchForSamples(sessionToken, criteria);
        assertEntities("[/CISD/3V-125, /CISD/CL-3V:A02]", samples);
    }

    @Test
    public void testSearchForSamplesByChildPermIdAndSampleCode()
    {
        SearchCriteria childCriteria = new SearchCriteria();
        childCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.PERM_ID, "200811050946559-980"));

        SearchCriteria criteria = new SearchCriteria();
        criteria.addSubCriteria(SearchSubCriteria.createSampleChildCriteria(childCriteria));
        criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "3V-*"));

        List<Sample> samples =
                generalInformationService.searchForSamples(sessionToken, criteria);
        assertEntities("[/CISD/3V-125]", samples);
    }

    @Test
    public void testListSamplesForExperiment()
    {
        List<Sample> samples =
                generalInformationService
                        .listSamplesForExperiment(sessionToken, "/CISD/NEMO/EXP10");
        assertEntities("[/CISD/NEMO/3VCP5]", samples);

        loginAsObserver();
        try
        {
            generalInformationService.listSamplesForExperiment(sessionToken, "/CISD/NEMO/EXP1");
            fail("AuthorizationFailureException expected");
        } catch (AuthorizationFailureException ex)
        {
            assertEquals(
                    "Authorization failure: ERROR: \"User 'observer' does not have enough privileges.\".",
                    ex.getMessage());
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListSamplesForExperimentWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        String session = generalInformationService.tryToAuthenticateForAllServices(user.getUserId(), PASSWORD);

        String experimentIdentifier = "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST";

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            List<Sample> samples = generalInformationService.listSamplesForExperiment(session, experimentIdentifier);
            assertEntities(
                    "[/TEST-SPACE/TEST-PROJECT/EV-INVALID, /TEST-SPACE/TEST-PROJECT/EV-PARENT, "
                    + "/TEST-SPACE/TEST-PROJECT/EV-PARENT-NORMAL, /TEST-SPACE/TEST-PROJECT/EV-TEST, "
                    + "/TEST-SPACE/TEST-PROJECT/FV-TEST, /TEST-SPACE/TEST-PROJECT/SAMPLE-TO-DELETE]",
                    samples);
        } else
        {
            try
            {
                generalInformationService.listSamplesForExperiment(session, experimentIdentifier);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListSamplesForExperimentOnBehalfOfUserWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        String session = generalInformationService.tryToAuthenticateForAllServices(TEST_USER, PASSWORD);

        String experimentIdentifier = "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST";

        List<Sample> samples = generalInformationService.listSamplesForExperimentOnBehalfOfUser(session, experimentIdentifier, user.getUserId());

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            assertEntities(
                    "[/TEST-SPACE/TEST-PROJECT/EV-INVALID, /TEST-SPACE/TEST-PROJECT/EV-PARENT, "
                    + "/TEST-SPACE/TEST-PROJECT/EV-PARENT-NORMAL, /TEST-SPACE/TEST-PROJECT/EV-TEST, "
                    + "/TEST-SPACE/TEST-PROJECT/FV-TEST, /TEST-SPACE/TEST-PROJECT/SAMPLE-TO-DELETE]",
                    samples);
        } else
        {
            assertEntities("[]", samples);
        }
    }

    @Test
    public void testListDataSets()
    {
        Sample s1 = getSample();
        List<DataSet> dataSets =
                generalInformationService.listDataSets(sessionToken, Arrays.asList(s1));

        assertDataSets("[20081105092159111-1]", dataSets);

        loginAsObserver();
        try
        {
            generalInformationService.listDataSets(sessionToken, Arrays.asList(s1));
            fail("AuthorizationFailureException expected");
        } catch (AuthorizationFailureException ex)
        {
            assertEquals(
                    "Authorization failure: ERROR: \"User 'observer' does not have enough privileges.\".",
                    ex.getMessage());
        }
    }

    @Test
    public void testListDataSetsWithPhysicalKind()
    {
        // given
        Sample s1 = getSample();

        // when
        List<DataSet> dataSets =
                generalInformationService.listDataSets(sessionToken, Arrays.asList(s1));

        // then
        DataSet dataSet = getDataSet(dataSets, "20081105092159111-1");
        assertFalse(dataSet.isContainerDataSet());
        assertFalse(dataSet.isLinkDataSet());
    }

    @Test
    public void testListDataSetsWithLinkKind()
    {
        // given
        Sample s1 = getSample(325L, "MP002-1", "200811050917877-331", "MASTER_PLATE");

        // when
        List<DataSet> dataSets =
                generalInformationService.listDataSets(sessionToken, Arrays.asList(s1));

        // then
        DataSet dataSet = getDataSet(dataSets, "20120628092259000-23");
        assertFalse(dataSet.isContainerDataSet());
        assertTrue(dataSet.isLinkDataSet());
    }

    private Sample getSample(Long id, String code, String permId, String sampleTypeCode)
    {
        SampleInitializer sampleIdentifier = getSampleIdentifier();
        sampleIdentifier.setId(id);
        sampleIdentifier.setCode(code);
        sampleIdentifier.setPermId(permId);
        sampleIdentifier.setSampleTypeCode(sampleTypeCode);
        return new Sample(sampleIdentifier);
    }

    private Sample getSample()
    {
        SampleInitializer s1 = getSampleIdentifier();
        return new Sample(s1);
    }

    private SampleInitializer getSampleIdentifier()
    {
        SampleInitializer s1 = new SampleInitializer();
        s1.setId(1042L);
        s1.setCode("CP-TEST-1");
        s1.setIdentifier("/CISD/NEMO/CP-TEST-1");
        s1.setPermId("200902091219327-1025");
        s1.setSampleTypeCode("CELL_PLATE");
        s1.setSampleTypeId(3L);
        s1.setRegistrationDetails(new EntityRegistrationDetails(
                new EntityRegistrationDetailsInitializer()));
        return s1;
    }

    private DataSet getDataSet(List<DataSet> dataSets, String code)
    {
        return dataSets.stream()
                .filter(ds -> ds.getCode().equals(code))
                .findFirst()
                .orElseThrow((() -> new RuntimeException("DataSet with code " + code + " missing.")));
    }

    public void testGetPostregistrationStatusTrueByDefaultForContainerDataSets()
    {
        SearchCriteria criteria = new SearchCriteria();
        criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "CONTAINER_1"));
        List<DataSet> dsList = generalInformationService.searchForDataSets(sessionToken, criteria);
        assertTrue(dsList.get(0).isPostRegistered());
    }

    @Test
    public void testGetPostRegistrationStatusViaDataLister()
    {
        SearchCriteria criteria = new SearchCriteria();
        criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "COMPONENT_1A"));
        List<DataSet> dsList = generalInformationService.searchForDataSets(sessionToken, criteria);
        assertTrue(dsList.get(0).isPostRegistered() == false);

        criteria = new SearchCriteria();
        criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "COMPONENT_1B"));
        dsList = generalInformationService.searchForDataSets(sessionToken, criteria);
        assertTrue(dsList.get(0).isPostRegistered() == true);
    }

    @Test
    public void testGetPostRegistrationStatusViaDataSetTranslator()
    {
        List<DataSet> dataSetsWithMetaData = generalInformationService.getDataSetMetaData(sessionToken, Arrays.asList("COMPONENT_1A"));
        assertTrue(dataSetsWithMetaData.get(0).isPostRegistered() == false);

        dataSetsWithMetaData = generalInformationService.getDataSetMetaData(sessionToken, Arrays.asList("COMPONENT_1B"));
        assertTrue(dataSetsWithMetaData.get(0).isPostRegistered() == true);
    }

    @Test
    public void testGetPostRegistrationStatusViaFetchOptionsDataLister()
    {
        EnumSet<DataSetFetchOption> fetchOptions = EnumSet.of(DataSetFetchOption.BASIC, DataSetFetchOption.PARENTS, DataSetFetchOption.CHILDREN);

        List<DataSet> dataSetsWithMetaData =
                generalInformationService.getDataSetMetaData(sessionToken, Arrays.asList("COMPONENT_1A"), fetchOptions);
        assertTrue(dataSetsWithMetaData.get(0).isPostRegistered() == false);

        dataSetsWithMetaData =
                generalInformationService.getDataSetMetaData(sessionToken, Arrays.asList("COMPONENT_1B"), fetchOptions);
        assertTrue(dataSetsWithMetaData.get(0).isPostRegistered() == true);
    }

    @Test
    public void testListDataSetsForSampleOnlyDirectlyConnected()
    {
        Sample sample = getSample();
        AbstractExternalData dataSetInfo =
                genericServer.getDataSetInfo(sessionToken, new TechId(13));
        DataSetUpdatesDTO updates = new DataSetUpdatesDTO();
        updates.setVersion(dataSetInfo.getVersion());
        updates.setDatasetId(new TechId(dataSetInfo.getId()));
        updates.setProperties(dataSetInfo.getProperties());
        updates.setExperimentIdentifierOrNull(ExperimentIdentifierFactory.parse(dataSetInfo
                .getExperiment().getIdentifier()));
        updates.setSampleIdentifierOrNull(SampleIdentifierFactory.parse(sample.getIdentifier()));
        commonServer.updateDataSet(sessionToken, updates);
        List<DataSet> dataSets =
                generalInformationService.listDataSetsForSample(sessionToken, sample, true);

        sortDataSets(dataSets);
        assertDataSets("[20081105092159111-1, 20110509092359990-10]", dataSets);
        List<DataSet> containedDataSets = dataSets.get(1).getContainedDataSets();
        sortDataSets(containedDataSets);
        assertEquals("[DataSet[20110509092359990-11,<null>,<null>,HCS_IMAGE,{}], "
                + "DataSet[20110509092359990-12,<null>,<null>,HCS_IMAGE,{}]]",
                containedDataSets.toString());

        loginAsObserver();
        try
        {
            generalInformationService.listDataSetsForSample(sessionToken, sample, false);
            fail("AuthorizationFailureException expected");
        } catch (AuthorizationFailureException ex)
        {
            assertEquals(
                    "Authorization failure: ERROR: \"User 'observer' does not have enough privileges.\".",
                    ex.getMessage());
        }
    }

    @Test
    public void testListDataSetsForSampleAlsoIndirectlyConnected()
    {
        Sample s1 = getSample();
        List<DataSet> dataSets =
                generalInformationService
                        .listDataSetsForSample(sessionToken, s1, false);

        assertDataSets(
                "[20081105092159111-1, 20081105092259000-9, 20081105092259900-0, 20081105092259900-1, 20081105092359990-2]",
                dataSets);
        assertEquals(
                "DataSet[20081105092259000-9,/CISD/DEFAULT/EXP-REUSE,<null>,HCS_IMAGE,{COMMENT=no comment}]",
                dataSets.get(1).toString());

        loginAsObserver();
        try
        {
            generalInformationService.listDataSetsForSample(sessionToken, s1, false);
            fail("AuthorizationFailureException expected");
        } catch (AuthorizationFailureException ex)
        {
            assertEquals(
                    "Authorization failure: ERROR: \"User 'observer' does not have enough privileges.\".",
                    ex.getMessage());
        }
    }

    @Test
    public void testListDataSetsWithConnections()
    {
        SampleInitializer s1 = new SampleInitializer();
        s1.setId(1043L);
        s1.setCode("CP-TEST-2");
        s1.setIdentifier("/CISD/NEMO/CP-TEST-2");
        s1.setPermId("200902091250077-1026");
        s1.setSampleTypeCode("CELL_PLATE");
        s1.setSampleTypeId(3L);
        s1.setRegistrationDetails(new EntityRegistrationDetails(
                new EntityRegistrationDetailsInitializer()));
        List<DataSet> dataSets =
                generalInformationService.listDataSets(sessionToken, Arrays.asList(new Sample(s1)),
                        EnumSet.allOf(Connections.class));

        assertDataSets("[20081105092159222-2]", dataSets);
        assertEquals("[]", dataSets.get(0).getParentCodes().toString());

        loginAsObserver();
        try
        {
            generalInformationService.listDataSets(sessionToken, Arrays.asList(new Sample(s1)),
                    EnumSet.of(Connections.CHILDREN));
            fail("AuthorizationFailureException expected");
        } catch (AuthorizationFailureException ex)
        {
            assertEquals(
                    "Authorization failure: ERROR: \"User 'observer' does not have enough privileges.\".",
                    ex.getMessage());
        }
    }

    @Test
    public void testListDataSetsForExperiments()
    {
        ExperimentInitializer e1 = new ExperimentInitializer();
        e1.setId(8L);
        e1.setCode("EXP-REUSE");
        e1.setIdentifier("/CISD/DEFAULT/EXP-REUSE");
        e1.setExperimentTypeCode("SIRNA_HCS");
        e1.setPermId("200811050940555-1032");
        e1.setRegistrationDetails(new EntityRegistrationDetails(
                new EntityRegistrationDetailsInitializer()));
        List<DataSet> dataSets =
                generalInformationService.listDataSetsForExperiments(sessionToken,
                        Arrays.asList(new Experiment(e1)), EnumSet.allOf(Connections.class));

        assertDataSets(
                "[20081105092259000-18, 20081105092259000-19, 20081105092259000-20, 20081105092259000-21, "
                        + "20081105092259000-8, 20081105092259000-9, 20081105092259900-0, 20081105092259900-1, "
                        + "20081105092359990-2, 20110509092359990-10, 20110509092359990-11, 20110509092359990-12, "
                        + "COMPONENT_1A, COMPONENT_1B, COMPONENT_2A, COMPONENT_3A, COMPONENT_3AB, CONTAINER_1, "
                        + "CONTAINER_2, CONTAINER_3A, CONTAINER_3B, ROOT_CONTAINER]",
                dataSets);

        List<String> parentCodes = new ArrayList<String>(dataSets.get(5).getParentCodes());
        Collections.sort(parentCodes);
        assertEquals("[20081105092159111-1, 20081105092159222-2, 20081105092159333-3]",
                parentCodes.toString());
        List<String> childrenCodes = new ArrayList<String>(dataSets.get(5).getChildrenCodes());
        Collections.sort(childrenCodes);
        assertEquals("[20081105092259900-0, 20081105092259900-1]", childrenCodes.toString());
        DataSet dataSet = dataSets.get(9);
        assertTrue(dataSet.isContainerDataSet());
        List<DataSet> containedDataSets = dataSet.getContainedDataSets();
        Collections.sort(containedDataSets, new Comparator<DataSet>()
            {
                @Override
                public int compare(DataSet d1, DataSet d2)
                {
                    return d1.getCode().compareTo(d2.getCode());
                }
            });
        assertEquals("[DataSet[20110509092359990-11,/CISD/DEFAULT/EXP-REUSE,<null>,HCS_IMAGE,"
                + "{COMMENT=non-virtual comment},[]], "
                + "DataSet[20110509092359990-12,/CISD/DEFAULT/EXP-REUSE,<null>,HCS_IMAGE,"
                + "{COMMENT=non-virtual comment},[]]]", containedDataSets.toString());
        assertEquals(
                "DataSet[20110509092359990-10,/CISD/DEFAULT/EXP-REUSE,<null>,CONTAINER_TYPE,{},[]]",
                dataSet.toString());

        loginAsObserver();
        try
        {
            generalInformationService.listDataSetsForExperiments(sessionToken,
                    Arrays.asList(new Experiment(e1)), EnumSet.allOf(Connections.class));
            fail("AuthorizationFailureException expected");
        } catch (AuthorizationFailureException ex)
        {
            assertEquals(
                    "Authorization failure: ERROR: \"User 'observer' does not have enough privileges.\".",
                    ex.getMessage());
        }
    }

    @Test
    public void testGetDataSetMetaData()
    {
        List<DataSet> dataSets =
                generalInformationService.getDataSetMetaData(sessionToken,
                        Arrays.asList("20081105092159222-2", "20110509092359990-10"));
        assertEquals(
                "[DataSet[20081105092159222-2,/CISD/NOE/EXP-TEST-2,/CISD/NOE/CP-TEST-2,HCS_IMAGE,{COMMENT=no comment},[]], "
                        + "DataSet[20110509092359990-10,/CISD/DEFAULT/EXP-REUSE,<null>,CONTAINER_TYPE,{},[]]]",
                dataSets.toString());
        assertEquals("[]", dataSets.get(1).getContainedDataSets().toString());

        loginAsObserver();

        try
        {
            dataSets =
                    generalInformationService.getDataSetMetaData(sessionToken,
                            Arrays.asList("20081105092159222-2"));
            fail();
        } catch (AuthorizationFailureException e)
        {
            // expected
        }
    }

    @Test
    public void testSearchForDataSets()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE,
                "CONTAINER_TYPE"));
        List<DataSet> dataSets =
                generalInformationService.searchForDataSets(sessionToken, searchCriteria);
        assertEquals(
                "[DataSet[CONTAINER_2,/CISD/DEFAULT/EXP-REUSE,<null>,CONTAINER_TYPE,{}], DataSet[CONTAINER_3A,/CISD/DEFAULT/EXP-REUSE,<null>,CONTAINER_TYPE,{}], DataSet[CONTAINER_3B,/CISD/DEFAULT/EXP-REUSE,<null>,CONTAINER_TYPE,{}], DataSet[20110509092359990-10,/CISD/DEFAULT/EXP-REUSE,<null>,CONTAINER_TYPE,{}], DataSet[20081105092259000-19,/CISD/DEFAULT/EXP-REUSE,<null>,CONTAINER_TYPE,{}], DataSet[ROOT_CONTAINER,/CISD/DEFAULT/EXP-REUSE,<null>,CONTAINER_TYPE,{}], DataSet[CONTAINER_1,/CISD/DEFAULT/EXP-REUSE,<null>,CONTAINER_TYPE,{}]]",
                dataSets.toString());

        List<DataSet> containedDataSets = dataSets.get(1).getContainedDataSets();
        assertEquals("[DataSet[COMPONENT_3A,<null>,<null>,HCS_IMAGE,{}], "
                + "DataSet[COMPONENT_3AB,<null>,<null>,HCS_IMAGE,{}]]",
                containedDataSets.toString());
        assertEquals(1304929379313L, containedDataSets.get(0).getRegistrationDate().getTime());

        loginAsObserver();
        dataSets = generalInformationService.searchForDataSets(sessionToken, searchCriteria);

        assertEquals("[]", dataSets.toString());
    }

    @Test
    public void testSearchForDataSetsOnBehalfOfUser()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE,
                "*9000-2*"));

        // executed by test
        List<DataSet> dataSets =
                generalInformationService.searchForDataSets(sessionToken, searchCriteria);
        assertCollection(
                "[20081105092259000-20, 20081105092259000-21, 20120619092259000-22, 20120628092259000-23, 20120628092259000-24, 20120628092259000-25]",
                dataSets, new DataSetToCode());

        // executed by test on behalf of test_space
        List<DataSet> onBehalfOfTestSpaceResult =
                generalInformationService.searchForDataSetsOnBehalfOfUser(sessionToken,
                        searchCriteria, "test_space");
        assertCollection(
                "[20120619092259000-22, 20120628092259000-24, 20120628092259000-25]",
                onBehalfOfTestSpaceResult, new DataSetToCode());

        // executed by test_space
        generalInformationService.logout(sessionToken);
        sessionToken =
                generalInformationService.tryToAuthenticateForAllServices("test_space", "password");

        List<DataSet> testSpaceResult =
                generalInformationService.searchForDataSets(sessionToken, searchCriteria);
        assertCollection(
                "[20120619092259000-22, 20120628092259000-24, 20120628092259000-25]",
                testSpaceResult, new DataSetToCode());
    }

    @Test(expectedExceptions = AuthorizationFailureException.class)
    public void testSearchForDataSetsOnBehalfOfUserExecutedByNotInstanceUser()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE,
                "SOME_CODE"));

        generalInformationService.logout(sessionToken);
        sessionToken =
                generalInformationService.tryToAuthenticateForAllServices("test_role", "password");
        generalInformationService.searchForDataSetsOnBehalfOfUser(sessionToken, searchCriteria,
                "admin");
    }

    @Test
    public void testSearchForDataSetsOnBehalfOfUserByMetaprojectName()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(
                MatchClauseAttribute.METAPROJECT, "TEST_METAPROJECTS"));

        // executed by test
        List<DataSet> dataSets =
                generalInformationService.searchForDataSets(sessionToken, searchCriteria);
        assertCollection("[20120619092259000-22]", dataSets, new DataSetToCode());

        // executed by test on behalf of test_space
        List<DataSet> onBehalfOfTestSpaceResult =
                generalInformationService.searchForDataSetsOnBehalfOfUser(sessionToken,
                        searchCriteria, "test_space");
        assertCollection("[20120619092259000-22, 20120628092259000-24]", onBehalfOfTestSpaceResult,
                new DataSetToCode());

        // executed by test_space
        generalInformationService.logout(sessionToken);
        sessionToken =
                generalInformationService.tryToAuthenticateForAllServices("test_space", "password");

        List<DataSet> testSpaceResult =
                generalInformationService.searchForDataSets(sessionToken, searchCriteria);
        assertCollection("[20120619092259000-22, 20120628092259000-24]", testSpaceResult,
                new DataSetToCode());
    }

    @Test
    public void testSearchForDataSetsByPermIdAndCheckModifier()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(
                MatchClauseAttribute.PERM_ID, "20081105092259000-21"));

        List<DataSet> dataSets =
                generalInformationService.searchForDataSets(sessionToken, searchCriteria);
        assertEquals("[DataSet[20081105092259000-21,/CISD/DEFAULT/EXP-REUSE,<null>,HCS_IMAGE,{COMMENT=co comment}]]",
                dataSets.toString());
        assertEquals("test", dataSets.get(0).getRegistrationDetails().getModifierUserId());

    }

    @Test
    public void testSearchForDataSetsByMetaprojectName()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(
                MatchClauseAttribute.METAPROJECT, "TEST_METAPROJECTS"));
        List<DataSet> dataSets =
                generalInformationService.searchForDataSets(sessionToken, searchCriteria);
        assertEquals(
                "[DataSet[20120619092259000-22,/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST,<null>,HCS_IMAGE,{COMMENT=co comment}]]",
                dataSets.toString());
    }

    @Test
    public void testSearchForDataSetsByMetaprojectNameOwnedBySomebodyElse()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(
                MatchClauseAttribute.METAPROJECT, "TEST_METAPROJECTS_2"));
        List<DataSet> dataSets =
                generalInformationService.searchForDataSets(sessionToken, searchCriteria);
        assertEquals("[]", dataSets.toString());
    }

    @Test
    public void testSearchForDataSetsByMetaprojectIdentifier()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(
                MatchClauseAttribute.METAPROJECT, "/test/TEST_METAPROJECTS"));
        List<DataSet> dataSets =
                generalInformationService.searchForDataSets(sessionToken, searchCriteria);
        assertEquals(
                "[DataSet[20120619092259000-22,/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST,<null>,HCS_IMAGE,{COMMENT=co comment}]]",
                dataSets.toString());
    }

    @Test
    public void testSearchForDataSetsByMetaprojectIdentifierOwnedBySomebodyElse()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(
                MatchClauseAttribute.METAPROJECT, "/test/TEST_METAPROJECTS_2"));
        List<DataSet> dataSets =
                generalInformationService.searchForDataSets(sessionToken, searchCriteria);
        assertEquals("[]", dataSets.toString());
    }

    @Test
    public void testSearchForDataSetsByAnyFieldMatchingProperty()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAnyFieldMatch("\"non-virtual comment\""));
        List<DataSet> dataSets =
                generalInformationService.searchForDataSets(sessionToken, searchCriteria);
        assertEquals(
                "[DataSet[20110509092359990-11,/CISD/DEFAULT/EXP-REUSE,<null>,HCS_IMAGE,{COMMENT=non-virtual comment}], DataSet[20110509092359990-12,/CISD/DEFAULT/EXP-REUSE,<null>,HCS_IMAGE,{COMMENT=non-virtual comment}]]",
                dataSets.toString());
    }

    // TODO: searching by tags is not supported yet because they are private information for the users who made a tag.
    // @Test
    // public void testSearchForDataSetsByAnyFieldMatchingAttribute()
    // {
    // SearchCriteria searchCriteria = new SearchCriteria();
    // searchCriteria.addMatchClause(MatchClause.createAnyFieldMatch("TEST_METAPROJECTS"));
    // List<DataSet> dataSets =
    // generalInformationService.searchForDataSets(sessionToken, searchCriteria);
    // assertEquals(
    // "[DataSet[20120619092259000-22,/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST,<null>,HCS_IMAGE,{COMMENT=co comment}]]",
    // dataSets.toString());
    // }

    @Test
    public void testSearchForDataSetsByAnyProperty()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria
                .addMatchClause(MatchClause.createAnyPropertyMatch("\"non-virtual comment\""));
        List<DataSet> dataSets =
                generalInformationService.searchForDataSets(sessionToken, searchCriteria);
        assertEquals(
                "[DataSet[20110509092359990-11,/CISD/DEFAULT/EXP-REUSE,<null>,HCS_IMAGE,{COMMENT=non-virtual comment}], DataSet[20110509092359990-12,/CISD/DEFAULT/EXP-REUSE,<null>,HCS_IMAGE,{COMMENT=non-virtual comment}]]",
                dataSets.toString());
    }

    @Test
    public void testSearchForDataSetsWithoutMainCriteriaAndWithoutSubcriteria()
    {
        SearchCriteria criteria = new SearchCriteria();
        List<DataSet> dataSets = generalInformationService.searchForDataSets(sessionToken, criteria);
        assertEquals(37, dataSets.size());
    }

    @Test
    public void testSearchForDataSetsByParentCode()
    {
        SearchCriteria parentCriteria = new SearchCriteria();
        parentCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "20081105092159333-3"));

        SearchCriteria criteria = new SearchCriteria();
        criteria.addSubCriteria(SearchSubCriteria.createDataSetParentCriteria(parentCriteria));

        List<DataSet> dataSets =
                generalInformationService.searchForDataSets(sessionToken, criteria);
        assertDataSets("[20081105092259000-8, 20081105092259000-9]", dataSets);
    }

    @Test
    public void testSearchForDataSetsByParentCodeAndChildCode()
    {
        SearchCriteria parentCriteria = new SearchCriteria();
        parentCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "20081105092159333-3"));

        SearchCriteria childCriteria = new SearchCriteria();
        childCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "20081105092259900-0"));

        SearchCriteria criteria = new SearchCriteria();
        criteria.addSubCriteria(SearchSubCriteria.createDataSetParentCriteria(parentCriteria));
        criteria.addSubCriteria(SearchSubCriteria.createDataSetChildCriteria(childCriteria));

        List<DataSet> dataSets =
                generalInformationService.searchForDataSets(sessionToken, criteria);
        assertDataSets("[20081105092259000-9]", dataSets);
    }

    @Test
    public void testSearchForDataSetsByParentCodeAndDataSetCode()
    {
        SearchCriteria parentCriteria = new SearchCriteria();
        parentCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "20081105092159333-3"));

        SearchCriteria criteria = new SearchCriteria();
        criteria.addSubCriteria(SearchSubCriteria.createDataSetParentCriteria(parentCriteria));
        criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "*-9"));

        List<DataSet> dataSets =
                generalInformationService.searchForDataSets(sessionToken, criteria);
        assertDataSets("[20081105092259000-9]", dataSets);
    }

    @Test
    public void testSearchForDataSetsByChildCode()
    {
        SearchCriteria childCriteria = new SearchCriteria();
        childCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "20081105092259000-9"));

        SearchCriteria criteria = new SearchCriteria();
        criteria.addSubCriteria(SearchSubCriteria.createDataSetChildCriteria(childCriteria));

        List<DataSet> dataSets =
                generalInformationService.searchForDataSets(sessionToken, criteria);
        assertDataSets("[20081105092159111-1, 20081105092159222-2, 20081105092159333-3]", dataSets);
    }

    @Test
    public void testSearchForDataSetsByChildCodeAndDataSetCode()
    {
        SearchCriteria childCriteria = new SearchCriteria();
        childCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "20081105092259000-9"));

        SearchCriteria criteria = new SearchCriteria();
        criteria.addSubCriteria(SearchSubCriteria.createDataSetChildCriteria(childCriteria));
        criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "*-2"));

        List<DataSet> dataSets =
                generalInformationService.searchForDataSets(sessionToken, criteria);
        assertDataSets("[20081105092159222-2]", dataSets);
    }

    @Test
    public void testSearchForDataSetsByRegistratorUserId()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.REGISTRATOR_USER_ID, "etlserver"));

        List<DataSet> dataSets =
                generalInformationService.searchForDataSets(sessionToken, searchCriteria);
        assertDataSets("[20081105092259900-1, 20081105092359990-2]", dataSets);
    }

    @Test
    public void testSearchForDataSetsByRegistratorFirstName()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.REGISTRATOR_FIRST_NAME, "\"John 2\""));

        List<DataSet> dataSets =
                generalInformationService.searchForDataSets(sessionToken, searchCriteria);
        assertDataSets("[20081105092259900-1, 20081105092359990-2]", dataSets);
    }

    @Test
    public void testSearchForDataSetsByRegistratorLastName()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.REGISTRATOR_LAST_NAME, "\"ETL Server\""));

        List<DataSet> dataSets =
                generalInformationService.searchForDataSets(sessionToken, searchCriteria);
        assertDataSets("[20081105092259900-1, 20081105092359990-2]", dataSets);
    }

    @Test
    public void testSearchForDataSetsByRegistratorEmail()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.REGISTRATOR_EMAIL, "\"etlserver@systemsx.ch\""));

        List<DataSet> dataSets =
                generalInformationService.searchForDataSets(sessionToken, searchCriteria);
        assertDataSets("[20081105092259900-1, 20081105092359990-2]", dataSets);
    }

    @Test
    public void testSearchForDataSetsByModifierUserId()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.MODIFIER_USER_ID, "etlserver"));

        List<DataSet> dataSets =
                generalInformationService.searchForDataSets(sessionToken, searchCriteria);
        assertDataSets("[20110509092359990-11, 20110509092359990-12]", dataSets);
    }

    @Test
    public void testSearchForDataSetsByModifierFirstName()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.MODIFIER_FIRST_NAME, "\"John 2\""));

        List<DataSet> dataSets =
                generalInformationService.searchForDataSets(sessionToken, searchCriteria);
        assertDataSets("[20110509092359990-11, 20110509092359990-12]", dataSets);
    }

    @Test
    public void testSearchForDataSetsByModifierLastName()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.MODIFIER_LAST_NAME, "\"ETL Server\""));

        List<DataSet> dataSets =
                generalInformationService.searchForDataSets(sessionToken, searchCriteria);
        assertDataSets("[20110509092359990-11, 20110509092359990-12]", dataSets);
    }

    @Test
    public void testSearchForDataSetsByModifierEmail()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.MODIFIER_EMAIL, "\"etlserver@systemsx.ch\""));

        List<DataSet> dataSets =
                generalInformationService.searchForDataSets(sessionToken, searchCriteria);
        assertDataSets("[20110509092359990-11, 20110509092359990-12]", dataSets);
    }

    @Test
    public void testSearchForExperimentsByCode()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE,
                "EXP1*"));

        List<Experiment> experiments =
                generalInformationService.searchForExperiments(sessionToken, searchCriteria);

        assertEntities("[/CISD/NEMO/EXP1, /CISD/NEMO/EXP10, /CISD/NEMO/EXP11]", experiments);
    }

    @Test
    public void testSearchForExperimentsByProject()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(
                MatchClauseAttribute.PROJECT, "NOE"));

        List<Experiment> experiments =
                generalInformationService.searchForExperiments(sessionToken, searchCriteria);

        assertEntities("[/CISD/NOE/EXP-TEST-2, /TEST-SPACE/NOE/EXP-TEST-2, /TEST-SPACE/NOE/EXPERIMENT-TO-DELETE]", experiments);
    }

    @Test
    public void testSearchForExperimentsByProperty()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createPropertyMatch("GENDER", "FEMALE"));

        List<Experiment> experiments =
                generalInformationService.searchForExperiments(sessionToken, searchCriteria);

        assertEquals("/CISD/NEMO/EXP-TEST-2", experiments.get(0).getIdentifier());
        assertEquals("SIRNA_HCS", experiments.get(0).getExperimentTypeCode());
        List<Entry<String, Serializable>> list =
                new ArrayList<Entry<String, Serializable>>(experiments.get(0).getProperties().entrySet());
        Collections.sort(list, new Comparator<Entry<String, Serializable>>()
            {
                @Override
                public int compare(Entry<String, Serializable> e1, Entry<String, Serializable> e2)
                {
                    return e1.getKey().compareTo(e2.getKey());
                }
            });
        assertEquals("[DESCRIPTION=very important expertiment, GENDER=FEMALE, "
                + "PURCHASE_DATE=2009-02-09 10:00:00 +0100]", list.toString());
        assertEquals(1, experiments.size());
    }

    @Test
    public void testSearchForExperimentsByRegistratorUserId()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.REGISTRATOR_USER_ID, "etlserver"));

        List<Experiment> experiments =
                generalInformationService.searchForExperiments(sessionToken, searchCriteria);
        assertEntities("[/CISD/NEMO/EXP-TEST-1, /CISD/NEMO/EXP-TEST-2]", experiments);
    }

    @Test
    public void testSearchForExperimentsByRegistratorFirstName()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.REGISTRATOR_FIRST_NAME, "\"John 2\""));

        List<Experiment> experiments =
                generalInformationService.searchForExperiments(sessionToken, searchCriteria);
        assertEntities("[/CISD/NEMO/EXP-TEST-1, /CISD/NEMO/EXP-TEST-2]", experiments);
    }

    @Test
    public void testSearchForExperimentsByRegistratorLastName()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.REGISTRATOR_LAST_NAME, "\"ETL Server\""));

        List<Experiment> experiments =
                generalInformationService.searchForExperiments(sessionToken, searchCriteria);
        assertEntities("[/CISD/NEMO/EXP-TEST-1, /CISD/NEMO/EXP-TEST-2]", experiments);
    }

    @Test
    public void testSearchForExperimentsByRegistratorEmail()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.REGISTRATOR_EMAIL, "\"etlserver@systemsx.ch\""));

        List<Experiment> experiments =
                generalInformationService.searchForExperiments(sessionToken, searchCriteria);
        assertEntities("[/CISD/NEMO/EXP-TEST-1, /CISD/NEMO/EXP-TEST-2]", experiments);
    }

    private Person getPerson(String userId)
    {
        List<Person> persons = generalInformationService.listPersons(sessionToken);
        for (Person person : persons)
        {
            if (person.getUserId().equals(userId))
            {
                return person;
            }
        }
        return null;
    }

    private boolean contains(List<Experiment> experiments, List<String> experimentIdentifiers)
    {
        int found = 0;
        for (String experimentIdentifier : experimentIdentifiers)
        {
            for (Experiment experiment : experiments)
            {
                if (experiment.getIdentifier().equals(experimentIdentifier))
                {
                    found++;
                }
            }
        }
        return found == experimentIdentifiers.size();
    }

    @Test
    public void testSearchForExperimentsByModifierUserId()
    {
        String userId = "test_role";
        Person person = getPerson(userId);
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.MODIFIER_USER_ID, person.getUserId()));

        List<Experiment> experiments =
                generalInformationService.searchForExperiments(sessionToken, searchCriteria);
        assertEntities("[/CISD/NEMO/EXP1, /CISD/NEMO/EXP10]", experiments);
    }

    @Test
    public void testSearchForExperimentsByModifierFirstName()
    {
        String userId = "test_role";
        Person person = getPerson(userId);
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria
                .addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.MODIFIER_FIRST_NAME, "\"" + person.getFirstName() + "\""));

        List<Experiment> experiments =
                generalInformationService.searchForExperiments(sessionToken, searchCriteria);
        assertTrue(contains(experiments, Arrays.asList("/CISD/NEMO/EXP1", "/CISD/NEMO/EXP10")));
    }

    @Test
    public void testSearchForExperimentsByModifierLastName()
    {
        String userId = "test_role";
        Person person = getPerson(userId);
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.MODIFIER_LAST_NAME, "\"" + person.getLastName() + "\""));

        List<Experiment> experiments =
                generalInformationService.searchForExperiments(sessionToken, searchCriteria);
        assertTrue(contains(experiments, Arrays.asList("/CISD/NEMO/EXP1", "/CISD/NEMO/EXP10")));
    }

    @Test
    public void testSearchForExperimentsByModifierEmail()
    {
        String userId = "test_role";
        Person person = getPerson(userId);
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.MODIFIER_EMAIL, "\"" + person.getEmail() + "\""));

        List<Experiment> experiments =
                generalInformationService.searchForExperiments(sessionToken, searchCriteria);
        assertTrue(contains(experiments, Arrays.asList("/CISD/NEMO/EXP1", "/CISD/NEMO/EXP10")));
    }

    @Test
    public void testGetDefaultPutDataStoreBaseURL()
    {
        String url = generalInformationService.getDefaultPutDataStoreBaseURL(sessionToken);

        assertEquals("http://localhost:8765", url);
    }

    @Test
    public void testTryGetDataStoreBaseURL()
    {
        String url =
                generalInformationService.tryGetDataStoreBaseURL(sessionToken,
                        "20081105092159111-1");

        assertEquals("http://localhost:8765", url);
    }

    @Test
    public void testListExperimentTypes()
    {
        List<ExperimentType> experimentTypes =
                generalInformationService.listExperimentTypes(sessionToken);
        Collections.sort(experimentTypes, new Comparator<ExperimentType>()
            {
                @Override
                public int compare(ExperimentType t1, ExperimentType t2)
                {
                    return t1.getCode().compareTo(t2.getCode());
                }
            });

        assertEquals("ExperimentType[COMPOUND_HCS,Compound High Content Screening,"
                + "{PropertyTypeGroup[<null>,{PropertyType[VARCHAR,DESCRIPTION,Description,"
                + "A Description,mandatory],PropertyType[VARCHAR,COMMENT,Comment,"
                + "Any other comments,optional],PropertyType[MATERIAL,ANY_MATERIAL,"
                + "any_material,any_material,optional]}]}]",
                experimentTypes.get(0).toString());
        assertEquals(3, experimentTypes.size());
    }

    @Test
    public void testListDataSetTypes()
    {
        List<DataSetType> types = generalInformationService.listDataSetTypes(sessionToken);
        Collections.sort(types, new Comparator<DataSetType>()
            {
                @Override
                public int compare(DataSetType t1, DataSetType t2)
                {
                    return t1.getCode().compareTo(t2.getCode());
                }
            });
        assertEquals("CONTAINER_TYPE", types.get(0).getCode());
        assertEquals("[]", types.get(0).getPropertyTypeGroups().toString());
        assertEquals("HCS_IMAGE", types.get(3).getCode());
        List<PropertyTypeGroup> groups = types.get(3).getPropertyTypeGroups();
        List<PropertyType> propertyTypes = groups.get(0).getPropertyTypes();
        Collections.sort(propertyTypes, new Comparator<PropertyType>()
            {
                @Override
                public int compare(PropertyType t1, PropertyType t2)
                {
                    return t1.getCode().compareTo(t2.getCode());
                }
            });
        List<String> propertyTypeCodes = new ArrayList<String>();
        for (PropertyType propertyType : propertyTypes)
        {
            propertyTypeCodes.add(propertyType.getCode());
        }
        assertEquals("[ANY_MATERIAL, BACTERIUM, COMMENT, GENDER]", propertyTypeCodes.toString());
        assertEquals("PropertyType[MATERIAL,ANY_MATERIAL,any_material,any_material,optional]",
                propertyTypes.get(0).toString());
        assertEquals(1, groups.size());
        assertEquals("HCS_IMAGE_ANALYSIS_DATA", types.get(4).getCode());
        assertEquals("[]", types.get(4).getPropertyTypeGroups().toString());
        assertEquals("LINK_TYPE", types.get(5).getCode());
        assertEquals("[]", types.get(0).getPropertyTypeGroups().toString());
        assertEquals("REQUIRES_EXPERIMENT", types.get(6).getCode());
        assertEquals("[]", types.get(4).getPropertyTypeGroups().toString());
        assertEquals("UNKNOWN", types.get(7).getCode());
        assertEquals("[]", types.get(7).getPropertyTypeGroups().toString());
        assertEquals(11, types.size());
    }

    @Test
    public void testListSampleTypes()
    {
        List<SampleType> types = generalInformationService.listSampleTypes(sessionToken);

        assertEquals("SampleType[WELL,Plate Well,ValidationPluginInfo[validateOK,<null>],"
                + "listable=false,showContainer=true,showParents=false,showParentMetaData=false,"
                + "uniqueSubcodes=false,automaticCodeGeneration=false,codePrefix=S,[]]",
                pick(types, "WELL").toString());
        assertEquals("SampleType[DILUTION_PLATE,Dilution Plate,<null>,listable=true,showContainer=false,"
                + "showParents=true,showParentMetaData=false,uniqueSubcodes=false,"
                + "automaticCodeGeneration=false,codePrefix=S,{PropertyTypeGroup[<null>,"
                + "{PropertyType[INTEGER,OFFSET,Offset,Offset from the start of the sequence,optional]}]}]",
                pick(types, "DILUTION_PLATE").toString());
        assertEquals(12, types.size());
    }

    private SampleType pick(List<SampleType> types, String code)
    {
        for (SampleType sampleType : types)
        {
            if (sampleType.getCode().equals(code))
            {
                return sampleType;
            }
        }
        fail("No sample type '" + code + "' found: " + types);
        return null;
    }

    @Test
    public void testListExperimentsByProjects()
    {
        List<Project> projects = Arrays.asList(new Project("CISD", "NEMO"));
        List<Experiment> experiments =
                generalInformationService.listExperiments(sessionToken, projects, "SIRNA_HCS");

        assertEntities(
                "[/CISD/NEMO/EXP-TEST-2, /CISD/NEMO/EXP1, /CISD/NEMO/EXP10, /CISD/NEMO/EXP11]",
                experiments);

        loginAsObserver();
        try
        {
            generalInformationService.listExperiments(sessionToken, projects, "SIRNA_HCS");
            fail("AuthorizationFailureException expected");
        } catch (AuthorizationFailureException ex)
        {
            assertEquals(
                    "Authorization failure: ERROR: \"User 'observer' does not have enough privileges.\".",
                    ex.getMessage());
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListExperimentsByExperimentIdentifiersWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        String session = generalInformationService.tryToAuthenticateForAllServices(user.getUserId(), PASSWORD);
        String identifier = "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST";

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            List<Experiment> experiments = generalInformationService.listExperiments(session, Arrays.asList(identifier));
            assertEquals(1, experiments.size());
            assertEquals(identifier, experiments.get(0).getIdentifier());
        } else
        {
            try
            {
                generalInformationService.listExperiments(session, Arrays.asList(identifier));
                Assert.fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListExperimentsByProjectsAndExperimentTypeWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        String session = generalInformationService.tryToAuthenticateForAllServices(user.getUserId(), PASSWORD);

        Project project = new Project("TEST-SPACE", "TEST-PROJECT");
        String experimentType = "SIRNA_HCS";

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            List<Experiment> experiments = generalInformationService.listExperiments(session, Arrays.asList(project), experimentType);
            assertEquals(1, experiments.size());
            assertEquals("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST", experiments.get(0).getIdentifier());
        } else
        {
            try
            {
                generalInformationService.listExperiments(session, Arrays.asList(project), experimentType);
                Assert.fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListExperimentsHavingDataSetsWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        String session = generalInformationService.tryToAuthenticateForAllServices(user.getUserId(), PASSWORD);

        Project project = new Project("TEST-SPACE", "TEST-PROJECT");
        String experimentType = "SIRNA_HCS";

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            List<Experiment> experiments = generalInformationService.listExperimentsHavingDataSets(session, Arrays.asList(project), experimentType);
            assertEquals(1, experiments.size());
            assertEquals("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST", experiments.get(0).getIdentifier());
        } else
        {
            try
            {
                generalInformationService.listExperimentsHavingDataSets(session, Arrays.asList(project), experimentType);
                Assert.fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListExperimentsHavingSamplesWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        String session = generalInformationService.tryToAuthenticateForAllServices(user.getUserId(), PASSWORD);

        Project project = new Project("TEST-SPACE", "TEST-PROJECT");
        String experimentType = "SIRNA_HCS";

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            List<Experiment> experiments = generalInformationService.listExperimentsHavingSamples(session, Arrays.asList(project), experimentType);
            assertEquals(1, experiments.size());
            assertEquals("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST", experiments.get(0).getIdentifier());
        } else
        {
            try
            {
                generalInformationService.listExperimentsHavingSamples(session, Arrays.asList(project), experimentType);
                Assert.fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testSearchForExperimentsWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        String session = generalInformationService.tryToAuthenticateForAllServices(user.getUserId(), PASSWORD);

        SearchCriteria criteria = new SearchCriteria();
        criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.PROJECT, "TEST-PROJECT"));

        if (user.isDisabledProjectUser())
        {
            try
            {
                generalInformationService.searchForExperiments(session, criteria);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        } else
        {
            List<Experiment> experiments = generalInformationService.searchForExperiments(session, criteria);

            if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
            {
                assertEquals(1, experiments.size());
                assertEquals("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST", experiments.get(0).getIdentifier());
            } else
            {
                assertEquals(0, experiments.size());
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testSearchForSamplesWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        String session = generalInformationService.tryToAuthenticateForAllServices(user.getUserId(), PASSWORD);

        SearchCriteria criteria = new SearchCriteria();
        criteria.setOperator(SearchOperator.MATCH_ALL_CLAUSES);
        criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.SPACE, "TEST-SPACE"));
        criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE, "CELL_PLATE"));

        if (user.isDisabledProjectUser())
        {
            try
            {
                generalInformationService.searchForSamples(session, criteria);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        } else
        {
            List<Sample> samples = generalInformationService.searchForSamples(session, criteria);

            if (user.isInstanceUser() || user.isTestSpaceUser())
            {
                assertEntities("[/TEST-SPACE/NOE/CP-TEST-4, /TEST-SPACE/TEST-PROJECT/FV-TEST]", samples);
            } else if (user.isTestProjectUser())
            {
                assertEntities("[/TEST-SPACE/TEST-PROJECT/FV-TEST]", samples);
            } else
            {
                assertEntities("[]", samples);
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testSearchForSamplesWithFetchOptionsWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        String session = generalInformationService.tryToAuthenticateForAllServices(user.getUserId(), PASSWORD);

        SearchCriteria criteria = new SearchCriteria();
        criteria.setOperator(SearchOperator.MATCH_ALL_CLAUSES);
        criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.SPACE, "TEST-SPACE"));
        criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE, "CELL_PLATE"));

        if (user.isDisabledProjectUser())
        {
            try
            {
                generalInformationService.searchForSamples(session, criteria, EnumSet.of(SampleFetchOption.BASIC));
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        } else
        {
            List<Sample> samples = generalInformationService.searchForSamples(session, criteria, EnumSet.of(SampleFetchOption.BASIC));

            if (user.isInstanceUser() || user.isTestSpaceUser())
            {
                assertEntities("[/TEST-SPACE/NOE/CP-TEST-4, /TEST-SPACE/TEST-PROJECT/FV-TEST]", samples);
            } else if (user.isTestProjectUser())
            {
                assertEntities("[/TEST-SPACE/TEST-PROJECT/FV-TEST]", samples);
            } else
            {
                assertEntities("[]", samples);
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testSearchForSamplesOnBehalfOfUserWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        String session = generalInformationService.tryToAuthenticateForAllServices(TEST_USER, PASSWORD);

        SearchCriteria criteria = new SearchCriteria();
        criteria.setOperator(SearchOperator.MATCH_ALL_CLAUSES);
        criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.SPACE, "TEST-SPACE"));
        criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE, "CELL_PLATE"));

        List<Sample> samples =
                generalInformationService.searchForSamplesOnBehalfOfUser(session, criteria, EnumSet.of(SampleFetchOption.BASIC), user.getUserId());

        if (user.isInstanceUser() || user.isTestSpaceUser())
        {
            assertEntities("[/TEST-SPACE/NOE/CP-TEST-4, /TEST-SPACE/TEST-PROJECT/FV-TEST]", samples);
        } else if (user.isTestProjectUser() && user.hasPAEnabled())
        {
            assertEntities("[/TEST-SPACE/TEST-PROJECT/FV-TEST]", samples);
        } else
        {
            assertEntities("[]", samples);
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testFilterSamplesVisibleToUserWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        String session = generalInformationService.tryToAuthenticateForAllServices(TEST_USER, PASSWORD);

        SearchCriteria criteria = new SearchCriteria();
        criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.SPACE, "TEST-SPACE"));

        List<Sample> samples = generalInformationService.searchForSamples(session, criteria);
        List<Sample> filteredSamples = generalInformationService.filterSamplesVisibleToUser(session, samples, user.getUserId());

        if (user.isInstanceUser() || user.isTestSpaceUser())
        {
            assertEntities(
                    "[/TEST-SPACE/NOE/CP-TEST-4, /TEST-SPACE/TEST-PROJECT/EV-INVALID, "
                    + "/TEST-SPACE/TEST-PROJECT/EV-NOT_INVALID, /TEST-SPACE/TEST-PROJECT/EV-PARENT, "
                    + "/TEST-SPACE/TEST-PROJECT/EV-PARENT-NORMAL, /TEST-SPACE/TEST-PROJECT/EV-TEST, "
                    + "/TEST-SPACE/TEST-PROJECT/FV-TEST, /TEST-SPACE/TEST-PROJECT/SAMPLE-TO-DELETE]",
                    filteredSamples);
        } else if (user.isTestProjectUser() && user.hasPAEnabled())
        {
            assertEntities(
                    "[/TEST-SPACE/TEST-PROJECT/EV-INVALID, /TEST-SPACE/TEST-PROJECT/EV-NOT_INVALID, "
                    + "/TEST-SPACE/TEST-PROJECT/EV-PARENT, /TEST-SPACE/TEST-PROJECT/EV-PARENT-NORMAL, "
                    + "/TEST-SPACE/TEST-PROJECT/EV-TEST, /TEST-SPACE/TEST-PROJECT/FV-TEST, "
                    + "/TEST-SPACE/TEST-PROJECT/SAMPLE-TO-DELETE]",
                    filteredSamples);
        } else
        {
            assertEntities("[]", filteredSamples);
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testFilterExperimentVisibleToUserWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        String session = generalInformationService.tryToAuthenticateForAllServices(TEST_USER, PASSWORD);

        SearchCriteria criteria = new SearchCriteria();
        criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.SPACE, "TEST-SPACE"));

        List<Experiment> experiments = generalInformationService.searchForExperiments(session, criteria);
        List<Experiment> filteredExperiments = generalInformationService.filterExperimentsVisibleToUser(session, experiments, user.getUserId());

        if (user.isInstanceUser() || user.isTestSpaceUser())
        {
            assertEntities(
                    "[/TEST-SPACE/NOE/EXP-TEST-2, /TEST-SPACE/NOE/EXPERIMENT-TO-DELETE, /TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST]",
                    filteredExperiments);
        } else if (user.isTestProjectUser() && user.hasPAEnabled())
        {
            assertEntities("[/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST]", filteredExperiments);
        } else
        {
            assertEntities("[]", filteredExperiments);
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListProjectsWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        String session = generalInformationService.tryToAuthenticateForAllServices(user.getUserId(), PASSWORD);

        if (user.isDisabledProjectUser())
        {
            try
            {
                generalInformationService.listProjects(session);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        } else
        {
            List<Project> projects = generalInformationService.listProjects(session);

            if (user.isInstanceUser())
            {
                assertEntities(
                        "[/CISD/DEFAULT, /CISD/NEMO, /CISD/NOE, /TEST-SPACE/NOE, /TEST-SPACE/PROJECT-TO-DELETE, /TEST-SPACE/TEST-PROJECT, /TESTGROUP/TESTPROJ]",
                        projects);
            } else if (user.isTestSpaceUser())
            {
                assertEntities("[/TEST-SPACE/NOE, /TEST-SPACE/PROJECT-TO-DELETE, /TEST-SPACE/TEST-PROJECT]", projects);
            } else if (user.isTestProjectUser())
            {
                assertEntities("[/TEST-SPACE/PROJECT-TO-DELETE, /TEST-SPACE/TEST-PROJECT]", projects);
            } else if (user.isTestGroupUser())
            {
                assertEntities("[/TESTGROUP/TESTPROJ]", projects);
            } else
            {
                assertEntities("[]", projects);
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListProjectsOnBehalfOfUserWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        String session = generalInformationService.tryToAuthenticateForAllServices(TEST_USER, PASSWORD);

        List<Project> projects = generalInformationService.listProjectsOnBehalfOfUser(session, user.getUserId());

        if (user.isInstanceUser())
        {
            assertEntities(
                    "[/CISD/DEFAULT, /CISD/NEMO, /CISD/NOE, /TEST-SPACE/NOE, /TEST-SPACE/PROJECT-TO-DELETE, /TEST-SPACE/TEST-PROJECT, /TESTGROUP/TESTPROJ]",
                    projects);
        } else if (user.isTestSpaceUser())
        {
            assertEntities("[/TEST-SPACE/NOE, /TEST-SPACE/PROJECT-TO-DELETE, /TEST-SPACE/TEST-PROJECT]", projects);
        } else if (user.isTestProjectUser() && user.hasPAEnabled())
        {
            assertEntities("[/TEST-SPACE/PROJECT-TO-DELETE, /TEST-SPACE/TEST-PROJECT]", projects);
        } else if (user.isTestGroupUser())
        {
            assertEntities("[/TESTGROUP/TESTPROJ]", projects);
        } else
        {
            assertEntities("[]", projects);
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListAttachmentForProjectWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        String session = generalInformationService.tryToAuthenticateForAllServices(user.getUserId(), PASSWORD);

        ProjectIdentifierId projectId = new ProjectIdentifierId("/TEST-SPACE/TEST-PROJECT");

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            List<Attachment> attachments =
                    generalInformationService.listAttachmentsForProject(session, projectId, false);
            assertEquals(1, attachments.size());
            assertEquals("testProject.txt", attachments.get(0).getFileName());
        } else
        {
            try
            {
                generalInformationService.listAttachmentsForProject(session, projectId, false);
                Assert.fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListAttachmentsForExperimentWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        String session = generalInformationService.tryToAuthenticateForAllServices(user.getUserId(), PASSWORD);
        String identifier = "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST";

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            List<Attachment> attachments =
                    generalInformationService.listAttachmentsForExperiment(session, new ExperimentIdentifierId(identifier), false);
            assertEquals(1, attachments.size());
            assertEquals("testExperiment.txt", attachments.get(0).getFileName());
        } else
        {
            try
            {
                generalInformationService.listAttachmentsForExperiment(session, new ExperimentIdentifierId(identifier), false);
                Assert.fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test
    public void testListExperimentsHavingSamples()
    {
        List<Project> projects = Arrays.asList(new Project("CISD", "NOE"));
        List<Experiment> experiments =
                generalInformationService.listExperimentsHavingSamples(sessionToken, projects,
                        "COMPOUND_HCS");

        assertEntities("[/CISD/NOE/EXP-TEST-2]", experiments);
        assertEquals("Experiment[/CISD/NOE/EXP-TEST-2,COMPOUND_HCS,{DESCRIPTION=desc2}]",
                experiments.get(0).toString());

        loginAsObserver();
        try
        {
            generalInformationService.listExperimentsHavingSamples(sessionToken, projects,
                    "COMPOUND_HCS");
            fail("AuthorizationFailureException expected");
        } catch (AuthorizationFailureException ex)
        {
            assertEquals(
                    "Authorization failure: ERROR: \"User 'observer' does not have enough privileges.\".",
                    ex.getMessage());
        }
    }

    @Test
    public void testListExperimentsHavingDataSetsSamples()
    {
        List<Project> projects = Arrays.asList(new Project("CISD", "NOE"));
        List<Experiment> experiments =
                generalInformationService.listExperimentsHavingDataSets(sessionToken, projects,
                        "COMPOUND_HCS");

        assertEntities("[/CISD/NOE/EXP-TEST-2]", experiments);
        assertEquals("Experiment[/CISD/NOE/EXP-TEST-2,COMPOUND_HCS,{DESCRIPTION=desc2}]",
                experiments.get(0).toString());

        loginAsObserver();
        try
        {
            generalInformationService.listExperimentsHavingDataSets(sessionToken, projects,
                    "COMPOUND_HCS");
            fail("AuthorizationFailureException expected");
        } catch (AuthorizationFailureException ex)
        {
            assertEquals(
                    "Authorization failure: ERROR: \"User 'observer' does not have enough privileges.\".",
                    ex.getMessage());
        }
    }

    @Test
    public void testListExperimentsByIdentifiers()
    {
        List<Experiment> experiments =
                generalInformationService.listExperiments(sessionToken,
                        Arrays.asList("/CISD/NOE/EXP-TEST-2"));

        assertEquals("[Experiment[/CISD/NOE/EXP-TEST-2,COMPOUND_HCS,{DESCRIPTION=desc2}]]",
                experiments.toString());

        loginAsObserver();
        try
        {
            generalInformationService.listExperiments(sessionToken,
                    Arrays.asList("/CISD/NEMO/EXP1"));
            fail("AuthorizationFailureException expected");
        } catch (AuthorizationFailureException ex)
        {
            assertEquals(
                    "Authorization failure: ERROR: \"User 'observer' does not have enough privileges.\".",
                    ex.getMessage());
        }
    }

    private void loginAsObserver()
    {
        generalInformationService.logout(sessionToken);
        sessionToken = generalInformationService.tryToAuthenticateForAllServices("observer", "a");
    }

    private interface IToStringDelegate<T>
    {
        String toString(T t);
    }

    private <T> void assertCollection(String expectedEntities, List<T> entities,
            IToStringDelegate<T> toStringMethod)
    {
        List<String> identifiers = new ArrayList<String>();
        for (T entity : entities)
        {
            identifiers.add(toStringMethod.toString(entity));
        }
        Collections.sort(identifiers);
        assertEquals(expectedEntities, identifiers.toString());
    }

    private void assertDataSets(String expectedDataSets, List<DataSet> dataSets)
    {
        List<String> codes = new ArrayList<String>();
        for (DataSet dataSet : dataSets)
        {
            codes.add(dataSet.getCode());
        }
        Collections.sort(codes);
        assertEquals(expectedDataSets, codes.toString());
    }

    private void assertMaterials(String expectedMaterials, List<Material> materials)
    {
        List<String> codes = new ArrayList<String>();
        for (Material material : materials)
        {
            codes.add(material.getAugmentedCode());
        }
        Collections.sort(codes);
        assertEquals(expectedMaterials, codes.toString());
    }

    @Test
    public void testSearchForMaterialsReferencing() throws java.text.ParseException
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE,
                "SELF_REF"));

        List<Material> materials =
                generalInformationService.searchForMaterials(sessionToken, searchCriteria);

        assertCollection("[SRM_1, SRM_1A]", materials, new IToStringDelegate<Material>()
            {
                @Override
                public String toString(Material t)
                {
                    return t.getMaterialCode();
                }
            });

        Material srm1 = materials.get(0);
        Material srm1a = materials.get(1);

        assertSelfReferencedMaterials(srm1, srm1a);
    }

    @Test
    public void testSearchForMaterialsByCode() throws java.text.ParseException
    {
        List<MaterialIdentifier> materialIdentifiers = new LinkedList<MaterialIdentifier>();

        MaterialTypeIdentifier mt = new MaterialTypeIdentifier("SELF_REF");
        materialIdentifiers.add(new MaterialIdentifier(mt, "SRM_1"));
        materialIdentifiers.add(new MaterialIdentifier(mt, "SRM_1A"));

        List<Material> materials =
                generalInformationService.getMaterialByCodes(sessionToken, materialIdentifiers);

        assertCollection("[SRM_1, SRM_1A]", materials, new IToStringDelegate<Material>()
            {
                @Override
                public String toString(Material t)
                {
                    return t.getMaterialCode();
                }
            });

        Material srm1 = materials.get(0);
        Material srm1a = materials.get(1);

        assertSelfReferencedMaterials(srm1, srm1a);
    }

    private void assertSelfReferencedMaterials(Material srm1, Material srm1a) throws ParseException
    {
        assertEquals("SRM_1", srm1.getMaterialCode());
        assertEquals("SRM_1A", srm1a.getMaterialCode());

        assertEquals("Material with attached material", srm1.getProperties().get("DESCRIPTION"));
        assertEquals("Material wich is attached material", srm1a.getProperties().get("DESCRIPTION"));

        assertEquals("Referenced material should be srm_1a", srm1a.getMaterialCode(), srm1
                .getMaterialProperties().get("ANY_MATERIAL").getMaterialCode());

        assertEquals("Referenced material should be present in properties as code", String.format(
                "%s (%s)", srm1a.getMaterialCode(), srm1a.getMaterialTypeIdentifier()
                        .getMaterialTypeCode()),
                srm1.getProperties().get("ANY_MATERIAL"));

        Date date2012 = new SimpleDateFormat("yyyy-MM-dd").parse("2012-01-02");
        Date date2013 = new SimpleDateFormat("yyyy-MM-dd").parse("2013-01-02");

        assertEquals("The date should be after 2012", true, srm1.getRegistrationDetails()
                .getRegistrationDate().after(date2012));
        assertEquals("The date should be before 2013", true, srm1.getRegistrationDetails()
                .getRegistrationDate().before(date2013));
    }

    @Test
    public void testSearchForMaterials()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE,
                "BACTERIUM"));

        List<Material> materials =
                generalInformationService.searchForMaterials(sessionToken, searchCriteria);

        assertCollection("[BACTERIUM-X, BACTERIUM-Y, BACTERIUM1, BACTERIUM2]", materials,
                new MaterialToCode());
    }

    @Test
    public void testSearchForMaterialsByMetaprojectName()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(
                MatchClauseAttribute.METAPROJECT, "TEST_METAPROJECTS"));

        List<Material> materials =
                generalInformationService.searchForMaterials(sessionToken, searchCriteria);

        assertCollection("[AD3]", materials, new MaterialToCode());
    }

    @Test
    public void testSearchForMaterialsByMetaprojectNameOwnedBySomebodyElse()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(
                MatchClauseAttribute.METAPROJECT, "TEST_METAPROJECTS_2"));

        List<Material> materials =
                generalInformationService.searchForMaterials(sessionToken, searchCriteria);

        assertCollection("[]", materials, new MaterialToCode());
    }

    @Test
    public void testSearchForMaterialsByMetaprojectIdentifier()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(
                MatchClauseAttribute.METAPROJECT, "/test/TEST_METAPROJECTS"));

        List<Material> materials =
                generalInformationService.searchForMaterials(sessionToken, searchCriteria);

        assertCollection("[AD3]", materials, new MaterialToCode());
    }

    @Test
    public void testSearchForMaterialsByMetaprojectIdentifierOwnedBySomebodyElse()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(
                MatchClauseAttribute.METAPROJECT, "/test_role/TEST_METAPROJECTS_2"));

        List<Material> materials =
                generalInformationService.searchForMaterials(sessionToken, searchCriteria);

        assertCollection("[]", materials, new MaterialToCode());
    }

    @Test
    public void testSearchForMaterialsByAnyFieldMatchingProperty()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAnyFieldMatch("\"Influenza A virus\""));

        List<Material> materials =
                generalInformationService.searchForMaterials(sessionToken, searchCriteria);

        assertCollection("[FLU]", materials, new MaterialToCode());
    }

    @Test
    public void testSearchForMaterialsByAnyFieldMatchingAttribute()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAnyFieldMatch("VIRUS1"));

        List<Material> materials =
                generalInformationService.searchForMaterials(sessionToken, searchCriteria);

        assertCollection("[VIRUS1]", materials, new MaterialToCode());
    }

    @Test
    public void testSearchForMaterialsByAnyProperty()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAnyPropertyMatch("\"Influenza A virus\""));

        List<Material> materials =
                generalInformationService.searchForMaterials(sessionToken, searchCriteria);

        assertCollection("[FLU]", materials, new MaterialToCode());
    }

    @Test
    public void testSearchForMaterialsByModificationDate()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createTimeAttributeMatch(
                MatchClauseTimeAttribute.MODIFICATION_DATE, CompareMode.LESS_THAN_OR_EQUAL,
                "2009-03-19", "+1"));

        List<Material> materials = filter(generalInformationService.searchForMaterials(sessionToken, searchCriteria));
        assertEquals(2774, materials.size());

        searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createTimeAttributeMatch(
                MatchClauseTimeAttribute.MODIFICATION_DATE, CompareMode.GREATER_THAN_OR_EQUAL,
                "2009-03-19", "+1"));

        materials = filter(generalInformationService.searchForMaterials(sessionToken, searchCriteria));
        assertEquals(0, materials.size());
    }

    private List<Material> filter(List<Material> materials)
    {
        return materials
                .stream().filter(m -> m.getMaterialTypeIdentifier().getMaterialTypeCode().equals("SIRNA"))
                .collect(Collectors.toList());
    }

    @Test
    public void testSearchForMaterialsByRegistratorUserId()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.REGISTRATOR_USER_ID, "etlserver"));

        List<Material> materials =
                generalInformationService.searchForMaterials(sessionToken, searchCriteria);
        assertMaterials("[BACTERIUM1 (BACTERIUM), BACTERIUM2 (BACTERIUM)]", materials);
    }

    @Test
    public void testSearchForMaterialsByRegistratorFirstName()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.REGISTRATOR_FIRST_NAME, "\"John 2\""));

        List<Material> materials =
                generalInformationService.searchForMaterials(sessionToken, searchCriteria);
        assertMaterials("[BACTERIUM1 (BACTERIUM), BACTERIUM2 (BACTERIUM)]", materials);
    }

    @Test
    public void testSearchForMaterialsByRegistratorLastName()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.REGISTRATOR_LAST_NAME, "\"ETL Server\""));

        List<Material> materials =
                generalInformationService.searchForMaterials(sessionToken, searchCriteria);
        assertMaterials("[BACTERIUM1 (BACTERIUM), BACTERIUM2 (BACTERIUM)]", materials);
    }

    @Test
    public void testSearchForMaterialsByRegistratorEmail()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.REGISTRATOR_EMAIL, "\"etlserver@systemsx.ch\""));

        List<Material> materials =
                generalInformationService.searchForMaterials(sessionToken, searchCriteria);
        assertMaterials("[BACTERIUM1 (BACTERIUM), BACTERIUM2 (BACTERIUM)]", materials);
    }

    @Test
    public void testSearchForMaterialsByModifierUserId()
    {
        // search by a modifier not supported yet
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.MODIFIER_USER_ID, "etlserver"));

        List<Material> materials =
                generalInformationService.searchForMaterials(sessionToken, searchCriteria);
        assertMaterials("[]", materials);
    }

    @Test
    public void testSearchForMaterialsByModifierFirstName()
    {
        // search by a modifier not supported yet
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.MODIFIER_FIRST_NAME, "\"John 2\""));

        List<Material> materials =
                generalInformationService.searchForMaterials(sessionToken, searchCriteria);
        assertMaterials("[]", materials);
    }

    @Test
    public void testSearchForMaterialsByModifierLastName()
    {
        // search by a modifier not supported yet
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.MODIFIER_LAST_NAME, "\"ETL Server\""));

        List<Material> materials =
                generalInformationService.searchForMaterials(sessionToken, searchCriteria);
        assertMaterials("[]", materials);
    }

    @Test
    public void testSearchForMaterialsByModifierEmail()
    {
        // search by a modifier not supported yet
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.MODIFIER_EMAIL, "\"etlserver@systemsx.ch\""));

        List<Material> materials =
                generalInformationService.searchForMaterials(sessionToken, searchCriteria);
        assertMaterials("[]", materials);
    }

    @Test
    public void testListMetaprojects()
    {
        List<Metaproject> metaprojects = generalInformationService.listMetaprojects(sessionToken);

        assertEquals(2, metaprojects.size());
    }

    @Test(expectedExceptions = AuthorizationFailureException.class)
    public void testGetMetaprojectOwnedBySomebodyElse()
    {
        generalInformationService.getMetaproject(sessionToken, new MetaprojectIdentifierId(
                "/test_role/TEST_METAPROJECTS"));
    }

    @Test
    public void testGetMetaprojects()
    {
        MetaprojectAssignments metaprojectAssignments =
                generalInformationService.getMetaproject(sessionToken, new MetaprojectIdentifierId(
                        "/test/TEST_METAPROJECTS"));

        assertEquals(1, metaprojectAssignments.getDataSets().size());
        assertEquals(1, metaprojectAssignments.getMaterials().size());
        assertEquals(1, metaprojectAssignments.getSamples().size());
        assertEquals(2, metaprojectAssignments.getExperiments().size());

        generalInformationService.logout(sessionToken);
        sessionToken = generalInformationService.tryToAuthenticateForAllServices("test_role", "a");

        metaprojectAssignments =
                generalInformationService.getMetaproject(sessionToken, new MetaprojectIdentifierId(
                        "/test_role/TEST_METAPROJECTS"));

        assertEquals(1, metaprojectAssignments.getMaterials().size());
        assertEquals(1, metaprojectAssignments.getDataSets().size());
        assertTrue(metaprojectAssignments.getDataSets().get(0).isStub());
        assertTrue(metaprojectAssignments.getDataSets().get(0).toString().contains("STUB"));
        assertEquals(1, metaprojectAssignments.getSamples().size());
        assertTrue(metaprojectAssignments.getSamples().get(0).isStub());
        assertTrue(metaprojectAssignments.getSamples().get(0).toString().contains("STUB"));
        assertEquals(1, metaprojectAssignments.getExperiments().size());
        assertTrue(metaprojectAssignments.getExperiments().get(0).isStub());
        assertTrue(metaprojectAssignments.getExperiments().get(0).toString().contains("STUB"));
    }

    @Test
    public void testGetMetaprojectsOnBehalfOfUser()
    {
        String instanceObserverId = "instance-observer";
        commonServer.registerPerson(systemSessionToken, instanceObserverId);
        commonServer.registerInstanceRole(systemSessionToken, RoleCode.OBSERVER,
                Grantee.createPerson(instanceObserverId));
        sessionToken =
                generalInformationService.tryToAuthenticateForAllServices(instanceObserverId, "a");

        List<Metaproject> metaProjects =
                generalInformationService.listMetaprojectsOnBehalfOfUser(sessionToken, "test");
        Collections.sort(metaProjects, new Comparator<Metaproject>()
            {
                @Override
                public int compare(Metaproject o1, Metaproject o2)
                {
                    return o1.getCode().compareTo(o2.getCode());
                }
            });

        assertEquals("ANOTHER_TEST_METAPROJECTS", metaProjects.get(0).getCode());
        assertEquals("TEST_METAPROJECTS", metaProjects.get(1).getCode());
        assertEquals(2, metaProjects.size());
        MetaprojectAssignments mas =
                generalInformationService.getMetaprojectOnBehalfOfUser(sessionToken,
                        new MetaprojectTechIdId(metaProjects.get(1).getId()), "test");
        assertEquals("[MaterialIdentifier [materialCode=AD3, "
                + "materialTypeIdentifier=MaterialTypeIdentifier [materialTypeCode=VIRUS]]]",
                mas
                        .getMaterials().toString());
        assertEntities("[/CISD/NEMO/EXP11, /TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST]",
                mas.getExperiments());
        assertEquals("[Sample[/TEST-SPACE/TEST-PROJECT/EV-TEST,VALIDATE_CHILDREN,{COMMENT=test comment},parents=?,children=?]]", mas
                .getSamples().toString());
        assertEquals("[DataSet[20120619092259000-22,/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST,"
                + "<null>,HCS_IMAGE,{}]]", mas.getDataSets().toString());
    }

    @Test
    public void testGetMetaprojectReturnsMetaprojectForNameWithDifferentCase()
    {
        MetaprojectAssignments metaprojectAssignments =
                generalInformationService.getMetaproject(sessionToken, new MetaprojectIdentifierId(
                        "/test/TEST_metaprojects"));
        assertEquals("/test/TEST_METAPROJECTS", metaprojectAssignments.getMetaproject()
                .getIdentifier());
        assertEquals("TEST_METAPROJECTS", metaprojectAssignments.getMetaproject().getName());
    }

    @Test
    public void testListAttachmentsForExperimentAllVersions() throws ParseException
    {
        final List<Experiment> experiments =
                generalInformationService.listExperiments(sessionToken,
                        Collections.singletonList("/CISD/NEMO/EXP1"));
        assertEquals(1, experiments.size());

        final List<Attachment> attachments =
                generalInformationService.listAttachmentsForExperiment(sessionToken,
                        new ExperimentTechIdId(experiments.get(0).getId()), true);
        assertEquals(4, attachments.size());
        int version = 4;
        final String[] regDates =
                new String[] { "2008-12-10 13:49:27.901 +0100", "2008-12-10 13:49:20.236 +0100",
                        "2008-12-10 13:49:14.564 +0100", "2008-12-10 13:48:17.996 +0100" };
        for (Attachment a : attachments)
        {
            assertEquals("exampleExperiments.txt", a.getFileName());
            assertEquals(version, a.getVersion());
            assertEquals(version == 4 ? "Latest version" : "", a.getTitle());
            assertEquals(version == 3 ? "Second latest version" : "", a.getDescription());
            final Date date =
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z").parse(regDates[4 - version]);
            assertEquals(date, a.getRegistrationDetails().getRegistrationDate());
            assertEquals("test", a.getRegistrationDetails().getUserId());
            assertEquals("franz-josef.elmer@systemsx.ch", a.getRegistrationDetails().getUserEmail());
            assertNotNull(a.getRegistrationDetails().getUserFirstName());
            assertNotNull(a.getRegistrationDetails().getUserLastName());
            assertEquals("/openbis/openbis/attachment-download?sessionID=" + sessionToken
                    + "&attachmentHolder=EXPERIMENT&id=2&fileName=exampleExperiments.txt&version="
                    + version, a.getDownloadLink());
            --version;
        }
    }

    @Test
    public void testListAttachmentsForExperimentLatestVersion() throws ParseException
    {
        final List<Experiment> experiments =
                generalInformationService.listExperiments(sessionToken,
                        Collections.singletonList("/CISD/NEMO/EXP1"));
        assertEquals(1, experiments.size());

        final List<Attachment> attachments =
                generalInformationService.listAttachmentsForExperiment(sessionToken,
                        new ExperimentPermIdId(experiments.get(0).getPermId()), false);
        assertEquals(1, attachments.size());
        final Attachment a = attachments.get(0);
        assertEquals("exampleExperiments.txt", a.getFileName());
        assertEquals(4, a.getVersion());
        assertEquals("Latest version", a.getTitle());
        assertEquals("", a.getDescription());
        final Date date =
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z")
                        .parse("2008-12-10 13:49:27.901 +0100");
        assertEquals(date, a.getRegistrationDetails().getRegistrationDate());
        assertEquals("test", a.getRegistrationDetails().getUserId());
        assertEquals("franz-josef.elmer@systemsx.ch", a.getRegistrationDetails().getUserEmail());
        assertNotNull(a.getRegistrationDetails().getUserFirstName());
        assertNotNull(a.getRegistrationDetails().getUserLastName());
        assertEquals("/openbis/openbis/attachment-download?sessionID=" + sessionToken
                + "&attachmentHolder=EXPERIMENT&id=2&fileName=exampleExperiments.txt&version=4",
                a.getDownloadLink());

        final List<Attachment> attachments2 =
                generalInformationService.listAttachmentsForExperiment(sessionToken,
                        new ExperimentIdentifierId("/CISD/NEMO/EXP1"), false);

        assertEquals(1, attachments2.size());

        final Attachment a2 = attachments2.get(0);
        assertEquals(a, a2);
    }

    @Test
    public void testListAttachmentsForSample() throws ParseException
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE,
                "3VCP6"));

        final List<Sample> samples =
                generalInformationService.searchForSamples(sessionToken, searchCriteria);
        assertEquals(1, samples.size());

        final List<Attachment> attachments =
                generalInformationService.listAttachmentsForSample(sessionToken,
                        new SampleTechIdId(samples.get(0).getId()), true);
        assertEquals(1, attachments.size());

        final Attachment a = attachments.get(0);
        assertEquals("sampleHistory.txt", a.getFileName());
        assertEquals("", a.getTitle());
        assertEquals("", a.getDescription());
        assertEquals(1, a.getVersion());
        final Date date =
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z")
                        .parse("2009-06-09 17:00:00.000 +0200");
        assertEquals(date, a.getRegistrationDetails().getRegistrationDate());
        assertEquals("test", a.getRegistrationDetails().getUserId());
        assertEquals("franz-josef.elmer@systemsx.ch", a.getRegistrationDetails().getUserEmail());
        assertNotNull(a.getRegistrationDetails().getUserFirstName());
        assertNotNull(a.getRegistrationDetails().getUserLastName());
        assertEquals("/openbis/openbis/attachment-download?sessionID=" + sessionToken
                + "&attachmentHolder=SAMPLE&id=987&fileName=sampleHistory.txt&version=1",
                a.getDownloadLink());

        final List<Attachment> attachments2 =
                generalInformationService.listAttachmentsForSample(sessionToken,
                        new SamplePermIdId("200811050946559-980"), true);

        assertEquals(1, attachments2.size());

        final Attachment a2 = attachments2.get(0);
        assertEquals(a, a2);

        final List<Attachment> attachments3 =
                generalInformationService.listAttachmentsForSample(sessionToken,
                        new SampleIdentifierId("/CISD/NEMO/3VCP6"), true);

        assertEquals(1, attachments3.size());

        final Attachment a3 = attachments3.get(0);
        assertEquals(a, a3);
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListAttachmentsForSampleWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        String session = generalInformationService.tryToAuthenticateForAllServices(user.getUserId(), PASSWORD);

        SampleTechIdId sampleId = new SampleTechIdId(1054L); // /TEST-SPACE/FV-TEST

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            List<Attachment> attachments = generalInformationService.listAttachmentsForSample(session, sampleId, false);
            assertEquals(attachments.size(), 1);
        } else
        {
            try
            {
                generalInformationService.listAttachmentsForSample(session, sampleId, false);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test
    public void testListAttachmentsForProjectNoAttachment()
    {
        final List<Attachment> attachments =
                generalInformationService.listAttachmentsForProject(sessionToken,
                        new ProjectIdentifierId("/CISD/DEFAULT"), true);

        assertEquals(0, attachments.size());
    }

    @Test
    public void testListAttachmentsForProjectNonExisting()
    {
        try
        {
            generalInformationService.listAttachmentsForProject(sessionToken,
                    new ProjectIdentifierId("/NONE/EXISTENT"), true);
        } catch (UserFailureException ex)
        {
            assertEquals("No project found for id '/NONE/EXISTENT'.", ex.getMessage());
        }
    }

    @Test
    public void testListAttachmentsForProject() throws ParseException
    {
        final List<Attachment> attachments =
                generalInformationService.listAttachmentsForProject(sessionToken,
                        new ProjectPermIdId("20120814110011738-103"), true);

        assertEquals(1, attachments.size());

        final Attachment a = attachments.get(0);
        assertEquals("projectDescription.txt", a.getFileName());
        assertEquals("The Project", a.getTitle());
        assertEquals("All about it.", a.getDescription());
        assertEquals(1, a.getVersion());
        final Date date =
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z")
                        .parse("2012-01-03 08:27:57.123 +0100");
        assertEquals(date, a.getRegistrationDetails().getRegistrationDate());
        assertEquals("test", a.getRegistrationDetails().getUserId());
        assertEquals("franz-josef.elmer@systemsx.ch", a.getRegistrationDetails().getUserEmail());
        assertNotNull(a.getRegistrationDetails().getUserFirstName());
        assertNotNull(a.getRegistrationDetails().getUserLastName());
        assertEquals("/openbis/openbis/attachment-download?sessionID=" + sessionToken
                + "&attachmentHolder=PROJECT&id=3&fileName=projectDescription.txt&version=1",
                a.getDownloadLink());

        final List<Attachment> attachments2 =
                generalInformationService.listAttachmentsForProject(sessionToken,
                        new ProjectIdentifierId("/CISD/NEMO"), true);

        assertEquals(1, attachments2.size());

        final Attachment a2 = attachments2.get(0);
        assertEquals(a, a2);

    }

    @Test
    public void testListDeletions()
    {
        String adminUserSessionToken = generalInformationService.tryToAuthenticateForAllServices("test", "password");
        String spaceUserSessionToken = generalInformationService.tryToAuthenticateForAllServices("test_role", "password");

        List<Deletion> adminUserDeletionsBefore = generalInformationService.listDeletions(adminUserSessionToken, null);
        List<Deletion> spaceUserDeletionsBefore = generalInformationService.listDeletions(spaceUserSessionToken, null);

        generalInformationChangingService.deleteDataSets(adminUserSessionToken, Arrays.asList("20081105092159188-3"), "test access to deletion",
                DeletionType.TRASH);

        List<Deletion> adminUserDeletionsAfter = generalInformationService.listDeletions(adminUserSessionToken, null);
        List<Deletion> spaceUserDeletionsAfter = generalInformationService.listDeletions(spaceUserSessionToken, null);

        assertEquals(adminUserDeletionsBefore.size() + 1, adminUserDeletionsAfter.size());
        assertEquals(spaceUserDeletionsBefore.size(), spaceUserDeletionsAfter.size());

        generalInformationService.logout(adminUserSessionToken);
        generalInformationService.logout(spaceUserSessionToken);
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListDataSetsWithSamplesWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        String session = generalInformationService.tryToAuthenticateForAllServices(user.getUserId(), PASSWORD);

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            List<DataSet> dataSets = generalInformationService.listDataSets(session, Arrays.asList(createFvTestSample()));
            assertEquals(1, dataSets.size());
            assertEquals("20120628092259000-41", dataSets.get(0).getCode());
        } else
        {
            try
            {
                generalInformationService.listDataSets(session, Arrays.asList(createFvTestSample()));
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListDataSetsWithSamplesAndConnectionsWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        String session = generalInformationService.tryToAuthenticateForAllServices(user.getUserId(), PASSWORD);

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            List<DataSet> dataSets = generalInformationService.listDataSets(session, Arrays.asList(createFvTestSample()), null);
            assertEquals(1, dataSets.size());
            assertEquals("20120628092259000-41", dataSets.get(0).getCode());
        } else
        {
            try
            {
                generalInformationService.listDataSets(session, Arrays.asList(createFvTestSample()), null);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListDataSetsForSampleWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        String session = generalInformationService.tryToAuthenticateForAllServices(user.getUserId(), PASSWORD);

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            List<DataSet> dataSets = generalInformationService.listDataSetsForSample(session, createFvTestSample(), true);
            assertEquals(1, dataSets.size());
            assertEquals("20120628092259000-41", dataSets.get(0).getCode());
        } else
        {
            try
            {
                generalInformationService.listDataSetsForSample(session, createFvTestSample(), true);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListDataSetsOnBehalfOfUserWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        String session = generalInformationService.tryToAuthenticateForAllServices(TEST_USER, PASSWORD);

        List<DataSet> dataSets =
                generalInformationService.listDataSetsOnBehalfOfUser(session, Arrays.asList(createFvTestSample()), null, user.getUserId());

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            assertEquals(1, dataSets.size());
            assertEquals("20120628092259000-41", dataSets.get(0).getCode());
        } else
        {
            assertEquals(0, dataSets.size());
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListDataSetsForExperimentWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        String session = generalInformationService.tryToAuthenticateForAllServices(user.getUserId(), PASSWORD);

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            List<DataSet> dataSets =
                    generalInformationService.listDataSetsForExperiments(session, Arrays.asList(createExpSpaceTestExperiment()), null);
            assertEquals(9, dataSets.size());
        } else
        {
            try
            {
                generalInformationService.listDataSetsForExperiments(session, Arrays.asList(createExpSpaceTestExperiment()), null);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListDataSetsForExperimentsOnBehalfOfUserWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        String session = generalInformationService.tryToAuthenticateForAllServices(TEST_USER, PASSWORD);

        List<DataSet> dataSets =
                generalInformationService.listDataSetsForExperimentsOnBehalfOfUser(session, Arrays.asList(createExpSpaceTestExperiment()), null,
                        user.getUserId());

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            assertEquals(9, dataSets.size());
        } else
        {
            assertEquals(0, dataSets.size());
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testGetDataSetMetaDataWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        String session = generalInformationService.tryToAuthenticateForAllServices(user.getUserId(), PASSWORD);

        String dataSetCode = "20120628092259000-41";

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            List<DataSet> dataSets = generalInformationService.getDataSetMetaData(session, Arrays.asList(dataSetCode));
            assertEquals(1, dataSets.size());
            assertEquals(dataSetCode, dataSets.get(0).getCode());
        } else
        {
            try
            {
                generalInformationService.getDataSetMetaData(session, Arrays.asList(dataSetCode));
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testGetDataSetMetaDataWithFetchOptionsWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        String session = generalInformationService.tryToAuthenticateForAllServices(user.getUserId(), PASSWORD);

        String dataSetCode = "20120628092259000-41";
        EnumSet<DataSetFetchOption> fetchOptions = EnumSet.noneOf(DataSetFetchOption.class);

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            List<DataSet> dataSets = generalInformationService.getDataSetMetaData(session, Arrays.asList(dataSetCode), fetchOptions);
            assertEquals(1, dataSets.size());
            assertEquals(dataSetCode, dataSets.get(0).getCode());
        } else
        {
            try
            {
                generalInformationService.getDataSetMetaData(session, Arrays.asList(dataSetCode), fetchOptions);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testSearchForDataSetsWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        String session = generalInformationService.tryToAuthenticateForAllServices(user.getUserId(), PASSWORD);

        SearchCriteria criteria = new SearchCriteria();
        criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "20120628092259000-41"));

        if (user.isDisabledProjectUser())
        {
            try
            {
                generalInformationService.searchForDataSets(session, criteria);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        } else
        {
            List<DataSet> dataSets = generalInformationService.searchForDataSets(session, criteria);

            if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
            {
                assertEquals(1, dataSets.size());
                assertEquals("20120628092259000-41", dataSets.get(0).getCode());
            } else
            {
                assertEquals(0, dataSets.size());
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testSearchForDataSetsOnBehalfOfUserWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        String session = generalInformationService.tryToAuthenticateForAllServices(TEST_USER, PASSWORD);

        SearchCriteria criteria = new SearchCriteria();
        criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "20120628092259000-41"));

        List<DataSet> dataSets = generalInformationService.searchForDataSetsOnBehalfOfUser(session, criteria, user.getUserId());

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            assertEquals(1, dataSets.size());
            assertEquals("20120628092259000-41", dataSets.get(0).getCode());
        } else
        {
            assertEquals(0, dataSets.size());
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testFilterDataSetsVisibleToUserWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        String session = generalInformationService.tryToAuthenticateForAllServices(TEST_USER, PASSWORD);

        SearchCriteria criteria = new SearchCriteria();
        criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "20120628092259000-41"));

        List<DataSet> dataSets = generalInformationService.searchForDataSets(session, criteria);

        assertEquals(1, dataSets.size());

        List<DataSet> filteredDataSets = generalInformationService.filterDataSetsVisibleToUser(session, dataSets, user.getUserId());

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            assertEquals(1, filteredDataSets.size());
            assertEquals("20120628092259000-41", dataSets.get(0).getCode());
        } else
        {
            assertEquals(0, filteredDataSets.size());
        }
    }

    private void sortDataSets(List<DataSet> dataSets)
    {
        Collections.sort(dataSets, new Comparator<DataSet>()
            {
                @Override
                public int compare(DataSet d1, DataSet d2)
                {
                    return d1.getCode().compareTo(d2.getCode());
                }
            });
    }

    private static class MaterialToCode implements IToStringDelegate<Material>
    {
        @Override
        public String toString(Material t)
        {
            return t.getMaterialCode();
        }
    }

    private static class DataSetToCode implements IToStringDelegate<DataSet>
    {
        @Override
        public String toString(DataSet t)
        {
            return t.getCode();
        }
    }

    private static Sample createFvTestSample()
    {
        SampleInitializer initializer = new SampleInitializer();
        initializer.setId(1054L);
        initializer.setPermId("201206191219327-1054");
        initializer.setCode("FV-TEST");
        initializer.setIdentifier("/TEST-SPACE/FV-TEST");
        initializer.setSampleTypeId(3L);
        initializer.setSampleTypeCode("CELL_PLATE");
        initializer.setRegistrationDetails(new EntityRegistrationDetails(new EntityRegistrationDetailsInitializer()));
        return new Sample(initializer);
    }

    private static Experiment createExpSpaceTestExperiment()
    {
        ExperimentInitializer initializer = new ExperimentInitializer();
        initializer.setId(23L);
        initializer.setPermId("201206190940555-1032");
        initializer.setCode("EXP-SPACE-TEST");
        initializer.setIdentifier("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");
        initializer.setExperimentTypeCode("SIRNA_HCS");
        initializer.setRegistrationDetails(new EntityRegistrationDetails(new EntityRegistrationDetailsInitializer()));
        return new Experiment(initializer);
    }

}
