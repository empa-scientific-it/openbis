import _ from 'lodash'
import React from 'react'
import ConfirmationDialog from '@src/js/components/common/dialog/ConfirmationDialog.jsx'
import EntityTypeFormSelectionType from '@src/js/components/types/form/entitytype/EntityTypeFormSelectionType.js'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

class EntityTypeFormDialogRemoveSection extends React.Component {
  render() {
    logger.log(logger.DEBUG, 'EntityTypeFormDialogRemoveSection.render')

    const { open, object, onConfirm, onCancel } = this.props

    const section = this.getSection()

    if (section) {
      return (
        <ConfirmationDialog
          open={open}
          onConfirm={onConfirm}
          onCancel={onCancel}
          title={this.getTitle(section)}
          content={this.getContent(object, section)}
          type={section.usages > 0 ? 'warning' : 'info'}
        />
      )
    } else {
      return null
    }
  }

  getTitle(section) {
    if (section.name.value) {
      return messages.get(messages.CONFIRMATION_REMOVE, section.name.value)
    } else {
      return messages.get(messages.CONFIRMATION_REMOVE_IT)
    }
  }

  getContent(object, section) {
    if (section.usages > 0) {
      return messages.get(messages.SECTION_IS_USED, object.id, section.usages)
    } else {
      return messages.get(messages.SECTION_IS_NOT_USED, object.id)
    }
  }

  getSection() {
    const { selection, sections } = this.props

    if (selection && selection.type === EntityTypeFormSelectionType.SECTION) {
      return _.find(sections, ['id', selection.params.id])
    } else {
      return null
    }
  }
}

export default EntityTypeFormDialogRemoveSection
