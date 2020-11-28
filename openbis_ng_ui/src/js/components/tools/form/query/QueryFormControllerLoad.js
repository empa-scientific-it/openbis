import _ from 'lodash'
import PageControllerLoad from '@src/js/components/common/page/PageControllerLoad.js'
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
      experimentTypes,
      sampleTypes,
      dataSetTypes,
      materialTypes
    ] = await Promise.all([
      this.facade.loadExperimentTypes(),
      this.facade.loadSampleTypes(),
      this.facade.loadDataSetTypes(),
      this.facade.loadMaterialTypes()
    ])

    await this.context.setState(() => ({
      dictionaries: {
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

    return this.context.setState({
      query
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
}
