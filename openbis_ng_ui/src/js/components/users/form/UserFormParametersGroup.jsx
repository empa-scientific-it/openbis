import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Container from '@src/js/components/common/form/Container.jsx'
import Header from '@src/js/components/common/form/Header.jsx'
import TextField from '@src/js/components/common/form/TextField.jsx'
import Message from '@src/js/components/common/form/Message.jsx'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  field: {
    paddingBottom: theme.spacing(1)
  }
})

class UserFormParametersGroup extends React.PureComponent {
  constructor(props) {
    super(props)
    this.state = {}
    this.references = {
      code: React.createRef(),
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
    const group = this.getGroup(this.props)
    if (group && this.props.selection) {
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
    const group = this.getGroup(this.props)
    this.props.onChange('group', {
      id: group.id,
      field: event.target.name,
      value: event.target.value
    })
  }

  handleFocus(event) {
    const group = this.getGroup(this.props)
    this.props.onSelectionChange('group', {
      id: group.id,
      part: event.target.name
    })
  }

  handleBlur() {
    this.props.onBlur()
  }

  render() {
    logger.log(logger.DEBUG, 'UserFormParametersGroup.render')

    const group = this.getGroup(this.props)
    if (!group) {
      return null
    }

    return (
      <Container>
        <Header>Group</Header>
        {this.renderMessageVisible()}
        {this.renderCode(group)}
        {this.renderDescription(group)}
      </Container>
    )
  }

  renderMessageVisible() {
    const { classes, selectedRow } = this.props

    if (selectedRow && !selectedRow.visible) {
      return (
        <div className={classes.field}>
          <Message type='warning'>
            The selected group is currently not visible in the group list due to
            the chosen filtering and paging.
          </Message>
        </div>
      )
    } else {
      return null
    }
  }

  renderCode(group) {
    const { visible, enabled, error, value } = { ...group.code }

    if (!visible) {
      return null
    }

    const { mode, classes } = this.props
    return (
      <div className={classes.field}>
        <TextField
          reference={this.references.code}
          label='Code'
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

  renderDescription(group) {
    const { visible, enabled, error, value } = { ...group.description }

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

  getGroup(props) {
    let { groups, selection } = props

    if (selection && selection.type === 'group') {
      let [group] = groups.filter(group => group.id === selection.params.id)
      return group
    } else {
      return null
    }
  }
}

export default withStyles(styles)(UserFormParametersGroup)
