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

package ch.ethz.sis.openbis.generic.server.asapi.v3.search.dao;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.SortOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.*;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.fetchoptions.GlobalSearchObjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.GlobalSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.GlobalSearchObjectKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.relationship.IGetRelationshipIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.AuthorisationInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.CriteriaMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.sql.ISQLExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.*;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.TranslatorUtils;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.GlobalSearchCriteriaTranslator.*;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.*;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.*;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.*;

public class PostgresSearchDAO implements ISQLSearchDAO
{
    private static final String[] POSTGRES_TYPES = Arrays.asList(
            DataType.INTEGER,
            DataType.REAL,
            DataType.BOOLEAN,
            DataType.DATE,
            DataType.TIMESTAMP,
            DataType.XML)
            .stream().map(DataType::toString).collect(Collectors.toList()).toArray(new String[0]);

    private ISQLExecutor sqlExecutor;

    public PostgresSearchDAO(final ISQLExecutor sqlExecutor)
    {
        this.sqlExecutor = sqlExecutor;
    }

    public Set<Long> queryDBForIdsAndRanksWithNonRecursiveCriteria(final Long userId, final AbstractCompositeSearchCriteria criterion,
            final TableMapper tableMapper, final String idsColumnName, final AuthorisationInformation authorisationInformation)
    {
        final Collection<ISearchCriteria> criteria = criterion.getCriteria();
        final SearchOperator operator = criterion.getOperator();

        final String finalIdColumnName = (idsColumnName == null) ? ID_COLUMN : idsColumnName;

        final TranslationContext translationContext = new TranslationContext();
        translationContext.setUserId(userId);
        translationContext.setTableMapper(tableMapper);
        translationContext.setParentCriterion(criterion);
        translationContext.setCriteria(criteria);
        translationContext.setOperator(operator);
        translationContext.setIdColumnName(finalIdColumnName);
        translationContext.setAuthorisationInformation(authorisationInformation);

        assertPropertyTypesConsistent(translationContext);

        final boolean containsProperties = criteria.stream().anyMatch(
                (subcriterion) -> subcriterion instanceof AbstractFieldSearchCriteria &&
                        ((AbstractFieldSearchCriteria) subcriterion).getFieldType().equals(SearchFieldType.PROPERTY));
        updateWithDataTypes(translationContext, containsProperties);

        final SelectQuery selectQuery = SearchCriteriaTranslator.translate(translationContext);
        final List<Map<String, Object>> result = sqlExecutor.execute(selectQuery.getQuery(), selectQuery.getArgs());
        return result.stream().map(stringLongMap -> (Long) stringLongMap.get(finalIdColumnName))
                .collect(Collectors.toSet());
    }

    private void assertPropertyTypesConsistent(final TranslationContext translationContext)
    {
        final Map<String, String> dataTypeByPropertyCode;

        if (translationContext.getDataTypeByPropertyCode() == null)
        {
            final String pt = "pt";
            final String dt = "dt";
            final String propertyTypeAlias = "propertytype";
            final String dataTypeAlias = "datatype";
            final String isManagedInternallyAlias = "ismanagedinternally";
            final String sql = SELECT + SP + pt + PERIOD + CODE_COLUMN + SP + propertyTypeAlias + COMMA
                    + SP + pt + PERIOD + IS_MANAGED_INTERNALLY + SP + isManagedInternallyAlias + COMMA
                    + SP + dt + PERIOD + CODE_COLUMN + SP + dataTypeAlias + NL
                    + FROM + SP + PROPERTY_TYPES_TABLE + SP + pt + NL
                    + INNER_JOIN + SP + DATA_TYPES_TABLE + SP + dt + SP
                    + ON + SP + pt + PERIOD + DATA_TYPE_COLUMN + SP + EQ + SP + dt + PERIOD + ID_COLUMN;

            final List<Map<String, Object>> queryResultList = sqlExecutor.execute(sql, Collections.emptyList());
            dataTypeByPropertyCode = queryResultList.stream().collect(Collectors.toMap(
                    (valueByColumnName) -> ((((Boolean) valueByColumnName.get(isManagedInternallyAlias)) ? "$" : "") + ((String) valueByColumnName.get(propertyTypeAlias))),
                    (valueByColumnName) -> (String) valueByColumnName.get(dataTypeAlias)));
            translationContext.setDataTypeByPropertyCode(dataTypeByPropertyCode);
        } else
        {
            dataTypeByPropertyCode = translationContext.getDataTypeByPropertyCode();
        }

        translationContext.getCriteria().forEach(criterion ->
        {
            if (criterion instanceof StrictlyStringPropertySearchCriteria)
            {
                final String dataType = dataTypeByPropertyCode.get(((StrictlyStringPropertySearchCriteria) criterion)
                        .getFieldName());
                if (!dataType.equals(DataTypeCode.VARCHAR.toString())
                        && !dataType.equals(DataTypeCode.MULTILINE_VARCHAR.toString())
                        && !dataType.equals(DataTypeCode.HYPERLINK.toString())
                        && !dataType.equals(DataTypeCode.XML.toString()))
                {
                    throwInconsistencyException(criterion, dataType);
                }
            } if (criterion instanceof NumberPropertySearchCriteria)
            {
                final String dataType = dataTypeByPropertyCode.get(((NumberPropertySearchCriteria) criterion)
                        .getFieldName());
                if (!dataType.equals(DataTypeCode.INTEGER.toString())
                        && !dataType.equals(DataTypeCode.REAL.toString()))
                {
                    throwInconsistencyException(criterion, dataType);
                }
            } else if (criterion instanceof DatePropertySearchCriteria)
            {
                final String dataType = dataTypeByPropertyCode.get(((DatePropertySearchCriteria) criterion)
                        .getFieldName());
                if (!dataType.equals(DataTypeCode.TIMESTAMP.toString())
                        && !dataType.equals(DataTypeCode.DATE.toString()))
                {
                    throwInconsistencyException(criterion, dataType);
                }
            } else if (criterion instanceof BooleanPropertySearchCriteria)
            {
                final String dataType = dataTypeByPropertyCode.get(((BooleanPropertySearchCriteria) criterion)
                        .getFieldName());
                if (!dataType.equals(DataTypeCode.BOOLEAN.toString()))
                {
                    throwInconsistencyException(criterion, dataType);
                }
            }
        });
    }

    private void throwInconsistencyException(final ISearchCriteria criterion, final String dataType)
    {
        throw new UserFailureException(String.format("Criterion of type %s cannot be applied to the data type %s.",
                criterion.getClass().getSimpleName(), dataType));
    }

    @Override
    public List<Map<String, Object>> queryDBForIdsAndRanksWithNonRecursiveCriteria(final Long userId,
            final GlobalSearchCriteria criterion, final String idsColumnName,
            final AuthorisationInformation authorisationInformation, final Set<GlobalSearchObjectKind> objectKinds,
            final GlobalSearchObjectFetchOptions fetchOptions, final boolean onlyTotalCount)
    {
        final TranslationContext translationContext = buildTranslationContext(userId, criterion,
                idsColumnName, authorisationInformation, objectKinds, fetchOptions);

        // Short query to narrow down the result set and calculate ranks.
        final SelectQuery selectQuery = GlobalSearchCriteriaTranslator.translateToShortQuery(translationContext,
                onlyTotalCount);
        return sqlExecutor.execute(selectQuery.getQuery(), selectQuery.getArgs());
    }

    @Override
    public Collection<Map<String, Object>> queryDBWithNonRecursiveCriteria(
            final Collection<Map<String, Object>> idsAndRanksResult, final Long userId,
            final GlobalSearchCriteria criterion, final String idsColumnName,
            final AuthorisationInformation authorisationInformation, final Set<GlobalSearchObjectKind> objectKinds,
            final GlobalSearchObjectFetchOptions fetchOptions)
    {
        final TranslationContext translationContext = buildTranslationContext(userId, criterion,
                idsColumnName, authorisationInformation, objectKinds, fetchOptions);

        final Map<GlobalSearchObjectKind, Set<Long>> idSetByObjectKindMap = new EnumMap<>(GlobalSearchObjectKind.class);
        final Map<GlobalSearchObjectKind, Map<Long, Map<String, Object>>> objectKindToRecordByIdMap =
                new EnumMap<>(GlobalSearchObjectKind.class);
        final Map<GlobalSearchObjectKind, Map<Long, Integer>> objectKindToIndexByIdMap =
                new EnumMap<>(GlobalSearchObjectKind.class);

        final GlobalSearchObjectKind[] objectKindValues = GlobalSearchObjectKind.values();
        int i = 0;
        for (Map<String, Object> stringObjectMap : idsAndRanksResult)
        {
            final Long id = (Long) stringObjectMap.get(ID_COLUMN);
            final Integer objectKindOrdinal = (Integer) stringObjectMap.get(OBJECT_KIND_ORDINAL_ALIAS);
            final GlobalSearchObjectKind objectKind = objectKindValues[objectKindOrdinal];

            final Set<Long> idSet = idSetByObjectKindMap.computeIfAbsent(objectKind, k -> new HashSet<>());
            idSet.add(id);

            final Map<Long, Map<String, Object>> recordByIdMap = objectKindToRecordByIdMap.computeIfAbsent(objectKind,
                    k -> new HashMap<>());
            recordByIdMap.put(id, stringObjectMap);

            final Map<Long, Integer> indexByIdMap = objectKindToIndexByIdMap.computeIfAbsent(objectKind,
                    k -> new HashMap<>());
            indexByIdMap.put(id, i);

            i++;
        }

        // Detailed query to fetch all required information as a final result.
        final SelectQuery detailsSelectQuery = GlobalSearchCriteriaTranslator.translateToDetailsQuery(
                translationContext, idSetByObjectKindMap);
        final List<Map<String, Object>> detailsResult = sqlExecutor.execute(detailsSelectQuery.getQuery(),
                detailsSelectQuery.getArgs());

        final Map<GlobalSearchObjectKind, Map<Long, Map<String, Object>>> objectKindToDetailedRecordByIdMap =
                new EnumMap<>(GlobalSearchObjectKind.class);
        detailsResult.forEach(record ->
        {
            final Long id = (Long) record.get(ID_COLUMN);
            final Integer objectKindOrdinal = (Integer) record.get(OBJECT_KIND_ORDINAL_ALIAS);
            final GlobalSearchObjectKind objectKind = objectKindValues[objectKindOrdinal];

            final Map<Long, Map<String, Object>> recordByIdMap = objectKindToDetailedRecordByIdMap
                    .computeIfAbsent(objectKind, k -> new HashMap<>());
            recordByIdMap.put(id, record);
        });

        // Adding ranks obtained from the first query to the result of the second one.
        detailsResult.forEach(record ->
        {
            final GlobalSearchObjectKind objectKind = objectKindValues[(Integer) record.get(OBJECT_KIND_ORDINAL_ALIAS)];
            final Map<Long, Map<String, Object>> detailedRecordByIdMap = objectKindToRecordByIdMap.get(objectKind);
            record.put(RANK_ALIAS, detailedRecordByIdMap.get((Long) record.get(ID_COLUMN)).get(RANK_ALIAS));
        });

        // Sorting final results in the same order as the short result.
        detailsResult.sort((record1, record2) ->
        {
            final Long id1 = (Long) record1.get(ID_COLUMN);
            final Integer objectKindOrdinal1 = (Integer) record1.get(OBJECT_KIND_ORDINAL_ALIAS);
            final GlobalSearchObjectKind objectKind1 = objectKindValues[objectKindOrdinal1];

            final Long id2 = (Long) record2.get(ID_COLUMN);
            final Integer objectKindOrdinal2 = (Integer) record2.get(OBJECT_KIND_ORDINAL_ALIAS);
            final GlobalSearchObjectKind objectKind2 = objectKindValues[objectKindOrdinal2];

            return objectKindToIndexByIdMap.get(objectKind1).get(id1) -
                    objectKindToIndexByIdMap.get(objectKind2).get(id2);
        });
        return detailsResult;
    }

    private static TranslationContext buildTranslationContext(final Long userId, final GlobalSearchCriteria criterion,
            final String idsColumnName, final AuthorisationInformation authorisationInformation,
            final Set<GlobalSearchObjectKind> objectKinds, final GlobalSearchObjectFetchOptions fetchOptions)
    {
        final Collection<ISearchCriteria> criteria = criterion.getCriteria();
        final SearchOperator operator = criterion.getOperator();

        final String finalIdColumnName = (idsColumnName == null) ? ID_COLUMN : idsColumnName;

        final TranslationContext translationContext = new TranslationContext();
        translationContext.setUserId(userId);
        translationContext.setParentCriterion(criterion);
        translationContext.setCriteria(criteria);
        translationContext.setOperator(operator);
        translationContext.setIdColumnName(finalIdColumnName);
        translationContext.setAuthorisationInformation(authorisationInformation);
        translationContext.setFetchOptions(fetchOptions);
        translationContext.setObjectKinds(objectKinds);
        return translationContext;
    }

    @Override
    public Set<Long> findChildIDs(final TableMapper tableMapper, final Set<Long> parentIdSet,
            final IGetRelationshipIdExecutor.RelationshipType relationshipType)
    {
        final String rel = "rel";
        final String child = "child";

        final String sql = SELECT + SP + DISTINCT + SP + child + PERIOD + ID_COLUMN + NL +
                FROM + SP + tableMapper.getRelationshipsTable() + SP + rel + NL +
                INNER_JOIN + SP + tableMapper.getEntitiesTable() + SP + child + SP +
                ON + SP + rel + PERIOD + tableMapper.getRelationshipsTableChildIdField() + SP + EQ + SP + child +
                PERIOD + ID_COLUMN + NL +
                WHERE + SP + tableMapper.getRelationshipsTableParentIdField() + SP + IN + SP + LP +
                SELECT + SP + UNNEST + LP + QU + RP + RP + SP +
                AND + SP + RELATIONSHIP_COLUMN + SP + EQ + SP + LP +
                SELECT + SP + ID_COLUMN + SP +
                FROM + SP + RELATIONSHIP_TYPES_TABLE + SP +
                WHERE + SP + CODE_COLUMN + SP + EQ + SP + QU +
                RP;

        final List<Object> args = Arrays.asList(parentIdSet.toArray(new Long[0]), relationshipType.toString());
        final List<Map<String, Object>> queryResultList = sqlExecutor.execute(sql, args);
        return queryResultList.stream().map(stringObjectMap -> (Long) stringObjectMap.get(ID_COLUMN))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Long> findParentIDs(final TableMapper tableMapper, final Set<Long> childIdSet,
            final IGetRelationshipIdExecutor.RelationshipType relationshipType)
    {
        final String rel = "rel";
        final String parent = "parent";

        final String sql = SELECT + SP + DISTINCT + SP + parent + PERIOD + ID_COLUMN + NL +
                FROM + SP + tableMapper.getRelationshipsTable() + SP + rel + NL +
                INNER_JOIN + SP + tableMapper.getEntitiesTable() + SP + parent + SP +
                ON + SP + rel + PERIOD + tableMapper.getRelationshipsTableParentIdField() + SP + EQ + SP +
                parent + PERIOD + ID_COLUMN + NL +
                WHERE + SP + tableMapper.getRelationshipsTableChildIdField() + SP + IN + SP + LP +
                SELECT + SP + UNNEST + LP + QU + RP + RP + SP +
                AND + SP + RELATIONSHIP_COLUMN + SP + EQ + SP + LP +
                SELECT + SP + ID_COLUMN + SP +
                FROM + SP + RELATIONSHIP_TYPES_TABLE + SP +
                WHERE + SP + CODE_COLUMN + SP + EQ + SP + QU +
                RP;
        final List<Object> args = Arrays.asList(childIdSet.toArray(new Long[0]), relationshipType.toString());
        final List<Map<String, Object>> queryResultList = sqlExecutor.execute(sql, args);
        return queryResultList.stream().map(stringObjectMap -> (Long) stringObjectMap.get(ID_COLUMN))
                .collect(Collectors.toSet());
    }

    @Override
    public List<Long> sortIDs(final TableMapper tableMapper, final Collection<Long> filteredIDs,
            final SortOptions<?> sortOptions)
    {
        final TranslationContext translationContext = new TranslationContext();
        translationContext.setTableMapper(tableMapper);
        translationContext.setIds(filteredIDs);
        translationContext.setSortOptions(sortOptions);

        final boolean containsProperties = sortOptions.getSortings().stream().anyMatch(
                (sorting) -> TranslatorUtils.isPropertySearchFieldName(sorting.getField()));

        updateWithDataTypes(translationContext, containsProperties);

        final SelectQuery orderQuery = OrderTranslator.translateToOrderQuery(translationContext);
        final List<Map<String, Object>> orderQueryResultList = sqlExecutor.execute(orderQuery.getQuery(),
                orderQuery.getArgs());
        return orderQueryResultList.stream().map((valueByColumnName) -> (Long) valueByColumnName.get(ID_COLUMN))
                .collect(Collectors.toList());
    }

    private void updateWithDataTypes(final TranslationContext translationContext, final boolean containsProperties)
    {
        translationContext.setTypesToFilter(POSTGRES_TYPES);
        final Map<String, String> typeByPropertyName;
        if (containsProperties)
        {
            // Making property types query only when it is needed.
            final SelectQuery dataTypesQuery = OrderTranslator.translateToSearchTypeQuery(translationContext);
            final List<Map<String, Object>> dataTypesQueryResultList = sqlExecutor.execute(dataTypesQuery.getQuery(),
                    dataTypesQuery.getArgs());
            typeByPropertyName = dataTypesQueryResultList.stream().collect(Collectors.toMap(
                    (valueByColumnName) -> (String) valueByColumnName.get(OrderTranslator.PROPERTY_CODE_ALIAS),
                    (valueByColumnName) -> (String) valueByColumnName.get(OrderTranslator.TYPE_CODE_ALIAS)));
        } else
        {
            typeByPropertyName = Collections.emptyMap();
        }

        translationContext.setDataTypeByPropertyName(typeByPropertyName);
    }

    @Autowired
    public void setApplicationContext(final ApplicationContext applicationContext)
    {
        CriteriaMapper.initCriteriaToManagerMap(applicationContext);
    }

}
