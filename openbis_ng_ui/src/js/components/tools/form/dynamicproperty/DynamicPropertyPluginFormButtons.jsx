import React from 'react'
import PageButtons from '@src/js/components/common/page/PageButtons.jsx'
import logger from '@src/js/common/logger.js'

class DynamicPropertyPluginFormButtons extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'DynamicPropertyPluginFormButtons.render')

    const { mode, onEdit, onSave, onCancel, changed, plugin } = this.props

    return (
      <PageButtons
        mode={mode}
        changed={changed}
        onEdit={onEdit}
        onSave={onSave}
        onCancel={plugin.id ? onCancel : null}
      />
    )
  }
}

export default DynamicPropertyPluginFormButtons
