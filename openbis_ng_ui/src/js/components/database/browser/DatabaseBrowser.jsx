import React from 'react'
import BrowserWithSettings from '@src/js/components/common/browser2/BrowserWithSettings.jsx'
import DatabaseBrowserController from '@src/js/components/database/browser/DatabaseBrowserController.js'
import ids from '@src/js/common/consts/ids.js'
import logger from '@src/js/common/logger.js'

class DatabaseBrowser extends React.Component {
  constructor(props) {
    super(props)
    this.controller = this.props.controller || new DatabaseBrowserController()
  }

  render() {
    logger.log(logger.DEBUG, 'DatabaseBrowser.render')
    return (
      <BrowserWithSettings
        id={ids.DATABASE_BROWSER_ID}
        controller={this.controller}
      />
    )
  }
}

export default DatabaseBrowser
