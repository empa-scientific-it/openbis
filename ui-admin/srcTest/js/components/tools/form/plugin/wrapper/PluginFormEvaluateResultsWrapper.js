import Typography from '@material-ui/core/Typography'
import PageParametersPanelWrapper from '@srcTest/js/components/common/page/wrapper/PageParametersPanelWrapper.js'

export default class QueryFormExecuteResultsWrapper extends PageParametersPanelWrapper {
  getResult() {
    const result = this.findComponent(Typography).filter({
      'data-part': 'result'
    })
    return result.exists() ? result.text() : null
  }

  toJSON() {
    return {
      ...super.toJSON(),
      result: this.getResult()
    }
  }
}
