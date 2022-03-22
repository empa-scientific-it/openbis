import PageMode from '@src/js/components/common/page/PageMode.js'
import AppController from '@src/js/components/AppController.js'
import openbis from '@src/js/services/openbis.js'

export default class QueryFormControllerExecute {
  constructor(controller) {
    this.controller = controller
    this.context = controller.getContext()
    this.facade = controller.getFacade()
  }

  async execute() {
    try {
      const { mode, query, executeParameters } = this.context.getState()

      this.context.setState(state => ({
        ...state,
        executeResults: {
          ...state.executeResults,
          loading: true
        }
      }))

      let tableModel = null

      if (mode === PageMode.VIEW) {
        tableModel = await this.executeQuery(query, executeParameters.values)
      } else if (mode === PageMode.EDIT) {
        tableModel = await this.executeSql(query, executeParameters.values)
      } else {
        throw new Error('Unsupported mode: ' + mode)
      }

      this.context.setState(state => ({
        ...state,
        executeResults: {
          ...state.executeResults,
          loaded: true,
          tableModel,
          timestamp: Date.now()
        }
      }))
    } catch (error) {
      AppController.getInstance().errorChange(error)
    } finally {
      this.context.setState(state => ({
        ...state,
        executeResults: {
          ...state.executeResults,
          loading: false
        }
      }))
    }
  }

  async executeQuery(query, parameters) {
    const id = new openbis.QueryName(query.name.value)
    const options = new openbis.QueryExecutionOptions()

    Object.entries(parameters).forEach(([key, value]) => {
      if (value && value.trim().length > 0) {
        options.withParameter(key, value)
      }
    })

    return await this.facade.executeQuery(id, options)
  }

  async executeSql(query, parameters) {
    const options = new openbis.SqlExecutionOptions()

    if (query.databaseId.value) {
      options.withDatabaseId(
        new openbis.QueryDatabaseName(query.databaseId.value)
      )
    }

    Object.entries(parameters).forEach(([key, value]) => {
      if (value && value.trim().length > 0) {
        options.withParameter(key, value)
      }
    })

    return await this.facade.executeSql(query.sql.value, options)
  }
}
