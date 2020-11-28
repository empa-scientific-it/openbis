import BaseWrapper from '@srcTest/js/components/common/wrapper/BaseWrapper.js'
import QueryFormSql from '@src/js/components/tools/form/query/QueryFormSql.jsx'
import QueryFormSqlWrapper from '@srcTest/js/components/tools/form/query/wrapper/QueryFormSqlWrapper.js'
import QueryFormParameters from '@src/js/components/tools/form/query/QueryFormParameters.jsx'
import QueryFormParametersWrapper from '@srcTest/js/components/tools/form/query/wrapper/QueryFormParametersWrapper.js'
import QueryFormButtons from '@src/js/components/tools/form/query/QueryFormButtons.jsx'
import QueryFormButtonsWrapper from '@srcTest/js/components/tools/form/query/wrapper/QueryFormButtonsWrapper.js'

export default class QueryFormWrapper extends BaseWrapper {
  getSql() {
    return new QueryFormSqlWrapper(this.findComponent(QueryFormSql))
  }

  getParameters() {
    return new QueryFormParametersWrapper(
      this.findComponent(QueryFormParameters)
    )
  }

  getButtons() {
    return new QueryFormButtonsWrapper(this.findComponent(QueryFormButtons))
  }

  toJSON() {
    return {
      sql: this.getSql().toJSON(),
      parameters: this.getParameters().toJSON(),
      buttons: this.getButtons().toJSON()
    }
  }
}
