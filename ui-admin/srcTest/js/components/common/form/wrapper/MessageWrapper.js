import BaseWrapper from '@srcTest/js/components/common/wrapper/BaseWrapper.js'

export default class MessageWrapper extends BaseWrapper {
  getText() {
    return this.getStringValue(this.wrapper.text())
  }

  getType() {
    return this.getStringValue(this.wrapper.prop('type'))
  }

  toJSON() {
    if (this.wrapper.exists()) {
      return {
        text: this.getText(),
        type: this.getType()
      }
    } else {
      return null
    }
  }
}
