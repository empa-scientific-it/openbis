import React from 'react'
import autoBind from 'auto-bind'
import { withStyles } from '@material-ui/core/styles'
import Tooltip from '@src/js/components/common/form/Tooltip.jsx'
import IconButton from '@material-ui/core/IconButton'
import UnfoldLessIcon from '@material-ui/icons/UnfoldLess'
import UnfoldMoreIcon from '@material-ui/icons/UnfoldMore'
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

class BrowserNodeCollapseExpandAll extends React.PureComponent {
  constructor(props) {
    super(props)
    autoBind(this)
  }

  handleClick(event) {
    event.preventDefault()
    event.stopPropagation()
    const { node, expand } = this.props
    if (node.id) {
      if (expand) {
        this.props.onExpandAll(node.id)
      } else {
        this.props.onCollapseAll(node.id)
      }
    }
  }

  render() {
    logger.log(logger.DEBUG, 'BrowserNodeCollapseAll.render')

    const { node, expand, classes } = this.props

    if (!node || !node.canHaveChildren) {
      return null
    }

    return (
      <div className={classes.container}>
        <Tooltip
          title={messages.get(
            expand ? messages.EXPAND_ALL : messages.COLLAPSE_ALL
          )}
        >
          <IconButton
            size='small'
            onClick={this.handleClick}
            classes={{ root: classes.button }}
          >
            {expand ? (
              <UnfoldMoreIcon fontSize='small' />
            ) : (
              <UnfoldLessIcon fontSize='small' />
            )}
          </IconButton>
        </Tooltip>
      </div>
    )
  }
}

export default withStyles(styles)(BrowserNodeCollapseExpandAll)
