import _ from 'lodash'
import autoBind from 'auto-bind'
import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Container from '@src/js/components/common/form/Container.jsx'
import AppController from '@src/js/components/AppController.js'
import GridContainer from '@src/js/components/common/grid/GridContainer.jsx'
import UsersGrid from '@src/js/components/users/common/UsersGrid.jsx'
import UserGroupsGrid from '@src/js/components/users/common/UserGroupsGrid.jsx'
import RolesGrid from '@src/js/components/users/common/RolesGrid.jsx'
import Message from '@src/js/components/common/form/Message.jsx'
import FormUtil from '@src/js/components/common/form/FormUtil.js'
import ids from '@src/js/common/consts/ids.js'
import objectTypes from '@src/js/common/consts/objectType.js'
import openbis from '@src/js/services/openbis.js'
import util from '@src/js/common/util.js'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  grid: {
    marginBottom: theme.spacing(2)
  }
})

const USER_FILTERED_FIELDS = ['userId', 'firstName', 'lastName']
const USER_GROUP_FILTERED_FIELDS = ['code', 'description']

class UserSearch extends React.Component {
  constructor(props) {
    super(props)
    autoBind(this)

    this.gridControllers = {}

    this.state = {
      loaded: false
    }
  }

  componentDidMount() {
    this.load()
  }

  async load() {
    try {
      await Promise.all([
        this.loadUsers(),
        this.loadUsersRoles(),
        this.loadUserGroups(),
        this.loadUserGroupsRoles()
      ])
      this.setState(() => ({
        loaded: true
      }))
    } catch (error) {
      AppController.getInstance().errorChange(error)
    }
  }

  async loadUsers() {
    if (!this.shouldLoad(objectTypes.USER)) {
      return
    }

    const fo = new openbis.PersonFetchOptions()
    fo.withSpace()
    fo.withRegistrator()

    const result = await openbis.searchPersons(
      new openbis.PersonSearchCriteria(),
      fo
    )

    const users = util
      .filter(result.objects, this.props.searchText, USER_FILTERED_FIELDS)
      .map(object => ({
        id: _.uniqueId('user-'),
        userId: FormUtil.createField({ value: _.get(object, 'userId') }),
        firstName: FormUtil.createField({ value: _.get(object, 'firstName') }),
        lastName: FormUtil.createField({ value: _.get(object, 'lastName') }),
        email: FormUtil.createField({ value: _.get(object, 'email') }),
        space: FormUtil.createField({ value: _.get(object, 'space.code') }),
        active: FormUtil.createField({ value: _.get(object, 'active') }),
        registrator: FormUtil.createField({
          value: _.get(object, 'registrator.userId')
        }),
        registrationDate: FormUtil.createField({
          value: _.get(object, 'registrationDate')
        })
      }))

    this.setState({
      users
    })
  }

  async loadUsersRoles() {
    if (!this.shouldLoad(objectTypes.USER)) {
      return
    }

    const userFo = new openbis.PersonFetchOptions()
    userFo.withRoleAssignments().withSpace()
    userFo.withRoleAssignments().withProject().withSpace()
    userFo.withRoleAssignments().withRegistrator()

    const groupFo = new openbis.AuthorizationGroupFetchOptions()
    groupFo.withUsers()
    groupFo.withRoleAssignments().withSpace()
    groupFo.withRoleAssignments().withProject().withSpace()
    groupFo.withRoleAssignments().withRegistrator()

    const [userResult, groupResult] = await Promise.all([
      openbis.searchPersons(new openbis.PersonSearchCriteria(), userFo),
      openbis.searchAuthorizationGroups(
        new openbis.AuthorizationGroupSearchCriteria(),
        groupFo
      )
    ])

    const usersRoles = []

    util
      .filter(userResult.objects, this.props.searchText, USER_FILTERED_FIELDS)
      .forEach(user => {
        const userAssignments = user.roleAssignments || []

        userAssignments.forEach(userAssignment => {
          const level = _.get(userAssignment, 'roleLevel', null)

          let space = null
          let project = null

          if (level === openbis.RoleLevel.SPACE) {
            space = _.get(userAssignment, 'space.code')
          } else if (level === openbis.RoleLevel.PROJECT) {
            space = _.get(userAssignment, 'project.space.code')
            project = _.get(userAssignment, 'project.code')
          }

          usersRoles.push({
            id: _.uniqueId('user-role-'),
            user: FormUtil.createField({
              value: _.get(user, 'userId', null)
            }),
            inheritedFrom: FormUtil.createField({
              value: null
            }),
            level: FormUtil.createField({ value: level }),
            space: FormUtil.createField({ value: space }),
            project: FormUtil.createField({ value: project }),
            role: FormUtil.createField({
              value: _.get(userAssignment, 'role', null)
            }),
            registrator: FormUtil.createField({
              value: _.get(userAssignment, 'registrator.userId')
            }),
            registrationDate: FormUtil.createField({
              value: _.get(userAssignment, 'registrationDate')
            })
          })
        })
      })

    groupResult.objects.forEach(group => {
      const groupUsers = util.filter(
        group.users || [],
        this.props.searchText,
        USER_FILTERED_FIELDS
      )

      const groupAssignments = group.roleAssignments || []

      groupUsers.forEach(groupUser => {
        groupAssignments.forEach(groupAssignment => {
          const level = _.get(groupAssignment, 'roleLevel', null)

          let space = null
          let project = null

          if (level === openbis.RoleLevel.SPACE) {
            space = _.get(groupAssignment, 'space.code')
          } else if (level === openbis.RoleLevel.PROJECT) {
            space = _.get(groupAssignment, 'project.space.code')
            project = _.get(groupAssignment, 'project.code')
          }

          usersRoles.push({
            id: _.uniqueId('user-role-'),
            user: FormUtil.createField({
              value: _.get(groupUser, 'userId', null)
            }),
            inheritedFrom: FormUtil.createField({
              value: _.get(group, 'code')
            }),
            level: FormUtil.createField({ value: level }),
            space: FormUtil.createField({ value: space }),
            project: FormUtil.createField({ value: project }),
            role: FormUtil.createField({
              value: _.get(groupAssignment, 'role', null)
            }),
            registrator: FormUtil.createField({
              value: _.get(groupAssignment, 'registrator.userId')
            }),
            registrationDate: FormUtil.createField({
              value: _.get(groupAssignment, 'registrationDate')
            })
          })
        })
      })
    })

    this.setState({
      usersRoles
    })
  }

  async loadUserGroups() {
    if (!this.shouldLoad(objectTypes.USER_GROUP)) {
      return
    }

    const fo = new openbis.AuthorizationGroupFetchOptions()
    fo.withRegistrator()

    const result = await openbis.searchAuthorizationGroups(
      new openbis.AuthorizationGroupSearchCriteria(),
      fo
    )

    const userGroups = util
      .filter(result.objects, this.props.searchText, USER_GROUP_FILTERED_FIELDS)
      .map(object => ({
        id: _.uniqueId('group-'),
        code: FormUtil.createField({ value: _.get(object, 'code') }),
        description: FormUtil.createField({
          value: _.get(object, 'description')
        }),
        registrator: FormUtil.createField({
          value: _.get(object, 'registrator.userId')
        }),
        registrationDate: FormUtil.createField({
          value: _.get(object, 'registrationDate')
        }),
        modificationDate: FormUtil.createField({
          value: _.get(object, 'modificationDate')
        })
      }))

    this.setState({
      userGroups
    })
  }

  async loadUserGroupsRoles() {
    if (!this.shouldLoad(objectTypes.USER_GROUP)) {
      return
    }

    const fo = new openbis.AuthorizationGroupFetchOptions()
    fo.withRoleAssignments().withSpace()
    fo.withRoleAssignments().withProject().withSpace()
    fo.withRoleAssignments().withRegistrator()

    const result = await openbis.searchAuthorizationGroups(
      new openbis.AuthorizationGroupSearchCriteria(),
      fo
    )

    const userGroups = util.filter(
      result.objects,
      this.props.searchText,
      USER_GROUP_FILTERED_FIELDS
    )

    const userGroupsRoles = []
    userGroups.forEach(userGroup => {
      const roleAssignments = userGroup.roleAssignments || []
      roleAssignments.forEach(roleAssignment => {
        const level = _.get(roleAssignment, 'roleLevel', null)

        let space = null
        let project = null

        if (level === openbis.RoleLevel.SPACE) {
          space = _.get(roleAssignment, 'space.code')
        } else if (level === openbis.RoleLevel.PROJECT) {
          space = _.get(roleAssignment, 'project.space.code')
          project = _.get(roleAssignment, 'project.code')
        }

        userGroupsRoles.push({
          id: _.uniqueId('group-role-'),
          group: FormUtil.createField({
            value: _.get(userGroup, 'code', null)
          }),
          level: FormUtil.createField({ value: level }),
          space: FormUtil.createField({ value: space }),
          project: FormUtil.createField({ value: project }),
          role: FormUtil.createField({
            value: _.get(roleAssignment, 'role', null)
          }),
          registrator: FormUtil.createField({
            value: _.get(roleAssignment, 'registrator.userId')
          }),
          registrationDate: FormUtil.createField({
            value: _.get(roleAssignment, 'registrationDate')
          })
        })
      })
    })

    this.setState({
      userGroupsRoles
    })
  }

  shouldLoad(objectType) {
    return this.props.objectType === objectType || !this.props.objectType
  }

  handleContainerClick() {
    for (let gridObjectType in this.gridControllers) {
      this.gridControllers[gridObjectType].selectRow(null)
    }
  }

  handleSelectedRowChange(objectType) {
    return row => {
      if (!row) {
        return
      }
      for (let gridObjectType in this.gridControllers) {
        if (gridObjectType !== objectType) {
          this.gridControllers[gridObjectType].selectRow(null)
        }
      }
    }
  }

  render() {
    logger.log(logger.DEBUG, 'UserSearch.render')

    if (!this.state.loaded) {
      return null
    }

    return (
      <GridContainer onClick={this.handleContainerClick}>
        {this.renderNoResultsFoundMessage()}
        {this.renderUsers()}
        {this.renderUsersRoles()}
        {this.renderUserGroups()}
        {this.renderUserGroupsRoles()}
      </GridContainer>
    )
  }

  renderNoResultsFoundMessage() {
    const { objectType } = this.props
    const {
      users = [],
      usersRoles = [],
      userGroups = [],
      userGroupsRoles = []
    } = this.state

    if (
      !objectType &&
      users.length === 0 &&
      usersRoles.length === 0 &&
      userGroups.length === 0 &&
      userGroupsRoles.length === 0
    ) {
      return (
        <Container>
          <Message type='info'>
            {messages.get(messages.NO_RESULTS_FOUND)}
          </Message>
        </Container>
      )
    } else {
      return null
    }
  }

  renderUsers() {
    if (this.shouldRender(objectTypes.USER, this.state.users)) {
      const { classes } = this.props
      return (
        <div className={classes.grid}>
          <UsersGrid
            id={ids.USERS_GRID_ID}
            controllerRef={controller =>
              (this.gridControllers[objectTypes.USER] = controller)
            }
            rows={this.state.users}
            onSelectedRowChange={this.handleSelectedRowChange(objectTypes.USER)}
          />
        </div>
      )
    } else {
      return null
    }
  }

  renderUsersRoles() {
    if (this.shouldRender(objectTypes.USER, this.state.usersRoles)) {
      const { classes } = this.props
      return (
        <div className={classes.grid}>
          <RolesGrid
            id={ids.ROLES_OF_USERS_GRID_ID}
            controllerRef={controller =>
              (this.gridControllers[objectTypes.USER + '-role'] = controller)
            }
            rows={this.state.usersRoles}
            onSelectedRowChange={this.handleSelectedRowChange(
              objectTypes.USER + '-role'
            )}
          />
        </div>
      )
    } else {
      return null
    }
  }

  renderUserGroups() {
    if (this.shouldRender(objectTypes.USER_GROUP, this.state.userGroups)) {
      const { classes } = this.props
      return (
        <div className={classes.grid}>
          <UserGroupsGrid
            id={ids.GROUPS_GRID_ID}
            controllerRef={controller =>
              (this.gridControllers[objectTypes.USER_GROUP] = controller)
            }
            rows={this.state.userGroups}
            onSelectedRowChange={this.handleSelectedRowChange(
              objectTypes.USER_GROUP
            )}
          />
        </div>
      )
    } else {
      return null
    }
  }

  renderUserGroupsRoles() {
    if (this.shouldRender(objectTypes.USER_GROUP, this.state.userGroupsRoles)) {
      const { classes } = this.props
      return (
        <div className={classes.grid}>
          <RolesGrid
            id={ids.ROLES_OF_GROUPS_GRID_ID}
            controllerRef={controller =>
              (this.gridControllers[objectTypes.USER_GROUP + '-role'] =
                controller)
            }
            rows={this.state.userGroupsRoles}
            onSelectedRowChange={this.handleSelectedRowChange(
              objectTypes.USER_GROUP + '-role'
            )}
          />
        </div>
      )
    } else {
      return null
    }
  }

  shouldRender(objectType, types) {
    return this.props.objectType === objectType || (types && types.length > 0)
  }
}

export default withStyles(styles)(UserSearch)
