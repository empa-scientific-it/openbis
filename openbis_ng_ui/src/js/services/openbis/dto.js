const CLASS_FULL_NAMES = [
  'as/dto/authorizationgroup/AuthorizationGroup',
  'as/dto/authorizationgroup/create/AuthorizationGroupCreation',
  'as/dto/authorizationgroup/create/CreateAuthorizationGroupsOperation',
  'as/dto/authorizationgroup/delete/AuthorizationGroupDeletionOptions',
  'as/dto/authorizationgroup/delete/DeleteAuthorizationGroupsOperation',
  'as/dto/authorizationgroup/fetchoptions/AuthorizationGroupFetchOptions',
  'as/dto/authorizationgroup/id/AuthorizationGroupPermId',
  'as/dto/authorizationgroup/search/AuthorizationGroupSearchCriteria',
  'as/dto/authorizationgroup/update/AuthorizationGroupUpdate',
  'as/dto/authorizationgroup/update/UpdateAuthorizationGroupsOperation',
  'as/dto/dataset/create/CreateDataSetTypesOperation',
  'as/dto/dataset/create/DataSetTypeCreation',
  'as/dto/dataset/delete/DataSetTypeDeletionOptions',
  'as/dto/dataset/delete/DeleteDataSetTypesOperation',
  'as/dto/dataset/fetchoptions/DataSetFetchOptions',
  'as/dto/dataset/fetchoptions/DataSetTypeFetchOptions',
  'as/dto/dataset/search/DataSetSearchCriteria',
  'as/dto/dataset/search/DataSetTypeSearchCriteria',
  'as/dto/dataset/search/SearchDataSetsOperation',
  'as/dto/dataset/update/DataSetTypeUpdate',
  'as/dto/dataset/update/UpdateDataSetTypesOperation',
  'as/dto/entitytype/EntityKind',
  'as/dto/entitytype/id/EntityTypePermId',
  'as/dto/experiment/create/CreateExperimentTypesOperation',
  'as/dto/experiment/create/ExperimentTypeCreation',
  'as/dto/experiment/delete/DeleteExperimentTypesOperation',
  'as/dto/experiment/delete/ExperimentTypeDeletionOptions',
  'as/dto/experiment/fetchoptions/ExperimentFetchOptions',
  'as/dto/experiment/fetchoptions/ExperimentTypeFetchOptions',
  'as/dto/experiment/search/ExperimentSearchCriteria',
  'as/dto/experiment/search/ExperimentTypeSearchCriteria',
  'as/dto/experiment/search/SearchExperimentsOperation',
  'as/dto/experiment/update/ExperimentTypeUpdate',
  'as/dto/experiment/update/UpdateExperimentTypesOperation',
  'as/dto/material/create/CreateMaterialTypesOperation',
  'as/dto/material/create/MaterialTypeCreation',
  'as/dto/material/delete/DeleteMaterialTypesOperation',
  'as/dto/material/delete/MaterialTypeDeletionOptions',
  'as/dto/material/fetchoptions/MaterialFetchOptions',
  'as/dto/material/fetchoptions/MaterialTypeFetchOptions',
  'as/dto/material/search/MaterialSearchCriteria',
  'as/dto/material/search/MaterialTypeSearchCriteria',
  'as/dto/material/search/SearchMaterialsOperation',
  'as/dto/material/update/MaterialTypeUpdate',
  'as/dto/material/update/UpdateMaterialTypesOperation',
  'as/dto/operation/SynchronousOperationExecutionOptions',
  'as/dto/person/create/CreatePersonsOperation',
  'as/dto/person/create/PersonCreation',
  'as/dto/person/fetchoptions/PersonFetchOptions',
  'as/dto/person/id/PersonPermId',
  'as/dto/person/search/PersonSearchCriteria',
  'as/dto/person/update/PersonUpdate',
  'as/dto/person/update/UpdatePersonsOperation',
  'as/dto/plugin/PluginType',
  'as/dto/plugin/fetchoptions/PluginFetchOptions',
  'as/dto/plugin/id/PluginPermId',
  'as/dto/plugin/search/PluginSearchCriteria',
  'as/dto/project/Project',
  'as/dto/project/fetchoptions/ProjectFetchOptions',
  'as/dto/project/id/ProjectIdentifier',
  'as/dto/project/search/ProjectSearchCriteria',
  'as/dto/property/DataType',
  'as/dto/property/PropertyType',
  'as/dto/property/create/CreatePropertyTypesOperation',
  'as/dto/property/create/PropertyAssignmentCreation',
  'as/dto/property/create/PropertyTypeCreation',
  'as/dto/property/delete/DeletePropertyTypesOperation',
  'as/dto/property/delete/PropertyTypeDeletionOptions',
  'as/dto/property/fetchoptions/PropertyAssignmentFetchOptions',
  'as/dto/property/fetchoptions/PropertyTypeFetchOptions',
  'as/dto/property/id/PropertyAssignmentPermId',
  'as/dto/property/id/PropertyTypePermId',
  'as/dto/property/search/PropertyAssignmentSearchCriteria',
  'as/dto/property/search/PropertyTypeSearchCriteria',
  'as/dto/property/update/PropertyTypeUpdate',
  'as/dto/property/update/UpdatePropertyTypesOperation',
  'as/dto/roleassignment/Role',
  'as/dto/roleassignment/RoleAssignment',
  'as/dto/roleassignment/RoleLevel',
  'as/dto/roleassignment/create/CreateRoleAssignmentsOperation',
  'as/dto/roleassignment/create/RoleAssignmentCreation',
  'as/dto/roleassignment/delete/DeleteRoleAssignmentsOperation',
  'as/dto/roleassignment/delete/RoleAssignmentDeletionOptions',
  'as/dto/roleassignment/id/RoleAssignmentTechId',
  'as/dto/sample/create/CreateSampleTypesOperation',
  'as/dto/sample/create/SampleTypeCreation',
  'as/dto/sample/delete/DeleteSampleTypesOperation',
  'as/dto/sample/delete/SampleTypeDeletionOptions',
  'as/dto/sample/fetchoptions/SampleFetchOptions',
  'as/dto/sample/fetchoptions/SampleTypeFetchOptions',
  'as/dto/sample/fetchoptions/SampleTypeFetchOptions',
  'as/dto/sample/search/SampleSearchCriteria',
  'as/dto/sample/search/SampleTypeSearchCriteria',
  'as/dto/sample/search/SearchSamplesOperation',
  'as/dto/sample/update/SampleTypeUpdate',
  'as/dto/sample/update/UpdateSampleTypesOperation',
  'as/dto/service/CustomASServiceExecutionOptions',
  'as/dto/service/id/CustomASServiceCode',
  'as/dto/space/Space',
  'as/dto/space/fetchoptions/SpaceFetchOptions',
  'as/dto/space/id/SpacePermId',
  'as/dto/space/search/SpaceSearchCriteria',
  'as/dto/vocabulary/Vocabulary',
  'as/dto/vocabulary/VocabularyTerm',
  'as/dto/vocabulary/create/CreateVocabulariesOperation',
  'as/dto/vocabulary/create/CreateVocabularyTermsOperation',
  'as/dto/vocabulary/create/VocabularyCreation',
  'as/dto/vocabulary/create/VocabularyTermCreation',
  'as/dto/vocabulary/delete/DeleteVocabulariesOperation',
  'as/dto/vocabulary/delete/DeleteVocabularyTermsOperation',
  'as/dto/vocabulary/delete/VocabularyDeletionOptions',
  'as/dto/vocabulary/delete/VocabularyTermDeletionOptions',
  'as/dto/vocabulary/fetchoptions/VocabularyFetchOptions',
  'as/dto/vocabulary/fetchoptions/VocabularyTermFetchOptions',
  'as/dto/vocabulary/id/VocabularyPermId',
  'as/dto/vocabulary/id/VocabularyTermPermId',
  'as/dto/vocabulary/search/VocabularySearchCriteria',
  'as/dto/vocabulary/search/VocabularyTermSearchCriteria',
  'as/dto/vocabulary/update/UpdateVocabulariesOperation',
  'as/dto/vocabulary/update/UpdateVocabularyTermsOperation',
  'as/dto/vocabulary/update/VocabularyTermUpdate',
  'as/dto/vocabulary/update/VocabularyUpdate',
  'as/dto/webapp/create/WebAppSettingCreation'
]

class Dto {
  _init() {
    let _this = this

    let load = function (index) {
      return new Promise((resolve, reject) => {
        if (index < CLASS_FULL_NAMES.length) {
          let classFullName = CLASS_FULL_NAMES[index]
          let className = classFullName.substring(
            classFullName.lastIndexOf('/') + 1
          )
          /* eslint-disable-next-line no-undef */
          requirejs(
            [classFullName],
            clazz => {
              _this[className] = clazz
              return load(index + 1).then(resolve, reject)
            },
            error => {
              reject(error)
            }
          )
        } else {
          resolve()
        }
      })
    }

    return load(0)
  }
}

const dto = new Dto()

CLASS_FULL_NAMES.forEach(classFullName => {
  let className = classFullName.substring(classFullName.lastIndexOf('/') + 1)
  dto[className] = function () {}
})

export default dto
