package ch.ethz.sis.openbis.systemtest.asapi.v3;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.ExternalDmsSearchCriteria;

public class SearchLinkDataSetTest extends AbstractLinkDataSetTest
{

    @Test
    void searchWithLegacyExternalCode()
    {
        DataSetSearchCriteria dc = new DataSetSearchCriteria();
        dc.withLinkedData().withExternalCode().thatEquals("CODE3");

        SearchResult<DataSet> result = searchWith(dc);

        assertThat(result.getTotalCount(), is(1));
        assertThat(result.getObjects().get(0).getCode(), is("20120628092259000-25"));
    }

    @Test
    void searchWithLegacyExternalDms()
    {
        DataSetSearchCriteria dc = new DataSetSearchCriteria().withAndOperator();
        ExternalDmsSearchCriteria externalDms = dc.withLinkedData().withExternalDms();
        externalDms.withCode().thatEquals("DMS_2");
        externalDms.withLabel().thatEquals("\"Test External openBIS instance\"");

        SearchResult<DataSet> result = searchWith(dc);

        assertThat(result.getTotalCount(), is(1));
        assertThat(result.getObjects().get(0).getCode(), is("20120628092259000-25"));
    }

    @Test
    void searchWithExternalDmsOfCopy()
    {
        DataSetSearchCriteria dc = new DataSetSearchCriteria().withAndOperator();
        ExternalDmsSearchCriteria externalDms = dc.withLinkedData().withCopy().withExternalDms();
        externalDms.withCode().thatEquals("DMS_2");
        externalDms.withLabel().thatEquals("\"Test External openBIS instance\"");

        SearchResult<DataSet> result = searchWith(dc);

        assertThat(result.getTotalCount(), is(1));
        assertThat(result.getObjects().get(0).getCode(), is("20120628092259000-25"));
    }

    @Test
    void searchWithExternalCodeOfCopy()
    {
        DataSetSearchCriteria dc = new DataSetSearchCriteria();
        dc.withLinkedData().withCopy().withExternalCode().thatEquals("CODE3");

        SearchResult<DataSet> result = searchWith(dc);

        assertThat(result.getTotalCount(), is(1));
        assertThat(result.getObjects().get(0).getCode(), is("20120628092259000-25"));
    }

    @Test
    void searchWithPathOfCopy()
    {
        DataSetSearchCriteria dc = new DataSetSearchCriteria();
        dc.withLinkedData().withCopy().withPath().thatEquals("path/to/20120628092259000-41");

        SearchResult<DataSet> result = searchWith(dc);

        assertThat(result.getTotalCount(), is(1));
        assertThat(result.getObjects().get(0).getCode(), is("20120628092259000-41"));
    }

    @Test
    void searchWithCommitHashOfCopy()
    {
        DataSetSearchCriteria dc = new DataSetSearchCriteria();
        dc.withLinkedData().withCopy().withGitCommitHash().thatEquals("abcdefg12345678");

        SearchResult<DataSet> result = searchWith(dc);

        assertThat(result.getTotalCount(), is(1));
        assertThat(result.getObjects().get(0).getCode(), is("20120628092259000-41"));
    }

    @Test
    void searchWithRepositoryIdOfCopy()
    {
        DataSetSearchCriteria dc = new DataSetSearchCriteria();
        dc.withLinkedData().withCopy().withGitRepositoryId().thatEquals("repo1");

        SearchResult<DataSet> result = searchWith(dc);

        assertThat(result.getTotalCount(), is(1));
        assertThat(result.getObjects().get(0).getCode(), is("20120628092259000-41"));
    }

    @Test
    void testFetchOptionsWithLinkedData()
    {
        DataSetSearchCriteria dc = new DataSetSearchCriteria();

        dc.withLinkedData().withCopy().withPath().thatContains("to");
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withLinkedData();

        SearchResult<DataSet> result = searchWith(dc, fetchOptions);

        assertThat(result.getTotalCount(), is(1));

        DataSet dataSet = result.getObjects().get(0);

        assertThat(dataSet.getCode(), is("20120628092259000-41"));
        assertThat(dataSet.getLinkedData().getContentCopies().get(0).getExternalDms(), is(nullValue()));
        assertThat(dataSet.getLinkedData().getExternalDms(), is(nullValue()));
    }

    @Test
    void testFetchOptionsWithExternalDms()
    {
        DataSetSearchCriteria dc = new DataSetSearchCriteria();

        dc.withLinkedData().withCopy().withPath().thatContains("to");
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withLinkedData().withExternalDms();

        SearchResult<DataSet> result = searchWith(dc, fetchOptions);

        assertThat(result.getTotalCount(), is(1));

        DataSet dataSet = result.getObjects().get(0);

        assertThat(result.getObjects().get(0).getCode(), is("20120628092259000-41"));
        assertThat(dataSet.getLinkedData().getContentCopies().get(0).getExternalDms(), is(not(nullValue())));
        // should be null with this dataset, as it is on file system (this is a legacy method)
        assertThat(dataSet.getLinkedData().getExternalDms(), is(nullValue()));
    }

    @Test(enabled = false)
    void searchAllLinkDataSetsThatHaveAtLeastOneCopy()
    {
        DataSetSearchCriteria dc = new DataSetSearchCriteria();
        dc.withLinkedData().withCopy();

        SearchResult<DataSet> result = searchWith(dc);

        assertThat(result.getTotalCount(), is(3));
    }

    @Test(enabled = false)
    void searchAllLinkDataSets()
    {
        DataSetSearchCriteria dc = new DataSetSearchCriteria();
        dc.withLinkedData();

        SearchResult<DataSet> result = searchWith(dc);

        assertThat(result.getTotalCount(), is(3));
    }

    private SearchResult<DataSet> searchWith(DataSetSearchCriteria criteria)
    {
        return searchWith(criteria, new DataSetFetchOptions());
    }

    private SearchResult<DataSet> searchWith(DataSetSearchCriteria criteria, DataSetFetchOptions fetchOptions)
    {
        return v3api.searchDataSets(session, criteria, fetchOptions);
    }
}
