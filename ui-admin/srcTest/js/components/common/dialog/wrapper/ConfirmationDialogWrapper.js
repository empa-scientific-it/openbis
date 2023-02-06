import BaseWrapper from '@srcTest/js/components/common/wrapper/BaseWrapper.js'
import Button from '@src/js/components/common/form/Button.jsx'
import ButtonWrapper from '@srcTest/js/components/common/form/wrapper/ButtonWrapper.js'

export default class ConfirmationDialogWrapper extends BaseWrapper {
  getOpen() {
    return this.getBooleanValue(this.wrapper.prop('open'))
  }

  getTitle() {
    return this.getStringValue(this.wrapper.prop('title'))
  }

  getContent() {
    return this.getStringValue(this.wrapper.prop('content'))
  }

  getType() {
    return this.getStringValue(this.wrapper.prop('type'))
  }

  getConfirm() {
    return new ButtonWrapper(
      this.findComponent(Button).filter({ name: 'confirm' })
    )
  }

  getCancel() {
    return new ButtonWrapper(
      this.findComponent(Button).filter({ name: 'cancel' })
    )
  }

  toJSON() {
    if (this.wrapper.exists() && this.getOpen()) {
      return {
        title: this.getTitle(),
        content: this.getContent(),
        type: this.getType(),
        confirm: this.getConfirm().toJSON(),
        cancel: this.getCancel().toJSON()
      }
    } else {
      return null
    }
  }
}
