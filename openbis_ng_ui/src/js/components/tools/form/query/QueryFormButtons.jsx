import React from 'react'
import PageButtons from '@src/js/components/common/page/PageButtons.jsx'
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
      />
    )
  }
}

export default QueryFormButtons
