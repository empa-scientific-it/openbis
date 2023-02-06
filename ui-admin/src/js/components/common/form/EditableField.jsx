import _ from 'lodash'
import React from 'react'
import EditIcon from '@material-ui/icons/EditOutlined'
import { withStyles } from '@material-ui/core/styles'
import util from '@src/js/common/util.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  container: {
    display: 'inline-flex',
    margin: '1px 30px 1px 1px',
    padding: '10px',
    minHeight: '24px',
    alignItems: 'center'
  },
  icon: {
    paddingLeft: '5px'
  },
  hovered: {
    margin: '0px',
    border: '1px solid',
    borderColor: theme.palette.action.selected
  }
})

class EditableField extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      edited: false,
      hovered: false
    }
    this.ref = React.createRef()
    this.handleMouseEnter = this.handleMouseEnter.bind(this)
    this.handleMouseLeave = this.handleMouseLeave.bind(this)
    this.handleClick = this.handleClick.bind(this)
    this.handleBlur = this.handleBlur.bind(this)
  }

  handleMouseEnter() {
    this.setState(() => ({
      hovered: true
    }))
  }

  handleMouseLeave() {
    this.setState(() => ({
      hovered: false
    }))
  }

  handleClick(event) {
    event.stopPropagation()

    if (!this.state.edited) {
      this.setState(
        () => ({
          edited: true
        }),
        () => {
          this.ref.current.focus()
        }
      )
    }
  }

  handleBlur() {
    if (this.state.edited) {
      this.setState(() => ({
        edited: false,
        hovered: false
      }))
    }
  }

  render() {
    logger.log(logger.DEBUG, 'EditableField.render')

    const { classes } = this.props
    const { edited, hovered } = this.state

    let classNames = util.classNames(
      classes.container,
      hovered ? classes.hovered : null,
      edited ? classes.edited : null
    )

    if (edited) {
      return (
        <span className={classNames} onClick={this.handleClick}>
          {this.props.renderField({
            edited: true,
            handleBlur: this.handleBlur,
            ref: this.ref
          })}
        </span>
      )
    } else {
      return (
        <span
          className={classNames}
          onMouseEnter={this.handleMouseEnter}
          onMouseLeave={this.handleMouseLeave}
          onClick={this.handleClick}
        >
          {this.props.renderField({
            edited: false,
            handleBlur: this.handleBlur,
            ref: this.ref
          })}
          {hovered && <EditIcon className={classes.icon} />}
        </span>
      )
    }
  }
}

export default _.flow(withStyles(styles))(EditableField)
