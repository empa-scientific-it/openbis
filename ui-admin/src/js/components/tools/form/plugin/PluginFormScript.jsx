import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Container from '@src/js/components/common/form/Container.jsx'
import Header from '@src/js/components/common/form/Header.jsx'
import SourceCodeField from '@src/js/components/common/form/SourceCodeField.jsx'
import PluginFormSelectionType from '@src/js/components/tools/form/plugin/PluginFormSelectionType.js'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

const styles = () => ({})

class PluginFormScript extends React.PureComponent {
  constructor(props) {
    super(props)
    this.state = {}
    this.references = {
      script: React.createRef()
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
    logger.log(logger.DEBUG, 'PluginFormScript.render')

    const { plugin } = this.props

    return (
      <Container>
        <Header>{messages.get(messages.SCRIPT)}</Header>
        {this.renderScript(plugin)}
      </Container>
    )
  }

  renderScript(plugin) {
    const { visible, enabled, error, value } = { ...plugin.script }

    if (!visible) {
      return null
    }

    const { mode, classes } = this.props
    return (
      <div className={classes.field}>
        <SourceCodeField
          reference={this.references.script}
          language='python'
          label={messages.get(messages.SCRIPT)}
          name='script'
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
}

export default withStyles(styles)(PluginFormScript)
