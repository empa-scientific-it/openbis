import BaseWrapper from '@srcTest/js/components/common/wrapper/BaseWrapper.js'
import Message from '@src/js/components/common/form/Message.jsx'
import MessageWrapper from '@srcTest/js/components/common/form/wrapper/MessageWrapper.js'

export default class EntityTypeFormPreviewPropertyWrapper extends BaseWrapper {
  getCode() {
    const code = this.wrapper.find({ 'data-part': 'code' })
    if (code.exists()) {
      return this.getStringValue(code.text())
    } else {
      return null
    }
  }

  getLabel() {
    const label = this.wrapper.find({ 'data-part': 'label' })
    if (label.exists()) {
      return this.getStringValue(label.text())
    } else {
      return null
    }
  }

  getDataType() {
    const dataType = this.wrapper.find({ 'data-part': 'dataType' })
    if (dataType.exists()) {
      return this.getStringValue(dataType.text())
    } else {
      return null
    }
  }

  getMessage() {
    return new MessageWrapper(this.findComponent(Message))
  }

  click() {
    this.wrapper.instance().handleDraggableClick({
      stopPropagation: () => {}
    })
  }

  toJSON() {
    return {
      code: this.getCode(),
      label: this.getLabel(),
      dataType: this.getDataType(),
      message: this.getMessage().toJSON()
    }
  }
}
