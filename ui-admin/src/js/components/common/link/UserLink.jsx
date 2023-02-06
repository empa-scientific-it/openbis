import React from 'react'
import LinkToObject from '@src/js/components/common/form/LinkToObject.jsx'
import pages from '@src/js/common/consts/pages.js'
import objectTypes from '@src/js/common/consts/objectType.js'
import logger from '@src/js/common/logger.js'

class UserLink extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'UserLink.render')

    const { userId } = this.props

    if (userId) {
      return (
        <LinkToObject
          page={pages.USERS}
          object={{
            type: objectTypes.USER,
            id: userId
          }}
        >
          {userId}
        </LinkToObject>
      )
    } else {
      return null
    }
  }
}

export default UserLink
