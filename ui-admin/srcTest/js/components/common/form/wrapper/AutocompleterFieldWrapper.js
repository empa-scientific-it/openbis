import FieldWrapper from './FieldWrapper.js'

export default class AutocompleterFieldWrapper extends FieldWrapper {
  getFocused() {
    if (this.getMode() === 'edit') {
      return document.activeElement === this.wrapper.find('input').getDOMNode()
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
