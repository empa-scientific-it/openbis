import BaseWrapper from '@srcTest/js/components/common/wrapper/BaseWrapper.js'

export default class GridColumnLabelWrapper extends BaseWrapper {
  getValue() {
    return this.wrapper.text()
  }

  click() {
    this.wrapper.instance().handleClick()
  }

  toJSON() {
    if (this.wrapper.exists()) {
      return this.getValue()
    } else {
      return null
    }
  }
}
