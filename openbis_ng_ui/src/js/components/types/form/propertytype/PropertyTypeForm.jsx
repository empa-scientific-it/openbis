import _ from 'lodash'
import React from 'react'
import autoBind from 'auto-bind'
import { connect } from 'react-redux'
import { withStyles } from '@material-ui/core/styles'
import ComponentContext from '@src/js/components/common/ComponentContext.js'
import PageWithTwoPanels from '@src/js/components/common/page/PageWithTwoPanels.jsx'
import PropertyTypeFormController from '@src/js/components/types/form/propertytype/PropertyTypeFormController.js'
import PropertyTypeFormFacade from '@src/js/components/types/form/propertytype/PropertyTypeFormFacade.js'
import PropertyTypeFormParameters from '@src/js/components/types/form/propertytype/PropertyTypeFormParameters.jsx'
import PropertyTypeFormButtons from '@src/js/components/types/form/propertytype/PropertyTypeFormButtons.jsx'
import selectors from '@src/js/store/selectors/selectors.js'
import logger from '@src/js/common/logger.js'

const styles = () => ({})

function mapStateToProps(state) {
  return {
    session: selectors.getSession(state)
  }
}

class PropertyTypeForm extends React.PureComponent {
  constructor(props) {
    super(props)
    autoBind(this)

    this.state = {}

    if (this.props.controller) {
      this.controller = this.props.controller
    } else {
      this.controller = new PropertyTypeFormController(
        new PropertyTypeFormFacade()
      )
    }

    this.controller.init(new ComponentContext(this))
  }

  componentDidMount() {
    this.controller.load()
  }

  render() {
    logger.log(logger.DEBUG, 'PropertyTypeForm.render')

    const { loadId, loading, loaded, propertyType } = this.state

    return (
      <PageWithTwoPanels
        key={loadId}
        loading={loading}
        loaded={loaded}
        object={propertyType}
        renderAdditionalPanel={() => this.renderAdditionalPanel()}
        renderButtons={() => this.renderButtons()}
      />
    )
  }

  renderAdditionalPanel() {
    const { controller } = this
    const { propertyType, selection, mode } = this.state

    return (
      <PropertyTypeFormParameters
        controller={controller}
        propertyType={propertyType}
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
    const { propertyType, selection, changed, mode } = this.state

    return (
      <PropertyTypeFormButtons
        onEdit={controller.handleEdit}
        onSave={controller.handleSave}
        onCancel={controller.handleCancel}
        propertyType={propertyType}
        selection={selection}
        changed={changed}
        mode={mode}
      />
    )
  }
}

export default _.flow(
  connect(mapStateToProps),
  withStyles(styles)
)(PropertyTypeForm)
