import React from 'react'
import UsersGrid from '@src/js/components/users/common/UsersGrid.jsx'
import ids from '@src/js/common/consts/ids.js'
import logger from '@src/js/common/logger.js'

export default class UserGroupFormGridUsers extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'UserGroupFormGridUsers.render')

    return <UsersGrid {...this.props} id={ids.USERS_OF_GROUP_GRID_ID} />
  }
}
