import React from 'react'
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
        <span>
          {messages.get(messages.ACTIVE_USERS_REPORT_DIALOG, activeUsersCount)}
          {" "}
          <Link href="mailto:cisd.helpdesk@bsse.ethz.ch" target="_blank">{messages.get(messages.HELPDESK)}</Link>
          {". "}
          <br/><br/>
        </span>
        <Button name='sendReport' label={messages.get(messages.SEND_REPORT)} styles={{ root: classes.button }} onClick={this.controller.sendReport}/>
        <br/>
        {this.renderResult()}
      </Container>
    )
  }

  renderResult() {
    const { result } = this.state

    if (result === undefined) {
      return <Container></Container>
    }

    return (
      <Container>
        <Message type={result.success ? 'success' : 'error'}>
          {result.success ? messages.get(messages.ACTIVE_USERS_REPORT_EMAIL_SENT_CONFIRMATION) : result.output}
        </Message>
      </Container>
    )
  }
}

export default ActiveUserReportForm
