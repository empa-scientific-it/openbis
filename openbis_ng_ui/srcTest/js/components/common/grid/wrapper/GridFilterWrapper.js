import BaseWrapper from '@srcTest/js/components/common/wrapper/BaseWrapper.js'

export default class GridColumnFilterWrapper extends BaseWrapper {
  getValue() {
    return this.getStringValue(this.wrapper.prop('filter'))
  }

  change(value) {
    this.wrapper.instance().handleFilterChange({
      target: {
        value
      }
    })
  }

  toJSON() {
    if (this.wrapper.exists()) {
      return {
        value: this.getValue()
      }
    } else {
      return null
    }
  }
}
