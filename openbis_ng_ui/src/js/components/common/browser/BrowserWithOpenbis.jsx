import React from 'react'
import autoBind from 'auto-bind'
import ResizableWithOpenbis from '@src/js/components/common/resizable/ResizableWithOpenbis.jsx'
import Browser from '@src/js/components/common/browser/Browser.jsx'
import AppController from '@src/js/components/AppController.js'
import logger from '@src/js/common/logger.js'

class BrowserWithOpenbis extends React.PureComponent {
  constructor(props) {
    super(props)
    autoBind(this)

    if (!props.id) {
      throw new Error('Browser id cannot be empty!')
    }
  }

  render() {
    logger.log(logger.DEBUG, 'BrowserWithOpenbis.render')

    const { id } = this.props

    return (
      <ResizableWithOpenbis
        id={id + '_resizable'}
        direction={{
          right: true
        }}
      >
        <Browser
          {...this.props}
          loadSettings={this.loadSettings}
          onSettingsChange={this.onSettingsChange}
        />
      </ResizableWithOpenbis>
    )
  }

  async loadSettings() {
    return await AppController.getInstance().getSetting(this.props.id)
  }

  async onSettingsChange(settings) {
    await AppController.getInstance().setSetting(this.props.id, settings)
  }
}

export default BrowserWithOpenbis
