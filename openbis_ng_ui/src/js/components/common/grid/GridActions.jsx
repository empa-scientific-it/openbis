import _ from 'lodash'
import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Popover from '@material-ui/core/Popover'
import Container from '@src/js/components/common/form/Container.jsx'
import Button from '@src/js/components/common/form/Button.jsx'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  container: {
    padding: theme.spacing(1),
    paddingLeft: 0
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
          color='default'
          onClick={this.handleOpen}
        />
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
      <div className={classes.row}>
        <Button
          key={action.label}
          label={action.label}
          onClick={() => this.handleExecute(action)}
        />
      </div>
    )
  }
}

export default _.flow(withStyles(styles))(GridActions)
