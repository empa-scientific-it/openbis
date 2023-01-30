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

package ch.systemsx.cisd.openbis.generic.client.web.server.translator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.systemsx.cisd.common.collection.SimpleComparator;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetFileBlastSearchResultLocation;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetFileSearchResultLocation;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityPropertyBlastSearchResultLocation;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.ISearchDomainResultLocation;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchDomainSearchResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifierHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MatchingEntity;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyMatch;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SearchDomainSearchResultWithFullEntity;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Span;
import ch.systemsx.cisd.openbis.generic.shared.dto.SearchableEntity;

/**
 * A {@link SearchableEntity} &lt;---&gt; {@link ch.systemsx.cisd.openbis.generic.client.web.client.dto.SearchableEntity} translator.
 * 
 * @author Christian Ribeaud
 */
public final class SearchableEntityTranslator
{
    private static final Set<Character> CHARACTERS_TO_ESCAPE = new HashSet<>(
            Arrays.asList('[', ']', '{', '}', '(', ')', '|', '&', '.', '^', '$'));

    private SearchableEntityTranslator()
    {
        // Can not be instantiated.
    }

    // if null all possible entities are returned
    public final static SearchableEntity[] translate(
            final ch.systemsx.cisd.openbis.generic.client.web.client.dto.SearchableEntity searchableEntityOrNull)
    {
        SearchableEntity[] matchingEntities = {};
        if (searchableEntityOrNull == null)
        {
            matchingEntities = SearchableEntity.values();
        } else
        {
            if (searchableEntityOrNull.getType() == ch.systemsx.cisd.openbis.generic.client.web.client.dto.Type.ENTITY)
            {
                matchingEntities = new SearchableEntity[] { SearchableEntity.valueOf(searchableEntityOrNull.getName()) };
            }
        }
        return matchingEntities;
    }

    public final static List<MatchingEntity> translateSearchDomains(
            List<SearchDomainSearchResultWithFullEntity> searchDomains, String searchString)
    {
        List<MatchingEntity> translatedEntities = new ArrayList<MatchingEntity>();
        for (SearchDomainSearchResultWithFullEntity searchDomain : searchDomains)
        {
            MatchingEntity matchingEntity = new MatchingEntity();
            IEntityInformationHolderWithPermId entity = searchDomain.getEntity();
            matchingEntity.setCode(entity.getCode());
            matchingEntity.setEntityKind(entity.getEntityKind());
            matchingEntity.setEntityType(entity.getEntityType());
            matchingEntity.setId(entity.getId());
            matchingEntity.setPermId(entity.getPermId());
            SearchDomainSearchResult searchResult = searchDomain.getSearchResult();
            matchingEntity.setSearchDomain(searchResult.getSearchDomain().getLabel());

            if (entity instanceof IIdentifierHolder)
            {
                matchingEntity.setIdentifier(((IIdentifierHolder) entity).getIdentifier());
            }
            if (searchResult.getScore() != null)
            {
                matchingEntity.setScore(searchResult.getScore().getScore());
            }

            ISearchDomainResultLocation resultLocation = searchResult.getResultLocation();
            List<PropertyMatch> matches = createMatches(searchString, resultLocation);
            matchingEntity.setMatches(matches);
            translatedEntities.add(matchingEntity);
        }
        Collections.sort(translatedEntities, new SimpleComparator<MatchingEntity, Double>()
            {
                @Override
                public Double evaluate(MatchingEntity item)
                {
                    return -item.getScore();
                }
            });
        return translatedEntities;
    }

    private static List<PropertyMatch> createMatches(String searchString, ISearchDomainResultLocation resultLocation)
    {
        List<PropertyMatch> matches = new ArrayList<PropertyMatch>();
        PropertyMatch match = new PropertyMatch();
        if (resultLocation instanceof DataSetFileBlastSearchResultLocation)
        {
            DataSetFileBlastSearchResultLocation fileBlastLocation = (DataSetFileBlastSearchResultLocation) resultLocation;
            match.setCode("File '" + fileBlastLocation.getPathInDataSet() + "'");
            match.setValue(fileBlastLocation.getAlignmentMatch().toString());
            matches.add(match);
        } else if (resultLocation instanceof EntityPropertyBlastSearchResultLocation)
        {
            EntityPropertyBlastSearchResultLocation entityBlastLocation = (EntityPropertyBlastSearchResultLocation) resultLocation;
            match.setCode("Property '" + entityBlastLocation.getPropertyType() + "'");
            match.setValue(entityBlastLocation.getAlignmentMatch().toString());
            matches.add(match);
        } else if (resultLocation instanceof DataSetFileSearchResultLocation)
        {
            match.setValue(((DataSetFileSearchResultLocation) resultLocation).getPathInDataSet());
            String pathString = ((DataSetFileSearchResultLocation) resultLocation).getPathInDataSet();
            List<Span> spans = createSpanList(searchString, pathString);
            match.setSpans(spans);
            matches.add(match);
        } else
        {
            match.setValue(((DataSetFileSearchResultLocation) resultLocation).toString());
            matches.add(match);
        }
        return matches;
    }

    static List<Span> createSpanList(String searchString, String filePath)
    {
        List<SearchStringElement> splittedSearchString = split("*" + searchString + "*");
        Pattern pattern = compile(splittedSearchString);
        String normalizedFilePath = filePath.toLowerCase();
        Matcher matcher = pattern.matcher(normalizedFilePath);
        List<Span> spans = new ArrayList<Span>();
        if (matcher.matches())
        {
            int groupCount = matcher.groupCount();
            int startIndex = 0;
            for (int i = 1; i <= groupCount; i++)
            {
                String group = matcher.group(i);
                if (splittedSearchString.get(i - 1).constant)
                {
                    Span span = new Span();
                    span.setStart(startIndex);
                    span.setEnd(startIndex + group.length());
                    spans.add(span);
                }
                startIndex += group.length();
            }
        }
        return spans;
    }
    
    private static List<SearchStringElement> split(String searchString)
    {
        boolean previousWasWildcard = true;
        List<SearchStringElement> result = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < searchString.length(); i++)
        {
            char c = Character.toLowerCase(searchString.charAt(i));
            if (c == '*' || c == '?')
            {
                if (previousWasWildcard == false)
                {
                    result.add(new SearchStringElement(builder.toString()));
                    builder.setLength(0);
                }
                builder.append(c);
                previousWasWildcard = true;
            } else
            {
                if (previousWasWildcard)
                {
                    result.add(new SearchStringElement(builder.toString()));
                    builder.setLength(0);
                }
                builder.append(c);
                previousWasWildcard = false;
            }
        }
        if (builder.length() > 0)
        {
            result.add(new SearchStringElement(builder.toString()));
        }
        return result;
    }
    
    private static Pattern compile(List<SearchStringElement> elements)
    {
        StringBuilder builder = new StringBuilder();
        for (SearchStringElement searchStringElement : elements)
        {
            builder.append('(').append(searchStringElement.text).append(')');
        }
        return Pattern.compile(builder.toString());
    }
    
    private static class SearchStringElement
    {
        private boolean constant;
        private String text;
        SearchStringElement(String text)
        {
            if (text.contains("*"))
            {
                this.text = ".*";
                constant = false;
            } else if (text.equals("?"))
            {
                this.text = ".";
                constant = false;
            } else
            {
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < text.length(); i++)
                {
                    char c = text.charAt(i);
                    if (CHARACTERS_TO_ESCAPE.contains(c))
                    {
                        builder.append('\\');
                    }
                    builder.append(c);
                }
                this.text = builder.toString();
                constant = true;
            }
        }
        
        @Override
        public String toString()
        {
            return text;
        }
    }
}
