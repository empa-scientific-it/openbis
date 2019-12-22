import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Typography from '@material-ui/core/Typography'
import CheckboxField from '../../common/form/CheckboxField.jsx'
import TextField from '../../common/form/TextField.jsx'
import SelectField from '../../common/form/SelectField.jsx'
import ObjectTypeWarningUsage from './ObjectTypeWarningUsage.jsx'
import ObjectTypeWarningLegacy from './ObjectTypeWarningLegacy.jsx'
import { dto } from '../../../services/openbis.js'
import logger from '../../../common/logger.js'

const styles = theme => ({
  container: {
    padding: theme.spacing(2)
  },
  header: {
    paddingBottom: theme.spacing(2)
  },
  field: {
    paddingBottom: theme.spacing(2)
  }
})

class ObjectTypeParametersProperty extends React.PureComponent {
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
      schema: React.createRef(),
      transformation: React.createRef(),
      initialValueForExistingEntities: React.createRef(),
      mandatory: React.createRef(),
      plugin: React.createRef(),
      showInEditView: React.createRef()
    }
    this.handleChange = this.handleChange.bind(this)
    this.handleFocus = this.handleFocus.bind(this)
    this.handleBlur = this.handleBlur.bind(this)
  }

  componentDidMount() {
    this.load()
    this.focus()
  }

  componentDidUpdate(prevProps) {
    const prevProperty = this.getProperty(prevProps)
    const property = this.getProperty(this.props)

    const prevDataType = prevProperty ? prevProperty.dataType : null
    const dataType = property ? property.dataType : null

    if (prevDataType !== dataType) {
      this.load()
    }

    const prevSelection = prevProps.selection
    const selection = this.props.selection

    if (prevSelection !== selection) {
      this.focus()
    }
  }

  load() {
    const property = this.getProperty(this.props)

    if (property) {
      const dataType = property.dataType

      if (dataType === dto.DataType.CONTROLLEDVOCABULARY) {
        this.loadVocabularies()
      } else if (dataType === dto.DataType.MATERIAL) {
        this.loadMaterialTypes()
      }

      this.loadDynamicPlugins()
    }
  }

  loadDynamicPlugins() {
    const { facade } = this.props
    return facade
      .loadDynamicPlugins()
      .then(result => {
        this.setState(() => ({
          dynamicPlugins: result.objects
        }))
      })
      .catch(error => {
        facade.catch(error)
      })
  }

  loadVocabularies() {
    const { facade } = this.props
    return facade
      .loadVocabularies()
      .then(result => {
        this.setState(() => ({
          vocabularies: result.objects
        }))
      })
      .catch(error => {
        facade.catch(error)
      })
  }

  loadMaterialTypes() {
    const { facade } = this.props
    return facade
      .loadMaterialTypes()
      .then(result => {
        this.setState(() => ({
          materialTypes: result.objects
        }))
      })
      .catch(error => {
        facade.catch(error)
      })
  }

  focus() {
    const property = this.getProperty(this.props)
    if (property) {
      const { part } = this.props.selection.params
      if (part) {
        const reference = this.references[part]
        if (reference) {
          reference.current.focus()
        }
      }
    }
  }

  params(event) {
    const property = this.getProperty(this.props)

    return {
      id: property.id,
      field: event.target.name,
      part: event.target.name,
      value: event.target.value
    }
  }

  handleChange(event) {
    this.props.onChange('property', this.params(event))
  }

  handleFocus(event) {
    this.props.onSelectionChange('property', this.params(event))
  }

  handleBlur(event) {
    this.props.onBlur('property', this.params(event))
  }

  render() {
    logger.log(logger.DEBUG, 'ObjectTypeParametersProperty.render')

    const property = this.getProperty(this.props)
    if (!property) {
      return null
    }

    const { classes } = this.props

    return (
      <div className={classes.container}>
        <Typography variant='h6' className={classes.header}>
          Property
        </Typography>
        {this.renderWarningLegacy(property)}
        {this.renderWarningUsage(property)}
        {this.renderCode(property)}
        {this.renderDataType(property)}
        {this.renderVocabulary(property)}
        {this.renderMaterialType(property)}
        {this.renderSchema(property)}
        {this.renderTransformation(property)}
        {this.renderLabel(property)}
        {this.renderDescription(property)}
        {this.renderDynamicPlugin(property)}
        {this.renderVisible(property)}
        {this.renderMandatory(property)}
        {this.renderInitialValue(property)}
      </div>
    )
  }

  renderWarningLegacy(property) {
    if (this.isLegacy(property)) {
      const { classes } = this.props
      return (
        <div className={classes.field}>
          <ObjectTypeWarningLegacy />
        </div>
      )
    } else {
      return null
    }
  }

  renderWarningUsage(property) {
    if (property.usages > 0) {
      const { classes } = this.props
      return (
        <div className={classes.field}>
          <ObjectTypeWarningUsage subject='property' usages={property.usages} />
        </div>
      )
    } else {
      return null
    }
  }

  renderLabel(property) {
    const { classes } = this.props
    return (
      <div className={classes.field}>
        <TextField
          reference={this.references.label}
          label='Label'
          name='label'
          mandatory={true}
          error={property.errors.label}
          disabled={this.isLegacy(property)}
          value={property.label}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderCode(property) {
    const { classes } = this.props
    return (
      <div className={classes.field}>
        <TextField
          reference={this.references.code}
          label='Code'
          name='code'
          mandatory={true}
          error={property.errors.code}
          disabled={!!property.original}
          value={property.code}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderDescription(property) {
    const { classes } = this.props
    return (
      <div className={classes.field}>
        <TextField
          reference={this.references.description}
          label='Description'
          name='description'
          mandatory={true}
          error={property.errors.description}
          disabled={this.isLegacy(property)}
          value={property.description}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderDataType(property) {
    const { classes } = this.props

    const options = dto.DataType.values.sort().map(dataType => {
      return {
        label: dataType,
        value: dataType
      }
    })

    return (
      <div className={classes.field}>
        <SelectField
          reference={this.references.dataType}
          label='Data Type'
          name='dataType'
          mandatory={true}
          error={property.errors.dataType}
          disabled={property.usages > 0 || this.isLegacy(property)}
          value={property.dataType}
          options={options}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderVocabulary(property) {
    if (property.dataType === dto.DataType.CONTROLLEDVOCABULARY) {
      const { classes } = this.props
      const { vocabularies = [] } = this.state

      let options = []

      if (vocabularies.length > 0) {
        options = vocabularies.map(vocabulary => {
          return {
            label: vocabulary.code,
            value: vocabulary.code
          }
        })
        options.unshift({})
      }

      return (
        <div className={classes.field}>
          <SelectField
            reference={this.references.vocabulary}
            label='Vocabulary'
            name='vocabulary'
            mandatory={true}
            error={property.errors.vocabulary}
            disabled={property.usages > 0 || this.isLegacy(property)}
            value={property.vocabulary}
            options={options}
            onChange={this.handleChange}
            onFocus={this.handleFocus}
            onBlur={this.handleBlur}
          />
        </div>
      )
    } else {
      return null
    }
  }

  renderMaterialType(property) {
    if (property.dataType === dto.DataType.MATERIAL) {
      const { classes } = this.props
      const { materialTypes = [] } = this.state

      let options = []

      if (materialTypes.length > 0) {
        options = materialTypes.map(materialType => {
          return {
            label: materialType.code,
            value: materialType.code
          }
        })
        options.unshift({})
      }

      return (
        <div className={classes.field}>
          <SelectField
            reference={this.references.materialType}
            label='Material Type'
            name='materialType'
            mandatory={true}
            error={property.errors.materialType}
            disabled={property.usages > 0 || this.isLegacy(property)}
            value={property.materialType}
            options={options}
            onChange={this.handleChange}
            onFocus={this.handleFocus}
            onBlur={this.handleBlur}
          />
        </div>
      )
    } else {
      return null
    }
  }

  renderSchema(property) {
    if (property.dataType === dto.DataType.XML) {
      const { classes } = this.props
      return (
        <div className={classes.field}>
          <TextField
            reference={this.references.schema}
            label='XML Schema'
            name='schema'
            error={property.errors.schema}
            disabled={this.isLegacy(property)}
            value={property.schema}
            multiline={true}
            onChange={this.handleChange}
            onFocus={this.handleFocus}
            onBlur={this.handleBlur}
          />
        </div>
      )
    } else {
      return null
    }
  }

  renderTransformation(property) {
    if (property.dataType === dto.DataType.XML) {
      const { classes } = this.props
      return (
        <div className={classes.field}>
          <TextField
            reference={this.references.transformation}
            label='XSLT Script'
            name='transformation'
            error={property.errors.transformation}
            disabled={this.isLegacy(property)}
            value={property.transformation}
            multiline={true}
            onChange={this.handleChange}
            onFocus={this.handleFocus}
            onBlur={this.handleBlur}
          />
        </div>
      )
    } else {
      return null
    }
  }

  renderDynamicPlugin(property) {
    const { classes } = this.props
    const { dynamicPlugins = [] } = this.state

    let options = []

    if (dynamicPlugins.length > 0) {
      options = dynamicPlugins.map(dynamicPlugin => {
        return {
          label: dynamicPlugin.name,
          value: dynamicPlugin.name
        }
      })
      options.unshift({})
    }

    return (
      <div className={classes.field}>
        <SelectField
          reference={this.references.plugin}
          label='Dynamic Plugin'
          name='plugin'
          error={property.errors.plugin}
          disabled={property.usages > 0}
          value={property.plugin}
          options={options}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderMandatory(property) {
    const { classes } = this.props
    return (
      <div className={classes.field}>
        <CheckboxField
          reference={this.references.mandatory}
          label='Mandatory'
          name='mandatory'
          value={property.mandatory}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderInitialValue(property) {
    const { classes, type } = this.props

    const typeIsUsed = type.usages > 0
    const propertyIsNew = !property.original
    const propertyIsMandatory = property.mandatory
    const propertyWasMandatory = property.original
      ? property.original.mandatory
      : false

    if (
      typeIsUsed &&
      propertyIsMandatory &&
      (propertyIsNew || !propertyWasMandatory)
    ) {
      return (
        <div className={classes.field}>
          <TextField
            reference={this.references.initialValueForExistingEntities}
            label='Initial Value'
            name='initialValueForExistingEntities'
            mandatory={true}
            error={property.errors.initialValueForExistingEntities}
            value={property.initialValueForExistingEntities}
            onChange={this.handleChange}
            onFocus={this.handleFocus}
            onBlur={this.handleBlur}
          />
        </div>
      )
    } else {
      return null
    }
  }

  renderVisible(property) {
    const { classes } = this.props
    return (
      <div className={classes.field}>
        <CheckboxField
          reference={this.references.showInEditView}
          label='Visible'
          name='showInEditView'
          value={property.showInEditView}
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

    if (selection && selection.type === 'property') {
      let [property] = properties.filter(
        property => property.id === selection.params.id
      )
      return property
    } else {
      return null
    }
  }

  isLegacy(property) {
    return (
      property.original && !property.code.startsWith(this.props.type.code + '.')
    )
  }
}

export default withStyles(styles)(ObjectTypeParametersProperty)
