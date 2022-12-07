import _ from 'lodash'
import React from 'react'
import autoBind from 'auto-bind'
import BrowserWithSettings from '@src/js/components/common/browser2/BrowserWithSettings.jsx'
import BrowserButtons from '@src/js/components/common/browser2/BrowserButtons.jsx'
import TypeBrowserController from '@src/js/components/types/browser2/TypeBrowserController.js'
import AppController from '@src/js/components/AppController.js'
import pages from '@src/js/common/consts/pages.js'
import ids from '@src/js/common/consts/ids.js'
import logger from '@src/js/common/logger.js'

class TypeBrowser extends React.Component {
  constructor(props) {
    super(props)
    autoBind(this)
    this.controller = this.props.controller || new TypeBrowserController()
  }

  componentDidMount() {
    this.componentDidUpdate({})
  }

  componentDidUpdate(prevProps) {
    if (!_.isEqual(this.props.selectedObject, prevProps.selectedObject)) {
      this.controller.selectObject(this.props.selectedObject)
    }

    if (
      !_.isEqual(
        this.props.lastObjectModifications,
        prevProps.lastObjectModifications
      )
    ) {
      this.controller.reload(this.props.lastObjectModifications)
    }
  }

  render() {
    logger.log(logger.DEBUG, 'TypeBrowser.render')

    return (
      <BrowserWithSettings
        id={ids.TYPE_BROWSER_ID}
        controller={this.controller}
        renderFooter={this.renderFooter}
        onSelectedChange={selectedObject => {
          if (selectedObject) {
            AppController.getInstance().objectOpen(
              pages.TYPES,
              selectedObject.type,
              selectedObject.id
            )
          }
        }}
      />
    )
  }

  renderFooter() {
    return (
      <BrowserButtons
        addEnabled={this.controller.canAddNode()}
        removeEnabled={this.controller.canRemoveNode()}
        onAdd={this.controller.addNode}
        onRemove={this.controller.removeNode}
      />
    )
  }
}

export default AppController.getInstance().withState(() => ({
  selectedObject: AppController.getInstance().getSelectedObject(pages.TYPES),
  lastObjectModifications:
    AppController.getInstance().getLastObjectModifications()
}))(TypeBrowser)
