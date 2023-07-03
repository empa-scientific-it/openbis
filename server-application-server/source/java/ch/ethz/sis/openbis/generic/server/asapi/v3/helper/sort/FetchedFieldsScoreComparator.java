/*
 * Copyright ETH 2018 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sort;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.SortParameter;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ICodeHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IEntityTypeHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPermIdHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPropertiesHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractDateObjectValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractDateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractEntitySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AnyFieldSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AnyPropertySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.CodeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.DatePropertySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.IDate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.NumberPropertySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.PermIdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringPropertySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.search.AbstractEntityTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.Material;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.MaterialTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleTypeSearchCriteria;

/**
 * @author juanf
 */
public class FetchedFieldsScoreComparator<OBJECT extends IEntityTypeHolder & IPropertiesHolder & ICodeHolder & IPermIdHolder>
        extends AbstractComparator<OBJECT, Integer>
{
    private AbstractEntitySearchCriteria criteria;

    private List<Pattern> partialMatchTerms = new ArrayList<Pattern>();

    private List<String> exactMatchTerms = new ArrayList<String>();

    private List<Boost> boosts = new ArrayList<Boost>();

    private Map<OBJECT, Integer> scoreCache = new HashMap<>();

    private Integer fullCodeBoost = 0;

    private Integer partialCodeBoost = 0;

    private Integer fullPropertyBoost = 0;

    private Integer fullTypeBoost = 0;

    private Integer partialPropertyBoost = 0;

    public FetchedFieldsScoreComparator(Map<SortParameter, String> parameters, ISearchCriteria criteria)
    {
        if (criteria == null || (criteria instanceof AbstractEntitySearchCriteria) == false)
        {
            throw new IllegalArgumentException("Missing criteria");
        }

        if (parameters == null)
        {
            throw new IllegalArgumentException("Missing score parameters");
        } else
        {
            if (parameters.containsKey(SortParameter.FULL_MATCH_CODE_BOOST))
            {
                fullCodeBoost = Integer.parseInt(parameters.get(SortParameter.FULL_MATCH_CODE_BOOST));
            }
            if (parameters.containsKey(SortParameter.PARTIAL_MATCH_CODE_BOOST))
            {
                partialCodeBoost = Integer.parseInt(parameters.get(SortParameter.PARTIAL_MATCH_CODE_BOOST));
            }
            if (parameters.containsKey(SortParameter.FULL_MATCH_PROPERTY_BOOST))
            {
                fullPropertyBoost = Integer.parseInt(parameters.get(SortParameter.FULL_MATCH_PROPERTY_BOOST));
            }
            if (parameters.containsKey(SortParameter.FULL_MATCH_TYPE_BOOST))
            {
                fullTypeBoost = Integer.parseInt(parameters.get(SortParameter.FULL_MATCH_TYPE_BOOST));
            }
            if (parameters.containsKey(SortParameter.PARTIAL_MATCH_PROPERTY_BOOST))
            {
                partialPropertyBoost = Integer.parseInt(parameters.get(SortParameter.PARTIAL_MATCH_PROPERTY_BOOST));
            }
        }

        this.criteria = (AbstractEntitySearchCriteria) criteria;

        // Shared
        this.partialMatchTerms = new ArrayList<Pattern>();
        this.exactMatchTerms = new ArrayList<String>();
        this.boosts = new ArrayList<Boost>();

        for (ISearchCriteria subCriteria : this.criteria.getCriteria())
        {
            ISearchCriteriaParser<ISearchCriteria> parser = criteriaParsers.get(subCriteria.getClass());

            if (parser != null)
            {
                String value = parser.getValue(subCriteria);
                if (value == null)
                {
                    continue;
                }
                // Full Index
                partialMatchTerms.add(getPartialMatchTerm(value));
                exactMatchTerms.add(getExactMatchTerm(value));
                boosts.add(parser.getBoost(subCriteria, 10));

                // Split Index
                String[] splitIndexes = value.replace("*", " ").replace("?", " ").replaceAll("\\s+", " ").trim().split(" ");

                for (String splitIndex : splitIndexes)
                {
                    partialMatchTerms.add(getPartialMatchTerm(splitIndex));
                    exactMatchTerms.add(getExactMatchTerm(splitIndex));
                    boosts.add(parser.getBoost(subCriteria, 1));
                }
            }

        }

    }

    @Override
    public int compare(OBJECT o1, OBJECT o2)
    {
        return -1 * super.compare(o1, o2); // Higher scores first
    }

    @Override
    protected Integer getValue(OBJECT o)
    {
        Integer score = scoreCache.get(o);
        if (score == null)
        {
            score = calculateScore(o);
            scoreCache.put(o, score);
        }
        return score;
    }

    //
    // V3 Helper Methods
    //

    private static Map<Class, ISearchCriteriaParser> criteriaParsers = new HashMap<>(3);

    static
    {
        criteriaParsers.put(CodeSearchCriteria.class, new CodeSearchCriteriaParser());
        criteriaParsers.put(PermIdSearchCriteria.class, new PermIdSearchCriteriaParser());
        criteriaParsers.put(StringPropertySearchCriteria.class, new StringPropertySearchCriteriaParser());
        criteriaParsers.put(AnyFieldSearchCriteria.class, new AnyFieldSearchCriteriaParser());
        criteriaParsers.put(AnyPropertySearchCriteria.class, new AnyPropertySearchCriteriaParser());
        criteriaParsers.put(NumberPropertySearchCriteria.class, new NumberPropertySearchCriteriaParser());
        criteriaParsers.put(DatePropertySearchCriteria.class, new DatePropertySearchCriteriaParser());
        criteriaParsers.put(SampleTypeSearchCriteria.class, new AbstractEntityTypeSearchCriteriaParser());
        criteriaParsers.put(ExperimentTypeSearchCriteria.class, new AbstractEntityTypeSearchCriteriaParser());
        criteriaParsers.put(DataSetTypeSearchCriteria.class, new AbstractEntityTypeSearchCriteriaParser());
        criteriaParsers.put(MaterialTypeSearchCriteria.class, new AbstractEntityTypeSearchCriteriaParser());
    }

    private static interface ISearchCriteriaParser<ISearchCriteria>
    {
        public String getValue(ISearchCriteria criteria);

        public Boost getBoost(ISearchCriteria criteria, int boost);
    }

    private static class CodeSearchCriteriaParser implements ISearchCriteriaParser<CodeSearchCriteria>
    {
        @Override
        public String getValue(CodeSearchCriteria criteria)
        {
            return criteria.getFieldValue().getValue();
        }

        @Override
        public Boost getBoost(CodeSearchCriteria criteria, int boost)
        {
            return new Boost(boost, 0, 0, 0, null);
        }
    }

    private static class PermIdSearchCriteriaParser implements ISearchCriteriaParser<PermIdSearchCriteria>
    {
        @Override
        public String getValue(PermIdSearchCriteria criteria)
        {
            return criteria.getFieldValue().getValue();
        }

        @Override
        public Boost getBoost(PermIdSearchCriteria criteria, int boost)
        {
            return new Boost(boost, 0, 0, 0, null);
        }
    }

    private static class StringPropertySearchCriteriaParser implements ISearchCriteriaParser<StringPropertySearchCriteria>
    {
        @Override
        public String getValue(StringPropertySearchCriteria criteria)
        {
            return criteria.getFieldValue().getValue();
        }

        @Override
        public Boost getBoost(StringPropertySearchCriteria criteria, int boost)
        {
            return new Boost(0, 0, 0, boost, criteria.getFieldName());
        }
    }

    private static class NumberPropertySearchCriteriaParser implements ISearchCriteriaParser<NumberPropertySearchCriteria>
    {
        @Override
        public String getValue(NumberPropertySearchCriteria criteria)
        {
            return criteria.getFieldValue().getValue().toString();
        }

        @Override
        public Boost getBoost(NumberPropertySearchCriteria criteria, int boost)
        {
            return new Boost(0, 0, 0, boost, criteria.getFieldName());
        }
    }

    private static class DatePropertySearchCriteriaParser implements ISearchCriteriaParser<DatePropertySearchCriteria>
    {
        @Override
        public String getValue(DatePropertySearchCriteria criteria)
        {
            IDate date = criteria.getFieldValue();
            if (date instanceof AbstractDateObjectValue)
            {
                return ((AbstractDateObjectValue) date).getValue().toString();
            }

            if (date instanceof AbstractDateValue)
            {
                return ((AbstractDateValue) date).getValue();
            }

            return null;
        }

        @Override
        public Boost getBoost(DatePropertySearchCriteria criteria, int boost)
        {
            return new Boost(0, 0, 0, boost, criteria.getFieldName());
        }
    }

    private static class AnyFieldSearchCriteriaParser implements ISearchCriteriaParser<AnyFieldSearchCriteria>
    {
        @Override
        public String getValue(AnyFieldSearchCriteria criteria)
        {
            return criteria.getFieldValue().getValue();
        }

        @Override
        public Boost getBoost(AnyFieldSearchCriteria criteria, int boost)
        {
            return new Boost(boost, boost, boost, boost, null);
        }
    }

    private static class AnyPropertySearchCriteriaParser implements ISearchCriteriaParser<AnyPropertySearchCriteria>
    {
        @Override
        public String getValue(AnyPropertySearchCriteria criteria)
        {
            return criteria.getFieldValue().getValue();
        }

        @Override
        public Boost getBoost(AnyPropertySearchCriteria criteria, int boost)
        {
            return new Boost(boost, boost, boost, boost, criteria.getFieldName());
        }
    }

    private static class AbstractEntityTypeSearchCriteriaParser implements ISearchCriteriaParser<AbstractEntityTypeSearchCriteria>
    {
        @Override
        public String getValue(AbstractEntityTypeSearchCriteria criteria)
        {
            for (ISearchCriteria subCriteria : criteria.getCriteria())
            {
                if (subCriteria instanceof CodeSearchCriteria)
                {
                    return criteriaParsers.get(subCriteria.getClass()).getValue(subCriteria);
                } else if (subCriteria instanceof PermIdSearchCriteria)
                {
                    return criteriaParsers.get(subCriteria.getClass()).getValue(subCriteria);
                }
            }
            return null;
        }

        @Override
        public Boost getBoost(AbstractEntityTypeSearchCriteria criteria, int boost)
        {
            return new Boost(0, boost, 0, 0, null);
        }
    }

    //
    // Scoring algorithm
    //

    private boolean hasType(OBJECT o)
    {
        if (o instanceof Sample)
        {
            return ((Sample) o).getFetchOptions().hasType();
        } else if (o instanceof Experiment)
        {
            return ((Experiment) o).getFetchOptions().hasType();
        }
        if (o instanceof DataSet)
        {
            return ((DataSet) o).getFetchOptions().hasType();
        }
        if (o instanceof Material)
        {
            return ((Material) o).getFetchOptions().hasType();
        } else
        {
            return false;
        }
    }

    private boolean hasProperties(OBJECT o)
    {
        if (o instanceof Sample)
        {
            return ((Sample) o).getFetchOptions().hasProperties();
        } else if (o instanceof Experiment)
        {
            return ((Experiment) o).getFetchOptions().hasProperties();
        }
        if (o instanceof DataSet)
        {
            return ((DataSet) o).getFetchOptions().hasProperties();
        }
        if (o instanceof Material)
        {
            return ((Material) o).getFetchOptions().hasProperties();
        } else
        {
            return false;
        }
    }

    private Integer calculateScore(OBJECT o)
    {
        int score = 0;
        String code = o.getCode();
        String typeCode = (hasType(o)) ? o.getType().getCode() : null;
        Map<String, Serializable> properties = (hasProperties(o)) ? o.getProperties() : null;

        for (int i = 0; i < exactMatchTerms.size(); i++)
        {
            Pattern partialTerm = partialMatchTerms.get(i);
            String exactTerm = exactMatchTerms.get(i);
            Boost boost = boosts.get(i);

            // 1. Code
            if (code != null && boost.getCodeBoost() > 0)
            {
                if (isPartialMatch(code, partialTerm))
                { // If code matches partially
                    score += partialCodeBoost * boost.getCodeBoost();
                    if (isExactMatch(code, exactTerm))
                    { // If code matches exactly
                        score += fullCodeBoost * boost.getCodeBoost();
                    }
                }
            }

            // 2. Entity type code
            if (typeCode != null && boost.getTypeCodeBoost() > 0)
            {
                if (isExactMatch(typeCode, exactTerm))
                { // If type matches exactly
                    score += fullTypeBoost * boost.getTypeCodeBoost();
                }
            }

            // 3. Properties
            if (properties != null)
            {
                if (properties != null && properties.keySet() != null)
                {
                    for (String propertykey : properties.keySet())
                    {
                        if (boost.getPropertyBoost(propertykey) > 0)
                        {
                            String propertyValue = (String) properties.get(propertykey);
                            if (isPartialMatch(propertyValue, partialTerm))
                            { // If property matches partially
                                score += partialPropertyBoost * boost.getPropertyBoost(propertykey);
                                if (isExactMatch(propertyValue, exactTerm))
                                { // If property matches exactly
                                    score += fullPropertyBoost * boost.getPropertyBoost(propertykey);
                                }
                            }
                        }
                    }
                }
            }
        }

        return score;
    }

    //
    // Helper Methods
    //

    private static class Boost
    {
        private int codeBoost;

        private int typeCodeBoost;

        private int propertyBoost;

        private int propertyDefaultBoost;

        private String propertyName;

        public Boost(int codeBoost, int typeCodeBoost, int propertyDefaultBoost, int propertyBoost, String propertyName)
        {
            super();
            this.codeBoost = codeBoost;
            this.typeCodeBoost = typeCodeBoost;
            this.propertyDefaultBoost = propertyDefaultBoost;
            this.propertyBoost = propertyBoost;
            this.propertyName = propertyName;
        }

        public int getCodeBoost()
        {
            return codeBoost;
        }

        public int getTypeCodeBoost()
        {
            return typeCodeBoost;
        }

        public int getPropertyBoost(String propertyNameToBoost)
        {
            if (this.propertyName != null && this.propertyName.equals(propertyNameToBoost))
            {
                return propertyBoost;
            } else
            {
                return propertyDefaultBoost;
            }
        }

    }

    private Pattern getPartialMatchTerm(String term)
    {
        return Pattern.compile(("*" + term + "*").replace("*", ".*").replace("?", ".?"), Pattern.CASE_INSENSITIVE);
    }

    private String getExactMatchTerm(String term)
    {
        return term.replace("*", "").replace("?", "");
    }

    private boolean isExactMatch(String value, String term)
    {
        if (value != null && term != null)
        {
            return value.equalsIgnoreCase(term);
        } else
        {
            return false;
        }
    }

    private boolean isPartialMatch(String value, Pattern pattern)
    {
        if (value != null && pattern != null)
        {
            return pattern.matcher(value).matches();
        } else
        {
            return false;
        }
    }
}
