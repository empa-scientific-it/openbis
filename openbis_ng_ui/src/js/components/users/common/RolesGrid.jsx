import _ from 'lodash'
import React from 'react'
import autoBind from 'auto-bind'
import { withStyles } from '@material-ui/core/styles'
import Grid from '@src/js/components/common/grid/Grid.jsx'
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

    const {
      id,
      rows,
      selectedRowId,
      onSelectedRowChange,
      controllerRef
    } = this.props

    let columnNames

    if (id === ids.USER_ROLES_GRID_ID) {
      columnNames = ['inheritedFrom', 'level', 'space', 'project', 'role']
    } else if (id === ids.USER_GROUP_ROLES_GRID_ID) {
      columnNames = ['level', 'space', 'project', 'role']
    } else {
      throw 'Unsupported id: ' + id
    }

    const columns = this.getColumns().filter(
      column => columnNames.indexOf(column.name) !== -1
    )

    return (
      <Grid
        id={id}
        controllerRef={controllerRef}
        header={messages.get(messages.ROLES)}
        columns={columns}
        rows={rows}
        selectedRowId={selectedRowId}
        onSelectedRowChange={onSelectedRowChange}
      />
    )
  }

  getColumns() {
    const { id } = this.props

    return [
      {
        name: 'inheritedFrom',
        label: messages.get(messages.INHERITED_FROM),
        sort: id === ids.USER_ROLES_GRID_ID ? 'asc' : null,
        getValue: this.getInheritedFromValue,
        renderValue: this.renderInheritedFromValue,
        compareValue: params => {
          return (
            10000 * this.compareInheritedFromValue(params) +
            1000 * this.compareLevelValue(params) +
            100 * this.compareSpaceValue(params) +
            10 * this.compareProjectValue(params) +
            this.compareRoleValue(params)
          )
        }
      },
      {
        name: 'level',
        label: messages.get(messages.LEVEL),
        sort: id === ids.USER_GROUP_ROLES_GRID_ID ? 'asc' : null,
        getValue: this.getLevelValue,
        renderValue: this.renderLevelValue,
        compareValue: params => {
          return (
            1000 * this.compareLevelValue(params) +
            100 * this.compareSpaceValue(params) +
            10 * this.compareProjectValue(params) +
            this.compareRoleValue(params)
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
            1000 * this.compareSpaceValue(params) +
            100 * this.compareLevelValue(params) +
            10 * this.compareProjectValue(params) +
            this.compareRoleValue(params)
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
            1000 * this.compareProjectValue(params) +
            100 * this.compareLevelValue(params) +
            10 * this.compareSpaceValue(params) +
            this.compareRoleValue(params)
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
            1000 * this.compareRoleValue(params) +
            100 * this.compareLevelValue(params) +
            10 * this.compareSpaceValue(params) +
            this.compareProjectValue(params)
          )
        }
      }
    ]
  }

  getInheritedFromValue({ row }) {
    return row.inheritedFrom.value
  }

  getLevelValue({ row }) {
    return row.level.value
  }

  getSpaceValue({ row }) {
    if (row.level.value === openbis.RoleLevel.INSTANCE) {
      return ALL_VALUE
    } else {
      return row.space.value
    }
  }

  getProjectValue({ row }) {
    if (row.level.value === openbis.RoleLevel.INSTANCE) {
      return ALL_VALUE
    } else if (row.level.value === openbis.RoleLevel.SPACE) {
      return row.space.value ? ALL_VALUE : null
    } else {
      return row.project.value
    }
  }

  getRoleValue({ row }) {
    return row.role.value
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

    if (row.inheritedFrom && row.inheritedFrom.value) {
      return <div className={classes.inherited}>{value}</div>
    } else {
      return value
    }
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
}

export default _.flow(withStyles(styles))(RolesGrid)
