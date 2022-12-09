import FilterField from '@src/js/components/common/form/FilterField.jsx'
import BrowserNode from '@src/js/components/common/browser/BrowserNode.jsx'
import BrowserNode2 from '@src/js/components/common/browser2/BrowserNode.jsx'
import BrowserButtons from '@src/js/components/common/browser/BrowserButtons.jsx'
import BrowserButtons2 from '@src/js/components/common/browser2/BrowserButtonsAddRemove.jsx'
import BrowserNodeAutoShowSelected from '@src/js/components/common/browser2/BrowserNodeAutoShowSelected.jsx'
import BaseWrapper from '@srcTest/js/components/common/wrapper/BaseWrapper.js'
import FilterFieldWrapper from '@srcTest/js/components/common/form/wrapper/FilterFieldWrapper.js'
import BrowserNodeWrapper from './BrowserNodeWrapper.js'
import BrowserButtonsWrapper from './BrowserButtonsWrapper.js'

export default class BrowserWrapper extends BaseWrapper {
  getFilter() {
    return new FilterFieldWrapper(this.findComponent(FilterField))
  }

  clickAutoShowSelected() {
    const showSelectedWrapper = this.findComponent(BrowserNodeAutoShowSelected)
    showSelectedWrapper.instance().handleClick({
      stopPropagation: function () {},
      preventDefault: function () {}
    })
  }

  getNodes() {
    return this._getNodes(this.wrapper, [])
  }

  _getNodes(wrapper, nodes) {
    wrapper.children().forEach(childWrapper => {
      let node = null
      if (childWrapper.is(this.unwrapComponent(BrowserNode))) {
        node = new BrowserNodeWrapper(childWrapper)
      } else if (childWrapper.is(this.unwrapComponent(BrowserNode2))) {
        node = new BrowserNodeWrapper(childWrapper)
      }

      if (node) {
        if (node.getLevel() >= 0) {
          nodes.push(node)
        }
        if (node.getExpanded()) {
          this._getNodes(childWrapper, nodes)
        }
      } else {
        this._getNodes(childWrapper, nodes)
      }
    })
    return nodes
  }

  getButtons() {
    const buttons = this.findComponent(BrowserButtons)
    if (buttons.exists()) {
      return new BrowserButtonsWrapper(buttons)
    }
    const buttons2 = this.findComponent(BrowserButtons2)
    if (buttons2.exists()) {
      return new BrowserButtonsWrapper(buttons2)
    }
    return null
  }

  toJSON() {
    if (this.wrapper.exists()) {
      return {
        filter: this.getFilter().toJSON(),
        nodes: this.getNodes().map(node => node.toJSON()),
        buttons: this.getButtons().toJSON()
      }
    } else {
      return null
    }
  }
}
