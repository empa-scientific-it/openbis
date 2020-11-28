import React from 'react'
import PageButtons from '@src/js/components/common/page/PageButtons.jsx'
import openbis from '@src/js/services/openbis.js'
import logger from '@src/js/common/logger.js'

class PluginFormButtons extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'PluginFormButtons.render')

    const { mode, onEdit, onSave, onCancel, changed, plugin } = this.props

    return (
      <PageButtons
        mode={mode}
        changed={changed}
        onEdit={plugin.pluginKind === openbis.PluginKind.JYTHON ? onEdit : null}
        onSave={onSave}
        onCancel={plugin.id ? onCancel : null}
      />
    )
  }
}

export default PluginFormButtons
