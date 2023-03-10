import _ from 'lodash'
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

    if (_.isNil(props.controller)) {
      throw new Error('Controller cannot be empty!')
    }

    if (_.isNil(props.controller.getId())) {
      throw new Error('Id cannot be empty!')
    }

    this.controller = Object.assign(props.controller, {
      loadSettings: this.loadSettings,
      onSettingsChange: this.onSettingsChange,
      onError: this.onError
    })
  }

  render() {
    logger.log(logger.DEBUG, 'BrowserWithOpenbis.render')

    const { controller } = this.props

    return (
      <ResizableWithOpenbis
        id={controller.getId() + '_resizable'}
        direction={{
          right: true
        }}
      >
        <Browser {...this.props} />
      </ResizableWithOpenbis>
    )
  }

  async loadSettings() {
    return await AppController.getInstance().getSetting(
      this.props.controller.getId()
    )
  }

  async onSettingsChange(settings) {
    await AppController.getInstance().setSetting(
      this.props.controller.getId(),
      settings
    )
  }

  async onError(error) {
    await AppController.getInstance().errorChange(error)
  }
}

export default BrowserWithOpenbis
