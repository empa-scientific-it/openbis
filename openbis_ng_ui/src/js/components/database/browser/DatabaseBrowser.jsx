import _ from 'lodash'
import React from 'react'
import BrowserWithOpenbis from '@src/js/components/common/browser2/BrowserWithOpenbis.jsx'
import DatabaseBrowserController from '@src/js/components/database/browser/DatabaseBrowserController.js'
import AppController from '@src/js/components/AppController.js'
import pages from '@src/js/common/consts/pages.js'
import ids from '@src/js/common/consts/ids.js'
import logger from '@src/js/common/logger.js'

class DatabaseBrowser extends React.Component {
  constructor(props) {
    super(props)
    this.controller = this.props.controller || new DatabaseBrowserController()
  }

  componentDidMount() {
    this.componentDidUpdate(null)
  }

  componentDidUpdate(prevProps) {
    const prevSelectedObject = prevProps ? prevProps.selectedObject : null
    const selectedObject = this.props.selectedObject

    if (!_.isEqual(prevSelectedObject, selectedObject)) {
      this.controller.selectObject(this.props.selectedObject)
    }
  }

  render() {
    logger.log(logger.DEBUG, 'DatabaseBrowser.render')
    return (
      <BrowserWithOpenbis
        id={ids.DATABASE_BROWSER_ID}
        controller={this.controller}
        onSelectedChange={selectedObject => {
          if (selectedObject) {
            AppController.getInstance().objectOpen(
              pages.DATABASE,
              selectedObject.type,
              selectedObject.id
            )
          }
        }}
      />
    )
  }
}

export default AppController.getInstance().withState(() => ({
  selectedObject: AppController.getInstance().getSelectedObject(pages.DATABASE)
}))(DatabaseBrowser)
