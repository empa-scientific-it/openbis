import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import { Draggable } from 'react-beautiful-dnd'
import DragHandleIcon from '@material-ui/icons/DragHandle'
import CheckboxField from '@src/js/components/common/form/CheckboxField.jsx'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  row: {
    display: 'flex',
    alignItems: 'center',
    padding: `${theme.spacing(1) / 2}px 0px`
  },
  label: {
    marginLeft: 0
  },
  drag: {
    display: 'flex',
    cursor: 'grab'
  }
})

class GridConfigRow extends React.PureComponent {
  constructor(props) {
    super(props)
    this.handleVisibleChange = this.handleVisibleChange.bind(this)
  }

  handleVisibleChange() {
    this.props.onVisibleChange({
      [this.props.column.name]: !this.props.visible
    })
  }

  render() {
    logger.log(logger.DEBUG, 'GridConfigRow.render')

    const { classes, column, visible, index } = this.props

    return (
      <Draggable draggableId={column.name} index={index}>
        {provided => (
          <div
            ref={provided.innerRef}
            {...provided.draggableProps}
            className={classes.row}
          >
            <div {...provided.dragHandleProps} className={classes.drag}>
              <DragHandleIcon fontSize='small' />
            </div>
            <CheckboxField
              label={column.label || column.name}
              value={visible}
              disabled={!column.configurable}
              onChange={this.handleVisibleChange}
            />
          </div>
        )}
      </Draggable>
    )
  }
}

export default withStyles(styles)(GridConfigRow)
