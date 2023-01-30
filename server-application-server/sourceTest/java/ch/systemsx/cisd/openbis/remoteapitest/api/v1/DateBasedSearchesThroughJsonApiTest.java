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

import static org.hamcrest.MatcherAssert.assertThat;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseTimeAttribute;
import ch.systemsx.cisd.openbis.remoteapitest.RemoteApiTestCase;

@Test(groups = { "remote api" }, dependsOnGroups = { "before remote api" })
public class DateBasedSearchesThroughJsonApiTest extends RemoteApiTestCase
{

    protected IGeneralInformationService generalInformationService;

    protected String sessionToken;

    protected IGeneralInformationService createService()
    {
        return TestJsonServiceFactory.createGeneralInfoService();
    }

    @BeforeMethod
    public void beforeMethod() throws MalformedURLException
    {
        generalInformationService = createService();
        sessionToken = generalInformationService.tryToAuthenticateForAllServices("test", "a");
    }

    @AfterMethod
    public void afterMethod() throws MalformedURLException
    {
        generalInformationService.logout(sessionToken);
    }

    @Test
    public void whenUsingEqualCompareModeOnlyDataFromGivenDateIsIncluded()
    {
        createDataSets(
                aDataSet().called("earlier").withRegistrationDateOn("2008-11-05"),
                aDataSet().called("atTheLimit").withRegistrationDateOn("2009-02-09"),
                aDataSet().called("later").withRegistrationDateOn("2011-05-09"));

        SearchCriteria criteria =
                criteriaWith(registrationDate(equals("2009-02-09", "+1")));

        List<DataSet> result = generalInformationService.searchForDataSets(sessionToken, criteria);

        assertThat(result, containsDataSets("atTheLimit"));
        assertThat(result, doesNotContainDataSets("earlier", "later"));
    }

    @Test
    public void whenUsingEqualCompareModeDataFromTheDayBeforeIsExcluded()
    {
        createDataSets(aDataSet().called("dataset").withRegistrationDateOn("2011-05-09"));

        SearchCriteria criteria =
                criteriaWith(registrationDate(equals("2011-05-10", "+1")));

        List<DataSet> result = generalInformationService.searchForDataSets(sessionToken, criteria);

        assertThat(result, doesNotContainDataSets("dataset"));
    }

    @Test
    public void whenUsingEqualCompareModeDataFromTheDayAfterIsExcluded()
    {
        createDataSets(aDataSet().called("dataset").withRegistrationDateOn("2011-05-09"));

        SearchCriteria criteria =
                criteriaWith(registrationDate(equals("2011-05-08", "+1")));

        List<DataSet> result = generalInformationService.searchForDataSets(sessionToken, criteria);

        assertThat(result, doesNotContainDataSets("dataset"));
    }

    @Test
    public void whenUsingLessThanOrEqualCompareModeOnlyDataFromEarlierDatesIsIncluded()
    {
        createDataSets(
                aDataSet().called("earlier").withRegistrationDateOn("2008-11-05"),
                aDataSet().called("atTheLimit").withRegistrationDateOn("2009-02-09"),
                aDataSet().called("later").withRegistrationDateOn("2011-05-09"));

        SearchCriteria criteria =
                criteriaWith(registrationDate(lessThanOrEqual("2009-02-09", "+1")));

        List<DataSet> result = generalInformationService.searchForDataSets(sessionToken, criteria);

        assertThat(result, containsDataSets("earlier", "atTheLimit"));
        assertThat(result, doesNotContainDataSets("later"));
    }

    @Test
    public void whenUsingMoreThanOrEqualCompareModeOnlyDataFromLaterDatesIsIncluded()
    {
        createDataSets(
                aDataSet().called("earlier").withRegistrationDateOn("2008-11-05"),
                aDataSet().called("atTheLimit").withRegistrationDateOn("2009-02-09"),
                aDataSet().called("later").withRegistrationDateOn("2011-05-09"));

        SearchCriteria criteria =
                criteriaWith(registrationDate(moreThanOrEqual("2009-02-09", "+1")));

        List<DataSet> result = generalInformationService.searchForDataSets(sessionToken, criteria);

        assertThat(result, containsDataSets("atTheLimit", "later"));
        assertThat(result, doesNotContainDataSets("earlier"));
    }

    @Test
    public void multipleMatchClausesCanBeUsedToCreateARangeQuery()
    {
        createDataSets(
                aDataSet().called("earlier").withRegistrationDateOn("2008-11-05"),
                aDataSet().called("middle").withRegistrationDateOn("2009-02-09"),
                aDataSet().called("later").withRegistrationDateOn("2011-05-09"));

        SearchCriteria criteria =
                criteriaWith(
                        registrationDate(moreThanOrEqual("2009-02-01", "+1")),
                        registrationDate(lessThanOrEqual("2009-03-01", "+1")));

        List<DataSet> result = generalInformationService.searchForDataSets(sessionToken, criteria);

        assertThat(result, containsDataSets("middle"));
        assertThat(result, doesNotContainDataSets("earlier", "later"));
    }

    @Test
    public void multipleAttributesCanBeCombinedInAWayThatNoDataSetsMatch()
    {
        createDataSets(
                aDataSet().called("first")
                        .withRegistrationDateOn("2008-11-05")
                        .withModificationDateOn("2009-03-23"),
                aDataSet().called("second")
                        .withRegistrationDateOn("2009-02-09")
                        .withModificationDateOn("2009-03-23"),
                aDataSet().called("third")
                        .withRegistrationDateOn("2011-05-09")
                        .withModificationDateOn("2011-05-09"));

        SearchCriteria criteria =
                criteriaWith(
                        registrationDate(lessThanOrEqual("2010-01-01", "+1")),
                        modificationDate(equals("2011-05-09", "+1")));

        List<DataSet> result = generalInformationService.searchForDataSets(sessionToken, criteria);

        assertThat(result, doesNotContainDataSets("first", "second", "third"));
    }

    @Test
    public void multipleAttributesCanBeCombinedInAWayThatADataSetMatches()
    {
        createDataSets(
                aDataSet().called("first")
                        .withRegistrationDateOn("2008-11-05")
                        .withModificationDateOn("2009-03-23"),
                aDataSet().called("second")
                        .withRegistrationDateOn("2009-02-09")
                        .withModificationDateOn("2009-03-23"),
                aDataSet().called("third")
                        .withRegistrationDateOn("2011-05-09")
                        .withModificationDateOn("2011-05-09"));

        SearchCriteria criteria =
                criteriaWith(
                        registrationDate(moreThanOrEqual("2010-01-01", "+1")),
                        modificationDate(equals("2011-05-09", "+1")));

        List<DataSet> result = generalInformationService.searchForDataSets(sessionToken, criteria);

        assertThat(result, containsDataSets("third"));
        assertThat(result, doesNotContainDataSets("first", "second"));
    }

    @Test
    public void aDataSetThatIsJustOutsideTheGivenDateOnGivenTimeZoneIsExcluded()
    {
        createDataSets(aDataSet().called("dataset").withRegistrationDateOn("2008-11-05 09:21:59.313+01"));

        SearchCriteria criteria =
                criteriaWith(registrationDate(moreThanOrEqual("2008-11-05", "-9")));

        List<DataSet> result = generalInformationService.searchForDataSets(sessionToken, criteria);

        assertThat(result, doesNotContainDataSets("dataset"));
    }

    @Test
    public void aDataSetThatIsJustInsideTheGivenDateOnGivenTimeZoneIsIncluded()
    {
        createDataSets(aDataSet().called("dataset").withRegistrationDateOn("2008-11-05 09:21:59.313+01"));

        SearchCriteria criteria =
                criteriaWith(registrationDate(moreThanOrEqual("2008-11-05", "-8")));

        List<DataSet> result = generalInformationService.searchForDataSets(sessionToken, criteria);

        assertThat(result, containsDataSets("dataset"));
    }

    private SearchCriteria criteriaWith(MatchClause... clauses)
    {
        SearchCriteria criteria = new SearchCriteria();
        for (MatchClause clause : clauses)
        {
            criteria.addMatchClause(clause);
        }
        return criteria;
    }

    private MatchClause registrationDate(SearchParameters param)
    {
        return MatchClause.createTimeAttributeMatch(MatchClauseTimeAttribute.REGISTRATION_DATE, param
                .getCompareMode(), param.getDate(), param.getTimeZone());
    }

    private MatchClause modificationDate(SearchParameters param)
    {
        return MatchClause.createTimeAttributeMatch(MatchClauseTimeAttribute.MODIFICATION_DATE, param
                .getCompareMode(), param.getDate(), param.getTimeZone());
    }

    private SearchParameters equals(String date, String timezone)
    {
        return new SearchParameters(SearchCriteria.CompareMode.EQUALS, date, timezone);
    }

    private SearchParameters lessThanOrEqual(String date, String timezone)
    {
        return new SearchParameters(SearchCriteria.CompareMode.LESS_THAN_OR_EQUAL, date, timezone);
    }

    private SearchParameters moreThanOrEqual(String date, String timezone)
    {
        return new SearchParameters(SearchCriteria.CompareMode.GREATER_THAN_OR_EQUAL, date, timezone);
    }

    private static class SearchParameters
    {
        private SearchCriteria.CompareMode compareMode;

        private String date;

        private String timezone;

        public SearchParameters(SearchCriteria.CompareMode compareMode, String date, String timezone)
        {
            this.compareMode = compareMode;
            this.date = date;
            this.timezone = timezone;
        }

        public SearchCriteria.CompareMode getCompareMode()
        {
            return this.compareMode;
        }

        public String getDate()
        {
            return this.date;
        }

        public String getTimeZone()
        {
            return this.timezone;
        }
    }

    private static class DataSetParameters
    {
        private String code;

        private List<Filter<DataSetInfo>> filters = new ArrayList<Filter<DataSetInfo>>();

        public DataSetParameters called(String aCode)
        {
            this.code = aCode;
            return this;
        }

        public DataSetParameters withRegistrationDateOn(final String aDate)
        {
            this.filters.add(new Filter<DataSetInfo>()
                {
                    @Override
                    public boolean accepts(DataSetInfo t)
                    {
                        return t.getRegistrationDate().startsWith(aDate);
                    }

                    @Override
                    public String toString()
                    {
                        return "registrationDate on " + aDate;
                    }
                });
            return this;
        }

        public DataSetParameters withModificationDateOn(final String aDateString)
        {
            this.filters.add(new Filter<DataSetInfo>()
                {
                    @Override
                    public boolean accepts(DataSetInfo t)
                    {
                        return t.getModificationDate().startsWith(aDateString);
                    }

                    @Override
                    public String toString()
                    {
                        return "modificationDate on " + aDateString;
                    }
                });
            return this;
        }

        public String getCode()
        {
            return code;
        }

        public List<Filter<DataSetInfo>> getFilters()
        {
            return this.filters;
        }

        @Override
        public String toString()
        {
            String description = "";
            for (Filter<DataSetInfo> filter : this.filters)
            {
                description += " and " + filter.toString();
            }
            return description.substring(4);
        }

    }

    public static DataSetParameters aDataSet()
    {
        return new DataSetParameters();
    }

    private static List<DataSetIdentifier> datasets;

    private static void createDataSets(DataSetParameters... params)
    {
        datasets = new ArrayList<DataSetIdentifier>();
        for (DataSetParameters param : params)
        {
            datasets.add(new DataSetIdentifier(param, findDataSetWithParam(param)));
        }
    }

    private static interface Filter<T>
    {
        public boolean accepts(T t);
    }

    private static DataSetInfo findDataSetWithParam(DataSetParameters param)
    {
        for (DataSetInfo ds : allDataSets)
        {
            boolean accepted = true;
            for (Filter<DataSetInfo> f : param.getFilters())
            {
                accepted = accepted && f.accepts(ds);
            }

            if (accepted)
            {
                return ds;
            }
        }
        throw new RuntimeException("Cannot find a dataset with " + param);
    }

    public static interface Identifier<T>
    {
        public boolean foundIn(Collection<T> items);

        public String getRealId();
    }

    private static class DataSetIdentifier implements Identifier<DataSet>
    {
        private DataSetParameters param;

        private DataSetInfo info;

        public DataSetIdentifier(DataSetParameters param, DataSetInfo info)
        {
            this.param = param;
            this.info = info;
        }

        public boolean isKnownAs(String name)
        {
            return this.param.getCode().equals(name);
        }

        @Override
        public boolean foundIn(Collection<DataSet> items)
        {
            for (DataSet dataset : items)
            {
                if (dataset.getCode().equals(this.info.getCode()))
                {
                    return true;
                }
            }
            return false;
        }

        @Override
        public String getRealId()
        {
            return this.info.getCode();
        }

        @Override
        public String toString()
        {
            return this.param.code;
        }

    }

    public static List<DataSetIdentifier> createIdentifiers(String... ids)
    {
        List<DataSetIdentifier> identifiers = new ArrayList<DataSetIdentifier>();

        main: for (String id : ids)
        {
            for (DataSetIdentifier dataset : datasets)
            {
                if (dataset.isKnownAs(id))
                {
                    identifiers.add(dataset);
                    continue main;
                }
            }
            throw new IllegalArgumentException("Unknown identifier: " + id);
        }
        return identifiers;
    }

    public static Matcher<Collection<DataSet>> containsDataSets(String... ids)
    {

        return new CollectionContainsMatcher<DataSet>(createIdentifiers(ids)
                .toArray(new DataSetIdentifier[] {}));
    }

    public static Matcher<Collection<DataSet>> doesNotContainDataSets(String... ids)
    {
        return new CollectionDoesNotContainMatcher<DataSet>(createIdentifiers(ids)
                .toArray(new DataSetIdentifier[] {}));
    }

    public static class CollectionContainsMatcher<T> extends TypeSafeMatcher<Collection<T>>
    {

        private List<Identifier<T>> identifiers;

        public CollectionContainsMatcher(Identifier<T>... ids)
        {
            this.identifiers = Collections.unmodifiableList(Arrays.asList(ids));
        }

        @Override
        public final void describeTo(Description description)
        {
            description.appendText("A collection containing at least these elements:");
            for (Identifier<T> id : this.identifiers)
            {
                description.appendText(" " + id.toString() + " (" + id.getRealId() + ")");
            }
        }

        @Override
        public final boolean matchesSafely(Collection<T> actualItems)
        {
            for (Identifier<T> identifier : this.identifiers)
            {
                if (!identifier.foundIn(actualItems))
                {
                    return false;
                }
            }
            return true;
        }

        /*
         * for hamcrest 1.3
         * @Override public void describeMismatchSafely(Collection<T> actualItems, Description mismatchDescription) { mismatchDescription.appendText(
         * "These elements were missing: "); for (Identifier<T> identifier : this.identifiers) { if (!identifier.foundIn(actualItems)) {
         * mismatchDescription.appendText(identifier.toString() + " "); } } }
         */
    }

    public static class CollectionDoesNotContainMatcher<T> extends TypeSafeMatcher<Collection<T>>
    {

        private List<Identifier<T>> identifiers;

        public CollectionDoesNotContainMatcher(Identifier<T>... ids)
        {
            this.identifiers = Collections.unmodifiableList(Arrays.asList(ids));
        }

        @Override
        public final void describeTo(Description description)
        {
            description.appendText("A collection that does not contain any of these elements:");
            for (Identifier<T> id : this.identifiers)
            {
                description.appendText(" " + id.toString() + " (" + id.getRealId() + ")");
            }
        }

        @Override
        public final boolean matchesSafely(Collection<T> actualItems)
        {
            for (Identifier<T> identifier : this.identifiers)
            {
                if (identifier.foundIn(actualItems))
                {
                    return false;
                }
            }
            return true;
        }

        /*
         * for hamcrest 1.3
         * @Override public void describeMismatchSafely(Collection<T> actualItems, Description mismatchDescription) { mismatchDescription.appendText(
         * "These unwanted elements were found: "); for (Identifier<T> identifier : this.identifiers) { if (identifier.foundIn(actualItems)) {
         * mismatchDescription.appendText(identifier.toString() + " "); } } }
         */
    }

    // This list contains all the datasets that are found in the database.
    // When this test is able to create it's own test data, these can and should be removed.
    private static List<DataSetInfo> allDataSets;

    {
        allDataSets = new ArrayList<DataSetInfo>();

        allDataSets.add(new DataSetInfo("20081105092159188-3", "2008-11-05 09:21:59.313+01",
                "2009-03-23 15:34:44.462776+01"));
        allDataSets.add(new DataSetInfo("20081105092159111-1", "2009-02-09 12:20:21.646654+01",
                "2009-03-23 15:34:44.462776+01"));
        allDataSets.add(new DataSetInfo("20081105092159222-2", "2009-02-09 12:21:11.479816+01",
                "2009-03-23 15:34:44.462776+01"));
        allDataSets.add(new DataSetInfo("20081105092159333-3", "2009-02-09 12:21:47.815468+01",
                "2009-03-23 15:34:44.462776+01"));
        allDataSets.add(new DataSetInfo("20081105092259000-8", "2008-11-05 09:22:59.313+01",
                "2009-03-23 15:34:44.462776+01"));
        allDataSets.add(new DataSetInfo("20081105092259000-9", "2008-11-05 09:22:59.313+01",
                "2009-03-23 15:34:44.462776+01"));
        allDataSets.add(new DataSetInfo("20081105092259900-0", "2008-11-05 09:22:59.313+01",
                "2009-03-23 15:34:44.462776+01"));
        allDataSets.add(new DataSetInfo("20081105092259900-1", "2008-11-05 09:22:59.313+01",
                "2009-03-23 15:34:44.462776+01"));
        allDataSets.add(new DataSetInfo("20081105092359990-2", "2008-11-05 09:22:59.313+01",
                "2009-03-23 15:34:44.462776+01"));
        allDataSets.add(new DataSetInfo("20110509092359990-10", "2011-05-09 10:22:59.313+02",
                "2011-05-09 16:34:44.462776+02"));
        allDataSets.add(new DataSetInfo("20110509092359990-11", "2011-05-09 10:22:59.313+02",
                "2011-05-09 16:34:44.462776+02"));
        allDataSets.add(new DataSetInfo("20110509092359990-12", "2011-05-09 10:22:59.313+02",
                "2011-05-09 16:34:44.462776+02"));
        allDataSets.add(new DataSetInfo("20110805092359990-17", "2009-02-09 12:21:47.815468+01",
                "2009-03-23 15:34:44.462776+01"));
        allDataSets.add(new DataSetInfo("20081105092259000-18", "2008-11-05 09:22:59.313+01",
                "2009-03-23 15:34:44.462776+01"));
        allDataSets.add(new DataSetInfo("20081105092259000-19", "2008-11-05 09:22:59.313+01",
                "2009-03-23 15:34:44.462776+01"));
        allDataSets.add(new DataSetInfo("20081105092259000-20", "2008-11-05 09:22:59.313+01",
                "2009-03-23 15:34:44.462776+01"));
        allDataSets.add(new DataSetInfo("20081105092259000-21", "2008-11-05 09:22:59.313+01",
                "2009-03-23 15:34:44.462776+01"));

    }

    private static class DataSetInfo
    {
        private String code;

        private String registrationDate;

        private String modificationDate;

        public DataSetInfo(String code, String registrationDate, String modificationDate)
        {
            this.code = code;
            this.registrationDate = registrationDate;
            this.modificationDate = modificationDate;
        }

        public String getCode()
        {
            return this.code;
        }

        public String getRegistrationDate()
        {
            return registrationDate;
        }

        public String getModificationDate()
        {
            return modificationDate;
        }

        @Override
        public String toString()
        {
            return this.code;
        }

    }
}
