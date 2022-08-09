import React from 'react'
import autoBind from 'auto-bind'
import ComponentContext from '@src/js/components/common/ComponentContext.js'
import PageWithTwoPanels from '@src/js/components/common/page/PageWithTwoPanels.jsx'
import GridWithSettings from '@src/js/components/common/grid/GridWithSettings.jsx'
import GridContainer from '@src/js/components/common/grid/GridContainer.jsx'
import PersonalAccessTokenFormController from '@src/js/components/tools/form/pat/PersonalAccessTokenFormController.js'
import PersonalAccessTokenFormFacade from '@src/js/components/tools/form/pat/PersonalAccessTokenFormFacade.js'
import PersonalAccessTokenFormParameters from '@src/js/components/tools/form/pat/PersonalAccessTokenFormParameters.jsx'
import PersonalAccessTokenFormButtons from '@src/js/components/tools/form/pat/PersonalAccessTokenFormButtons.jsx'
import ids from '@src/js/common/consts/ids.js'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

const columns = [
  {
    name: 'sessionName',
    label: messages.get(messages.SESSION_NAME),
    getValue: ({ row }) => row.sessionName.value
  }
]

class PersonalAccessTokenForm extends React.PureComponent {
  constructor(props) {
    super(props)
    autoBind(this)

    this.state = {}

    if (this.props.controller) {
      this.controller = this.props.controller
    } else {
      this.controller = new PersonalAccessTokenFormController(
        new PersonalAccessTokenFormFacade()
      )
    }

    this.controller.init(new ComponentContext(this))
  }

  componentDidMount() {
    this.controller.load()
  }

  handleClickContainer() {
    this.controller.handleSelectionChange()
  }

  handleSelectedRowChange(row) {
    const { controller } = this
    if (row) {
      controller.handleSelectionChange({
        id: row.id
      })
    } else {
      controller.handleSelectionChange()
    }
  }

  handleGridControllerRef(gridController) {
    this.controller.gridController = gridController
  }

  render() {
    logger.log(logger.DEBUG, 'PersonalAccessTokenForm.render')

    const { loadId, loading, loaded } = this.state

    return (
      <PageWithTwoPanels
        key={loadId}
        loading={loading}
        loaded={loaded}
        object={{}}
        renderMainPanel={() => this.renderMainPanel()}
        renderAdditionalPanel={() => this.renderAdditionalPanel()}
        renderButtons={() => this.renderButtons()}
      />
    )
  }

  renderMainPanel() {
    const { pats, selection } = this.state

    return (
      <GridContainer onClick={this.handleClickContainer}>
        <GridWithSettings
          id={ids.PERSONAL_ACCESS_TOKEN_GRID_ID}
          controllerRef={this.handleGridControllerRef}
          header={messages.get(messages.PERSONAL_ACCESS_TOKENS)}
          columns={columns}
          rows={pats}
          sort='sessionName'
          selectable={true}
          selectedRowId={selection ? selection.params.id : null}
          onSelectedRowChange={this.handleSelectedRowChange}
        />
      </GridContainer>
    )
  }

  renderAdditionalPanel() {
    const { controller } = this
    const { pats, selection, selectedRow, mode } = this.state

    return (
      <PersonalAccessTokenFormParameters
        controller={controller}
        pats={pats}
        selection={selection}
        selectedRow={selectedRow}
        mode={mode}
        onChange={controller.handleChange}
        onSelectionChange={controller.handleSelectionChange}
        onBlur={controller.handleBlur}
      />
    )
  }

  renderButtons() {
    const { controller } = this
    const { pats, selection, changed, mode } = this.state

    return (
      <PersonalAccessTokenFormButtons
        pats={pats}
        selection={selection}
        changed={changed}
        mode={mode}
        onEdit={controller.handleEdit}
        onSave={controller.handleSave}
        onCancel={controller.handleCancel}
        onAdd={controller.handleAdd}
        onRemove={controller.handleRemove}
      />
    )
  }
}

export default PersonalAccessTokenForm
