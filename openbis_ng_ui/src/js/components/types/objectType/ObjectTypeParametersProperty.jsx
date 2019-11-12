import _ from 'lodash'
import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Typography from '@material-ui/core/Typography'
import FormControlLabel from '@material-ui/core/FormControlLabel'
import Checkbox from '@material-ui/core/Checkbox'
import TextField from '@material-ui/core/TextField'
import { facade, dto } from '../../../services/openbis.js'
import logger from '../../../common/logger.js'

const styles = theme => ({
  container: {
    padding: theme.spacing(2)
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
      mandatory: React.createRef()
    }
    this.actions = {}
    this.handleChange = this.handleChange.bind(this)
    this.handleFocus = this.handleFocus.bind(this)
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

      if (dataType === 'CONTROLLEDVOCABULARY') {
        this.loadVocabularies()
      } else if (dataType === 'MATERIAL') {
        this.loadMaterialTypes()
      }
    }
  }

  loadVocabularies() {
    let criteria = new dto.VocabularySearchCriteria()
    let fo = new dto.VocabularyFetchOptions()

    return facade.searchVocabularies(criteria, fo).then(result => {
      this.setState(() => ({
        vocabularies: result.objects
      }))
    })
  }

  loadMaterialTypes() {
    let criteria = new dto.MaterialTypeSearchCriteria()
    let fo = new dto.MaterialTypeFetchOptions()

    return facade.searchMaterialTypes(criteria, fo).then(result => {
      this.setState(() => ({
        materialTypes: result.objects
      }))
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
        const action = this.actions[part]
        if (action) {
          action.focusVisible()
        }
      }
    }
  }

  handleChange(event) {
    const property = this.getProperty(this.props)

    let params = null

    if (_.has(event.target, 'checked')) {
      params = {
        id: property.id,
        field: event.target.value,
        value: event.target.checked
      }
    } else {
      params = {
        id: property.id,
        field: event.target.name,
        value: event.target.value
      }
    }

    this.props.onChange('property', params)
  }

  handleFocus(event) {
    const property = this.getProperty(this.props)

    let params = null

    if (_.has(event.target, 'checked')) {
      params = {
        id: property.id,
        part: event.target.value
      }
    } else {
      params = {
        id: property.id,
        part: event.target.name
      }
    }

    this.props.onSelectionChange('property', params)
  }

  render() {
    logger.log(logger.DEBUG, 'ObjectTypeParametersProperty.render')

    let property = this.getProperty(this.props)
    if (!property) {
      return null
    }

    let { classes } = this.props
    let { vocabularies, materialTypes } = this.state

    return (
      <div className={classes.container}>
        <Typography variant='h6'>Property</Typography>
        <form>
          <div>
            <TextField
              inputRef={this.references.label}
              label='Label'
              name='label'
              value={property.label}
              fullWidth={true}
              margin='normal'
              variant='filled'
              InputLabelProps={{
                shrink: true
              }}
              onChange={this.handleChange}
              onFocus={this.handleFocus}
            />
          </div>
          <div>
            <TextField
              inputRef={this.references.code}
              label='Code'
              name='code'
              value={property.code}
              fullWidth={true}
              margin='normal'
              variant='filled'
              InputLabelProps={{
                shrink: true
              }}
              onChange={this.handleChange}
              onFocus={this.handleFocus}
            />
          </div>
          <div>
            <TextField
              inputRef={this.references.description}
              label='Description'
              name='description'
              value={property.description}
              fullWidth={true}
              margin='normal'
              variant='filled'
              InputLabelProps={{
                shrink: true
              }}
              onChange={this.handleChange}
              onFocus={this.handleFocus}
            />
          </div>
          <div>
            <TextField
              select
              inputRef={this.references.dataType}
              label='Data Type'
              name='dataType'
              SelectProps={{
                native: true
              }}
              value={property.dataType}
              fullWidth={true}
              margin='normal'
              variant='filled'
              InputLabelProps={{
                shrink: true
              }}
              onChange={this.handleChange}
              onFocus={this.handleFocus}
            >
              {dto.DataType.values.sort().map(dataType => {
                return (
                  <option key={dataType} value={dataType}>
                    {dataType}
                  </option>
                )
              })}
            </TextField>
          </div>
          {property.dataType === 'CONTROLLEDVOCABULARY' && (
            <div>
              <TextField
                select
                label='Vocabulary'
                name='vocabulary'
                SelectProps={{
                  native: true
                }}
                value={property.vocabulary ? property.vocabulary : ''}
                fullWidth={true}
                margin='normal'
                variant='filled'
                InputLabelProps={{
                  shrink: true
                }}
                onChange={this.handleChange}
                onFocus={this.handleFocus}
              >
                <option key='' value=''></option>
                {vocabularies &&
                  vocabularies.map(vocabulary => {
                    return (
                      <option key={vocabulary.code} value={vocabulary.code}>
                        {vocabulary.code}
                      </option>
                    )
                  })}
              </TextField>
            </div>
          )}
          {property.dataType === 'MATERIAL' && (
            <div>
              <TextField
                select
                label='Material Type'
                name='materialType'
                SelectProps={{
                  native: true
                }}
                value={property.materialType ? property.materialType : ''}
                fullWidth={true}
                margin='normal'
                variant='filled'
                InputLabelProps={{
                  shrink: true
                }}
                onChange={this.handleChange}
                onFocus={this.handleFocus}
              >
                <option key='' value=''></option>
                {materialTypes &&
                  materialTypes.map(materialType => {
                    return (
                      <option key={materialType.code} value={materialType.code}>
                        {materialType.code}
                      </option>
                    )
                  })}
              </TextField>
            </div>
          )}
          <div>
            <FormControlLabel
              control={
                <Checkbox
                  inputRef={this.references.mandatory}
                  action={actions => {
                    this.actions.mandatory = actions
                  }}
                  value='mandatory'
                  checked={property.mandatory}
                  onChange={this.handleChange}
                  onFocus={this.handleFocus}
                />
              }
              label='Mandatory'
            />
          </div>
          <div>
            <FormControlLabel
              control={
                <Checkbox
                  value='visible'
                  checked={property.visible}
                  onChange={this.handleChange}
                  onFocus={this.handleFocus}
                />
              }
              label='Visible'
            />
          </div>
        </form>
      </div>
    )
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
}

export default withStyles(styles)(ObjectTypeParametersProperty)
