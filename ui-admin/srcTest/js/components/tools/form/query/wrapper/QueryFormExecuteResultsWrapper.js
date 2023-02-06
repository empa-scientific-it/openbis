import Grid from '@src/js/components/common/grid/Grid.jsx'
import GridWrapper from '@srcTest/js/components/common/grid/wrapper/GridWrapper.js'
import PageParametersPanelWrapper from '@srcTest/js/components/common/page/wrapper/PageParametersPanelWrapper.js'

export default class QueryFormExecuteResultsWrapper extends PageParametersPanelWrapper {
  getGrid() {
    return new GridWrapper(this.findComponent(Grid))
  }

  toJSON() {
    return {
      ...super.toJSON(),
      grid: this.getGrid().toJSON()
    }
  }
}
