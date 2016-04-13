	// primitives and abstracts - we don't need to test them as they cannot be instantiated  

	// 'as/dto/common/fetchoptions/CacheMode',
	// 'as/dto/common/search/AbstractCompositeSearchCriteria',
	// 'as/dto/common/search/AbstractDateObjectValue',
	// 'as/dto/common/search/AbstractDateValue',
	// 'as/dto/common/search/AbstractEntitySearchCriteria',
	// 'as/dto/common/search/AbstractFieldSearchCriteria',
	// 'as/dto/common/search/AbstractNumberValue',
	// 'as/dto/common/search/AbstractObjectSearchCriteria',
	// 'as/dto/common/search/AbstractSearchCriteria',
	// 'as/dto/common/search/AbstractStringValue',
	// 'as/dto/common/search/AbstractTimeZoneValue',
	// 'as/dto/common/search/AbstractValue',
	// 'as/dto/common/search/SearchFieldType',
	// 'as/dto/common/search/SearchOperator',
	// 'as/dto/dataset/ArchivingStatus',
	// 'as/dto/dataset/Complete',
	// 'as/dto/dataset/DataSetKind',
	// 'as/dto/dataset/history/DataSetRelationType',
	// 'as/dto/dataset/search/DataSetSearchRelation',
	// 'as/dto/deletion/AbstractObjectDeletionOptions',
	// 'as/dto/experiment/history/ExperimentRelationType',
	// 'as/dto/global/search/GlobalSearchObjectKind',
	// 'as/dto/objectkindmodification/ObjectKind',
	// 'as/dto/objectkindmodification/OperationKind',
	// 'as/dto/project/history/ProjectRelationType',
	// 'as/dto/sample/history/SampleRelationType',
	// 'as/dto/sample/search/SampleSearchRelation',

	// 'as/dto/deletion/id/IDeletionId',
	// 'as/dto/entitytype/id/IEntityTypeId',
	// 'as/dto/experiment/id/IExperimentId',
	// 'as/dto/externaldms/id/IExternalDmsId',
	// 'as/dto/history/IRelationType',
	// 'as/dto/attachment/id/IAttachmentId',
	// 'as/dto/common/id/IObjectId',
	// 'as/dto/common/interfaces/IAttachmentsHolder',
	// 'as/dto/common/interfaces/ICodeHolder',
	// 'as/dto/common/interfaces/ICreationIdHolder',
	// 'as/dto/common/interfaces/IModificationDateHolder',
	// 'as/dto/common/interfaces/IModifierHolder',
	// 'as/dto/common/interfaces/IParentChildrenHolder',
	// 'as/dto/common/interfaces/IPermIdHolder',
	// 'as/dto/common/interfaces/IPropertiesHolder',
	// 'as/dto/common/interfaces/IRegistrationDateHolder',
	// 'as/dto/common/interfaces/IRegistratorHolder',
	// 'as/dto/common/interfaces/ISpaceHolder',
	// 'as/dto/common/interfaces/ITagsHolder',
	// 'as/dto/common/operations/IOperation',
	// 'as/dto/common/operations/IOperationResult',
	// 'as/dto/common/search/IDate',
	// 'as/dto/common/search/IDateFormat',
	// 'as/dto/common/search/IdSearchCriteria',
	// 'as/dto/common/search/ISearchCriteria',
	// 'as/dto/common/search/ITimeZone',
	// 'as/dto/dataset/id/IDataSetId',
	// 'as/dto/dataset/id/IFileFormatTypeId',
	// 'as/dto/dataset/id/ILocatorTypeId',
	// 'as/dto/dataset/id/IStorageFormatId',
	// 'as/dto/datastore/id/IDataStoreId',
	// 'as/dto/material/id/IMaterialId',
	// 'as/dto/person/id/IPersonId',
	// 'as/dto/project/id/IProjectId',
	// 'as/dto/sample/id/ISampleId',
	// 'as/dto/service/id/ICustomASServiceId',
	// 'as/dto/space/id/ISpaceId',
	// 'as/dto/tag/id/ITagId',
	// 'as/dto/common/fetchoptions/SortOptions',
	// 'as/dto/common/fetchoptions/FetchOptions',
	// 'as/dto/common/id/ObjectIdentifier',
	// 'as/dto/common/id/ObjectPermId',
	// 'as/dto/common/search/DateFieldSearchCriteria', 
	// 'as/dto/common/search/NumberFieldSearchCriteria',
	// 'as/dto/common/search/StringFieldSearchCriteria',
	// 'as/dto/common/search/TimeZone',
	// 'as/dto/vocabulary/id/IVocabularyId',
	// 'as/dto/vocabulary/id/IVocabularyTermId',


// these are the DTOs that can be "manually" created on the client
var sources = [
	'as/dto/attachment/Attachment',
	'as/dto/attachment/create/AttachmentCreation',
	'as/dto/attachment/fetchoptions/AttachmentFetchOptions',
	'as/dto/attachment/fetchoptions/AttachmentSortOptions',
	'as/dto/attachment/id/AttachmentFileName',

	'as/dto/attachment/update/AttachmentListUpdateValue',
	'as/dto/common/Enum',
	'as/dto/common/fetchoptions/EmptyFetchOptions',
	'as/dto/common/fetchoptions/EntitySortOptions',
	'as/dto/common/fetchoptions/EntityWithPropertiesSortOptions',
	'as/dto/common/fetchoptions/Sorting',

	'as/dto/common/fetchoptions/SortOrder',
	'as/dto/common/id/CreationId',
	'as/dto/common/id/ObjectTechId',

	'as/dto/common/search/AnyFieldSearchCriteria',
	'as/dto/common/search/AnyPropertySearchCriteria',
	'as/dto/common/search/AnyStringValue',
	'as/dto/common/search/CodeSearchCriteria',
	'as/dto/common/search/DateEarlierThanOrEqualToValue', 
	'as/dto/common/search/DateEqualToValue',
	'as/dto/common/search/DateLaterThanOrEqualToValue',
	'as/dto/common/search/DateObjectEarlierThanOrEqualToValue',
	'as/dto/common/search/DateObjectEqualToValue',
	'as/dto/common/search/DateObjectLaterThanOrEqualToValue',
	'as/dto/common/search/DatePropertySearchCriteria',
	'as/dto/common/search/LongDateFormat',
	'as/dto/common/search/ModificationDateSearchCriteria',
	'as/dto/common/search/NormalDateFormat',
	'as/dto/common/search/NumberEqualToValue',
	'as/dto/common/search/NumberGreaterThanOrEqualToValue',
	'as/dto/common/search/NumberGreaterThanValue',
	'as/dto/common/search/NumberLessThanOrEqualToValue',
	'as/dto/common/search/NumberLessThanValue',
	'as/dto/common/search/NumberPropertySearchCriteria',
	'as/dto/common/search/PermIdSearchCriteria',
	'as/dto/common/search/RegistrationDateSearchCriteria',
	'as/dto/common/search/SearchResult',
	'as/dto/common/search/ServerTimeZone',
	'as/dto/common/search/ShortDateFormat',
	'as/dto/common/search/StringContainsExactlyValue',
	'as/dto/common/search/StringContainsValue',
	'as/dto/common/search/StringEndsWithValue',
	'as/dto/common/search/StringEqualToValue',
	'as/dto/common/search/StringPropertySearchCriteria',
	'as/dto/common/search/StringStartsWithValue',
	'as/dto/common/search/TechIdSearchCriteria',
	'as/dto/common/update/FieldUpdateValue',
	'as/dto/common/update/IdListUpdateValue',
	'as/dto/common/update/ListUpdateAction',
	'as/dto/common/update/ListUpdateActionAdd',
	'as/dto/common/update/ListUpdateActionRemove',
	'as/dto/common/update/ListUpdateActionSet',
	'as/dto/common/update/ListUpdateValue',
	'as/dto/dataset/DataSet',
	'as/dto/dataset/DataSetType',
	'as/dto/dataset/delete/DataSetDeletionOptions',
	'as/dto/dataset/fetchoptions/DataSetFetchOptions',
	'as/dto/dataset/fetchoptions/DataSetSortOptions',
	'as/dto/dataset/fetchoptions/DataSetTypeFetchOptions',
	'as/dto/dataset/fetchoptions/DataSetTypeSortOptions',
	'as/dto/dataset/fetchoptions/FileFormatTypeFetchOptions',
	'as/dto/dataset/fetchoptions/FileFormatTypeSortOptions',
	'as/dto/dataset/fetchoptions/LinkedDataFetchOptions',
	'as/dto/dataset/fetchoptions/LinkedDataSortOptions',
	'as/dto/dataset/fetchoptions/LocatorTypeFetchOptions',
	'as/dto/dataset/fetchoptions/LocatorTypeSortOptions',
	'as/dto/dataset/fetchoptions/PhysicalDataFetchOptions',
	'as/dto/dataset/fetchoptions/PhysicalDataSortOptions',
	'as/dto/dataset/fetchoptions/StorageFormatFetchOptions',
	'as/dto/dataset/fetchoptions/StorageFormatSortOptions',
	'as/dto/dataset/FileFormatType',
	'as/dto/dataset/id/BdsDirectoryStorageFormatPermId',
	'as/dto/dataset/id/DataSetPermId',
	'as/dto/dataset/id/FileFormatTypePermId',
	'as/dto/dataset/id/LocatorTypePermId',
	'as/dto/dataset/id/ProprietaryStorageFormatPermId',
	'as/dto/dataset/id/RelativeLocationLocatorTypePermId',
	'as/dto/dataset/id/StorageFormatPermId',
	'as/dto/dataset/LinkedData',
	'as/dto/dataset/LocatorType',
	'as/dto/dataset/PhysicalData',
	'as/dto/dataset/search/DataSetSearchCriteria',
	'as/dto/dataset/StorageFormat',
	'as/dto/dataset/update/DataSetUpdate',
	'as/dto/dataset/update/LinkedDataUpdate',
	'as/dto/dataset/update/PhysicalDataUpdate',
	'as/dto/datastore/DataStore',
	'as/dto/datastore/fetchoptions/DataStoreFetchOptions',
	'as/dto/datastore/fetchoptions/DataStoreSortOptions',
	'as/dto/datastore/id/DataStorePermId',
	'as/dto/deletion/DeletedObject',
	'as/dto/deletion/Deletion',
	'as/dto/deletion/fetchoptions/DeletedObjectFetchOptions',
	'as/dto/deletion/fetchoptions/DeletionFetchOptions',
	'as/dto/deletion/fetchoptions/DeletionSortOptions',

	'as/dto/deletion/id/DeletionTechId',
	'as/dto/deletion/search/DeletionSearchCriteria',
	'as/dto/entitytype/id/EntityTypePermId',
	'as/dto/entitytype/search/EntityTypeSearchCriteria',
	'as/dto/experiment/create/CreateExperimentsOperation',
	// 'as/dto/experiment/create/CreateExperimentsOperationResult',
	'as/dto/experiment/create/ExperimentCreation',
	'as/dto/experiment/delete/ExperimentDeletionOptions',
	'as/dto/experiment/Experiment',
	'as/dto/experiment/ExperimentType',
	'as/dto/experiment/fetchoptions/ExperimentFetchOptions',
	'as/dto/experiment/fetchoptions/ExperimentSortOptions',
	'as/dto/experiment/fetchoptions/ExperimentTypeFetchOptions',
	'as/dto/experiment/fetchoptions/ExperimentTypeSortOptions',
	'as/dto/experiment/id/ExperimentIdentifier',
	'as/dto/experiment/id/ExperimentPermId',
	'as/dto/experiment/list/ListExperimentsOperation',
	// 'as/dto/experiment/list/ListExperimentsOperationResult',
	'as/dto/experiment/search/ExperimentSearchCriteria',
	'as/dto/experiment/search/NoExperimentSearchCriteria',
	'as/dto/experiment/search/SearchExperimentsOperation',
	// 'as/dto/experiment/search/SearchExperimentsOperationResult',
	'as/dto/experiment/update/ExperimentUpdate',
	'as/dto/experiment/update/UpdateExperimentsOperation',
	// 'as/dto/experiment/update/UpdateExperimentsOperationResult',
	'as/dto/externaldms/ExternalDms',
	'as/dto/externaldms/fetchoptions/ExternalDmsFetchOptions',
	'as/dto/externaldms/fetchoptions/ExternalDmsSortOptions',
	'as/dto/externaldms/id/ExternalDmsPermId',
	'as/dto/global/fetchoptions/GlobalSearchObjectFetchOptions',
	'as/dto/global/fetchoptions/GlobalSearchObjectSortOptions',
	'as/dto/global/GlobalSearchObject',
	'as/dto/global/search/GlobalSearchCriteria',
	'as/dto/global/search/GlobalSearchObjectKindCriteria',
	'as/dto/global/search/GlobalSearchTextCriteria',
	'as/dto/global/search/GlobalSearchWildCardsCriteria',
	'as/dto/history/fetchoptions/HistoryEntryFetchOptions',
	'as/dto/history/fetchoptions/HistoryEntrySortOptions',
	'as/dto/history/HistoryEntry',
	'as/dto/history/PropertyHistoryEntry',
	'as/dto/history/RelationHistoryEntry',
	'as/dto/material/create/MaterialCreation',
	'as/dto/material/delete/MaterialDeletionOptions',
	'as/dto/material/fetchoptions/MaterialFetchOptions',
	'as/dto/material/fetchoptions/MaterialSortOptions',

	'as/dto/material/fetchoptions/MaterialTypeFetchOptions',
	'as/dto/material/fetchoptions/MaterialTypeSortOptions',
	'as/dto/material/id/MaterialPermId',
	'as/dto/material/Material',
	'as/dto/material/MaterialType',
	'as/dto/material/search/MaterialSearchCriteria',
	'as/dto/material/update/MaterialUpdate',
	'as/dto/objectkindmodification/fetchoptions/ObjectKindModificationFetchOptions',
	'as/dto/objectkindmodification/fetchoptions/ObjectKindModificationSortOptions',
	'as/dto/objectkindmodification/ObjectKindModification',
	'as/dto/objectkindmodification/search/ObjectKindCriteria',
	'as/dto/objectkindmodification/search/ObjectKindModificationSearchCriteria',
	'as/dto/objectkindmodification/search/OperationKindCriteria',
	'as/dto/person/fetchoptions/PersonFetchOptions',
	'as/dto/person/fetchoptions/PersonSortOptions',
	'as/dto/person/id/PersonPermId',
	'as/dto/person/Person',
	'as/dto/project/create/ProjectCreation',
	'as/dto/project/delete/ProjectDeletionOptions',
	'as/dto/project/fetchoptions/ProjectFetchOptions',
	'as/dto/project/fetchoptions/ProjectSortOptions',
	'as/dto/project/id/ProjectIdentifier',
	'as/dto/project/id/ProjectPermId',
	'as/dto/project/Project',
	'as/dto/project/search/ProjectSearchCriteria',
	'as/dto/project/update/ProjectUpdate',
	'as/dto/property/fetchoptions/PropertyAssignmentFetchOptions',
	'as/dto/property/fetchoptions/PropertyAssignmentSortOptions',
	'as/dto/property/fetchoptions/PropertyFetchOptions',
//	'as/dto/property/DataTypeCode',
	'as/dto/property/PropertyAssignment',
	'as/dto/property/PropertyType',
	'as/dto/sample/create/CreateSamplesOperation',
	// 'as/dto/sample/create/CreateSamplesOperationResult',
	'as/dto/sample/create/SampleCreation',
	'as/dto/sample/delete/SampleDeletionOptions',
	'as/dto/sample/fetchoptions/SampleFetchOptions',
	'as/dto/sample/fetchoptions/SampleSortOptions',
	'as/dto/sample/fetchoptions/SampleTypeFetchOptions',
	'as/dto/sample/fetchoptions/SampleTypeSortOptions',
	'as/dto/sample/id/SampleIdentifier',
	'as/dto/sample/id/SamplePermId',
	'as/dto/sample/Sample',
	'as/dto/sample/SampleType',
	'as/dto/sample/search/NoSampleContainerSearchCriteria',
	'as/dto/sample/search/NoSampleSearchCriteria',
	'as/dto/sample/search/SampleSearchCriteria',
	'as/dto/sample/update/SampleUpdate',
	'as/dto/sample/update/UpdateSamplesOperation',
	'as/dto/service/CustomASService',
	'as/dto/service/CustomASServiceExecutionOptions',
	'as/dto/service/fetchoptions/CustomASServiceFetchOptions',
	'as/dto/service/fetchoptions/CustomASServiceSortOptions',
	'as/dto/service/id/CustomASServiceCode',
	'as/dto/service/search/CustomASServiceSearchCriteria',
	'as/dto/space/create/SpaceCreation',
	'as/dto/space/delete/SpaceDeletionOptions',
	'as/dto/space/fetchoptions/SpaceFetchOptions',
	'as/dto/space/fetchoptions/SpaceSortOptions',
	'as/dto/space/id/SpacePermId',
	'as/dto/space/search/SpaceSearchCriteria',
	'as/dto/space/Space',
	'as/dto/space/update/SpaceUpdate',
	'as/dto/tag/fetchoptions/TagFetchOptions',
	'as/dto/tag/fetchoptions/TagSortOptions',
	'as/dto/tag/id/TagCode',
	'as/dto/tag/id/TagPermId',
	'as/dto/tag/search/TagSearchCriteria',
	'as/dto/tag/Tag',
	'as/dto/tag/update/TagUpdate',
	'as/dto/tag/create/TagCreation',
	'as/dto/vocabulary/fetchoptions/VocabularyFetchOptions',
	'as/dto/vocabulary/fetchoptions/VocabularySortOptions',
	'as/dto/vocabulary/fetchoptions/VocabularyTermFetchOptions',
	'as/dto/vocabulary/fetchoptions/VocabularyTermSortOptions',
	'as/dto/vocabulary/id/VocabularyPermId',
	'as/dto/vocabulary/id/VocabularyTermPermId',
	'as/dto/vocabulary/Vocabulary',
	'as/dto/vocabulary/VocabularyTerm',	
	'as/dto/vocabulary/search/VocabularySearchCriteria',
	'as/dto/vocabulary/search/VocabularyTermSearchCriteria',
	'as/dto/vocabulary/create/VocabularyTermCreation',
	'as/dto/vocabulary/delete/VocabularyTermDeletionOptions',
	'as/dto/vocabulary/delete/VocabularyTermReplacement',
	'as/dto/vocabulary/update/VocabularyTermUpdate'
 ];

define(sources, 
		function() 
{
	var allDTOConstructors = Array.prototype.slice.call(arguments);

	var Dtos = function() {
		for (var x=0; x<allDTOConstructors.length; x++) {
			var arg = allDTOConstructors[x];
			if (arg.prototype) {
				var fullType = arg.prototype['@type'];
				if (fullType) {
					var type = fullType.split('.').slice(-1)[0];
					this[type] = arg;
				}
			} else {
				console.log(sources[x] + ' has no prototype');
			}
		}

	};
	return new Dtos();
})
