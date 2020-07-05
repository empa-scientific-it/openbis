import ButtonWrapper from '@srcTest/js/common/wrapper/ButtonWrapper.js'
import MessageWrapper from '@srcTest/js/common/wrapper/MessageWrapper.js'

export default class TypeFormButtonsWrapper {
  constructor(wrapper) {
    this.wrapper = wrapper
  }

  getEdit() {
    return new ButtonWrapper(this.wrapper.find('button[name="edit"]'))
  }

  getAddSection() {
    return new ButtonWrapper(this.wrapper.find('button[name="addSection"]'))
  }

  getAddProperty() {
    return new ButtonWrapper(this.wrapper.find('button[name="addProperty"]'))
  }

  getRemove() {
    return new ButtonWrapper(this.wrapper.find('button[name="remove"]'))
  }

  getSave() {
    return new ButtonWrapper(this.wrapper.find('button[name="save"]'))
  }

  getCancel() {
    return new ButtonWrapper(this.wrapper.find('button[name="cancel"]'))
  }

  getMessage() {
    return new MessageWrapper(this.wrapper.find('Message'))
  }

  toJSON() {
    return {
      edit: this.getEdit().toJSON(),
      addSection: this.getAddSection().toJSON(),
      addProperty: this.getAddProperty().toJSON(),
      remove: this.getRemove().toJSON(),
      save: this.getSave().toJSON(),
      cancel: this.getCancel().toJSON(),
      message: this.getMessage().toJSON()
    }
  }
}
