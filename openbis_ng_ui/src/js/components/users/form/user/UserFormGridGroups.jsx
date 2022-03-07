import React from 'react'
import UserGroupsGrid from '@src/js/components/users/common/UserGroupsGrid.jsx'
import ids from '@src/js/common/consts/ids.js'
import logger from '@src/js/common/logger.js'

export default class UserFormGridGroups extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'UserFormGridGroups.render')

    return <UserGroupsGrid {...this.props} id={ids.GROUPS_OF_USER_GRID_ID} />
  }
}
