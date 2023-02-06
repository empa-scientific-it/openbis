import BaseWrapper from '@srcTest/js/components/common/wrapper/BaseWrapper.js'
import GridWrapper from '@srcTest/js/components/common/grid/wrapper/GridWrapper.js'
import Grid from '@src/js/components/common/grid/Grid.jsx'
import MessageWrapper from '@srcTest/js/components/common/form/wrapper/MessageWrapper.js'
import Message from '@src/js/components/common/form/Message.jsx'
import ids from '@src/js/common/consts/ids.js'

export default class ToolSearchWrapper extends BaseWrapper {
  getMessages() {
    const messages = []
    this.findComponent(Message).forEach(message => {
      messages.push(new MessageWrapper(message))
    })
    return messages
  }

  getDynamicPropertyPlugins() {
    return new GridWrapper(
      this.findComponent(Grid).filter({
        id: ids.DYNAMIC_PROPERTY_PLUGINS_GRID_ID
      })
    )
  }

  getEntityValidationPlugins() {
    return new GridWrapper(
      this.findComponent(Grid).filter({
        id: ids.ENTITY_VALIDATION_PLUGINS_GRID_ID
      })
    )
  }

  getQueries() {
    return new GridWrapper(
      this.findComponent(Grid).filter({
        id: ids.QUERIES_GRID_ID
      })
    )
  }

  toJSON() {
    return {
      messages: this.getMessages().map(message => message.toJSON()),
      dynamicPropertyPlugins: this.getDynamicPropertyPlugins().toJSON(),
      entityValidationPlugins: this.getEntityValidationPlugins().toJSON(),
      queries: this.getQueries().toJSON()
    }
  }
}
