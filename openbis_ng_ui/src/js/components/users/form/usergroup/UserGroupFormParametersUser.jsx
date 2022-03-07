import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Container from '@src/js/components/common/form/Container.jsx'
import Header from '@src/js/components/common/form/Header.jsx'
import SelectField from '@src/js/components/common/form/SelectField.jsx'
import Message from '@src/js/components/common/form/Message.jsx'
import UserGroupFormSelectionType from '@src/js/components/users/form/usergroup/UserGroupFormSelectionType.js'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  field: {
    paddingBottom: theme.spacing(1)
  }
})

class UserGroupFormParametersUser extends React.PureComponent {
  constructor(props) {
    super(props)
    this.state = {}
    this.references = {
      userId: React.createRef()
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
    const user = this.getUser(this.props)
    if (user && this.props.selection) {
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
    const user = this.getUser(this.props)
    this.props.onChange(UserGroupFormSelectionType.USER, {
      id: user.id,
      field: event.target.name,
      value: event.target.value
    })
  }

  handleFocus(event) {
    const user = this.getUser(this.props)
    this.props.onSelectionChange(UserGroupFormSelectionType.USER, {
      id: user.id,
      part: event.target.name
    })
  }

  handleBlur() {
    this.props.onBlur()
  }

  render() {
    logger.log(logger.DEBUG, 'UserGroupFormParametersUser.render')

    const user = this.getUser(this.props)
    if (!user) {
      return null
    }

    return (
      <Container>
        <Header>{messages.get(messages.USER)}</Header>
        {this.renderMessageVisible()}
        {this.renderUserId(user)}
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

  renderUserId(user) {
    const { visible, enabled, error, value } = { ...user.userId }

    if (!visible) {
      return null
    }

    const { mode, classes, controller } = this.props
    const { users } = controller.getDictionaries()

    let options = []

    if (users) {
      options = users.map(user => {
        return {
          label: user.userId,
          value: user.userId
        }
      })
    }

    return (
      <div className={classes.field}>
        <SelectField
          reference={this.references.userId}
          label={messages.get(messages.USER_ID)}
          name='userId'
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

  getUser(props) {
    let { users, selection } = props

    if (selection && selection.type === UserGroupFormSelectionType.USER) {
      let [user] = users.filter(user => user.id === selection.params.id)
      return user
    } else {
      return null
    }
  }
}

export default withStyles(styles)(UserGroupFormParametersUser)
