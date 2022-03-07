import Button from '@src/js/components/common/form/Button.jsx'
import ButtonWrapper from '@srcTest/js/components/common/form/wrapper/ButtonWrapper.js'
import PageButtonsWrapper from '@srcTest/js/components/common/page/wrapper/PageButtonsWrapper.js'

export default class UserFormButtonsWrapper extends PageButtonsWrapper {
  getAddGroup() {
    return new ButtonWrapper(
      this.findComponent(Button).filter({ name: 'addGroup' })
    )
  }

  getAddRole() {
    return new ButtonWrapper(
      this.findComponent(Button).filter({ name: 'addRole' })
    )
  }

  getRemove() {
    return new ButtonWrapper(
      this.findComponent(Button).filter({ name: 'remove' })
    )
  }

  toJSON() {
    return {
      ...super.toJSON(),
      addGroup: this.getAddGroup().toJSON(),
      addRole: this.getAddRole().toJSON(),
      remove: this.getRemove().toJSON()
    }
  }
}
