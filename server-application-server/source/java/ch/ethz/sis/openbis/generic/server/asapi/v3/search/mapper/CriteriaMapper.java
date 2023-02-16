/*
 * Copyright ETH 2020 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper;

import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.DATA_SET_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.DATA_SET_TYPE_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.EXPERIMENT_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.EXPERIMENT_TYPE_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.EXTERNAL_DATA_MANAGEMENT_SYSTEM_ID_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.FILE_FORMAT_TYPE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.ID_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.LOCATOR_TYPE_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.MATERIAL_TYPE_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.PART_OF_SAMPLE_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.PERSON_MODIFIER_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.PERSON_REGISTERER_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.PROJECT_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.PROPERTY_TYPE_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.SAMPLE_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.SAMPLE_TYPE_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.SAMPLE_TYPE_PROPERTY_TYPE_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.SPACE_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.STORAGE_FORMAT_COLUMN;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AnyBooleanPropertySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AnyDatePropertySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AnyFieldSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AnyNumberPropertySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AnyPropertySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AnyStringPropertySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.BooleanPropertySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.CodeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.CodesSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.CollectionFieldSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.DateFieldSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.DatePropertySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.IdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.IdentifierSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.IdsSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ModificationDateSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.NameSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.NumberFieldSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.NumberPropertySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.PermIdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.RegistrationDateSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SamplePropertySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StrictlyStringPropertySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringFieldSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringPropertySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.TextAttributeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ControlledVocabularyPropertySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.ArchivingRequestedSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.CompleteSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.ContentCopySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.ExternalCodeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.ExternalDmsSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.FileFormatTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.GitCommitHashSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.GitRepositoryIdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.LinkedDataSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.LocationSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.LocatorTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.PathSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.PhysicalDataSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.PresentInArchiveSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.ShareIdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.SizeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.SpeedHintSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.StatusSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.StorageConfirmationSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.StorageFormatSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.search.EventDescriptionSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.search.EventEntityProjectIdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.search.EventEntityProjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.search.EventEntityRegistrationDateSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.search.EventEntityRegistratorSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.search.EventEntitySpaceIdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.search.EventEntitySpaceSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.search.EventEntityTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.search.EventIdentifierSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.search.EventReasonSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.search.EventSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.search.EventTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.NoExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.search.LabelSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.GlobalSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.MaterialSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.MaterialTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search.EmailSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search.FirstNameSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search.LastNameSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search.ModifierSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search.RegistratorSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search.UserIdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search.UserIdsSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search.NoProjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search.ProjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.search.PropertyAssignmentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.search.PropertyTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.ListableSampleTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.NoSampleContainerSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.NoSampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleContainerSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.search.SemanticAnnotationSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.NoSpaceSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.search.TagSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DataSetKindSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.EventSearchManager;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.IGlobalSearchManager;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ILocalSearchManager;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ISearchManager;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.AbsenceConditionTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.AnyFieldSearchConditionTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.AnyPropertySearchConditionTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.BooleanFieldSearchConditionTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.CodeSearchConditionTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.CollectionFieldSearchConditionTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.CompleteSearchConditionTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.ControlledVocabularyPropertySearchConditionTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.DataSetKindSearchConditionTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.DateFieldSearchConditionTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.EmailSearchConditionTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.EnumFieldSearchConditionTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.EventEntityTypeConditionTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.FirstNameSearchConditionTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.IConditionTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.IdSearchConditionTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.IdentifierSearchConditionTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.LastNameSearchConditionTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.ListableSampleTypeSearchConditionTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.NameSearchConditionTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.NumberFieldSearchConditionTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.SamplePropertySearchConditionTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.StringFieldSearchConditionTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.TextAttributeConditionTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.UserIdSearchConditionTranslator;

/**
 * Mapper from criteria to translators or to managers.
 *
 * @author Viktor Kovtun
 */
public class CriteriaMapper
{

    private static final Map<Class<? extends ISearchCriteria>, ISearchManager>
            CRITERIA_TO_MANAGER_MAP = new HashMap<>();

    /**
     * This map is used when subqeury is not needed. Either no tables should be joined or they are joined in the
     * FROM clause.
     */
    private static final Map<Class<? extends ISearchCriteria>, IConditionTranslator<? extends ISearchCriteria>>
            CRITERIA_TO_CONDITION_TRANSLATOR_MAP = new HashMap<>();

    /**
     * This map is used for the special case when EntityTypeSearchCriteria should be substituted by a concrete
     * criterion.
     */
    private static final Map<EntityKind, ILocalSearchManager<ISearchCriteria, ?, ?>> ENTITY_KIND_TO_MANAGER_MAP =
            new EnumMap<>(EntityKind.class);

    /**
     * This map is used when a subquery manager is used. It maps criteria to a column name which is on the left of the
     * "IN" statement.
     */
    private static final Map<Class<? extends ISearchCriteria>, String> CRITERIA_TO_IN_COLUMN_MAP = new HashMap<>();

    /**
     * This map is used do set an ID different from default for subqueries. The key is the couple (parent, child).
     */
    private static final Map<List<Class<? extends ISearchCriteria>>, String>
            PARENT_CHILD_CRITERIA_TO_CHILD_SELECT_ID_MAP = new HashMap<>();

    static
    {
        init();
    }

    private CriteriaMapper()
    {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    private static void init()
    {
        final StringFieldSearchConditionTranslator stringFieldSearchConditionTranslator =
                new StringFieldSearchConditionTranslator();
        final BooleanFieldSearchConditionTranslator booleanFieldSearchConditionTranslator =
                new BooleanFieldSearchConditionTranslator();
        final DateFieldSearchConditionTranslator dateFieldSearchConditionTranslator =
                new DateFieldSearchConditionTranslator();
        final NumberFieldSearchConditionTranslator numberFieldSearchConditionTranslator =
                new NumberFieldSearchConditionTranslator();
        final CollectionFieldSearchConditionTranslator collectionFieldSearchConditionTranslator =
                new CollectionFieldSearchConditionTranslator();
        final AbsenceConditionTranslator absenceConditionTranslator = new AbsenceConditionTranslator();
        final CodeSearchConditionTranslator codeSearchConditionTranslator = new CodeSearchConditionTranslator();
        final EnumFieldSearchConditionTranslator enumFieldConditionTranslator =
                new EnumFieldSearchConditionTranslator();

        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(AnyFieldSearchCriteria.class, new AnyFieldSearchConditionTranslator());
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(AnyBooleanPropertySearchCriteria.class,
                booleanFieldSearchConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(AnyDatePropertySearchCriteria.class,
                dateFieldSearchConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(AnyNumberPropertySearchCriteria.class,
                numberFieldSearchConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(AnyPropertySearchCriteria.class,
                new AnyPropertySearchConditionTranslator());
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(AnyStringPropertySearchCriteria.class,
                stringFieldSearchConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(ArchivingRequestedSearchCriteria.class,
                booleanFieldSearchConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(BooleanPropertySearchCriteria.class,
                booleanFieldSearchConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(CodeSearchCriteria.class, codeSearchConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(CodesSearchCriteria.class, collectionFieldSearchConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(CollectionFieldSearchCriteria.class,
                collectionFieldSearchConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(ControlledVocabularyPropertySearchCriteria.class,
                new ControlledVocabularyPropertySearchConditionTranslator());
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(CompleteSearchCriteria.class, new CompleteSearchConditionTranslator());
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(DataSetKindSearchCriteria.class,
                new DataSetKindSearchConditionTranslator());
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(DateFieldSearchCriteria.class, dateFieldSearchConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(DatePropertySearchCriteria.class, dateFieldSearchConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(EmailSearchCriteria.class, new EmailSearchConditionTranslator());
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(ExternalCodeSearchCriteria.class,
                stringFieldSearchConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(FirstNameSearchCriteria.class,
                new FirstNameSearchConditionTranslator());
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(GitCommitHashSearchCriteria.class,
                stringFieldSearchConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(GitRepositoryIdSearchCriteria.class,
                stringFieldSearchConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(IdSearchCriteria.class, new IdSearchConditionTranslator());
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(IdsSearchCriteria.class, collectionFieldSearchConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(IdentifierSearchCriteria.class,
                new IdentifierSearchConditionTranslator());
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(LabelSearchCriteria.class, stringFieldSearchConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(LastNameSearchCriteria.class, new LastNameSearchConditionTranslator());
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(ListableSampleTypeSearchCriteria.class,
                new ListableSampleTypeSearchConditionTranslator());
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(LocationSearchCriteria.class, stringFieldSearchConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(ModificationDateSearchCriteria.class,
                dateFieldSearchConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(NameSearchCriteria.class, new NameSearchConditionTranslator());
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(NoExperimentSearchCriteria.class, absenceConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(NoProjectSearchCriteria.class, absenceConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(NoSampleContainerSearchCriteria.class, absenceConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(NoSampleSearchCriteria.class, absenceConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(NoSpaceSearchCriteria.class, absenceConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(NumberFieldSearchCriteria.class, numberFieldSearchConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(NumberPropertySearchCriteria.class,
                numberFieldSearchConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(PathSearchCriteria.class, stringFieldSearchConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(PermIdSearchCriteria.class, stringFieldSearchConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(PresentInArchiveSearchCriteria.class,
                booleanFieldSearchConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(RegistrationDateSearchCriteria.class,
                dateFieldSearchConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(SamplePropertySearchCriteria.class,
                new SamplePropertySearchConditionTranslator());
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(ShareIdSearchCriteria.class, stringFieldSearchConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(SizeSearchCriteria.class, numberFieldSearchConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(SpeedHintSearchCriteria.class, numberFieldSearchConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(StatusSearchCriteria.class, enumFieldConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(StorageConfirmationSearchCriteria.class,
                booleanFieldSearchConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(StrictlyStringPropertySearchCriteria.class,
                stringFieldSearchConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(StringFieldSearchCriteria.class, stringFieldSearchConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(StringPropertySearchCriteria.class,
                stringFieldSearchConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(TextAttributeSearchCriteria.class,
                new TextAttributeConditionTranslator());
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(UserIdSearchCriteria.class, new UserIdSearchConditionTranslator());
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(UserIdsSearchCriteria.class, collectionFieldSearchConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(EventTypeSearchCriteria.class, enumFieldConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(EventIdentifierSearchCriteria.class, stringFieldSearchConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(EventDescriptionSearchCriteria.class, stringFieldSearchConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(EventReasonSearchCriteria.class, stringFieldSearchConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(EventEntityTypeSearchCriteria.class, new EventEntityTypeConditionTranslator());
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(EventEntitySpaceSearchCriteria.class, stringFieldSearchConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(EventEntitySpaceIdSearchCriteria.class, stringFieldSearchConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(EventEntityProjectSearchCriteria.class, stringFieldSearchConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(EventEntityProjectIdSearchCriteria.class, stringFieldSearchConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(EventEntityRegistratorSearchCriteria.class, stringFieldSearchConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(EventEntityRegistrationDateSearchCriteria.class, dateFieldSearchConditionTranslator);

        // When adding a new manager to CRITERIA_TO_IN_COLUMN_MAP, create the manager as a bean in
        // genericApplicationContext.xml and add the corresponding record in initCriteriaToManagerMap().
        CRITERIA_TO_IN_COLUMN_MAP.put(DataSetSearchCriteria.class, DATA_SET_COLUMN);
        CRITERIA_TO_IN_COLUMN_MAP.put(DataSetTypeSearchCriteria.class, DATA_SET_TYPE_COLUMN);
        CRITERIA_TO_IN_COLUMN_MAP.put(ExperimentSearchCriteria.class, EXPERIMENT_COLUMN);
        CRITERIA_TO_IN_COLUMN_MAP.put(ExperimentTypeSearchCriteria.class, EXPERIMENT_TYPE_COLUMN);
        CRITERIA_TO_IN_COLUMN_MAP.put(ExternalDmsSearchCriteria.class, EXTERNAL_DATA_MANAGEMENT_SYSTEM_ID_COLUMN);
        CRITERIA_TO_IN_COLUMN_MAP.put(MaterialTypeSearchCriteria.class, MATERIAL_TYPE_COLUMN);
        CRITERIA_TO_IN_COLUMN_MAP.put(ModifierSearchCriteria.class, PERSON_MODIFIER_COLUMN);
        CRITERIA_TO_IN_COLUMN_MAP.put(ProjectSearchCriteria.class, PROJECT_COLUMN);
        CRITERIA_TO_IN_COLUMN_MAP.put(PropertyTypeSearchCriteria.class, PROPERTY_TYPE_COLUMN);
        CRITERIA_TO_IN_COLUMN_MAP.put(RegistratorSearchCriteria.class, PERSON_REGISTERER_COLUMN);
        CRITERIA_TO_IN_COLUMN_MAP.put(SampleContainerSearchCriteria.class, PART_OF_SAMPLE_COLUMN);
        CRITERIA_TO_IN_COLUMN_MAP.put(SampleSearchCriteria.class, SAMPLE_COLUMN);
        CRITERIA_TO_IN_COLUMN_MAP.put(SampleTypeSearchCriteria.class, SAMPLE_TYPE_COLUMN);
        CRITERIA_TO_IN_COLUMN_MAP.put(SpaceSearchCriteria.class, SPACE_COLUMN);
        CRITERIA_TO_IN_COLUMN_MAP.put(FileFormatTypeSearchCriteria.class, FILE_FORMAT_TYPE);
        CRITERIA_TO_IN_COLUMN_MAP.put(LocatorTypeSearchCriteria.class, LOCATOR_TYPE_COLUMN);
        CRITERIA_TO_IN_COLUMN_MAP.put(StorageFormatSearchCriteria.class, STORAGE_FORMAT_COLUMN);

        CRITERIA_TO_IN_COLUMN_MAP.put(ContentCopySearchCriteria.class, ID_COLUMN);
        CRITERIA_TO_IN_COLUMN_MAP.put(LinkedDataSearchCriteria.class, ID_COLUMN);
        CRITERIA_TO_IN_COLUMN_MAP.put(MaterialSearchCriteria.class, ID_COLUMN);
        CRITERIA_TO_IN_COLUMN_MAP.put(PhysicalDataSearchCriteria.class, ID_COLUMN);
        CRITERIA_TO_IN_COLUMN_MAP.put(PropertyAssignmentSearchCriteria.class, ID_COLUMN);
        CRITERIA_TO_IN_COLUMN_MAP.put(SemanticAnnotationSearchCriteria.class, ID_COLUMN);
        CRITERIA_TO_IN_COLUMN_MAP.put(TagSearchCriteria.class, ID_COLUMN);

        PARENT_CHILD_CRITERIA_TO_CHILD_SELECT_ID_MAP.put(
                Arrays.asList(PropertyTypeSearchCriteria.class, SemanticAnnotationSearchCriteria.class),
                PROPERTY_TYPE_COLUMN);

        PARENT_CHILD_CRITERIA_TO_CHILD_SELECT_ID_MAP.put(
                Arrays.asList(PropertyAssignmentSearchCriteria.class, SemanticAnnotationSearchCriteria.class),
                SAMPLE_TYPE_PROPERTY_TYPE_COLUMN);

        PARENT_CHILD_CRITERIA_TO_CHILD_SELECT_ID_MAP.put(
                Arrays.asList(SampleTypeSearchCriteria.class, PropertyAssignmentSearchCriteria.class),
                SAMPLE_TYPE_COLUMN);

        PARENT_CHILD_CRITERIA_TO_CHILD_SELECT_ID_MAP.put(
                Arrays.asList(SampleTypeSearchCriteria.class, SemanticAnnotationSearchCriteria.class),
                SAMPLE_TYPE_COLUMN);
    }

    @SuppressWarnings("unchecked")
    public static void initCriteriaToManagerMap(final ApplicationContext applicationContext)
    {
        final ILocalSearchManager<ISearchCriteria, ?, ?> sampleTypeSearchManager = applicationContext.getBean("sample-type-search-manager",
                ILocalSearchManager.class);
        final ILocalSearchManager<ISearchCriteria, ?, ?> experimentTypeSearchManager = applicationContext.getBean("experiment-type-search-manager",
                ILocalSearchManager.class);
        final ILocalSearchManager<ISearchCriteria, ?, ?> materialTypeSearchManager = applicationContext.getBean("material-type-search-manager",
                ILocalSearchManager.class);
        final ILocalSearchManager<ISearchCriteria, ?, ?> dataSetTypeSearchManager = applicationContext.getBean("data-set-type-search-manager",
                ILocalSearchManager.class);

        ENTITY_KIND_TO_MANAGER_MAP.put(EntityKind.SAMPLE, sampleTypeSearchManager);
        ENTITY_KIND_TO_MANAGER_MAP.put(EntityKind.EXPERIMENT, experimentTypeSearchManager);
        ENTITY_KIND_TO_MANAGER_MAP.put(EntityKind.DATA_SET, dataSetTypeSearchManager);
        ENTITY_KIND_TO_MANAGER_MAP.put(EntityKind.MATERIAL, materialTypeSearchManager);

        CRITERIA_TO_MANAGER_MAP.put(ContentCopySearchCriteria.class,
                applicationContext.getBean("content-copy-search-manager", ILocalSearchManager.class));
        CRITERIA_TO_MANAGER_MAP.put(DataSetSearchCriteria.class,
                applicationContext.getBean("data-set-search-manager", ILocalSearchManager.class));
        CRITERIA_TO_MANAGER_MAP.put(DataSetTypeSearchCriteria.class, dataSetTypeSearchManager);
        CRITERIA_TO_MANAGER_MAP.put(ExperimentSearchCriteria.class,
                applicationContext.getBean("experiment-search-manager", ILocalSearchManager.class));
        CRITERIA_TO_MANAGER_MAP.put(ExperimentTypeSearchCriteria.class, experimentTypeSearchManager);
        CRITERIA_TO_MANAGER_MAP.put(SampleSearchCriteria.class,
                applicationContext.getBean("sample-search-manager", ILocalSearchManager.class));
        CRITERIA_TO_MANAGER_MAP.put(SampleTypeSearchCriteria.class, sampleTypeSearchManager);
        CRITERIA_TO_MANAGER_MAP.put(SampleContainerSearchCriteria.class,
                applicationContext.getBean("sample-container-search-manager", ILocalSearchManager.class));
        CRITERIA_TO_MANAGER_MAP.put(RegistratorSearchCriteria.class,
                applicationContext.getBean("person-search-manager", ILocalSearchManager.class));
        CRITERIA_TO_MANAGER_MAP.put(ModifierSearchCriteria.class,
                applicationContext.getBean("person-search-manager", ILocalSearchManager.class));
        CRITERIA_TO_MANAGER_MAP.put(ProjectSearchCriteria.class,
                applicationContext.getBean("project-search-manager", ILocalSearchManager.class));
        CRITERIA_TO_MANAGER_MAP.put(SpaceSearchCriteria.class,
                applicationContext.getBean("space-search-manager", ILocalSearchManager.class));
        CRITERIA_TO_MANAGER_MAP.put(TagSearchCriteria.class,
                applicationContext.getBean("tag-search-manager", ILocalSearchManager.class));
        CRITERIA_TO_MANAGER_MAP.put(SemanticAnnotationSearchCriteria.class,
                applicationContext.getBean("semantic-annotation-search-manager", ILocalSearchManager.class));
        CRITERIA_TO_MANAGER_MAP.put(PropertyAssignmentSearchCriteria.class,
                applicationContext.getBean("property-assignment-search-manager", ILocalSearchManager.class));
        CRITERIA_TO_MANAGER_MAP.put(PropertyTypeSearchCriteria.class,
                applicationContext.getBean("property-type-search-manager", ILocalSearchManager.class));
        CRITERIA_TO_MANAGER_MAP.put(LinkedDataSearchCriteria.class,
                applicationContext.getBean("linked-data-set-kind-search-manager", ILocalSearchManager.class));
        CRITERIA_TO_MANAGER_MAP.put(PhysicalDataSearchCriteria.class,
                applicationContext.getBean("physical-data-set-kind-search-manager", ILocalSearchManager.class));
        CRITERIA_TO_MANAGER_MAP.put(ExternalDmsSearchCriteria.class,
                applicationContext.getBean("external-dms-search-manager", ILocalSearchManager.class));
        CRITERIA_TO_MANAGER_MAP.put(FileFormatTypeSearchCriteria.class,
                applicationContext.getBean("ffty-search-manager", ILocalSearchManager.class));
        CRITERIA_TO_MANAGER_MAP.put(LocatorTypeSearchCriteria.class,
                applicationContext.getBean("locator-type-search-manager", ILocalSearchManager.class));
        CRITERIA_TO_MANAGER_MAP.put(StorageFormatSearchCriteria.class,
                applicationContext.getBean("storage-format-search-manager", ILocalSearchManager.class));
        CRITERIA_TO_MANAGER_MAP.put(MaterialSearchCriteria.class,
                applicationContext.getBean("material-search-manager", ILocalSearchManager.class));
        CRITERIA_TO_MANAGER_MAP.put(MaterialTypeSearchCriteria.class, materialTypeSearchManager);
        CRITERIA_TO_MANAGER_MAP.put(GlobalSearchCriteria.class,
                applicationContext.getBean("global-search-manager", IGlobalSearchManager.class));
        CRITERIA_TO_MANAGER_MAP.put(EventSearchCriteria.class, applicationContext.getBean(EventSearchManager.class));
    }

    public static Map<Class<? extends ISearchCriteria>, ISearchManager> getCriteriaToManagerMap()
    {
        return CRITERIA_TO_MANAGER_MAP;
    }

    public static Map<Class<? extends ISearchCriteria>, IConditionTranslator<? extends ISearchCriteria>>
    getCriteriaToConditionTranslatorMap()
    {
        return CRITERIA_TO_CONDITION_TRANSLATOR_MAP;
    }

    public static Map<Class<? extends ISearchCriteria>, String> getCriteriaToInColumnMap()
    {
        return CRITERIA_TO_IN_COLUMN_MAP;
    }

    public static Map<List<Class<? extends ISearchCriteria>>, String> getParentChildCriteriaToChildSelectIdMap()
    {
        return PARENT_CHILD_CRITERIA_TO_CHILD_SELECT_ID_MAP;
    }

    public static Map<EntityKind, ILocalSearchManager<ISearchCriteria, ?, ?>> getEntityKindToManagerMap()
    {
        return ENTITY_KIND_TO_MANAGER_MAP;
    }

}
