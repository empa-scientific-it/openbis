import _ from 'lodash'
import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Popover from '@material-ui/core/Popover'
import Typography from '@material-ui/core/Typography'
import Container from '@src/js/components/common/form/Container.jsx'
import Button from '@src/js/components/common/form/Button.jsx'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  container: {
    paddingLeft: theme.spacing(2.5)
  },
  row: {
    padding: `${theme.spacing(1) / 2}px 0px`
  },
  label: {
    cursor: 'pointer'
  }
})

class GridActions extends React.PureComponent {
  constructor(props) {
    super(props)
    this.state = {
      el: null
    }
    this.handleOpen = this.handleOpen.bind(this)
    this.handleClose = this.handleClose.bind(this)
    this.handleExecute = this.handleExecute.bind(this)
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

  handleExecute(action) {
    const { onExecute } = this.props
    this.handleClose()
    if (onExecute) {
      onExecute(action)
    }
  }

  render() {
    logger.log(logger.DEBUG, 'GridActions.render')

    const { actions, disabled, classes } = this.props
    const { el } = this.state

    if (!actions || actions.length === 0) {
      return null
    }

    return (
      <div className={classes.container}>
        <Button
          label={messages.get(messages.ACTIONS)}
          disabled={disabled}
          onClick={this.handleOpen}
        />
        <Popover
          open={Boolean(el)}
          anchorEl={el}
          onClose={this.handleClose}
          anchorOrigin={{
            vertical: 'top',
            horizontal: 'left'
          }}
          transformOrigin={{
            vertical: 'bottom',
            horizontal: 'left'
          }}
        >
          <Container>
            {actions.map(action => this.renderAction(action))}
          </Container>
        </Popover>
      </div>
    )
  }

  renderAction(action) {
    const { classes } = this.props
    return (
      <div className={classes.row} onClick={() => this.handleExecute(action)}>
        <span className={classes.label}>
          <Typography variant='body2' data-part='range'>
            {action.label}
          </Typography>
        </span>
      </div>
    )
  }
}

export default _.flow(withStyles(styles))(GridActions)
