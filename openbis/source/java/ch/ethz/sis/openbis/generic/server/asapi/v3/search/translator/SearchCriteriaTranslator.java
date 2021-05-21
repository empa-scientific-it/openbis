/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.*;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.ID_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.METAPROJECT_ID_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.METAPROJECTS_TABLE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.METAPROJECT_ASSIGNMENTS_ALL_TABLE;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.*;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.search.EntityTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.MaterialTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleContainerSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.search.TagSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.AuthorisationInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.CriteriaMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ILocalSearchManager;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ISearchManager;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.IConditionTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.JoinInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.TranslatorUtils;

public class SearchCriteriaTranslator
{

    public static final DateFormat DATE_HOURS_MINUTES_SECONDS_FORMAT = new SimpleDateFormat(
            new LongDateFormat().getFormat());

    public static final DateFormat DATE_HOURS_MINUTES_FORMAT = new SimpleDateFormat(
            new NormalDateFormat().getFormat());

    public static final DateFormat DATE_FORMAT = new SimpleDateFormat(new ShortDateFormat().getFormat());

    public static final String MAIN_TABLE_ALIAS = TranslatorUtils.getAlias(new AtomicInteger(0));

    private SearchCriteriaTranslator()
    {
        throw new UnsupportedOperationException();
    }

    public static SelectQuery translate(final TranslationContext translationContext)
    {
        if (translationContext.getCriteria() == null)
        {
            throw new IllegalArgumentException("Null criteria provided.");
        }

        final String from = buildFrom(translationContext);
        final String where = buildWhere(translationContext);
        final String select = buildSelect(translationContext);

        final String mainQuery = select + NL + from + NL + where;
        return new SelectQuery(translationContext.getParentCriterion().isNegated()
                ? select + NL + from + NL + WHERE + SP + MAIN_TABLE_ALIAS + PERIOD +
                        translationContext.getIdColumnName() + SP + NOT + SP + IN + SP + LP + NL + mainQuery + NL + RP
                : mainQuery, translationContext.getArgs());
    }

    private static String buildSelect(final TranslationContext translationContext)
    {
        return SELECT + SP + (translationContext.getParentCriterion().isNegated() ? "" : DISTINCT) + SP +
                MAIN_TABLE_ALIAS + PERIOD + translationContext.getIdColumnName();
    }

    private static String buildFrom(final TranslationContext translationContext)
    {
        final StringBuilder sqlBuilder = new StringBuilder();

        final TableMapper tableMapper = translationContext.getTableMapper();
        final String entitiesTableName = tableMapper.getEntitiesTable();
        sqlBuilder.append(FROM).append(SP).append(entitiesTableName).append(SP).append(MAIN_TABLE_ALIAS);

        final AtomicInteger indexCounter = new AtomicInteger(1);
        translationContext.getCriteria().forEach(criterion ->
        {
            if (!(CriteriaMapper.getCriteriaToManagerMap().containsKey(criterion.getClass()) || criterion instanceof EntityTypeSearchCriteria))
            {
                final IConditionTranslator conditionTranslator = CriteriaMapper.getCriteriaToConditionTranslatorMap().get(criterion.getClass());
                if (conditionTranslator != null)
                {
                    @SuppressWarnings("unchecked")
                    final Map<String, JoinInformation> joinInformationMap = conditionTranslator.getJoinInformationMap(criterion,
                            tableMapper, () -> TranslatorUtils.getAlias(indexCounter));

                    if (joinInformationMap != null)
                    {
                        joinInformationMap.values().forEach((joinInformation) ->
                                TranslatorUtils.appendJoin(sqlBuilder, joinInformation));
                        translationContext.getAliases().put(criterion, joinInformationMap);
                    }
                } else
                {
                    throw new IllegalArgumentException("Unsupported criterion type: " + criterion.getClass().getSimpleName());
                }
            }
        });
        return sqlBuilder.toString();
    }

    private static String buildWhere(final TranslationContext translationContext)
    {
        final Collection<ISearchCriteria> criteria = translationContext.getCriteria();
        if (isSearchAllCriteria(criteria))
        {
            return WHERE + SP + TRUE;
        } else
        {
            final String logicalOperator = translationContext.getOperator().toString();
            final String separator = SP + logicalOperator + SP;

            final StringBuilder resultSqlBuilder = criteria.stream().collect(
                    StringBuilder::new,
                    (sqlBuilder, criterion) ->
                    {
                        sqlBuilder.append(separator).append(LP);
                        appendCriterionCondition(translationContext, translationContext.getAuthorisationInformation(),
                                sqlBuilder, criterion);
                        sqlBuilder.append(RP);
                    },
                    StringBuilder::append
            );


            final String condition = resultSqlBuilder.substring(separator.length());
            return WHERE + SP + condition;
        }
    }

    /**
     * Appends condition translated from a criterion.
     * @param translationContext context with miscellaneous information.
     * @param authorisationInformation authorisation information to be used to filter final results.
     * @param sqlBuilder string builder to append the condition to.
     * @param criterion criterion to be translated.
     */
    private static void appendCriterionCondition(final TranslationContext translationContext,
            final AuthorisationInformation authorisationInformation,
            final StringBuilder sqlBuilder, ISearchCriteria criterion)
    {
        final TableMapper tableMapper = translationContext.getTableMapper();
        final ISearchManager subqueryManager = (criterion instanceof EntityTypeSearchCriteria)
                ? CriteriaMapper.getEntityKindToManagerMap().get(tableMapper.getEntityKind())
                : CriteriaMapper.getCriteriaToManagerMap().get(criterion.getClass());
        final AbstractCompositeSearchCriteria parentCriterion = translationContext.getParentCriterion();

        if (tableMapper == null || isCriterionConsistentWithEntityKind(criterion, tableMapper.getEntityKind()))
        {
            if (subqueryManager != null)
            {
                final String column;
                if (parentCriterion.getClass() == criterion.getClass())
                {
                    column = ID_COLUMN;
                } else if (criterion instanceof EntityTypeSearchCriteria)
                {
                    column = tableMapper.getEntityTypesAttributeTypesTableEntityTypeIdField();
                } else
                {
                    column = CriteriaMapper.getCriteriaToInColumnMap().get(criterion.getClass());
                }

                if (tableMapper != null)
                {
                    criterion = convertCriterionIfNeeded(criterion, tableMapper.getEntityKind());
                }

                if (tableMapper != null && column != null)
                {
                    final Set<Long> ids;
                    if (subqueryManager instanceof ILocalSearchManager<?, ?, ?>)
                    {
                        final ILocalSearchManager<ISearchCriteria, ?, ?> localSearchManager =
                                (ILocalSearchManager<ISearchCriteria, ?, ?>) subqueryManager;
                        ids = localSearchManager.searchForIDs(translationContext.getUserId(), authorisationInformation, criterion, parentCriterion,
                                CriteriaMapper.getParentChildCriteriaToChildSelectIdMap().getOrDefault(
                                        Arrays.asList(parentCriterion.getClass(), criterion.getClass()), ID_COLUMN));
                    } else {
                        throw new IllegalArgumentException("Only local search subqueries are supported.");
                    }

                    appendInStatement(sqlBuilder, criterion, column, tableMapper);
                    translationContext.getArgs().add(ids.toArray(new Long[0]));
                } else
                {
                    throw new NullPointerException("tableMapper = " + tableMapper + ", column = " + column + ", criterion.getClass() = " +
                            criterion.getClass());
                }
            } else
            {
                @SuppressWarnings("unchecked")
                final IConditionTranslator<ISearchCriteria> conditionTranslator =
                        (IConditionTranslator<ISearchCriteria>) CriteriaMapper.getCriteriaToConditionTranslatorMap().get(criterion.getClass());
                if (conditionTranslator != null)
                {
                    conditionTranslator.translate(criterion, tableMapper, translationContext.getArgs(), sqlBuilder,
                            translationContext.getAliases().get(criterion),
                            translationContext.getDataTypeByPropertyCode());
                } else
                {
                    throw new IllegalArgumentException("Unsupported criterion type: " + criterion.getClass().getSimpleName());
                }
            }
        } else
        {
            sqlBuilder.append(FALSE);
        }
    }

    private static ISearchCriteria convertCriterionIfNeeded(final ISearchCriteria criterion, final EntityKind entityKind)
    {
        if (criterion instanceof EntityTypeSearchCriteria && entityKind != null)
        {
            final EntityTypeSearchCriteria entityTypeSearchCriterion = (EntityTypeSearchCriteria) criterion;
            switch (entityKind)
            {
                case MATERIAL:
                {
                    final MaterialTypeSearchCriteria newCriterion = new MaterialTypeSearchCriteria();
                    switch (entityTypeSearchCriterion.getOperator())
                    {
                        case AND:
                        {
                            newCriterion.withAndOperator();
                            break;
                        }
                        case OR:
                        {
                            newCriterion.withOrOperator();
                            break;
                        }
                    }
                    newCriterion.setCriteria(entityTypeSearchCriterion.getCriteria());
                    return newCriterion;
                }
                case EXPERIMENT:
                {
                    final ExperimentTypeSearchCriteria newCriterion = new ExperimentTypeSearchCriteria();
                    switch (entityTypeSearchCriterion.getOperator())
                    {
                        case AND:
                        {
                            newCriterion.withAndOperator();
                            break;
                        }
                        case OR:
                        {
                            newCriterion.withOrOperator();
                            break;
                        }
                    }
                    newCriterion.setCriteria(entityTypeSearchCriterion.getCriteria());
                    return newCriterion;
                }
                case SAMPLE:
                {
                    final SampleTypeSearchCriteria newCriterion = new SampleTypeSearchCriteria();
                    switch (entityTypeSearchCriterion.getOperator())
                    {
                        case AND:
                        {
                            newCriterion.withAndOperator();
                            break;
                        }
                        case OR:
                        {
                            newCriterion.withOrOperator();
                            break;
                        }
                    }
                    newCriterion.setCriteria(entityTypeSearchCriterion.getCriteria());
                    return newCriterion;
                }
                case DATA_SET:
                {
                    final DataSetTypeSearchCriteria newCriterion = new DataSetTypeSearchCriteria();
                    switch (entityTypeSearchCriterion.getOperator())
                    {
                        case AND:
                        {
                            newCriterion.withAndOperator();
                            break;
                        }
                        case OR:
                        {
                            newCriterion.withOrOperator();
                            break;
                        }
                    }
                    newCriterion.setCriteria(entityTypeSearchCriterion.getCriteria());
                    return newCriterion;
                }
                default:
                {
                    throw new IllegalArgumentException("Unknown entity kind: " + entityKind);
                }
            }
        } else
        {
            return criterion;
        }
    }

    /**
     * Checks whether criterion is IdSearchCriteria and if so its entity kind should be equal to the provided one (if it is not null).
     *
     * @param criterion the criterion to be checked for compatibility.
     * @param entityKind the entity kind checked for equality, can be null.
     * @return {@code false} only in the case when {@code entityKind != null} and criterion is an instance of IdSearchCriteria and its entity kind
     * (if exists) is not equal to {@code entityKind}.
     */
    private static boolean isCriterionConsistentWithEntityKind(final ISearchCriteria criterion, final EntityKind entityKind)
    {
        if (criterion instanceof IdSearchCriteria<?> && entityKind != null)
        {
            final IdSearchCriteria<? extends IObjectId> idSearchCriterion = (IdSearchCriteria<? extends IObjectId>) criterion;
            if (idSearchCriterion.getId() instanceof EntityTypePermId)
            {
                final EntityTypePermId objectPermId = (EntityTypePermId) idSearchCriterion.getId();
                return objectPermId.getEntityKind() == null || objectPermId.getEntityKind() == entityKind;
            } else
            {
                return true;
            }
        } else
        {
            return true;
        }
    }

    private static void appendInStatement(final StringBuilder sqlBuilder, final ISearchCriteria criterion, final String column,
            final TableMapper tableMapper)
    {
        sqlBuilder.append(MAIN_TABLE_ALIAS).append(PERIOD).append(column).append(SP).append(IN).append(SP).append(LP);
        if (!(criterion instanceof TagSearchCriteria))
        {
            sqlBuilder.append(SELECT).append(SP).append(UNNEST).append(LP).append(QU).append(RP);
        } else
        {
            final String e = "e";
            final String mpa = "mpa";
            final String mp = "mp";
            sqlBuilder.append(SELECT).append(SP).append(e).append(PERIOD).append(column).append(NL).
                    append(FROM).append(SP).append(tableMapper.getEntitiesTable()).append(SP).append(e).append(NL).
                    append(LEFT_JOIN).append(SP).append(METAPROJECT_ASSIGNMENTS_ALL_TABLE).append(SP).append(mpa).append(SP).
                    append(ON).append(SP).append(e).append(PERIOD).append(ID_COLUMN).append(SP).append(EQ).append(SP).append(mpa).append(PERIOD).
                    append(tableMapper.getMetaprojectAssignmentsEntityIdField()).append(NL).
                    append(LEFT_JOIN).append(SP).append(METAPROJECTS_TABLE).append(SP).append(mp).append(SP).
                    append(ON).append(SP).append(mpa).append(PERIOD).append(METAPROJECT_ID_COLUMN).append(SP).append(EQ).append(SP).append(mp).
                    append(PERIOD).append(ID_COLUMN).append(NL).
                    append(WHERE).append(SP).append(mp).append(PERIOD).append(ID_COLUMN).append(SP).append(IN).append(SP).append(LP);
            sqlBuilder.append(SELECT).append(SP).append(UNNEST).append(LP).append(QU).append(RP);
            sqlBuilder.append(RP);
        }
        sqlBuilder.append(RP);
    }

    /**
     * Checks whether the criteria is for searching all values.
     *
     * @param criteria the criteria to be checked.
     * @return {@code true} if the criteria contain only one entity search value which is empty.
     */
    private static boolean isSearchAllCriteria(final Collection<ISearchCriteria> criteria)
    {
        switch (criteria.size())
        {
            case 0:
            {
                return true;
            }

            case 1:
            {
                final ISearchCriteria criterion = criteria.iterator().next();
                return (criterion instanceof AbstractObjectSearchCriteria<?>) &&
                        !(criterion instanceof SampleContainerSearchCriteria) &&
                        ((AbstractCompositeSearchCriteria) criterion).getCriteria().isEmpty();
            }

            default:
            {
                return false;
            }
        }
    }

}
