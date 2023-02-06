import FieldWrapper from './FieldWrapper.js'

export default class SourceCodeFieldWrapper extends FieldWrapper {
  getFocused() {
    if (this.getMode() === 'edit') {
      return (
        document.activeElement === this.wrapper.find('textarea').getDOMNode()
      )
    } else {
      return false
    }
  }
}
