import React from 'react'
import autoBind from 'auto-bind'
import { withStyles } from '@material-ui/core/styles'
import ComponentContext from '@src/js/components/common/ComponentContext.js'
import PageWithTwoPanels from '@src/js/components/common/page/PageWithTwoPanels.jsx'
import GridContainer from '@src/js/components/common/grid/GridContainer.jsx'
import UserGroupFormController from '@src/js/components/users/form/usergroup/UserGroupFormController.js'
import UserGroupFormFacade from '@src/js/components/users/form/usergroup/UserGroupFormFacade.js'
import UserGroupFormSelectionType from '@src/js/components/users/form/usergroup/UserGroupFormSelectionType.js'
import UserGroupFormParametersGroup from '@src/js/components/users/form/usergroup/UserGroupFormParametersGroup.jsx'
import UserGroupFormParametersUser from '@src/js/components/users/form/usergroup/UserGroupFormParametersUser.jsx'
import UserGroupFormParametersRole from '@src/js/components/users/form/usergroup/UserGroupFormParametersRole.jsx'
import UserGroupFormGridUsers from '@src/js/components/users/form/usergroup/UserGroupFormGridUsers.jsx'
import UserGroupFormGridRoles from '@src/js/components/users/form/usergroup/UserGroupFormGridRoles.jsx'
import UserGroupFormButtons from '@src/js/components/users/form/usergroup/UserGroupFormButtons.jsx'
import ids from '@src/js/common/consts/ids.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  grid: {
    marginBottom: theme.spacing(2)
  }
})

class UserGroupForm extends React.PureComponent {
  constructor(props) {
    super(props)
    autoBind(this)

    this.state = {}

    if (this.props.controller) {
      this.controller = this.props.controller
    } else {
      this.controller = new UserGroupFormController(new UserGroupFormFacade())
    }

    this.controller.init(new ComponentContext(this))
  }

  componentDidMount() {
    this.controller.load()
  }

  handleClickContainer() {
    this.controller.handleSelectionChange()
  }

  handleSelectedUserRowChange(row) {
    const { controller } = this
    if (row) {
      controller.handleSelectionChange(UserGroupFormSelectionType.USER, {
        id: row.id
      })
    }
  }

  handleSelectedRoleRowChange(row) {
    const { controller } = this
    if (row) {
      controller.handleSelectionChange(UserGroupFormSelectionType.ROLE, {
        id: row.id
      })
    }
  }

  handleUsersGridControllerRef(gridController) {
    this.controller.usersGridController = gridController
  }

  handleRolesGridControllerRef(gridController) {
    this.controller.rolesGridController = gridController
  }

  render() {
    logger.log(logger.DEBUG, 'UserGroupForm.render')

    const { loadId, loading, loaded, group } = this.state

    return (
      <PageWithTwoPanels
        id={ids.USER_GROUP_FORM_ID}
        key={loadId}
        loading={loading}
        loaded={loaded}
        object={group}
        renderMainPanel={() => this.renderMainPanel()}
        renderAdditionalPanel={() => this.renderAdditionalPanel()}
        renderButtons={() => this.renderButtons()}
      />
    )
  }

  renderMainPanel() {
    const { classes } = this.props
    const { users, roles, selection } = this.state

    return (
      <GridContainer onClick={this.handleClickContainer}>
        <div className={classes.grid}>
          <UserGroupFormGridUsers
            controllerRef={this.handleUsersGridControllerRef}
            rows={users}
            selectedRowId={
              selection && selection.type === UserGroupFormSelectionType.USER
                ? selection.params.id
                : null
            }
            onSelectedRowChange={this.handleSelectedUserRowChange}
          />
        </div>
        <div className={classes.grid}>
          <UserGroupFormGridRoles
            controllerRef={this.handleRolesGridControllerRef}
            rows={roles}
            selectedRowId={
              selection && selection.type === UserGroupFormSelectionType.ROLE
                ? selection.params.id
                : null
            }
            onSelectedRowChange={this.handleSelectedRoleRowChange}
          />
        </div>
      </GridContainer>
    )
  }

  renderAdditionalPanel() {
    const { controller } = this
    const {
      group,
      users,
      roles,
      selection,
      selectedUserRow,
      selectedRoleRow,
      mode
    } = this.state

    return (
      <div>
        <UserGroupFormParametersGroup
          controller={controller}
          group={group}
          selection={selection}
          mode={mode}
          onChange={controller.handleChange}
          onSelectionChange={controller.handleSelectionChange}
          onBlur={controller.handleBlur}
        />
        <UserGroupFormParametersUser
          controller={controller}
          users={users}
          selection={selection}
          selectedRow={selectedUserRow}
          mode={mode}
          onChange={controller.handleChange}
          onSelectionChange={controller.handleSelectionChange}
          onBlur={controller.handleBlur}
        />
        <UserGroupFormParametersRole
          controller={controller}
          roles={roles}
          selection={selection}
          selectedRow={selectedRoleRow}
          mode={mode}
          onChange={controller.handleChange}
          onSelectionChange={controller.handleSelectionChange}
          onBlur={controller.handleBlur}
        />
      </div>
    )
  }

  renderButtons() {
    const { controller } = this
    const { group, roles, selection, changed, mode } = this.state

    return (
      <UserGroupFormButtons
        onEdit={controller.handleEdit}
        onSave={controller.handleSave}
        onCancel={controller.handleCancel}
        onAddUser={controller.handleAddUser}
        onAddRole={controller.handleAddRole}
        onRemove={controller.handleRemove}
        group={group}
        roles={roles}
        selection={selection}
        changed={changed}
        mode={mode}
      />
    )
  }
}

export default withStyles(styles)(UserGroupForm)
