import _ from 'lodash'
import React from 'react'
import autoBind from 'auto-bind'
import { withStyles } from '@material-ui/core/styles'
import { DragDropContext, Droppable } from 'react-beautiful-dnd'
import Button from '@src/js/components/common/form/Button.jsx'
import Mask from '@src/js/components/common/loading/Mask.jsx'
import Container from '@src/js/components/common/form/Container.jsx'
import Link from '@src/js/components/common/form/Link.jsx'
import SettingsIcon from '@material-ui/icons/Settings'
import GridConfigRow from '@src/js/components/common/grid/GridConfigRow.jsx'
import Popover from '@material-ui/core/Popover'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  container: {
    display: 'flex',
    alignItems: 'center',
    paddingRight: theme.spacing(1)
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
    autoBind(this)
    this.state = {
      el: null
    }
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

  handleShowAll() {
    this.handleVisibleChangeAll(true)
  }

  handleHideAll() {
    this.handleVisibleChangeAll(false)
  }

  handleVisibleChangeAll(visibility) {
    const { columns, onVisibleChange } = this.props

    const visibilityMap = columns.reduce((map, column) => {
      map[column.name] = visibility
      return map
    }, {})

    onVisibleChange(visibilityMap)
  }

  render() {
    logger.log(logger.DEBUG, 'GridConfig.render')

    const { classes, loading } = this.props
    const { el } = this.state

    return (
      <div className={classes.container}>
        <Button
          label='Columns'
          color='default'
          variant='outlined'
          onClick={this.handleOpen}
        >
          <SettingsIcon fontSize='small' />
        </Button>
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
            <Container>{this.renderColumns()}</Container>
          </Mask>
        </Popover>
      </div>
    )
  }

  renderColumns() {
    const { classes, columns, columnsVisibility, onVisibleChange } = this.props
    return (
      <div>
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
        <br />
        <Button
          label={messages.get(messages.SHOW_ALL)}
          onClick={this.handleShowAll}
        />
        <span>&nbsp;</span>
        <Button
          label={messages.get(messages.HIDE_ALL)}
          onClick={this.handleHideAll}
        />
      </div>
    )
  }
}

export default _.flow(withStyles(styles))(GridConfig)
