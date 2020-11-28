import _ from 'lodash'
import React from 'react'
import autoBind from 'auto-bind'
import { connect } from 'react-redux'
import { withStyles } from '@material-ui/core/styles'
import ComponentContext from '@src/js/components/common/ComponentContext.js'
import PageWithTwoPanels from '@src/js/components/common/page/PageWithTwoPanels.jsx'
import PluginFormController from '@src/js/components/tools/form/plugin/PluginFormController.js'
import PluginFormFacade from '@src/js/components/tools/form/plugin/PluginFormFacade.js'
import PluginFormScript from '@src/js/components/tools/form/plugin/PluginFormScript.jsx'
import PluginFormParameters from '@src/js/components/tools/form/plugin/PluginFormParameters.jsx'
import PluginFormButtons from '@src/js/components/tools/form/plugin/PluginFormButtons.jsx'
import openbis from '@src/js/services/openbis.js'
import logger from '@src/js/common/logger.js'

const styles = () => ({})

class PluginForm extends React.PureComponent {
  constructor(props) {
    super(props)
    autoBind(this)

    this.state = {}

    if (this.props.controller) {
      this.controller = this.props.controller
    } else {
      this.controller = new PluginFormController(new PluginFormFacade())
    }

    this.controller.init(new ComponentContext(this))
  }

  componentDidMount() {
    this.controller.load()
  }

  render() {
    logger.log(logger.DEBUG, 'PluginForm.render')

    const { loading, loaded, plugin } = this.state

    return (
      <PageWithTwoPanels
        loading={loading}
        loaded={loaded}
        object={plugin}
        renderMainPanel={() => this.renderMainPanel()}
        renderAdditionalPanel={() => this.renderAdditionalPanel()}
        renderButtons={() => this.renderButtons()}
      />
    )
  }

  renderMainPanel() {
    const { controller } = this
    const { plugin, selection, mode } = this.state

    if (plugin.pluginKind === openbis.PluginKind.JYTHON) {
      return (
        <PluginFormScript
          plugin={plugin}
          selection={selection}
          mode={mode}
          onChange={controller.handleChange}
          onSelectionChange={controller.handleSelectionChange}
          onBlur={controller.handleBlur}
        />
      )
    } else {
      return <div></div>
    }
  }

  renderAdditionalPanel() {
    const { controller } = this
    const { plugin, selection, mode } = this.state

    return (
      <PluginFormParameters
        plugin={plugin}
        selection={selection}
        mode={mode}
        onChange={controller.handleChange}
        onSelectionChange={controller.handleSelectionChange}
        onBlur={controller.handleBlur}
      />
    )
  }

  renderButtons() {
    const { controller } = this
    const { plugin, changed, mode } = this.state

    return (
      <PluginFormButtons
        onEdit={controller.handleEdit}
        onSave={controller.handleSave}
        onCancel={controller.handleCancel}
        plugin={plugin}
        changed={changed}
        mode={mode}
      />
    )
  }
}

export default _.flow(connect(), withStyles(styles))(PluginForm)
