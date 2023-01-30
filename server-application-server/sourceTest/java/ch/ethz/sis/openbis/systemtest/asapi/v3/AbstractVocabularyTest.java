/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.systemtest.asapi.v3;

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.testng.annotations.DataProvider;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.Material;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.IMaterialId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.MaterialPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.VocabularyTerm;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyTermFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.IVocabularyId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.IVocabularyTermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.search.VocabularyTermSearchCriteria;

/**
 * @author pkupczyk
 */
public class AbstractVocabularyTest extends AbstractTest
{

    public static final String PROVIDE_USERS_NOT_ALLOWED_TO_MANAGE_VOCABULARIES = "usersNotAllowedToManageVocabularies";

    public static final String PROVIDE_USERS_NOT_ALLOWED_TO_MANAGE_INTERNAL_VOCABULARIES = "usersNotAllowedToManageInternalVocabularies";

    @DataProvider(name = PROVIDE_USERS_NOT_ALLOWED_TO_MANAGE_VOCABULARIES)
    Object[][] usersNotAllowedToManageVocabularies()
    {
        return createTestUsersProvider(TEST_GROUP_ADMIN, TEST_GROUP_OBSERVER, TEST_GROUP_POWERUSER,
                TEST_INSTANCE_OBSERVER, TEST_POWER_USER_CISD, TEST_SPACE_USER);
    }

    @DataProvider(name = PROVIDE_USERS_NOT_ALLOWED_TO_MANAGE_INTERNAL_VOCABULARIES)
    Object[][] usersNotAllowedToManageInternalVocabularies()
    {
        return createTestUsersProvider(TEST_USER, TEST_GROUP_ADMIN, TEST_GROUP_OBSERVER, TEST_GROUP_POWERUSER,
                TEST_INSTANCE_OBSERVER, TEST_POWER_USER_CISD, TEST_SPACE_USER);
    }

    protected List<VocabularyTerm> listTerms(IVocabularyId vocabularyId)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        VocabularyTermSearchCriteria criteria = new VocabularyTermSearchCriteria();
        criteria.withVocabulary().withCode().thatEquals(((VocabularyPermId) vocabularyId).getPermId());

        VocabularyTermFetchOptions fetchOptions = new VocabularyTermFetchOptions();
        fetchOptions.sortBy().ordinal().asc();

        SearchResult<VocabularyTerm> results = v3api.searchVocabularyTerms(sessionToken, criteria, fetchOptions);

        v3api.logout(sessionToken);

        return results.getObjects();
    }

    protected void assertTerms(List<VocabularyTerm> actualTerms, String... expectedCodes)
    {
        List<String> actualCodes = new ArrayList<String>();

        for (VocabularyTerm actualTerm : actualTerms)
        {
            actualCodes.add(actualTerm.getCode());
        }

        assertEquals(actualCodes, Arrays.asList(expectedCodes),
                "Actual codes: " + actualCodes + ", Expected codes: " + Arrays.asList(expectedCodes));
    }

    protected VocabularyTerm searchTerm(IVocabularyTermId id, VocabularyTermFetchOptions fetchOptions)
    {
        return searchTerms(Arrays.asList(id), fetchOptions).get(0);
    }

    protected List<VocabularyTerm> searchTerms(List<IVocabularyTermId> ids, VocabularyTermFetchOptions fetchOptions)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        VocabularyTermSearchCriteria criteria = new VocabularyTermSearchCriteria();
        criteria.withOrOperator();
        for (IVocabularyTermId id : ids)
        {
            criteria.withId().thatEquals(id);
        }

        SearchResult<VocabularyTerm> searchResult =
                v3api.searchVocabularyTerms(sessionToken, criteria, fetchOptions);

        assertEquals(searchResult.getObjects().size(), ids.size());

        v3api.logout(sessionToken);

        return searchResult.getObjects();
    }

    protected List<VocabularyTerm> searchTerms(VocabularyTermSearchCriteria criteria, VocabularyTermFetchOptions fetchOptions)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SearchResult<VocabularyTerm> searchResult = v3api.searchVocabularyTerms(sessionToken, criteria, fetchOptions);

        v3api.logout(sessionToken);

        return searchResult.getObjects();
    }

    protected List<VocabularyTerm> searchTerms(String vocabularyCode)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        VocabularyTermSearchCriteria criteria = new VocabularyTermSearchCriteria();
        criteria.withVocabulary().withCode().thatEquals(vocabularyCode);

        VocabularyTermFetchOptions fetchOptions = new VocabularyTermFetchOptions();
        fetchOptions.sortBy().ordinal().asc();

        SearchResult<VocabularyTerm> result = v3api.searchVocabularyTerms(sessionToken, criteria, fetchOptions);
        v3api.logout(sessionToken);
        return result.getObjects();
    }

    protected Experiment getExperiment(String permId)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentPermId id = new ExperimentPermId(permId);

        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withProperties();

        Map<IExperimentId, Experiment> map = v3api.getExperiments(sessionToken, Arrays.asList(id), fetchOptions);
        assertEquals(map.size(), 1);

        v3api.logout(sessionToken);

        return map.get(id);
    }

    protected Sample getSample(String permId)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SamplePermId id = new SamplePermId(permId);

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProperties();

        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, Arrays.asList(id), fetchOptions);
        assertEquals(map.size(), 1);

        v3api.logout(sessionToken);

        return map.get(id);
    }

    protected DataSet getDataSet(String permId)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId id = new DataSetPermId(permId);

        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withProperties();

        Map<IDataSetId, DataSet> map = v3api.getDataSets(sessionToken, Arrays.asList(id), fetchOptions);
        assertEquals(map.size(), 1);

        return map.get(id);
    }

    protected Material getMaterial(String materialCode, String materialTypeCode)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        MaterialPermId id = new MaterialPermId(materialCode, materialTypeCode);

        MaterialFetchOptions fetchOptions = new MaterialFetchOptions();
        fetchOptions.withProperties();

        Map<IMaterialId, Material> map = v3api.getMaterials(sessionToken, Arrays.asList(id), fetchOptions);
        assertEquals(map.size(), 1);

        return map.get(id);
    }

}
