import PageControllerValidate from '@src/js/components/common/page/PageConrollerValidate.js'
import QueryFormSelectionType from '@src/js/components/tools/form/query/QueryFormSelectionType.js'

export default class QueryFormControllerValidate extends PageControllerValidate {
  validate(validator) {
    const { query } = this.context.getState()

    const newQuery = this._validateQuery(validator, query)

    return {
      query: newQuery
    }
  }

  async select(firstError) {
    const { query } = this.context.getState()

    if (firstError.object === query) {
      await this.setSelection({
        type: QueryFormSelectionType.QUERY,
        params: {
          part: firstError.name
        }
      })
    }
  }

  _validateQuery(validator, query) {
    validator.validateNotEmpty(query, 'name', 'Name')
    validator.validateNotEmpty(query, 'databaseId', 'Database')
    validator.validateNotEmpty(query, 'queryType', 'Query Type')
    validator.validateNotEmpty(query, 'sql', 'SQL')
    return validator.withErrors(query)
  }
}
