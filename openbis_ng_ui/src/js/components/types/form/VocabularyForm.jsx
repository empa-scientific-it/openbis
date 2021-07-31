import _ from 'lodash'
import React from 'react'
import autoBind from 'auto-bind'
import { connect } from 'react-redux'
import { withStyles } from '@material-ui/core/styles'
import ComponentContext from '@src/js/components/common/ComponentContext.js'
import PageWithTwoPanels from '@src/js/components/common/page/PageWithTwoPanels.jsx'
import Grid from '@src/js/components/common/grid/Grid.jsx'
import GridContainer from '@src/js/components/common/grid/GridContainer.jsx'
import VocabularyFormSelectionType from '@src/js/components/types/form/VocabularyFormSelectionType.js'
import VocabularyFormController from '@src/js/components/types/form/VocabularyFormController.js'
import VocabularyFormFacade from '@src/js/components/types/form/VocabularyFormFacade.js'
import VocabularyFormParameters from '@src/js/components/types/form/VocabularyFormParameters.jsx'
import VocabularyFormButtons from '@src/js/components/types/form/VocabularyFormButtons.jsx'
import selectors from '@src/js/store/selectors/selectors.js'
import ids from '@src/js/common/consts/ids.js'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

const styles = () => ({})

const columns = [
  {
    name: 'code',
    label: messages.get(messages.CODE),
    sort: 'asc',
    getValue: ({ row }) => row.code.value
  },
  {
    name: 'label',
    label: messages.get(messages.LABEL),
    getValue: ({ row }) => row.label.value
  },
  {
    name: 'description',
    label: messages.get(messages.DESCRIPTION),
    getValue: ({ row }) => row.description.value
  },
  {
    name: 'official',
    label: messages.get(messages.OFFICIAL),
    getValue: ({ row }) => row.official.value
  }
]

function mapStateToProps(state) {
  return {
    session: selectors.getSession(state)
  }
}

class VocabularyForm extends React.PureComponent {
  constructor(props) {
    super(props)
    autoBind(this)

    this.state = {}

    if (this.props.controller) {
      this.controller = this.props.controller
    } else {
      this.controller = new VocabularyFormController(new VocabularyFormFacade())
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
      controller.handleSelectionChange(VocabularyFormSelectionType.TERM, {
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
    logger.log(logger.DEBUG, 'VocabularyForm.render')

    const { loading, loaded, vocabulary } = this.state

    return (
      <PageWithTwoPanels
        loading={loading}
        loaded={loaded}
        object={vocabulary}
        renderMainPanel={() => this.renderMainPanel()}
        renderAdditionalPanel={() => this.renderAdditionalPanel()}
        renderButtons={() => this.renderButtons()}
      />
    )
  }

  renderMainPanel() {
    const { terms, selection } = this.state

    return (
      <GridContainer onClick={this.handleClickContainer}>
        <Grid
          id={ids.VOCABULARY_TERMS_GRID_ID}
          controllerRef={this.handleGridControllerRef}
          header={messages.get(messages.TERMS)}
          columns={columns}
          rows={terms}
          selectedRowId={
            selection && selection.type === VocabularyFormSelectionType.TERM
              ? selection.params.id
              : null
          }
          onSelectedRowChange={this.handleSelectedRowChange}
        />
      </GridContainer>
    )
  }

  renderAdditionalPanel() {
    const { controller } = this
    const { vocabulary, terms, selection, mode } = this.state

    const selectedRow = controller.gridController
      ? controller.gridController.getSelectedRow()
      : null

    return (
      <VocabularyFormParameters
        controller={controller}
        vocabulary={vocabulary}
        terms={terms}
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
    const { vocabulary, terms, selection, changed, mode } = this.state

    return (
      <VocabularyFormButtons
        onEdit={controller.handleEdit}
        onSave={controller.handleSave}
        onCancel={controller.handleCancel}
        onAdd={controller.handleAdd}
        onRemove={controller.handleRemove}
        vocabulary={vocabulary}
        terms={terms}
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
)(VocabularyForm)
