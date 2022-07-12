import Button from '@src/js/components/common/form/Button.jsx'
import Header from '@src/js/components/common/form/Header.jsx'
import Message from '@src/js/components/common/form/Message.jsx'
import BaseWrapper from '@srcTest/js/components/common/wrapper/BaseWrapper.js'
import ButtonWrapper from '@srcTest/js/components/common/form/wrapper/ButtonWrapper.js'
import MessageWrapper from '@srcTest/js/components/common/form/wrapper/MessageWrapper.js'

export default class ActiveUserReportWrapper extends BaseWrapper {
  getTitle() {
    return this.findComponent(Header)
  }

  getMessage() {
    return new MessageWrapper(this.findComponent(Message))
  }

  getButton() {
    return new ButtonWrapper(this.findComponent(Button).filter({ name: 'sendReport' }))
  }

  toJSON() {
    if (this.wrapper.exists()) {
      return {
        title: this.getTitle().exists() ? this.getTitle().text() : null,
        button: this.getButton().toJSON(),
        message: this.getMessage().toJSON(),
      }
    } else {
      return null
    }
  }
}
