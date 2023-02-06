import Select from '@material-ui/core/Select'
import FieldWrapper from './FieldWrapper.js'

export default class SelectFieldWrapper extends FieldWrapper {
  getFocused() {
    if (this.getMode() === 'edit') {
      return (
        document.activeElement ===
        this.findComponent(Select).find('div.MuiSelect-root').getDOMNode()
      )
    } else {
      return false
    }
  }
  getOptions() {
    return this.wrapper.prop('options')
  }

  toJSON() {
    if (this.wrapper.exists()) {
      return {
        ...super.toJSON(),
        options: this.getOptions()
      }
    } else {
      return null
    }
  }
}
