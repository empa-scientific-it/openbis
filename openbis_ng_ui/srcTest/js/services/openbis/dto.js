import AuthorizationGroup from 'as/dto/authorizationgroup/AuthorizationGroup'
import AuthorizationGroupCreation from 'as/dto/authorizationgroup/create/AuthorizationGroupCreation'
import AuthorizationGroupDeletionOptions from 'as/dto/authorizationgroup/delete/AuthorizationGroupDeletionOptions'
import AuthorizationGroupFetchOptions from 'as/dto/authorizationgroup/fetchoptions/AuthorizationGroupFetchOptions'
import AuthorizationGroupPermId from 'as/dto/authorizationgroup/id/AuthorizationGroupPermId'
import AuthorizationGroupSearchCriteria from 'as/dto/authorizationgroup/search/AuthorizationGroupSearchCriteria'
import AuthorizationGroupUpdate from 'as/dto/authorizationgroup/update/AuthorizationGroupUpdate'
import CreateAuthorizationGroupsOperation from 'as/dto/authorizationgroup/create/CreateAuthorizationGroupsOperation'
import CreateDataSetTypesOperation from 'as/dto/dataset/create/CreateDataSetTypesOperation'
import CreateExperimentTypesOperation from 'as/dto/experiment/create/CreateExperimentTypesOperation'
import CreateMaterialTypesOperation from 'as/dto/material/create/CreateMaterialTypesOperation'
import CreatePersonsOperation from 'as/dto/person/create/CreatePersonsOperation'
import CreatePluginsOperation from 'as/dto/plugin/create/CreatePluginsOperation'
import CreatePropertyTypesOperation from 'as/dto/property/create/CreatePropertyTypesOperation'
import CreateQueriesOperation from 'as/dto/query/create/CreateQueriesOperation'
import CreateRoleAssignmentsOperation from 'as/dto/roleassignment/create/CreateRoleAssignmentsOperation'
import CreateSampleTypesOperation from 'as/dto/sample/create/CreateSampleTypesOperation'
import CreateVocabulariesOperation from 'as/dto/vocabulary/create/CreateVocabulariesOperation'
import CreateVocabularyTermsOperation from 'as/dto/vocabulary/create/CreateVocabularyTermsOperation'
import CustomASServiceCode from 'as/dto/service/id/CustomASServiceCode'
import CustomASServiceExecutionOptions from 'as/dto/service/CustomASServiceExecutionOptions'
import DataSetFetchOptions from 'as/dto/dataset/fetchoptions/DataSetFetchOptions'
import DataSetSearchCriteria from 'as/dto/dataset/search/DataSetSearchCriteria'
import DataSetType from 'as/dto/dataset/DataSetType'
import DataSetTypeCreation from 'as/dto/dataset/create/DataSetTypeCreation'
import DataSetTypeDeletionOptions from 'as/dto/dataset/delete/DataSetTypeDeletionOptions'
import DataSetTypeFetchOptions from 'as/dto/dataset/fetchoptions/DataSetTypeFetchOptions'
import DataSetTypeSearchCriteria from 'as/dto/dataset/search/DataSetTypeSearchCriteria'
import DataSetTypeUpdate from 'as/dto/dataset/update/DataSetTypeUpdate'
import DataType from 'as/dto/property/DataType'
import DeleteAuthorizationGroupsOperation from 'as/dto/authorizationgroup/delete/DeleteAuthorizationGroupsOperation'
import DeleteDataSetTypesOperation from 'as/dto/dataset/delete/DeleteDataSetTypesOperation'
import DeleteExperimentTypesOperation from 'as/dto/experiment/delete/DeleteExperimentTypesOperation'
import DeleteMaterialTypesOperation from 'as/dto/material/delete/DeleteMaterialTypesOperation'
import DeletePluginsOperation from 'as/dto/plugin/delete/DeletePluginsOperation'
import DeletePropertyTypesOperation from 'as/dto/property/delete/DeletePropertyTypesOperation'
import DeleteQueriesOperation from 'as/dto/query/delete/DeleteQueriesOperation'
import DeleteRoleAssignmentsOperation from 'as/dto/roleassignment/delete/DeleteRoleAssignmentsOperation'
import DeleteSampleTypesOperation from 'as/dto/sample/delete/DeleteSampleTypesOperation'
import DeleteVocabulariesOperation from 'as/dto/vocabulary/delete/DeleteVocabulariesOperation'
import DeleteVocabularyTermsOperation from 'as/dto/vocabulary/delete/DeleteVocabularyTermsOperation'
import EntityKind from 'as/dto/entitytype/EntityKind'
import EntityTypePermId from 'as/dto/entitytype/id/EntityTypePermId'
import ExperimentFetchOptions from 'as/dto/experiment/fetchoptions/ExperimentFetchOptions'
import ExperimentSearchCriteria from 'as/dto/experiment/search/ExperimentSearchCriteria'
import ExperimentType from 'as/dto/experiment/ExperimentType'
import ExperimentTypeCreation from 'as/dto/experiment/create/ExperimentTypeCreation'
import ExperimentTypeDeletionOptions from 'as/dto/experiment/delete/ExperimentTypeDeletionOptions'
import ExperimentTypeFetchOptions from 'as/dto/experiment/fetchoptions/ExperimentTypeFetchOptions'
import ExperimentTypeSearchCriteria from 'as/dto/experiment/search/ExperimentTypeSearchCriteria'
import ExperimentTypeUpdate from 'as/dto/experiment/update/ExperimentTypeUpdate'
import MaterialFetchOptions from 'as/dto/material/fetchoptions/MaterialFetchOptions'
import MaterialSearchCriteria from 'as/dto/material/search/MaterialSearchCriteria'
import MaterialType from 'as/dto/material/MaterialType'
import MaterialTypeCreation from 'as/dto/material/create/MaterialTypeCreation'
import MaterialTypeDeletionOptions from 'as/dto/material/delete/MaterialTypeDeletionOptions'
import MaterialTypeFetchOptions from 'as/dto/material/fetchoptions/MaterialTypeFetchOptions'
import MaterialTypeSearchCriteria from 'as/dto/material/search/MaterialTypeSearchCriteria'
import MaterialTypeUpdate from 'as/dto/material/update/MaterialTypeUpdate'
import Person from 'as/dto/person/Person'
import PersonCreation from 'as/dto/person/create/PersonCreation'
import PersonFetchOptions from 'as/dto/person/fetchoptions/PersonFetchOptions'
import PersonPermId from 'as/dto/person/id/PersonPermId'
import PersonSearchCriteria from 'as/dto/person/search/PersonSearchCriteria'
import PersonUpdate from 'as/dto/person/update/PersonUpdate'
import Plugin from 'as/dto/plugin/Plugin'
import PluginCreation from 'as/dto/plugin/create/PluginCreation'
import PluginDeletionOptions from 'as/dto/plugin/delete/PluginDeletionOptions'
import PluginFetchOptions from 'as/dto/plugin/fetchoptions/PluginFetchOptions'
import PluginKind from 'as/dto/plugin/PluginKind'
import PluginPermId from 'as/dto/plugin/id/PluginPermId'
import PluginSearchCriteria from 'as/dto/plugin/search/PluginSearchCriteria'
import PluginType from 'as/dto/plugin/PluginType'
import PluginUpdate from 'as/dto/plugin/update/PluginUpdate'
import Project from 'as/dto/project/Project'
import ProjectFetchOptions from 'as/dto/project/fetchoptions/ProjectFetchOptions'
import ProjectIdentifier from 'as/dto/project/id/ProjectIdentifier'
import ProjectSearchCriteria from 'as/dto/project/search/ProjectSearchCriteria'
import PropertyAssignment from 'as/dto/property/PropertyAssignment'
import PropertyAssignmentCreation from 'as/dto/property/create/PropertyAssignmentCreation'
import PropertyAssignmentFetchOptions from 'as/dto/property/fetchoptions/PropertyAssignmentFetchOptions'
import PropertyAssignmentPermId from 'as/dto/property/id/PropertyAssignmentPermId'
import PropertyAssignmentSearchCriteria from 'as/dto/property/search/PropertyAssignmentSearchCriteria'
import PropertyType from 'as/dto/property/PropertyType'
import PropertyTypeCreation from 'as/dto/property/create/PropertyTypeCreation'
import PropertyTypeDeletionOptions from 'as/dto/property/delete/PropertyTypeDeletionOptions'
import PropertyTypeFetchOptions from 'as/dto/property/fetchoptions/PropertyTypeFetchOptions'
import PropertyTypePermId from 'as/dto/property/id/PropertyTypePermId'
import PropertyTypeSearchCriteria from 'as/dto/property/search/PropertyTypeSearchCriteria'
import PropertyTypeUpdate from 'as/dto/property/update/PropertyTypeUpdate'
import Query from 'as/dto/query/Query'
import QueryCreation from 'as/dto/query/create/QueryCreation'
import QueryDatabaseName from 'as/dto/query/id/QueryDatabaseName'
import QueryDeletionOptions from 'as/dto/query/delete/QueryDeletionOptions'
import QueryFetchOptions from 'as/dto/query/fetchoptions/QueryFetchOptions'
import QueryName from 'as/dto/query/id/QueryName'
import QuerySearchCriteria from 'as/dto/query/search/QuerySearchCriteria'
import QueryTechId from 'as/dto/query/id/QueryTechId'
import QueryType from 'as/dto/query/QueryType'
import QueryUpdate from 'as/dto/query/update/QueryUpdate'
import Role from 'as/dto/roleassignment/Role'
import RoleAssignment from 'as/dto/roleassignment/RoleAssignment'
import RoleAssignmentCreation from 'as/dto/roleassignment/create/RoleAssignmentCreation'
import RoleAssignmentDeletionOptions from 'as/dto/roleassignment/delete/RoleAssignmentDeletionOptions'
import RoleAssignmentTechId from 'as/dto/roleassignment/id/RoleAssignmentTechId'
import RoleLevel from 'as/dto/roleassignment/RoleLevel'
import SampleFetchOptions from 'as/dto/sample/fetchoptions/SampleFetchOptions'
import SampleSearchCriteria from 'as/dto/sample/search/SampleSearchCriteria'
import SampleType from 'as/dto/sample/SampleType'
import SampleTypeCreation from 'as/dto/sample/create/SampleTypeCreation'
import SampleTypeDeletionOptions from 'as/dto/sample/delete/SampleTypeDeletionOptions'
import SampleTypeFetchOptions from 'as/dto/sample/fetchoptions/SampleTypeFetchOptions'
import SampleTypeSearchCriteria from 'as/dto/sample/search/SampleTypeSearchCriteria'
import SampleTypeUpdate from 'as/dto/sample/update/SampleTypeUpdate'
import SearchDataSetsOperation from 'as/dto/dataset/search/SearchDataSetsOperation'
import SearchExperimentsOperation from 'as/dto/experiment/search/SearchExperimentsOperation'
import SearchMaterialsOperation from 'as/dto/material/search/SearchMaterialsOperation'
import SearchResult from 'as/dto/common/search/SearchResult'
import SearchSamplesOperation from 'as/dto/sample/search/SearchSamplesOperation'
import Space from 'as/dto/space/Space'
import SpaceFetchOptions from 'as/dto/space/fetchoptions/SpaceFetchOptions'
import SpacePermId from 'as/dto/space/id/SpacePermId'
import SpaceSearchCriteria from 'as/dto/space/search/SpaceSearchCriteria'
import SynchronousOperationExecutionOptions from 'as/dto/operation/SynchronousOperationExecutionOptions'
import UpdateAuthorizationGroupsOperation from 'as/dto/authorizationgroup/update/UpdateAuthorizationGroupsOperation'
import UpdateDataSetTypesOperation from 'as/dto/dataset/update/UpdateDataSetTypesOperation'
import UpdateExperimentTypesOperation from 'as/dto/experiment/update/UpdateExperimentTypesOperation'
import UpdateMaterialTypesOperation from 'as/dto/material/update/UpdateMaterialTypesOperation'
import UpdatePersonsOperation from 'as/dto/person/update/UpdatePersonsOperation'
import UpdatePluginsOperation from 'as/dto/plugin/update/UpdatePluginsOperation'
import UpdatePropertyTypesOperation from 'as/dto/property/update/UpdatePropertyTypesOperation'
import UpdateQueriesOperation from 'as/dto/query/update/UpdateQueriesOperation'
import UpdateSampleTypesOperation from 'as/dto/sample/update/UpdateSampleTypesOperation'
import UpdateVocabulariesOperation from 'as/dto/vocabulary/update/UpdateVocabulariesOperation'
import UpdateVocabularyTermsOperation from 'as/dto/vocabulary/update/UpdateVocabularyTermsOperation'
import Vocabulary from 'as/dto/vocabulary/Vocabulary'
import VocabularyCreation from 'as/dto/vocabulary/create/VocabularyCreation'
import VocabularyDeletionOptions from 'as/dto/vocabulary/delete/VocabularyDeletionOptions'
import VocabularyFetchOptions from 'as/dto/vocabulary/fetchoptions/VocabularyFetchOptions'
import VocabularyPermId from 'as/dto/vocabulary/id/VocabularyPermId'
import VocabularySearchCriteria from 'as/dto/vocabulary/search/VocabularySearchCriteria'
import VocabularyTerm from 'as/dto/vocabulary/VocabularyTerm'
import VocabularyTermCreation from 'as/dto/vocabulary/create/VocabularyTermCreation'
import VocabularyTermDeletionOptions from 'as/dto/vocabulary/delete/VocabularyTermDeletionOptions'
import VocabularyTermFetchOptions from 'as/dto/vocabulary/fetchoptions/VocabularyTermFetchOptions'
import VocabularyTermPermId from 'as/dto/vocabulary/id/VocabularyTermPermId'
import VocabularyTermSearchCriteria from 'as/dto/vocabulary/search/VocabularyTermSearchCriteria'
import VocabularyTermUpdate from 'as/dto/vocabulary/update/VocabularyTermUpdate'
import VocabularyUpdate from 'as/dto/vocabulary/update/VocabularyUpdate'
import WebAppSettingCreation from 'as/dto/webapp/create/WebAppSettingCreation'
const dto = {
  AuthorizationGroup,
  AuthorizationGroupCreation,
  AuthorizationGroupDeletionOptions,
  AuthorizationGroupFetchOptions,
  AuthorizationGroupPermId,
  AuthorizationGroupSearchCriteria,
  AuthorizationGroupUpdate,
  CreateAuthorizationGroupsOperation,
  CreateDataSetTypesOperation,
  CreateExperimentTypesOperation,
  CreateMaterialTypesOperation,
  CreatePersonsOperation,
  CreatePluginsOperation,
  CreatePropertyTypesOperation,
  CreateQueriesOperation,
  CreateRoleAssignmentsOperation,
  CreateSampleTypesOperation,
  CreateVocabulariesOperation,
  CreateVocabularyTermsOperation,
  CustomASServiceCode,
  CustomASServiceExecutionOptions,
  DataSetFetchOptions,
  DataSetSearchCriteria,
  DataSetType,
  DataSetTypeCreation,
  DataSetTypeDeletionOptions,
  DataSetTypeFetchOptions,
  DataSetTypeSearchCriteria,
  DataSetTypeUpdate,
  DataType,
  DeleteAuthorizationGroupsOperation,
  DeleteDataSetTypesOperation,
  DeleteExperimentTypesOperation,
  DeleteMaterialTypesOperation,
  DeletePluginsOperation,
  DeletePropertyTypesOperation,
  DeleteQueriesOperation,
  DeleteRoleAssignmentsOperation,
  DeleteSampleTypesOperation,
  DeleteVocabulariesOperation,
  DeleteVocabularyTermsOperation,
  EntityKind,
  EntityTypePermId,
  ExperimentFetchOptions,
  ExperimentSearchCriteria,
  ExperimentType,
  ExperimentTypeCreation,
  ExperimentTypeDeletionOptions,
  ExperimentTypeFetchOptions,
  ExperimentTypeSearchCriteria,
  ExperimentTypeUpdate,
  MaterialFetchOptions,
  MaterialSearchCriteria,
  MaterialType,
  MaterialTypeCreation,
  MaterialTypeDeletionOptions,
  MaterialTypeFetchOptions,
  MaterialTypeSearchCriteria,
  MaterialTypeUpdate,
  Person,
  PersonCreation,
  PersonFetchOptions,
  PersonPermId,
  PersonSearchCriteria,
  PersonUpdate,
  Plugin,
  PluginCreation,
  PluginDeletionOptions,
  PluginFetchOptions,
  PluginKind,
  PluginPermId,
  PluginSearchCriteria,
  PluginType,
  PluginUpdate,
  Project,
  ProjectFetchOptions,
  ProjectIdentifier,
  ProjectSearchCriteria,
  PropertyAssignment,
  PropertyAssignmentCreation,
  PropertyAssignmentFetchOptions,
  PropertyAssignmentPermId,
  PropertyAssignmentSearchCriteria,
  PropertyType,
  PropertyTypeCreation,
  PropertyTypeDeletionOptions,
  PropertyTypeFetchOptions,
  PropertyTypePermId,
  PropertyTypeSearchCriteria,
  PropertyTypeUpdate,
  Query,
  QueryCreation,
  QueryDatabaseName,
  QueryDeletionOptions,
  QueryFetchOptions,
  QueryName,
  QuerySearchCriteria,
  QueryTechId,
  QueryType,
  QueryUpdate,
  Role,
  RoleAssignment,
  RoleAssignmentCreation,
  RoleAssignmentDeletionOptions,
  RoleAssignmentTechId,
  RoleLevel,
  SampleFetchOptions,
  SampleSearchCriteria,
  SampleType,
  SampleTypeCreation,
  SampleTypeDeletionOptions,
  SampleTypeFetchOptions,
  SampleTypeSearchCriteria,
  SampleTypeUpdate,
  SearchDataSetsOperation,
  SearchExperimentsOperation,
  SearchMaterialsOperation,
  SearchResult,
  SearchSamplesOperation,
  Space,
  SpaceFetchOptions,
  SpacePermId,
  SpaceSearchCriteria,
  SynchronousOperationExecutionOptions,
  UpdateAuthorizationGroupsOperation,
  UpdateDataSetTypesOperation,
  UpdateExperimentTypesOperation,
  UpdateMaterialTypesOperation,
  UpdatePersonsOperation,
  UpdatePluginsOperation,
  UpdatePropertyTypesOperation,
  UpdateQueriesOperation,
  UpdateSampleTypesOperation,
  UpdateVocabulariesOperation,
  UpdateVocabularyTermsOperation,
  Vocabulary,
  VocabularyCreation,
  VocabularyDeletionOptions,
  VocabularyFetchOptions,
  VocabularyPermId,
  VocabularySearchCriteria,
  VocabularyTerm,
  VocabularyTermCreation,
  VocabularyTermDeletionOptions,
  VocabularyTermFetchOptions,
  VocabularyTermPermId,
  VocabularyTermSearchCriteria,
  VocabularyTermUpdate,
  VocabularyUpdate,
  WebAppSettingCreation
}

export default dto
