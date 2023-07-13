import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import AppController from '@src/js/components/AppController.js'
import Container from '@src/js/components/common/form/Container.jsx'
import Header from '@src/js/components/common/form/Header.jsx'
import TextField from '@src/js/components/common/form/TextField.jsx'
import CheckboxField from '@src/js/components/common/form/CheckboxField.jsx'
import Message from '@src/js/components/common/form/Message.jsx'
import VocabularyTypeFormSelectionType from '@src/js/components/types/form/vocabularytype/VocabularyTypeFormSelectionType.js'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  field: {
    paddingBottom: theme.spacing(1)
  }
})

class VocabularyTypeFormParametersVocabulary extends React.PureComponent {
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
    this.props.onChange(VocabularyTypeFormSelectionType.VOCABULARY, {
      field: event.target.name,
      value: event.target.value
    })
  }

  handleFocus(event) {
    this.props.onSelectionChange(VocabularyTypeFormSelectionType.VOCABULARY, {
      part: event.target.name
    })
  }

  handleBlur() {
    this.props.onBlur()
  }

  render() {
    logger.log(logger.DEBUG, 'VocabularyTypeFormParametersVocabulary.render')

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
    const { classes } = this.props

    if (vocabulary.internal.value) {
      if (AppController.getInstance().isSystemUser()) {
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
          description={messages.get(messages.URL_TEMPLATE_HINT)}
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
      selection.type === VocabularyTypeFormSelectionType.VOCABULARY
    ) {
      return vocabulary
    } else {
      return null
    }
  }
}

export default withStyles(styles)(VocabularyTypeFormParametersVocabulary)
