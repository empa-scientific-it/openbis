import _ from 'lodash'
import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Message from '@src/js/components/common/form/Message.jsx'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  root: {
    color: theme.typography.label.color,
    fontStyle: 'italic'
  }
})

class BrowserNodeLoadMore extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'BrowserNodeLoadMore.render')

    const { count, classes } = this.props

    return (
      <Message styles={{ root: classes.root }}>
        {_.isNil(count)
          ? messages.get(messages.LOAD_MORE)
          : messages.get(messages.LOAD_MORE_COUNT, count)}
      </Message>
    )
  }
}

export default withStyles(styles)(BrowserNodeLoadMore)
