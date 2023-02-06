import BaseWrapper from '@srcTest/js/components/common/wrapper/BaseWrapper.js'

export default class ButtonWrapper extends BaseWrapper {
  getLabel() {
    return this.getStringValue(this.wrapper.text())
  }

  getEnabled() {
    return !this.getBooleanValue(this.wrapper.prop('disabled'))
  }

  click() {
    this.wrapper.prop('onClick')()
  }

  toJSON() {
    if (this.wrapper.exists()) {
      return {
        label: this.getLabel(),
        enabled: this.getEnabled()
      }
    } else {
      return null
    }
  }
}
