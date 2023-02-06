import React from 'react'
import logger from '@src/js/common/logger.js'

class ContentTab extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'ContentTab.render')

    const { label, changed } = this.props

    return label + (changed ? '*' : '')
  }
}

export default ContentTab
