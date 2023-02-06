import _ from 'lodash'
import PageControllerLoad from '@src/js/components/common/page/PageControllerLoad.js'
import QueryFormControllerParseSql from '@src/js/components/tools/form/query/QueryFormControllerParseSql.js'
import FormUtil from '@src/js/components/common/form/FormUtil.js'
import openbis from '@src/js/services/openbis.js'

export default class QueryFormControllerLoad extends PageControllerLoad {
  async load(object, isNew) {
    return Promise.all([
      this._loadDictionaries(object),
      this._loadQuery(object, isNew)
    ])
  }

  async _loadDictionaries() {
    const [
      queryDatabases,
      experimentTypes,
      sampleTypes,
      dataSetTypes,
      materialTypes
    ] = await Promise.all([
      this.facade.loadQueryDatabases(),
      this.facade.loadExperimentTypes(),
      this.facade.loadSampleTypes(),
      this.facade.loadDataSetTypes(),
      this.facade.loadMaterialTypes()
    ])

    await this.context.setState(() => ({
      dictionaries: {
        queryDatabases,
        experimentTypes,
        sampleTypes,
        dataSetTypes,
        materialTypes
      }
    }))
  }

  async _loadQuery(object, isNew) {
    let loadedQuery = null

    if (!isNew) {
      loadedQuery = await this.facade.loadQuery(object.id)
      if (!loadedQuery) {
        return
      }
    }

    const query = this._createQuery(loadedQuery)
    const executeParameters = this._createExecuteParameters(loadedQuery)
    const executeResults = this._createExecuteResults()

    return this.context.setState({
      query,
      executeParameters,
      executeResults
    })
  }

  _createQuery(loadedQuery) {
    const queryType = _.get(loadedQuery, 'queryType', null)

    const query = {
      name: FormUtil.createField({
        value: _.get(loadedQuery, 'name', null),
        enabled: loadedQuery === null
      }),
      description: FormUtil.createField({
        value: _.get(loadedQuery, 'description', null)
      }),
      databaseId: FormUtil.createField({
        value: _.get(loadedQuery, 'databaseId.name', null)
      }),
      queryType: FormUtil.createField({
        value: queryType
      }),
      entityTypeCodePattern: FormUtil.createField({
        value: _.get(loadedQuery, 'entityTypeCodePattern', null),
        visible: queryType && queryType !== openbis.QueryType.GENERIC
      }),
      sql: FormUtil.createField({
        value: _.get(loadedQuery, 'sql', null)
      }),
      publicFlag: FormUtil.createField({
        value: _.get(loadedQuery, 'publicFlag', false)
      })
    }
    if (loadedQuery) {
      query.original = _.cloneDeep(query)
    }
    return query
  }

  _createExecuteParameters(loadedQuery) {
    let parameterNames = []

    if (loadedQuery) {
      const parsedSql = new QueryFormControllerParseSql().parse(loadedQuery.sql)
      parameterNames = parsedSql.parameterNames
    }

    return {
      names: parameterNames,
      values: {}
    }
  }

  _createExecuteResults() {
    return {
      loading: false,
      loaded: false,
      tableModel: null
    }
  }
}
