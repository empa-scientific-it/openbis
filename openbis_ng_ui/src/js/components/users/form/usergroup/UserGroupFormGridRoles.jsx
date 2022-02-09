import React from 'react'
import RolesGrid from '@src/js/components/users/common/RolesGrid.jsx'
import ids from '@src/js/common/consts/ids.js'
import logger from '@src/js/common/logger.js'

class UserGroupFormGridRoles extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'UserGroupFormGridRoles.render')

    return <RolesGrid {...this.props} id={ids.ROLES_OF_GROUP_GRID_ID} />
  }
}

export default UserGroupFormGridRoles
