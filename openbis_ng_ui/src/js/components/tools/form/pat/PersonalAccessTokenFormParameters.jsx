import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Container from '@src/js/components/common/form/Container.jsx'
import Header from '@src/js/components/common/form/Header.jsx'
import TextField from '@src/js/components/common/form/TextField.jsx'
import SelectField from '@src/js/components/common/form/SelectField.jsx'
import DateField from '@src/js/components/common/form/DateField.jsx'
import Message from '@src/js/components/common/form/Message.jsx'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  field: {
    paddingBottom: theme.spacing(1)
  }
})

class PersonalAccessTokenFormParameters extends React.PureComponent {
  constructor(props) {
    super(props)
    this.state = {}
    this.references = {
      hash: React.createRef(),
      sessionName: React.createRef(),
      validFromDate: React.createRef(),
      validToDate: React.createRef(),
      owner: React.createRef()
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
    const pat = this.getPat(this.props)
    if (pat && this.props.selection) {
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
    const pat = this.getPat(this.props)
    this.props.onChange({
      id: pat.id,
      field: event.target.name,
      value: event.target.value
    })
  }

  handleFocus(event) {
    const pat = this.getPat(this.props)
    this.props.onSelectionChange({
      id: pat.id,
      part: event.target.name
    })
  }

  handleBlur() {
    this.props.onBlur()
  }

  render() {
    logger.log(logger.DEBUG, 'PersonalAccessTokenFormParameters.render')

    const pat = this.getPat(this.props)
    if (!pat) {
      return null
    }

    return (
      <Container>
        <Header>{messages.get(messages.PERSONAL_ACCESS_TOKEN)}</Header>
        {this.renderMessageVisible(pat)}
        {this.renderOwner(pat)}
        {this.renderSessionName(pat)}
        {this.renderHash(pat)}
        {this.renderValidFromDate(pat)}
        {this.renderValidToDate(pat)}
        {this.renderRegistrator(pat)}
        {this.renderRegistrationDate(pat)}
        {this.renderAccessDate(pat)}
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

  renderHash(pat) {
    const { visible, enabled, error, value } = { ...pat.hash }

    if (!visible) {
      return null
    }

    const { mode, classes } = this.props
    return (
      <div className={classes.field}>
        <TextField
          reference={this.references.hash}
          label={messages.get(messages.HASH)}
          name='hash'
          mandatory={false}
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

  renderOwner(pat) {
    const { visible, enabled, error, value } = { ...pat.owner }

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
          reference={this.references.owner}
          label={messages.get(messages.OWNER)}
          name='owner'
          mandatory={true}
          error={error}
          disabled={!enabled}
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

  renderSessionName(pat) {
    const { visible, enabled, error, value } = { ...pat.sessionName }

    if (!visible) {
      return null
    }

    const { mode, classes } = this.props
    return (
      <div className={classes.field}>
        <TextField
          reference={this.references.sessionName}
          label={messages.get(messages.SESSION_NAME)}
          name='sessionName'
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

  renderValidFromDate(pat) {
    const { visible, enabled, error, value } = { ...pat.validFromDate }

    if (!visible) {
      return null
    }

    const { mode, classes } = this.props
    return (
      <div className={classes.field}>
        <DateField
          reference={this.references.validFromDate}
          label={messages.get(messages.VALID_FROM)}
          name='validFromDate'
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

  renderValidToDate(pat) {
    const { visible, enabled, error, value } = { ...pat.validToDate }

    if (!visible) {
      return null
    }

    const { mode, classes } = this.props
    return (
      <div className={classes.field}>
        <DateField
          reference={this.references.validToDate}
          label={messages.get(messages.VALID_TO)}
          name='validToDate'
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

  renderRegistrator(pat) {
    const { visible, enabled, error, value } = { ...pat.registrator }

    if (!visible) {
      return null
    }

    const { mode, classes } = this.props
    return (
      <div className={classes.field}>
        <TextField
          reference={this.references.registrator}
          label={messages.get(messages.REGISTRATOR)}
          name='registrator'
          mandatory={false}
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

  renderRegistrationDate(pat) {
    const { visible, enabled, error, value } = { ...pat.registrationDate }

    if (!visible) {
      return null
    }

    const { mode, classes } = this.props
    return (
      <div className={classes.field}>
        <DateField
          reference={this.references.registrationDate}
          label={messages.get(messages.REGISTRATION_DATE)}
          name='registrationDate'
          mandatory={false}
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

  renderAccessDate(pat) {
    const { visible, enabled, error, value } = { ...pat.accessDate }

    if (!visible) {
      return null
    }

    const { mode, classes } = this.props
    return (
      <div className={classes.field}>
        <DateField
          reference={this.references.accessDate}
          label={messages.get(messages.ACCESS_DATE)}
          name='accessDate'
          mandatory={false}
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

  getPat(props) {
    let { pats, selection } = props

    if (selection) {
      let [pat] = pats.filter(pat => pat.id === selection.params.id)
      return pat
    } else {
      return null
    }
  }
}

export default withStyles(styles)(PersonalAccessTokenFormParameters)
