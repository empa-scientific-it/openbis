import Tab from '@material-ui/core/Tab'
import BaseWrapper from '@srcTest/js/components/common/wrapper/BaseWrapper.js'
import TabWrapper from '@srcTest/js/components/common/menu/wrapper/TabWrapper.js'

export default class MenuWrapper extends BaseWrapper {
  getTabs() {
    return this.findComponent(Tab).map(tabWrapper => new TabWrapper(tabWrapper))
  }

  toJSON() {
    if (this.wrapper.exists()) {
      return {
        tabs: this.getTabs().map(tab => tab.toJSON())
      }
    } else {
      return null
    }
  }
}
