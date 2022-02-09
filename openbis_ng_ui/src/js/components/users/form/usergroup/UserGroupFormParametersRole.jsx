import React from 'react'
import RoleParameters from '@src/js/components/users/form/common/RoleParameters.jsx'
import logger from '@src/js/common/logger.js'

class UserGroupFormParametersRole extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'UserGroupFormParametersRole.render')

    return <RoleParameters {...this.props} />
  }
}

export default UserGroupFormParametersRole
