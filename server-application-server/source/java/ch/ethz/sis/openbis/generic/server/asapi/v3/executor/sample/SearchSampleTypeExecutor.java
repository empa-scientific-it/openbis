/*
 * Copyright 2016 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.sample;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.ListableSampleTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.search.SemanticAnnotationSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.Matcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractSearchEntityTypeExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.semanticannotation.ISearchSemanticAnnotationExecutor;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SemanticAnnotationPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class SearchSampleTypeExecutor extends AbstractSearchEntityTypeExecutor<SampleTypeSearchCriteria, SampleTypePE>
        implements ISearchSampleTypeExecutor
{

    @Autowired
    private ISampleTypeAuthorizationExecutor authorizationExecutor;

    @Autowired
    private ISearchSemanticAnnotationExecutor searchSemanticAnnotationExecutor;

    @Override
    public List<SampleTypePE> search(IOperationContext context, SampleTypeSearchCriteria criteria)
    {
        authorizationExecutor.canSearch(context);
        return super.search(context, criteria);
    }

    public SearchSampleTypeExecutor()
    {
        super(EntityKind.SAMPLE);
    }

    @Override
    protected Matcher<SampleTypePE> getMatcher(ISearchCriteria criteria)
    {
        if (criteria instanceof ListableSampleTypeSearchCriteria)
        {
            return new ListableMatcher();
        } else if (criteria instanceof SemanticAnnotationSearchCriteria)
        {
            return new SemanticAnnotationMatcher();
        }
        return super.getMatcher(criteria);
    }

    private static final class ListableMatcher extends Matcher<SampleTypePE>
    {
        @Override
        public List<SampleTypePE> getMatching(IOperationContext context, List<SampleTypePE> objects,
                ISearchCriteria criteria)
        {
            List<SampleTypePE> list = new ArrayList<>();
            boolean listable = ((ListableSampleTypeSearchCriteria) criteria).isListable();
            for (SampleTypePE entity : objects)
            {
                if (listable == entity.isListable())
                {
                    list.add(entity);
                }
            }
            return list;
        }
    }

    private class SemanticAnnotationMatcher extends Matcher<SampleTypePE>
    {
        @Override
        public List<SampleTypePE> getMatching(IOperationContext context, List<SampleTypePE> objects, ISearchCriteria criteria)
        {
            List<SemanticAnnotationPE> annotations =
                    searchSemanticAnnotationExecutor.search(context, (SemanticAnnotationSearchCriteria) criteria);

            Set<SampleTypePE> sampleTypesSet = new HashSet<SampleTypePE>(objects);
            Set<SampleTypePE> matches = new HashSet<SampleTypePE>();

            for (SemanticAnnotationPE annotation : annotations)
            {
                if (annotation.getSampleType() != null && sampleTypesSet.contains(annotation.getSampleType()))
                {
                    matches.add(annotation.getSampleType());
                }
            }

            return new ArrayList<SampleTypePE>(matches);
        }
    }

}
