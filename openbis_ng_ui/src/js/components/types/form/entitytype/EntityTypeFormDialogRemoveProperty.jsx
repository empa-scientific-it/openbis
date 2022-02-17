import _ from 'lodash'
import React from 'react'
import ConfirmationDialog from '@src/js/components/common/dialog/ConfirmationDialog.jsx'
import EntityTypeFormSelectionType from '@src/js/components/types/form/entitytype/EntityTypeFormSelectionType.js'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

class EntityTypeFormDialogRemoveProperty extends React.Component {
  render() {
    logger.log(logger.DEBUG, 'EntityTypeFormDialogRemoveProperty.render')

    const { open, object, onConfirm, onCancel } = this.props

    const property = this.getProperty()

    if (property) {
      return (
        <ConfirmationDialog
          open={open}
          onConfirm={onConfirm}
          onCancel={onCancel}
          title={this.getTitle(property)}
          content={this.getContent(object, property)}
          type={property.usages > 0 ? 'warning' : 'info'}
        />
      )
    } else {
      return null
    }
  }

  getTitle(property) {
    return messages.get(messages.CONFIRMATION_REMOVE, property.code.value)
  }

  getContent(object, property) {
    if (property.usages > 0) {
      return messages.get(messages.PROPERTY_IS_USED, object.id, property.usages)
    } else {
      return messages.get(messages.PROPERTY_IS_NOT_USED, object.id)
    }
  }

  getProperty() {
    const { selection, properties } = this.props

    if (selection && selection.type === EntityTypeFormSelectionType.PROPERTY) {
      return _.find(properties, ['id', selection.params.id])
    } else {
      return null
    }
  }
}

export default EntityTypeFormDialogRemoveProperty
