import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Message from '@src/js/components/common/form/Message.jsx'
import Button from '@src/js/components/common/form/Button.jsx'
import Dialog from '@src/js/components/common/dialog/Dialog.jsx'
import Link from '@material-ui/core/Link'
import Collapse from '@material-ui/core/Collapse'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  button: {
    marginLeft: theme.spacing(1)
  },
  content: {
    whiteSpace: 'pre'
  },
  stackContainer: {
    marginTop: theme.spacing(1)
  },
  stackLink: {
    cursor: 'pointer'
  },
  stackContent: {
    marginTop: theme.spacing(1),
    marginBottom: theme.spacing(1)
  }
})

class ErrorDialog extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      stackVisible: false
    }
    this.handleStackVisibleChange = this.handleStackVisibleChange.bind(this)
  }

  handleStackVisibleChange() {
    this.setState(state => ({
      stackVisible: !state.stackVisible
    }))
  }

  render() {
    logger.log(logger.DEBUG, 'ErrorDialog.render')

    const { error, onClose } = this.props

    return (
      <Dialog
        open={!!error}
        onClose={onClose}
        title={messages.get(messages.ERROR)}
        content={this.renderContent()}
        actions={this.renderButtons()}
      />
    )
  }

  renderContent() {
    const { classes } = this.props

    const message = this.getErrorMessage()

    return (
      <div className={classes.content}>
        <Message type='error'>
          <div>
            {message && <div>{message}</div>}
            {this.renderStack()}
          </div>
        </Message>
      </div>
    )
  }

  renderStack() {
    const stack = this.getErrorStack()

    if (!stack) {
      return null
    }

    const { classes } = this.props
    const { stackVisible } = this.state

    return (
      <div className={classes.stackContainer}>
        <Link
          onClick={this.handleStackVisibleChange}
          className={classes.stackLink}
        >
          {stackVisible
            ? messages.get(messages.HIDE_STACK_TRACE)
            : messages.get(messages.SHOW_STACK_TRACE)}
        </Link>
        <Collapse in={stackVisible} mountOnEnter={true} unmountOnExit={true}>
          <pre className={classes.stackContent}>{stack}</pre>
        </Collapse>
      </div>
    )
  }

  renderButtons() {
    const { onClose, classes } = this.props
    return (
      <div>
        <Button
          label={messages.get(messages.CLOSE)}
          styles={{ root: classes.button }}
          onClick={onClose}
        />
      </div>
    )
  }

  getErrorMessage() {
    const { error } = this.props

    if (error) {
      if (error.message) {
        return error.message
      } else {
        return error
      }
    } else {
      return null
    }
  }

  getErrorStack() {
    const { error } = this.props

    if (error && error.data && error.data.stackTrace) {
      return error.data.stackTrace
    } else {
      return null
    }
  }
}

export default withStyles(styles)(ErrorDialog)
