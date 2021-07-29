import _ from 'lodash'
import React from 'react'
import { connect } from 'react-redux'
import { withStyles } from '@material-ui/core/styles'
import ComponentContext from '@src/js/components/common/ComponentContext.js'
import PageWithTwoPanels from '@src/js/components/common/page/PageWithTwoPanels.jsx'
import TypeFormController from '@src/js/components/types/form/TypeFormController.js'
import TypeFormFacade from '@src/js/components/types/form/TypeFormFacade.js'
import TypeFormButtons from '@src/js/components/types/form/TypeFormButtons.jsx'
import TypeFormParameters from '@src/js/components/types/form/TypeFormParameters.jsx'
import TypeFormPreview from '@src/js/components/types/form/TypeFormPreview.jsx'
import TypeFormDialogRemoveSection from '@src/js/components/types/form/TypeFormDialogRemoveSection.jsx'
import TypeFormDialogRemoveProperty from '@src/js/components/types/form/TypeFormDialogRemoveProperty.jsx'
import selectors from '@src/js/store/selectors/selectors.js'
import logger from '@src/js/common/logger.js'

const styles = () => ({})

function mapStateToProps(state) {
  return {
    session: selectors.getSession(state)
  }
}

class TypeForm extends React.PureComponent {
  constructor(props) {
    super(props)

    this.state = {}

    if (this.props.controller) {
      this.controller = this.props.controller
    } else {
      this.controller = new TypeFormController(new TypeFormFacade())
    }

    this.controller.init(new ComponentContext(this))
  }

  componentDidMount() {
    this.controller.load()
  }

  render() {
    logger.log(logger.DEBUG, 'TypeForm.render')

    const { loading, loaded, type } = this.state

    return (
      <PageWithTwoPanels
        loading={loading}
        loaded={loaded}
        object={type}
        renderMainPanel={() => this.renderMainPanel()}
        renderAdditionalPanel={() => this.renderAdditionalPanel()}
        renderButtons={() => this.renderButtons()}
      />
    )
  }

  renderMainPanel() {
    const { controller } = this
    const { type, properties, sections, preview, selection, mode } = this.state

    return (
      <TypeFormPreview
        controller={controller}
        type={type}
        properties={properties}
        sections={sections}
        preview={preview}
        selection={selection}
        mode={mode}
        onChange={controller.handleChange}
        onOrderChange={controller.handleOrderChange}
        onSelectionChange={controller.handleSelectionChange}
      />
    )
  }

  renderAdditionalPanel() {
    let { controller } = this
    let { type, properties, sections, selection, mode } = this.state

    return (
      <TypeFormParameters
        controller={controller}
        type={type}
        properties={properties}
        sections={sections}
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
    const {
      properties,
      sections,
      selection,
      removePropertyDialogOpen,
      removeSectionDialogOpen,
      changed,
      mode
    } = this.state
    const { object } = this.props

    return (
      <React.Fragment>
        <TypeFormButtons
          onAddSection={controller.handleAddSection}
          onAddProperty={controller.handleAddProperty}
          onRemove={controller.handleRemove}
          onEdit={controller.handleEdit}
          onSave={controller.handleSave}
          onCancel={controller.handleCancel}
          object={object}
          selection={selection}
          sections={sections}
          properties={properties}
          changed={changed}
          mode={mode}
        />
        <TypeFormDialogRemoveSection
          open={removeSectionDialogOpen}
          object={object}
          selection={selection}
          sections={sections}
          onConfirm={controller.handleRemoveConfirm}
          onCancel={controller.handleRemoveCancel}
        />
        <TypeFormDialogRemoveProperty
          open={removePropertyDialogOpen}
          object={object}
          selection={selection}
          properties={properties}
          onConfirm={controller.handleRemoveConfirm}
          onCancel={controller.handleRemoveCancel}
        />
      </React.Fragment>
    )
  }
}

export default _.flow(connect(mapStateToProps), withStyles(styles))(TypeForm)
