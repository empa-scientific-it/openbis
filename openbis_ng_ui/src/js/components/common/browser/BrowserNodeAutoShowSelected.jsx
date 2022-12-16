import React from 'react'
import autoBind from 'auto-bind'
import { withStyles } from '@material-ui/core/styles'
import Tooltip from '@src/js/components/common/form/Tooltip.jsx'
import IconButton from '@material-ui/core/IconButton'
import MyLocation from '@material-ui/icons/MyLocation'
import LocationDisabled from '@material-ui/icons/LocationDisabled'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

const styles = () => ({
  button: {
    padding: '4px',
    margin: '-4px'
  }
})

class BrowserNodeAutoShowSelected extends React.PureComponent {
  constructor(props) {
    super(props)
    autoBind(this)
  }

  handleClick(event) {
    event.preventDefault()
    event.stopPropagation()
    this.props.onClick()
  }

  render() {
    logger.log(logger.DEBUG, 'BrowserNodeAutoShowSelected.render')

    const { value, classes } = this.props

    return (
      <Tooltip title={messages.get(messages.AUTO_SHOW_SELECTED)}>
        <IconButton
          size='small'
          onClick={this.handleClick}
          classes={{ root: classes.button }}
        >
          {value ? (
            <MyLocation fontSize='small' />
          ) : (
            <LocationDisabled fontSize='small' />
          )}
        </IconButton>
      </Tooltip>
    )
  }
}

export default withStyles(styles)(BrowserNodeAutoShowSelected)
