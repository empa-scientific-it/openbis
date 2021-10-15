import _ from 'lodash'
import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import { DragDropContext, Droppable } from 'react-beautiful-dnd'
import Container from '@src/js/components/common/form/Container.jsx'
import IconButton from '@material-ui/core/IconButton'
import SettingsIcon from '@material-ui/icons/Settings'
import ColumnConfigRow from '@src/js/components/common/grid/ColumnConfigRow.jsx'
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

class ColumnConfig extends React.PureComponent {
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
    logger.log(logger.DEBUG, 'ColumnConfig.render')

    const { classes, columns, columnsVisibility, onVisibleChange } = this.props
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
            vertical: 'top',
            horizontal: 'center'
          }}
          transformOrigin={{
            vertical: 'bottom',
            horizontal: 'center'
          }}
        >
          <DragDropContext onDragEnd={this.handleDragEnd}>
            <Droppable droppableId='root'>
              {provided => (
                <Container>
                  <ol
                    ref={provided.innerRef}
                    {...provided.droppableProps}
                    className={classes.columns}
                  >
                    {columns.map((column, index) => (
                      <ColumnConfigRow
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
              )}
            </Droppable>
          </DragDropContext>
        </Popover>
      </div>
    )
  }
}

export default _.flow(withStyles(styles))(ColumnConfig)
