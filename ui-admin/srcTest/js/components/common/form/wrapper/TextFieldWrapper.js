import FieldWrapper from './FieldWrapper.js'

export default class TextFieldWrapper extends FieldWrapper {
  getMultiline() {
    return this.getBooleanValue(this.wrapper.prop('multiline'))
  }

  getFocused() {
    if (this.getMode() === 'edit') {
      if (this.getMultiline()) {
        return (
          document.activeElement ===
          this.wrapper
            .find('textarea')
            .filterWhere(node => !node.prop('aria-hidden'))
            .getDOMNode()
        )
      } else {
        return (
          document.activeElement === this.wrapper.find('input').getDOMNode()
        )
      }
    } else {
      return false
    }
  }
}
