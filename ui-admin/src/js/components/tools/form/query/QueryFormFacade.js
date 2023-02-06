import openbis from '@src/js/services/openbis.js'

export default class QueryFormFacade {
  async loadQuery(queryName) {
    const id = new openbis.QueryName(queryName)
    const fo = new openbis.QueryFetchOptions()
    return openbis.getQueries([id], fo).then(map => {
      return map[queryName]
    })
  }

  async loadQueryDatabases() {
    const criteria = new openbis.QueryDatabaseSearchCriteria()
    const fo = new openbis.QueryDatabaseFetchOptions()
    fo.withSpace()
    const result = await openbis.searchQueryDatabases(criteria, fo)
    return result.getObjects()
  }

  async loadExperimentTypes() {
    const result = await openbis.searchExperimentTypes(
      new openbis.ExperimentTypeSearchCriteria(),
      new openbis.ExperimentTypeFetchOptions()
    )
    return result.getObjects()
  }

  async loadSampleTypes() {
    const result = await openbis.searchSampleTypes(
      new openbis.SampleTypeSearchCriteria(),
      new openbis.SampleTypeFetchOptions()
    )
    return result.getObjects()
  }

  async loadDataSetTypes() {
    const result = await openbis.searchDataSetTypes(
      new openbis.DataSetTypeSearchCriteria(),
      new openbis.DataSetTypeFetchOptions()
    )
    return result.getObjects()
  }

  async loadMaterialTypes() {
    const result = await openbis.searchMaterialTypes(
      new openbis.MaterialTypeSearchCriteria(),
      new openbis.MaterialTypeFetchOptions()
    )
    return result.getObjects()
  }

  async executeQuery(id, options) {
    return openbis.executeQuery(id, options)
  }

  async executeSql(sql, options) {
    return openbis.executeSql(sql, options)
  }

  async executeOperations(operations, options) {
    return openbis.executeOperations(operations, options)
  }
}
