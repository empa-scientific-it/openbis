import _ from 'lodash'
import PageControllerChange from '@src/js/components/common/page/PageControllerChange.js'
import QueryFormSelectionType from '@src/js/components/tools/form/query/QueryFormSelectionType.js'
import FormUtil from '@src/js/components/common/form/FormUtil.js'
import openbis from '@src/js/services/openbis.js'

export default class QueryFormControllerChange extends PageControllerChange {
  async execute(type, params) {
    if (type === QueryFormSelectionType.QUERY) {
      await this._handleChangeQuery(params)
    }
  }

  async _handleChangeQuery(params) {
    await this.context.setState(state => {
      const { oldObject, newObject } = FormUtil.changeObjectField(
        state.query,
        params.field,
        params.value
      )

      this._handleChangeQueryType(oldObject, newObject)

      return {
        query: newObject
      }
    })
    await this.controller.changed(true)
  }

  _handleChangeQueryType(oldQuery, newQuery) {
    const oldQueryType = oldQuery.queryType.value
    const newQueryType = newQuery.queryType.value

    if (oldQueryType !== newQueryType) {
      _.assign(newQuery, {
        entityTypeCodePattern: {
          ...newQuery.entityTypeCodePattern,
          visible: newQueryType !== openbis.QueryType.GENERIC,
          value: null
        }
      })
    }
  }
}
