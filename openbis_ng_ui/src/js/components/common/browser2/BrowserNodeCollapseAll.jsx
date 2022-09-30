import React from 'react'
import autoBind from 'auto-bind'
import { withStyles } from '@material-ui/core/styles'
import Tooltip from '@src/js/components/common/form/Tooltip.jsx'
import IconButton from '@material-ui/core/IconButton'
import UnfoldLessIcon from '@material-ui/icons/UnfoldLess'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

const styles = () => ({
  button: {
    padding: '4px',
    margin: '-4px'
  }
})

class BrowserNodeCollapseAll extends React.PureComponent {
  constructor(props) {
    super(props)
    autoBind(this)
  }

  handleClick(event) {
    event.preventDefault()
    event.stopPropagation()
    const { node } = this.props
    this.props.onClick(node.id)
  }

  render() {
    logger.log(logger.DEBUG, 'BrowserNodeCollapseAll.render')

    const { node, classes } = this.props

    if (!node || !node.canHaveChildren) {
      return null
    }

    return (
      <Tooltip title={messages.get(messages.COLLAPSE_ALL)}>
        <IconButton
          size='small'
          onClick={this.handleClick}
          classes={{ root: classes.button }}
        >
          <UnfoldLessIcon fontSize='small' />
        </IconButton>
      </Tooltip>
    )
  }
}

export default withStyles(styles)(BrowserNodeCollapseAll)
