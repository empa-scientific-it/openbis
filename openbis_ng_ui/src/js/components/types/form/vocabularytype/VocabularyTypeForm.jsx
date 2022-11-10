import React from 'react'
import autoBind from 'auto-bind'
import ComponentContext from '@src/js/components/common/ComponentContext.js'
import PageWithTwoPanels from '@src/js/components/common/page/PageWithTwoPanels.jsx'
import GridWithOpenbis from '@src/js/components/common/grid/GridWithOpenbis.jsx'
import GridExportOptions from '@src/js/components/common/grid/GridExportOptions.js'
import GridContainer from '@src/js/components/common/grid/GridContainer.jsx'
import VocabularyTypeFormSelectionType from '@src/js/components/types/form/vocabularytype/VocabularyTypeFormSelectionType.js'
import VocabularyTypeFormController from '@src/js/components/types/form/vocabularytype/VocabularyTypeFormController.js'
import VocabularyTypeFormFacade from '@src/js/components/types/form/vocabularytype/VocabularyTypeFormFacade.js'
import VocabularyTypeFormParameters from '@src/js/components/types/form/vocabularytype/VocabularyTypeFormParameters.jsx'
import VocabularyTypeFormButtons from '@src/js/components/types/form/vocabularytype/VocabularyTypeFormButtons.jsx'
import ids from '@src/js/common/consts/ids.js'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

const columns = [
  {
    name: 'code',
    label: messages.get(messages.CODE),
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

class VocabularyTypeForm extends React.PureComponent {
  constructor(props) {
    super(props)
    autoBind(this)

    this.state = {}

    if (this.props.controller) {
      this.controller = this.props.controller
    } else {
      this.controller = new VocabularyTypeFormController(
        new VocabularyTypeFormFacade()
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
      controller.handleSelectionChange(VocabularyTypeFormSelectionType.TERM, {
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
    logger.log(logger.DEBUG, 'VocabularyTypeForm.render')

    const { loadId, loading, loaded, vocabulary } = this.state

    return (
      <PageWithTwoPanels
        key={loadId}
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

    const id = ids.VOCABULARY_TERMS_GRID_ID

    return (
      <GridContainer onClick={this.handleClickContainer}>
        <GridWithOpenbis
          id={id}
          settingsId={id}
          controllerRef={this.handleGridControllerRef}
          header={messages.get(messages.TERMS)}
          columns={columns}
          rows={terms}
          sort='code'
          exportable={{
            fileFormat: GridExportOptions.TSV_FILE_FORMAT,
            filePrefix: 'vocabulary-terms'
          }}
          selectable={true}
          selectedRowId={
            selection && selection.type === VocabularyTypeFormSelectionType.TERM
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
    const { vocabulary, terms, selection, selectedRow, mode } = this.state

    return (
      <VocabularyTypeFormParameters
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
      <VocabularyTypeFormButtons
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

export default VocabularyTypeForm
