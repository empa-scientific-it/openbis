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
      this.controller = new ActiveUserReportController(new ActiveUserReportFacade())
    }

    this.controller.init(new ComponentContext(this))
  }

  render() {
    logger.log(logger.DEBUG, 'ActiveUserReportForm.render')

    const { activeUsersCount, classes } = this.props
    const { result } = this.state

    return (
      <Container>
        <Header>{messages.get(messages.ACTIVE_USERS_REPORT)}</Header>
        <div className={classes.message}>
          <Message>{messages.get(messages.ACTIVE_USERS_REPORT_DIALOG, activeUsersCount)}</Message>
          <div>&nbsp;</div>
          <Link href="mailto:cisd.helpdesk@bsse.ethz.ch" target="_blank">{messages.get(messages.HELPDESK)}</Link>{". "}
        </div>
        <Button name='sendReport' label={messages.get(messages.SEND_REPORT)} onClick={this.controller.sendReport} styles={{ root: classes.button }}/>
        <div className={classes.message}>
          {this.renderResult()}
        </div>
      </Container>
    )
  }

  renderResult() {
    const { result } = this.state

    if (result === undefined) {
      return <Container></Container>
    }

    return (
      <Message type={result.success ? 'success' : 'error'}>
        {result.success ? messages.get(messages.ACTIVE_USERS_REPORT_EMAIL_SENT_CONFIRMATION) : result.output}
      </Message>
    )
  }
}

export default withStyles(styles)(ActiveUserReportForm)
