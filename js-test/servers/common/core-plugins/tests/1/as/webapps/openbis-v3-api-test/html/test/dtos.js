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
	// 'as/dto/deletion/id/DeletionTechId',
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
	// 'as/dto/common/id/ObjectTechId',
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
	// 'as/dto/roleassignment/id/RoleAssignmentTechId',


// these are the DTOs that can be "manually" created on the client
var sources = [
	'as/dto/attachment/Attachment',
	'as/dto/attachment/create/AttachmentCreation',
	'as/dto/attachment/fetchoptions/AttachmentFetchOptions',
	'as/dto/attachment/fetchoptions/AttachmentSortOptions',
	'as/dto/attachment/id/AttachmentFileName',
	'as/dto/attachment/update/AttachmentListUpdateValue',
	
	'as/dto/authorizationgroup/create/AuthorizationGroupCreation',
	'as/dto/authorizationgroup/create/CreateAuthorizationGroupsOperation',
	'as/dto/authorizationgroup/create/CreateAuthorizationGroupsOperationResult',
	'as/dto/authorizationgroup/delete/AuthorizationGroupDeletionOptions',
	'as/dto/authorizationgroup/delete/DeleteAuthorizationGroupsOperation',
	'as/dto/authorizationgroup/delete/DeleteAuthorizationGroupsOperationResult',
	'as/dto/authorizationgroup/fetchoptions/AuthorizationGroupFetchOptions',
	'as/dto/authorizationgroup/fetchoptions/AuthorizationGroupSortOptions',
	'as/dto/authorizationgroup/get/GetAuthorizationGroupsOperation',
	'as/dto/authorizationgroup/get/GetAuthorizationGroupsOperationResult',
	'as/dto/authorizationgroup/id/AuthorizationGroupPermId',
	'as/dto/authorizationgroup/search/AuthorizationGroupSearchCriteria',
	'as/dto/authorizationgroup/search/SearchAuthorizationGroupsOperation',
	'as/dto/authorizationgroup/search/SearchAuthorizationGroupsOperationResult',
	'as/dto/authorizationgroup/update/AuthorizationGroupUpdate',
	'as/dto/authorizationgroup/update/UpdateAuthorizationGroupsOperation',
	'as/dto/authorizationgroup/update/UpdateAuthorizationGroupsOperationResult',
	'as/dto/authorizationgroup/AuthorizationGroup',
	
	'as/dto/common/Enum',
	'as/dto/common/fetchoptions/EmptyFetchOptions',
	'as/dto/common/fetchoptions/EntitySortOptions',
	'as/dto/common/fetchoptions/EntityWithPropertiesSortOptions',
	'as/dto/common/fetchoptions/Sorting',
	'as/dto/common/fetchoptions/SortOrder',
	'as/dto/common/id/CreationId',
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
	'as/dto/common/search/CodesSearchCriteria',
	'as/dto/common/search/IdsSearchCriteria',
	'as/dto/common/update/FieldUpdateValue',
	'as/dto/common/update/IdListUpdateValue',
	'as/dto/common/update/ListUpdateAction',
	'as/dto/common/update/ListUpdateActionAdd',
	'as/dto/common/update/ListUpdateActionRemove',
	'as/dto/common/update/ListUpdateActionSet',
	'as/dto/common/update/ListUpdateValue',
	
	'as/dto/dataset/DataSet',
	'as/dto/dataset/DataSetType',
	'as/dto/dataset/archive/DataSetArchiveOptions',
	'as/dto/dataset/archive/ArchiveDataSetsOperation',
	'as/dto/dataset/archive/ArchiveDataSetsOperationResult',
	'as/dto/dataset/unarchive/DataSetUnarchiveOptions',
	'as/dto/dataset/unarchive/UnarchiveDataSetsOperation',
	'as/dto/dataset/unarchive/UnarchiveDataSetsOperationResult',
	'as/dto/dataset/create/DataSetCreation',
	'as/dto/dataset/create/LinkedDataCreation',
	'as/dto/dataset/create/DataSetTypeCreation',
	'as/dto/dataset/create/ContentCopyCreation',
	'as/dto/dataset/create/CreateDataSetsOperation',
	'as/dto/dataset/create/CreateDataSetsOperationResult',
	'as/dto/dataset/create/CreateDataSetTypesOperation',
	'as/dto/dataset/create/CreateDataSetTypesOperationResult',
	'as/dto/dataset/delete/DataSetDeletionOptions',
	'as/dto/dataset/delete/DeleteDataSetsOperation',
	'as/dto/dataset/delete/DeleteDataSetsOperationResult',
	'as/dto/dataset/get/GetDataSetsOperation',
	'as/dto/dataset/get/GetDataSetsOperationResult',
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
	'as/dto/dataset/id/ContentCopyPermId',
	'as/dto/dataset/id/DataSetPermId',
	'as/dto/dataset/id/FileFormatTypePermId',
	'as/dto/dataset/id/IContentCopyId',
	'as/dto/dataset/id/LocatorTypePermId',
	'as/dto/dataset/id/ProprietaryStorageFormatPermId',
	'as/dto/dataset/id/RelativeLocationLocatorTypePermId',
	'as/dto/dataset/id/StorageFormatPermId',
	'as/dto/dataset/ContentCopy',
	'as/dto/dataset/LinkedData',
	'as/dto/dataset/LocatorType',
	'as/dto/dataset/PhysicalData',
	'as/dto/dataset/search/DataSetSearchCriteria',
	'as/dto/dataset/search/DataSetTypeSearchCriteria',
	'as/dto/dataset/search/CompleteSearchCriteria',
	'as/dto/dataset/search/ExternalCodeSearchCriteria',
	'as/dto/dataset/search/ExternalDmsSearchCriteria',
	'as/dto/dataset/search/FileFormatTypeSearchCriteria',
	'as/dto/dataset/search/LinkedDataSearchCriteria',
	'as/dto/dataset/search/LocationSearchCriteria',
	'as/dto/dataset/search/LocatorTypeSearchCriteria',
	'as/dto/dataset/search/PhysicalDataSearchCriteria',
	'as/dto/dataset/search/PresentInArchiveSearchCriteria',
	'as/dto/dataset/search/ShareIdSearchCriteria',
	'as/dto/dataset/search/SizeSearchCriteria',
	'as/dto/dataset/search/SpeedHintSearchCriteria',
	'as/dto/dataset/search/StatusSearchCriteria',
	'as/dto/dataset/search/StorageConfirmationSearchCriteria',
	'as/dto/dataset/search/StorageFormatSearchCriteria',
	'as/dto/dataset/search/SearchDataSetsOperation',
	'as/dto/dataset/search/SearchDataSetsOperationResult',
	'as/dto/dataset/search/SearchDataSetTypesOperation',
	'as/dto/dataset/search/SearchDataSetTypesOperationResult',
	'as/dto/dataset/StorageFormat',
	'as/dto/dataset/update/DataSetUpdate',
	'as/dto/dataset/update/ContentCopyListUpdateValue',
	'as/dto/dataset/update/LinkedDataUpdate',
	'as/dto/dataset/update/PhysicalDataUpdate',
	'as/dto/dataset/update/UpdateDataSetsOperation',
	'as/dto/dataset/update/UpdateDataSetsOperationResult',
	
	'as/dto/datastore/DataStore',
	'as/dto/datastore/fetchoptions/DataStoreFetchOptions',
	'as/dto/datastore/fetchoptions/DataStoreSortOptions',
	'as/dto/datastore/id/DataStorePermId',
	'as/dto/datastore/search/DataStoreSearchCriteria',
	'as/dto/datastore/search/SearchDataStoresOperation',
	'as/dto/datastore/search/SearchDataStoresOperationResult',
	
	'as/dto/deletion/DeletedObject',
	'as/dto/deletion/Deletion',
	'as/dto/deletion/confirm/ConfirmDeletionsOperation',
	'as/dto/deletion/confirm/ConfirmDeletionsOperationResult',
	'as/dto/deletion/fetchoptions/DeletedObjectFetchOptions',
	'as/dto/deletion/fetchoptions/DeletionFetchOptions',
	'as/dto/deletion/fetchoptions/DeletionSortOptions',
	'as/dto/deletion/revert/RevertDeletionsOperation',
	'as/dto/deletion/revert/RevertDeletionsOperationResult',
	'as/dto/deletion/search/DeletionSearchCriteria',
	'as/dto/deletion/search/SearchDeletionsOperation',
	'as/dto/deletion/search/SearchDeletionsOperationResult',
	
	'as/dto/entitytype/id/EntityTypePermId',
	'as/dto/entitytype/fetchoptions/EntityTypeFetchOptions',
	'as/dto/entitytype/fetchoptions/EntityTypeSortOptions',
	'as/dto/entitytype/search/EntityKindSearchCriteria',
	'as/dto/entitytype/search/EntityTypeSearchCriteria',
	'as/dto/entitytype/EntityKind',
	
	'as/dto/experiment/create/ExperimentCreation',
	'as/dto/experiment/create/CreateExperimentsOperation',
	'as/dto/experiment/create/CreateExperimentsOperationResult',
	'as/dto/experiment/create/ExperimentTypeCreation',
	'as/dto/experiment/create/CreateExperimentTypesOperation',
	'as/dto/experiment/create/CreateExperimentTypesOperationResult',
	'as/dto/experiment/delete/ExperimentDeletionOptions',
	'as/dto/experiment/delete/DeleteExperimentsOperation',
	'as/dto/experiment/delete/DeleteExperimentsOperationResult',
	'as/dto/experiment/Experiment',
	'as/dto/experiment/ExperimentType',
	'as/dto/experiment/fetchoptions/ExperimentFetchOptions',
	'as/dto/experiment/fetchoptions/ExperimentSortOptions',
	'as/dto/experiment/fetchoptions/ExperimentTypeFetchOptions',
	'as/dto/experiment/fetchoptions/ExperimentTypeSortOptions',
	'as/dto/experiment/get/GetExperimentsOperation',
	'as/dto/experiment/get/GetExperimentsOperationResult',
	'as/dto/experiment/id/ExperimentIdentifier',
	'as/dto/experiment/id/ExperimentPermId',
	'as/dto/experiment/search/ExperimentSearchCriteria',
	'as/dto/experiment/search/ExperimentTypeSearchCriteria',
	'as/dto/experiment/search/NoExperimentSearchCriteria',
	'as/dto/experiment/search/SearchExperimentsOperation',
	'as/dto/experiment/search/SearchExperimentsOperationResult',
	'as/dto/experiment/search/SearchExperimentTypesOperation',
	'as/dto/experiment/search/SearchExperimentTypesOperationResult',
	'as/dto/experiment/update/ExperimentUpdate',
	'as/dto/experiment/update/UpdateExperimentsOperation',
	'as/dto/experiment/update/UpdateExperimentsOperationResult',
	
	'as/dto/externaldms/ExternalDms',
	'as/dto/externaldms/delete/ExternalDmsDeletionOptions',
	'as/dto/externaldms/delete/DeleteExternalDmsOperation',
	'as/dto/externaldms/delete/DeleteExternalDmsOperationResult',
	'as/dto/externaldms/fetchoptions/ExternalDmsFetchOptions',
	'as/dto/externaldms/fetchoptions/ExternalDmsSortOptions',
	'as/dto/externaldms/id/ExternalDmsPermId',
	'as/dto/externaldms/create/CreateExternalDmsOperation',
	'as/dto/externaldms/create/CreateExternalDmsOperationResult',
	'as/dto/externaldms/create/ExternalDmsCreation',
	'as/dto/externaldms/ExternalDmsAddressType',
	'as/dto/externaldms/get/GetExternalDmsOperation',
	'as/dto/externaldms/get/GetExternalDmsOperationResult',
	'as/dto/externaldms/search/ExternalDmsSearchCriteria',
	'as/dto/externaldms/search/SearchExternalDmsOperation',
	'as/dto/externaldms/search/SearchExternalDmsOperationResult',
	'as/dto/externaldms/update/ExternalDmsUpdate',
	'as/dto/externaldms/update/UpdateExternalDmsOperation',
	'as/dto/externaldms/update/UpdateExternalDmsOperationResult',
	
	'as/dto/global/fetchoptions/GlobalSearchObjectFetchOptions',
	'as/dto/global/fetchoptions/GlobalSearchObjectSortOptions',
	'as/dto/global/GlobalSearchObject',
	'as/dto/global/search/GlobalSearchCriteria',
	'as/dto/global/search/GlobalSearchObjectKindCriteria',
	'as/dto/global/search/GlobalSearchTextCriteria',
	'as/dto/global/search/GlobalSearchWildCardsCriteria',
	'as/dto/global/search/SearchGloballyOperation',
	'as/dto/global/search/SearchGloballyOperationResult',
	
	'as/dto/history/fetchoptions/HistoryEntryFetchOptions',
	'as/dto/history/fetchoptions/HistoryEntrySortOptions',
	'as/dto/history/HistoryEntry',
	'as/dto/history/PropertyHistoryEntry',
	'as/dto/history/RelationHistoryEntry',
	
	'as/dto/material/create/MaterialCreation',
	'as/dto/material/create/CreateMaterialsOperation',
	'as/dto/material/create/CreateMaterialsOperationResult',
	'as/dto/material/create/MaterialTypeCreation',
	'as/dto/material/create/CreateMaterialTypesOperation',
	'as/dto/material/create/CreateMaterialTypesOperationResult',
	'as/dto/material/delete/MaterialDeletionOptions',
	'as/dto/material/delete/DeleteMaterialsOperation',
	'as/dto/material/delete/DeleteMaterialsOperationResult',
	'as/dto/material/fetchoptions/MaterialFetchOptions',
	'as/dto/material/fetchoptions/MaterialSortOptions',
	'as/dto/material/fetchoptions/MaterialTypeFetchOptions',
	'as/dto/material/fetchoptions/MaterialTypeSortOptions',
	'as/dto/material/get/GetMaterialsOperation',
	'as/dto/material/get/GetMaterialsOperationResult',
	'as/dto/material/id/MaterialPermId',
	'as/dto/material/Material',
	'as/dto/material/MaterialType',
	'as/dto/material/search/MaterialSearchCriteria',
	'as/dto/material/search/MaterialTypeSearchCriteria',
	'as/dto/material/search/SearchMaterialsOperation',
	'as/dto/material/search/SearchMaterialsOperationResult',
	'as/dto/material/search/SearchMaterialTypesOperation',
	'as/dto/material/search/SearchMaterialTypesOperationResult',
	'as/dto/material/update/MaterialUpdate',
	'as/dto/material/update/UpdateMaterialsOperation',
	'as/dto/material/update/UpdateMaterialsOperationResult',
	
	'as/dto/objectkindmodification/fetchoptions/ObjectKindModificationFetchOptions',
	'as/dto/objectkindmodification/fetchoptions/ObjectKindModificationSortOptions',
	'as/dto/objectkindmodification/ObjectKindModification',
	'as/dto/objectkindmodification/search/ObjectKindCriteria',
	'as/dto/objectkindmodification/search/ObjectKindModificationSearchCriteria',
	'as/dto/objectkindmodification/search/OperationKindCriteria',
	'as/dto/objectkindmodification/search/SearchObjectKindModificationsOperation',
	'as/dto/objectkindmodification/search/SearchObjectKindModificationsOperationResult',
	
	'as/dto/person/fetchoptions/PersonFetchOptions',
	'as/dto/person/fetchoptions/PersonSortOptions',
	'as/dto/person/id/PersonPermId',
	'as/dto/person/search/EmailSearchCriteria',
	'as/dto/person/search/FirstNameSearchCriteria',
	'as/dto/person/search/LastNameSearchCriteria',
	'as/dto/person/search/ModifierSearchCriteria',
	'as/dto/person/search/RegistratorSearchCriteria',
	'as/dto/person/search/UserIdSearchCriteria',
	'as/dto/person/search/UserIdsSearchCriteria',
	'as/dto/person/Person',
	
	'as/dto/project/create/ProjectCreation',
	'as/dto/project/create/CreateProjectsOperation',
	'as/dto/project/create/CreateProjectsOperationResult',
	'as/dto/project/delete/ProjectDeletionOptions',
	'as/dto/project/delete/DeleteProjectsOperation',
	'as/dto/project/delete/DeleteProjectsOperationResult',
	'as/dto/project/fetchoptions/ProjectFetchOptions',
	'as/dto/project/fetchoptions/ProjectSortOptions',
	'as/dto/project/get/GetProjectsOperation',
	'as/dto/project/get/GetProjectsOperationResult',
	'as/dto/project/id/ProjectIdentifier',
	'as/dto/project/id/ProjectPermId',
	'as/dto/project/Project',
	'as/dto/project/search/NoProjectSearchCriteria',
	'as/dto/project/search/ProjectSearchCriteria',
	'as/dto/project/search/SearchProjectsOperation',
	'as/dto/project/search/SearchProjectsOperationResult',
	'as/dto/project/update/ProjectUpdate',
	'as/dto/project/update/UpdateProjectsOperation',
	'as/dto/project/update/UpdateProjectsOperationResult',
	
	'as/dto/property/create/PropertyAssignmentCreation',
	'as/dto/property/id/PropertyTypePermId',
	'as/dto/property/id/PropertyAssignmentPermId',
	'as/dto/property/fetchoptions/PropertyAssignmentFetchOptions',
	'as/dto/property/fetchoptions/PropertyAssignmentSortOptions',
	'as/dto/property/fetchoptions/PropertyFetchOptions',
	'as/dto/property/fetchoptions/PropertyTypeFetchOptions',
	'as/dto/property/fetchoptions/PropertyTypeSortOptions',
	'as/dto/property/search/PropertyTypeSearchCriteria',
	'as/dto/property/search/PropertyAssignmentSearchCriteria',
	'as/dto/property/search/SearchPropertyTypesOperation',
	'as/dto/property/search/SearchPropertyTypesOperationResult',
	'as/dto/property/search/SearchPropertyAssignmentsOperation',
	'as/dto/property/search/SearchPropertyAssignmentsOperationResult',
	'as/dto/property/DataType',
	'as/dto/property/PropertyAssignment',
	'as/dto/property/PropertyType',
	
	'as/dto/roleassignment/create/RoleAssignmentCreation',
	'as/dto/roleassignment/create/CreateRoleAssignmentsOperation',
	'as/dto/roleassignment/create/CreateRoleAssignmentsOperationResult',
	'as/dto/roleassignment/fetchoptions/RoleAssignmentFetchOptions',
	'as/dto/roleassignment/fetchoptions/RoleAssignmentSortOptions',
	'as/dto/roleassignment/get/GetRoleAssignmentsOperation',
	'as/dto/roleassignment/get/GetRoleAssignmentsOperationResult',
	'as/dto/roleassignment/id/IRoleAssignmentId',
	'as/dto/roleassignment/Role',
	'as/dto/roleassignment/RoleLevel',
	'as/dto/roleassignment/RoleAssignment',
	
	'as/dto/plugin/id/PluginPermId',
	
	'as/dto/sample/create/SampleCreation',
	'as/dto/sample/create/CreateSamplesOperation',
	'as/dto/sample/create/CreateSamplesOperationResult',
	'as/dto/sample/create/SampleTypeCreation',
	'as/dto/sample/create/CreateSampleTypesOperation',
	'as/dto/sample/create/CreateSampleTypesOperationResult',
	'as/dto/sample/delete/SampleDeletionOptions',
	'as/dto/sample/delete/DeleteSamplesOperation',
	'as/dto/sample/delete/DeleteSamplesOperationResult',
	'as/dto/sample/fetchoptions/SampleFetchOptions',
	'as/dto/sample/fetchoptions/SampleSortOptions',
	'as/dto/sample/fetchoptions/SampleTypeFetchOptions',
	'as/dto/sample/fetchoptions/SampleTypeSortOptions',
	'as/dto/sample/get/GetSamplesOperation',
	'as/dto/sample/get/GetSamplesOperationResult',
	'as/dto/sample/id/SampleIdentifier',
	'as/dto/sample/id/SamplePermId',
	'as/dto/sample/Sample',
	'as/dto/sample/SampleType',
	'as/dto/sample/search/SampleTypeSearchCriteria',
	'as/dto/sample/search/NoSampleContainerSearchCriteria',
	'as/dto/sample/search/NoSampleSearchCriteria',
	'as/dto/sample/search/SampleSearchCriteria',
	'as/dto/sample/search/SampleTypeSearchCriteria',
	'as/dto/sample/search/ListableSampleTypeSearchCriteria',
	'as/dto/sample/search/SearchSamplesOperation',
	'as/dto/sample/search/SearchSamplesOperationResult',
	'as/dto/sample/search/SearchSampleTypesOperation',
	'as/dto/sample/search/SearchSampleTypesOperationResult',
	'as/dto/sample/update/SampleUpdate',
	'as/dto/sample/update/UpdateSamplesOperation',
	'as/dto/sample/update/UpdateSamplesOperationResult',
	
	'as/dto/service/CustomASService',
	'as/dto/service/CustomASServiceExecutionOptions',
	'as/dto/service/execute/ExecuteCustomASServiceOperation',
	'as/dto/service/execute/ExecuteCustomASServiceOperationResult',
	'as/dto/service/fetchoptions/CustomASServiceFetchOptions',
	'as/dto/service/fetchoptions/CustomASServiceSortOptions',
	'as/dto/service/id/CustomASServiceCode',
	'as/dto/service/search/CustomASServiceSearchCriteria',
	'as/dto/service/search/SearchCustomASServicesOperation',
	'as/dto/service/search/SearchCustomASServicesOperationResult',
	
	'as/dto/session/SessionInformation',
	'as/dto/session/get/GetSessionInformationOperation',
	'as/dto/session/get/GetSessionInformationOperationResult',
	
	'as/dto/space/create/SpaceCreation',
	'as/dto/space/create/CreateSpacesOperation',
	'as/dto/space/create/CreateSpacesOperationResult',
	'as/dto/space/delete/SpaceDeletionOptions',
	'as/dto/space/delete/DeleteSpacesOperation',
	'as/dto/space/delete/DeleteSpacesOperationResult',
	'as/dto/space/fetchoptions/SpaceFetchOptions',
	'as/dto/space/fetchoptions/SpaceSortOptions',
	'as/dto/space/get/GetSpacesOperation',
	'as/dto/space/get/GetSpacesOperationResult',
	'as/dto/space/id/SpacePermId',
	'as/dto/space/search/NoSpaceSearchCriteria',
	'as/dto/space/search/SpaceSearchCriteria',
	'as/dto/space/search/SearchSpacesOperation',
	'as/dto/space/search/SearchSpacesOperationResult',
	'as/dto/space/Space',
	'as/dto/space/update/SpaceUpdate',
	'as/dto/space/update/UpdateSpacesOperation',
	'as/dto/space/update/UpdateSpacesOperationResult',
	
	'as/dto/semanticannotation/create/SemanticAnnotationCreation',
	'as/dto/semanticannotation/create/CreateSemanticAnnotationsOperation',
	'as/dto/semanticannotation/create/CreateSemanticAnnotationsOperationResult',
	'as/dto/semanticannotation/delete/SemanticAnnotationDeletionOptions',
	'as/dto/semanticannotation/delete/DeleteSemanticAnnotationsOperation',
	'as/dto/semanticannotation/delete/DeleteSemanticAnnotationsOperationResult',
	'as/dto/semanticannotation/fetchoptions/SemanticAnnotationFetchOptions',
	'as/dto/semanticannotation/fetchoptions/SemanticAnnotationSortOptions',
	'as/dto/semanticannotation/get/GetSemanticAnnotationsOperation',
	'as/dto/semanticannotation/get/GetSemanticAnnotationsOperationResult',
	'as/dto/semanticannotation/id/SemanticAnnotationPermId',
	'as/dto/semanticannotation/search/SemanticAnnotationSearchCriteria',
	'as/dto/semanticannotation/search/SearchSemanticAnnotationsOperation',
	'as/dto/semanticannotation/search/SearchSemanticAnnotationsOperationResult',
	'as/dto/semanticannotation/search/DescriptorAccessionIdSearchCriteria',
	'as/dto/semanticannotation/search/DescriptorOntologyIdSearchCriteria',
	'as/dto/semanticannotation/search/DescriptorOntologyVersionSearchCriteria',
	'as/dto/semanticannotation/search/PredicateAccessionIdSearchCriteria',
	'as/dto/semanticannotation/search/PredicateOntologyIdSearchCriteria',
	'as/dto/semanticannotation/search/PredicateOntologyVersionSearchCriteria',
	'as/dto/semanticannotation/update/SemanticAnnotationUpdate',
	'as/dto/semanticannotation/update/UpdateSemanticAnnotationsOperation',
	'as/dto/semanticannotation/update/UpdateSemanticAnnotationsOperationResult',
	'as/dto/semanticannotation/SemanticAnnotation',
	
	'as/dto/tag/fetchoptions/TagFetchOptions',
	'as/dto/tag/fetchoptions/TagSortOptions',
	'as/dto/tag/id/TagCode',
	'as/dto/tag/id/TagPermId',
	'as/dto/tag/search/TagSearchCriteria',
	'as/dto/tag/search/SearchTagsOperation',
	'as/dto/tag/search/SearchTagsOperationResult',
	'as/dto/tag/Tag',
	'as/dto/tag/update/TagUpdate',
	'as/dto/tag/update/UpdateTagsOperation',
	'as/dto/tag/update/UpdateTagsOperationResult',
	'as/dto/tag/create/TagCreation',
	'as/dto/tag/create/CreateTagsOperation',
	'as/dto/tag/create/CreateTagsOperationResult',
   	'as/dto/tag/delete/TagDeletionOptions',
   	'as/dto/tag/delete/DeleteTagsOperation',
   	'as/dto/tag/delete/DeleteTagsOperationResult',
   	'as/dto/tag/get/GetTagsOperation',
   	'as/dto/tag/get/GetTagsOperationResult',
   	
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
	'as/dto/vocabulary/search/SearchVocabularyTermsOperation',
	'as/dto/vocabulary/search/SearchVocabularyTermsOperationResult',
	'as/dto/vocabulary/create/VocabularyTermCreation',
	'as/dto/vocabulary/create/CreateVocabularyTermsOperation',
	'as/dto/vocabulary/create/CreateVocabularyTermsOperationResult',
	'as/dto/vocabulary/delete/VocabularyTermDeletionOptions',
	'as/dto/vocabulary/delete/VocabularyTermReplacement',
	'as/dto/vocabulary/delete/DeleteVocabularyTermsOperation',
	'as/dto/vocabulary/delete/DeleteVocabularyTermsOperationResult',
	'as/dto/vocabulary/get/GetVocabularyTermsOperation',
	'as/dto/vocabulary/get/GetVocabularyTermsOperationResult',
	'as/dto/vocabulary/update/VocabularyTermUpdate',
	'as/dto/vocabulary/update/UpdateVocabularyTermsOperation',
	'as/dto/vocabulary/update/UpdateVocabularyTermsOperationResult',
	
	'as/dto/webapp/WebAppSettings',
	
	'as/dto/operation/delete/OperationExecutionDeletionOptions',
	'as/dto/operation/delete/DeleteOperationExecutionsOperation',
	'as/dto/operation/delete/DeleteOperationExecutionsOperationResult',
	'as/dto/operation/fetchoptions/OperationExecutionDetailsFetchOptions',
	'as/dto/operation/fetchoptions/OperationExecutionDetailsSortOptions',
	'as/dto/operation/fetchoptions/OperationExecutionFetchOptions',
	'as/dto/operation/fetchoptions/OperationExecutionNotificationFetchOptions',
	'as/dto/operation/fetchoptions/OperationExecutionNotificationSortOptions',
	'as/dto/operation/fetchoptions/OperationExecutionSortOptions',
	'as/dto/operation/fetchoptions/OperationExecutionSummaryFetchOptions',
	'as/dto/operation/fetchoptions/OperationExecutionSummarySortOptions',
	'as/dto/operation/get/GetOperationExecutionsOperation',
	'as/dto/operation/get/GetOperationExecutionsOperationResult',
	'as/dto/operation/id/OperationExecutionPermId',
	'as/dto/operation/search/OperationExecutionSearchCriteria',
	'as/dto/operation/search/SearchOperationExecutionsOperation',
	'as/dto/operation/search/SearchOperationExecutionsOperationResult',
	'as/dto/operation/update/OperationExecutionUpdate',
	'as/dto/operation/update/UpdateOperationExecutionsOperation',
	'as/dto/operation/update/UpdateOperationExecutionsOperationResult',
	'as/dto/operation/AsynchronousOperationExecutionOptions',
	'as/dto/operation/AsynchronousOperationExecutionResults',
	'as/dto/operation/OperationExecution',
	'as/dto/operation/OperationExecutionDetails',
	'as/dto/operation/OperationExecutionEmailNotification',
	'as/dto/operation/OperationExecutionError',
	'as/dto/operation/OperationExecutionProgress',
	'as/dto/operation/OperationExecutionSummary',
	'as/dto/operation/SynchronousOperationExecutionOptions',
	'as/dto/operation/SynchronousOperationExecutionResults',
	
	'dss/dto/dataset/create/FullDataSetCreation',
	'dss/dto/datasetfile/DataSetFile',
	'dss/dto/datasetfile/create/DataSetFileCreation',
	'dss/dto/datasetfile/fetchoptions/DataSetFileSortOptions',
	'dss/dto/datasetfile/fetchoptions/DataSetFileFetchOptions',
	'dss/dto/datasetfile/id/DataSetFilePermId',
	'dss/dto/datasetfile/search/DataSetFileSearchCriteria'

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
