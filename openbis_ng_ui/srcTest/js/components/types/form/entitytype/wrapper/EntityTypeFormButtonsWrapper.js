import Button from '@src/js/components/common/form/Button.jsx'
import ButtonWrapper from '@srcTest/js/components/common/form/wrapper/ButtonWrapper.js'
import PageButtonsWrapper from '@srcTest/js/components/common/page/wrapper/PageButtonsWrapper.js'

export default class EntityTypeFormButtonsWrapper extends PageButtonsWrapper {
  getAddSection() {
    return new ButtonWrapper(
      this.findComponent(Button).filter({ name: 'addSection' })
    )
  }

  getAddProperty() {
    return new ButtonWrapper(
      this.findComponent(Button).filter({ name: 'addProperty' })
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
      addSection: this.getAddSection().toJSON(),
      addProperty: this.getAddProperty().toJSON(),
      remove: this.getRemove().toJSON()
    }
  }
}
