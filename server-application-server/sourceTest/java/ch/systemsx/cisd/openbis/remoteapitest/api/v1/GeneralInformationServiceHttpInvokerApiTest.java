/*
 * Copyright 2010 ETH Zuerich, CISD
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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.common.api.client.ServiceFinder;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.ControlledVocabularyPropertyType.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityRegistrationDetails;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.util.TestInstanceHostUtils;

/**
 * Verifies that an instance of {@link IGeneralInformationService} is published via {@link HttpInvokerServiceExporter} and that it is correctly
 * functioning with external clients.
 * 
 * @author Kaloyan Enimanev
 */
@Test(groups = { "remote api" }, dependsOnGroups = { "before remote api" })
public class GeneralInformationServiceHttpInvokerApiTest extends
        GeneralInformationServiceJsonApiTest
{
    @Override
    protected IGeneralInformationService createService()
    {
        ServiceFinder generalInformationServiceFinder =
                new ServiceFinder("openbis", IGeneralInformationService.SERVICE_URL);
        return generalInformationServiceFinder.createService(IGeneralInformationService.class,
                TestInstanceHostUtils.getOpenBISUrl(), 10000000);
    }

    @Test
    public void testRegistrationDetailsAvailable()
    {
        // project
        List<Project> projects = generalInformationService.listProjects(sessionToken);
        assertTrue(projects.size() > 0);
        checkRegistrationDetails(projects.get(0).getRegistrationDetails());

        // experiment
        List<Experiment> experiments =
                generalInformationService.listExperiments(sessionToken, projects, "SIRNA_HCS");
        assertTrue(experiments.size() > 0);
        checkRegistrationDetails(experiments.get(0).getRegistrationDetails());

        // sample
        SearchCriteria sc = new SearchCriteria();
        sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "CP-TEST-1"));
        List<Sample> samples = generalInformationService.searchForSamples(sessionToken, sc);
        assertTrue(samples.size() > 0);
        Sample sample = samples.get(0);
        checkRegistrationDetails(sample.getRegistrationDetails());

        // data set
        List<DataSet> dataSets =
                generalInformationService.listDataSetsForSample(sessionToken, sample, false);
        assertTrue(dataSets.size() > 0);
        assertNotNull(dataSets.get(0).getRegistrationDetails().getRegistrationDate());

        // vocabularies
        @SuppressWarnings("deprecation")
        Map<Vocabulary, List<VocabularyTerm>> termMap =
                generalInformationService.getVocabularyTermsMap(sessionToken);
        ArrayList<Vocabulary> vocabs = new ArrayList<Vocabulary>(termMap.keySet().size());
        vocabs.addAll(termMap.keySet());
        List<VocabularyTerm> terms = termMap.get(vocabs.get(0));
        assertTrue(terms.size() > 0);
        assertNotNull(terms.get(0).getRegistrationDetails().getUserId());
        assertNotNull(terms.get(0).getRegistrationDetails().getRegistrationDate());
    }

    private void checkRegistrationDetails(EntityRegistrationDetails registrationDetails)
    {
        assertNotNull(registrationDetails);
        assertEquals("test", registrationDetails.getUserId());
        assertEquals("franz-josef.elmer@systemsx.ch", registrationDetails.getUserEmail());
        assertNotNull(registrationDetails.getRegistrationDate());

    }

}
