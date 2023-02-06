import BaseWrapper from '@srcTest/js/components/common/wrapper/BaseWrapper.js'
import Header from '@src/js/components/common/form/Header.jsx'
import Message from '@src/js/components/common/form/Message.jsx'
import MessageWrapper from '@srcTest/js/components/common/form/wrapper/MessageWrapper.js'

export default class PageParametersPanelWrapper extends BaseWrapper {
  getTitle() {
    return this.findComponent(Header)
  }

  getMessages() {
    const messages = []
    this.findComponent(Message).forEach(message => {
      messages.push(new MessageWrapper(message))
    })
    return messages
  }

  toJSON() {
    return {
      title: this.getTitle().exists() ? this.getTitle().text() : null,
      messages: this.getMessages().map(message => message.toJSON())
    }
  }
}
