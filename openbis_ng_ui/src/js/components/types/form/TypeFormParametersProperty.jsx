import _ from 'lodash'
import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import { connect } from 'react-redux'
import Container from '@src/js/components/common/form/Container.jsx'
import Header from '@src/js/components/common/form/Header.jsx'
import AutocompleterField from '@src/js/components/common/form/AutocompleterField.jsx'
import CheckboxField from '@src/js/components/common/form/CheckboxField.jsx'
import TextField from '@src/js/components/common/form/TextField.jsx'
import SelectField from '@src/js/components/common/form/SelectField.jsx'
import Message from '@src/js/components/common/form/Message.jsx'
import TypeFormSelectionType from '@src/js/components/types/form/TypeFormSelectionType.js'
import TypeFormPropertyScope from '@src/js/components/types/form/TypeFormPropertyScope.js'
import DataType from '@src/js/components/common/dto/DataType.js'
import openbis from '@src/js/services/openbis.js'
import users from '@src/js/common/consts/users.js'
import selectors from '@src/js/store/selectors/selectors.js'
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

class TypeFormParametersProperty extends React.PureComponent {
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
      initialValueForExistingEntities: React.createRef(),
      mandatory: React.createRef(),
      internal: React.createRef(),
      plugin: React.createRef(),
      showInEditView: React.createRef()
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
    const property = this.getProperty(this.props)
    if (property) {
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
    const property = this.getProperty(this.props)
    this.props.onChange(TypeFormSelectionType.PROPERTY, {
      id: property.id,
      field: event.target.name,
      value: event.target.value
    })
  }

  handleFocus(event) {
    const property = this.getProperty(this.props)
    this.props.onSelectionChange(TypeFormSelectionType.PROPERTY, {
      id: property.id,
      part: event.target.name
    })
  }

  handleBlur() {
    this.props.onBlur()
  }

  render() {
    logger.log(logger.DEBUG, 'TypeFormParametersProperty.render')

    const property = this.getProperty(this.props)
    if (!property) {
      return null
    }

    return (
      <Container>
        <Header>{messages.get(messages.PROPERTY)}</Header>
        {this.renderMessageGlobal(property)}
        {this.renderMessageAssignments(property)}
        {this.renderMessageSystemInternal(property)}
        {this.renderScope(property)}
        {this.renderCode(property)}
        {this.renderDataType(property)}
        {this.renderVocabulary(property)}
        {this.renderMaterialType(property)}
        {this.renderSampleType(property)}
        {this.renderSchema(property)}
        {this.renderTransformation(property)}
        {this.renderLabel(property)}
        {this.renderDescription(property)}
        {this.renderDynamicPlugin(property)}
        {this.renderInternal(property)}
        {this.renderVisible(property)}
        {this.renderMandatory(property)}
        {this.renderInitialValue(property)}
      </Container>
    )
  }

  renderMessageGlobal(property) {
    if (property.scope.value === TypeFormPropertyScope.GLOBAL) {
      const { classes } = this.props
      return (
        <div className={classes.field}>
          <Message type='warning'>
            {messages.get(messages.PROPERTY_IS_GLOBAL)}
          </Message>
        </div>
      )
    } else {
      return null
    }
  }

  renderMessageAssignments(property) {
    const { classes } = this.props

    if (
      (property.original && property.assignments > 1) ||
      (!property.original && property.assignments > 0)
    ) {
      return (
        <div className={classes.field}>
          <Message type='info'>
            {messages.get(messages.PROPERTY_IS_ASSIGNED, property.assignments)}
          </Message>
        </div>
      )
    } else {
      return null
    }
  }

  renderMessageSystemInternal(property) {
    if (property.internal.value || property.assignmentInternal.value) {
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
              {property.internal.value
                ? ' ' +
                  messages.get(messages.PROPERTY_PARAMETERS_CANNOT_BE_CHANGED)
                : ''}
              {property.assignmentInternal.value
                ? ' ' +
                  messages.get(messages.PROPERTY_ASSIGNMENT_CANNOT_BE_REMOVED)
                : ''}
            </Message>
          </div>
        )
      }
    } else {
      return null
    }
  }

  renderScope(property) {
    const { visible, enabled, error, value } = { ...property.scope }

    if (!visible) {
      return null
    }

    const options = [
      {
        label: messages.get(messages.LOCAL),
        value: TypeFormPropertyScope.LOCAL
      },
      {
        label: messages.get(messages.GLOBAL),
        value: TypeFormPropertyScope.GLOBAL
      }
    ]

    const { mode, classes } = this.props
    return (
      <div className={classes.field}>
        <SelectField
          reference={this.references.scope}
          label={messages.get(messages.SCOPE)}
          name='scope'
          mandatory={true}
          error={error}
          disabled={!enabled}
          value={value}
          mode={mode}
          options={options}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderLabel(property) {
    const { visible, enabled, error, value } = { ...property.label }

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

  renderCode(property) {
    const { visible, enabled, error, value } = { ...property.code }

    if (!visible) {
      return null
    }

    const { mode, classes, controller } = this.props

    if (property.scope.value === TypeFormPropertyScope.LOCAL) {
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
    } else if (property.scope.value === TypeFormPropertyScope.GLOBAL) {
      const { globalPropertyTypes = [] } = controller.getDictionaries()

      const options = globalPropertyTypes.map(globalPropertyType => {
        return globalPropertyType.code
      })

      return (
        <div className={classes.field}>
          <AutocompleterField
            reference={this.references.code}
            label={messages.get(messages.CODE)}
            name='code'
            options={options}
            mandatory={true}
            error={error}
            disabled={!enabled}
            value={value}
            freeSolo={true}
            mode={mode}
            onChange={this.handleChange}
            onFocus={this.handleFocus}
            onBlur={this.handleBlur}
          />
        </div>
      )
    }
  }

  renderDescription(property) {
    const { visible, enabled, error, value } = { ...property.description }

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

  renderDataType(property) {
    const { visible, enabled, error, value } = {
      ...property.dataType
    }

    if (!visible) {
      return null
    }

    const options = []

    if (property.originalGlobal || property.original) {
      const {
        dataType: { value: originalValue }
      } = property.originalGlobal || property.original

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

  renderVocabulary(property) {
    const { visible, enabled, error, value } = { ...property.vocabulary }

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

  renderMaterialType(property) {
    const { visible, enabled, error, value } = { ...property.materialType }

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

  renderSampleType(property) {
    const { visible, enabled, error, value } = { ...property.sampleType }

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

  renderSchema(property) {
    const { visible, enabled, error, value } = { ...property.schema }

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

  renderTransformation(property) {
    const { visible, enabled, error, value } = { ...property.transformation }

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

  renderDynamicPlugin(property) {
    const { visible, enabled, error, value } = { ...property.plugin }

    if (!visible) {
      return null
    }

    const { mode, classes, controller } = this.props
    const { dynamicPlugins } = controller.getDictionaries()

    let options = []

    if (dynamicPlugins) {
      options = dynamicPlugins.map(dynamicPlugin => {
        return {
          label: dynamicPlugin.name,
          value: dynamicPlugin.name
        }
      })
    }

    return (
      <div className={classes.field}>
        <SelectField
          reference={this.references.plugin}
          label={messages.get(messages.DYNAMIC_PROPERTY_PLUGIN)}
          name='plugin'
          error={error}
          disabled={!enabled}
          value={value}
          options={options}
          emptyOption={
            property.original && property.original.plugin.value ? null : {}
          }
          mode={mode}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderInternal(property) {
    const { visible, enabled, error, value } = { ...property.internal }

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

  renderMandatory(property) {
    const { visible, enabled, error, value } = { ...property.mandatory }

    if (!visible) {
      return null
    }

    const { mode, classes } = this.props
    return (
      <div className={classes.field}>
        <CheckboxField
          reference={this.references.mandatory}
          label={messages.get(messages.MANDATORY)}
          name='mandatory'
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

  renderInitialValue(property) {
    const { visible, enabled, error, value } = {
      ...property.initialValueForExistingEntities
    }

    if (!visible) {
      return null
    }

    const { mode, classes } = this.props
    return (
      <div className={classes.field}>
        <TextField
          reference={this.references.initialValueForExistingEntities}
          label={messages.get(messages.INITIAL_VALUE)}
          name='initialValueForExistingEntities'
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

  renderVisible(property) {
    const { visible, enabled, error, value } = { ...property.showInEditView }

    if (!visible) {
      return null
    }

    const { mode, classes } = this.props
    return (
      <div className={classes.field}>
        <CheckboxField
          reference={this.references.showInEditView}
          label={messages.get(messages.EDITABLE)}
          name='showInEditView'
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

  getType() {
    return this.props.type
  }

  getProperty(props) {
    let { properties, selection } = props

    if (selection && selection.type === TypeFormSelectionType.PROPERTY) {
      let [property] = properties.filter(
        property => property.id === selection.params.id
      )
      return property
    } else {
      return null
    }
  }
}

export default _.flow(
  connect(mapStateToProps),
  withStyles(styles)
)(TypeFormParametersProperty)
