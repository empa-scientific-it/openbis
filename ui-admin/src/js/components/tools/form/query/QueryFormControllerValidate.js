import PageControllerValidate from '@src/js/components/common/page/PageConrollerValidate.js'
import QueryFormSelectionType from '@src/js/components/tools/form/query/QueryFormSelectionType.js'
import messages from '@src/js/common/messages.js'

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
    validator.validateNotEmpty(query, 'name', messages.get(messages.NAME))
    validator.validateNotEmpty(
      query,
      'databaseId',
      messages.get(messages.DATABASE)
    )
    validator.validateNotEmpty(
      query,
      'queryType',
      messages.get(messages.QUERY_TYPE)
    )
    validator.validateNotEmpty(query, 'sql', messages.get(messages.SQL))
    return validator.withErrors(query)
  }
}
