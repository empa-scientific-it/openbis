import _ from 'lodash'
import React from 'react'
import autoBind from 'auto-bind'
import { withStyles } from '@material-ui/core/styles'
import GridWithOpenbis from '@src/js/components/common/grid/GridWithOpenbis.jsx'
import GridExportOptions from '@src/js/components/common/grid/GridExportOptions.js'
import UserLink from '@src/js/components/common/link/UserLink.jsx'
import UserGroupLink from '@src/js/components/common/link/UserGroupLink.jsx'
import openbis from '@src/js/services/openbis.js'
import ids from '@src/js/common/consts/ids.js'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  inherited: {
    color: theme.palette.hint.main
  }
})

const ALL_VALUE = '(' + messages.get(messages.ALL) + ')'

class RolesGrid extends React.PureComponent {
  constructor(props) {
    super(props)
    autoBind(this)
  }

  render() {
    logger.log(logger.DEBUG, 'RolesGrid.render')

    const { id, rows, selectedRowId, onSelectedRowChange, controllerRef } =
      this.props

    let columnNames = ['level', 'space', 'project', 'role']

    if (id === ids.ROLES_OF_USER_GRID_ID) {
      columnNames = ['inheritedFrom', ...columnNames]
    } else if (id === ids.ROLES_OF_USERS_GRID_ID) {
      columnNames = ['user', 'inheritedFrom', ...columnNames]
    } else if (id === ids.ROLES_OF_GROUP_GRID_ID) {
      columnNames = [...columnNames]
    } else if (id === ids.ROLES_OF_GROUPS_GRID_ID) {
      columnNames = ['group', ...columnNames]
    } else {
      throw 'Unsupported id: ' + id
    }

    const columns = this.getColumns().filter(
      column => columnNames.indexOf(column.name) !== -1
    )

    return (
      <GridWithOpenbis
        id={id}
        settingsId={id}
        controllerRef={controllerRef}
        header={this.getHeader()}
        columns={columns}
        rows={rows}
        sort={this.getSort()}
        exportable={this.getExportable()}
        selectable={true}
        selectedRowId={selectedRowId}
        onSelectedRowChange={onSelectedRowChange}
      />
    )
  }

  getHeader() {
    const { id } = this.props

    let message = null
    if (id === ids.ROLES_OF_USER_GRID_ID || id === ids.ROLES_OF_GROUP_GRID_ID) {
      message = messages.ROLES
    } else if (id === ids.ROLES_OF_USERS_GRID_ID) {
      message = messages.ROLES_OF_USERS
    } else if (id === ids.ROLES_OF_GROUPS_GRID_ID) {
      message = messages.ROLES_OF_GROUPS
    }

    return messages.get(message)
  }

  getSort() {
    const { id } = this.props

    if (id === ids.ROLES_OF_USERS_GRID_ID) {
      return 'user'
    } else if (id === ids.ROLES_OF_GROUPS_GRID_ID) {
      return 'group'
    } else if (id === ids.ROLES_OF_USER_GRID_ID) {
      return 'inheritedFrom'
    } else if (id === ids.ROLES_OF_GROUP_GRID_ID) {
      return 'level'
    }
  }

  getExportable() {
    const { id } = this.props

    if (id === ids.ROLES_OF_USERS_GRID_ID) {
      return {
        fileFormat: GridExportOptions.TSV_FILE_FORMAT,
        filePrefix: 'users-roles'
      }
    } else if (id === ids.ROLES_OF_GROUPS_GRID_ID) {
      return {
        fileFormat: GridExportOptions.TSV_FILE_FORMAT,
        filePrefix: 'groups-roles'
      }
    } else if (id === ids.ROLES_OF_USER_GRID_ID) {
      return {
        fileFormat: GridExportOptions.TSV_FILE_FORMAT,
        filePrefix: 'user-roles'
      }
    } else if (id === ids.ROLES_OF_GROUP_GRID_ID) {
      return {
        fileFormat: GridExportOptions.TSV_FILE_FORMAT,
        filePrefix: 'group-roles'
      }
    }
  }

  getColumns() {
    return [
      {
        name: 'user',
        label: messages.get(messages.USER),
        getValue: this.getUserValue,
        renderValue: this.renderUserValue,
        compareValue: params => {
          return (
            100000 * this.compareUserValue(params) +
            this.ignoreSortDirectionFactor(params) *
              (10000 * this.compareInheritedFromValue(params) +
                1000 * this.compareLevelValue(params) +
                100 * this.compareSpaceValue(params) +
                10 * this.compareProjectValue(params) +
                this.compareRoleValue(params))
          )
        }
      },
      {
        name: 'group',
        label: messages.get(messages.GROUP),
        getValue: this.getGroupValue,
        renderValue: this.renderGroupValue,
        compareValue: params => {
          return (
            100000 * this.compareGroupValue(params) +
            this.ignoreSortDirectionFactor(params) *
              (10000 * this.compareInheritedFromValue(params) +
                1000 * this.compareLevelValue(params) +
                100 * this.compareSpaceValue(params) +
                10 * this.compareProjectValue(params) +
                this.compareRoleValue(params))
          )
        }
      },
      {
        name: 'inheritedFrom',
        label: messages.get(messages.INHERITED_FROM),
        getValue: this.getInheritedFromValue,
        renderValue: this.renderInheritedFromValue,
        compareValue: params => {
          return (
            10000 * this.compareInheritedFromValue(params) +
            this.ignoreSortDirectionFactor(params) *
              (1000 * this.compareLevelValue(params) +
                100 * this.compareSpaceValue(params) +
                10 * this.compareProjectValue(params) +
                this.compareRoleValue(params))
          )
        }
      },
      {
        name: 'level',
        label: messages.get(messages.LEVEL),
        getValue: this.getLevelValue,
        renderValue: this.renderLevelValue,
        compareValue: params => {
          return (
            10000 * this.compareLevelValue(params) +
            this.ignoreSortDirectionFactor(params) *
              (1000 * this.compareInheritedFromValue(params) +
                100 * this.compareSpaceValue(params) +
                10 * this.compareProjectValue(params) +
                this.compareRoleValue(params))
          )
        }
      },
      {
        name: 'space',
        label: messages.get(messages.SPACE),
        getValue: this.getSpaceValue,
        renderValue: this.renderSpaceValue,
        compareValue: params => {
          return (
            10000 * this.compareSpaceValue(params) +
            this.ignoreSortDirectionFactor(params) *
              (1000 * this.compareLevelValue(params) +
                100 * this.compareInheritedFromValue(params) +
                10 * this.compareProjectValue(params) +
                this.compareRoleValue(params))
          )
        }
      },
      {
        name: 'project',
        label: messages.get(messages.PROJECT),
        getValue: this.getProjectValue,
        renderValue: this.renderProjectValue,
        compareValue: params => {
          return (
            10000 * this.compareProjectValue(params) +
            this.ignoreSortDirectionFactor(params) *
              (1000 * this.compareLevelValue(params) +
                100 * this.compareInheritedFromValue(params) +
                10 * this.compareSpaceValue(params) +
                this.compareRoleValue(params))
          )
        }
      },
      {
        name: 'role',
        label: messages.get(messages.ROLE),
        getValue: this.getRoleValue,
        renderValue: this.renderRoleValue,
        compareValue: params => {
          return (
            10000 * this.compareRoleValue(params) +
            this.ignoreSortDirectionFactor(params) *
              (1000 * this.compareLevelValue(params) +
                100 * this.compareInheritedFromValue(params) +
                10 * this.compareSpaceValue(params) +
                this.compareProjectValue(params))
          )
        }
      }
    ]
  }

  getUserValue({ row }) {
    return _.get(row, 'user.value')
  }

  getGroupValue({ row }) {
    return _.get(row, 'group.value')
  }

  getInheritedFromValue({ row }) {
    return _.get(row, 'inheritedFrom.value')
  }

  getLevelValue({ row }) {
    return _.get(row, 'level.value')
  }

  getSpaceValue({ row }) {
    if (this.getLevelValue({ row }) === openbis.RoleLevel.INSTANCE) {
      return ALL_VALUE
    } else {
      return _.get(row, 'space.value')
    }
  }

  getProjectValue({ row }) {
    if (this.getLevelValue({ row }) === openbis.RoleLevel.INSTANCE) {
      return ALL_VALUE
    } else if (this.getLevelValue({ row }) === openbis.RoleLevel.SPACE) {
      return this.getSpaceValue({ row }) ? ALL_VALUE : null
    } else {
      return _.get(row, 'project.value')
    }
  }

  getRoleValue({ row }) {
    return _.get(row, 'role.value')
  }

  renderUserValue({ value }) {
    return <UserLink userId={value} />
  }

  renderGroupValue({ value }) {
    return <UserGroupLink groupCode={value} />
  }

  renderInheritedFromValue({ value }) {
    return <UserGroupLink groupCode={value} />
  }

  renderLevelValue({ value, row }) {
    return this.renderDefault({ value, row })
  }

  renderSpaceValue({ value, row }) {
    return this.renderDefault({ value, row })
  }

  renderProjectValue({ value, row }) {
    return this.renderDefault({ value, row })
  }

  renderRoleValue({ value, row }) {
    return this.renderDefault({ value, row })
  }

  renderDefault({ value, row }) {
    const { classes } = this.props

    if (this.getInheritedFromValue({ row })) {
      return <div className={classes.inherited}>{value}</div>
    } else {
      return value
    }
  }

  compareUserValue({ row1, row2, defaultCompare }) {
    const user1 = this.getUserValue({ row: row1 })
    const user2 = this.getUserValue({ row: row2 })
    return defaultCompare(user1, user2)
  }

  compareGroupValue({ row1, row2, defaultCompare }) {
    const group1 = this.getGroupValue({ row: row1 })
    const group2 = this.getGroupValue({ row: row2 })
    return defaultCompare(group1, group2)
  }

  compareInheritedFromValue({ row1, row2, defaultCompare }) {
    const inheritedFrom1 = this.getInheritedFromValue({ row: row1 })
    const inheritedFrom2 = this.getInheritedFromValue({ row: row2 })

    if (inheritedFrom1 && !inheritedFrom2) {
      return -1
    } else if (!inheritedFrom1 && inheritedFrom2) {
      return 1
    } else {
      return defaultCompare(inheritedFrom1, inheritedFrom2)
    }
  }

  compareLevelValue({ row1, row2, defaultCompare }) {
    const LEVEL_SORTING = {
      [null]: 0,
      [openbis.RoleLevel.INSTANCE]: 1,
      [openbis.RoleLevel.SPACE]: 2,
      [openbis.RoleLevel.PROJECT]: 3
    }
    const level1 = this.getLevelValue({ row: row1 })
    const level2 = this.getLevelValue({ row: row2 })
    return defaultCompare(LEVEL_SORTING[level1], LEVEL_SORTING[level2])
  }

  compareSpaceValue({ row1, row2, defaultCompare }) {
    const space1 = this.getSpaceValue({ row: row1 })
    const space2 = this.getSpaceValue({ row: row2 })
    return defaultCompare(space1, space2)
  }

  compareProjectValue({ row1, row2, defaultCompare }) {
    const project1 = this.getProjectValue({ row: row1 })
    const project2 = this.getProjectValue({ row: row2 })
    return defaultCompare(project1, project2)
  }

  compareRoleValue({ row1, row2, defaultCompare }) {
    const ROLE_SORTING = {
      [null]: 0,
      [openbis.Role.ETL_SERVER]: 1,
      [openbis.Role.ADMIN]: 2,
      [openbis.Role.POWER_USER]: 3,
      [openbis.Role.USER]: 4,
      [openbis.Role.OBSERVER]: 5,
      [openbis.Role.DISABLED]: 6
    }
    const role1 = this.getRoleValue({ row: row1 })
    const role2 = this.getRoleValue({ row: row2 })
    return defaultCompare(ROLE_SORTING[role1], ROLE_SORTING[role2])
  }

  ignoreSortDirectionFactor(params) {
    return params.sortDirection === 'asc' ? 1 : -1
  }
}

export default _.flow(withStyles(styles))(RolesGrid)
