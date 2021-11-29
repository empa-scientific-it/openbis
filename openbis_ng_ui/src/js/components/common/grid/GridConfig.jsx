import _ from 'lodash'
import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import { DragDropContext, Droppable } from 'react-beautiful-dnd'
import Mask from '@src/js/components/common/loading/Mask.jsx'
import Container from '@src/js/components/common/form/Container.jsx'
import Header from '@src/js/components/common/form/Header.jsx'
import SelectField from '@src/js/components/common/form/SelectField.jsx'
import IconButton from '@material-ui/core/IconButton'
import SettingsIcon from '@material-ui/icons/Settings'
import GridConfigRow from '@src/js/components/common/grid/GridConfigRow.jsx'
import GridFilterOptions from '@src/js/components/common/grid/GridFilterOptions.js'
import Popover from '@material-ui/core/Popover'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  container: {
    display: 'flex',
    alignItems: 'center'
  },
  filters: {
    paddingBottom: theme.spacing(1)
  },
  columnsList: {
    listStyle: 'none',
    margin: 0,
    padding: 0
  }
})

class GridConfig extends React.PureComponent {
  constructor(props) {
    super(props)
    this.state = {
      el: null
    }
    this.handleOpen = this.handleOpen.bind(this)
    this.handleClose = this.handleClose.bind(this)
    this.handleDragEnd = this.handleDragEnd.bind(this)
    this.handleFilterModeChange = this.handleFilterModeChange.bind(this)
  }

  handleOpen(event) {
    this.setState({
      el: event.currentTarget
    })
  }

  handleClose() {
    this.setState({
      el: null
    })
  }

  handleDragEnd(result) {
    if (!result.destination) {
      return
    }
    this.props.onOrderChange(result.source.index, result.destination.index)
  }

  handleFilterModeChange(event) {
    const { onFilterModeChange } = this.props
    if (onFilterModeChange) {
      onFilterModeChange(event.target.value)
    }
  }

  render() {
    logger.log(logger.DEBUG, 'GridConfig.render')

    const { classes, loading } = this.props
    const { el } = this.state

    return (
      <div className={classes.container}>
        <IconButton onClick={this.handleOpen}>
          <SettingsIcon fontSize='small' />
        </IconButton>
        <Popover
          open={Boolean(el)}
          anchorEl={el}
          onClose={this.handleClose}
          anchorOrigin={{
            vertical: 'bottom',
            horizontal: 'left'
          }}
          transformOrigin={{
            vertical: 'top',
            horizontal: 'left'
          }}
        >
          <Mask visible={loading}>
            <Container>
              {this.renderFilters()}
              {this.renderColumns()}
            </Container>
          </Mask>
        </Popover>
      </div>
    )
  }

  renderFilters() {
    const { classes, filterMode } = this.props
    return (
      <div className={classes.filters}>
        <Header size='small'>{messages.get(messages.FILTERS)}</Header>
        <SelectField
          name='filters'
          options={[
            {
              label: messages.get(messages.GLOBAL_FILTER),
              value: GridFilterOptions.GLOBAL_FILTER
            },
            {
              label: messages.get(messages.COLUMN_FILTERS),
              value: GridFilterOptions.COLUMN_FILTERS
            }
          ]}
          value={filterMode}
          variant='standard'
          onChange={this.handleFilterModeChange}
        />
      </div>
    )
  }

  renderColumns() {
    const { classes, columns, columnsVisibility, onVisibleChange } = this.props
    return (
      <div>
        <Header size='small'>{messages.get(messages.COLUMNS)}</Header>
        <DragDropContext onDragEnd={this.handleDragEnd}>
          <Droppable droppableId='root'>
            {provided => (
              <ol
                ref={provided.innerRef}
                {...provided.droppableProps}
                className={classes.columnsList}
              >
                {columns.map((column, index) => (
                  <GridConfigRow
                    key={column.name}
                    column={column}
                    visible={columnsVisibility[column.name]}
                    index={index}
                    onVisibleChange={onVisibleChange}
                  />
                ))}
                {provided.placeholder}
              </ol>
            )}
          </Droppable>
        </DragDropContext>
      </div>
    )
  }
}

export default _.flow(withStyles(styles))(GridConfig)
