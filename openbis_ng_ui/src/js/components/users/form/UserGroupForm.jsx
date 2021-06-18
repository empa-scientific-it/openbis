import _ from 'lodash'
import React from 'react'
import autoBind from 'auto-bind'
import { connect } from 'react-redux'
import { withStyles } from '@material-ui/core/styles'
import ComponentContext from '@src/js/components/common/ComponentContext.js'
import PageWithTwoPanels from '@src/js/components/common/page/PageWithTwoPanels.jsx'
import GridContainer from '@src/js/components/common/grid/GridContainer.jsx'
import UserGroupFormController from '@src/js/components/users/form/UserGroupFormController.js'
import UserGroupFormFacade from '@src/js/components/users/form/UserGroupFormFacade.js'
import UserGroupFormSelectionType from '@src/js/components/users/form/UserGroupFormSelectionType.js'
import UserGroupFormParametersGroup from '@src/js/components/users/form/UserGroupFormParametersGroup.jsx'
import UserGroupFormParametersUser from '@src/js/components/users/form/UserGroupFormParametersUser.jsx'
import UserGroupFormParametersRole from '@src/js/components/users/form/UserGroupFormParametersRole.jsx'
import UserGroupFormGridUsers from '@src/js/components/users/form/UserGroupFormGridUsers.jsx'
import UserGroupFormGridRoles from '@src/js/components/users/form/UserGroupFormGridRoles.jsx'
import UserGroupFormButtons from '@src/js/components/users/form/UserGroupFormButtons.jsx'
import logger from '@src/js/common/logger.js'

const styles = () => ({})

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

    const { loading, loaded, group } = this.state

    return (
      <PageWithTwoPanels
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
    const { users, roles, selection } = this.state

    return (
      <GridContainer onClick={this.handleClickContainer}>
        <div>
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
        <div>
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
    const { group, users, roles, selection, mode } = this.state

    const selectedUserRow = controller.usersGridController
      ? controller.usersGridController.getSelectedRow()
      : null

    const selectedRoleRow = controller.rolesGridController
      ? controller.rolesGridController.getSelectedRow()
      : null

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

export default _.flow(connect(), withStyles(styles))(UserGroupForm)
