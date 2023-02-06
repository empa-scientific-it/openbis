import React from 'react'
import LinkToObject from '@src/js/components/common/form/LinkToObject.jsx'
import pages from '@src/js/common/consts/pages.js'
import objectTypes from '@src/js/common/consts/objectType.js'
import logger from '@src/js/common/logger.js'

class QueryLink extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'QueryLink.render')

    const { queryName } = this.props

    if (queryName) {
      return (
        <LinkToObject
          page={pages.TOOLS}
          object={{
            type: objectTypes.QUERY,
            id: queryName
          }}
        >
          {queryName}
        </LinkToObject>
      )
    } else {
      return null
    }
  }
}

export default QueryLink
