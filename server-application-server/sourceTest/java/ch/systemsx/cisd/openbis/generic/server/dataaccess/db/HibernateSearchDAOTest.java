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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import static ch.systemsx.cisd.openbis.generic.server.dataaccess.db.DAOFactory.USE_NEW_SQL_ENGINE;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.testng.AssertJUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IHibernateSearchDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetAttributeSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriterion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchField;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MatchingEntity;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SearchCriteriaConnection;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SearchableEntity;
import ch.systemsx.cisd.openbis.generic.shared.translator.DtoConverters;

/**
 * Test cases for corresponding {@link IHibernateSearchDAO} class.
 *
 * @author Christian Ribeaud
 */
@Test(groups = { "db", "hibernateSearch" })
@Friend(toClasses = HibernateSearchDAOV3Adaptor.class)
public final class HibernateSearchDAOTest extends AbstractDAOTest
{

    private static final String USER_ID = "test";

    private static final String FILE_TYPE_TIFF = "TIFF";

    private static final String FILE_TYPE_3VPROPRIETARY = "3VPROPRIETARY";

    private static final String FILE_TYPE_XML = "XML";

    @DataProvider(name = "registratorTerm")
    private final static Object[][] getRegistratorTerm()
    {
        return new Object[][] {
                { "john" },
                { "Jo?n" },
                { "*ohn" } };
    }

    @Test
    public final void testSearchEntitiesByTermFailed()
    {
        final IHibernateSearchDAO hibernateSearchDAO = daoFactory.getHibernateSearchDAO();
        boolean fail = true;
        try
        {
            hibernateSearchDAO.searchEntitiesByTerm(USER_ID, null, null, createDataProvider(),
                    true, 0, Integer.MAX_VALUE);
        } catch (final AssertionError ex)
        {
            fail = false;
        }
        assertFalse(fail);
        fail = true;
        try
        {
            hibernateSearchDAO.searchEntitiesByTerm(USER_ID, SearchableEntity.MATERIAL, "",
                    createDataProvider(), true, 0, Integer.MAX_VALUE);
        } catch (final AssertionError ex)
        {
            fail = false;
        }
        assertFalse(fail);
    }

    private final HibernateSearchDataProvider createDataProvider()
    {
        return new HibernateSearchDataProvider(daoFactory);
    }

//    NOTE - NEW SEARCH ENGINE: Missing feature, Registrator on full text search
//    @Test(dataProvider = "registratorTerm")
//    public final void testSearchEntitiesByRegistrator(final String term)
//    {
//        final IHibernateSearchDAO hibernateSearchDAO = daoFactory.getHibernateSearchDAO();
//        final String lastName = "John";
//        final List<MatchingEntity> hits =
//                hibernateSearchDAO.searchEntitiesByTerm(USER_ID, SearchableEntity.SAMPLE, term,
//                        createDataProvider(), true, 0, Integer.MAX_VALUE);
//        assertTrue(hits.size() > 0);
//        for (MatchingEntity matchingEntity : hits)
//        {
//            AssertionUtil.assertContains(lastName, matchingEntity.getRegistrator().getFirstName());
//
//            for (PropertyMatch match : matchingEntity.getMatches())
//            {
//                String fieldDescription = match.getCode();
//                if (fieldDescription.contains("Name of registrator") == false
//                        && fieldDescription.contains("Name of modifier") == false)
//                {
//                    fail("Field description '" + fieldDescription + "' neither contains 'First name of registrator' " +
//                            "nor 'First name of modifier'.");
//                }
//            }
//        }
//    }

//    NOTE - NEW SEARCH ENGINE: Missing feature, wildcard search
//    @DataProvider(name = "experimentQueriestAndModeToTest")
//    protected Object[][] getExperimentQueriesAndModeToTest()
//    {
//        return new Object[][] {
//                { "exp-*", "exp-", true },
//                { "exp-", "exp-", false } };
//    }
//
//    @Test(dataProvider = "experimentQueriestAndModeToTest", groups = "broken")
//    public final void testSearchEntitiesByTermForExperiment(String query, String querySubstring,
//            boolean useWildcardMode)
//    {
//        final IHibernateSearchDAO hibernateSearchDAO = daoFactory.getHibernateSearchDAO();
//        final List<MatchingEntity> hits =
//                hibernateSearchDAO.searchEntitiesByTerm(USER_ID, SearchableEntity.EXPERIMENT,
//                        query, createDataProvider(), useWildcardMode, 0, Integer.MAX_VALUE);
//        assertEquals(8, hits.size());
//        for (MatchingEntity matchingEntity : hits)
//        {
//            AssertionUtil.assertContainsInsensitive(querySubstring, matchingEntity.getCode());
//            assertEquals("Code", matchingEntity.getMatches().get(0).getCode());
//        }
//    }

    @Test
    public final void testSearchEntitiesByTermForMaterial()
    {
        final IHibernateSearchDAO hibernateSearchDAO = daoFactory.getHibernateSearchDAO();
        String propertyValue = "adenovirus";
        final List<MatchingEntity> hits =
                hibernateSearchDAO.searchEntitiesByTerm(USER_ID, SearchableEntity.MATERIAL,
                        propertyValue, createDataProvider(), false, 0, Integer.MAX_VALUE);
        assertEquals(2, hits.size());
        for (MatchingEntity matchingEntity : hits)
        {
            MaterialPE material =
                    daoFactory.getMaterialDAO().getByTechId(new TechId(matchingEntity.getId()));
            ensureContains(material.getProperties(), propertyValue);
        }
    }

    private static void ensureContains(Set<MaterialPropertyPE> properties, String propertyValue)
    {
        boolean ok = false;
        for (MaterialPropertyPE prop : properties)
        {
            ok = ok || containsInsensitve(prop.tryGetUntypedValue(), propertyValue);
        }
        assertTrue("No property contains text " + propertyValue, ok);
    }

    private static boolean containsInsensitve(String text, String substring)
    {
        return text.toUpperCase().contains(substring.toUpperCase());
    }

    // ----------------- test serach for datasets

    private static DetailedSearchCriteria createOrDatasetQuery(DetailedSearchCriterion... criteria)
    {
        return createDatasetQuery(SearchCriteriaConnection.MATCH_ANY, criteria);
    }

    private static DetailedSearchCriteria createAndDatasetQuery(DetailedSearchCriterion... criteria)
    {
        return createDatasetQuery(SearchCriteriaConnection.MATCH_ALL, criteria);
    }

    private static DetailedSearchCriteria createDatasetQuery(SearchCriteriaConnection connection,
            DetailedSearchCriterion[] criteria)
    {
        DetailedSearchCriteria result = new DetailedSearchCriteria();
        result.setConnection(connection);
        result.setCriteria(Arrays.asList(criteria));
        return result;
    }

    private static DetailedSearchCriterion mkCriterion(DetailedSearchField field, String value)
    {
        return new DetailedSearchCriterion(field, value);
    }

    private List<String> fetchPropertyTypeCodes()
    {
        List<PropertyTypePE> propertyTypes = daoFactory.getPropertyTypeDAO().listAllPropertyTypes();
        List<String> codes = new ArrayList<String>();
        for (PropertyTypePE prop : propertyTypes)
        {
            codes.add(prop.getCode());
        }
        return codes;
    }

    /**
     * Returns a list of datasets mathing given criteria.<br>
     * <br>
     * Only eager connections are loaded.
     */
    private List<ExternalDataPE> searchForDatasets(DetailedSearchCriteria criteria)
    {
        final IHibernateSearchDAO hibernateSearchDAO = daoFactory.getHibernateSearchDAO();
        List<Long> datasetIds =
                hibernateSearchDAO.searchForEntityIds(USER_ID, criteria,
                        DtoConverters.convertEntityKind(EntityKind.DATA_SET));
        final List<ExternalDataPE> result = new ArrayList<ExternalDataPE>();
        for (Long datasetId : datasetIds)
        {
            ExternalDataPE dataSet = tryGetDataSetById(datasetId);
            if (dataSet != null)
            {
                result.add(dataSet);
            }
        }
        return result;
    }

    // NOTE: depends on proper implementation of DataDAO.
    //
    // HibernateSearhDAO returns only dataset ids, which are too strongly connected with database
    // content and should not be directly tested in asserts. Using ExternalDataDAO we can check
    // if dataset with given id is in fact in the DB and use dataset attributes in asserts instead
    // of using ids.
    private ExternalDataPE tryGetDataSetById(Long datasetId)
    {
        return daoFactory.getDataDAO().getByTechId(new TechId(datasetId)).tryAsExternalData();
    }

    // NOTE: such a check depends strongly on the test database content. Use it only when the better
    // way to check the results is much harder.
    private void assertCorrectDatasetsFound(DetailedSearchCriteria criteria,
            DSLoc... expectedLocations)
    {
        List<ExternalDataPE> dataSets =
                searchForDatasets(criteria);

        AssertJUnit.assertEquals(expectedLocations.length, dataSets.size());
        for (ExternalDataPE dataSet : dataSets)
        {
            assertContains(expectedLocations, dataSet.getLocation());
        }
    }

    /**
     * a more loose check for the database contents, where we expect to know only part of the resutls.
     */
    private void assertAtLeastDatasetsFound(DetailedSearchCriteria criteria,
            int expectedTotalResults, DSLoc... expectedLocations)
    {
        List<ExternalDataPE> dataSets =
                searchForDatasets(criteria);
        AssertJUnit.assertEquals(expectedTotalResults, dataSets.size());
        for (DSLoc expectedLocation : expectedLocations)
        {
            boolean found = false;
            for (ExternalDataPE dataSet : dataSets)
            {
                if (expectedLocation.location.equals(dataSet.getLocation()))
                {
                    found = true;
                }
            }
            AssertJUnit.assertTrue("Expected dataset location " + expectedLocation
                    + " not found in database.", found);
        }
    }

    private static void assertContains(DSLoc[] expectedLocations, String location)
    {
        for (DSLoc loc : expectedLocations)
        {
            if (loc.getLocation().equals(location))
            {
                return;
            }
        }
        fail("Dataset location " + location + " not found in the search result");
    }

    // enumerates existing dataset locations in the database
    private static enum DSLoc
    {
        A_1("a/1"),

        A_2("a/2"),

        A_3("a/3"),

        A_4("a/4"),

        ANALYSIS_RESULT("analysis/result"),

        CONTAINED_1("contained/20110509092359990-11"),

        CONTAINED_2("contained/20110509092359990-12"),

        XML_RESULT_8("xml/result-8"),

        XML_RESULT_9("xml/result-9"),

        XML_RESULT_10("xml/result-10"),

        XML_RESULT_11("xml/result-11"),

        XML_RESULT_12("xml/result-12"),

        XML_RESULT_18("xml/result-18"),

        XML_RESULT_20("xml/result-20"),

        XML_RESULT_21("xml/result-21"),

        XML_RESULT_22("xml/result-22"),

        XML_RESULT_27("xml/result-27"),

        XML_RESULT_28("xml/result-28"),

        XXX_YYY_ZZZ("xxx/yyy/zzz"),

        COMPONENT_1A("contained/COMPONENT_1B"),

        COMPONENT_1B("contained/COMPONENT_1B"),

        COMPONENT_2A("contained/COMPONENT_2A");

        private final String location;

        private DSLoc(String location)
        {
            this.location = location;
        }

        public String getLocation()
        {
            return location;
        }
    }

    private static DetailedSearchField createAnySearchField(List<String> propertyTypes)
    {
        return DetailedSearchField.createAnyField(propertyTypes);
    }

    private DetailedSearchCriterion createAnyFieldCriterion(String queryText)
    {
        List<String> propertyTypes = fetchPropertyTypeCodes();
        return mkCriterion(createAnySearchField(propertyTypes), queryText);
    }

    private DetailedSearchCriterion createFieldTypeCriterion(String fieldType)
    {
        return mkCriterion(
                DetailedSearchField.createAttributeField(DataSetAttributeSearchFieldKind.FILE_TYPE),
                fieldType);
    }

//  NOTE - NEW SEARCH ENGINE: "any field" don't include location attribute
//    @Test
//    public final void testSearchForDataSetsAnyField()
//    {
//        DetailedSearchCriterion criterion = createAnyFieldCriterion("*-1*");
//        DetailedSearchCriteria criteria = createAndDatasetQuery(criterion);
//        assertAtLeastDatasetsFound(criteria, 8, DSLoc.A_1, DSLoc.A_4, DSLoc.XML_RESULT_10, DSLoc.XML_RESULT_11, DSLoc.XML_RESULT_12,
//                DSLoc.XML_RESULT_18, DSLoc.CONTAINED_1, DSLoc.CONTAINED_2);
//    }

    @Test
    public final void testSearchForDataSetsSimpleField()
    {
        DetailedSearchCriterion criterion = createFieldTypeCriterion(FILE_TYPE_3VPROPRIETARY);
        DetailedSearchCriteria criteria = createAndDatasetQuery(criterion);
        assertCorrectDatasetsFound(criteria, DSLoc.A_2, DSLoc.ANALYSIS_RESULT);
    }

    @Test
    public final void testSearchForDataSetsSimpleFieldWithDeletedFilteredOut()
    {
        DetailedSearchCriterion criterion = createFieldTypeCriterion(FILE_TYPE_TIFF);
        DetailedSearchCriteria criteria = createAndDatasetQuery(criterion);
        assertCorrectDatasetsFound(criteria, DSLoc.A_1); // without deleted DSLoc.XXX_YYY_ZZZ
    }

//    NOTE - NEW SEARCH ENGINE: This test check Associations without including the rules on the sub criteria, something the core doesn't do and is not implemented on the adaptor.
//    @Test(dependsOnMethods = "testSearchForDataSetsSimpleField")
//    public final void testSearchForDataSetsSimpleFieldWithExperiment()
//    {
//        DetailedSearchCriterion criterion = createFieldTypeCriterion(FILE_TYPE_3VPROPRIETARY);
//        DetailedSearchCriteria criteria = createAndDatasetQuery(criterion);
//        DetailedSearchAssociationCriteria association =
//                new DetailedSearchAssociationCriteria(AssociatedEntityKind.EXPERIMENT,
//                        Collections.singleton(Long.valueOf(2L)));
//        // compared to testSearchForDataSetsSimpleField() DSLoc.A_2 should be filtered
//        // because of different experiment
//        assertCorrectDatasetsFound(criteria, association, DSLoc.ANALYSIS_RESULT);
//    }

//    NOTE - NEW SEARCH ENGINE: "any field" don't include location attribute
//    @Test
//    public final void testSearchForDataSetsComplexAndQuery()
//    {
//        DetailedSearchCriterion criterion1 = createAnyFieldCriterion("*-1*");
//        DetailedSearchCriterion criterion2 = createFieldTypeCriterion(FILE_TYPE_XML);
//        DetailedSearchCriteria criteria = createAndDatasetQuery(criterion1, criterion2);
//        assertAtLeastDatasetsFound(criteria, 6, DSLoc.XML_RESULT_10, DSLoc.XML_RESULT_11, DSLoc.XML_RESULT_12, DSLoc.XML_RESULT_18, DSLoc.CONTAINED_1,
//                DSLoc.CONTAINED_2);
//    }

    @Test
    public final void testSearchForDataSetsComplexOrQuery()
    {
        DetailedSearchCriterion criterion1 = createAnyFieldCriterion("*-1*");
        DetailedSearchCriterion criterion2 = createFieldTypeCriterion(FILE_TYPE_XML);
        DetailedSearchCriteria criteria = createOrDatasetQuery(criterion1, criterion2);
        criteria.setUseWildcardSearchMode(true);
        assertAtLeastDatasetsFound(criteria, 22, DSLoc.A_1, DSLoc.A_4, DSLoc.XML_RESULT_8,
                DSLoc.XML_RESULT_9, DSLoc.XML_RESULT_10, DSLoc.XML_RESULT_11, DSLoc.XML_RESULT_12,
                DSLoc.XML_RESULT_18, DSLoc.XML_RESULT_20, DSLoc.XML_RESULT_21, DSLoc.XML_RESULT_22,
                DSLoc.XML_RESULT_27, DSLoc.XML_RESULT_28, DSLoc.CONTAINED_1, DSLoc.CONTAINED_2,
                DSLoc.COMPONENT_1A, DSLoc.COMPONENT_1B, DSLoc.COMPONENT_2A);
    }

    // @Test
    // public final void testSearchForDataSetsComplexGeneric()
    // {
    // List<String> propertyTypes = fetchPropertyTypeCodes();
    // DataSetSearchCriterion criterion1 =
    // mkCriterion(createAnySearchField(propertyTypes), "3VCP1");
    // String propertyValue = "\"simple experiment\"";
    // DataSetSearchCriterion criterion2 =
    // mkCriterion(DataSetSearchField.createAnyExperimentProperty(propertyTypes),
    // propertyValue);
    // DataSetSearchCriterion criterion3 =
    // mkCriterion(DataSetSearchField.createSimpleField(DataSetSearchFieldKind.PROJECT),
    // "NEMO");
    // DataSetSearchCriterion criterion4 =
    // mkCriterion(DataSetSearchField.createExperimentProperty("GENDER"), "MALE");
    //
    // DataSetSearchCriteria criteria =
    // createAndDatasetQuery(criterion1, criterion2, criterion3, criterion4);
    // assertCorrectDatasetsFound(criteria, DSLoc.LOC4, DSLoc.LOC5);
    // }

    // TODO 2009-08-17, Piotr Buczek: write tests for related data sets with similar use cases

    // @Test
    // public final void testSearchForDataSetsSimpleField()
    // {
    // DataSetSearchCriterion criterion =
    // mkCriterion(DataSetSearchField.createSimpleField(DataSetSearchFieldKind.PROJECT),
    // "NEMO");
    // DataSetSearchCriteria criteria = createAndDatasetQuery(criterion);
    // assertCorrectDatasetsFound(criteria, DSLoc.LOC1, DSLoc.LOC3, DSLoc.LOC4, DSLoc.LOC5);
    // }

    // @Test
    // public final void testSearchForDataSetsSpecificSampleProperty()
    // {
    // String propertyValue = "stuff";
    // DataSetSearchCriterion criterion =
    // mkCriterion(DataSetSearchField.createSampleProperty("COMMENT"), propertyValue);
    // DataSetSearchCriteria criteria = createAndDatasetQuery(criterion);
    // assertCorrectDatasetsFound(criteria, DSLoc.LOC1, DSLoc.LOC2, DSLoc.LOC3);
    // }

    // @Test(groups = "broken")
    // public final void testSearchForDataSetsAnyProperty()
    // {
    // List<String> propertyTypes = fetchPropertyTypeCodes();
    // DataSetSearchCriterion criterion1 =
    // mkCriterion(DataSetSearchField.createAnyExperimentProperty(propertyTypes), "male");
    // DataSetSearchCriterion criterion2 =
    // mkCriterion(DataSetSearchField.createAnySampleProperty(propertyTypes), "fly");
    //
    // DataSetSearchCriteria criteria = createOrDatasetQuery(criterion1, criterion2);
    // assertCorrectDatasetsFound(criteria, DSLoc.LOC1, DSLoc.LOC4, DSLoc.LOC5);
    // }

    // @Test
    // public final void testSearchForDataSetsComplexGeneric()
    // {
    // List<String> propertyTypes = fetchPropertyTypeCodes();
    // DataSetSearchCriterion criterion1 =
    // mkCriterion(createAnySearchField(propertyTypes), "3VCP1");
    // String propertyValue = "\"simple experiment\"";
    // DataSetSearchCriterion criterion2 =
    // mkCriterion(DataSetSearchField.createAnyExperimentProperty(propertyTypes),
    // propertyValue);
    // DataSetSearchCriterion criterion3 =
    // mkCriterion(DataSetSearchField.createSimpleField(DataSetSearchFieldKind.PROJECT),
    // "NEMO");
    // DataSetSearchCriterion criterion4 =
    // mkCriterion(DataSetSearchField.createExperimentProperty("GENDER"), "MALE");
    //
    // DataSetSearchCriteria criteria =
    // createAndDatasetQuery(criterion1, criterion2, criterion3, criterion4);
    // assertCorrectDatasetsFound(criteria, DSLoc.LOC4, DSLoc.LOC5);
    // }

    // @Test
    // /*
    // * Checks if the dataset search index is properly updated after properties for a connected
    // * sample have changed.
    // */
    // public final void testSearchForDataSetsAfterSamplePropertiesUpdate()
    // throws InterruptedException
    // {
    // String propertyCode = "COMMENT";
    // DataSetSearchCriterion criterion =
    // mkCriterion(DataSetSearchField.createSampleProperty(propertyCode), "stuff");
    //
    // DataSetSearchCriteria criteria = createAndDatasetQuery(criterion);
    // assertCorrectDatasetsFound(criteria, DSLoc.LOC1, DSLoc.LOC2, DSLoc.LOC3);
    //
    // SamplePE sample = findSample("CP-TEST-3", "CISD");
    //
    // String newValue = "Bonanza";
    // changeSampleProperty(sample, propertyCode, newValue);
    //
    // flushSearchIndices();
    // assertCorrectDatasetsFound(criteria, DSLoc.LOC2, DSLoc.LOC3);
    // restoreSearchIndex();
    // }

    // TODO 2010-10-22, Piotr Buczek: write a different test (auto update is switched off)
    @Test(groups = "broken")
    /*
     * Checks if the dataset search index is properly updated after properties of a dataset have changed.
     */
    public final void testSearchForDataSetsAfterPropertiesUpdate() throws InterruptedException
    {
        String propertyCode = "COMMENT";
        DetailedSearchCriterion criterion1 =
                mkCriterion(DetailedSearchField.createPropertyField(propertyCode), "no comment");
        DetailedSearchCriterion criterion2 = createFieldTypeCriterion(FILE_TYPE_TIFF);

        DetailedSearchCriteria criteria = createAndDatasetQuery(criterion1, criterion2);
        if (USE_NEW_SQL_ENGINE) {
            assertCorrectDatasetsFound(criteria, DSLoc.A_1);
        } else {
            assertCorrectDatasetsFound(criteria, DSLoc.A_1, DSLoc.XXX_YYY_ZZZ); // The old search engine returns a deleted dataset
        }

        // This data set has "no comment" value as a COMMENT property and TIFF file type.
        // We change it and check if it is removed from results.
        ExternalDataPE externalData = findExternalData("20081105092159111-1"); // LOC3
        String newValue = "sth";
        changeExternalDataProperty(externalData, propertyCode, newValue);
        assertCorrectDatasetsFound(criteria, DSLoc.XXX_YYY_ZZZ);
    }

    private void flushSession()
    {
        sessionFactory.getCurrentSession().flush();
    }

    private void changeExternalDataProperty(ExternalDataPE externalData, String propertyCode,
            String newValue)
    {
        EntityPropertyPE property = findProperty(externalData, propertyCode);

        removeProperty(externalData, property);
        flushSession();

        DataSetPropertyPE newProperty = new DataSetPropertyPE();
        copyPropertyWithNewValue(newValue, property, newProperty);
        addProperty(externalData, newProperty);
        flushSession();
    }

    private void copyPropertyWithNewValue(String newValue, EntityPropertyPE oldProperty,
            EntityPropertyPE newProperty)
    {
        newProperty.setEntityTypePropertyType(oldProperty.getEntityTypePropertyType());
        newProperty.setRegistrator(oldProperty.getRegistrator());
        newProperty.setValue(newValue);
    }

    private static <T extends EntityPropertyPE> void addProperty(
            IEntityPropertiesHolder propertiesHolder, T newProperty)
    {
        Set<EntityPropertyPE> properties = getCopiedProperties(propertiesHolder);
        properties.add(newProperty);
        propertiesHolder.setProperties(properties);
    }

    private static Set<EntityPropertyPE> removeProperty(IEntityPropertiesHolder propertiesHolder,
            EntityPropertyPE property)
    {
        Set<EntityPropertyPE> properties = getCopiedProperties(propertiesHolder);
        boolean removed = properties.remove(property);
        assert removed : "property could not be removed";
        propertiesHolder.setProperties(properties);
        return properties;
    }

    private static Set<EntityPropertyPE> getCopiedProperties(
            IEntityPropertiesHolder propertiesHolder)
    {
        return new HashSet<EntityPropertyPE>(propertiesHolder.getProperties());
    }

    private static EntityPropertyPE findProperty(IEntityPropertiesHolder propertiesHolder,
            String propertyCode)
    {
        for (EntityPropertyPE prop : propertiesHolder.getProperties())
        {
            if (prop.getEntityTypePropertyType().getPropertyType().getCode().equals(propertyCode))
            {
                return prop;
            }
        }
        fail("property not found: " + propertyCode);
        return null; // never happens
    }

}
