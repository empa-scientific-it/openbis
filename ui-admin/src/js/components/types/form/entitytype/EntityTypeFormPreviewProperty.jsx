import _ from 'lodash'
import React from 'react'
import { Draggable } from 'react-beautiful-dnd'
import { withStyles } from '@material-ui/core/styles'
import PageMode from '@src/js/components/common/page/PageMode.js'
import Message from '@src/js/components/common/form/Message.jsx'
import CheckboxField from '@src/js/components/common/form/CheckboxField.jsx'
import TextField from '@src/js/components/common/form/TextField.jsx'
import SelectField from '@src/js/components/common/form/SelectField.jsx'
import EntityTypeFormSelectionType from '@src/js/components/types/form/entitytype/EntityTypeFormSelectionType.js'
import DataType from '@src/js/components/common/dto/DataType.js'
import AppController from '@src/js/components/AppController.js'
import openbis from '@src/js/services/openbis.js'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'
import util from '@src/js/common/util.js'

const EMPTY = 'empty'

const styles = theme => ({
  draggable: {
    width: '100%',
    cursor: 'pointer',
    padding: theme.spacing(1),
    boxSizing: 'border-box',
    borderWidth: '2px',
    borderStyle: 'solid',
    borderColor: theme.palette.background.paper,
    backgroundColor: theme.palette.background.paper,
    '&:last-child': {
      marginBottom: 0
    },
    '&:hover': {
      borderColor: theme.palette.border.primary
    },
    '& .MuiInputBase-root.Mui-disabled': {
      cursor: 'pointer'
    }
  },
  selected: {
    borderColor: theme.palette.secondary.main,
    '&:hover': {
      borderColor: theme.palette.secondary.main
    }
  },
  hidden: {
    opacity: 0.4
  },
  partEmpty: {
    fontStyle: 'italic'
  },
  partSelected: {
    cursor: 'pointer',
    pointerEvents: 'initial',
    paddingBottom: '1px',
    borderColor: theme.palette.secondary.main,
    borderStyle: 'solid',
    borderWidth: '0px 0px 2px 0px',
    '&:hover': {
      borderColor: theme.palette.secondary.main
    }
  },
  partNotSelected: {
    cursor: 'pointer',
    pointerEvents: 'initial',
    paddingBottom: '1px',
    borderColor: 'transparent',
    borderStyle: 'solid',
    borderWidth: '0px 0px 2px 0px',
    '&:hover': {
      borderColor: theme.palette.border.primary
    }
  },
  property: {
    marginTop: '-6px',
    overflow: 'hidden'
  }
})

class EntityTypeFormPreviewProperty extends React.PureComponent {
  constructor(props) {
    super(props)
    this.state = {
      values: {}
    }
    this.handleDraggableClick = this.handleDraggableClick.bind(this)
    this.handlePropertyClick = this.handlePropertyClick.bind(this)
    this.handleChange = this.handleChange.bind(this)
  }

  componentDidMount() {
    this.load(null, this.props)
  }

  componentDidUpdate(prevProps) {
    this.load(prevProps, this.props)
  }

  load(prevProps, props) {
    const prevProperty = prevProps ? prevProps.property : null
    const property = props ? props.property : null

    if (this.shouldLoadMaterials(prevProperty, property)) {
      this.loadMaterials()
    }
    if (this.shouldLoadSamples(prevProperty, property)) {
      this.loadSamples()
    }
    if (this.shouldLoadVocabularyTerms(prevProperty, property)) {
      this.loadVocabularyTerms()
    }
  }

  shouldLoadMaterials(prevProperty, property) {
    const dataType = _.get(property, 'dataType.value')
    const prevDataType = _.get(prevProperty, 'dataType.value')
    const materialType = _.get(property, 'materialType.value')
    const prevMaterialType = _.get(prevProperty, 'materialType.value')

    return (
      dataType === openbis.DataType.MATERIAL &&
      (prevDataType !== openbis.DataType.MATERIAL ||
        prevMaterialType !== materialType)
    )
  }

  shouldLoadSamples(prevProperty, property) {
    const dataType = _.get(property, 'dataType.value')
    const prevDataType = _.get(prevProperty, 'dataType.value')
    const sampleType = _.get(property, 'sampleType.value')
    const prevSampleType = _.get(prevProperty, 'sampleType.value')

    return (
      dataType === openbis.DataType.SAMPLE &&
      (prevDataType !== openbis.DataType.SAMPLE ||
        prevSampleType !== sampleType)
    )
  }

  shouldLoadVocabularyTerms(prevProperty, property) {
    const dataType = _.get(property, 'dataType.value')
    const prevDataType = _.get(prevProperty, 'dataType.value')
    const vocabulary = _.get(property, 'vocabulary.value')
    const prevVocabulary = _.get(prevProperty, 'vocabulary.value')

    return (
      dataType === openbis.DataType.CONTROLLEDVOCABULARY &&
      (prevDataType !== openbis.DataType.CONTROLLEDVOCABULARY ||
        prevVocabulary !== vocabulary)
    )
  }

  loadMaterials() {
    const { controller, property } = this.props

    return controller
      .getFacade()
      .loadMaterials(property.materialType.value)
      .then(materials => {
        this.setState(() => ({
          materials
        }))
      })
      .catch(error => {
        AppController.getInstance().errorChange(error)
      })
  }

  loadSamples() {
    const { controller, property } = this.props

    return controller
      .getFacade()
      .loadSamples(property.sampleType.value)
      .then(samples => {
        this.setState(() => ({
          samples
        }))
      })
      .catch(error => {
        AppController.getInstance().errorChange(error)
      })
  }

  loadVocabularyTerms() {
    const { controller, property } = this.props

    if (property.vocabulary.value) {
      return controller
        .getFacade()
        .loadVocabularyTerms(property.vocabulary.value)
        .then(terms => {
          this.setState(() => ({
            terms
          }))
        })
        .catch(error => {
          AppController.getInstance().errorChange(error)
        })
    } else {
      this.setState(() => ({
        terms: null
      }))
    }
  }

  handleDraggableClick(event) {
    let newSelection = {
      type: EntityTypeFormSelectionType.PROPERTY,
      params: {
        id: this.props.property.id
      }
    }
    this.handleClick(event, newSelection)
  }

  handlePropertyClick(event) {
    let newSelection = {
      type: EntityTypeFormSelectionType.PROPERTY,
      params: {
        id: this.props.property.id
      }
    }
    if (event.target.dataset && event.target.dataset.part) {
      newSelection.params.part = event.target.dataset.part
    }
    this.handleClick(event, newSelection)
  }

  handleClick(event, newSelection) {
    event.stopPropagation()
    this.props.onSelectionChange(newSelection.type, newSelection.params)
  }

  handleChange(event) {
    this.props.onChange(EntityTypeFormSelectionType.PREVIEW, {
      field: this.props.property.id,
      value: event.target.value
    })
  }

  render() {
    logger.log(logger.DEBUG, 'EntityTypeFormPreviewProperty.render')

    let { mode, property, selection, index, classes } = this.props

    const selected =
      selection &&
      selection.type === EntityTypeFormSelectionType.PROPERTY &&
      selection.params.id === property.id

    return (
      <Draggable
        draggableId={property.id}
        index={index}
        isDragDisabled={mode !== PageMode.EDIT}
      >
        {provided => (
          <div
            ref={provided.innerRef}
            {...provided.draggableProps}
            {...provided.dragHandleProps}
            className={util.classNames(
              classes.draggable,
              selected ? classes.selected : null
            )}
            onClick={this.handleDraggableClick}
          >
            {this.renderProperty()}
          </div>
        )}
      </Draggable>
    )
  }

  renderProperty() {
    const dataType = this.props.property.dataType.value

    if (
      dataType === openbis.DataType.VARCHAR ||
      dataType === openbis.DataType.MULTILINE_VARCHAR ||
      dataType === openbis.DataType.HYPERLINK ||
      dataType === openbis.DataType.TIMESTAMP ||
      dataType === openbis.DataType.DATE ||
      dataType === openbis.DataType.XML
    ) {
      return this.renderVarcharProperty()
    } else if (
      dataType === openbis.DataType.REAL ||
      dataType === openbis.DataType.INTEGER
    ) {
      return this.renderNumberProperty()
    } else if (dataType === openbis.DataType.BOOLEAN) {
      return this.renderBooleanProperty()
    } else if (dataType === openbis.DataType.CONTROLLEDVOCABULARY) {
      return this.renderVocabularyProperty()
    } else if (dataType === openbis.DataType.MATERIAL) {
      return this.renderMaterialProperty()
    } else if (dataType === openbis.DataType.SAMPLE) {
      return this.renderSampleProperty()
    } else {
      if (dataType) {
        return this.renderPropertyNotSupported()
      } else {
        return this.renderPropertyWithoutDataType()
      }
    }
  }

  renderPropertyNotSupported() {
    return (
      <Message type='warning'>
        {messages.get(messages.DATA_TYPE_NOT_SUPPORTED)}
      </Message>
    )
  }

  renderPropertyWithoutDataType() {
    return (
      <Message type='info'>
        {messages.get(messages.DATA_TYPE_NOT_SELECTED_FOR_PREVIEW)}
      </Message>
    )
  }

  renderVarcharProperty() {
    const { property, value, mode, classes } = this.props
    return (
      <div className={classes.property}>
        <TextField
          name={property.id}
          label={this.getLabel()}
          description={this.getDescription()}
          value={value}
          mandatory={this.getMandatory()}
          multiline={this.getMultiline()}
          metadata={this.getMetadata()}
          error={this.getError()}
          styles={this.getStyles()}
          mode={PageMode.EDIT}
          disabled={mode !== PageMode.EDIT}
          onClick={this.handlePropertyClick}
          onChange={this.handleChange}
        />
      </div>
    )
  }

  renderNumberProperty() {
    const { property, value, mode, classes } = this.props
    return (
      <div className={classes.property}>
        <TextField
          type='number'
          name={property.id}
          label={this.getLabel()}
          description={this.getDescription()}
          value={value}
          mandatory={this.getMandatory()}
          metadata={this.getMetadata()}
          error={this.getError()}
          styles={this.getStyles()}
          mode={PageMode.EDIT}
          disabled={mode !== PageMode.EDIT}
          onClick={this.handlePropertyClick}
          onChange={this.handleChange}
        />
      </div>
    )
  }

  renderBooleanProperty() {
    const { property, value, mode, classes } = this.props
    return (
      <div className={classes.property}>
        <CheckboxField
          name={property.id}
          label={this.getLabel()}
          description={this.getDescription()}
          value={value}
          mandatory={this.getMandatory()}
          metadata={this.getMetadata()}
          error={this.getError()}
          styles={this.getStyles()}
          mode={PageMode.EDIT}
          disabled={mode !== PageMode.EDIT}
          onClick={this.handlePropertyClick}
          onChange={this.handleChange}
        />
      </div>
    )
  }

  renderVocabularyProperty() {
    const { property, value, mode, classes } = this.props
    const { terms } = this.state

    let options = []

    if (terms) {
      options = terms.map(term => ({
        value: term.code,
        label: term.label
      }))
    }

    return (
      <div className={classes.property}>
        <SelectField
          name={property.id}
          label={this.getLabel()}
          description={this.getDescription()}
          value={value}
          mandatory={this.getMandatory()}
          options={options}
          emptyOption={{
            label: '(' + messages.get(messages.PREVIEW) + ')',
            selectable: false
          }}
          metadata={this.getMetadata()}
          error={this.getError()}
          styles={this.getStyles()}
          mode={PageMode.EDIT}
          disabled={mode !== PageMode.EDIT}
          onClick={this.handlePropertyClick}
          onChange={this.handleChange}
        />
      </div>
    )
  }

  renderMaterialProperty() {
    const { property, value, mode, classes } = this.props
    const { materials } = this.state

    let options = []

    if (materials) {
      options = materials.map(material => ({
        value: material.code
      }))
    }

    return (
      <div className={classes.property}>
        <SelectField
          name={property.id}
          label={this.getLabel()}
          description={this.getDescription()}
          value={value}
          mandatory={this.getMandatory()}
          options={options}
          emptyOption={{
            label: '(' + messages.get(messages.PREVIEW) + ')',
            selectable: false
          }}
          metadata={this.getMetadata()}
          error={this.getError()}
          styles={this.getStyles()}
          mode={PageMode.EDIT}
          disabled={mode !== PageMode.EDIT}
          onClick={this.handlePropertyClick}
          onChange={this.handleChange}
        />
      </div>
    )
  }

  renderSampleProperty() {
    const { property, value, mode, classes } = this.props
    const { samples } = this.state

    let options = []

    if (samples) {
      options = samples.map(sample => ({
        value: sample.identifier.identifier
      }))
    }

    return (
      <div className={classes.property}>
        <SelectField
          name={property.id}
          label={this.getLabel()}
          description={this.getDescription()}
          value={value}
          mandatory={this.getMandatory()}
          options={options}
          emptyOption={{
            label: '(' + messages.get(messages.PREVIEW) + ')',
            selectable: false
          }}
          metadata={this.getMetadata()}
          error={this.getError()}
          styles={this.getStyles()}
          mode={PageMode.EDIT}
          disabled={mode !== PageMode.EDIT}
          onClick={this.handlePropertyClick}
          onChange={this.handleChange}
        />
      </div>
    )
  }

  getCode() {
    return this.props.property.code.value || EMPTY
  }

  getLabel() {
    return this.props.property.label.value || EMPTY
  }

  getDescription() {
    return this.props.property.description.value || EMPTY
  }

  getDataType() {
    return new DataType(this.props.property.dataType.value).getLabel() || EMPTY
  }

  getMandatory() {
    return this.props.property.mandatory.value
  }

  getMultiline() {
    return (
      this.props.property.dataType.value ===
        openbis.DataType.MULTILINE_VARCHAR ||
      this.props.property.dataType.value === openbis.DataType.XML
    )
  }

  getMetadata() {
    const styles = this.getStyles()

    return (
      <React.Fragment>
        [
        <span
          data-part='code'
          className={styles.code}
          onClick={this.handlePropertyClick}
        >
          {this.getCode()}
        </span>
        ][
        <span
          data-part='dataType'
          className={styles.dataType}
          onClick={this.handlePropertyClick}
        >
          {this.getDataType()}
        </span>
        ]
      </React.Fragment>
    )
  }

  getError() {
    if (this.props.property.errors > 0) {
      return messages.get(messages.PROPERTY_CONFIGURATION_IS_INCORRECT)
    } else {
      return null
    }
  }

  getStyles() {
    const { property, selection, classes } = this.props

    let styles = {}

    const parts = ['code', 'label', 'dataType', 'mandatory', 'description']
    const selectedPart =
      selection &&
      selection.type === EntityTypeFormSelectionType.PROPERTY &&
      selection.params.id === property.id &&
      selection.params.part

    parts.forEach(part => {
      const partStyles = []

      if (part === selectedPart) {
        partStyles.push(classes.partSelected)
      } else {
        partStyles.push(classes.partNotSelected)
      }

      const partValue = property[part].value

      if (!partValue) {
        partStyles.push(classes.partEmpty)
      }

      styles = {
        ...styles,
        [part]: partStyles.join(' ')
      }
    })

    if (!property.showInEditView.value) {
      styles = {
        ...styles,
        container: classes.hidden
      }
    }

    return styles
  }
}

export default _.flow(withStyles(styles))(EntityTypeFormPreviewProperty)
