import Button from '@src/js/components/common/form/Button.jsx'
import BaseWrapper from '@srcTest/js/components/common/wrapper/BaseWrapper.js'
import ButtonWrapper from '@srcTest/js/components/common/form/wrapper/ButtonWrapper.js'

export default class BrowserWrapper extends BaseWrapper {
  getAdd() {
    return new ButtonWrapper(this.findComponent(Button).filter({ name: 'add' }))
  }

  getRemove() {
    return new ButtonWrapper(
      this.findComponent(Button).filter({ name: 'remove' })
    )
  }

  toJSON() {
    return {
      add: this.getAdd().toJSON(),
      remove: this.getRemove().toJSON()
    }
  }
}
