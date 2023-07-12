/*
 * Copyright ETH 2013 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.server.api.v1.sort;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.server.business.search.sort.IEntitySearchResult;
import ch.systemsx.cisd.openbis.generic.server.business.search.sort.SearchResultSorterByScore;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SampleFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;

public class SampleSearchResultSorter
{

    private class SampleSearchResult implements IEntitySearchResult
    {
        private Sample sample;

        public SampleSearchResult(Sample sample)
        {
            this.sample = sample;
        }

        public Sample getSample()
        {
            return sample;
        }

        @Override
        public String getCode()
        {
            return sample.getCode();
        }

        @Override
        public Map<String, String> getProperties()
        {
            if (sample.getRetrievedFetchOptions().contains(SampleFetchOption.PROPERTIES))
            {
                return sample.getProperties();
            } else
            {
                return Collections.emptyMap();
            }
        }

        @Override
        public String getTypeCode()
        {
            return this.sample.getSampleTypeCode();
        }

        @Override
        public String toString()
        {
            return sample.getCode();
        }
    }

    public List<Sample> sort(List<Sample> samples, DetailedSearchCriteria criteria)
    {
        List<SampleSearchResult> samplesToSort = new ArrayList<SampleSearchResult>();
        for (Sample sample : samples)
        {
            samplesToSort.add(new SampleSearchResult(sample));
        }

        SearchResultSorterByScore sort = new SearchResultSorterByScore();
        sort.sort(samplesToSort, criteria);

        List<Sample> sortedSamples = new ArrayList<Sample>();
        for (SampleSearchResult sampleSearchResult : samplesToSort)
        {
            sortedSamples.add(sampleSearchResult.getSample());
        }
        return sortedSamples;
    }

}
