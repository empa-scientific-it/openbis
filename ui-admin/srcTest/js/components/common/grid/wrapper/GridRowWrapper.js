import BaseWrapper from '@srcTest/js/components/common/wrapper/BaseWrapper.js'
import TableCell from '@material-ui/core/TableCell'

export default class GridRowWrapper extends BaseWrapper {
  getValues() {
    const columns = this.wrapper.prop('columns')
    const values = {}

    this.findComponent(TableCell).forEach((cell, index) => {
      const column = columns[index]
      values[column.name] = this.getStringValue(cell.text().trim())
    })

    return values
  }

  getSelected() {
    return this.getBooleanValue(this.wrapper.prop('selected'))
  }

  click() {
    this.wrapper.instance().handleClick()
  }

  toJSON() {
    if (this.wrapper.exists()) {
      return {
        values: this.getValues(),
        selected: this.getSelected()
      }
    } else {
      return null
    }
  }
}
