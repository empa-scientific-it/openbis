import _ from 'lodash'
import React from 'react'
import autoBind from 'auto-bind'
import { connect } from 'react-redux'
import { withStyles } from '@material-ui/core/styles'
import ComponentContext from '@src/js/components/common/ComponentContext.js'
import PageWithTwoPanels from '@src/js/components/common/page/PageWithTwoPanels.jsx'
import GridContainer from '@src/js/components/common/grid/GridContainer.jsx'
import UserFormController from '@src/js/components/users/form/UserFormController.js'
import UserFormFacade from '@src/js/components/users/form/UserFormFacade.js'
import UserFormSelectionType from '@src/js/components/users/form/UserFormSelectionType.js'
import UserFormParametersUser from '@src/js/components/users/form/UserFormParametersUser.jsx'
import UserFormParametersGroup from '@src/js/components/users/form/UserFormParametersGroup.jsx'
import UserFormParametersRole from '@src/js/components/users/form/UserFormParametersRole.jsx'
import UserFormGridGroups from '@src/js/components/users/form/UserFormGridGroups.jsx'
import UserFormGridRoles from '@src/js/components/users/form/UserFormGridRoles.jsx'
import UserFormButtons from '@src/js/components/users/form/UserFormButtons.jsx'
import logger from '@src/js/common/logger.js'

const styles = () => ({})

class UserForm extends React.PureComponent {
  constructor(props) {
    super(props)
    autoBind(this)

    this.state = {}

    if (this.props.controller) {
      this.controller = this.props.controller
    } else {
      this.controller = new UserFormController(new UserFormFacade())
    }

    this.controller.init(new ComponentContext(this))
  }

  componentDidMount() {
    this.controller.load()
  }

  handleClickContainer() {
    this.controller.handleSelectionChange()
  }

  handleSelectedGroupRowChange(row) {
    const { controller } = this
    if (row) {
      controller.handleSelectionChange(UserFormSelectionType.GROUP, {
        id: row.id
      })
    }
  }

  handleSelectedRoleRowChange(row) {
    const { controller } = this
    if (row) {
      controller.handleSelectionChange(UserFormSelectionType.ROLE, {
        id: row.id
      })
    }
  }

  handleGroupsGridControllerRef(gridController) {
    this.controller.groupsGridController = gridController
  }

  handleRolesGridControllerRef(gridController) {
    this.controller.rolesGridController = gridController
  }

  render() {
    logger.log(logger.DEBUG, 'UserForm.render')

    const { loading, loaded, user } = this.state

    return (
      <PageWithTwoPanels
        loading={loading}
        loaded={loaded}
        object={user}
        renderMainPanel={() => this.renderMainPanel()}
        renderAdditionalPanel={() => this.renderAdditionalPanel()}
        renderButtons={() => this.renderButtons()}
      />
    )
  }

  renderMainPanel() {
    const { groups, roles, selection } = this.state

    return (
      <GridContainer onClick={this.handleClickContainer}>
        <div>
          <UserFormGridGroups
            controllerRef={this.handleGroupsGridControllerRef}
            rows={groups}
            selectedRowId={
              selection && selection.type === UserFormSelectionType.GROUP
                ? selection.params.id
                : null
            }
            onSelectedRowChange={this.handleSelectedGroupRowChange}
          />
        </div>
        <div>
          <UserFormGridRoles
            controllerRef={this.handleRolesGridControllerRef}
            rows={roles}
            selectedRowId={
              selection && selection.type === UserFormSelectionType.ROLE
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
    const { user, groups, roles, selection, mode } = this.state

    const selectedGroupRow = controller.groupsGridController
      ? controller.groupsGridController.getSelectedRow()
      : null

    const selectedRoleRow = controller.rolesGridController
      ? controller.rolesGridController.getSelectedRow()
      : null

    return (
      <div>
        <UserFormParametersUser
          controller={controller}
          user={user}
          selection={selection}
          mode={mode}
          onChange={controller.handleChange}
          onSelectionChange={controller.handleSelectionChange}
          onBlur={controller.handleBlur}
        />
        <UserFormParametersGroup
          controller={controller}
          groups={groups}
          selection={selection}
          selectedRow={selectedGroupRow}
          mode={mode}
          onChange={controller.handleChange}
          onSelectionChange={controller.handleSelectionChange}
          onBlur={controller.handleBlur}
        />
        <UserFormParametersRole
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
    const { user, roles, selection, changed, mode } = this.state

    return (
      <UserFormButtons
        onEdit={controller.handleEdit}
        onSave={controller.handleSave}
        onCancel={controller.handleCancel}
        onAddGroup={controller.handleAddGroup}
        onAddRole={controller.handleAddRole}
        onRemove={controller.handleRemove}
        user={user}
        roles={roles}
        selection={selection}
        changed={changed}
        mode={mode}
      />
    )
  }
}

export default _.flow(connect(), withStyles(styles))(UserForm)
