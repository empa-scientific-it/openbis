import React from 'react'
import PageButtons from '@src/js/components/common/page/PageButtons.jsx'
import Button from '@src/js/components/common/form/Button.jsx'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

class QueryFormButtons extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'QueryFormButtons.render')

    const { mode, onEdit, onSave, onCancel, changed, query } = this.props

    return (
      <PageButtons
        mode={mode}
        changed={changed}
        onEdit={onEdit}
        onSave={onSave}
        onCancel={query.original ? onCancel : null}
        renderAdditionalButtons={params => this.renderAdditionalButtons(params)}
      />
    )
  }

  renderAdditionalButtons({ classes }) {
    const { onExecute } = this.props

    return (
      <Button
        name='execute'
        label={messages.get(messages.EXECUTE)}
        styles={{ root: classes.button }}
        onClick={onExecute}
      />
    )
  }
}

export default QueryFormButtons
