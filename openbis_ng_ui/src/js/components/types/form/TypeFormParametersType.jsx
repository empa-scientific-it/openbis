import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Container from '@src/js/components/common/form/Container.jsx'
import Header from '@src/js/components/common/form/Header.jsx'
import CheckboxField from '@src/js/components/common/form/CheckboxField.jsx'
import TextField from '@src/js/components/common/form/TextField.jsx'
import SelectField from '@src/js/components/common/form/SelectField.jsx'
import TypeFormSelectionType from '@src/js/components/types/form/TypeFormSelectionType.js'
import objectTypes from '@src/js/common/consts/objectType.js'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  field: {
    paddingBottom: theme.spacing(1)
  }
})

class TypeFormParametersType extends React.PureComponent {
  constructor(props) {
    super(props)
    this.state = {}
    this.references = {
      code: React.createRef(),
      description: React.createRef(),
      validationPlugin: React.createRef(),
      generatedCodePrefix: React.createRef(),
      autoGeneratedCode: React.createRef(),
      subcodeUnique: React.createRef()
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
    const type = this.getType(this.props)
    if (type && this.props.selection) {
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
    this.props.onChange(TypeFormSelectionType.TYPE, {
      field: event.target.name,
      value: event.target.value
    })
  }

  handleFocus(event) {
    this.props.onSelectionChange(TypeFormSelectionType.TYPE, {
      part: event.target.name
    })
  }

  handleBlur() {
    this.props.onBlur()
  }

  render() {
    logger.log(logger.DEBUG, 'TypeFormParametersType.render')

    const type = this.getType(this.props)
    if (!type) {
      return null
    }

    return (
      <Container>
        {this.renderHeader(type)}
        {this.renderCode(type)}
        {this.renderDescription(type)}
        {this.renderValidationPlugin(type)}
        {this.renderGeneratedCodePrefix(type)}
        {this.renderAutoGeneratedCode(type)}
        {this.renderSubcodeUnique(type)}
        {this.renderMainDataSetPattern(type)}
        {this.renderMainDataSetPath(type)}
        {this.renderDisallowDeletion(type)}
      </Container>
    )
  }

  renderHeader(type) {
    const map = {
      [objectTypes.OBJECT_TYPE]: messages.OBJECT_TYPE,
      [objectTypes.COLLECTION_TYPE]: messages.COLLECTION_TYPE,
      [objectTypes.DATA_SET_TYPE]: messages.DATA_SET_TYPE,
      [objectTypes.MATERIAL_TYPE]: messages.MATERIAL_TYPE,
      [objectTypes.NEW_OBJECT_TYPE]: messages.NEW_OBJECT_TYPE,
      [objectTypes.NEW_COLLECTION_TYPE]: messages.NEW_COLLECTION_TYPE,
      [objectTypes.NEW_DATA_SET_TYPE]: messages.NEW_DATA_SET_TYPE,
      [objectTypes.NEW_MATERIAL_TYPE]: messages.NEW_MATERIAL_TYPE
    }

    return <Header>{messages.get(map[type.objectType.value])}</Header>
  }

  renderCode(type) {
    const { visible, enabled, error, value } = { ...type.code }

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

  renderDescription(type) {
    const { visible, enabled, error, value } = { ...type.description }

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

  renderValidationPlugin(type) {
    const { visible, enabled, error, value } = { ...type.validationPlugin }

    if (!visible) {
      return null
    }

    const { mode, classes, controller } = this.props
    const { validationPlugins } = controller.getDictionaries()

    let options = []

    if (validationPlugins) {
      options = validationPlugins.map(validationPlugin => {
        return {
          label: validationPlugin.name,
          value: validationPlugin.name
        }
      })
    }

    return (
      <div className={classes.field}>
        <SelectField
          reference={this.references.validationPlugin}
          label={messages.get(messages.ENTITY_VALIDATION_PLUGIN)}
          name='validationPlugin'
          error={error}
          disabled={!enabled}
          value={value}
          options={options}
          emptyOption={{}}
          mode={mode}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderGeneratedCodePrefix(type) {
    const { visible, enabled, error, value } = { ...type.generatedCodePrefix }

    if (!visible) {
      return null
    }

    const { mode, classes } = this.props
    return (
      <div className={classes.field}>
        <TextField
          reference={this.references.generatedCodePrefix}
          label={messages.get(messages.GENERATED_CODE_PREFIX)}
          name='generatedCodePrefix'
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

  renderAutoGeneratedCode(type) {
    const { visible, enabled, error, value } = { ...type.autoGeneratedCode }

    if (!visible) {
      return null
    }

    const { mode, classes } = this.props
    return (
      <div className={classes.field}>
        <CheckboxField
          reference={this.references.autoGeneratedCode}
          label={messages.get(messages.GENERATE_CODES)}
          name='autoGeneratedCode'
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

  renderSubcodeUnique(type) {
    const { visible, enabled, error, value } = { ...type.subcodeUnique }

    if (!visible) {
      return null
    }

    const { mode, classes } = this.props
    return (
      <div className={classes.field}>
        <CheckboxField
          reference={this.references.subcodeUnique}
          label={messages.get(messages.SUBCODES_UNIQUE)}
          name='subcodeUnique'
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

  renderDisallowDeletion(type) {
    const { visible, enabled, error, value } = { ...type.disallowDeletion }

    if (!visible) {
      return null
    }

    const { mode, classes } = this.props
    return (
      <div className={classes.field}>
        <CheckboxField
          reference={this.references.disallowDeletion}
          label={messages.get(messages.DISALLOW_DELETION)}
          name='disallowDeletion'
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

  renderMainDataSetPattern(type) {
    const { visible, enabled, error, value } = { ...type.mainDataSetPattern }

    if (!visible) {
      return null
    }

    const { mode, classes } = this.props
    return (
      <div className={classes.field}>
        <TextField
          reference={this.references.mainDataSetPattern}
          label={messages.get(messages.MAIN_DATA_SET_PATTERN)}
          name='mainDataSetPattern'
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

  renderMainDataSetPath(type) {
    const { visible, enabled, error, value } = { ...type.mainDataSetPath }

    if (!visible) {
      return null
    }

    const { mode, classes } = this.props
    return (
      <div className={classes.field}>
        <TextField
          reference={this.references.mainDataSetPath}
          label={messages.get(messages.MAIN_DATA_SET_PATH)}
          name='mainDataSetPath'
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

  getType(props) {
    let { type, selection } = props

    if (!selection || selection.type === TypeFormSelectionType.TYPE) {
      return type
    } else {
      return null
    }
  }
}

export default withStyles(styles)(TypeFormParametersType)
