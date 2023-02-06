import React from 'react'
import PageButtons from '@src/js/components/common/page/PageButtons.jsx'
import Button from '@src/js/components/common/form/Button.jsx'
import openbis from '@src/js/services/openbis.js'
import messages from '@src/js/common/messages.js'
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
        renderAdditionalButtons={params => this.renderAdditionalButtons(params)}
      />
    )
  }

  renderAdditionalButtons({ classes }) {
    const { onEvaluate } = this.props

    return (
      <Button
        name='evaluate'
        label={messages.get(messages.EVALUATE)}
        styles={{ root: classes.button }}
        onClick={onEvaluate}
      />
    )
  }
}

export default PluginFormButtons
