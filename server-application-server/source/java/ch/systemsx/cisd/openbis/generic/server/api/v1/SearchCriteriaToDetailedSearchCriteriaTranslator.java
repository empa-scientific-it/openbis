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

package ch.systemsx.cisd.openbis.generic.server.api.v1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityPropertyTypeDAO;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.AttributeMatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseFieldType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseTimeAttribute;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.PropertyMatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.TimeAttributeMatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchSubCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchableEntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AssociatedEntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CompareType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetAttributeSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriterion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchField;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchSubCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentAttributeSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IAttributeSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialAttributeSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleAttributeSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SearchCriteriaConnection;
import ch.systemsx.cisd.openbis.generic.shared.translator.DtoConverters;

/**
 * Converts {@link SearchCriteria} objects to {@link DetailedSearchCriteria} objects.
 * <p>
 * Clients of this class need to provide a translator from {@link MatchClauseAttribute} to {@link IAttributeSearchFieldKind} appropriate to the entity
 * they are searching for.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class SearchCriteriaToDetailedSearchCriteriaTranslator
{
    private final static String INVALID_SEARCH_ATTRIBUTE_TEMPLATE =
            "%s is not a valid search attribute for %s";

    static Map<SearchableEntityKind, IMatchClauseAttributeTranslator> translators =
            new HashMap<SearchableEntityKind, IMatchClauseAttributeTranslator>();

    static
    {
        translators.put(SearchableEntityKind.EXPERIMENT, new ExperimentAttributeTranslator());
        translators.put(SearchableEntityKind.SAMPLE, new SampleAttributeTranslator());
        translators.put(SearchableEntityKind.DATA_SET, new DataSetAttributeTranslator());
        translators.put(SearchableEntityKind.DATA_SET_PARENT, new DataSetAttributeTranslator());
        translators.put(SearchableEntityKind.DATA_SET_CHILD, new DataSetAttributeTranslator());
        translators.put(SearchableEntityKind.DATA_SET_CONTAINER, new DataSetAttributeTranslator());
        translators.put(SearchableEntityKind.SAMPLE_PARENT, new SampleAttributeTranslator());
        translators.put(SearchableEntityKind.SAMPLE_CHILD, new SampleAttributeTranslator());
        translators.put(SearchableEntityKind.SAMPLE_CONTAINER, new SampleAttributeTranslator());
        translators.put(SearchableEntityKind.MATERIAL, new MaterialAttributeTranslator());
    }

    private static IMatchClauseAttributeTranslator translatorFor(SearchableEntityKind entityKind)
    {
        return translators.get(entityKind);
    }

    private static void throwInvalidSearchAttributeException(MatchClauseAttribute attribute,
            SearchableEntityKind entityKind) throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException(String.format(INVALID_SEARCH_ATTRIBUTE_TEMPLATE,
                attribute, entityKind));
    }

    private static void throwInvalidSearchAttributeException(MatchClauseTimeAttribute attribute,
            SearchableEntityKind entityKind) throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException(String.format(INVALID_SEARCH_ATTRIBUTE_TEMPLATE,
                attribute, entityKind));
    }

    private static AssociatedEntityKind convertToAssociatedEntityKind(
            SearchableEntityKind entityKind)
    {
        switch (entityKind)
        {
            case EXPERIMENT:
                return AssociatedEntityKind.EXPERIMENT;
            case DATA_SET:
                return AssociatedEntityKind.DATA_SET;
            case DATA_SET_CONTAINER:
                return AssociatedEntityKind.DATA_SET_CONTAINER;
            case DATA_SET_PARENT:
                return AssociatedEntityKind.DATA_SET_PARENT;
            case DATA_SET_CHILD:
                return AssociatedEntityKind.DATA_SET_CHILD;
            case SAMPLE:
                return AssociatedEntityKind.SAMPLE;
            case SAMPLE_CONTAINER:
                return AssociatedEntityKind.SAMPLE_CONTAINER;
            case SAMPLE_PARENT:
                return AssociatedEntityKind.SAMPLE_PARENT;
            case SAMPLE_CHILD:
                return AssociatedEntityKind.SAMPLE_CHILD;
            case MATERIAL:
                return AssociatedEntityKind.MATERIAL;
        }
        return null; // can't happen
    }

    private final IDAOFactory daoFactory;

    private final SearchCriteria searchCriteria;

    private final SearchableEntityKind entityKind;

    private final IMatchClauseAttributeTranslator attributeTranslator;

    private final DetailedSearchCriteria newDetailedSearchCriteria;

    /**
     * Interface for a translator from {@link MatchClauseAttribute} to {@link IAttributeSearchFieldKind}.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    public static interface IMatchClauseAttributeTranslator
    {
        IAttributeSearchFieldKind convertMatchClauseAttributeToAttributeSearchFieldKind(
                MatchClauseAttribute attribute);

        public IAttributeSearchFieldKind convertMatchClauseAttributeToAttributeSearchFieldKind(
                MatchClauseTimeAttribute attribute);
    }

    public static class SampleAttributeTranslator implements IMatchClauseAttributeTranslator
    {
        @Override
        public IAttributeSearchFieldKind convertMatchClauseAttributeToAttributeSearchFieldKind(
                MatchClauseAttribute attribute)
        {
            final IAttributeSearchFieldKind ans;
            switch (attribute)
            {
                case CODE:
                    ans = SampleAttributeSearchFieldKind.CODE;
                    break;
                case TYPE:
                    ans = SampleAttributeSearchFieldKind.SAMPLE_TYPE;
                    break;
                case PERM_ID:
                    ans = SampleAttributeSearchFieldKind.PERM_ID;
                    break;
                case SPACE:
                    ans = SampleAttributeSearchFieldKind.SPACE;
                    break;
                case METAPROJECT:
                    ans = SampleAttributeSearchFieldKind.METAPROJECT;
                    break;
                case REGISTRATOR_USER_ID:
                    ans = SampleAttributeSearchFieldKind.REGISTRATOR_USER_ID;
                    break;
                case REGISTRATOR_FIRST_NAME:
                    ans = SampleAttributeSearchFieldKind.REGISTRATOR_FIRST_NAME;
                    break;
                case REGISTRATOR_LAST_NAME:
                    ans = SampleAttributeSearchFieldKind.REGISTRATOR_LAST_NAME;
                    break;
                case REGISTRATOR_EMAIL:
                    ans = SampleAttributeSearchFieldKind.REGISTRATOR_EMAIL;
                    break;
                case MODIFIER_USER_ID:
                    ans = SampleAttributeSearchFieldKind.MODIFIER_USER_ID;
                    break;
                case MODIFIER_FIRST_NAME:
                    ans = SampleAttributeSearchFieldKind.MODIFIER_FIRST_NAME;
                    break;
                case MODIFIER_LAST_NAME:
                    ans = SampleAttributeSearchFieldKind.MODIFIER_LAST_NAME;
                    break;
                case MODIFIER_EMAIL:
                    ans = SampleAttributeSearchFieldKind.MODIFIER_EMAIL;
                    break;
                default:
                    throwInvalidSearchAttributeException(attribute, SearchableEntityKind.SAMPLE);
                    ans = null; // for Eclipse
            }
            return ans;
        }

        @Override
        public IAttributeSearchFieldKind convertMatchClauseAttributeToAttributeSearchFieldKind(
                MatchClauseTimeAttribute attribute)
        {
            final IAttributeSearchFieldKind ans;
            switch (attribute)
            {
                case REGISTRATION_DATE:
                    ans = SampleAttributeSearchFieldKind.REGISTRATION_DATE;
                    break;
                case MODIFICATION_DATE:
                    ans = SampleAttributeSearchFieldKind.MODIFICATION_DATE;
                    break;
                default:
                    throwInvalidSearchAttributeException(attribute, SearchableEntityKind.SAMPLE);
                    ans = null; // for Eclipse
            }
            return ans;
        }

    }

    public static class ExperimentAttributeTranslator implements IMatchClauseAttributeTranslator
    {
        @Override
        public IAttributeSearchFieldKind convertMatchClauseAttributeToAttributeSearchFieldKind(
                MatchClauseAttribute attribute)
        {
            IAttributeSearchFieldKind ans = null;
            switch (attribute)
            {
                case CODE:
                    ans = ExperimentAttributeSearchFieldKind.CODE;
                    break;
                case TYPE:
                    ans = ExperimentAttributeSearchFieldKind.EXPERIMENT_TYPE;
                    break;
                case PERM_ID:
                    ans = ExperimentAttributeSearchFieldKind.PERM_ID;
                    break;
                case SPACE:
                    ans = ExperimentAttributeSearchFieldKind.PROJECT_SPACE;
                    break;
                case PROJECT:
                    ans = ExperimentAttributeSearchFieldKind.PROJECT;
                    break;
                case PROJECT_PERM_ID:
                    ans = ExperimentAttributeSearchFieldKind.PROJECT_PERM_ID;
                    break;
                case METAPROJECT:
                    ans = ExperimentAttributeSearchFieldKind.METAPROJECT;
                    break;
                case REGISTRATOR_USER_ID:
                    ans = ExperimentAttributeSearchFieldKind.REGISTRATOR_USER_ID;
                    break;
                case REGISTRATOR_FIRST_NAME:
                    ans = ExperimentAttributeSearchFieldKind.REGISTRATOR_FIRST_NAME;
                    break;
                case REGISTRATOR_LAST_NAME:
                    ans = ExperimentAttributeSearchFieldKind.REGISTRATOR_LAST_NAME;
                    break;
                case REGISTRATOR_EMAIL:
                    ans = ExperimentAttributeSearchFieldKind.REGISTRATOR_EMAIL;
                    break;
                case MODIFIER_USER_ID:
                    ans = ExperimentAttributeSearchFieldKind.MODIFIER_USER_ID;
                    break;
                case MODIFIER_FIRST_NAME:
                    ans = ExperimentAttributeSearchFieldKind.MODIFIER_FIRST_NAME;
                    break;
                case MODIFIER_LAST_NAME:
                    ans = ExperimentAttributeSearchFieldKind.MODIFIER_LAST_NAME;
                    break;
                case MODIFIER_EMAIL:
                    ans = ExperimentAttributeSearchFieldKind.MODIFIER_EMAIL;
                    break;
                default:
                    throwInvalidSearchAttributeException(attribute, SearchableEntityKind.EXPERIMENT);
            }
            return ans;
        }

        @Override
        public IAttributeSearchFieldKind convertMatchClauseAttributeToAttributeSearchFieldKind(
                MatchClauseTimeAttribute attribute)
        {
            IAttributeSearchFieldKind ans = null;
            switch (attribute)
            {
                case REGISTRATION_DATE:
                    ans = ExperimentAttributeSearchFieldKind.REGISTRATION_DATE;
                    break;
                case MODIFICATION_DATE:
                    ans = ExperimentAttributeSearchFieldKind.MODIFICATION_DATE;
                    break;
                default:
                    throwInvalidSearchAttributeException(attribute, SearchableEntityKind.EXPERIMENT);
            }
            return ans;
        }

    }

    public static class MaterialAttributeTranslator implements IMatchClauseAttributeTranslator
    {
        @Override
        public IAttributeSearchFieldKind convertMatchClauseAttributeToAttributeSearchFieldKind(
                MatchClauseAttribute attribute)
        {
            IAttributeSearchFieldKind ans = null;
            switch (attribute)
            {
                case CODE:
                    ans = MaterialAttributeSearchFieldKind.CODE;
                    break;
                case TYPE:
                    ans = MaterialAttributeSearchFieldKind.MATERIAL_TYPE;
                    break;
                case METAPROJECT:
                    ans = MaterialAttributeSearchFieldKind.METAPROJECT;
                    break;
                case REGISTRATOR_USER_ID:
                    ans = MaterialAttributeSearchFieldKind.REGISTRATOR_USER_ID;
                    break;
                case REGISTRATOR_FIRST_NAME:
                    ans = MaterialAttributeSearchFieldKind.REGISTRATOR_FIRST_NAME;
                    break;
                case REGISTRATOR_LAST_NAME:
                    ans = MaterialAttributeSearchFieldKind.REGISTRATOR_LAST_NAME;
                    break;
                case REGISTRATOR_EMAIL:
                    ans = MaterialAttributeSearchFieldKind.REGISTRATOR_EMAIL;
                    break;
                case MODIFIER_USER_ID:
                    ans = MaterialAttributeSearchFieldKind.MODIFIER_USER_ID;
                    break;
                case MODIFIER_FIRST_NAME:
                    ans = MaterialAttributeSearchFieldKind.MODIFIER_FIRST_NAME;
                    break;
                case MODIFIER_LAST_NAME:
                    ans = MaterialAttributeSearchFieldKind.MODIFIER_LAST_NAME;
                    break;
                case MODIFIER_EMAIL:
                    ans = MaterialAttributeSearchFieldKind.MODIFIER_EMAIL;
                    break;
                default:
                    throwInvalidSearchAttributeException(attribute, SearchableEntityKind.MATERIAL);
            }
            return ans;
        }

        @Override
        public IAttributeSearchFieldKind convertMatchClauseAttributeToAttributeSearchFieldKind(
                MatchClauseTimeAttribute attribute)
        {
            IAttributeSearchFieldKind ans = null;
            switch (attribute)
            {
                case REGISTRATION_DATE:
                    ans = MaterialAttributeSearchFieldKind.REGISTRATION_DATE;
                    break;
                case MODIFICATION_DATE:
                    ans = MaterialAttributeSearchFieldKind.MODIFICATION_DATE;
                    break;
                default:
                    throwInvalidSearchAttributeException(attribute, SearchableEntityKind.MATERIAL);
            }
            return ans;
        }
    }

    public static class DataSetAttributeTranslator implements IMatchClauseAttributeTranslator
    {
        @Override
        public IAttributeSearchFieldKind convertMatchClauseAttributeToAttributeSearchFieldKind(
                MatchClauseAttribute attribute)
        {
            IAttributeSearchFieldKind ans = null;
            switch (attribute)
            {
                case CODE:
                case PERM_ID:
                    ans = DataSetAttributeSearchFieldKind.CODE;
                    break;
                case TYPE:
                    ans = DataSetAttributeSearchFieldKind.DATA_SET_TYPE;
                    break;
                case METAPROJECT:
                    ans = DataSetAttributeSearchFieldKind.METAPROJECT;
                    break;
                case REGISTRATOR_USER_ID:
                    ans = DataSetAttributeSearchFieldKind.REGISTRATOR_USER_ID;
                    break;
                case REGISTRATOR_FIRST_NAME:
                    ans = DataSetAttributeSearchFieldKind.REGISTRATOR_FIRST_NAME;
                    break;
                case REGISTRATOR_LAST_NAME:
                    ans = DataSetAttributeSearchFieldKind.REGISTRATOR_LAST_NAME;
                    break;
                case REGISTRATOR_EMAIL:
                    ans = DataSetAttributeSearchFieldKind.REGISTRATOR_EMAIL;
                    break;
                case MODIFIER_USER_ID:
                    ans = DataSetAttributeSearchFieldKind.MODIFIER_USER_ID;
                    break;
                case MODIFIER_FIRST_NAME:
                    ans = DataSetAttributeSearchFieldKind.MODIFIER_FIRST_NAME;
                    break;
                case MODIFIER_LAST_NAME:
                    ans = DataSetAttributeSearchFieldKind.MODIFIER_LAST_NAME;
                    break;
                case MODIFIER_EMAIL:
                    ans = DataSetAttributeSearchFieldKind.MODIFIER_EMAIL;
                    break;
                default:
                    throwInvalidSearchAttributeException(attribute, SearchableEntityKind.DATA_SET);
            }
            return ans;
        }

        @Override
        public IAttributeSearchFieldKind convertMatchClauseAttributeToAttributeSearchFieldKind(
                MatchClauseTimeAttribute attribute)
        {
            IAttributeSearchFieldKind ans = null;
            switch (attribute)
            {
                case REGISTRATION_DATE:
                    ans = DataSetAttributeSearchFieldKind.REGISTRATION_DATE;
                    break;
                case MODIFICATION_DATE:
                    ans = DataSetAttributeSearchFieldKind.MODIFICATION_DATE;
                    break;
                default:
                    throwInvalidSearchAttributeException(attribute, SearchableEntityKind.DATA_SET);
            }
            return ans;
        }

    }

    public static DetailedSearchCriteria convert(IDAOFactory daoFactory,
            SearchableEntityKind entityKind, SearchCriteria criteria)
    {
        DetailedSearchCriteria mainCriteria =
                new SearchCriteriaToDetailedSearchCriteriaTranslator(daoFactory, criteria,
                        entityKind).convertToDetailedSearchCriteria();

        for (SearchSubCriteria subCriteria : criteria.getSubCriterias())
        {
            DetailedSearchSubCriteria detailedSearchSubCriteria =
                    SearchCriteriaToDetailedSearchCriteriaTranslator
                            .convertToDetailedSearchSubCriteria(daoFactory, subCriteria);
            mainCriteria.addSubCriteria(detailedSearchSubCriteria);
        }

        return mainCriteria;
    }

    // exposed for tests
    static DetailedSearchSubCriteria convertToDetailedSearchSubCriteria(IDAOFactory daoFactory,
            SearchSubCriteria subCriteria)
    {
        final SearchCriteria criteria = subCriteria.getCriteria();
        final SearchableEntityKind targetEntityKind = subCriteria.getTargetEntityKind();
        final DetailedSearchCriteria detailedSearchCriteria =
                convert(daoFactory, targetEntityKind, criteria);
        return new DetailedSearchSubCriteria(convertToAssociatedEntityKind(targetEntityKind),
                detailedSearchCriteria);
    }

    public SearchCriteriaToDetailedSearchCriteriaTranslator(IDAOFactory daoFactory,
            SearchCriteria searchCriteria, SearchableEntityKind entityKind)
    {
        this.daoFactory = daoFactory;
        this.searchCriteria = searchCriteria;
        this.entityKind = entityKind;
        this.attributeTranslator = translatorFor(entityKind);
        newDetailedSearchCriteria = new DetailedSearchCriteria();
    }

    public DetailedSearchCriteria convertToDetailedSearchCriteria()
    {
        // The API is for expert users, we can trust them to provide their own wildcards
        newDetailedSearchCriteria.setUseWildcardSearchMode(true);
        newDetailedSearchCriteria.setConnection(convertSearchOperatorToConnection());
        newDetailedSearchCriteria.setCriteria(convertMatchClausesToDetailedSearchCriterionList());

        return newDetailedSearchCriteria;
    }

    private List<DetailedSearchCriterion> convertMatchClausesToDetailedSearchCriterionList()
    {
        List<MatchClause> searchCriterionList = searchCriteria.getMatchClauses();
        ArrayList<DetailedSearchCriterion> detailedSearchCriterionList =
                new ArrayList<DetailedSearchCriterion>(searchCriterionList.size());
        for (MatchClause matchClause : searchCriterionList)
        {
            detailedSearchCriterionList
                    .add(convertMatchClauseToDetailedSearchCriterion(matchClause));
        }

        return detailedSearchCriterionList;
    }

    private DetailedSearchCriterion convertMatchClauseToDetailedSearchCriterion(
            MatchClause matchClause)
    {

        if (matchClause.getFieldType().equals(MatchClauseFieldType.ATTRIBUTE)
                && matchClause instanceof TimeAttributeMatchClause)
        {
            CompareType t;
            TimeAttributeMatchClause timeMatchClause = (TimeAttributeMatchClause) matchClause;
            switch (matchClause.getCompareMode())
            {
                case EQUALS:
                    t = CompareType.EQUALS;
                    break;
                case LESS_THAN_OR_EQUAL:
                    t = CompareType.LESS_THAN_OR_EQUAL;
                    break;
                case GREATER_THAN_OR_EQUAL:
                    t = CompareType.MORE_THAN_OR_EQUAL;
                    break;
                default:
                    throw new IllegalArgumentException("" + matchClause.getCompareMode());
            }
            return new DetailedSearchCriterion(extractDetailedSearchField(matchClause), t,
                    timeMatchClause.getDesiredValue(), timeMatchClause.getTimeZone());
        } else
        {
            return new DetailedSearchCriterion(extractDetailedSearchField(matchClause),
                    matchClause.getDesiredValue());
        }
    }

    private DetailedSearchField extractDetailedSearchField(MatchClause matchClause)
    {
        DetailedSearchField searchField;

        switch (matchClause.getFieldType())
        {
            case PROPERTY:
                searchField =
                        DetailedSearchField.createPropertyField(((PropertyMatchClause) matchClause)
                                .getPropertyCode());
                break;
            case ATTRIBUTE:
                if (matchClause instanceof TimeAttributeMatchClause)
                {
                    MatchClauseTimeAttribute attribute =
                            ((TimeAttributeMatchClause) matchClause).getAttribute();
                    IAttributeSearchFieldKind searchFieldKind =
                            attributeTranslator
                                    .convertMatchClauseAttributeToAttributeSearchFieldKind(attribute);
                    searchField = DetailedSearchField.createAttributeField(searchFieldKind);
                } else
                {
                    MatchClauseAttribute attribute =
                            ((AttributeMatchClause) matchClause).getAttribute();
                    IAttributeSearchFieldKind searchFieldKind =
                            attributeTranslator
                                    .convertMatchClauseAttributeToAttributeSearchFieldKind(attribute);
                    searchField = DetailedSearchField.createAttributeField(searchFieldKind);
                }
                break;
            case ANY_FIELD:
                searchField =
                        DetailedSearchField.createAnyField(getEntityPropertyTypeDAO()
                                .listPropertyTypeCodes());
                break;
            case ANY_PROPERTY:
                searchField =
                        DetailedSearchField.createAnyPropertyField(getEntityPropertyTypeDAO()
                                .listPropertyTypeCodes());
                break;
            default:
                // Should never reach here
                searchField = null;
        }

        return searchField;
    }

    private SearchCriteriaConnection convertSearchOperatorToConnection()
    {
        SearchCriteriaConnection connection;
        switch (searchCriteria.getOperator())
        {
            case MATCH_ALL_CLAUSES:
                connection = SearchCriteriaConnection.MATCH_ALL;
                break;
            case MATCH_ANY_CLAUSES:
                connection = SearchCriteriaConnection.MATCH_ANY;
                break;
            default:
                // Should never get here
                connection = SearchCriteriaConnection.MATCH_ALL;
        }

        return connection;
    }

    private IEntityPropertyTypeDAO getEntityPropertyTypeDAO()
    {
        AssociatedEntityKind associatedEntityKind = convertToAssociatedEntityKind(entityKind);
        return daoFactory.getEntityPropertyTypeDAO(DtoConverters
                .convertEntityKind(associatedEntityKind.getEntityKind()));
    }

}
