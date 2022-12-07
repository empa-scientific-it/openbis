import _ from 'lodash'
import React from 'react'
import autoBind from 'auto-bind'
import BrowserWithOpenbis from '@src/js/components/common/browser2/BrowserWithOpenbis.jsx'
import BrowserButtonsAddRemove from '@src/js/components/common/browser2/BrowserButtonsAddRemove.jsx'
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
      <BrowserWithOpenbis
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
      <div>
        <BrowserButtonsAddRemove
          selectedObject={this.controller.getSelectedObject()}
          addEnabled={this.controller.canAddNode()}
          removeEnabled={this.controller.canRemoveNode()}
          onAdd={this.controller.addNode}
          onRemove={this.controller.removeNode}
        />
      </div>
    )
  }
}

export default AppController.getInstance().withState(() => ({
  selectedObject: AppController.getInstance().getSelectedObject(pages.TYPES),
  lastObjectModifications:
    AppController.getInstance().getLastObjectModifications()
}))(TypeBrowser)
