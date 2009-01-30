/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.server;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.client.shared.DataType;
import ch.systemsx.cisd.openbis.generic.client.shared.PropertyType;
import ch.systemsx.cisd.openbis.generic.client.shared.SampleType;
import ch.systemsx.cisd.openbis.generic.client.shared.Vocabulary;
import ch.systemsx.cisd.openbis.generic.client.shared.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.CacheManager;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.DefaultResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.IOriginalDataProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.IResultSetKeyGenerator;
import ch.systemsx.cisd.openbis.generic.server.SessionConstants;
import ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.FileFormatTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityDataType;

/**
 * Test cases for corresponding {@link CommonClientService} class.
 * 
 * @author Christian Ribeaud
 */
public final class CommonClientServiceTest extends AbstractClientServiceTest
{
    private CommonClientService commonClientService;

    private ICommonServer commonServer;

    private final static ListSampleCriteria createListCriteria()
    {
        final ListSampleCriteria criteria = new ListSampleCriteria();
        final SampleType sampleType = createSampleType("MASTER_PLATE", "DB1");
        criteria.setSampleType(sampleType);
        return criteria;
    }

    private final static SampleType createSampleType(final String code, final String dbCode)
    {
        final SampleType sampleType = new SampleType();
        sampleType.setCode(code);
        sampleType.setDatabaseInstance(createDatabaseInstance(dbCode));
        return sampleType;
    }

    private final static List<Sample> createSampleList()
    {
        return Collections.emptyList();
    }

    private final static void assertDataTypeEquals(final DataTypePE dataTypePE,
            final DataType dataType)
    {
        assertEquals(dataTypePE.getCode().name(), dataType.getCode().name());
        assertEquals(dataTypePE.getDescription(), dataType.getDescription());
    }

    private final static VocabularyPE createVocabulary()
    {
        final VocabularyPE vocabularyPE = new VocabularyPE();
        vocabularyPE.setCode("USER.COLOR");
        vocabularyPE.setDescription("Vocabulary color");
        vocabularyPE.setRegistrator(ManagerTestTool.EXAMPLE_PERSON);
        vocabularyPE.setDatabaseInstance(ManagerTestTool.EXAMPLE_DATABASE_INSTANCE);
        vocabularyPE.addTerm(createVocabularyTerm("RED"));
        vocabularyPE.addTerm(createVocabularyTerm("BLACK"));
        vocabularyPE.addTerm(createVocabularyTerm("WHITE"));
        return vocabularyPE;
    }

    private final static VocabularyTermPE createVocabularyTerm(final String code)
    {
        final VocabularyTermPE vocabularyTermPE = new VocabularyTermPE();
        vocabularyTermPE.setCode(code);
        vocabularyTermPE.setRegistrator(ManagerTestTool.EXAMPLE_PERSON);
        return vocabularyTermPE;
    }

    private final static void assertVocabularyEquals(final VocabularyPE vocabularyPE,
            final Vocabulary vocabulary)
    {
        assertEquals(vocabulary.getCode(), vocabularyPE.getCode());
        assertEquals(vocabulary.getDescription(), vocabularyPE.getDescription());
        final List<VocabularyTerm> terms = vocabulary.getTerms();
        final Set<VocabularyTermPE> termPEs = vocabularyPE.getTerms();
        assertEquals(terms.size(), termPEs.size());
    }

    //
    // AbstractClientServiceTest
    //

    @Override
    @BeforeMethod
    public final void setUp()
    {
        super.setUp();
        commonServer = context.mock(ICommonServer.class);
        commonClientService = new CommonClientService(commonServer, requestContextProvider);
    }

    @Test
    public final void testListSamples()
    {
        final String resultSetKey = "1";
        final DefaultResultSet<String, Sample> defaultResultSet =
                new DefaultResultSet<String, Sample>(resultSetKey, createSampleList(), 0);
        final ListSampleCriteria listCriteria = createListCriteria();
        context.checking(new Expectations()
            {
                {
                    prepareGetHttpSession(this);
                    prepareGetSessionToken(this);
                    prepareGetResultSetManager(this);

                    one(resultSetManager).getResultSet(with(listCriteria),
                            getAnyOriginalDataProvider(this));
                    will(returnValue(defaultResultSet));
                }
            });
        final ResultSet<Sample> resultSet = commonClientService.listSamples(listCriteria);
        assertEquals(0, resultSet.getList().size());
        assertEquals(resultSetKey, resultSet.getResultSetKey());
        assertEquals(0, resultSet.getTotalLength());
        context.assertIsSatisfied();
    }

    @Test
    public final void testListDataTypes()
    {
        final DataTypePE dataTypePE = new DataTypePE();
        dataTypePE.setCode(EntityDataType.INTEGER);
        dataTypePE.setDescription("The description");
        context.checking(new Expectations()
            {
                {
                    prepareGetSessionToken(this);

                    one(commonServer).listDataTypes(SESSION_TOKEN);
                    will(returnValue(Collections.singletonList(dataTypePE)));
                }
            });
        final List<DataType> dataTypes = commonClientService.listDataTypes();
        assertEquals(1, dataTypes.size());
        assertDataTypeEquals(dataTypePE, dataTypes.get(0));
        context.assertIsSatisfied();
    }

    @Test
    public final void testPrepareExportSamples()
    {
        final TableExportCriteria<Sample> criteria = new TableExportCriteria<Sample>();
        final CacheManager<String, TableExportCriteria<Sample>> manager = createCacheManager();
        context.checking(new Expectations()
            {
                {
                    prepareGetSessionToken(this);
                    prepareGetCacheManager(this, manager);
                }
            });
        final String key = commonClientService.prepareExportSamples(criteria);
        assertEquals("" + CounterBasedResultSetKeyGenerator.INIT_VALUE, key);
        assertEquals(criteria, manager.tryGetData(key));
        context.assertIsSatisfied();
    }

    private void prepareGetCacheManager(final Expectations exp,
            final CacheManager<String, TableExportCriteria<Sample>> manager)
    {
        prepareGetHttpSession(exp);
        exp.allowing(httpSession).getAttribute(SessionConstants.OPENBIS_EXPORT_MANAGER);
        exp.will(Expectations.returnValue(manager));
    }

    @SuppressWarnings("unchecked")
    private final IOriginalDataProvider<Sample> getAnyOriginalDataProvider(final Expectations exp)
    {
        return exp.with(Expectations.any(IOriginalDataProvider.class));
    }

    private <T> CacheManager<String, T> createCacheManager()
    {
        return new CacheManager<String, T>(new CounterBasedResultSetKeyGenerator());
    }

    private static final class CounterBasedResultSetKeyGenerator implements
            IResultSetKeyGenerator<String>
    {
        public static final int INIT_VALUE = 123;

        private static final long serialVersionUID = 1L;

        private int counter = INIT_VALUE;

        public final String createKey()
        {
            return "" + counter++;
        }
    }

    @Test
    public final void testRegisterPropertyType()
    {
        context.checking(new Expectations()
            {
                {
                    prepareGetSessionToken(this);

                    one(commonServer).registerPropertyType(with(SESSION_TOKEN),
                            with(aNonNull(PropertyType.class)));
                }
            });
        commonClientService.registerPropertyType(new PropertyType());
        context.assertIsSatisfied();
    }

    @Test
    public final void testRegisterVocabulary()
    {
        context.checking(new Expectations()
            {
                {
                    prepareGetSessionToken(this);

                    one(commonServer).registerVocabulary(with(SESSION_TOKEN),
                            with(aNonNull(Vocabulary.class)));
                }
            });
        commonClientService.registerVocabulary(new Vocabulary());
        context.assertIsSatisfied();
    }

    @Test
    public final void testListVocabularies()
    {
        final VocabularyPE vocabularyPE = createVocabulary();
        final boolean excludeInternals = true;
        context.checking(new Expectations()
            {
                {
                    prepareGetSessionToken(this);

                    one(commonServer).listVocabularies(SESSION_TOKEN, false, excludeInternals);
                    will(returnValue(Collections.singletonList(vocabularyPE)));
                }
            });
        DefaultResultSetConfig<String, Vocabulary> criteria =
                DefaultResultSetConfig.createFetchAll();
        final List<Vocabulary> vocabularies =
                commonClientService.listVocabularies(false, excludeInternals, criteria).getList();
        assertEquals(1, vocabularies.size());
        assertVocabularyEquals(vocabularyPE, vocabularies.get(0));
        context.assertIsSatisfied();
    }

    @Test
    public void testListExternalDataForExperiment()
    {
        final ExternalDataPE externalDataPE = new ExternalDataPE();
        FileFormatTypePE fileFormatTypePE = new FileFormatTypePE();
        fileFormatTypePE.setCode("PNG");
        fileFormatTypePE.setDescription("Portable Network Graphics");
        externalDataPE.setFileFormatType(fileFormatTypePE);
        context.checking(new Expectations()
            {
                {
                    prepareGetSessionToken(this);

                    one(commonServer).listExternalData(
                            SESSION_TOKEN,
                            new ExperimentIdentifier(
                                    new ProjectIdentifier("db", "group", "project"), "exp"));
                    will(returnValue(Collections.singletonList(externalDataPE)));
                }
            });

        List<ExternalData> list =
                commonClientService.listExternalDataForExperiment("db:/group/project/exp");
        assertEquals(1, list.size());
        assertEquals("PNG", list.get(0).getFileFormatType().getCode());
        assertEquals("Portable Network Graphics", list.get(0).getFileFormatType().getDescription());

        context.assertIsSatisfied();
    }
}
