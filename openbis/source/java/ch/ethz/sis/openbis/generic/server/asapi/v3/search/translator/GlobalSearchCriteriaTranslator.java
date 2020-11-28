package ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.Sorting;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractStringValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringContainsExactlyValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.fetchoptions.GlobalSearchObjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.fetchoptions.GlobalSearchObjectSortOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.GlobalSearchObjectKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.GlobalSearchObjectKindCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.GlobalSearchTextCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.GlobalSearchWildCardsCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.AuthorisationInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.AttributesMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.TableNames;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.stream.StreamSupport;

import static ch.ethz.sis.openbis.generic.asapi.v3.dto.global.fetchoptions.GlobalSearchObjectSortOptions.*;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.PSQLTypes.*;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.AttributesMapper.PERM_ID_ATTRIBUTE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper.*;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.*;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SearchCriteriaTranslator.MAIN_TABLE_ALIAS;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.TranslatorUtils.buildFullIdentifierConcatenationString;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.TranslatorUtils.buildTypeCodeIdentifierConcatenationString;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.*;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.DATA_TYPES_TABLE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.PROJECTS_TABLE;

public class GlobalSearchCriteriaTranslator
{
    private static final Random RANDOM = new Random();

    public static final String START_SEL = "<" + generateRandomString() + "--";

    public static final String STOP_SEL = "--" + generateRandomString() + ">";

    public static final String RANK_ALIAS = "rank";

    public static final String IDENTIFIER_ALIAS = "identifier";

    public static final String OBJECT_KIND_ORDINAL_ALIAS = "object_kind_ordinal";

    public static final String SPACE_CODE_ALIAS = "space_code";

    public static final String CODE_MATCH_ALIAS = "code_match";

    public static final String PERM_ID_MATCH_ALIAS = "perm_id_match";

    public static final String TOTAL_COUNT_ALIAS = "total_count";

    public static final String SAMPLE_IDENTIFIER_MATCH_ALIAS = "sample_identifier_match";

    public static final String MATERIAL_MATCH_ALIAS = "material_match";

    public static final String SAMPLE_MATCH_ALIAS = "sample_match";

    public static final String ENTITY_TYPES_CODE_ALIAS = "enty_code";

    public static final String PROPERTY_TYPE_LABEL_ALIAS = "property_type_label";

    public static final String CV_CODE_ALIAS = "cv_code";

    public static final String CV_LABEL_ALIAS = "cv_label";

    public static final String PROPERTY_VALUE_ALIAS = "property_value";

    public static final String VALUE_HEADLINE_ALIAS = "value_headline";

    public static final String LABEL_HEADLINE_ALIAS = "label_headline";

    public static final String CODE_HEADLINE_ALIAS = "code_headline";

    public static final String DESCRIPTION_HEADLINE_ALIAS = "description_headline";

    private static final String REG_CONFIG = "english";

    private static final String PROPERTIES_TABLE_ALIAS = "prop";

    private static final String ENTITY_TYPES_TABLE_ALIAS = "enty";

    private static final String CONTROLLED_VOCABULARY_TERMS_TABLE_ALIAS = "cvte";

    private static final String SAMPLES_TABLE_ALIAS = "samp";

    private static final String MATERIALS_TABLE_ALIAS = "mat";

    private static final String SPACE_TABLE_ALIAS = "space";

    private static final String PROJECT_TABLE_ALIAS = "proj";

    private static final String CONTAINER_TABLE_ALIAS = "cont";

    private static final String ENTITY_TYPES_ATTRIBUTE_TYPES_TABLE_ALIAS = "etpt";

    private static final String ATTRIBUTE_TYPES_TABLE_ALIAS = "prty";

    private static final String DATA_TYPES_TABLE_ALIAS = "daty";

    private static final String TS_HEADLINE_OPTIONS = "HighlightAll=TRUE, StartSel=" + START_SEL
            +", StopSel=" + STOP_SEL;

    private static final Logger LOG = LogFactory.getLogger(LogCategory.OPERATION, GlobalSearchCriteriaTranslator.class);

    private static final Map<String, String> ALIAS_BY_FIELD_NAME = new HashMap<>(4);

    static
    {
        ALIAS_BY_FIELD_NAME.put(SCORE, RANK_ALIAS);
        ALIAS_BY_FIELD_NAME.put(OBJECT_KIND, OBJECT_KIND_ORDINAL_ALIAS);
        ALIAS_BY_FIELD_NAME.put(OBJECT_PERM_ID, PERM_ID_COLUMN);
        ALIAS_BY_FIELD_NAME.put(OBJECT_IDENTIFIER, IDENTIFIER_ALIAS);
    }

    private GlobalSearchCriteriaTranslator()
    {
        throw new UnsupportedOperationException();
    }

    private static String generateRandomString()
    {
        final int leftLimit = 97; // letter 'a'
        final int rightLimit = 122; // letter 'z'
        final int targetStringLength = 10;

        return RANDOM.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    public static SelectQuery translateToShortQuery(final TranslationContext translationContext,
            final boolean onlyTotalCount)
    {
        if (translationContext.getCriteria() == null)
        {
            throw new IllegalArgumentException("Null criteria provided.");
        }

        final boolean withWildcards = translationContext.getCriteria().stream()
                .anyMatch((criterion) -> criterion instanceof GlobalSearchWildCardsCriteria);
        if (withWildcards)
        {
            LOG.warn("Full text search with wildcards is not supported.");
        }

        final StringBuilder sqlBuilder = new StringBuilder(LP);
        final Spliterator<ISearchCriteria> spliterator = translationContext.getCriteria().stream()
                .filter((criterion) -> !(criterion instanceof GlobalSearchWildCardsCriteria)
                        && !(criterion instanceof GlobalSearchObjectKindCriteria)).spliterator();

        if (spliterator.tryAdvance((criterion) -> translateShortCriterion(sqlBuilder, translationContext, criterion)))
        {
            StreamSupport.stream(spliterator, false).forEach((criterion) ->
            {
                sqlBuilder.append(RP).append(NL).append(UNION_ALL).append(NL).append(LP).append(NL);
                translateShortCriterion(sqlBuilder, translationContext, criterion);
            });
        }
        sqlBuilder.append(RP);

        final String prefixSelectContent = onlyTotalCount ? COUNT + LP + ASTERISK + RP + SP + TOTAL_COUNT_ALIAS
                : ID_COLUMN + COMMA + SP + PERM_ID_COLUMN + COMMA + SP +
                OBJECT_KIND_ORDINAL_ALIAS + COMMA + SP + RANK_ALIAS + COMMA + SP + IDENTIFIER_ALIAS + COMMA + SP +
                COUNT + LP + ASTERISK + RP + SP + OVER + LP + RP + SP + TOTAL_COUNT_ALIAS;
        final String prefixSql = SELECT + SP + prefixSelectContent + NL +
                FROM + SP + LP + NL +
                SELECT + SP + PROJECT_COLUMN + COMMA + SP + SPACE_COLUMN + COMMA + SP + ID_COLUMN + COMMA + SP +
                PERM_ID_COLUMN + SP + PERM_ID_COLUMN + COMMA + SP + OBJECT_KIND_ORDINAL_ALIAS + COMMA +
                SP + SUM + LP + RANK_ALIAS + RP + SP + RANK_ALIAS + COMMA + SP + IDENTIFIER_ALIAS + NL +
                FROM + SP + LP + NL;

        final StringBuilder suffixSqlBuilder = new StringBuilder();
        suffixSqlBuilder.append(RP).append(SP).append("q1").append(NL)
                .append(GROUP_BY).append(SP);

        suffixSqlBuilder.append(PROJECT_COLUMN).append(COMMA).append(SP);
        suffixSqlBuilder.append(SPACE_COLUMN).append(COMMA).append(SP);
        suffixSqlBuilder.append(EXPERIMENT_COLUMN).append(COMMA).append(SP);
        suffixSqlBuilder.append(ID_COLUMN).append(COMMA).append(SP);
        suffixSqlBuilder.append(PERM_ID_COLUMN).append(COMMA).append(SP);
        suffixSqlBuilder.append(OBJECT_KIND_ORDINAL_ALIAS).append(COMMA).append(SP);
        suffixSqlBuilder.append(IDENTIFIER_ALIAS).append(NL);

        suffixSqlBuilder.append(RP).append(SP).append("q2").append(NL);

        if (!onlyTotalCount)
        {
            translateOrderBy(suffixSqlBuilder, translationContext);
            translateLimitOffset(suffixSqlBuilder, translationContext);
        }

        return new SelectQuery(prefixSql + sqlBuilder.toString() + suffixSqlBuilder.toString(),
                translationContext.getArgs());
    }

    private static void translateOrderBy(final StringBuilder sqlBuilder,
            final TranslationContext translationContext)
    {
        final GlobalSearchObjectSortOptions sortOptions = translationContext.getFetchOptions().getSortBy();
        if (sortOptions != null)
        {
            final List<Sorting> sortings = sortOptions.getSortings();

            if (sortings != null && !sortings.isEmpty())
            {
                sqlBuilder.append(ORDER_BY).append(SP);
                final Spliterator<Sorting> spliterator = sortings.stream().spliterator();

                if (spliterator.tryAdvance(sorting -> addOrderByField(sqlBuilder, sorting)))
                {
                    StreamSupport.stream(spliterator, false).forEach(sorting ->
                    {
                        sqlBuilder.append(COMMA).append(SP);
                        addOrderByField(sqlBuilder, sorting);
                    });
                }
                sqlBuilder.append(NL);
            }
        }
    }

    private static void addOrderByField(final StringBuilder suffixSqlBuilder, final Sorting sorting)
    {
        suffixSqlBuilder.append(ALIAS_BY_FIELD_NAME.get(sorting.getField())).append(SP)
                .append(sorting.getOrder().toString());
    }

    private static void translateLimitOffset(final StringBuilder suffixSqlBuilder,
            final TranslationContext translationContext)
    {
        final GlobalSearchObjectFetchOptions fetchOptions = translationContext.getFetchOptions();
        final Integer foFromRecord = fetchOptions.getFrom();
        final Integer foRecordsCount = fetchOptions.getCount();
        if (foRecordsCount != null)
        {
            suffixSqlBuilder.append(LIMIT).append(SP).append(foRecordsCount).append(NL);
        }
        if (foFromRecord != null)
        {
            suffixSqlBuilder.append(OFFSET).append(SP).append(foFromRecord).append(NL);
        }
    }

    private static void translateAuthorisation(final StringBuilder sqlBuilder,
            final TranslationContext translationContext, final TableMapper tableMapper)
    {
        final AuthorisationInformation authorisationInformation = translationContext.getAuthorisationInformation();
        final List<Object> args = translationContext.getArgs();
        if (!authorisationInformation.isInstanceRole())
        {
            if (tableMapper != MATERIAL)
            {
                sqlBuilder.append(SP).append(AND).append(SP).append(LP).append(NL);
            }
            switch (tableMapper)
            {
                case SAMPLE:
                {
                    sqlBuilder.append(MAIN_TABLE_ALIAS).append(PERIOD).append(SPACE_COLUMN).append(SP).append(IN)
                            .append(SP).append(LP)
                            .append(SELECT).append(SP).append(UNNEST).append(LP).append(QU).append(RP).append(RP)
                            .append(SP).append(OR).append(NL);
                    sqlBuilder.append(MAIN_TABLE_ALIAS).append(PERIOD).append(PROJECT_COLUMN).append(SP).append(IN)
                            .append(SP).append(LP).append(SELECT)
                            .append(SP).append(UNNEST).append(LP).append(QU).append(RP).append(RP)
                            .append(SP).append(OR).append(NL);
                    sqlBuilder.append(MAIN_TABLE_ALIAS).append(PERIOD).append(EXPERIMENT_COLUMN).append(SP).append(IN)
                            .append(SP).append(LP).append(SELECT)
                            .append(SP).append(ID_COLUMN).append(SP).append(FROM).append(SP)
                            .append(TableMapper.EXPERIMENT.getEntitiesTable()).append(SP)
                            .append(WHERE).append(SP).append(PROJECT_COLUMN).append(SP).append(IN).append(SP)
                            .append(SELECT_UNNEST).append(RP)
                            .append(SP).append(OR).append(NL);
                    sqlBuilder.append(MAIN_TABLE_ALIAS).append(PERIOD).append(SPACE_COLUMN).append(SP).append(IS_NULL)
                            .append(SP).append(AND).append(SP).append(MAIN_TABLE_ALIAS).append(PERIOD)
                            .append(PROJECT_COLUMN).append(SP).append(IS_NULL)
                            .append(NL);

                    final Long[] projectIds = authorisationInformation.getProjectIds().toArray(new Long[0]);
                    args.add(authorisationInformation.getSpaceIds().toArray(new Long[0]));
                    args.add(projectIds);
                    args.add(projectIds);
                    break;
                }

                case EXPERIMENT:
                {
                    sqlBuilder.append(MAIN_TABLE_ALIAS).append(PERIOD).append(PROJECT_COLUMN).append(SP).append(IN)
                            .append(SP).append(LP)
                            .append(SELECT).append(SP).append(ID_COLUMN).append(SP)
                            .append(FROM).append(SP).append(PROJECTS_TABLE).append(SP)
                            .append(WHERE).append(SP).append(SPACE_COLUMN).append(SP).append(IN).append(SP).append(LP)
                            .append(SELECT).append(SP).append(UNNEST).append(LP).append(QU).append(RP)
                            .append(RP)
                            .append(RP).append(SP).append(OR).append(NL);
                    sqlBuilder.append(MAIN_TABLE_ALIAS).append(PERIOD).append(PROJECT_COLUMN).append(SP).append(IN)
                            .append(SP).append(LP)
                            .append(SELECT).append(SP).append(UNNEST).append(LP).append(QU).append(RP)
                            .append(RP).append(NL);

                    args.add(authorisationInformation.getSpaceIds().toArray(new Long[0]));
                    args.add(authorisationInformation.getProjectIds().toArray(new Long[0]));
                    break;
                }

                case DATA_SET:
                {
                    final String d = "d";
                    final String ep = "ep";
                    final String sp = "sp";
                    final String exp = "exp";
                    final String samp = "samp";
                    sqlBuilder.append(MAIN_TABLE_ALIAS).append(PERIOD).append(ID_COLUMN).append(SP).append(IN)
                            .append(SP).append(LP);
                    sqlBuilder.append(SELECT).append(SP).append(d).append(PERIOD).append(ID_COLUMN).append(NL);
                    sqlBuilder.append(FROM).append(SP).append(TableMapper.DATA_SET.getEntitiesTable()).append(SP)
                            .append(d).append(NL);
                    sqlBuilder.append(LEFT_JOIN).append(SP).append(TableMapper.EXPERIMENT.getEntitiesTable()).append(SP)
                            .append(exp).append(SP).append(ON).append(SP).append(d).append(PERIOD)
                            .append(EXPERIMENT_COLUMN).append(SP).append(EQ).append(SP).append(exp).append(PERIOD)
                            .append(ID_COLUMN).append(NL);
                    sqlBuilder.append(LEFT_JOIN).append(SP).append(TableMapper.PROJECT.getEntitiesTable()).append(SP)
                            .append(ep).append(SP).append(ON).append(SP).append(exp).append(PERIOD)
                            .append(PROJECT_COLUMN).append(SP).append(EQ).append(SP).append(ep).append(PERIOD)
                            .append(ID_COLUMN).append(NL);
                    sqlBuilder.append(LEFT_JOIN).append(SP).append(TableMapper.SAMPLE.getEntitiesTable()).append(SP)
                            .append(samp).append(SP).append(ON).append(SP).append(d).append(PERIOD)
                            .append(SAMPLE_COLUMN).append(SP).append(EQ).append(SP).append(samp).append(PERIOD)
                            .append(ID_COLUMN).append(NL);
                    sqlBuilder.append(LEFT_JOIN).append(SP).append(TableMapper.PROJECT.getEntitiesTable()).append(SP)
                            .append(sp).append(SP).append(ON).append(SP).append(samp).append(PERIOD)
                            .append(PROJECT_COLUMN).append(SP).append(EQ).append(SP).append(sp).append(PERIOD)
                            .append(ID_COLUMN).append(NL);
                    sqlBuilder.append(WHERE).append(SP).append(ep).append(PERIOD).append(ID_COLUMN)
                            .append(SP).append(IN).append(SP).append(SELECT_UNNEST)
                            .append(SP).append(OR).append(SP)
                            .append(sp).append(PERIOD).append(ID_COLUMN).append(SP).append(IN).append(SP)
                            .append(SELECT_UNNEST)
                            .append(SP).append(OR).append(NL)
                            .append(ep).append(PERIOD).append(SPACE_COLUMN).append(SP).append(IN).append(SP)
                            .append(SELECT_UNNEST)
                            .append(SP).append(OR).append(SP)
                            .append(sp).append(PERIOD).append(SPACE_COLUMN).append(SP).append(IN).append(SP)
                            .append(SELECT_UNNEST);
                    sqlBuilder.append(RP).append(NL);
                    args.add(authorisationInformation.getProjectIds().toArray(new Long[0]));
                    args.add(authorisationInformation.getProjectIds().toArray(new Long[0]));
                    args.add(authorisationInformation.getSpaceIds().toArray(new Long[0]));
                    args.add(authorisationInformation.getSpaceIds().toArray(new Long[0]));
                    break;
                }

                case MATERIAL:
                {
                    // No filtering is needed in this case.
                    break;
                }

                default:
                {
                    throw new IllegalArgumentException("Full text search does not support this table mapper: "
                            + tableMapper);
                }
            }
            if (tableMapper != MATERIAL)
            {
                sqlBuilder.append(RP);
            }
        }
    }

    public static SelectQuery translateToDetailsQuery(final TranslationContext translationContext,
            final Map<GlobalSearchObjectKind, Set<Long>> idSetByObjectKindMap)
    {
        final StringBuilder sqlBuilder = new StringBuilder(SELECT + SP + ASTERISK + NL);
        sqlBuilder.append(FROM).append(SP).append(LP).append(NL);

        sqlBuilder.append(LP).append(NL);
        final Spliterator<ISearchCriteria> spliterator = translationContext.getCriteria().stream()
                .filter((criterion) -> !(criterion instanceof GlobalSearchWildCardsCriteria)
                        && !(criterion instanceof GlobalSearchObjectKindCriteria)).spliterator();
        if (spliterator.tryAdvance((criterion) -> translateDetailsCriterion(sqlBuilder, translationContext, criterion,
                idSetByObjectKindMap)))
        {
            StreamSupport.stream(spliterator, false).forEach((criterion) ->
            {
                sqlBuilder.append(RP).append(NL).append(UNION_ALL).append(NL).append(LP).append(NL);
                translateDetailsCriterion(sqlBuilder, translationContext, criterion, idSetByObjectKindMap);
            });
        }
        sqlBuilder.append(RP).append(NL);
        sqlBuilder.append(RP).append(SP).append("q1").append(NL);

        return new SelectQuery(sqlBuilder.toString(), translationContext.getArgs());
    }

    private static void translateShortCriterion(final StringBuilder sqlBuilder,
            final TranslationContext translationContext, final ISearchCriteria criterion)
    {
        final Set<GlobalSearchObjectKind> objectKinds = translationContext.getObjectKinds();
        if (criterion instanceof GlobalSearchTextCriteria && objectKinds != null && !objectKinds.isEmpty())
        {
            final GlobalSearchTextCriteria globalSearchTextCriterion = (GlobalSearchTextCriteria) criterion;
            final boolean containsSample = objectKinds.contains(GlobalSearchObjectKind.SAMPLE);
            final boolean containsExperiment = objectKinds.contains(GlobalSearchObjectKind.EXPERIMENT);
            final boolean containsDataSet = objectKinds.contains(GlobalSearchObjectKind.DATA_SET);
            final boolean containsMaterial = objectKinds.contains(GlobalSearchObjectKind.MATERIAL);

            if (containsSample)
            {
                buildShortSubquery(sqlBuilder, translationContext, globalSearchTextCriterion, SAMPLE);
            }

            if (containsExperiment)
            {
                if (containsSample)
                {
                    sqlBuilder.append(UNION_ALL).append(NL);
                }
                buildShortSubquery(sqlBuilder, translationContext, globalSearchTextCriterion, EXPERIMENT);
            }

            if (containsDataSet)
            {
                if (containsSample || containsExperiment)
                {
                    sqlBuilder.append(UNION_ALL).append(NL);
                }
                buildShortSubquery(sqlBuilder, translationContext, globalSearchTextCriterion, DATA_SET);
            }

            if (containsMaterial)
            {
                if (containsSample || containsExperiment || containsDataSet)
                {
                    sqlBuilder.append(UNION_ALL).append(NL);
                }
                buildShortSubquery(sqlBuilder, translationContext, globalSearchTextCriterion, MATERIAL);
            }
        }
    }

    private static void buildShortSubquery(final StringBuilder sqlBuilder, final TranslationContext translationContext,
            final GlobalSearchTextCriteria globalSearchTextCriterion, final TableMapper tableMapper)
    {
        // Fields
        buildShortSelect(sqlBuilder, translationContext, globalSearchTextCriterion, tableMapper, true);
        buildShortFrom(sqlBuilder, tableMapper, true);
        buildShortWhere(sqlBuilder, translationContext, globalSearchTextCriterion, tableMapper, true);

        sqlBuilder.append(UNION_ALL).append(NL);

        // Properties
        buildShortSelect(sqlBuilder, translationContext, globalSearchTextCriterion, tableMapper, false);
        buildShortFrom(sqlBuilder, tableMapper, false);
        buildShortWhere(sqlBuilder, translationContext, globalSearchTextCriterion, tableMapper, false);
    }

    private static void translateDetailsCriterion(final StringBuilder sqlBuilder,
            final TranslationContext translationContext, final ISearchCriteria criterion,
            final Map<GlobalSearchObjectKind, Set<Long>> idSetByObjectKindMap)
    {
        final Set<GlobalSearchObjectKind> objectKinds = translationContext.getObjectKinds();
        if (criterion instanceof GlobalSearchTextCriteria && objectKinds != null && !objectKinds.isEmpty())
        {
            final GlobalSearchTextCriteria globalSearchTextCriterion = (GlobalSearchTextCriteria) criterion;
            final Set<Long> sampleIdSet = idSetByObjectKindMap.get(GlobalSearchObjectKind.SAMPLE);
            final Set<Long> experimentIdSet = idSetByObjectKindMap.get(GlobalSearchObjectKind.EXPERIMENT);
            final Set<Long> dataSetIdSet = idSetByObjectKindMap.get(GlobalSearchObjectKind.DATA_SET);
            final Set<Long> materialIdSet = idSetByObjectKindMap.get(GlobalSearchObjectKind.MATERIAL);
            final boolean containsSample = sampleIdSet != null;
            final boolean containsExperiment = experimentIdSet != null;
            final boolean containsDataSet = dataSetIdSet != null;
            final boolean containsMaterial = materialIdSet != null;

            if (containsSample)
            {
                buildDetailsSubquery(sqlBuilder, translationContext, globalSearchTextCriterion, SAMPLE, sampleIdSet);
            }

            if (containsExperiment)
            {
                if (containsSample)
                {
                    sqlBuilder.append(UNION_ALL).append(NL);
                }
                buildDetailsSubquery(sqlBuilder, translationContext, globalSearchTextCriterion, EXPERIMENT,
                        experimentIdSet);
            }

            if (containsDataSet)
            {
                if (containsSample || containsExperiment)
                {
                    sqlBuilder.append(UNION_ALL).append(NL);
                }
                buildDetailsSubquery(sqlBuilder, translationContext, globalSearchTextCriterion, DATA_SET,
                        dataSetIdSet);
            }

            if (containsMaterial)
            {
                if (containsSample || containsExperiment || containsDataSet)
                {
                    sqlBuilder.append(UNION_ALL).append(NL);
                }
                buildDetailsSubquery(sqlBuilder, translationContext, globalSearchTextCriterion, MATERIAL,
                        materialIdSet);
            }
        }
    }

    private static void buildDetailsSubquery(final StringBuilder sqlBuilder,
            final TranslationContext translationContext, final GlobalSearchTextCriteria globalSearchTextCriterion,
            final TableMapper tableMapper, final Collection<Long> resultIds)
    {
        // Fields
        buildDetailsSelect(sqlBuilder, translationContext, globalSearchTextCriterion, tableMapper, true);
        buildDetailsFrom(sqlBuilder, tableMapper, true);
        buildDetailsWhere(sqlBuilder, translationContext, globalSearchTextCriterion, resultIds, tableMapper, true);

        sqlBuilder.append(UNION_ALL).append(NL);

        // Properties
        buildDetailsSelect(sqlBuilder, translationContext, globalSearchTextCriterion, tableMapper, false);
        buildDetailsFrom(sqlBuilder, tableMapper, false);
        buildDetailsWhere(sqlBuilder, translationContext, globalSearchTextCriterion, resultIds, tableMapper, false);
    }

    private static void buildShortSelect(final StringBuilder sqlBuilder, final TranslationContext translationContext,
            final GlobalSearchTextCriteria criterion, final TableMapper tableMapper, final boolean forAttributes)
    {
        final AbstractStringValue stringValue = criterion.getFieldValue();
        final List<Object> args = translationContext.getArgs();

        final boolean hasSpaces = hasSpaces(tableMapper);
        final boolean hasProjects = hasProjects(tableMapper);
        final boolean hasExperiments = hasExperiments(tableMapper);

        final String prefix = MAIN_TABLE_ALIAS + PERIOD;
        sqlBuilder.append(SELECT).append(SP);
        sqlBuilder.append(prefix).append(ID_COLUMN).append(COMMA).append(SP);

        buildSelectIdentifier(sqlBuilder, tableMapper, hasSpaces, hasProjects);
        sqlBuilder.append(COMMA).append(SP);

        if (hasProjects)
        {
            sqlBuilder.append(prefix).append(PROJECT_COLUMN);
        } else
        {
            sqlBuilder.append(NULL).append(SP).append(PROJECT_COLUMN);
        }
        sqlBuilder.append(COMMA).append(SP);

        if (hasSpaces)
        {
            sqlBuilder.append(prefix).append(SPACE_COLUMN);
        } else
        {
            sqlBuilder.append(NULL).append(SP).append(SPACE_COLUMN);
        }
        sqlBuilder.append(COMMA).append(SP);

        if (hasExperiments)
        {
            sqlBuilder.append(prefix).append(EXPERIMENT_COLUMN);
        } else
        {
            sqlBuilder.append(NULL).append(SP).append(EXPERIMENT_COLUMN);
        }
        sqlBuilder.append(COMMA).append(SP);

        final int objectKindOrdinal = GlobalSearchObjectKind.valueOf(tableMapper.toString()).ordinal();
        if (forAttributes)
        {
            sqlBuilder.append(prefix).append(getPermId(tableMapper)).append(SP)
                    .append(PERM_ID_COLUMN).append(COMMA).append(SP)
                    .append(objectKindOrdinal).append(SP).append(OBJECT_KIND_ORDINAL_ALIAS)
                    .append(COMMA).append(SP);
            buildTsRank(sqlBuilder, MAIN_TABLE_ALIAS, () -> buildCastingTsQueryPart(sqlBuilder, stringValue, args));
        } else
        {
            sqlBuilder.append(prefix).append(getPermId(tableMapper)).append(COMMA).append(SP)
                    .append(objectKindOrdinal).append(SP).append(OBJECT_KIND_ORDINAL_ALIAS)
                    .append(COMMA).append(SP);
            buildTsRank(sqlBuilder, PROPERTIES_TABLE_ALIAS, () -> buildTsQueryPart(sqlBuilder, stringValue, args));

            sqlBuilder.append(SP).append(PLUS).append(SP);
            sqlBuilder.append(COALESCE).append(LP);
            buildTsRank(sqlBuilder, MATERIALS_TABLE_ALIAS,
                    () -> buildCastingTsQueryPart(sqlBuilder, stringValue, args));
            sqlBuilder.append(COMMA).append(SP).append(0).append(RP);

            if (tableMapper == TableMapper.SAMPLE || tableMapper == TableMapper.EXPERIMENT
                    || tableMapper == TableMapper.DATA_SET)
            {
                sqlBuilder.append(SP).append(PLUS).append(SP);
                sqlBuilder.append(COALESCE).append(LP);
                buildTsRank(sqlBuilder, SAMPLES_TABLE_ALIAS,
                        () -> buildCastingTsQueryPart(sqlBuilder, stringValue, args));
                sqlBuilder.append(COMMA).append(SP).append(0).append(RP);
            }
        }

        sqlBuilder.append(SP).append(RANK_ALIAS).append(NL);
    }

    private static void buildTsRank(final StringBuilder sqlBuilder, final String tsVectorReference,
            final Runnable tsQueryBuilder)
    {
        sqlBuilder.append(TS_RANK).append(LP).append(ARRAY).append(LB)
                .append(SQ).append(0.001).append(SQ).append(COMMA).append(SP)
                .append(SQ).append(0.01).append(SQ).append(COMMA).append(SP)
                .append(SQ).append(0.1).append(SQ).append(COMMA).append(SP)
                .append(SQ).append(1.0).append(SQ).append(RB)
                .append(DOUBLE_COLON).append(FLOAT4).append(LB).append(RB).append(COMMA).append(SP)
                .append(tsVectorReference).append(PERIOD)
                .append(TSVECTOR_DOCUMENT).append(COMMA).append(SP);
        tsQueryBuilder.run();
        sqlBuilder.append(RP);
    }

    private static void buildShortFrom(final StringBuilder sqlBuilder, final TableMapper tableMapper,
            final boolean forAttributes)
    {
        final String entitiesTable = tableMapper.getEntitiesTable();

        sqlBuilder.append(FROM).append(SP).append(entitiesTable).append(SP).append(MAIN_TABLE_ALIAS).append(NL);
        if (!forAttributes)
        {
            sqlBuilder.append(INNER_JOIN).append(SP).append(tableMapper.getValuesTable()).append(SP)
                    .append(PROPERTIES_TABLE_ALIAS).append(SP)
                    .append(ON).append(SP).append(MAIN_TABLE_ALIAS).append(PERIOD).append(ID_COLUMN).append(SP)
                    .append(EQ).append(SP).append(PROPERTIES_TABLE_ALIAS).append(PERIOD)
                    .append(tableMapper.getValuesTableEntityIdField()).append(NL);
            sqlBuilder.append(LEFT_JOIN).append(SP).append(MATERIAL.getEntitiesTable()).append(SP)
                    .append(MATERIALS_TABLE_ALIAS).append(SP)
                    .append(ON).append(SP).append(PROPERTIES_TABLE_ALIAS).append(PERIOD).append(MATERIAL_PROP_COLUMN)
                    .append(SP).append(EQ).append(SP).append(MATERIALS_TABLE_ALIAS).append(PERIOD).append(ID_COLUMN)
                    .append(NL);

            if (tableMapper == SAMPLE || tableMapper == EXPERIMENT || tableMapper == DATA_SET)
            {
                sqlBuilder.append(LEFT_JOIN).append(SP).append(SAMPLE.getEntitiesTable()).append(SP)
                        .append(SAMPLES_TABLE_ALIAS).append(SP)
                        .append(ON).append(SP).append(PROPERTIES_TABLE_ALIAS).append(PERIOD).append(SAMPLE_PROP_COLUMN)
                        .append(SP).append(EQ).append(SP).append(SAMPLES_TABLE_ALIAS).append(PERIOD).append(ID_COLUMN)
                        .append(NL);
            }
        }

        if (tableMapper == EXPERIMENT)
        {
            buildProjectAndSpacesJoin(sqlBuilder, tableMapper);
        }

        if (tableMapper == MATERIAL)
        {
            buildEntityTypesJoin(sqlBuilder, tableMapper);
        }
    }

    private static void buildShortWhere(final StringBuilder sqlBuilder, final TranslationContext translationContext,
            final GlobalSearchTextCriteria criterion, final TableMapper tableMapper, final boolean forAttributes)
    {
        final List<Object> args = translationContext.getArgs();

        sqlBuilder.append(WHERE).append(SP);
        if (forAttributes)
        {
            sqlBuilder.append(MAIN_TABLE_ALIAS).append(PERIOD)
                    .append(TS_VECTOR_COLUMN).append(SP).append(DOUBLE_AT)
                    .append(SP).append(QU).append(DOUBLE_COLON).append(TSQUERY);
            args.add(toTsQueryText(criterion.getFieldValue()));
        } else
        {
            buildTsVectorMatch(sqlBuilder, criterion.getFieldValue(), tableMapper, args);
        }
        translateAuthorisation(sqlBuilder, translationContext, tableMapper);
        sqlBuilder.append(NL);
    }

    private static void buildDetailsSelect(final StringBuilder sqlBuilder, final TranslationContext translationContext,
            final GlobalSearchTextCriteria criterion, final TableMapper tableMapper, final boolean forAttributes)
    {
        final AbstractStringValue stringValue = criterion.getFieldValue();
        final List<Object> args = translationContext.getArgs();
        final int objectKindOrdinal = GlobalSearchObjectKind.valueOf(tableMapper.toString()).ordinal();

        final boolean hasSpaces = hasSpaces(tableMapper);
        final boolean hasProjects = hasProjects(tableMapper);

        sqlBuilder.append(SELECT).append(SP).append(MAIN_TABLE_ALIAS).append(PERIOD).append(ID_COLUMN)
                .append(COMMA).append(NL);

        switch (tableMapper)
        {
            case SAMPLE:
            case EXPERIMENT:
            {
                sqlBuilder.append(MAIN_TABLE_ALIAS).append(PERIOD).append(PERM_ID_COLUMN);
                break;
            }
            default:
            {
                sqlBuilder.append(MAIN_TABLE_ALIAS).append(PERIOD).append(CODE_COLUMN).append(SP)
                        .append(PERM_ID_COLUMN);
                break;
            }
        }
        sqlBuilder.append(COMMA).append(NL);

        sqlBuilder.append(ENTITY_TYPES_TABLE_ALIAS).append(PERIOD).append(CODE_COLUMN).append(SP)
                .append(ENTITY_TYPES_CODE_ALIAS).append(COMMA).append(NL);

        sqlBuilder.append(MAIN_TABLE_ALIAS).append(PERIOD).append(CODE_COLUMN).append(COMMA).append(NL);
        sqlBuilder.append(MAIN_TABLE_ALIAS).append(PERIOD).append(PERSON_REGISTERER_COLUMN).append(COMMA).append(NL);
        sqlBuilder.append(objectKindOrdinal).append(SP).append(OBJECT_KIND_ORDINAL_ALIAS)
                .append(COMMA).append(NL);

        buildSelectIdentifier(sqlBuilder, tableMapper, hasSpaces, hasProjects);
        sqlBuilder.append(COMMA).append(NL);

        if (hasSpaces || hasProjects)
        {
            sqlBuilder.append(SPACE_TABLE_ALIAS).append(PERIOD).append(CODE_COLUMN);
        } else
        {
            sqlBuilder.append(NULL);
        }
        sqlBuilder.append(SP).append(SPACE_CODE_ALIAS).append(COMMA).append(NL);

        final String[] criterionValues = criterion.getFieldValue().getValue().toLowerCase().trim().split("\\s+");
        if (forAttributes)
        {
            buildAttributesMatchSelection(sqlBuilder, criterionValues, tableMapper, args);
            sqlBuilder.append(COMMA).append(NL);

            if (tableMapper == SAMPLE)
            {
                final String sampleIdentifierColumnReference = MAIN_TABLE_ALIAS + PERIOD + SAMPLE_IDENTIFIER_COLUMN;
                buildCaseWhenIn(sqlBuilder, args, criterionValues,
                        new String[] { sampleIdentifierColumnReference },
                        sampleIdentifierColumnReference, NULL);
            } else
            {
                sqlBuilder.append(NULL);
            }
            sqlBuilder.append(SP).append(SAMPLE_IDENTIFIER_MATCH_ALIAS).append(COMMA).append(NL);

            sqlBuilder.append(NULL).append(SP).append(MATERIAL_MATCH_ALIAS).append(COMMA).append(NL);
            sqlBuilder.append(NULL).append(SP).append(SAMPLE_MATCH_ALIAS).append(COMMA).append(NL);
            sqlBuilder.append(NULL).append(SP).append(PROPERTY_TYPE_LABEL_ALIAS).append(COMMA).append(NL);
            sqlBuilder.append(NULL).append(SP).append(PROPERTY_VALUE_ALIAS).append(COMMA).append(NL);
            sqlBuilder.append(NULL).append(SP).append(CV_CODE_ALIAS).append(COMMA).append(NL);
            sqlBuilder.append(NULL).append(SP).append(CV_LABEL_ALIAS).append(COMMA).append(NL);
            sqlBuilder.append(NULL).append(SP).append(VALUE_HEADLINE_ALIAS).append(COMMA).append(NL);
            sqlBuilder.append(NULL).append(SP).append(CODE_HEADLINE_ALIAS).append(COMMA).append(NL);
            sqlBuilder.append(NULL).append(SP).append(LABEL_HEADLINE_ALIAS);
        } else
        {
            sqlBuilder.append(NULL).append(SP).append(CODE_MATCH_ALIAS).append(COMMA).append(NL);
            sqlBuilder.append(NULL).append(SP).append(PERM_ID_MATCH_ALIAS).append(COMMA).append(NL);
            sqlBuilder.append(NULL).append(SP).append(SAMPLE_IDENTIFIER_MATCH_ALIAS).append(COMMA).append(NL);

            buildMaterialMatch(sqlBuilder, criterionValues, args);
            sqlBuilder.append(COMMA).append(NL);

            if (tableMapper == SAMPLE || tableMapper == EXPERIMENT || tableMapper == DATA_SET)
            {
                buildSampleMatch(sqlBuilder, criterionValues, args);
            } else
            {
                sqlBuilder.append(NULL).append(SP).append(SAMPLE_MATCH_ALIAS);
            }
            sqlBuilder.append(COMMA).append(NL);

            sqlBuilder.append(ATTRIBUTE_TYPES_TABLE_ALIAS).append(PERIOD).append(LABEL_COLUMN).append(SP)
                    .append(PROPERTY_TYPE_LABEL_ALIAS).append(COMMA).append(NL);
            sqlBuilder.append(PROPERTIES_TABLE_ALIAS).append(PERIOD).append(VALUE_COLUMN).append(SP)
                    .append(PROPERTY_VALUE_ALIAS).append(COMMA).append(NL);
            sqlBuilder.append(CONTROLLED_VOCABULARY_TERMS_TABLE_ALIAS).append(PERIOD).append(CODE_COLUMN).append(SP)
                    .append(CV_CODE_ALIAS).append(COMMA).append(NL);
            sqlBuilder.append(CONTROLLED_VOCABULARY_TERMS_TABLE_ALIAS).append(PERIOD).append(LABEL_COLUMN).append(SP)
                    .append(CV_LABEL_ALIAS).append(COMMA).append(NL);

            final boolean useHeadline = translationContext.getFetchOptions().hasMatch();
            buildTsHeadline(sqlBuilder, stringValue, args, PROPERTIES_TABLE_ALIAS + PERIOD + VALUE_COLUMN,
                    VALUE_HEADLINE_ALIAS, useHeadline);
            sqlBuilder.append(COMMA).append(NL);
            buildTsHeadline(sqlBuilder, stringValue, args,
                    CONTROLLED_VOCABULARY_TERMS_TABLE_ALIAS + PERIOD + CODE_COLUMN, CODE_HEADLINE_ALIAS,
                    useHeadline);
            sqlBuilder.append(COMMA).append(NL);
            buildTsHeadline(sqlBuilder, stringValue, args,
                    CONTROLLED_VOCABULARY_TERMS_TABLE_ALIAS + PERIOD + LABEL_COLUMN, LABEL_HEADLINE_ALIAS,
                    useHeadline);
        }

        sqlBuilder.append(NL);
    }

    private static void buildSelectIdentifier(final StringBuilder sqlBuilder, final TableMapper tableMapper,
            final boolean hasSpaces, final boolean hasProjects)
    {
        switch (tableMapper)
        {
            case MATERIAL:
            {
                buildTypeCodeIdentifierConcatenationString(sqlBuilder, ENTITY_TYPES_TABLE_ALIAS);
                break;
            }
            case SAMPLE:
            {
                sqlBuilder.append(MAIN_TABLE_ALIAS).append(PERIOD).append(SAMPLE_IDENTIFIER_COLUMN);
                break;
            }
            default:
            {
                buildFullIdentifierConcatenationString(sqlBuilder, hasSpaces || hasProjects ? SPACE_TABLE_ALIAS : null,
                        hasProjects ? PROJECT_TABLE_ALIAS : null, null, false);
                break;
            }
        }
        sqlBuilder.append(SP).append(IDENTIFIER_ALIAS);
    }

    private static void buildSampleMatch(final StringBuilder sqlBuilder, final String[] values,
            final List<Object> args)
    {
        sqlBuilder.append(CASE).append(NL);
        appendWhenThen(sqlBuilder, SAMPLES_TABLE_ALIAS, PERM_ID_COLUMN, values, args);
        appendWhenThen(sqlBuilder, SAMPLES_TABLE_ALIAS, CODE_COLUMN, values, args);
        appendWhenThen(sqlBuilder, SAMPLES_TABLE_ALIAS, SAMPLE_IDENTIFIER_COLUMN, values, args);
        sqlBuilder.append('\t').append(ELSE).append(SP).append(NULL).append(NL);
        sqlBuilder.append(END).append(SP).append(SAMPLE_MATCH_ALIAS);
    }

    private static void buildMaterialMatch(final StringBuilder sqlBuilder, final String[] values,
            final List<Object> args)
    {
        sqlBuilder.append(CASE).append(NL);
        appendWhenThen(sqlBuilder, MATERIALS_TABLE_ALIAS, CODE_COLUMN, values, args);
        sqlBuilder.append('\t').append(ELSE).append(SP).append(NULL).append(NL);
        sqlBuilder.append(END).append(SP).append(MATERIAL_MATCH_ALIAS);
    }

    private static void appendWhenThen(final StringBuilder sqlBuilder, final String tableAlias, final String column,
            final String[] values, final List<Object> args)
    {
        sqlBuilder.append('\t').append(WHEN).append(SP).append(LOWER).append(LP)
                .append(tableAlias).append(PERIOD).append(column).append(RP).append(SP).append(IN).append(SP)
                .append(SELECT_UNNEST).append(SP)
                .append(THEN).append(SP).append(tableAlias).append(PERIOD).append(column).append(NL);
        args.add(values);
    }

    private static void buildTsHeadline(final StringBuilder sqlBuilder, final AbstractStringValue stringValue,
            final List<Object> args, final String field, final String alias, final boolean useHeadline)
    {
        if (useHeadline)
        {
            sqlBuilder.append(TS_HEADLINE).append(LP).append(field).append(COMMA).append(SP);
            buildTsQueryPart(sqlBuilder, stringValue, args);
            sqlBuilder.append(COMMA).append(SP).append(SQ).append(TS_HEADLINE_OPTIONS).append(SQ);
            sqlBuilder.append(RP);
        } else
        {
            sqlBuilder.append(NULL);
        }
        sqlBuilder.append(SP).append(alias);
    }

    /**
     * Appends condition for the attributes that they may match.
     * @param sqlBuilder {@link StringBuilder string builder} containing SQL to be operated on.
     * @param criterion the full text search criterion.
     * @param args query arguments.
     */
    private static void buildAttributesMatchCondition(final StringBuilder sqlBuilder,
            final GlobalSearchTextCriteria criterion, final List<Object> args)
    {
        sqlBuilder.append(MAIN_TABLE_ALIAS).append(PERIOD).append(TS_VECTOR_COLUMN).append(SP).append(DOUBLE_AT)
                .append(SP).append(QU).append(DOUBLE_COLON).append(TSQUERY);
        args.add(toTsQueryText(criterion.getFieldValue()));
    }

    /**
     * Appends selection text for the attributes that they may match.
     *
     * @param sqlBuilder {@link StringBuilder string builder} containing SQL to be operated on.
     * @param criterionValues full text search values to be put to the full text search matching function.
     * @param tableMapper the table mapper.
     * @param args query arguments.
     */
    private static void buildAttributesMatchSelection(final StringBuilder sqlBuilder, final String[] criterionValues,
            final TableMapper tableMapper, final List<Object> args)
    {
        final String thenValue = MAIN_TABLE_ALIAS + PERIOD + CODE_COLUMN;
        buildCaseWhenIn(sqlBuilder, args, criterionValues, new String[]{thenValue}, thenValue, NULL);
        sqlBuilder.append(SP).append(CODE_MATCH_ALIAS).append(COMMA).append(NL);

        switch (tableMapper)
        {
            case SAMPLE:
            case EXPERIMENT:
            {

                sqlBuilder.append(CASE).append(SP).append(WHEN).append(SP).append(LOWER).append(LP);
                sqlBuilder.append(MAIN_TABLE_ALIAS).append(PERIOD).append(PERM_ID_COLUMN).append(RP);
                sqlBuilder.append(SP).append(IN).append(SP).append(LP)
                        .append(SELECT).append(SP).append(UNNEST).append(LP).append(QU).append(RP)
                        .append(RP);
                sqlBuilder.append(SP).append(THEN).append(SP).append(MAIN_TABLE_ALIAS).append(PERIOD)
                        .append(PERM_ID_COLUMN);
                sqlBuilder.append(SP).append(ELSE).append(SP).append(NULL).append(SP).append(END);

                args.add(criterionValues);
                break;
            }
            default:
            {
                sqlBuilder.append(NULL);
                break;
            }
        }

        sqlBuilder.append(SP).append(PERM_ID_MATCH_ALIAS);
    }

    /**
     * Builds the following part of the query.
     * <pre>
     *     CASE WHEN
     *     matchingColumns[0] IN (SELECT UNNEST(?))
     *     OR
     *     ...
     *     OR
     *     matchingColumns[n] IN (SELECT UNNEST(?))
     *     THEN thenValue ELSE elseValue END
     * </pre>
     * @param sqlBuilder {@link StringBuilder string builder} containing SQL to be operated on.
     * @param args query arguments.
     * @param criterionValues full text search values to be put to the full text search matching function.
     * @param matchingColumns array of columns at least one of which should be equal to one of {@code criterionValues}.
     * @param thenValue resulting value for the true case.
     * @param elseValue resulting value for the false case.
     */
    private static void buildCaseWhenIn(final StringBuilder sqlBuilder, final List<Object> args,
            final String[] criterionValues, final String[] matchingColumns, final String thenValue,
            final String elseValue)
    {
        sqlBuilder.append(CASE).append(SP).append(WHEN).append(SP);

        final Spliterator<String> spliterator = Arrays.stream(matchingColumns).spliterator();

        if (spliterator.tryAdvance(matchingColumn -> appendMatchingColumnCondition(sqlBuilder, matchingColumn, args,
                criterionValues)))
        {
            spliterator.forEachRemaining(matchingColumn ->
            {
                sqlBuilder.append(SP).append(OR).append(SP);
                appendMatchingColumnCondition(sqlBuilder, matchingColumn, args, criterionValues);
            });
        }

        sqlBuilder.append(SP).append(THEN).append(SP).append(thenValue);
        sqlBuilder.append(SP).append(ELSE).append(SP).append(elseValue).append(SP).append(END);
    }

    /**
     * Builds the following part of the query and adds the corresponding array of values to the query arguments list.
     * <pre>
     *     matchingColumns[0] IN (SELECT UNNEST(?))
     * </pre>
     * @param sqlBuilder {@link StringBuilder string builder} containing SQL to be operated on.
     * @param matchingColumn column which should be equal to one of values in the query parameter ('?').
     * @param args query arguments.
     * @param criterionValues full text search values to be put to the full text search matching function.
     */
    private static void appendMatchingColumnCondition(final StringBuilder sqlBuilder,
            final String matchingColumn, final List<Object> args, final String[] criterionValues)
    {
        sqlBuilder.append(LOWER).append(SP).append(LP).append(matchingColumn).append(RP);
        sqlBuilder.append(SP).append(IN).append(SP).append(LP)
                .append(SELECT).append(SP).append(UNNEST).append(LP).append(QU).append(RP)
                .append(RP);
        args.add(criterionValues);
    }

    private static void buildDetailsFrom(final StringBuilder sqlBuilder, final TableMapper tableMapper,
            final boolean forAttributes)
    {
        final String entitiesTable = tableMapper.getEntitiesTable();

        sqlBuilder.append(FROM).append(SP).append(entitiesTable).append(SP).append(MAIN_TABLE_ALIAS).append(NL);

        if (!forAttributes)
        {
            sqlBuilder.append(LEFT_JOIN).append(SP).append(tableMapper.getValuesTable()).append(SP)
                    .append(PROPERTIES_TABLE_ALIAS).append(SP)
                    .append(ON).append(SP).append(MAIN_TABLE_ALIAS).append(PERIOD).append(ID_COLUMN).append(SP)
                    .append(EQ)
                    .append(SP).append(PROPERTIES_TABLE_ALIAS).append(PERIOD)
                    .append(tableMapper.getValuesTableEntityIdField()).append(NL);

            sqlBuilder.append(LEFT_JOIN).append(SP).append(tableMapper.getEntityTypesAttributeTypesTable()).append(SP)
                    .append(ENTITY_TYPES_ATTRIBUTE_TYPES_TABLE_ALIAS).append(SP).append(ON).append(SP)
                    .append(PROPERTIES_TABLE_ALIAS).append(PERIOD)
                    .append(tableMapper.getValuesTableEntityTypeAttributeTypeIdField()).append(SP)
                    .append(EQ).append(SP).append(ENTITY_TYPES_ATTRIBUTE_TYPES_TABLE_ALIAS).append(PERIOD)
                    .append(ID_COLUMN).append(NL);
            sqlBuilder.append(LEFT_JOIN).append(SP).append(tableMapper.getAttributeTypesTable()).append(SP)
                    .append(ATTRIBUTE_TYPES_TABLE_ALIAS).append(SP).append(ON).append(SP)
                    .append(ENTITY_TYPES_ATTRIBUTE_TYPES_TABLE_ALIAS).append(PERIOD)
                    .append(tableMapper.getEntityTypesAttributeTypesTableAttributeTypeIdField())
                    .append(SP).append(EQ).append(SP)
                    .append(ATTRIBUTE_TYPES_TABLE_ALIAS).append(PERIOD).append(ID_COLUMN).append(NL);
            sqlBuilder.append(LEFT_JOIN).append(SP).append(DATA_TYPES_TABLE).append(SP).append(DATA_TYPES_TABLE_ALIAS)
                    .append(SP).append(ON).append(SP).append(ATTRIBUTE_TYPES_TABLE_ALIAS).append(PERIOD)
                    .append(DATA_TYPE_COLUMN).append(SP).append(EQ).append(SP)
                    .append(DATA_TYPES_TABLE_ALIAS).append(PERIOD).append(ID_COLUMN).append(NL);

            sqlBuilder.append(LEFT_JOIN).append(SP).append(TableNames.CONTROLLED_VOCABULARY_TERM_TABLE).append(SP)
                    .append(CONTROLLED_VOCABULARY_TERMS_TABLE_ALIAS).append(SP).append(ON).append(SP)
                    .append(PROPERTIES_TABLE_ALIAS).append(PERIOD)
                    .append(VOCABULARY_TERM_COLUMN).append(SP).append(EQ).append(SP)
                    .append(CONTROLLED_VOCABULARY_TERMS_TABLE_ALIAS).append(PERIOD)
                    .append(ID_COLUMN).append(NL);

            sqlBuilder.append(LEFT_JOIN).append(SP).append(MATERIAL.getEntitiesTable()).append(SP)
                    .append(MATERIALS_TABLE_ALIAS).append(SP).append(ON).append(SP).append(PROPERTIES_TABLE_ALIAS)
                    .append(PERIOD).append(MATERIAL_PROP_COLUMN).append(SP).append(EQ).append(SP)
                    .append(MATERIALS_TABLE_ALIAS).append(PERIOD).append(ID_COLUMN).append(NL);

            if (tableMapper == TableMapper.SAMPLE || tableMapper == TableMapper.EXPERIMENT
                    || tableMapper == TableMapper.DATA_SET)
            {
                sqlBuilder.append(LEFT_JOIN).append(SP).append(SAMPLE.getEntitiesTable()).append(SP)
                        .append(SAMPLES_TABLE_ALIAS).append(SP).append(ON).append(SP).append(PROPERTIES_TABLE_ALIAS)
                        .append(PERIOD).append(SAMPLE_PROP_COLUMN).append(SP).append(EQ).append(SP)
                        .append(SAMPLES_TABLE_ALIAS).append(PERIOD).append(ID_COLUMN).append(NL);
            }
        }

        buildProjectAndSpacesJoin(sqlBuilder, tableMapper);
        buildEntityTypesJoin(sqlBuilder, tableMapper);

        if (tableMapper == SAMPLE)
        {
            sqlBuilder.append(LEFT_JOIN).append(SP).append(SAMPLE.getEntitiesTable())
                    .append(SP).append(CONTAINER_TABLE_ALIAS)
                    .append(SP).append(ON).append(SP).append(MAIN_TABLE_ALIAS).append(PERIOD)
                    .append(PART_OF_SAMPLE_COLUMN)
                    .append(SP).append(EQ).append(SP).append(CONTAINER_TABLE_ALIAS).append(PERIOD).append(ID_COLUMN)
                    .append(NL);
        }
    }

    private static void buildEntityTypesJoin(final StringBuilder sqlBuilder, final TableMapper tableMapper)
    {
        sqlBuilder.append(LEFT_JOIN).append(SP).append(tableMapper.getEntityTypesTable()).append(SP)
                .append(ENTITY_TYPES_TABLE_ALIAS)
                .append(SP).append(ON).append(SP).append(MAIN_TABLE_ALIAS).append(PERIOD)
                .append(tableMapper.getEntitiesTableEntityTypeIdField())
                .append(SP).append(EQ).append(SP).append(ENTITY_TYPES_TABLE_ALIAS).append(PERIOD)
                .append(ID_COLUMN).append(NL);
    }

    private static void buildProjectAndSpacesJoin(final StringBuilder sqlBuilder, final TableMapper tableMapper)
    {
        final boolean hasSpaces = hasSpaces(tableMapper);
        if (hasProjects(tableMapper))
        {
            sqlBuilder.append(LEFT_JOIN).append(SP).append(PROJECT.getEntitiesTable()).append(SP)
                    .append(PROJECT_TABLE_ALIAS).append(SP)
                    .append(ON).append(SP).append(MAIN_TABLE_ALIAS).append(PERIOD).append(PROJECT_COLUMN).append(SP)
                    .append(EQ).append(SP)
                    .append(PROJECT_TABLE_ALIAS).append(PERIOD).append(ID_COLUMN).append(NL);
            if (!hasSpaces)
            {
                sqlBuilder.append(LEFT_JOIN).append(SP).append(TableMapper.SPACE.getEntitiesTable()).append(SP)
                        .append(SPACE_TABLE_ALIAS).append(SP)
                        .append(ON).append(SP).append(PROJECT_TABLE_ALIAS).append(PERIOD).append(SPACE_COLUMN)
                        .append(SP).append(EQ).append(SP)
                        .append(SPACE_TABLE_ALIAS).append(PERIOD).append(ID_COLUMN).append(NL);
            }
        }

        if (hasSpaces)
        {
            sqlBuilder.append(LEFT_JOIN).append(SP).append(TableMapper.SPACE.getEntitiesTable()).append(SP)
                    .append(SPACE_TABLE_ALIAS).append(SP)
                    .append(ON).append(SP).append(MAIN_TABLE_ALIAS).append(PERIOD).append(SPACE_COLUMN)
                    .append(SP).append(EQ).append(SP)
                    .append(SPACE_TABLE_ALIAS).append(PERIOD).append(ID_COLUMN).append(NL);
        }
    }

    private static boolean hasProjects(final TableMapper tableMapper)
    {
        final String entitiesTable = tableMapper.getEntitiesTable();
        return SAMPLE.getEntitiesTable().equals(entitiesTable) || EXPERIMENT.getEntitiesTable().equals(entitiesTable);
    }

    private static boolean hasSpaces(final TableMapper tableMapper)
    {
        final String entitiesTable = tableMapper.getEntitiesTable();
        return SAMPLE.getEntitiesTable().equals(entitiesTable) || PROJECT.getEntitiesTable().equals(entitiesTable);
    }

    private static boolean hasExperiments(final TableMapper tableMapper)
    {
        final String entitiesTable = tableMapper.getEntitiesTable();
        return SAMPLE.getEntitiesTable().equals(entitiesTable) || DATA_SET.getEntitiesTable().equals(entitiesTable);
    }

    private static void buildDetailsWhere(final StringBuilder sqlBuilder, final TranslationContext translationContext,
            final GlobalSearchTextCriteria criterion, final Collection<Long> resultIds, final TableMapper tableMapper,
            final boolean forAttributes)
    {
        final List<Object> args = translationContext.getArgs();
        sqlBuilder.append(WHERE).append(SP).append(MAIN_TABLE_ALIAS).append(PERIOD)
                .append(ID_COLUMN).append(SP).append(IN).append(SP).append(SELECT_UNNEST);
        args.add(resultIds.toArray(new Long[0]));

        sqlBuilder.append(SP).append(AND).append(SP).append(LP);

        if (forAttributes)
        {
            buildAttributesMatchCondition(sqlBuilder, criterion, args);
        } else
        {
            buildTsVectorMatch(sqlBuilder, criterion.getFieldValue(), tableMapper, args);
        }
        sqlBuilder.append(RP).append(NL);
    }

    private static String getPermId(final TableMapper tableMapper)
    {
        return AttributesMapper.getColumnName(PERM_ID_ATTRIBUTE, tableMapper.getEntitiesTable(), null);
    }

    private static void buildTsVectorMatch(final StringBuilder sqlBuilder, final AbstractStringValue stringValue,
            final TableMapper tableMapper, final List<Object> args)
    {
        sqlBuilder.append(GlobalSearchCriteriaTranslator.PROPERTIES_TABLE_ALIAS)
                .append(PERIOD).append(TS_VECTOR_COLUMN).append(SP)
                .append(DOUBLE_AT).append(SP);
        buildTsQueryPart(sqlBuilder, stringValue, args);

        sqlBuilder.append(SP).append(OR).append(SP);

        sqlBuilder.append(MATERIALS_TABLE_ALIAS).append(PERIOD).append(TS_VECTOR_COLUMN).append(SP)
                .append(DOUBLE_AT).append(SP).append(QU).append(DOUBLE_COLON).append(TSQUERY);
        args.add(toTsQueryText(stringValue));

        if (tableMapper == TableMapper.SAMPLE || tableMapper == TableMapper.EXPERIMENT
                || tableMapper == TableMapper.DATA_SET)
        {
            sqlBuilder.append(SP).append(OR).append(SP);

            sqlBuilder.append(SAMPLES_TABLE_ALIAS).append(PERIOD).append(TS_VECTOR_COLUMN).append(SP)
                    .append(DOUBLE_AT).append(SP).append(QU).append(DOUBLE_COLON).append(TSQUERY);
            args.add(toTsQueryText(stringValue));
        }
    }

    private static void buildTsQueryPart(final StringBuilder sqlBuilder, final AbstractStringValue stringValue,
            final List<Object> args)
    {
        sqlBuilder.append(TO_TSQUERY).append(LP).append(SQ).append(REG_CONFIG).append(SQ).append(COMMA).append(SP)
                .append(QU).append(RP);
        args.add(toTsQueryText(stringValue));
    }

    private static void buildCastingTsQueryPart(final StringBuilder sqlBuilder,
            final AbstractStringValue stringValue, final List<Object> args)
    {
        sqlBuilder.append(QU).append(DOUBLE_COLON).append(TSQUERY);
        args.add(toTsQueryText(stringValue));
    }

    private static String toTsQueryText(final AbstractStringValue stringValue)
    {
        return (StringContainsExactlyValue.class.isAssignableFrom(stringValue.getClass()))
                ? '\'' + stringValue.getValue().toLowerCase().replaceAll("'", "''") + '\''
                : stringValue.getValue().toLowerCase().replaceAll("['&|:!()<>]", " ").trim().replaceAll("\\s+", " | ");
    }

}
