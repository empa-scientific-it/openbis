import BaseWrapper from '@srcTest/js/components/common/wrapper/BaseWrapper.js'

export default class FieldWrapper extends BaseWrapper {
  getName() {
    return this.wrapper.prop('name')
  }

  getLabel() {
    return this.wrapper.prop('label')
  }

  getValue() {
    return this.getStringValue(this.wrapper.prop('value'))
  }

  getEnabled() {
    return !this.getBooleanValue(this.wrapper.prop('disabled'))
  }

  getFocused() {
    throw 'Method not implemented'
  }

  getMode() {
    return this.getStringValue(this.wrapper.prop('mode'))
  }

  getError() {
    return this.getStringValue(this.wrapper.prop('error'))
  }

  change(value) {
    this.wrapper.prop('onChange')({
      target: {
        name: this.getName(),
        value
      }
    })
  }

  toJSON() {
    if (this.wrapper.exists()) {
      return {
        label: this.getLabel(),
        value: this.getValue(),
        enabled: this.getEnabled(),
        focused: this.getFocused(),
        mode: this.getMode(),
        error: this.getError()
      }
    } else {
      return null
    }
  }
}
