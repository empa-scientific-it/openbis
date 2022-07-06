import React from 'react'
import Link from '@material-ui/core/Link'
import logger from '@src/js/common/logger.js'
import messages from '@src/js/common/messages.js'
import Header from '@src/js/components/common/form/Header.jsx'
import Container from '@src/js/components/common/form/Container.jsx'
import Button from '@src/js/components/common/form/Button.jsx'

class ActiveUserReportForm extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'ActiveUserReportForm.render')

    const { activeUsersCount, classes } = this.props

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
        <Button name='sendReport' label={messages.get(messages.SEND_REPORT)} styles={{ root: classes.button }} onClick={this.sendReport}/>
      </Container>
    )
  }

  sendReport() {
  }
}

export default ActiveUserReportForm
