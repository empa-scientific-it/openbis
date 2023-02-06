import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Container from '@src/js/components/common/form/Container.jsx'
import Header from '@src/js/components/common/form/Header.jsx'
import Message from '@src/js/components/common/form/Message.jsx'
import TextField from '@src/js/components/common/form/TextField.jsx'
import SelectField from '@src/js/components/common/form/SelectField.jsx'
import PluginFormSelectionType from '@src/js/components/tools/form/plugin/PluginFormSelectionType.js'
import EntityKind from '@src/js/components/common/dto/EntityKind.js'
import openbis from '@src/js/services/openbis.js'
import messages from '@src/js/common/messages.js'
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
        {this.renderHeader(plugin)}
        {this.renderMessageDisabled(plugin)}
        {this.renderMessagePredeployed(plugin)}
        {this.renderName(plugin)}
        {this.renderEntityKind(plugin)}
        {this.renderDescription(plugin)}
      </Container>
    )
  }

  renderHeader(plugin) {
    let message = null

    if (plugin.pluginType === openbis.PluginType.DYNAMIC_PROPERTY) {
      message = plugin.original
        ? messages.DYNAMIC_PROPERTY_PLUGIN
        : messages.NEW_DYNAMIC_PROPERTY_PLUGIN
    } else if (plugin.pluginType === openbis.PluginType.ENTITY_VALIDATION) {
      message = plugin.original
        ? messages.ENTITY_VALIDATION_PLUGIN
        : messages.NEW_ENTITY_VALIDATION_PLUGIN
    }

    return <Header>{messages.get(message)}</Header>
  }

  renderMessageDisabled(plugin) {
    const { classes } = this.props

    if (!plugin.available.value) {
      return (
        <div className={classes.field}>
          <Message type='warning'>
            {messages.get(messages.PLUGIN_IS_DISABLED)}
          </Message>
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
            {messages.get(messages.PLUGIN_IS_PREDEPLOYED)}
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
          label={messages.get(messages.NAME)}
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
        label: new EntityKind(entityKind).getLabel(),
        value: entityKind
      }
    })

    return (
      <div className={classes.field}>
        <SelectField
          reference={this.references.entityKind}
          label={messages.get(messages.ENTITY_KIND)}
          name='entityKind'
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
}

export default withStyles(styles)(PluginFormParameters)
