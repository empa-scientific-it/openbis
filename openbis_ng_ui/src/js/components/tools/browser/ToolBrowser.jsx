import React from 'react'
import Browser from '@src/js/components/common/browser/Browser.jsx'
import ToolBrowserController from '@src/js/components/tools/browser/ToolBrowserController.js'
import logger from '@src/js/common/logger.js'

class ToolBrowser extends React.Component {
  constructor(props) {
    super(props)
    this.controller = this.props.controller || new ToolBrowserController()
  }

  render() {
    logger.log(logger.DEBUG, 'ToolBrowser.render')
    return <Browser controller={this.controller} />
  }
}

export default ToolBrowser
