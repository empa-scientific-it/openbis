import _ from 'lodash'
import autoBind from 'auto-bind'
import React from 'react'
import Resizable from '@src/js/components/common/resizable/Resizable.jsx'
import AppController from '@src/js/components/AppController.js'
import logger from '@src/js/common/logger.js'

class ResizableWithOpenbis extends React.PureComponent {
  constructor(props) {
    super(props)
    autoBind(this)
  }

  async loadSettings() {
    const { id } = this.props

    if (id) {
      return await AppController.getInstance().getSetting(id)
    } else {
      return null
    }
  }

  onSettingsChange(settings) {
    const { id } = this.props

    if (id) {
      AppController.getInstance().setSetting(id, settings)
    }
  }

  render() {
    logger.log(logger.DEBUG, 'ResizableWithOpenbis.render')

    const { direction, width, children } = this.props

    return (
      <Resizable
        direction={direction}
        width={width}
        loadSettings={this.loadSettings}
        onSettingsChange={this.onSettingsChange}
      >
        {children}
      </Resizable>
    )
  }
}

export default ResizableWithOpenbis
