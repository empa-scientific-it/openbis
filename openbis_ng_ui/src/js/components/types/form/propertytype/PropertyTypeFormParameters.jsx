import _ from 'lodash'
import React from 'react'
import { connect } from 'react-redux'
import { withStyles } from '@material-ui/core/styles'
import Container from '@src/js/components/common/form/Container.jsx'
import Header from '@src/js/components/common/form/Header.jsx'
import TextField from '@src/js/components/common/form/TextField.jsx'
import CheckboxField from '@src/js/components/common/form/CheckboxField.jsx'
import SelectField from '@src/js/components/common/form/SelectField.jsx'
import Message from '@src/js/components/common/form/Message.jsx'
import DataType from '@src/js/components/common/dto/DataType.js'
import openbis from '@src/js/services/openbis.js'
import selectors from '@src/js/store/selectors/selectors.js'
import users from '@src/js/common/consts/users.js'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'
const styles = theme => ({
  field: {
    paddingBottom: theme.spacing(1)
  }
})

function mapStateToProps(state) {
  return {
    session: selectors.getSession(state)
  }
}

class PropertyTypeFormParameters extends React.PureComponent {
  constructor(props) {
    super(props)
    this.state = {}
    this.references = {
      code: React.createRef(),
      label: React.createRef(),
      description: React.createRef(),
      dataType: React.createRef(),
      vocabulary: React.createRef(),
      materialType: React.createRef(),
      sampleType: React.createRef(),
      schema: React.createRef(),
      transformation: React.createRef(),
      internal: React.createRef()
    }
    this.handleChange = this.handleChange.bind(this)
    this.handleFocus = this.handleFocus.bind(this)
    this.handleBlur = this.handleBlur.bind(this)
  }

  componentDidMount() {
    this.focus()
  }

  componentDidUpdate(prevProps) {
    const prevSelection = prevProps.selection
    const selection = this.props.selection

    if (prevSelection !== selection) {
      this.focus()
    }
  }

  focus() {
    if (this.props.selection) {
      const { part } = this.props.selection.params
      if (part) {
        const reference = this.references[part]
        if (reference && reference.current) {
          reference.current.focus()
        }
      }
    }
  }

  handleChange(event) {
    this.props.onChange(null, {
      field: event.target.name,
      value: event.target.value
    })
  }

  handleFocus(event) {
    this.props.onSelectionChange(null, {
      part: event.target.name
    })
  }

  handleBlur() {
    this.props.onBlur()
  }

  render() {
    logger.log(logger.DEBUG, 'PropertyTypeFormParameters.render')

    return (
      <Container>
        {this.renderHeader()}
        {this.renderMessageInternal()}
        {this.renderCode()}
        {this.renderDataType()}
        {this.renderVocabulary()}
        {this.renderMaterialType()}
        {this.renderSampleType()}
        {this.renderSchema()}
        {this.renderTransformation()}
        {this.renderLabel()}
        {this.renderDescription()}
        {this.renderInternal()}
      </Container>
    )
  }

  renderHeader() {
    const { propertyType } = this.props
    const message = propertyType.original
      ? messages.PROPERTY_TYPE
      : messages.NEW_PROPERTY_TYPE
    return <Header>{messages.get(message)}</Header>
  }

  renderMessageInternal() {
    const { propertyType } = this.props

    if (propertyType.internal.value) {
      const { classes, session } = this.props

      if (session && session.userName === users.SYSTEM) {
        return (
          <div className={classes.field}>
            <Message type='lock'>
              {messages.get(messages.PROPERTY_IS_INTERNAL)}
            </Message>
          </div>
        )
      } else {
        return (
          <div className={classes.field}>
            <Message type='lock'>
              {messages.get(messages.PROPERTY_IS_INTERNAL)}
              {propertyType.internal.value
                ? ' ' +
                  messages.get(messages.PROPERTY_PARAMETERS_CANNOT_BE_CHANGED)
                : ''}
            </Message>
          </div>
        )
      }
    } else {
      return null
    }
  }

  renderCode() {
    const { propertyType } = this.props
    const { visible, enabled, error, value } = { ...propertyType.code }

    if (!visible) {
      return null
    }

    const { mode, classes } = this.props
    return (
      <div className={classes.field}>
        <TextField
          reference={this.references.code}
          label={messages.get(messages.CODE)}
          name='code'
          mandatory={true}
          error={error}
          disabled={!enabled}
          value={value}
          mode={mode}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderDataType() {
    const { propertyType } = this.props
    const { visible, enabled, error, value } = {
      ...propertyType.dataType
    }

    if (!visible) {
      return null
    }

    const options = []

    if (propertyType.original) {
      const {
        dataType: { value: originalValue }
      } = propertyType.original

      const SUFFIX = ' (' + messages.get(messages.CONVERTED) + ')'
      options.push({
        label: new DataType(originalValue).getLabel(),
        value: originalValue
      })
      if (originalValue !== openbis.DataType.VARCHAR) {
        options.push({
          label: new DataType(openbis.DataType.VARCHAR).getLabel() + SUFFIX,
          value: openbis.DataType.VARCHAR
        })
      }
      if (originalValue !== openbis.DataType.MULTILINE_VARCHAR) {
        options.push({
          label:
            new DataType(openbis.DataType.MULTILINE_VARCHAR).getLabel() +
            SUFFIX,
          value: openbis.DataType.MULTILINE_VARCHAR
        })
      }
      if (originalValue === openbis.DataType.TIMESTAMP) {
        options.push({
          label: new DataType(openbis.DataType.DATE).getLabel() + SUFFIX,
          value: openbis.DataType.DATE
        })
      }
      if (originalValue === openbis.DataType.INTEGER) {
        options.push({
          label: new DataType(openbis.DataType.REAL).getLabel() + SUFFIX,
          value: openbis.DataType.REAL
        })
      }
    } else {
      openbis.DataType.values.map(dataType => {
        options.push({
          label: new DataType(dataType).getLabel(),
          value: dataType
        })
      })
    }

    const { mode, classes } = this.props
    return (
      <div className={classes.field}>
        <SelectField
          reference={this.references.dataType}
          label={messages.get(messages.DATA_TYPE)}
          name='dataType'
          mandatory={true}
          error={error}
          disabled={!enabled}
          value={value}
          options={options}
          mode={mode}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderVocabulary() {
    const { propertyType } = this.props
    const { visible, enabled, error, value } = { ...propertyType.vocabulary }

    if (!visible) {
      return null
    }

    const { mode, classes, controller } = this.props
    const { vocabularies } = controller.getDictionaries()

    let options = []

    if (vocabularies) {
      options = vocabularies.map(vocabulary => {
        return {
          label: vocabulary.code,
          value: vocabulary.code
        }
      })
    }

    return (
      <div className={classes.field}>
        <SelectField
          reference={this.references.vocabulary}
          label={messages.get(messages.VOCABULARY_TYPE)}
          name='vocabulary'
          mandatory={true}
          error={error}
          disabled={!enabled}
          value={value}
          options={options}
          mode={mode}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderMaterialType() {
    const { propertyType } = this.props
    const { visible, enabled, error, value } = { ...propertyType.materialType }

    if (!visible) {
      return null
    }

    const { mode, classes, controller } = this.props
    const { materialTypes } = controller.getDictionaries()

    let options = []

    if (materialTypes) {
      options = materialTypes.map(materialType => {
        return {
          label: materialType.code,
          value: materialType.code
        }
      })
    }

    return (
      <div className={classes.field}>
        <SelectField
          reference={this.references.materialType}
          label={messages.get(messages.MATERIAL_TYPE)}
          name='materialType'
          error={error}
          disabled={!enabled}
          value={value}
          options={options}
          emptyOption={{
            label: '(' + messages.get(messages.ALL) + ')',
            selectable: true
          }}
          mode={mode}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderSampleType() {
    const { propertyType } = this.props
    const { visible, enabled, error, value } = { ...propertyType.sampleType }

    if (!visible) {
      return null
    }

    const { mode, classes, controller } = this.props
    const { sampleTypes } = controller.getDictionaries()

    let options = []

    if (sampleTypes) {
      options = sampleTypes.map(sampleType => {
        return {
          label: sampleType.code,
          value: sampleType.code
        }
      })
    }

    return (
      <div className={classes.field}>
        <SelectField
          reference={this.references.sampleType}
          label={messages.get(messages.OBJECT_TYPE)}
          name='sampleType'
          error={error}
          disabled={!enabled}
          value={value}
          options={options}
          emptyOption={{
            label: '(' + messages.get(messages.ALL) + ')',
            selectable: true
          }}
          mode={mode}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderSchema() {
    const { propertyType } = this.props
    const { visible, enabled, error, value } = { ...propertyType.schema }

    if (!visible) {
      return null
    }

    const { mode, classes } = this.props

    return (
      <div className={classes.field}>
        <TextField
          reference={this.references.schema}
          label={messages.get(messages.XML_SCHEMA)}
          name='schema'
          error={error}
          disabled={!enabled}
          value={value}
          multiline={true}
          mode={mode}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderTransformation() {
    const { propertyType } = this.props
    const { visible, enabled, error, value } = {
      ...propertyType.transformation
    }

    if (!visible) {
      return null
    }

    const { mode, classes } = this.props

    return (
      <div className={classes.field}>
        <TextField
          reference={this.references.transformation}
          label={messages.get(messages.XSLT_SCRIPT)}
          name='transformation'
          error={error}
          disabled={!enabled}
          value={value}
          multiline={true}
          mode={mode}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderLabel() {
    const { propertyType } = this.props
    const { visible, enabled, error, value } = { ...propertyType.label }

    if (!visible) {
      return null
    }

    const { mode, classes } = this.props
    return (
      <div className={classes.field}>
        <TextField
          reference={this.references.label}
          label={messages.get(messages.LABEL)}
          name='label'
          mandatory={true}
          error={error}
          disabled={!enabled}
          value={value}
          mode={mode}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderDescription() {
    const { propertyType } = this.props
    const { visible, enabled, error, value } = { ...propertyType.description }

    if (!visible) {
      return null
    }

    const { mode, classes } = this.props
    return (
      <div className={classes.field}>
        <TextField
          reference={this.references.description}
          label={messages.get(messages.DESCRIPTION)}
          name='description'
          mandatory={true}
          error={error}
          disabled={!enabled}
          value={value}
          mode={mode}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderInternal() {
    const { propertyType } = this.props
    const { visible, enabled, error, value } = { ...propertyType.internal }

    if (!visible) {
      return null
    }

    const { mode, classes } = this.props
    return (
      <div className={classes.field}>
        <CheckboxField
          reference={this.references.internal}
          label={messages.get(messages.INTERNAL)}
          name='internal'
          error={error}
          disabled={!enabled}
          value={value}
          mode={mode}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }
}

export default _.flow(
  connect(mapStateToProps),
  withStyles(styles)
)(PropertyTypeFormParameters)
