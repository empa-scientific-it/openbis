import _ from 'lodash'
import PageControllerChange from '@src/js/components/common/page/PageControllerChange.js'
import QueryFormSelectionType from '@src/js/components/tools/form/query/QueryFormSelectionType.js'
import QueryFormControllerParseSql from '@src/js/components/tools/form/query/QueryFormControllerParseSql.js'
import FormUtil from '@src/js/components/common/form/FormUtil.js'
import openbis from '@src/js/services/openbis.js'

export default class QueryFormControllerChange extends PageControllerChange {
  async execute(type, params) {
    if (type === QueryFormSelectionType.QUERY) {
      await this._handleChangeQuery(params)
    } else if (type === QueryFormSelectionType.EXECUTE_PARAMETER) {
      await this._handleChangeExecuteParameter(params)
    }
  }

  async _handleChangeQuery(params) {
    await this.context.setState(oldState => {
      const { newObject } = FormUtil.changeObjectField(
        oldState.query,
        params.field,
        params.value
      )

      const newState = {
        ...oldState,
        query: newObject
      }

      this._handleChangeQueryType(oldState, newState)
      this._handleChangeSql(oldState, newState)

      return newState
    })
    await this.controller.changed(true)
  }

  async _handleChangeExecuteParameter(params) {
    await this.context.setState(oldState => ({
      executeParameters: {
        ...oldState.executeParameters,
        values: {
          ...oldState.executeParameters.values,
          [params.field]: params.value
        }
      }
    }))
  }

  _handleChangeQueryType(oldState, newState) {
    const oldQuery = oldState.query
    const newQuery = newState.query

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

  _handleChangeSql(oldState, newState) {
    const oldSql = oldState.query.sql.value
    const newSql = newState.query.sql.value

    if (oldSql !== newSql) {
      const parsedSql = new QueryFormControllerParseSql().parse(newSql)
      _.assign(newState, {
        executeParameters: {
          ...oldState.executeParameters,
          names: parsedSql.parameterNames,
          values: _.pick(
            oldState.executeParameters.values,
            parsedSql.parameterNames
          )
        }
      })
    }
  }
}
