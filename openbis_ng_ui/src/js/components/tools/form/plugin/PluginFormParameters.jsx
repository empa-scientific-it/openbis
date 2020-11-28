import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Container from '@src/js/components/common/form/Container.jsx'
import Header from '@src/js/components/common/form/Header.jsx'
import Message from '@src/js/components/common/form/Message.jsx'
import TextField from '@src/js/components/common/form/TextField.jsx'
import SelectField from '@src/js/components/common/form/SelectField.jsx'
import PluginFormSelectionType from '@src/js/components/tools/form/plugin/PluginFormSelectionType.js'
import openbis from '@src/js/services/openbis.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  field: {
    paddingBottom: theme.spacing(1)
  }
})

class PluginFormParameters extends React.PureComponent {
  constructor(props) {
    super(props)
    this.state = {}
    this.references = {
      name: React.createRef(),
      entityKind: React.createRef(),
      description: React.createRef()
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
    this.props.onChange(PluginFormSelectionType.PLUGIN, {
      field: event.target.name,
      value: event.target.value
    })
  }

  handleFocus(event) {
    this.props.onSelectionChange(PluginFormSelectionType.PLUGIN, {
      part: event.target.name
    })
  }

  handleBlur() {
    this.props.onBlur()
  }

  render() {
    logger.log(logger.DEBUG, 'PluginFormParameters.render')

    const { plugin } = this.props

    return (
      <Container>
        <Header>Plugin</Header>
        {this.renderMessageDisabled(plugin)}
        {this.renderMessagePredeployed(plugin)}
        {this.renderName(plugin)}
        {this.renderEntityKind(plugin)}
        {this.renderDescription(plugin)}
      </Container>
    )
  }

  renderMessageDisabled(plugin) {
    const { classes } = this.props

    if (!plugin.available.value) {
      return (
        <div className={classes.field}>
          <Message type='warning'>The plugin is disabled.</Message>
        </div>
      )
    } else {
      return null
    }
  }

  renderMessagePredeployed(plugin) {
    const { classes } = this.props

    if (plugin.pluginKind === openbis.PluginKind.PREDEPLOYED) {
      return (
        <div className={classes.field}>
          <Message type='info'>
            This is a predeployed Java plugin. Its parameters and logic are
            defined in the plugin Java class and therefore cannot be changed
            from the UI.
          </Message>
        </div>
      )
    } else {
      return null
    }
  }

  renderName(plugin) {
    const { visible, enabled, error, value } = { ...plugin.name }

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

  renderEntityKind(plugin) {
    const { visible, enabled, error, value } = { ...plugin.entityKind }

    if (!visible) {
      return null
    }

    const { mode, classes } = this.props

    const options = openbis.EntityKind.values.map(entityKind => {
      return {
        label: entityKind,
        value: entityKind
      }
    })

    return (
      <div className={classes.field}>
        <SelectField
          reference={this.references.entityKind}
          label='Entity Kind'
          name='entityKind'
          error={error}
          disabled={!enabled}
          value={value}
          options={options}
          emptyOption={{
            label: '(all)',
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

  renderDescription(plugin) {
    const { visible, enabled, error, value } = { ...plugin.description }

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
}

export default withStyles(styles)(PluginFormParameters)
