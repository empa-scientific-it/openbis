import BaseWrapper from '@srcTest/js/components/common/wrapper/BaseWrapper.js'
import GridWrapper from '@srcTest/js/components/common/grid/wrapper/GridWrapper.js'
import Grid from '@src/js/components/common/grid/Grid.jsx'
import MessageWrapper from '@srcTest/js/components/common/form/wrapper/MessageWrapper.js'
import Message from '@src/js/components/common/form/Message.jsx'
import ids from '@src/js/common/consts/ids.js'

export default class TypeSearchWrapper extends BaseWrapper {
  getMessages() {
    const messages = []
    this.findComponent(Message).forEach(message => {
      messages.push(new MessageWrapper(message))
    })
    return messages
  }

  getObjectTypes() {
    return new GridWrapper(
      this.findComponent(Grid).filter({
        id: ids.OBJECT_TYPES_GRID_ID
      })
    )
  }

  getCollectionTypes() {
    return new GridWrapper(
      this.findComponent(Grid).filter({
        id: ids.COLLECTION_TYPES_GRID_ID
      })
    )
  }

  getDataSetTypes() {
    return new GridWrapper(
      this.findComponent(Grid).filter({
        id: ids.DATA_SET_TYPES_GRID_ID
      })
    )
  }

  getMaterialTypes() {
    return new GridWrapper(
      this.findComponent(Grid).filter({
        id: ids.MATERIAL_TYPES_GRID_ID
      })
    )
  }

  getVocabularyTypes() {
    return new GridWrapper(
      this.findComponent(Grid).filter({
        id: ids.VOCABULARY_TYPES_GRID_ID
      })
    )
  }

  getPropertyTypes() {
    return new GridWrapper(
      this.findComponent(Grid).filter({
        id: ids.PROPERTY_TYPES_GRID_ID
      })
    )
  }

  toJSON() {
    return {
      messages: this.getMessages().map(message => message.toJSON()),
      objectTypes: this.getObjectTypes().toJSON(),
      collectionTypes: this.getCollectionTypes().toJSON(),
      dataSetTypes: this.getDataSetTypes().toJSON(),
      materialTypes: this.getMaterialTypes().toJSON(),
      vocabularyTypes: this.getVocabularyTypes().toJSON(),
      propertyTypes: this.getPropertyTypes().toJSON()
    }
  }
}
