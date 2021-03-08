package ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.SortOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.SortOrder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.Sorting;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.*;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.GlobalSearchObject;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.fetchoptions.GlobalSearchObjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.GlobalSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.GlobalSearchObjectKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.GlobalSearchTextCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.GlobalSearchWildCardsCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.MaterialSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.AuthorisationInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.ISQLAuthorisationInformationProviderDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.dao.ISQLSearchDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ch.ethz.sis.openbis.generic.asapi.v3.dto.global.fetchoptions.GlobalSearchObjectSortOptions.*;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.GlobalSearchCriteriaTranslator.*;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.*;

public class GlobalSearchManager implements IGlobalSearchManager
{

    private static final String PERM_ID_FIELD_NAME = "Perm ID";

    private static final String IDENTIFIER_FIELD_NAME = "Identifier";

    private static final String CODE_FIELD_NAME = "Code";

    private static final String PROPERTY_NAME = "Property";

    private static final Map<String, String> ALIAS_BY_FIELD_NAME = new HashMap<>(4);

    static
    {
        ALIAS_BY_FIELD_NAME.put(SCORE, RANK_ALIAS);
        ALIAS_BY_FIELD_NAME.put(OBJECT_KIND, OBJECT_KIND_ORDINAL_ALIAS);
        ALIAS_BY_FIELD_NAME.put(OBJECT_PERM_ID, OBJECT_PERM_ID);
        ALIAS_BY_FIELD_NAME.put(OBJECT_IDENTIFIER, ID_COLUMN);
    }

    protected final ISQLAuthorisationInformationProviderDAO authProvider;

    private final ISQLSearchDAO searchDAO;

    public GlobalSearchManager(final ISQLAuthorisationInformationProviderDAO authProvider,
            final ISQLSearchDAO searchDAO)
    {
        this.searchDAO = searchDAO;
        this.authProvider = authProvider;
    }

    @Override
    public Collection<Map<String, Object>> searchForIDs(final Long userId,
            final AuthorisationInformation authorisationInformation, final GlobalSearchCriteria criteria,
            final String idsColumnName, final Set<GlobalSearchObjectKind> objectKinds,
            final GlobalSearchObjectFetchOptions fetchOptions, final boolean onlyTotalCount)
    {
        if (objectKinds == null || objectKinds.isEmpty())
        {
            return createEmptyResult(onlyTotalCount);
        }

        final boolean hasStringMatches = criteria.getCriteria().stream().anyMatch(
                criterion -> criterion instanceof GlobalSearchTextCriteria &&
                ((GlobalSearchTextCriteria) criterion).getFieldValue() instanceof StringMatchesValue);

        final List<GlobalSearchTextCriteria> stringContainsGlobalSearchTextCriteria = criteria.getCriteria().stream()
                .filter(criterion -> criterion instanceof GlobalSearchTextCriteria &&
                        ((((GlobalSearchTextCriteria) criterion).getFieldValue() instanceof StringContainsValue) ||
                                ((GlobalSearchTextCriteria) criterion).getFieldValue()
                                        instanceof StringContainsExactlyValue))
                .map(criterion -> (GlobalSearchTextCriteria) criterion).collect(Collectors.toList());

        final boolean hasStringContains = !stringContainsGlobalSearchTextCriteria.isEmpty();

        if (hasStringMatches && hasStringContains)
        {
            throw new IllegalArgumentException("Cannot combine matches and contains criteria in global search.");
        } else if (!hasStringMatches && !hasStringContains)
        {
            return createEmptyResult(onlyTotalCount);
        } else if (hasStringMatches)
        {
            // String matches
            return searchForIdsUsingMatches(userId, criteria, idsColumnName, authorisationInformation, objectKinds,
                    fetchOptions, onlyTotalCount);
        } else
        {
            // String contains
            return searchForIdsUsingContains(userId, criteria, stringContainsGlobalSearchTextCriteria,
                    authorisationInformation, objectKinds, fetchOptions, onlyTotalCount);
        }
    }

    private List<Map<String, Object>> searchForIdsUsingMatches(final Long userId, final GlobalSearchCriteria criteria,
            final String idsColumnName, final AuthorisationInformation authorisationInformation,
            final Set<GlobalSearchObjectKind> objectKinds, final GlobalSearchObjectFetchOptions fetchOptions,
            final boolean onlyTotalCount)
    {
        // Removing blank criteria because they should not affect the result for the match criteria
        final List<ISearchCriteria> filteredCriteria = criteria.getCriteria().stream()
                .filter(criterion -> !(criterion instanceof GlobalSearchTextCriteria) ||
                        !((GlobalSearchTextCriteria) criterion).getFieldValue().getValue().trim().isEmpty())
                .collect(Collectors.toList());
        if (filteredCriteria.isEmpty())
        {
            return createEmptyResult(onlyTotalCount);
        } else
        {
            criteria.setCriteria(filteredCriteria);
            return searchDAO.queryDBForIdsAndRanksWithNonRecursiveCriteria(userId, criteria, idsColumnName,
                    authorisationInformation, objectKinds, fetchOptions, onlyTotalCount);
        }
    }

    private List<Map<String, Object>> searchForIdsUsingContains(final Long userId, final GlobalSearchCriteria criteria,
            final List<GlobalSearchTextCriteria> stringContainsGlobalSearchTextCriteria,
            final AuthorisationInformation authorisationInformation, final Set<GlobalSearchObjectKind> objectKinds,
            final GlobalSearchObjectFetchOptions fetchOptions, final boolean onlyTotalCount)
    {
        final boolean includeExperiments= objectKinds.contains(GlobalSearchObjectKind.EXPERIMENT);
        final boolean includeSamples = objectKinds.contains(GlobalSearchObjectKind.SAMPLE);
        final boolean includeDataSets = objectKinds.contains(GlobalSearchObjectKind.DATA_SET);
        final boolean includeMaterials = objectKinds.contains(GlobalSearchObjectKind.MATERIAL);

        final ExperimentSearchCriteria experimentSearchCriterion = new ExperimentSearchCriteria();
        final SampleSearchCriteria sampleSearchCriterion = new SampleSearchCriteria();
        final DataSetSearchCriteria dataSetSearchCriterion = new DataSetSearchCriteria();
        final MaterialSearchCriteria materialSearchCriterion = new MaterialSearchCriteria();

        experimentSearchCriterion.withOperator(criteria.getOperator());
        sampleSearchCriterion.withOperator(criteria.getOperator());
        dataSetSearchCriterion.withOperator(criteria.getOperator());
        materialSearchCriterion.withOperator(criteria.getOperator());

        for (final GlobalSearchTextCriteria globalSearchTextCriterion : stringContainsGlobalSearchTextCriteria)
        {
            final AbstractStringValue fieldValue = globalSearchTextCriterion.getFieldValue();
            final boolean containsExactly = fieldValue instanceof StringContainsExactlyValue;
            final boolean containsWildCards = criteria.getCriteria().stream()
                    .anyMatch(criterion -> criterion instanceof GlobalSearchWildCardsCriteria);

            if (containsExactly)
            {
                final String stringValue = fieldValue.getValue();
                setValueToCriteria(containsWildCards, experimentSearchCriterion, sampleSearchCriterion,
                        dataSetSearchCriterion, materialSearchCriterion, stringValue);
            } else
            {
                final String[] stringValues = fieldValue.getValue().split("\\s");
                final ExperimentSearchCriteria experimentSearchSubcriteria =
                        experimentSearchCriterion.withSubcriteria().withOrOperator();
                final SampleSearchCriteria sampleSearchSubcriteria =
                        sampleSearchCriterion.withSubcriteria().withOrOperator();
                final DataSetSearchCriteria dataSetSearchSubcriteria =
                        dataSetSearchCriterion.withSubcriteria().withOrOperator();
                final MaterialSearchCriteria materialSearchSubcriteria =
                        materialSearchCriterion.withSubcriteria().withOrOperator();
                for (final String stringValue : stringValues)
                {
                    setValueToCriteria(containsWildCards, experimentSearchSubcriteria, sampleSearchSubcriteria,
                            dataSetSearchSubcriteria, materialSearchSubcriteria, stringValue);
                }
            }
        }

        final Set<Long> experimentIds = includeExperiments
                ? searchDAO.queryDBForIdsAndRanksWithNonRecursiveCriteria(userId, experimentSearchCriterion,
                TableMapper.EXPERIMENT, ID_COLUMN, authorisationInformation)
                : Collections.emptySet();
        final Set<Long> sampleIds = includeSamples
                ? searchDAO.queryDBForIdsAndRanksWithNonRecursiveCriteria(userId, sampleSearchCriterion,
                TableMapper.SAMPLE, ID_COLUMN, authorisationInformation)
                : Collections.emptySet();
        final Set<Long> dataSetIds = includeDataSets
                ? searchDAO.queryDBForIdsAndRanksWithNonRecursiveCriteria(userId, dataSetSearchCriterion,
                TableMapper.DATA_SET, ID_COLUMN, authorisationInformation)
                : Collections.emptySet();
        final Set<Long> materialIds = includeMaterials
                ? searchDAO.queryDBForIdsAndRanksWithNonRecursiveCriteria(userId, materialSearchCriterion,
                TableMapper.MATERIAL, ID_COLUMN, authorisationInformation)
                : Collections.emptySet();

        final int stringContainsIntermediateResultsCount = sampleIds.size() + experimentIds.size() +
                dataSetIds.size() + materialIds.size();
        final List<Map<String, Object>> stringContainsCriteriaIntermediateResults =
                new ArrayList<>(stringContainsIntermediateResultsCount);

        final List<Map<String, Object>> experimentIntermediateResults = includeExperiments
                ? convertIdsToObjectKind(experimentIds, GlobalSearchObjectKind.EXPERIMENT,
                stringContainsIntermediateResultsCount)
                : Collections.emptyList();
        final List<Map<String, Object>> sampleIntermediateResults = convertIdsToObjectKind(sampleIds,
                GlobalSearchObjectKind.SAMPLE, stringContainsIntermediateResultsCount);
        final List<Map<String, Object>> dataSetIntermediateResults = convertIdsToObjectKind(dataSetIds,
                GlobalSearchObjectKind.DATA_SET, stringContainsIntermediateResultsCount);
        final List<Map<String, Object>> materialIntermediateResults = convertIdsToObjectKind(materialIds,
                GlobalSearchObjectKind.MATERIAL, stringContainsIntermediateResultsCount);

        if (fetchOptions.getSortBy() != null && fetchOptions.getSortBy().getSortings() != null
                && fetchOptions.getSortBy().getSortings().size() > 0
                && fetchOptions.getSortBy().getSortings().get(0).getField().equals(OBJECT_KIND)
                && !fetchOptions.getSortBy().getSortings().get(0).getOrder().isAsc())
        {
            stringContainsCriteriaIntermediateResults.addAll(materialIntermediateResults);
            stringContainsCriteriaIntermediateResults.addAll(dataSetIntermediateResults);
            stringContainsCriteriaIntermediateResults.addAll(sampleIntermediateResults);
            stringContainsCriteriaIntermediateResults.addAll(experimentIntermediateResults);
        } else
        {
            stringContainsCriteriaIntermediateResults.addAll(experimentIntermediateResults);
            stringContainsCriteriaIntermediateResults.addAll(sampleIntermediateResults);
            stringContainsCriteriaIntermediateResults.addAll(dataSetIntermediateResults);
            stringContainsCriteriaIntermediateResults.addAll(materialIntermediateResults);
        }

        if (stringContainsCriteriaIntermediateResults.isEmpty())
        {
            return createEmptyResult(onlyTotalCount);
        } else
        {
            final Integer foFromRecord = fetchOptions.getFrom();
            final Integer foRecordsCount = fetchOptions.getCount();
            final boolean hasPaging = foFromRecord != null || foRecordsCount != null;
            if (hasPaging)
            {
                final int fromRecord = foFromRecord != null ? foFromRecord : 0;
                final int toRecord = foRecordsCount != null ? Math.min(fromRecord + foRecordsCount,
                        stringContainsCriteriaIntermediateResults.size())
                        : stringContainsCriteriaIntermediateResults.size();
                return fromRecord <= toRecord
                        ? stringContainsCriteriaIntermediateResults.subList(fromRecord, toRecord)
                        : Collections.emptyList();
            } else
            {
                return stringContainsCriteriaIntermediateResults;
            }
        }
    }

    private void setValueToCriteria(final boolean containsWildCards,
            final ExperimentSearchCriteria experimentSearchSubcriteria,
            final SampleSearchCriteria sampleSearchSubcriteria, final DataSetSearchCriteria dataSetSearchSubcriteria,
            final MaterialSearchCriteria materialSearchSubcriteria, final String stringValue)
    {
        final StringFieldSearchCriteria experimentStringFieldSearchCriteria =
                experimentSearchSubcriteria.withAnyField();
        final StringFieldSearchCriteria sampleStringFieldSearchCriteria =
                sampleSearchSubcriteria.withAnyField();
        final StringFieldSearchCriteria dataSetStringFieldSearchCriteria =
                dataSetSearchSubcriteria.withAnyField();
        final StringFieldSearchCriteria materialStringFieldSearchCriteria =
                materialSearchSubcriteria.withAnyField();

        setWildcardsToCriterion(experimentStringFieldSearchCriteria, containsWildCards);
        setWildcardsToCriterion(sampleStringFieldSearchCriteria, containsWildCards);
        setWildcardsToCriterion(dataSetStringFieldSearchCriteria, containsWildCards);
        setWildcardsToCriterion(materialStringFieldSearchCriteria, containsWildCards);

        experimentStringFieldSearchCriteria.thatContains(stringValue);
        sampleStringFieldSearchCriteria.thatContains(stringValue);
        dataSetStringFieldSearchCriteria.thatContains(stringValue);
        materialStringFieldSearchCriteria.thatContains(stringValue);
    }

    private void setWildcardsToCriterion(final StringFieldSearchCriteria criterion, final boolean containsWildCards)
    {
        if (containsWildCards)
        {
            criterion.withWildcards();
        } else
        {
            criterion.withoutWildcards();
        }
    }

    private List<Map<String, Object>> convertIdsToObjectKind(final Set<Long> ids,
            final GlobalSearchObjectKind objectKind, final int totalCount)
    {
        return ids.stream().map(sampleId ->
        {
            final Map<String, Object> result = new HashMap<>();
            result.put(ID_COLUMN, sampleId);
            result.put(OBJECT_KIND_ORDINAL_ALIAS, objectKind.ordinal());
            result.put(TOTAL_COUNT_ALIAS, (long) totalCount);
            return result;
        }).collect(Collectors.toList());
    }

    private static List<Map<String, Object>> createEmptyResult(final boolean onlyTotalCount)
    {
        return onlyTotalCount ? Collections.singletonList(Collections.singletonMap(TOTAL_COUNT_ALIAS, 0L))
                : Collections.emptyList();
    }

    @Override
    public Collection<Map<String, Object>> searchForDetails(final Collection<Map<String, Object>> idsAndRanksResult,
            final Long userId, final AuthorisationInformation authorisationInformation,
            final GlobalSearchCriteria criteria, final String idsColumnName,
            final Set<GlobalSearchObjectKind> objectKinds, final GlobalSearchObjectFetchOptions fetchOptions)
    {
        return containsValues(idsAndRanksResult)
                ? searchDAO.queryDBWithNonRecursiveCriteria(idsAndRanksResult, userId, criteria,
                idsColumnName, authorisationInformation, objectKinds, fetchOptions)
                : Collections.emptySet();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public List<Map<String, Object>> sortRecords(final Collection<Map<String, Object>> records,
            final SortOptions<GlobalSearchObject> sortOptions)
    {
        final ArrayList<Map<String, Object>> result = new ArrayList<>(records);
        final List<Sorting> sortingList = sortOptions.getSortings();
        result.sort((o1, o2) ->
        {
            for (final Sorting sorting : sortingList)
            {
                final Comparable<Comparable> v1 = getPropertyByKey(o1, sorting.getField());
                final Comparable<Comparable> v2 = getPropertyByKey(o2, sorting.getField());

                if (v1 != null && v2 != null && !v1.equals(v2))
                {
                    return sorting.getOrder().isAsc() ? v1.compareTo(v2) : -v1.compareTo(v2);
                } else if (v1 == null && v2 != null)
                {
                    return sortOrderToInt(sorting.getOrder());
                } else if (v1 != null && v2 == null)
                {
                    return -sortOrderToInt(sorting.getOrder());
                }
            }
            return 0;
        });
        return result;
    }

    @SuppressWarnings({"unchecked", "UnnecessaryBoxing"})
    private <T extends Comparable<T>> Comparable<T> getPropertyByKey(final Map<String, Object> record,
            final String field)
    {
        final String alias = ALIAS_BY_FIELD_NAME.get(field);
        if (alias == null)
        {
            throw new IllegalArgumentException(String.format("Unknown field %s", field));
        }
        return (Comparable<T>) record.get(alias);
    }

    private static int sortOrderToInt(final SortOrder sortOrder)
    {
        return sortOrder.isAsc() ? 1 : -1;
    }

    /**
     * Checks whether a collection contains any values.
     *
     * @param collection collection to be checked for values.
     * @return {@code false} if collection is {@code null} or empty, true otherwise.
     */
    protected static boolean containsValues(final Collection<?> collection)
    {
        return collection != null && !collection.isEmpty();
    }

    @Override
    public Collection<MatchingEntity> map(final Collection<Map<String, Object>> records, final boolean withMatches)
    {
        return records.stream().map(stringObjectMap -> mapRecordToMatchingEntity(stringObjectMap, withMatches))
                .collect(Collectors.toMap(
                        matchingEntity -> matchingEntity.getEntityKind() + "-" + matchingEntity.getId(),
                        Function.identity(),
                        (existingMatchingEntity, newMatchingEntity) ->
                                mergeMatchingEntities(existingMatchingEntity, newMatchingEntity, withMatches),
                        LinkedHashMap::new
                )).values();
    }

    private static MatchingEntity mapRecordToMatchingEntity(final Map<String, Object> fieldsMap,
            final boolean withMatches)
    {
        final MatchingEntity matchingEntity = new MatchingEntity();
        matchingEntity.setCode((String) fieldsMap.get(CODE_COLUMN));
        final EntityKind entityKind = EntityKind.valueOf(
                GlobalSearchObjectKind.values()[(Integer) fieldsMap.get(OBJECT_KIND_ORDINAL_ALIAS)].toString());
        matchingEntity.setEntityKind(entityKind);
        matchingEntity.setId((Long) fieldsMap.get(ID_COLUMN));
        matchingEntity.setPermId((String) fieldsMap.get(PERM_ID_COLUMN));

        final String entityTypesCode = (String) fieldsMap.get(ENTITY_TYPES_CODE_ALIAS);
        if (entityTypesCode != null)
        {
            matchingEntity.setEntityType(new BasicEntityType(entityTypesCode));
        }

        matchingEntity.setIdentifier((String) fieldsMap.get(IDENTIFIER_ALIAS));

        if (entityKind == EntityKind.EXPERIMENT || entityKind == EntityKind.DATA_SET)
        {
            final Space space = new Space();
            space.setCode((String) fieldsMap.get(SPACE_CODE_ALIAS));
            matchingEntity.setSpace(space);
        }

        final Float rank = (Float) fieldsMap.get(RANK_ALIAS);
        matchingEntity.setScore(rank != null ? rank : 0F);

        final List<PropertyMatch> matches = new ArrayList<>();

        if (withMatches)
        {
            mapPropertyMatches(fieldsMap, matches);
            mapAttributeMatches(fieldsMap, entityKind, matches);
        }

        matchingEntity.setMatches(matches);
        return matchingEntity;
    }

    private static MatchingEntity mergeMatchingEntities(final MatchingEntity existingMatchingEntity,
            final MatchingEntity newMatchingEntity, final boolean withMatches)
    {
        existingMatchingEntity.setScore(existingMatchingEntity.getScore() + newMatchingEntity.getScore());
        if (withMatches)
        {
            final Collection<PropertyMatch> existingMatches = existingMatchingEntity.getMatches();
            final Collection<PropertyMatch> newMatches = newMatchingEntity.getMatches();
            final Collection<PropertyMatch> mergedMatches = mergeMatches(existingMatches, newMatches);
            existingMatchingEntity.setMatches(new ArrayList<>(mergedMatches));
        }
        return existingMatchingEntity;
    }

    private static Collection<PropertyMatch> mergeMatches(final Collection<PropertyMatch> existingMatches,
            final Collection<PropertyMatch> newMatches)
    {
        final List<PropertyMatch> combinedMatches = new ArrayList<>(existingMatches);
        combinedMatches.addAll(newMatches);
        return combinedMatches.stream().collect(Collectors.toMap(
                (propertyMatch) -> Arrays.asList(propertyMatch.getCode(), propertyMatch.getValue()),
                Function.identity(),
                (existingPropertyMatch, newPropertyMatch) ->
                {
                    existingPropertyMatch.getSpans().addAll(newPropertyMatch.getSpans());
                    return existingPropertyMatch;
                },
                HashMap::new
                )).values();
    }

    private static void mapAttributeMatches(final Map<String, Object> fieldsMap, final EntityKind entityKind,
            final List<PropertyMatch> matches)
    {
        switch (entityKind)
        {
            case MATERIAL:
            {
                mapAttributeMatch(fieldsMap, matches, IDENTIFIER_ALIAS, IDENTIFIER_FIELD_NAME);
                break;
            }

            case SAMPLE:
            {
                mapAttributeMatch(fieldsMap, matches, SAMPLE_IDENTIFIER_MATCH_ALIAS, IDENTIFIER_FIELD_NAME);
                // Falls through.
            }
            case EXPERIMENT:
            {
                mapAttributeMatch(fieldsMap, matches, CODE_MATCH_ALIAS, CODE_FIELD_NAME);
                mapAttributeMatch(fieldsMap, matches, PERM_ID_MATCH_ALIAS, PERM_ID_FIELD_NAME);
                break;
            }

            case DATA_SET:
            {
                mapAttributeMatch(fieldsMap, matches, CODE_MATCH_ALIAS, PERM_ID_FIELD_NAME);
                mapAttributeMatch(fieldsMap, matches, PERM_ID_MATCH_ALIAS, PERM_ID_FIELD_NAME);
                break;
            }
        }
    }

    private static void mapPropertyMatches(final Map<String, Object> fieldsMap, final List<PropertyMatch> matches)
    {
        final String propertyValueMatch = (String) fieldsMap.get(PROPERTY_VALUE_ALIAS);
        if (propertyValueMatch != null)
        {
            addPropertyMatch(propertyValueMatch, fieldsMap, matches);
        }

        final String cvLabelMatch = (String) fieldsMap.get(CV_LABEL_ALIAS);
        if (cvLabelMatch != null)
        {
            addPropertyMatch(cvLabelMatch, fieldsMap, matches);
        }

        final String cvCodeMatch = (String) fieldsMap.get(CV_CODE_ALIAS);
        if (cvCodeMatch != null)
        {
            addPropertyMatch(cvCodeMatch, fieldsMap, matches);
        }

        final String materialMatch = (String) fieldsMap.get(MATERIAL_MATCH_ALIAS);
        if (materialMatch != null)
        {
            addPropertyMatch(materialMatch, fieldsMap, matches);
        }

        final String sampleMatch = (String) fieldsMap.get(SAMPLE_MATCH_ALIAS);
        if (sampleMatch != null)
        {
            addPropertyMatch(sampleMatch, fieldsMap, matches);
        }
    }

    private static void addPropertyMatch(final String codeMatchString, final Map<String, Object> fieldsMap,
            final List<PropertyMatch> matches)
    {
        final PropertyMatch propertyMatch = new PropertyMatch();
        propertyMatch.setCode(PROPERTY_NAME + " '" + fieldsMap.get(PROPERTY_TYPE_LABEL_ALIAS) + "'");
        propertyMatch.setValue(codeMatchString);

        final String headline = coalesceMap(fieldsMap, VALUE_HEADLINE_ALIAS, LABEL_HEADLINE_ALIAS,
                CODE_HEADLINE_ALIAS);

        if (headline != null)
        {
            computeSpans(propertyMatch, headline);
        }

        matches.add(propertyMatch);
    }

    /**
     * Computes matching text spans for given property match.
     * @param propertyMatch property match to which the spans should be added.
     * @param headline headline string which contains markers of start and stop of matches.
     */
    private static void computeSpans(final PropertyMatch propertyMatch, final String headline)
    {
        final List<Span> spans = new ArrayList<>();
        final int startSelLength = START_SEL.length();
        final int stopSelLength = STOP_SEL.length();
        final int combinedSelLength = startSelLength + stopSelLength;
        int cursorIndex = headline.indexOf(START_SEL);
        int matchesCount = 0;
        while (cursorIndex >= 0)
        {
            final int matchStartIndex = cursorIndex;
            final int matchEndIndex = headline.indexOf(STOP_SEL, matchStartIndex + startSelLength);
            final Span span = new Span();
            span.setStart(matchStartIndex - matchesCount * combinedSelLength);
            span.setEnd(matchEndIndex - startSelLength - matchesCount * combinedSelLength);
            spans.add(span);

            cursorIndex = headline.indexOf(START_SEL, matchEndIndex + stopSelLength);
            matchesCount++;
        }

        propertyMatch.setSpans(spans);
    }

    /**
     * Returns first not null value.
     * @param keys keys in the order how the values should be checked.
     * @return the first not null value from map casted to string.
     */
    private static String coalesceMap(final Map<String, ?> map, final String... keys)
    {
        return (String) Arrays.stream(keys).map(map::get)
                .filter(Objects::nonNull).findFirst().orElse(null);
    }

    private static void mapAttributeMatch(final Map<String, Object> fieldsMap, final List<PropertyMatch> matches,
            final String matchAlias, final String code)
    {
        final Object fieldMatch = fieldsMap.get(matchAlias);
        if (fieldMatch != null)
        {
            final String codeMatchString = (String) fieldMatch;
            final PropertyMatch propertyMatch = new PropertyMatch();
            propertyMatch.setCode(code);
            propertyMatch.setValue(codeMatchString);

            final Span span = new Span();
            span.setStart(0);
            span.setEnd(codeMatchString.length());
            propertyMatch.setSpans(new ArrayList<>(Collections.singletonList(span)));
            matches.add(propertyMatch);
        }
    }

}
