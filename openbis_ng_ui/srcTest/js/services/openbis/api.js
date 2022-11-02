import dto from './dto.js'

const init = jest.fn()
const login = jest.fn()
const logout = jest.fn()
const deleteDataSetTypes = jest.fn()
const deleteExperimentTypes = jest.fn()
const deleteMaterialTypes = jest.fn()
const deleteSampleTypes = jest.fn()
const executeOperations = jest.fn()
const executeQuery = jest.fn()
const executeSql = jest.fn()
const evaluatePlugin = jest.fn()
const getAuthorizationGroups = jest.fn()
const getDataSetTypes = jest.fn()
const getExperimentTypes = jest.fn()
const getMaterialTypes = jest.fn()
const getPersons = jest.fn()
const getPropertyTypes = jest.fn()
const getSampleTypes = jest.fn()
const getServerPublicInformation = jest.fn()
const getVocabularies = jest.fn()
const getPlugins = jest.fn()
const getQueries = jest.fn()
const searchAuthorizationGroups = jest.fn()
const searchDataSetTypes = jest.fn()
const searchExperimentTypes = jest.fn()
const searchMaterialTypes = jest.fn()
const searchMaterials = jest.fn()
const searchPersons = jest.fn()
const searchPlugins = jest.fn()
const searchQueries = jest.fn()
const searchQueryDatabases = jest.fn()
const searchSpaces = jest.fn()
const searchProjects = jest.fn()
const searchPropertyAssignments = jest.fn()
const searchPropertyTypes = jest.fn()
const searchSamples = jest.fn()
const searchSampleTypes = jest.fn()
const searchVocabularies = jest.fn()
const searchVocabularyTerms = jest.fn()
const searchPersonalAccessTokens = jest.fn()
const updateDataSetTypes = jest.fn()
const updateExperimentTypes = jest.fn()
const updateMaterialTypes = jest.fn()
const updatePersons = jest.fn()
const updateSampleTypes = jest.fn()

const mockGetMe = me => {
  const map = {}

  let person = me
  if (!person) {
    person = new dto.Person()
    person.setWebAppSettings({})
  }
  map[new dto.Me()] = person

  getPersons.mockReturnValue(Promise.resolve(map))
}

const mockSearchGroups = groups => {
  const searchGroupsResult = new dto.SearchResult()
  searchGroupsResult.setObjects(groups)

  searchAuthorizationGroups.mockReturnValue(Promise.resolve(searchGroupsResult))
}

const mockSearchPersons = persons => {
  const searchPersonResult = new dto.SearchResult()
  searchPersonResult.setObjects(persons)

  searchPersons.mockReturnValue(Promise.resolve(searchPersonResult))
}

const mockSearchSampleTypes = sampleTypes => {
  const searchResult = new dto.SearchResult()
  searchResult.setObjects(sampleTypes)
  searchSampleTypes.mockReturnValue(Promise.resolve(searchResult))
}

const mockSearchExperimentTypes = experimentTypes => {
  const searchResult = new dto.SearchResult()
  searchResult.setObjects(experimentTypes)
  searchExperimentTypes.mockReturnValue(Promise.resolve(searchResult))
}

const mockSearchDataSetTypes = dataSetTypes => {
  const searchResult = new dto.SearchResult()
  searchResult.setObjects(dataSetTypes)
  searchDataSetTypes.mockReturnValue(Promise.resolve(searchResult))
}

const mockSearchMaterialTypes = materialTypes => {
  const searchResult = new dto.SearchResult()
  searchResult.setObjects(materialTypes)
  searchMaterialTypes.mockReturnValue(Promise.resolve(searchResult))
}

const mockSearchPropertyTypes = propertyTypes => {
  const searchResult = new dto.SearchResult()
  searchResult.setObjects(propertyTypes)
  searchPropertyTypes.mockReturnValue(Promise.resolve(searchResult))
}

const mockSearchPropertyAssignments = propertyAssignments => {
  const searchResult = new dto.SearchResult()
  searchResult.setObjects(propertyAssignments)
  searchPropertyAssignments.mockReturnValue(Promise.resolve(searchResult))
}

const mockSearchVocabularies = vocabularies => {
  const searchResult = new dto.SearchResult()
  searchResult.setObjects(vocabularies)
  searchVocabularies.mockReturnValue(Promise.resolve(searchResult))
}

const mockSearchPlugins = plugins => {
  const searchResult = new dto.SearchResult()
  searchResult.setObjects(plugins)
  searchPlugins.mockReturnValue(Promise.resolve(searchResult))
}

const mockSearchQueries = queries => {
  const searchResult = new dto.SearchResult()
  searchResult.setObjects(queries)
  searchQueries.mockReturnValue(Promise.resolve(searchResult))
}

const mockSearchQueryDatabases = databases => {
  const searchResult = new dto.SearchResult()
  searchResult.setObjects(databases)
  searchQueries.mockReturnValue(Promise.resolve(searchResult))
}

const mockSearchPersonalAccessTokens = pats => {
  const searchResult = new dto.SearchResult()
  searchResult.setObjects(pats)
  searchPersonalAccessTokens.mockReturnValue(Promise.resolve(searchResult))
}

const mockSearchSpaces = spaces => {
  const searchResult = new dto.SearchResult()
  searchResult.setObjects(spaces)
  searchSpaces.mockReturnValue(Promise.resolve(searchResult))
}

const mockSearchSamples = samples => {
  const searchResult = new dto.SearchResult()
  searchResult.setObjects(samples)
  searchSamples.mockReturnValue(Promise.resolve(searchResult))
}

export default {
  init,
  login,
  logout,
  deleteDataSetTypes,
  deleteExperimentTypes,
  deleteMaterialTypes,
  deleteSampleTypes,
  executeOperations,
  executeQuery,
  executeSql,
  evaluatePlugin,
  getAuthorizationGroups,
  getDataSetTypes,
  getExperimentTypes,
  getMaterialTypes,
  getPersons,
  getPropertyTypes,
  getSampleTypes,
  getServerPublicInformation,
  getVocabularies,
  getPlugins,
  getQueries,
  searchAuthorizationGroups,
  searchDataSetTypes,
  searchExperimentTypes,
  searchMaterialTypes,
  searchMaterials,
  searchPersons,
  searchPlugins,
  searchQueries,
  searchQueryDatabases,
  searchSpaces,
  searchProjects,
  searchPropertyAssignments,
  searchPropertyTypes,
  searchSamples,
  searchSampleTypes,
  searchVocabularies,
  searchVocabularyTerms,
  searchPersonalAccessTokens,
  updateDataSetTypes,
  updateExperimentTypes,
  updateMaterialTypes,
  updatePersons,
  updateSampleTypes,
  mockGetMe,
  mockSearchDataSetTypes,
  mockSearchExperimentTypes,
  mockSearchGroups,
  mockSearchMaterialTypes,
  mockSearchPersons,
  mockSearchSampleTypes,
  mockSearchPropertyTypes,
  mockSearchPropertyAssignments,
  mockSearchVocabularies,
  mockSearchPlugins,
  mockSearchQueries,
  mockSearchQueryDatabases,
  mockSearchPersonalAccessTokens,
  mockSearchSpaces,
  mockSearchSamples
}
