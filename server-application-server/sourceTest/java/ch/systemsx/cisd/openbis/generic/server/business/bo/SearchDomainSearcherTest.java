/*
 * Copyright 2014 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.business.IDataStoreServiceFactory;
import ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool;
import ch.systemsx.cisd.openbis.generic.shared.IDataStoreService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.AlignmentMatch;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.BlastScore;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetFileSearchResultLocation;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityPropertyBlastSearchResultLocation;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.ISearchDomainResultScore;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchDomain;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchDomainSearchResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.IPermIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SearchDomainSearchResultWithFullEntity;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.FileFormatTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author Franz-Josef Elmer
 */
public class SearchDomainSearcherTest extends AbstractBOTest
{
    private static final Map<String, String> OPTIONAL_PARAMETERS = Collections.singletonMap("greeting", "hi");

    private static final String SEQUENCE_SNIPPET = "GATTACA";

    private static final String SEQUENCE_DATABASE = "MY_DB";

    private IDataStoreServiceFactory dssFactory;

    private IDataStoreService dataStoreService1;

    private IDataStoreService dataStoreService2;

    private DataStorePE store1;

    private DataStorePE store2;

    private DataStorePE store3;

    @BeforeMethod
    @Override
    public void beforeMethod()
    {
        super.beforeMethod();
        dssFactory = context.mock(IDataStoreServiceFactory.class);
        dataStoreService1 = context.mock(IDataStoreService.class, "dataStoreService1");
        dataStoreService2 = context.mock(IDataStoreService.class, "dataStoreService2");
        store1 = new DataStorePE();
        store1.setRemoteUrl("http://abc1.de");
        store1.setSessionToken("session-1");
        store2 = new DataStorePE();
        store2.setRemoteUrl("http://abc2.de");
        store2.setSessionToken("session-2");
        store3 = new DataStorePE();
        prepareListDataStores(store1, store2, store3);
        prepareCreateService(dataStoreService1, store1);
        prepareCreateService(dataStoreService2, store2);
    }

    @Test
    public void testListAvailableSearchDomains()
    {
        final SearchDomain searchDomain1 = new SearchDomain();
        final SearchDomain searchDomain2 = new SearchDomain();
        final SearchDomain searchDomain3 = new SearchDomain();
        context.checking(new Expectations()
            {
                {
                    one(dataStoreService1).listAvailableSearchDomains(store1.getSessionToken());
                    will(returnValue(Arrays.asList(searchDomain1)));

                    one(dataStoreService2).listAvailableSearchDomains(store2.getSessionToken());
                    will(returnValue(Arrays.asList(searchDomain2, searchDomain3)));
                }
            });

        List<SearchDomain> searchDomains = createSearcher().listAvailableSearchDomains();

        assertSame(searchDomain1, searchDomains.get(0));
        assertSame(searchDomain2, searchDomains.get(1));
        assertSame(searchDomain3, searchDomains.get(2));
        assertEquals(3, searchDomains.size());
        context.assertIsSatisfied();

    }

    @Test
    public void testSearchForEntitiesWithSequences()
    {
        prepareSearchForEntityPropertiesWithSequences(store1, dataStoreService1, 0.5, "DATA_SET:DS1:SEQ");
        prepareSearchForEntityPropertiesWithSequences(store2, dataStoreService2, 9.5,
                "SAMPLE:S1:OLIGO", "SAMPLE:S2:OLIGO", "EXPERIMENT:E1:S");
        ExternalDataPE ds1 = createDataSet("DS1", store1);
        prepareListDataSetsByCode(ds1);
        SamplePE s1 = createSample("S1");
        SamplePE s2 = createSample("S2");
        prepareListSamplesByPermId(s2, s1);
        ExperimentPE e1 = createExperiment("E1");
        prepareListExperimentsByPermId(e1);

        List<SearchDomainSearchResultWithFullEntity> results =
                createSearcher().searchForEntitiesWithSequences(SEQUENCE_DATABASE, SEQUENCE_SNIPPET, OPTIONAL_PARAMETERS);

        assertEquals("E1", results.get(0).getEntity().getPermId());
        assertEquals("Search Domain: test-db, Score: [Score: 11.5, bit score: 5.75, evalue: 12.5], Result location: "
                + "[Experiment type: UNKNOWN-E, perm id: E1, code: CODE-E1, property type: S, alignment in sequence: [42-45], "
                + "alignment in query: [7-10], number of mismatches: 0, total number of gaps: 0]",
                results.get(0).getSearchResult().toString());
        assertEquals("S2", results.get(1).getEntity().getPermId());
        assertEquals("Search Domain: test-db, Score: [Score: 10.5, bit score: 5.25, evalue: 11.5], Result location: "
                + "[Sample type: TYPE, perm id: S2, code: CODE-S2, property type: OLIGO, alignment in sequence: [42-45], "
                + "alignment in query: [7-10], number of mismatches: 0, total number of gaps: 0]",
                results.get(1).getSearchResult().toString());
        assertEquals("S1", results.get(2).getEntity().getPermId());
        assertEquals("Search Domain: test-db, Score: [Score: 9.5, bit score: 4.75, evalue: 10.5], Result location: "
                + "[Sample type: TYPE, perm id: S1, code: CODE-S1, property type: OLIGO, alignment in sequence: [42-45], "
                + "alignment in query: [7-10], number of mismatches: 0, total number of gaps: 0]",
                results.get(2).getSearchResult().toString());
        assertEquals("DS1", results.get(3).getEntity().getPermId());
        assertEquals("Search Domain: test-db, Score: [Score: 0.5, bit score: 0.25, evalue: 1.5], Result location: "
                + "[Data set type: UNKNOWN, perm id: DS1, code: DS1, property type: SEQ, alignment in sequence: [42-45], "
                + "alignment in query: [7-10], number of mismatches: 0, total number of gaps: 0]",
                results.get(3).getSearchResult().toString());
        assertEquals(4, results.size());
        context.assertIsSatisfied();
    }

    @Test
    public void testSearchForDataSetsWithSequences()
    {
        prepareSearchForDataSetsWithSequences(store1, dataStoreService1, 0.5, "ds1", "ds2");
        prepareSearchForDataSetsWithSequences(store2, dataStoreService2, 13.5, "ds3", "ds3");
        final ExternalDataPE ds1 = createDataSet("ds1", store1);
        final ExternalDataPE ds2 = createDataSet("ds2", store1);
        final ExternalDataPE ds3 = createDataSet("ds3", store2);
        prepareListDataSetsByCode(ds1, ds2, ds3);

        List<SearchDomainSearchResultWithFullEntity> results =
                createSearcher().searchForEntitiesWithSequences(SEQUENCE_DATABASE, SEQUENCE_SNIPPET, OPTIONAL_PARAMETERS);

        assertEquals("Search Domain: test-db, Score: [Score: 14.5, bit score: 7.25, evalue: 15.5], "
                + "Result location: [Data set type: UNKNOWN, code: ds3, path: ds3/path, identifier: [id-ds3], position: 42]",
                results.get(0).getSearchResult().toString());
        assertEquals(ds3.getCode(), ((AbstractExternalData) results.get(0).getEntity()).getCode());
        assertEquals("/G1/P1/exp1", ((AbstractExternalData) results.get(0).getEntity()).getExperiment().getIdentifier());
        assertEquals("Search Domain: test-db, Score: [Score: 13.5, bit score: 6.75, evalue: 14.5], "
                + "Result location: [Data set type: UNKNOWN, code: ds3, path: ds3/path, "
                + "identifier: [id-ds3], position: 42]",
                results.get(1).getSearchResult().toString());
        assertEquals(ds3.getCode(), ((AbstractExternalData) results.get(1).getEntity()).getCode());
        assertEquals("/G1/P1/exp1", ((AbstractExternalData) results.get(1).getEntity()).getExperiment().getIdentifier());
        assertEquals("Search Domain: test-db, Score: [Score: 1.5, bit score: 0.75, evalue: 2.5], "
                + "Result location: [Data set type: UNKNOWN, code: ds2, path: ds2/path, "
                + "identifier: [id-ds2], position: 42]",
                results.get(2).getSearchResult().toString());
        assertEquals(ds2.getCode(), ((AbstractExternalData) results.get(2).getEntity()).getCode());
        assertEquals("/G1/P1/exp1", ((AbstractExternalData) results.get(2).getEntity()).getExperiment().getIdentifier());
        assertEquals("Search Domain: test-db, Score: [Score: 0.5, bit score: 0.25, evalue: 1.5], "
                + "Result location: [Data set type: UNKNOWN, code: ds1, path: ds1/path, "
                + "identifier: [id-ds1], position: 42]",
                results.get(3).getSearchResult().toString());
        assertEquals(ds1.getCode(), ((AbstractExternalData) results.get(3).getEntity()).getCode());
        assertEquals("/G1/P1/exp1", ((AbstractExternalData) results.get(3).getEntity()).getExperiment().getIdentifier());
        assertEquals(4, results.size());
        context.assertIsSatisfied();
    }

    private void prepareListDataStores(final DataStorePE... dataStores)
    {
        context.checking(new Expectations()
            {
                {
                    one(dataStoreDAO).listDataStores();
                    will(returnValue(Arrays.asList(dataStores)));
                }
            });
    }

    private void prepareCreateService(final IDataStoreService service, final DataStorePE... dataStores)
    {
        context.checking(new Expectations()
            {
                {
                    for (DataStorePE dataStore : dataStores)
                    {
                        allowing(dssFactory).create(dataStore.getRemoteUrl());
                        will(returnValue(service));
                    }
                }
            });

    }

    private void prepareSearchForDataSetsWithSequences(final DataStorePE dataStore, final IDataStoreService service,
            final double initialScore, final String... foundDataSets)
    {
        context.checking(new Expectations()
            {
                {
                    one(service).searchForEntitiesWithSequences(dataStore.getSessionToken(),
                            SEQUENCE_DATABASE, SEQUENCE_SNIPPET, OPTIONAL_PARAMETERS);
                    List<SearchDomainSearchResult> results = new ArrayList<SearchDomainSearchResult>();
                    double score = initialScore;
                    for (String foundDataSet : foundDataSets)
                    {
                        SearchDomainSearchResult result = new SearchDomainSearchResult();
                        SearchDomain searchDomain = new SearchDomain();
                        searchDomain.setName("test-db");
                        result.setSearchDomain(searchDomain);
                        result.setScore(createScore(score++));
                        DataSetFileSearchResultLocation resultLocation = new DataSetFileSearchResultLocation();
                        resultLocation.setPermId(foundDataSet);
                        resultLocation.setPathInDataSet(foundDataSet + "/path");
                        resultLocation.setPosition(42);
                        resultLocation.setIdentifier("id-" + foundDataSet);
                        result.setResultLocation(resultLocation);
                        results.add(result);
                    }
                    will(returnValue(results));
                }
            });
    }

    private void prepareSearchForEntityPropertiesWithSequences(final DataStorePE dataStore,
            final IDataStoreService service, final double initialScore, final String... foundLocations)
    {
        context.checking(new Expectations()
            {
                {
                    one(service).searchForEntitiesWithSequences(dataStore.getSessionToken(),
                            SEQUENCE_DATABASE, SEQUENCE_SNIPPET, OPTIONAL_PARAMETERS);
                    List<SearchDomainSearchResult> results = new ArrayList<SearchDomainSearchResult>();
                    double score = initialScore;
                    for (String foundLocation : foundLocations)
                    {
                        SearchDomainSearchResult result = new SearchDomainSearchResult();
                        SearchDomain searchDomain = new SearchDomain();
                        searchDomain.setName("test-db");
                        result.setSearchDomain(searchDomain);
                        result.setScore(createScore(score++));
                        EntityPropertyBlastSearchResultLocation resultLocation = new EntityPropertyBlastSearchResultLocation();
                        String[] splittedLocation = foundLocation.split(":");
                        resultLocation.setEntityKind(EntityKind.valueOf(splittedLocation[0]));
                        resultLocation.setPermId(splittedLocation[1]);
                        resultLocation.setPropertyType(splittedLocation[2]);
                        AlignmentMatch alignmentMatch = new AlignmentMatch();
                        alignmentMatch.setSequenceStart(42);
                        alignmentMatch.setSequenceEnd(45);
                        alignmentMatch.setQueryStart(7);
                        alignmentMatch.setQueryEnd(10);
                        resultLocation.setAlignmentMatch(alignmentMatch);
                        result.setResultLocation(resultLocation);
                        results.add(result);
                    }
                    will(returnValue(results));
                }
            });
    }

    private void prepareListDataSetsByCode(final ExternalDataPE... dataSets)
    {
        context.checking(new Expectations()
            {
                {
                    one(dataDAO).listByCode(new LinkedHashSet<String>(getPermIds(dataSets)));
                    will(returnValue(Arrays.asList(dataSets)));
                }
            });
    }

    private void prepareListSamplesByPermId(final SamplePE... samples)
    {
        context.checking(new Expectations()
            {
                {
                    one(sampleDAO).listByPermID(getPermIds(samples));
                    will(returnValue(Arrays.asList(samples)));
                }
            });
    }

    private void prepareListExperimentsByPermId(final ExperimentPE... experiments)
    {
        context.checking(new Expectations()
            {
                {
                    one(experimentDAO).listByPermID(getPermIds(experiments));
                    will(returnValue(Arrays.asList(experiments)));
                }
            });
    }

    private ExternalDataPE createDataSet(String code, DataStorePE dataStore)
    {
        ExternalDataPE data = new ExternalDataPE();
        data.setId((long) code.hashCode());
        data.setCode(code);
        data.setDataStore(dataStore);
        data.setLocation("here/" + code);
        ExperimentPE experiment = new ExperimentPE();
        experiment.setCode("exp1");
        experiment.setExperimentType(new ExperimentTypePE());
        ProjectPE project = new ProjectPE();
        project.setCode("p1");
        SpacePE group = new SpacePE();
        group.setCode("g1");
        project.setSpace(group);
        experiment.setProject(project);
        data.setExperiment(experiment);
        DataSetTypePE type = new DataSetTypePE();
        type.setCode("UNKNOWN");
        data.setDataSetType(type);
        data.setDataSetKind(DataSetKind.PHYSICAL.name());
        FileFormatTypePE fileFormatType = new FileFormatTypePE();
        fileFormatType.setCode("fileFormat");
        data.setFileFormatType(fileFormatType);
        return data;
    }

    private SamplePE createSample(String permID)
    {
        SamplePE sample = new SamplePE();
        sample.setCode("CODE-" + permID);
        sample.setPermId(permID);
        SampleTypePE sampleType = new SampleTypePE();
        sampleType.setCode("TYPE");
        sampleType.setListable(true);
        sampleType.setSubcodeUnique(false);
        sampleType.setAutoGeneratedCode(false);
        sampleType.setShowParentMetadata(false);
        sampleType.setContainerHierarchyDepth(0);
        sampleType.setGeneratedFromHierarchyDepth(0);
        sample.setSampleType(sampleType);
        return sample;
    }

    private ExperimentPE createExperiment(String permID)
    {
        ExperimentPE experiment = new ExperimentPE();
        experiment.setPermId(permID);
        experiment.setCode("CODE-" + permID);
        ExperimentTypePE experimentType = new ExperimentTypePE();
        experimentType.setCode("UNKNOWN-E");
        experiment.setExperimentType(experimentType);
        ProjectPE project = new ProjectPE();
        project.setSpace(new SpacePE());
        experiment.setProject(project);
        return experiment;
    }

    private SearchDomainSearcher createSearcher()
    {
        return new SearchDomainSearcher(daoFactory, ManagerTestTool.EXAMPLE_SESSION,
                managedPropertyEvaluatorFactory, null, null, dssFactory);
    }

    protected List<String> getPermIds(final IPermIdHolder... permIdHolders)
    {
        List<String> permIds = new ArrayList<String>();
        for (IPermIdHolder permIdHolder : permIdHolders)
        {
            permIds.add(permIdHolder.getPermId());
        }
        return permIds;
    }

    private ISearchDomainResultScore createScore(double score)
    {
        BlastScore blastScore = new BlastScore();
        blastScore.setScore(score);
        blastScore.setBitScore(score / 2);
        blastScore.setEvalue(score + 1);
        return blastScore;
    }
}
