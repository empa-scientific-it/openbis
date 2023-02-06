import React from 'react'
import autoBind from 'auto-bind'
import { withStyles } from '@material-ui/core/styles'
import Tooltip from '@src/js/components/common/form/Tooltip.jsx'
import IconButton from '@material-ui/core/IconButton'
import AccountTreeIcon from '@material-ui/icons/AccountTreeOutlined'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  container: {
    paddingLeft: theme.spacing(2)
  },
  button: {
    padding: '4px',
    margin: '-4px'
  }
})

class BrowserNodeSetAsRoot extends React.PureComponent {
  constructor(props) {
    super(props)
    autoBind(this)
  }

  handleClick(event) {
    event.preventDefault()
    event.stopPropagation()
    const { node } = this.props
    this.props.onClick(node)
  }

  render() {
    logger.log(logger.DEBUG, 'BrowserNodeSetAsRoot.render')

    const { node, classes } = this.props

    if (!node || !node.rootable) {
      return null
    }

    return (
      <div className={classes.container}>
        <Tooltip title={messages.get(messages.SET_AS_ROOT)}>
          <IconButton
            size='small'
            onClick={this.handleClick}
            classes={{ root: classes.button }}
          >
            <AccountTreeIcon fontSize='small' />
          </IconButton>
        </Tooltip>
      </div>
    )
  }
}

export default withStyles(styles)(BrowserNodeSetAsRoot)
