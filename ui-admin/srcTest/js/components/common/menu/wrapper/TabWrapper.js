import BaseWrapper from '@srcTest/js/components/common/wrapper/BaseWrapper.js'
import IconWrapper from '@srcTest/js/components/common/form/wrapper/IconWrapper.js'

export default class TabWrapper extends BaseWrapper {
  getLabel() {
    return this.getStringValue(this.wrapper.text())
  }

  getSelected() {
    return this.getBooleanValue(this.wrapper.prop('selected'))
  }

  getCloseIcon() {
    return new IconWrapper(this.wrapper.find('svg').first())
  }

  toJSON() {
    if (this.wrapper.exists()) {
      return {
        label: this.getLabel(),
        selected: this.getSelected()
      }
    } else {
      return null
    }
  }
}
