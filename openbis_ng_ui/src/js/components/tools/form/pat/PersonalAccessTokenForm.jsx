import React from 'react'
import autoBind from 'auto-bind'
import ComponentContext from '@src/js/components/common/ComponentContext.js'
import PageWithTwoPanels from '@src/js/components/common/page/PageWithTwoPanels.jsx'
import GridWithSettings from '@src/js/components/common/grid/GridWithSettings.jsx'
import GridContainer from '@src/js/components/common/grid/GridContainer.jsx'
import DateRangeField from '@src/js/components/common/form/DateRangeField.jsx'
import UserLink from '@src/js/components/common/link/UserLink.jsx'
import PersonalAccessTokenFormController from '@src/js/components/tools/form/pat/PersonalAccessTokenFormController.js'
import PersonalAccessTokenFormFacade from '@src/js/components/tools/form/pat/PersonalAccessTokenFormFacade.js'
import PersonalAccessTokenFormParameters from '@src/js/components/tools/form/pat/PersonalAccessTokenFormParameters.jsx'
import PersonalAccessTokenFormButtons from '@src/js/components/tools/form/pat/PersonalAccessTokenFormButtons.jsx'
import ids from '@src/js/common/consts/ids.js'
import messages from '@src/js/common/messages.js'
import date from '@src/js/common/date.js'
import logger from '@src/js/common/logger.js'

const columns = [
  {
    name: 'owner',
    label: messages.get(messages.OWNER),
    getValue: ({ row }) => row.owner.value,
    renderValue: ({ value }) => {
      return <UserLink userId={value} />
    }
  },
  {
    name: 'sessionName',
    label: messages.get(messages.SESSION_NAME),
    getValue: ({ row }) => row.sessionName.value
  },
  {
    name: 'hash',
    label: messages.get(messages.HASH),
    getValue: ({ row }) => row.hash.value
  },
  dateColumn('validFromDate', messages.get(messages.VALID_FROM)),
  dateColumn('validToDate', messages.get(messages.VALID_TO)),
  {
    name: 'registrator',
    label: messages.get(messages.REGISTRATOR),
    getValue: ({ row }) => row.registrator.value,
    renderValue: ({ value }) => {
      return <UserLink userId={value} />
    }
  },
  dateColumn('registrationDate', messages.get(messages.REGISTRATION_DATE)),
  dateColumn('accessDate', messages.get(messages.ACCESS_DATE))
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
          sortings={[
            { columnName: 'owner' },
            { columnName: 'sessionName' },
            { columnName: 'validFromDate' }
          ]}
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

function dateColumn(name, message) {
  return {
    name: name,
    label: message,
    getValue: ({ row }) => {
      return date.format(row[name].value)
    },
    renderFilter: ({ value, onChange }) => {
      return <DateRangeField value={value} onChange={onChange} />
    },
    matchesValue: ({ row, filter }) => {
      return date.inRange(
        row[name].value,
        filter.from ? filter.from.value : null,
        filter.to ? filter.to.value : null
      )
    }
  }
}

export default PersonalAccessTokenForm
