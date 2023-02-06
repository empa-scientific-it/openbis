import PageControllerSave from '@src/js/components/common/page/PageControllerSave.js'
import FormUtil from '@src/js/components/common/form/FormUtil.js'
import openbis from '@src/js/services/openbis.js'

export default class QueryFormControllerSave extends PageControllerSave {
  async save() {
    const state = this.context.getState()

    const query = FormUtil.trimFields({ ...state.query })
    const operations = []

    if (query.original) {
      if (this._isQueryUpdateNeeded(query)) {
        operations.push(this._updateQueryOperation(query))
      }
    } else {
      operations.push(this._createQueryOperation(query))
    }

    const options = new openbis.SynchronousOperationExecutionOptions()
    options.setExecuteInOrder(true)
    await this.facade.executeOperations(operations, options)

    return query.name.value
  }

  _isQueryUpdateNeeded(query) {
    return FormUtil.haveFieldsChanged(query, query.original, [
      'name',
      'description',
      'databaseId',
      'queryType',
      'entityTypeCodePattern',
      'sql',
      'publicFlag'
    ])
  }

  _createQueryOperation(query) {
    const creation = new openbis.QueryCreation()
    creation.setName(query.name.value)
    creation.setDescription(query.description.value)
    creation.setDatabaseId(
      new openbis.QueryDatabaseName(query.databaseId.value)
    )
    creation.setQueryType(query.queryType.value)
    creation.setEntityTypeCodePattern(query.entityTypeCodePattern.value)
    creation.setSql(query.sql.value)
    creation.setPublic(query.publicFlag.value)
    return new openbis.CreateQueriesOperation([creation])
  }

  _updateQueryOperation(query) {
    const update = new openbis.QueryUpdate()
    update.setQueryId(new openbis.QueryName(query.name.value))
    update.setDescription(query.description.value)
    update.setDatabaseId(new openbis.QueryDatabaseName(query.databaseId.value))
    update.setQueryType(query.queryType.value)
    update.setEntityTypeCodePattern(query.entityTypeCodePattern.value)
    update.setSql(query.sql.value)
    update.setPublic(query.publicFlag.value)
    return new openbis.UpdateQueriesOperation([update])
  }
}
