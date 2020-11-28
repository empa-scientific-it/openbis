import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Container from '@src/js/components/common/form/Container.jsx'
import Header from '@src/js/components/common/form/Header.jsx'
import TextField from '@src/js/components/common/form/TextField.jsx'
import SelectField from '@src/js/components/common/form/SelectField.jsx'
import CheckboxField from '@src/js/components/common/form/CheckboxField.jsx'
import AutocompleterField from '@src/js/components/common/form/AutocompleterField.jsx'
import QueryFormSelectionType from '@src/js/components/tools/form/query/QueryFormSelectionType.js'
import openbis from '@src/js/services/openbis.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  field: {
    paddingBottom: theme.spacing(1)
  }
})

class QueryFormParameters extends React.PureComponent {
  constructor(props) {
    super(props)
    this.state = {}
    this.references = {
      name: React.createRef(),
      description: React.createRef(),
      databaseId: React.createRef(),
      queryType: React.createRef(),
      entityTypeCodePattern: React.createRef(),
      publicFlag: React.createRef()
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
    this.props.onChange(QueryFormSelectionType.QUERY, {
      field: event.target.name,
      value: event.target.value
    })
  }

  handleFocus(event) {
    this.props.onSelectionChange(QueryFormSelectionType.QUERY, {
      part: event.target.name
    })
  }

  handleBlur() {
    this.props.onBlur()
  }

  render() {
    logger.log(logger.DEBUG, 'QueryFormParameters.render')

    const { query } = this.props

    return (
      <Container>
        <Header>Query</Header>
        {this.renderName(query)}
        {this.renderDescription(query)}
        {this.renderDatabaseId(query)}
        {this.renderQueryType(query)}
        {this.renderEntityTypeCodePattern(query)}
        {this.renderPublic(query)}
      </Container>
    )
  }

  renderName(query) {
    const { visible, enabled, error, value } = { ...query.name }

    if (!visible) {
      return null
    }

    const { mode, classes } = this.props
    return (
      <div className={classes.field}>
        <TextField
          reference={this.references.name}
          label='Name'
          name='name'
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

  renderDescription(query) {
    const { visible, enabled, error, value } = { ...query.description }

    if (!visible) {
      return null
    }

    const { mode, classes } = this.props
    return (
      <div className={classes.field}>
        <TextField
          reference={this.references.description}
          label='Description'
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

  renderDatabaseId(query) {
    const { visible, enabled, error, value } = { ...query.databaseId }

    if (!visible) {
      return null
    }

    const { mode, classes } = this.props
    return (
      <div className={classes.field}>
        <TextField
          reference={this.references.databaseId}
          label='Database'
          name='databaseId'
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

  renderQueryType(query) {
    const { visible, enabled, error, value } = { ...query.queryType }

    if (!visible) {
      return null
    }

    const { mode, classes } = this.props

    const options = [
      { value: openbis.QueryType.GENERIC },
      { value: openbis.QueryType.EXPERIMENT },
      { value: openbis.QueryType.SAMPLE },
      { value: openbis.QueryType.DATA_SET },
      { value: openbis.QueryType.MATERIAL }
    ]

    return (
      <div className={classes.field}>
        <SelectField
          reference={this.references.queryType}
          label='Query Type'
          name='queryType'
          mandatory={true}
          error={error}
          disabled={!enabled}
          value={value}
          options={options}
          sort={false}
          mode={mode}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderEntityTypeCodePattern(query) {
    const { visible, enabled, error, value } = {
      ...query.entityTypeCodePattern
    }

    if (!visible) {
      return null
    }

    const { dictionaries, mode, classes } = this.props
    const types = []

    if (query.queryType.value === openbis.QueryType.EXPERIMENT) {
      types.push(...dictionaries.experimentTypes)
    } else if (query.queryType.value === openbis.QueryType.SAMPLE) {
      types.push(...dictionaries.sampleTypes)
    } else if (query.queryType.value === openbis.QueryType.DATA_SET) {
      types.push(...dictionaries.dataSetTypes)
    } else if (query.queryType.value === openbis.QueryType.MATERIAL) {
      types.push(...dictionaries.materialTypes)
    }

    const options = types.map(type => {
      return type.code
    })

    return (
      <div className={classes.field}>
        <AutocompleterField
          reference={this.references.entityTypeCodePattern}
          label='Entity Type Pattern'
          name='entityTypeCodePattern'
          options={options}
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

  renderPublic(query) {
    const { visible, enabled, error, value } = { ...query.publicFlag }

    if (!visible) {
      return null
    }

    const { mode, classes } = this.props
    return (
      <div className={classes.field}>
        <CheckboxField
          reference={this.references.publicFlag}
          label='Public'
          name='publicFlag'
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

export default withStyles(styles)(QueryFormParameters)
