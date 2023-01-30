/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MetaprojectCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;

/**
 * Criteria for listing <i>samples</i> and displaying them in the grid.<br>
 * <br>
 * Used either for browsing or for searching.
 * 
 * @author Tomasz Pylak
 * @author Piotr Buczek
 */
public class ListSampleDisplayCriteria extends DefaultResultSetConfig<String, Sample> implements
        IsSerializable
{
    public static ListSampleDisplayCriteria createForContainer(final TechId containerSampleId)
    {
        return new ListSampleDisplayCriteria(
                ListSampleCriteria.createForContainer(containerSampleId));
    }

    public static ListSampleDisplayCriteria createForParent(final TechId parentSampleId)
    {
        return new ListSampleDisplayCriteria(ListSampleCriteria.createForParent(parentSampleId));
    }

    public static ListSampleDisplayCriteria createForChild(final TechId childSampleId)
    {
        return new ListSampleDisplayCriteria(ListSampleCriteria.createForChild(childSampleId));
    }

    public static ListSampleDisplayCriteria createForExperiment(final TechId experimentId)
    {
        return new ListSampleDisplayCriteria(ListSampleCriteria.createForExperiment(experimentId));
    }

    public static ListSampleDisplayCriteria createForMetaproject(
            final MetaprojectCriteria criteria)
    {
        return new ListSampleDisplayCriteria(criteria);
    }

    public static ListSampleDisplayCriteria createForSearch()
    {
        return createForSearch(new DetailedSearchCriteria());
    }

    private static ListSampleDisplayCriteria createForSearch(
            final DetailedSearchCriteria searchCriteria)
    {
        return new ListSampleDisplayCriteria(searchCriteria);
    }

    private ListEntityDisplayCriteriaKind criteriaKind;

    // artificial 'all' SampleType
    private SampleType allSampleTypeOrNull;

    // either search criteria or list criteria or metaproject criteria is set
    private DetailedSearchCriteria searchCriteriaOrNull;

    private ListSampleCriteria listCriteriaOrNull;

    private MetaprojectCriteria metaprojectCriteriaOrNull;

    private ListSampleDisplayCriteria(final DetailedSearchCriteria searchCriteria)
    {
        assert searchCriteria != null : "search criteria not set";
        this.criteriaKind = ListEntityDisplayCriteriaKind.SEARCH;
        this.setSearchCriteriaOrNull(searchCriteria);
    }

    public ListSampleDisplayCriteria(final ListSampleCriteria listCriteria)
    {
        assert listCriteria != null : "list criteria not set";
        this.criteriaKind = ListEntityDisplayCriteriaKind.BROWSE;
        this.setListCriteriaOrNull(listCriteria);
    }

    public ListSampleDisplayCriteria(final MetaprojectCriteria metaprojectCriteria)
    {
        assert metaprojectCriteria != null : "metaproject criteria not set";
        this.criteriaKind = ListEntityDisplayCriteriaKind.METAPROJECT;
        this.setMetaprojectCriteriaOrNull(metaprojectCriteria);
    }

    public ListEntityDisplayCriteriaKind getCriteriaKind()
    {
        return criteriaKind;
    }

    public ListSampleCriteria getBrowseCriteria()
    {
        assert getCriteriaKind() == ListEntityDisplayCriteriaKind.BROWSE : "not a browse criteria";
        return getListCriteriaOrNull();
    }

    public DetailedSearchCriteria getSearchCriteria()
    {
        assert getCriteriaKind() == ListEntityDisplayCriteriaKind.SEARCH : "not a search criteria";
        return getSearchCriteriaOrNull();
    }

    public MetaprojectCriteria getMetaprojectCriteria()
    {
        assert getCriteriaKind() == ListEntityDisplayCriteriaKind.METAPROJECT : "not a metaproject criteria";
        return getMetaprojectCriteriaOrNull();
    }

    public void updateSearchCriteria(final DetailedSearchCriteria newSearchCriteria)
    {
        assert getCriteriaKind() == ListEntityDisplayCriteriaKind.SEARCH : "not a search criteria";
        assert newSearchCriteria != null : "new search criteria not set";
        setSearchCriteriaOrNull(newSearchCriteria);
    }

    private ListSampleCriteria getListCriteriaOrNull()
    {
        return listCriteriaOrNull;
    }

    private void setListCriteriaOrNull(ListSampleCriteria listCriteriaOrNull)
    {
        this.listCriteriaOrNull = listCriteriaOrNull;
    }

    private DetailedSearchCriteria getSearchCriteriaOrNull()
    {
        return searchCriteriaOrNull;
    }

    private void setSearchCriteriaOrNull(DetailedSearchCriteria searchCriteriaOrNull)
    {
        this.searchCriteriaOrNull = searchCriteriaOrNull;
    }

    public MetaprojectCriteria getMetaprojectCriteriaOrNull()
    {
        return metaprojectCriteriaOrNull;
    }

    public void setMetaprojectCriteriaOrNull(MetaprojectCriteria metaprojectCriteriaOrNull)
    {
        this.metaprojectCriteriaOrNull = metaprojectCriteriaOrNull;
    }

    public void setAllSampleType(SampleType sampleType)
    {
        this.allSampleTypeOrNull = sampleType;
    }

    public SampleType tryGetSampleType()
    {
        if (allSampleTypeOrNull != null)
        {
            return allSampleTypeOrNull;
        } else
        {
            return listCriteriaOrNull != null ? listCriteriaOrNull.getSampleType() : null;
        }
    }

    //
    // GWT only
    //
    @SuppressWarnings("unused")
    private ListSampleDisplayCriteria()
    {
    }

}
