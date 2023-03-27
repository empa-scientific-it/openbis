import React from 'react'
import Message from '@src/js/components/common/form/Message.jsx'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

class BrowserNodeTooManyResultsFound extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'BrowserNodeTooManyResultsFound.render')

    return (
      <Message type='warning'>
        {messages.get(messages.TOO_MANY_FILTERED_RESULTS_FOUND)}
      </Message>
    )
  }
}

export default BrowserNodeTooManyResultsFound
