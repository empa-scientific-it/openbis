import BaseWrapper from '@srcTest/js/components/common/wrapper/BaseWrapper.js'

export default class IconWrapper extends BaseWrapper {
  click() {
    this.wrapper.prop('onClick')({
      stopPropagation: function () {},
      preventDefault: function () {}
    })
  }

  toJSON() {
    if (this.wrapper.exists()) {
      return {}
    } else {
      return null
    }
  }
}
