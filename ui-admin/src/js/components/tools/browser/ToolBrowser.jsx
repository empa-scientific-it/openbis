import _ from 'lodash'
import React from 'react'
import autoBind from 'auto-bind'
import BrowserWithOpenbis from '@src/js/components/common/browser/BrowserWithOpenbis.jsx'
import BrowserButtonsAddRemove from '@src/js/components/common/browser/BrowserButtonsAddRemove.jsx'
import ToolBrowserController from '@src/js/components/tools/browser/ToolBrowserController.js'
import AppController from '@src/js/components/AppController.js'
import pages from '@src/js/common/consts/pages.js'
import logger from '@src/js/common/logger.js'

export class ToolBrowser extends React.Component {
  constructor(props) {
    super(props)
    autoBind(this)
    this.controller = this.props.controller || new ToolBrowserController()
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
    logger.log(logger.DEBUG, 'ToolBrowser.render')

    return (
      <BrowserWithOpenbis
        controller={this.controller}
        renderFooter={this.renderFooter}
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
  selectedObject: AppController.getInstance().getSelectedObject(pages.TOOLS),
  lastObjectModifications:
    AppController.getInstance().getLastObjectModifications()
}))(ToolBrowser)
