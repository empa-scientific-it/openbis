import _ from 'lodash'
import React from 'react'
import { connect } from 'react-redux'
import { withStyles } from '@material-ui/core/styles'
import Container from '@src/js/components/common/form/Container.jsx'
import Header from '@src/js/components/common/form/Header.jsx'
import TextField from '@src/js/components/common/form/TextField.jsx'
import CheckboxField from '@src/js/components/common/form/CheckboxField.jsx'
import Message from '@src/js/components/common/form/Message.jsx'
import VocabularyFormSelectionType from '@src/js/components/types/form/VocabularyFormSelectionType.js'
import selectors from '@src/js/store/selectors/selectors.js'
import users from '@src/js/common/consts/users.js'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'
const styles = theme => ({
  field: {
    paddingBottom: theme.spacing(1)
  }
})

function mapStateToProps(state) {
  return {
    session: selectors.getSession(state)
  }
}

class VocabularyFormParametersVocabulary extends React.PureComponent {
  constructor(props) {
    super(props)
    this.state = {}
    this.references = {
      code: React.createRef(),
      description: React.createRef(),
      internal: React.createRef(),
      urlTemplate: React.createRef()
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
    const vocabulary = this.getVocabulary(this.props)
    if (vocabulary && this.props.selection) {
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
    this.props.onChange(VocabularyFormSelectionType.VOCABULARY, {
      field: event.target.name,
      value: event.target.value
    })
  }

  handleFocus(event) {
    this.props.onSelectionChange(VocabularyFormSelectionType.VOCABULARY, {
      part: event.target.name
    })
  }

  handleBlur() {
    this.props.onBlur()
  }

  render() {
    logger.log(logger.DEBUG, 'VocabularyFormParametersVocabulary.render')

    const vocabulary = this.getVocabulary(this.props)
    if (!vocabulary) {
      return null
    }

    return (
      <Container>
        {this.renderHeader(vocabulary)}
        {this.renderMessageInternal(vocabulary)}
        {this.renderCode(vocabulary)}
        {this.renderDescription(vocabulary)}
        {this.renderUrlTemplate(vocabulary)}
        {this.renderInternal(vocabulary)}
      </Container>
    )
  }

  renderHeader(vocabulary) {
    const message = vocabulary.original
      ? messages.VOCABULARY_TYPE
      : messages.NEW_VOCABULARY_TYPE
    return <Header>{messages.get(message)}</Header>
  }

  renderMessageInternal(vocabulary) {
    const { classes, session } = this.props

    if (vocabulary.internal.value) {
      if (session && session.userName === users.SYSTEM) {
        return (
          <div className={classes.field}>
            <Message type='lock'>
              {messages.get(messages.VOCABULARY_TYPE_IS_INTERNAL)}
            </Message>
          </div>
        )
      } else {
        return (
          <div className={classes.field}>
            <Message type='lock'>
              {messages.get(messages.VOCABULARY_TYPE_IS_INTERNAL)}{' '}
              {messages.get(
                messages.VOCABULARY_TYPE_CANNOT_BE_CHANGED_OR_REMOVED
              )}
            </Message>
          </div>
        )
      }
    } else {
      return null
    }
  }

  renderCode(vocabulary) {
    const { visible, enabled, error, value } = { ...vocabulary.code }

    if (!visible) {
      return null
    }

    const { mode, classes } = this.props
    return (
      <div className={classes.field}>
        <TextField
          reference={this.references.code}
          label={messages.get(messages.CODE)}
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

  renderDescription(vocabulary) {
    const { visible, enabled, error, value } = { ...vocabulary.description }

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

  renderInternal(vocabulary) {
    const { visible, enabled, error, value } = { ...vocabulary.internal }

    if (!visible) {
      return null
    }

    const { mode, classes } = this.props
    return (
      <div className={classes.field}>
        <CheckboxField
          reference={this.references.internal}
          label={messages.get(messages.INTERNAL)}
          name='internal'
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

  renderUrlTemplate(vocabulary) {
    const { visible, enabled, error, value } = { ...vocabulary.urlTemplate }

    if (!visible) {
      return null
    }

    const { mode, classes } = this.props
    return (
      <div className={classes.field}>
        <TextField
          reference={this.references.urlTemplate}
          label={messages.get(messages.URL_TEMPLATE)}
          name='urlTemplate'
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

  getVocabulary(props) {
    let { vocabulary, selection } = props

    if (
      !selection ||
      selection.type === VocabularyFormSelectionType.VOCABULARY
    ) {
      return vocabulary
    } else {
      return null
    }
  }
}

export default _.flow(
  connect(mapStateToProps),
  withStyles(styles)
)(VocabularyFormParametersVocabulary)
