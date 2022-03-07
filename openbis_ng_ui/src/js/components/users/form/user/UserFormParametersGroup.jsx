import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Container from '@src/js/components/common/form/Container.jsx'
import Header from '@src/js/components/common/form/Header.jsx'
import SelectField from '@src/js/components/common/form/SelectField.jsx'
import Message from '@src/js/components/common/form/Message.jsx'
import UserFormSelectionType from '@src/js/components/users/form/user/UserFormSelectionType.js'
import messages from '@src/js/common/messages.js'
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
      code: React.createRef()
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
    this.props.onChange(UserFormSelectionType.GROUP, {
      id: group.id,
      field: event.target.name,
      value: event.target.value
    })
  }

  handleFocus(event) {
    const group = this.getGroup(this.props)
    this.props.onSelectionChange(UserFormSelectionType.GROUP, {
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
        <Header>{messages.get(messages.GROUP)}</Header>
        {this.renderMessageVisible()}
        {this.renderCode(group)}
      </Container>
    )
  }

  renderMessageVisible() {
    const { classes, selectedRow } = this.props

    if (selectedRow && !selectedRow.visible) {
      return (
        <div className={classes.field}>
          <Message type='warning'>
            {messages.get(
              messages.OBJECT_NOT_VISIBLE_DUE_TO_FILTERING_AND_PAGING
            )}
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

    const { mode, classes, controller } = this.props
    const { groups } = controller.getDictionaries()

    let options = []

    if (groups) {
      options = groups.map(group => {
        return {
          label: group.code,
          value: group.code
        }
      })
    }

    return (
      <div className={classes.field}>
        <SelectField
          reference={this.references.code}
          label={messages.get(messages.CODE)}
          name='code'
          error={error}
          disabled={!enabled}
          mandatory={true}
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

  getGroup(props) {
    let { groups, selection } = props

    if (selection && selection.type === UserFormSelectionType.GROUP) {
      let [group] = groups.filter(group => group.id === selection.params.id)
      return group
    } else {
      return null
    }
  }
}

export default withStyles(styles)(UserFormParametersGroup)
