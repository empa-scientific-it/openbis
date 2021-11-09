import _ from 'lodash'
import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import { DragDropContext, Droppable } from 'react-beautiful-dnd'
import Mask from '@src/js/components/common/loading/Mask.jsx'
import Container from '@src/js/components/common/form/Container.jsx'
import IconButton from '@material-ui/core/IconButton'
import SettingsIcon from '@material-ui/icons/Settings'
import GridConfigRow from '@src/js/components/common/grid/GridConfigRow.jsx'
import Popover from '@material-ui/core/Popover'
import logger from '@src/js/common/logger.js'

const styles = () => ({
  container: {
    display: 'flex',
    alignItems: 'center'
  },
  columns: {
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

  render() {
    logger.log(logger.DEBUG, 'GridConfig.render')

    const { classes, loading, columns, columnsVisibility, onVisibleChange } =
      this.props
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
            horizontal: 'right'
          }}
          transformOrigin={{
            vertical: 'top',
            horizontal: 'right'
          }}
        >
          <DragDropContext onDragEnd={this.handleDragEnd}>
            <Droppable droppableId='root'>
              {provided => (
                <Mask visible={loading}>
                  <Container>
                    <ol
                      ref={provided.innerRef}
                      {...provided.droppableProps}
                      className={classes.columns}
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
                  </Container>
                </Mask>
              )}
            </Droppable>
          </DragDropContext>
        </Popover>
      </div>
    )
  }
}

export default _.flow(withStyles(styles))(GridConfig)
