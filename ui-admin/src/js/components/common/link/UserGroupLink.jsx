import React from 'react'
import LinkToObject from '@src/js/components/common/form/LinkToObject.jsx'
import pages from '@src/js/common/consts/pages.js'
import objectTypes from '@src/js/common/consts/objectType.js'
import logger from '@src/js/common/logger.js'

class UserGroupLink extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'UserGroupLink.render')

    const { groupCode } = this.props

    if (groupCode) {
      return (
        <LinkToObject
          page={pages.USERS}
          object={{
            type: objectTypes.USER_GROUP,
            id: groupCode
          }}
        >
          {groupCode}
        </LinkToObject>
      )
    } else {
      return null
    }
  }
}

export default UserGroupLink
