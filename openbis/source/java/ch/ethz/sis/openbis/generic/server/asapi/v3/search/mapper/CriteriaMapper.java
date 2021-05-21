/*
 * Copyright 2016 ETH Zuerich, CISD
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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.*;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.*;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.search.*;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.NoExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.search.LabelSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.GlobalSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.MaterialSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.MaterialTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search.*;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search.NoProjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search.ProjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.search.PropertyAssignmentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.search.PropertyTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.*;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.search.SemanticAnnotationSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.NoSpaceSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.search.TagSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.*;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.*;
import org.springframework.context.ApplicationContext;

import java.util.*;

import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.*;

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
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(EventEntityTypeSearchCriteria.class, enumFieldConditionTranslator);
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
