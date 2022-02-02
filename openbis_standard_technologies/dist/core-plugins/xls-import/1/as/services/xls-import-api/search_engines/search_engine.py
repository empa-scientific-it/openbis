from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions import DataSetTypeFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search import DataSetTypeSearchCriteria, \
    SearchDataSetTypesOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions import ExperimentTypeFetchOptions, \
    ExperimentFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search import ExperimentTypeSearchCriteria, \
    SearchExperimentTypesOperation, ExperimentSearchCriteria, SearchExperimentsOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.operation import SynchronousOperationExecutionOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.fetchoptions import PluginFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.search import PluginSearchCriteria, SearchPluginsOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions import ProjectFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search import ProjectSearchCriteria, SearchProjectsOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions import PropertyTypeFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.property.search import PropertyTypeSearchCriteria, \
    SearchPropertyTypesOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions import SampleTypeFetchOptions, SampleFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search import SampleTypeSearchCriteria, \
    SearchSampleTypesOperation, SampleSearchCriteria, SearchSamplesOperation, \
    SampleSearchRelation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions import SpaceFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search import SpaceSearchCriteria, SearchSpacesOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions import VocabularyFetchOptions, \
    VocabularyTermFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.search import VocabularySearchCriteria, \
    SearchVocabulariesOperation, \
    SearchVocabularyTermsOperation, VocabularyTermSearchCriteria
from parsers import PropertyTypeDefinitionToCreationType, VocabularyDefinitionToCreationType, \
    VocabularyTermDefinitionToCreationType, \
    PropertyAssignmentDefinitionToCreationType, SampleTypeDefinitionToCreationType, \
    ExperimentTypeDefinitionToCreationType, \
    DatasetTypeDefinitionToCreationType, SpaceDefinitionToCreationType, ProjectDefinitionToCreationType, \
    ExperimentDefinitionToCreationType, \
    SampleDefinitionToCreationType, ScriptDefinitionToCreationType
from search_criteria_factory import DefaultCreationElementSearchCriteria, SampleCreationSampleSearchCriteria, \
    VocabularyTermCreationVocabularyTermSearchCriteria, ProjectFromProjectCreationSearchCriteria, \
    ScriptCreationScriptSearchCriteria, EntityCreationEntityTypeSearchCriteria, \
    FindAllSearchCriteria, SpaceFromPropertiesSearchCriteria, ProjectFromPropertiesSearchCriteria, \
    ExperimentFromPropertiesSearchCriteria, SampleCreationSampleChildrenParentSearchCriteria
from ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id import VocabularyPermId


class SearchEngine():

    def __init__(self, api, session_token):
        self.api = api
        self.session_token = session_token

    def get_excel_version_vocabulary(self, vocabulary_code):
        code_perm_id = VocabularyPermId(vocabulary_code)
        vocabulary_ids = [code_perm_id]
        vocabulary_fetch_options = VocabularyFetchOptions()
        vocabulary_fetch_options.withTerms().withVocabulary()
        xls_version_vocabulary = self.api.getVocabularies(self.session_token, vocabulary_ids,
                                                          vocabulary_fetch_options).get(code_perm_id)
        return xls_version_vocabulary

    def find_all_existing_vocabularies(self):
        vocabulary_fetch_options = VocabularyFetchOptions()
        vocabulary_fetch_options.withTerms().withVocabulary()

        strategy = {
            'creations_type': VocabularyDefinitionToCreationType,
            'search_criteria_build_strategy': FindAllSearchCriteria,
            'search_criteria_class': VocabularySearchCriteria,
            'search_operation': SearchVocabulariesOperation,
            'fetch_options': vocabulary_fetch_options
        }

        existing_elements = {}
        creations_type = strategy['creations_type']
        search_criteria_class = strategy['search_criteria_class']
        search_criteria_builder = strategy['search_criteria_build_strategy'](search_criteria_class)
        search_criterias = search_criteria_builder.get_search_criteria()
        existing_specific_elements = self._get_existing_elements(search_criterias, **strategy)
        if existing_specific_elements is not None:
            existing_elements[creations_type] = existing_specific_elements

        return existing_elements

    def find_existing_entity_kind_definitions_for(self, creations):
        experiment_fetch_options = ExperimentTypeFetchOptions()
        experiment_fetch_options.withPropertyAssignments().withPropertyType().withVocabulary().withTerms().withVocabulary()
        sample_fetch_options = SampleTypeFetchOptions()
        sample_fetch_options.withPropertyAssignments().withPropertyType().withVocabulary().withTerms().withVocabulary()

        search_strategy = [
            {
                'creations_type': SampleDefinitionToCreationType,
                'search_criteria_build_strategy': EntityCreationEntityTypeSearchCriteria,
                'search_criteria_class': SampleTypeSearchCriteria,
                'search_operation': SearchSampleTypesOperation,
                'fetch_options': sample_fetch_options
            },
            {
                'creations_type': ExperimentDefinitionToCreationType,
                'search_criteria_build_strategy': EntityCreationEntityTypeSearchCriteria,
                'search_criteria_class': ExperimentTypeSearchCriteria,
                'search_operation': SearchExperimentTypesOperation,
                'fetch_options': experiment_fetch_options
            }
        ]

        existing_elements = {}
        for strategy in search_strategy:
            creations_type = strategy['creations_type']
            search_criteria_class = strategy['search_criteria_class']
            search_criteria_builder = strategy['search_criteria_build_strategy'](search_criteria_class)
            if creations_type in creations:
                search_criterias = search_criteria_builder.get_search_criteria(creations[creations_type])
                existing_specific_elements = self._get_existing_elements(search_criterias, **strategy)
                if existing_specific_elements is not None:
                    existing_elements[creations_type] = existing_specific_elements

        return existing_elements

    def find_all_existing_elements(self, creations):
        property_type_fetch_options = PropertyTypeFetchOptions()
        property_type_fetch_options.withVocabulary().withTerms().withVocabulary()
        vocabulary_fetch_options = VocabularyFetchOptions()
        vocabulary_fetch_options.withTerms().withVocabulary()
        vocabulary_term_fetch_options = VocabularyTermFetchOptions()
        vocabulary_term_fetch_options.withVocabulary()
        sample_type_fetch_options = SampleTypeFetchOptions()
        sample_type_fetch_options.withPropertyAssignments().withPropertyType()
        experiment_type_fetch_options = ExperimentTypeFetchOptions()
        experiment_type_fetch_options.withPropertyAssignments().withPropertyType()
        dataset_type_fetch_options = DataSetTypeFetchOptions()
        dataset_type_fetch_options.withPropertyAssignments().withPropertyType()
        sample_fetch_options = SampleFetchOptions()
        sample_fetch_options.withType()
        sample_fetch_options.withChildren()
        sample_fetch_options.withParents()
        sample_fetch_options.withProperties()
        experiment_fetch_options = ExperimentFetchOptions()
        experiment_fetch_options.withType()
        search_strategy = [
            {
                'creations_type': VocabularyDefinitionToCreationType,
                'search_criteria_build_strategy': DefaultCreationElementSearchCriteria,
                'search_criteria_class': VocabularySearchCriteria,
                'search_operation': SearchVocabulariesOperation,
                'fetch_options': vocabulary_fetch_options
            },
            {
                'creations_type': VocabularyTermDefinitionToCreationType,
                'search_criteria_build_strategy': VocabularyTermCreationVocabularyTermSearchCriteria,
                'search_criteria_class': VocabularyTermSearchCriteria,
                'search_operation': SearchVocabularyTermsOperation,
                'fetch_options': vocabulary_term_fetch_options
            },
            {
                'creations_type': SampleDefinitionToCreationType,
                'search_criteria_build_strategy': SampleCreationSampleChildrenParentSearchCriteria,
                'search_criteria_class': SampleSearchCriteria,
                'search_operation': SearchSamplesOperation,
                'fetch_options': sample_fetch_options
            },
            {
                'creations_type': PropertyTypeDefinitionToCreationType,
                'search_criteria_build_strategy': DefaultCreationElementSearchCriteria,
                'search_criteria_class': PropertyTypeSearchCriteria,
                'search_operation': SearchPropertyTypesOperation,
                'fetch_options': property_type_fetch_options
            },
            {
                'creations_type': SampleTypeDefinitionToCreationType,
                'search_criteria_build_strategy': DefaultCreationElementSearchCriteria,
                'search_criteria_class': SampleTypeSearchCriteria,
                'search_operation': SearchSampleTypesOperation,
                'fetch_options': sample_type_fetch_options
            },
            {
                'creations_type': ExperimentTypeDefinitionToCreationType,
                'search_criteria_build_strategy': DefaultCreationElementSearchCriteria,
                'search_criteria_class': ExperimentTypeSearchCriteria,
                'search_operation': SearchExperimentTypesOperation,
                'fetch_options': experiment_type_fetch_options
            },
            {
                'creations_type': DatasetTypeDefinitionToCreationType,
                'search_criteria_build_strategy': DefaultCreationElementSearchCriteria,
                'search_criteria_class': DataSetTypeSearchCriteria,
                'search_operation': SearchDataSetTypesOperation,
                'fetch_options': dataset_type_fetch_options
            },
            {
                'creations_type': SpaceDefinitionToCreationType,
                'search_criteria_build_strategy': DefaultCreationElementSearchCriteria,
                'search_criteria_class': SpaceSearchCriteria,
                'search_operation': SearchSpacesOperation,
                'fetch_options': SpaceFetchOptions()
            },
            {
                'creations_type': ProjectDefinitionToCreationType,
                'search_criteria_build_strategy': ProjectFromProjectCreationSearchCriteria,
                'search_criteria_class': ProjectSearchCriteria,
                'search_operation': SearchProjectsOperation,
                'fetch_options': ProjectFetchOptions()
            },
            {
                'creations_type': ProjectDefinitionToCreationType,
                'result_creations_type': SpaceDefinitionToCreationType,
                'search_criteria_build_strategy': SpaceFromPropertiesSearchCriteria,
                'search_criteria_class': SpaceSearchCriteria,
                'search_operation': SearchSpacesOperation,
                'fetch_options': SpaceFetchOptions()
            },
            {
                'creations_type': ExperimentDefinitionToCreationType,
                'search_criteria_build_strategy': DefaultCreationElementSearchCriteria,
                'search_criteria_class': ExperimentSearchCriteria,
                'search_operation': SearchExperimentsOperation,
                'fetch_options': experiment_fetch_options
            },
            {
                'creations_type': ExperimentDefinitionToCreationType,
                'result_creations_type': ProjectDefinitionToCreationType,
                'search_criteria_build_strategy': ProjectFromPropertiesSearchCriteria,
                'search_criteria_class': ProjectSearchCriteria,
                'search_operation': SearchProjectsOperation,
                'fetch_options': ProjectFetchOptions()
            },
            {
                'creations_type': SampleDefinitionToCreationType,
                'search_criteria_build_strategy': SampleCreationSampleSearchCriteria,
                'search_criteria_class': SampleSearchCriteria,
                'search_operation': SearchSamplesOperation,
                'fetch_options': sample_fetch_options
            },
            {
                'creations_type': SampleDefinitionToCreationType,
                'result_creations_type': SpaceDefinitionToCreationType,
                'search_criteria_build_strategy': SpaceFromPropertiesSearchCriteria,
                'search_criteria_class': SpaceSearchCriteria,
                'search_operation': SearchSpacesOperation,
                'fetch_options': SpaceFetchOptions()
            },
            {
                'creations_type': SampleDefinitionToCreationType,
                'result_creations_type': ProjectDefinitionToCreationType,
                'search_criteria_build_strategy': ProjectFromPropertiesSearchCriteria,
                'search_criteria_class': ProjectSearchCriteria,
                'search_operation': SearchProjectsOperation,
                'fetch_options': ProjectFetchOptions()
            },
            {
                'creations_type': SampleDefinitionToCreationType,
                'result_creations_type': ExperimentDefinitionToCreationType,
                'search_criteria_build_strategy': ExperimentFromPropertiesSearchCriteria,
                'search_criteria_class': ExperimentSearchCriteria,
                'search_operation': SearchExperimentsOperation,
                'fetch_options': experiment_fetch_options
            },
            {
                'creations_type': ScriptDefinitionToCreationType,
                'search_criteria_build_strategy': ScriptCreationScriptSearchCriteria,
                'search_criteria_class': PluginSearchCriteria,
                'search_operation': SearchPluginsOperation,
                'fetch_options': PluginFetchOptions()
            }
        ]

        existing_elements = {}
        for strategy in search_strategy:
            creations_type = strategy['creations_type']
            search_criteria_class = strategy['search_criteria_class']
            search_criteria_builder = strategy['search_criteria_build_strategy'](search_criteria_class)
            if creations_type in creations:
                search_criterias = search_criteria_builder.get_search_criteria(creations[creations_type])
                if search_criterias is not None:
                    existing_specific_elements = self._get_existing_elements(search_criterias, **strategy)
                    results_key = strategy[
                        'result_creations_type'] if 'result_creations_type' in strategy else creations_type
                    if existing_specific_elements is not None:
                        if results_key not in existing_elements:
                            existing_elements[results_key] = []
                        # May contain duplicates when i.e Project exists on the server but is also
                        # explicitly mentioned in xls sheet, it is ok.
                        existing_elements[results_key].extend(existing_specific_elements)

        return existing_elements

    def _get_existing_elements(self, search_criterias, **kwargs):
        search_operation = kwargs['search_operation']
        fetch_options = kwargs['fetch_options']
        if not search_criterias:
            return None
        search_criterias = search_criterias if type(search_criterias) == list else [search_criterias]
        operations = [search_operation(search_criteria, fetch_options) for search_criteria in search_criterias]
        return self._execute_search_operation(operations)

    def _execute_search_operation(self, operations):
        execution_results = self.api.executeOperations(self.session_token, operations,
                                                       SynchronousOperationExecutionOptions())

        result_objects = []
        for search_result in execution_results.getResults():
            result_objects.extend(search_result.getSearchResult().getObjects())
        return result_objects
