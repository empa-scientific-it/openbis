import React from 'react'
import Browser from '@src/js/components/common/browser2/Browser.jsx'
import DatabaseBrowserController from '@src/js/components/database/browser/DatabaseBrowserController.js'
import logger from '@src/js/common/logger.js'

class DatabaseBrowser extends React.Component {
  constructor(props) {
    super(props)
    this.controller = this.props.controller || new DatabaseBrowserController()
  }

  render() {
    logger.log(logger.DEBUG, 'DatabaseBrowser.render')
    return <Browser controller={this.controller} />
  }
}

export default DatabaseBrowser
