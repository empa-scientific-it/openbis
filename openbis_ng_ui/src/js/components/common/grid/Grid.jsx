import _ from 'lodash'
import React from 'react'
import autoBind from 'auto-bind'
import { connect } from 'react-redux'
import { withStyles } from '@material-ui/core/styles'
import Loading from '@src/js/components/common/loading/Loading.jsx'
import Table from '@material-ui/core/Table'
import TableBody from '@material-ui/core/TableBody'
import Header from '@src/js/components/common/form/Header.jsx'
import GridController from '@src/js/components/common/grid/GridController.js'
import GridHeader from '@src/js/components/common/grid/GridHeader.jsx'
import GridRow from '@src/js/components/common/grid/GridRow.jsx'
import GridPaging from '@src/js/components/common/grid/GridPaging.jsx'
import ColumnConfig from '@src/js/components/common/grid/ColumnConfig.jsx'
import ComponentContext from '@src/js/components/common/ComponentContext.js'
import selectors from '@src/js/store/selectors/selectors.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  container: {
    height: '100%'
  },
  tableHeaderAndBody: {
    width: '100%',
    overflow: 'auto'
  },
  table: {
    borderCollapse: 'unset',
    marginTop: -theme.spacing(1)
  },
  tableBody: {
    '& tr:last-child td': {
      border: 0
    }
  },
  tableFooter: {
    position: 'sticky',
    bottom: 0,
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'flex-end',
    borderTopWidth: '1px',
    borderTopStyle: 'solid',
    borderTopColor: theme.palette.border.secondary,
    backgroundColor: theme.palette.background.paper
  }
})

function mapStateToProps(state) {
  return {
    session: selectors.getSession(state)
  }
}

class Grid extends React.PureComponent {
  constructor(props) {
    super(props)
    autoBind(this)

    this.state = {}

    if (this.props.controller) {
      this.controller = this.props.controller
    } else {
      this.controller = new GridController()
    }

    this.controller.init(new ComponentContext(this))

    if (this.props.controllerRef) {
      this.props.controllerRef(this.controller)
    }
  }

  componentDidMount() {
    this.controller.load()
  }

  componentDidUpdate(prevProps) {
    if (
      this.props.rows !== prevProps.rows ||
      this.props.totalCount !== prevProps.totalCount
    ) {
      this.controller.updateRows(this.props.rows, this.props.totalCount)
    }
    if (this.props.selectedRowId !== prevProps.selectedRowId) {
      this.controller.updateSelectedRowId(this.props.selectedRowId)
    }
  }

  handleClickContainer() {
    const { selectedRowId, onSelectedRowChange } = this.props

    if (!selectedRowId && !onSelectedRowChange) {
      this.controller.handleRowSelect(null)
    }
  }

  handleClickTable(event) {
    event.stopPropagation()
  }

  render() {
    logger.log(logger.DEBUG, 'Grid.render')

    if (!this.state.loaded) {
      return <Loading loading={true}></Loading>
    }

    const { header, classes } = this.props
    const {
      loading,
      filters,
      sort,
      sortDirection,
      page,
      pageSize,
      columns,
      currentRows,
      selectedRow,
      totalCount
    } = this.state

    return (
      <div onClick={this.handleClickContainer} className={classes.container}>
        <div>{header && <Header>{header}</Header>}</div>
        <div>
          <Loading loading={loading}>
            <div onClick={this.handleClickTable}>
              <div className={classes.tableHeaderAndBody}>
                <Table classes={{ root: classes.table }}>
                  <GridHeader
                    columns={columns}
                    filters={filters}
                    sort={sort}
                    sortDirection={sortDirection}
                    onSortChange={this.controller.handleSortChange}
                    onFilterChange={this.controller.handleFilterChange}
                  />
                  <TableBody classes={{ root: classes.tableBody }}>
                    {currentRows.map(row => {
                      return (
                        <GridRow
                          key={row.id}
                          columns={columns}
                          row={row}
                          selected={
                            selectedRow ? selectedRow.id === row.id : false
                          }
                          onClick={this.controller.handleRowSelect}
                        />
                      )
                    })}
                  </TableBody>
                </Table>
              </div>
              <div className={classes.tableFooter}>
                <GridPaging
                  count={totalCount}
                  page={page}
                  pageSize={pageSize}
                  onPageChange={this.controller.handlePageChange}
                  onPageSizeChange={this.controller.handlePageSizeChange}
                />
                <ColumnConfig
                  columns={columns}
                  onVisibleChange={this.controller.handleColumnVisibleChange}
                  onOrderChange={this.controller.handleColumnOrderChange}
                />
              </div>
            </div>
          </Loading>
        </div>
      </div>
    )
  }
}

export default _.flow(connect(mapStateToProps, null), withStyles(styles))(Grid)
