/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.remoteapitest.api.v1;

import static ch.systemsx.cisd.openbis.generic.server.dataaccess.db.DAOFactory.USE_NEW_SQL_ENGINE;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.string.ToStringComparator;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationChangingService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Attachment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet.Connections;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.PropertyTypeGroup;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Role;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchSubCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SpaceWithProjectsAndRoleAssignments;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.experiment.ExperimentIdentifierId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.experiment.ExperimentPermIdId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.experiment.ExperimentTechIdId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.metaproject.MetaprojectIdentifierId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.project.ProjectIdentifierId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.project.ProjectPermIdId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.sample.SampleIdentifierId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.sample.SamplePermIdId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.sample.SampleTechIdId;
import ch.systemsx.cisd.openbis.remoteapitest.RemoteApiTestCase;

/**
 * Verifies that an instance of {@link IGeneralInformationService} is published via JSON-RPC and that it is correctly functioning with external
 * clients.
 * 
 * @author Kaloyan Enimanev
 */
@Test(groups = { "remote api" }/* , dependsOnGroups = { "before remote api" } */)
public class GeneralInformationServiceJsonApiTest extends RemoteApiTestCase
{
    protected IGeneralInformationService generalInformationService;

    protected IGeneralInformationChangingService generalInformationChangingService;

    protected String sessionToken;

    protected String userSessionToken;

    protected IGeneralInformationService createService()
    {
        return TestJsonServiceFactory.createGeneralInfoService();
    }

    protected IGeneralInformationChangingService createChangingService()
    {
        return TestJsonServiceFactory.createGeneralInfoChangingService();
    }

    @BeforeMethod
    public void beforeMethod() throws MalformedURLException
    {
        generalInformationService = createService();
        generalInformationChangingService = createChangingService();

        sessionToken = generalInformationService.tryToAuthenticateForAllServices("test", "a");
        userSessionToken =
                generalInformationService.tryToAuthenticateForAllServices("test_role", "a");
    }

    @AfterMethod
    public void afterMethod() throws MalformedURLException
    {
        generalInformationService.logout(sessionToken);
    }

    @Test
    public void testListNamedRoleSets()
    {
        Map<String, Set<Role>> namedRoleSets =
                generalInformationService.listNamedRoleSets(sessionToken);

        List<String> roles = new ArrayList<String>();
        for (Role role : namedRoleSets.get("SPACE_ADMIN"))
        {
            roles.add(role.toString());
        }
        Collections.sort(roles);
        assertEquals("[ADMIN(instance), ADMIN(space)]", roles.toString());
    }

    @Test
    public void testListSpacesWithProjectsAndRoleAssignments()
    {
        List<SpaceWithProjectsAndRoleAssignments> spaces =
                generalInformationService.listSpacesWithProjectsAndRoleAssignments(sessionToken,
                        null);

        Collections.sort(spaces, new Comparator<SpaceWithProjectsAndRoleAssignments>()
            {
                @Override
                public int compare(SpaceWithProjectsAndRoleAssignments s1,
                        SpaceWithProjectsAndRoleAssignments s2)
                {
                    return s1.getCode().compareTo(s2.getCode());
                }
            });
        checkSpace("CISD", "[/CISD/DEFAULT, /CISD/NEMO, /CISD/NOE]",
                "[ADMIN(instance), ADMIN(space), ETL_SERVER(instance), ETL_SERVER(space)]",
                spaces.get(0));
        checkSpace("TESTGROUP", "[/TESTGROUP/TESTPROJ]",
                "[ADMIN(instance), ADMIN(space), ETL_SERVER(instance)]", spaces.get(2));
        assertEquals(3, spaces.size());
    }

    @Test
    public void testListProjects()
    {
        List<Project> result = generalInformationService.listProjects(sessionToken);
        assertEquals(true, result.size() > 0);
        String expectedSampleIdentifier = "/TESTGROUP/TESTPROJ";
        for (Project project : result)
        {
            if (expectedSampleIdentifier.equals(project.toString()))
            {
                return;
            }
        }
        fail("result didn't contain project " + expectedSampleIdentifier);
    }

    @Test
    public void testSearchForExperiments()
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
    public void testSearchForSamples()
    {
        SearchCriteria sc = new SearchCriteria();
        sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "*"));
        List<Sample> result = generalInformationService.searchForSamples(sessionToken, sc);
        assertEquals(true, result.size() > 0);
        String expectedSampleIdentifier = "/CISD/CL1";
        for (Sample sample : result)
        {
            if (expectedSampleIdentifier.equals(sample.getIdentifier()))
            {
                return;
            }
        }

        fail("result didn't contain sample " + expectedSampleIdentifier);
    }

    @Test(dependsOnMethods = "testSearchForSamples")
    public void testSearchForSamplesWithNoCriterion()
    {
        SearchCriteria sc = new SearchCriteria();
        sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "*"));
        List<Sample> result = generalInformationService.searchForSamples(sessionToken, sc);

        // if no criterion was specified all samples should be returned as well
        SearchCriteria scWithNoCriterion = new SearchCriteria();
        List<Sample> resultWithNoCriterion =
                generalInformationService.searchForSamples(sessionToken, scWithNoCriterion);
        assertEquals(result, resultWithNoCriterion);
    }

    @Test
    public void testSearchForSamplesByProperty()
    {
        // Search for Samples first
        SearchCriteria sc = new SearchCriteria();
        sc.addMatchClause(MatchClause.createPropertyMatch("ORGANISM", "HUMAN"));
        List<Sample> result = generalInformationService.searchForSamples(sessionToken, sc);
        assertEquals(1, result.size());
    }

    @Test
    public void testSearchForSamplesByAnyField()
    {
        SearchCriteria sc = new SearchCriteria();
        sc.addMatchClause(MatchClause.createAnyFieldMatch("\"very advanced stuff\""));
        List<Sample> result = generalInformationService.searchForSamples(sessionToken, sc);
        assertEquals(1, result.size());
    }

    @Test
    public void testSearchForSamplesByAnyProperty()
    {
        SearchCriteria sc = new SearchCriteria();
        sc.addMatchClause(MatchClause.createAnyPropertyMatch("\"very advanced stuff\""));
        List<Sample> result = generalInformationService.searchForSamples(sessionToken, sc);
        assertEquals(1, result.size());
    }

    @Test
    public void testSearchForSamplesByExperimentCode()
    {
        // Search for Samples with only experiment's code limiting the results
        SearchCriteria sc = new SearchCriteria();
        SearchCriteria ec = new SearchCriteria();
        ec.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "EXP-TEST-1"));
        sc.addSubCriteria(SearchSubCriteria.createExperimentCriteria(ec));
        List<Sample> result = generalInformationService.searchForSamples(sessionToken, sc);
        System.out.println(result);
        assertEquals(2, result.size());
        assertEquals("/CISD/NEMO/EXP-TEST-1", result.get(0).getExperimentIdentifierOrNull());
        assertEquals("/CISD/NEMO/EXP-TEST-1", result.get(1).getExperimentIdentifierOrNull());
    }

    @Test
    public void testSearchForSamplesByExperimentProperty()
    {
        // Search for Samples with only experiment's property limiting the results
        SearchCriteria sc = new SearchCriteria();
        SearchCriteria ec = new SearchCriteria();
        ec.addMatchClause(MatchClause.createPropertyMatch("DESCRIPTION", "A simple experiment"));
        sc.addSubCriteria(SearchSubCriteria.createExperimentCriteria(ec));
        List<Sample> result = generalInformationService.searchForSamples(sessionToken, sc);
        if (USE_NEW_SQL_ENGINE)
        {
            assertEquals(2, result.size());
        } else
        {
            assertEquals(9, result.size()); // The old search engine doesn't have real equals
        }
    }

    @Test
    public void testSearchForSamplesByParentCode()
    {
        // Search for Samples with only parent's code limiting the results
        SearchCriteria sc = new SearchCriteria();
        sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "*"));
        SearchCriteria pc = new SearchCriteria();
        pc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "MP002-1"));
        sc.addSubCriteria(SearchSubCriteria.createSampleParentCriteria(pc));
        List<Sample> result = generalInformationService.searchForSamples(sessionToken, sc);
        assertEquals(2, result.size());
    }

    @Test
    public void testSearchForSamplesByChildCode()
    {
        // Search for Samples with only child's code limiting the results
        SearchCriteria sc = new SearchCriteria();
        sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "*"));
        SearchCriteria cc = new SearchCriteria();
        cc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "3VCP*"));
        sc.addSubCriteria(SearchSubCriteria.createSampleChildCriteria(cc));
        List<Sample> result = generalInformationService.searchForSamples(sessionToken, sc);
        assertEquals(2, result.size());
    }

    @Test
    public void testSearchForSamplesByContainerCode()
    {
        // Search for Samples with only container's code limiting the results
        SearchCriteria sc = new SearchCriteria();
        SearchCriteria cc = new SearchCriteria();
        cc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "CL1"));
        sc.addSubCriteria(SearchSubCriteria.createSampleContainerCriteria(cc));
        List<Sample> result = generalInformationService.searchForSamples(sessionToken, sc);
        assertEquals(2, result.size());
        for (Sample s : result)
        {
            assertTrue(s.getCode() + "doesn't start with 'CL1:'", s.getCode().startsWith("CL1:"));
        }
    }

    @Test
    public void testSearchForSamplesByExperimentAndParentCode()
    {
        // Search for Samples first
        SearchCriteria sc = new SearchCriteria();
        List<Sample> result = generalInformationService.searchForSamples(sessionToken, sc);
        assertEquals(true, result.size() > 600);
        // Add experiment criteria limiting results to 7
        SearchCriteria ec = new SearchCriteria();
        ec.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "EXP-REUSE"));
        sc.addSubCriteria(SearchSubCriteria.createExperimentCriteria(ec));
        List<Sample> result2 = generalInformationService.searchForSamples(sessionToken, sc);
        assertEquals(7, result2.size());
        // Add parent criteria limiting results to only 2
        SearchCriteria pc = new SearchCriteria();
        pc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "DP1-A"));
        sc.addSubCriteria(SearchSubCriteria.createSampleParentCriteria(pc));
        List<Sample> result3 = generalInformationService.searchForSamples(sessionToken, sc);
        assertEquals(2, result3.size());
    }

    @Test
    public void testListDataSetsForAllSamples()
    {
        // Search for Samples first
        SearchCriteria sc = new SearchCriteria();
        List<Sample> samples = generalInformationService.searchForSamples(sessionToken, sc);
        List<DataSet> result = generalInformationService.listDataSets(sessionToken, samples);
        assertEquals(true, result.size() > 0);
    }

    @Test
    public void testListDataSetsWithParentsForAllSamples()
    {
        // Search for Samples first
        SearchCriteria sc = new SearchCriteria();
        List<Sample> samples = generalInformationService.searchForSamples(sessionToken, sc);
        List<DataSet> result =
                generalInformationService.listDataSets(sessionToken, samples,
                        EnumSet.of(Connections.PARENTS));
        assertTrue(result.size() > 0);
        for (DataSet dataSet : result)
        {
            assertEquals(
                    "No parent codes should have been found for data set " + dataSet.getCode(),
                    "[]", dataSet.getParentCodes().toString());
        }
    }

    @Test
    public void testListDataSetsWithParentsForExperiment()
    {
        List<String> experimentIdentifiers = Arrays.asList("/CISD/NEMO/EXP1");
        List<Experiment> experiments =
                generalInformationService.listExperiments(sessionToken, experimentIdentifiers);
        List<DataSet> result =
                generalInformationService.listDataSetsForExperiments(sessionToken, experiments,
                        EnumSet.of(Connections.PARENTS));
        assertTrue(result.size() > 0);
        for (DataSet dataSet : result)
        {
            assertEquals(
                    "No parent codes should have been found for data set " + dataSet.getCode(),
                    "[]", dataSet.getParentCodes().toString());
        }

    }

    @Test
    public void testListDataSetsForSample()
    {
        // Search for Samples first
        SearchCriteria sc = new SearchCriteria();
        sc.addMatchClause(MatchClause.createPropertyMatch("ORGANISM", "HUMAN"));
        List<Sample> samples = generalInformationService.searchForSamples(sessionToken, sc);
        List<DataSet> result =
                generalInformationService.listDataSetsForSample(sessionToken, samples.get(0), true);
        assertEquals(true, result.size() > 0);
    }

    @Test
    public void testListDataSetsForSampleForRegularUserDoesntIncludeStorageNonConfirmed()
    {
        // Search for Samples first
        SearchCriteria sc = new SearchCriteria();
        sc.addMatchClause(MatchClause.createPropertyMatch("ORGANISM", "HUMAN"));
        List<Sample> samples = generalInformationService.searchForSamples(userSessionToken, sc);
        List<DataSet> result =
                generalInformationService.listDataSetsForSample(userSessionToken, samples.get(0),
                        true);
        assertEquals(0, result.size());
    }

    @Test
    public void testListDataSetsForEmptySampleList()
    {
        List<Sample> samples = new ArrayList<Sample>();
        List<DataSet> result = generalInformationService.listDataSets(sessionToken, samples);
        assertEquals(true, result.size() == 0);
    }

    private static final String DEFAULT_PLATE_GEOMETRY_VALUE = "384_WELLS_16X24";

    @Test
    public void testSearchForDataSetsByProperty()
    {
        // Search for Samples first
        SearchCriteria sc = new SearchCriteria();
        sc.addMatchClause(MatchClause.createPropertyMatch("$PLATE_GEOMETRY",
                DEFAULT_PLATE_GEOMETRY_VALUE));
        List<Sample> samples = generalInformationService.searchForSamples(sessionToken, sc);
        assertEquals(true, samples.size() > 0);
        List<DataSet> result = generalInformationService.listDataSets(sessionToken, samples);
        assertEquals("20120628092259000-23", result.get(0).getCode());
        assertEquals(1, result.size());
    }

    private void checkSpace(String expectedCode, String expectedProjects, String expectedRoles,
            SpaceWithProjectsAndRoleAssignments space)
    {
        assertEquals(expectedCode, space.getCode());
        List<Project> projects = space.getProjects();
        Collections.sort(projects, new Comparator<Project>()
            {
                @Override
                public int compare(Project p1, Project p2)
                {
                    return p1.getCode().compareTo(p2.getCode());
                }
            });
        assertEquals(expectedProjects, projects.toString());
        List<Role> roles = new ArrayList<Role>(space.getRoles("test"));
        Collections.sort(roles, new Comparator<Role>()
            {
                @Override
                public int compare(Role r1, Role r2)
                {
                    return r1.toString().compareTo(r2.toString());
                }
            });
        assertEquals(expectedRoles, roles.toString());
    }

    @Test
    public void testListExperiments()
    {
        List<SpaceWithProjectsAndRoleAssignments> spaces =
                generalInformationService.listSpacesWithProjectsAndRoleAssignments(sessionToken,
                        null);
        ArrayList<Project> projects = new ArrayList<Project>();
        for (SpaceWithProjectsAndRoleAssignments space : spaces)
        {
            projects.addAll(space.getProjects());
        }
        List<Experiment> result =
                generalInformationService.listExperiments(sessionToken, projects, "SIRNA_HCS");
        assertEquals(true, result.size() > 0);
        Experiment resultExperiment = result.get(0);
        boolean identifierIsOk = "/CISD/DEFAULT/EXP-REUSE".equals(resultExperiment.getIdentifier());
        identifierIsOk |= "/CISD/NEMO/EXP-TEST-2".equals(resultExperiment.getIdentifier());
        identifierIsOk |=
                "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST".equals(resultExperiment.getIdentifier());
        assertEquals("Experiment should be: " + resultExperiment.getIdentifier(), true,
                identifierIsOk);
    }

    @Test
    public void testListExperimentsByIdentifier()
    {
        List<SpaceWithProjectsAndRoleAssignments> spaces =
                generalInformationService.listSpacesWithProjectsAndRoleAssignments(sessionToken,
                        null);
        ArrayList<Project> projects = new ArrayList<Project>();
        for (SpaceWithProjectsAndRoleAssignments space : spaces)
        {
            projects.addAll(space.getProjects());
        }
        List<Experiment> experimentsByProject =
                generalInformationService.listExperiments(sessionToken, projects, "SIRNA_HCS");
        assertEquals(true, experimentsByProject.size() > 0);

        List<String> experimentIdentifiers = new ArrayList<String>();
        for (Experiment exp : experimentsByProject)
        {
            experimentIdentifiers.add(exp.getIdentifier());
        }

        List<Experiment> results =
                generalInformationService.listExperiments(sessionToken, experimentIdentifiers);
        Comparator<Experiment> experimentCompare = new Comparator<Experiment>()
            {

                @Override
                public int compare(Experiment o1, Experiment o2)
                {
                    return o1.getIdentifier().compareTo(o2.getIdentifier());
                }
            };
        Collections.sort(experimentsByProject, experimentCompare);
        Collections.sort(results, experimentCompare);
        assertEquals(experimentsByProject, results);
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
        List<DataSetType> dataSetTypes = generalInformationService.listDataSetTypes(sessionToken);
        assertEquals(11, dataSetTypes.size());

        Collections.sort(dataSetTypes, new ToStringComparator());
        DataSetType dataSetType = dataSetTypes.get(3);
        assertEquals("HCS_IMAGE", dataSetType.getCode());

        List<PropertyTypeGroup> propertyTypeGroups = dataSetType.getPropertyTypeGroups();
        assertEquals(1, propertyTypeGroups.size());

        PropertyTypeGroup propertyTypeGroup = propertyTypeGroups.get(0);
        assertEquals(null, propertyTypeGroup.getName());

        List<PropertyType> propertyTypes = propertyTypeGroup.getPropertyTypes();
        assertEquals(4, propertyTypes.size());

        PropertyType propertyType;
        propertyType = propertyTypes.get(0);
        assertEquals("COMMENT", propertyType.getCode());
        assertEquals("Comment", propertyType.getLabel());
        assertEquals("Any other comments", propertyType.getDescription());

        propertyType = propertyTypes.get(1);
        assertEquals("ANY_MATERIAL", propertyType.getCode());
        assertEquals("any_material", propertyType.getLabel());
        assertEquals("any_material", propertyType.getDescription());
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
                + "{PropertyType[INTEGER,OFFSET,Offset,Offset from the start of the sequence,"
                + "optional]}]}]",
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
    public void testSearchForDataSetsByCode()
    {
        SearchCriteria sc = new SearchCriteria();
        sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "*"));
        List<DataSet> result = generalInformationService.searchForDataSets(userSessionToken, sc);
        assertTrue(result.size() > 0);
        String expectedDataSetCode = "20081105092159188-3";

        for (DataSet dataSet : result)
        {
            System.out.println(dataSet.getRegistrationDetails().toString());
        }
        for (DataSet dataSet : result)
        {
            if (expectedDataSetCode.equals(dataSet.getCode()))
            {
                return;
            }
        }
        fail("result didn't contain data set" + expectedDataSetCode);
    }

    @Test
    public void testSearchForContainerDataSetByCode()
    {
        SearchCriteria sc = new SearchCriteria();
        sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE,
                "20110509092359990-10"));
        List<DataSet> result = generalInformationService.searchForDataSets(userSessionToken, sc);
        assertTrue(result.size() > 0);
        String expectedDataSetCode = "20110509092359990-10";

        for (DataSet dataSet : result)
        {
            System.out.println(dataSet.getRegistrationDetails().toString());
        }
        for (DataSet dataSet : result)
        {
            if (expectedDataSetCode.equals(dataSet.getCode()))
            {
                return;
            }
        }
        fail("result didn't contain data set" + expectedDataSetCode);
    }

    @Test
    public void testSearchForDataSetsByExperiments()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        SearchCriteria expCriteria = new SearchCriteria();
        expCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE,
                "EXP-TEST-1"));
        searchCriteria.addSubCriteria(SearchSubCriteria.createExperimentCriteria(expCriteria));
        List<DataSet> result =
                generalInformationService.searchForDataSets(sessionToken, searchCriteria);
        assertEquals(1, result.size());

        DataSet dataSet = result.get(0);
        assertEquals("20081105092159111-1", dataSet.getCode());
        assertEquals("/CISD/NEMO/EXP-TEST-1", dataSet.getExperimentIdentifier());

    }

    @Test
    public void testSearchForDataSetsBySamples()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        SearchCriteria sampleCriteria = new SearchCriteria();
        sampleCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE,
                "CP-TEST-1"));
        searchCriteria.addSubCriteria(SearchSubCriteria.createSampleCriteria(sampleCriteria));
        List<DataSet> result =
                generalInformationService.searchForDataSets(sessionToken, searchCriteria);
        assertEquals(1, result.size());

        DataSet dataSet = result.get(0);
        assertEquals("20081105092159111-1", dataSet.getCode());
        assertEquals("/CISD/NEMO/EXP-TEST-1", dataSet.getExperimentIdentifier());
        assertEquals("/CISD/NEMO/CP-TEST-1", dataSet.getSampleIdentifierOrNull());
    }

    @Test
    public void testSearchForDataSetsByParent()
    {
        SearchCriteria searchCriteria = new SearchCriteria();

        SearchCriteria parentCriteria = new SearchCriteria();
        parentCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE,
                "20081105092159333-3"));
        searchCriteria
                .addSubCriteria(SearchSubCriteria.createDataSetParentCriteria(parentCriteria));

        List<DataSet> result =
                generalInformationService.searchForDataSets(userSessionToken, searchCriteria);

        assertEquals(2, result.size());
        assertEquals(
                "[DataSet[20081105092259000-8,/CISD/DEFAULT/EXP-REUSE,<null>,HCS_IMAGE,{COMMENT=no comment}], "
                        + "DataSet[20081105092259000-9,/CISD/DEFAULT/EXP-REUSE,<null>,HCS_IMAGE,{COMMENT=no comment}]]",
                result.toString());
    }

    @Test
    public void testSearchForDataSetsByChild()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        SearchCriteria childCriteria = new SearchCriteria();
        childCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE,
                "20081105092259000-9"));
        searchCriteria.addSubCriteria(SearchSubCriteria.createDataSetChildCriteria(childCriteria));

        List<DataSet> result =
                generalInformationService.searchForDataSets(sessionToken, searchCriteria);
        assertEquals(3, result.size());
        assertEquals(
                "[DataSet[20081105092159111-1,/CISD/NEMO/EXP-TEST-1,/CISD/NEMO/CP-TEST-1,HCS_IMAGE,{ANY_MATERIAL=1000_C (SIRNA), BACTERIUM=BACTERIUM1 (BACTERIUM), COMMENT=no comment, GENDER=FEMALE}], "
                        + "DataSet[20081105092159222-2,/CISD/NOE/EXP-TEST-2,/CISD/NOE/CP-TEST-2,HCS_IMAGE,{COMMENT=no comment}], "
                        + "DataSet[20081105092159333-3,/CISD/NEMO/EXP-TEST-2,/CISD/NEMO/CP-TEST-3,HCS_IMAGE,{COMMENT=no comment}]]",
                result.toString());
    }

    @Test
    public void testSearchForDataSetsByParentAndChild()
    {
        SearchCriteria searchCriteria = new SearchCriteria();

        SearchCriteria parentCriteria = new SearchCriteria();
        parentCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE,
                "20081105092159333-3"));
        searchCriteria
                .addSubCriteria(SearchSubCriteria.createDataSetParentCriteria(parentCriteria));

        SearchCriteria childCriteria = new SearchCriteria();
        childCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE,
                "20081105092259900-1"));
        searchCriteria.addSubCriteria(SearchSubCriteria.createDataSetChildCriteria(childCriteria));

        List<DataSet> result =
                generalInformationService.searchForDataSets(sessionToken, searchCriteria);
        assertEquals(1, result.size());
        assertEquals(
                "[DataSet[20081105092259000-9,/CISD/DEFAULT/EXP-REUSE,<null>,HCS_IMAGE,{COMMENT=no comment}]]",
                result.toString());
    }

    @Test
    public void testSearchForDataSetsByContainer()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        SearchCriteria containerCriteria = new SearchCriteria();

        containerCriteria.addMatchClause(MatchClause.createAttributeMatch(
                MatchClauseAttribute.CODE, "20110509092359990-10"));
        searchCriteria.addSubCriteria(SearchSubCriteria
                .createDataSetContainerCriteria(containerCriteria));

        List<DataSet> result =
                generalInformationService.searchForDataSets(sessionToken, searchCriteria);
        assertEquals(2, result.size());
        assertEquals(
                "[DataSet[20110509092359990-11,/CISD/DEFAULT/EXP-REUSE,<null>,HCS_IMAGE,{COMMENT=non-virtual comment}], "
                        + "DataSet[20110509092359990-12,/CISD/DEFAULT/EXP-REUSE,<null>,HCS_IMAGE,{COMMENT=non-virtual comment}]]",
                result.toString());
    }

    @Test
    public void testSearchForDataSetsLinked()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE,
                "LINK_TYPE"));

        List<DataSet> result =
                generalInformationService.searchForDataSets(sessionToken, searchCriteria);

        Collections.sort(result, new Comparator<DataSet>()
            {
                @Override
                public int compare(DataSet o1, DataSet o2)
                {
                    return o1.getCode().compareTo(o2.getCode());
                }
            });

        assertEquals(4, result.size());
        assertEquals("CODE1", result.get(0).getExternalDataSetCode());
        assertEquals("CODE2", result.get(1).getExternalDataSetCode());
        assertEquals("CODE3", result.get(2).getExternalDataSetCode());
        assertEquals("http://example.edms.pl/code=CODE1", result.get(0).getExternalDataSetLink());
        assertEquals("http://example.edms.pl/code=CODE2", result.get(1).getExternalDataSetLink());
        assertEquals("http://www.openbis.ch/perm_id=CODE3", result.get(2).getExternalDataSetLink());
        assertEquals("DMS_1", result.get(0).getExternalDataManagementSystem().getCode());
        assertEquals("DMS_1", result.get(1).getExternalDataManagementSystem().getCode());
        assertEquals("DMS_2", result.get(2).getExternalDataManagementSystem().getCode());
    }

    @Test
    public void testListVocabularyTerms()
    {
        List<Vocabulary> vocabularies = generalInformationService.listVocabularies(sessionToken);

        final Vocabulary gender = findVocabulary(vocabularies, "GENDER");
        assertEquals("Vocabulary[GENDER,{VocabularyTerm[MALE,MALE],VocabularyTerm[FEMALE,FEMALE]}]",
                gender.toString());
        final Vocabulary human = findVocabulary(vocabularies, "HUMAN");
        assertEquals("Vocabulary[HUMAN,{VocabularyTerm[MAN,MAN],VocabularyTerm[WOMAN,WOMAN],"
                + "VocabularyTerm[CHILD,CHILD]}]",
                human.toString());

        Vocabulary organism = findVocabulary(vocabularies, "ORGANISM");
        assertNotNull(organism);
        assertEquals("VocabularyTerm[RAT,RAT]", organism.getTerms().get(0).toString());
    }

    @Test
    public void testDataSetVocabularyProperties()
    {
        List<DataSet> dataSets =
                generalInformationService.getDataSetMetaData(sessionToken,
                        Collections.singletonList("20081105092159111-1"));

        assertEquals(1, dataSets.size());
        assertEquals("FEMALE", dataSets.get(0).getProperties().get("GENDER"));

    }

    @Test
    public void testMetaprojects()
    {
        // as the metaprojects functionality is already tested in detail by the system tests here
        // we just want to check that metaproject related classes serialize/deserialize to/from JSON
        // properly
        generalInformationService.listMetaprojects(sessionToken);
        generalInformationService.getMetaproject(sessionToken, new MetaprojectIdentifierId(
                "/test/TEST_METAPROJECTS"));
    }

    private Vocabulary findVocabulary(List<Vocabulary> vocabularies, String vocabularyCode)
    {
        for (Vocabulary vocabulary : vocabularies)
        {
            if (vocabulary.getCode().equals(vocabularyCode))
            {
                return vocabulary;
            }
        }
        return null;
    }

    @Test
    public void testListAttachmentsForExperimentAllVersions()
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
        for (Attachment a : attachments)
        {
            assertEquals("exampleExperiments.txt", a.getFileName());
            assertEquals(version, a.getVersion());
            assertEquals(version == 4 ? "Latest version" : "", a.getTitle());
            assertEquals(version == 3 ? "Second latest version" : "", a.getDescription());
            assertTrue(a.getRegistrationDetails().getRegistrationDate().getTime() > 0);
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
    public void testListAttachmentsForSample()
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
        assertTrue(a.getRegistrationDetails().getRegistrationDate().getTime() > 0);
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
    public void testListAttachmentsForProjects() throws ParseException
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
    }

}
