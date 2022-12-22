import React from 'react'
import ComponentContext from '@src/js/components/common/ComponentContext.js'
import PageWithTwoPanels from '@src/js/components/common/page/PageWithTwoPanels.jsx'
import EntityTypeFormController from '@src/js/components/types/form/entitytype/EntityTypeFormController.js'
import EntityTypeFormFacade from '@src/js/components/types/form/entitytype/EntityTypeFormFacade.js'
import EntityTypeFormButtons from '@src/js/components/types/form/entitytype/EntityTypeFormButtons.jsx'
import EntityTypeFormParameters from '@src/js/components/types/form/entitytype/EntityTypeFormParameters.jsx'
import EntityTypeFormPreview from '@src/js/components/types/form/entitytype/EntityTypeFormPreview.jsx'
import EntityTypeFormDialogRemoveSection from '@src/js/components/types/form/entitytype/EntityTypeFormDialogRemoveSection.jsx'
import EntityTypeFormDialogRemoveProperty from '@src/js/components/types/form/entitytype/EntityTypeFormDialogRemoveProperty.jsx'
import ids from '@src/js/common/consts/ids.js'
import logger from '@src/js/common/logger.js'

class EntityTypeForm extends React.PureComponent {
  constructor(props) {
    super(props)

    this.state = {}

    if (this.props.controller) {
      this.controller = this.props.controller
    } else {
      this.controller = new EntityTypeFormController(new EntityTypeFormFacade())
    }

    this.controller.init(new ComponentContext(this))
  }

  componentDidMount() {
    this.controller.load()
  }

  render() {
    logger.log(logger.DEBUG, 'EntityTypeForm.render')

    const { loading, loaded, type } = this.state

    return (
      <PageWithTwoPanels
        id={ids.ENTITY_TYPE_FORM_ID}
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
      <EntityTypeFormPreview
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
      <EntityTypeFormParameters
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
        <EntityTypeFormButtons
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
        <EntityTypeFormDialogRemoveSection
          open={removeSectionDialogOpen}
          object={object}
          selection={selection}
          sections={sections}
          onConfirm={controller.handleRemoveConfirm}
          onCancel={controller.handleRemoveCancel}
        />
        <EntityTypeFormDialogRemoveProperty
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

export default EntityTypeForm
