import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import autoBind from 'auto-bind'
import Link from '@material-ui/core/Link'
import logger from '@src/js/common/logger.js'
import messages from '@src/js/common/messages.js'
import Header from '@src/js/components/common/form/Header.jsx'
import Button from '@src/js/components/common/form/Button.jsx'
import Message from '@src/js/components/common/form/Message.jsx'
import Container from '@src/js/components/common/form/Container.jsx'
import ComponentContext from '@src/js/components/common/ComponentContext.js'
import ActiveUserReportFacade from '@src/js/components/tools/form/activeUserReport/ActiveUserReportFacade.js'
import ActiveUserReportController from '@src/js/components/tools/form/activeUserReport/ActiveUserReportController.js'
import AppController from '@src/js/components/AppController.js'

const styles = theme => ({
  message: {
    display: 'flex',
    paddingBottom: theme.spacing(1),
    paddingTop: theme.spacing(1)
  }
})

class ActiveUserReportForm extends React.PureComponent {
  constructor(props) {
    super(props)
    autoBind(this)

    this.state = {}
    if (this.props.controller) {
      this.controller = this.props.controller
    } else {
      this.controller = new ActiveUserReportController(
        new ActiveUserReportFacade()
      )
    }

    this.controller.init(new ComponentContext(this))
  }

  componentDidMount() {
    this.load()
  }

  async load() {
    try {
      await Promise.all([
        this.controller.loadActiveUsersCount(),
        this.controller.loadOpenbisSupportEmail()
      ])
      this.setState(() => ({
        loaded: true
      }))
    } catch (error) {
      AppController.getInstance().errorChange(error)
    }
  }

  render() {
    logger.log(logger.DEBUG, 'ActiveUserReportForm.render')
    if (this.state.activeUsersCount === undefined) {
      return <Container></Container>
    }

    const { loading } = this.state
    const { classes } = this.props

    return (
      <Container>
        <Header>{messages.get(messages.ACTIVE_USERS_REPORT)}</Header>
        <div className={classes.message}>
          <Message>
            {messages.get(
              messages.ACTIVE_USERS_REPORT_DIALOG,
              this.state.activeUsersCount
            )}
          </Message>
          <div>&nbsp;</div>
          {this.renderEmailLink()}
          {'. '}
        </div>
        <Button
          name='sendReport'
          disabled={loading}
          label={messages.get(messages.SEND_REPORT)}
          onClick={this.controller.sendReport}
          styles={{ root: classes.button }}
        />
        <div className={classes.message}>{this.renderResult()}</div>
      </Container>
    )
  }

  renderEmailLink() {
    if (
      this.state.openbisSupportEmail === undefined ||
      this.state.openbisSupportEmail === null
    ) {
      return <Message>{messages.get(messages.SUPPORT)}</Message>
    }
    let href = 'mailto:' + this.state.openbisSupportEmail
    return (
      <Message>
        <Link href={href} target='_blank'>
          {messages.get(messages.SUPPORT)}
        </Link>
      </Message>
    )
  }

  renderResult() {
    const { result } = this.state

    if (result === null || result === undefined) {
      return <Container></Container>
    }

    return (
      <Message type={result.success ? 'success' : 'error'}>
        {result.success
          ? messages.get(messages.ACTIVE_USERS_REPORT_EMAIL_SENT_CONFIRMATION)
          : result.output}
      </Message>
    )
  }
}

export default withStyles(styles)(ActiveUserReportForm)
