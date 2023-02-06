import BaseWrapper from '@srcTest/js/components/common/wrapper/BaseWrapper.js'
import IconWrapper from '@srcTest/js/components/common/form/wrapper/IconWrapper.js'

export default class BrowserNodeWrapper extends BaseWrapper {
  getId() {
    return this.getStringValue(this.wrapper.prop('node').id)
  }

  getLevel() {
    return this.wrapper.prop('level')
  }

  getText() {
    return this.getStringValue(this.wrapper.prop('node').text)
  }

  getExpanded() {
    return this.getBooleanValue(this.wrapper.prop('node').expanded)
  }

  getSelected() {
    return this.getBooleanValue(this.wrapper.prop('node').selected)
  }

  getIcon() {
    return new IconWrapper(this.wrapper.find('svg').first())
  }

  click() {
    this.wrapper.instance().handleClick()
  }

  toJSON() {
    return {
      id: this.getId(),
      level: this.getLevel(),
      text: this.getText(),
      expanded: this.getExpanded(),
      selected: this.getSelected()
    }
  }
}
