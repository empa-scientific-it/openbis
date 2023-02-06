import React from 'react'
import ConfirmationDialog from '@src/js/components/common/dialog/ConfirmationDialog.jsx'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

export default class UnsavedChangesDialog extends React.Component {
  render() {
    logger.log(logger.DEBUG, 'UnsavedChangesDialog.render')

    const { open, onConfirm, onCancel } = this.props

    return (
      <ConfirmationDialog
        open={open}
        onConfirm={onConfirm}
        onCancel={onCancel}
        title={messages.get(messages.UNSAVED_CHANGES)}
        content={messages.get(messages.CONFIRMATION_UNSAVED_CHANGES)}
      />
    )
  }
}
