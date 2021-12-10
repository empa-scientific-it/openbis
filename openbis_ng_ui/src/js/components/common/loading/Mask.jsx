import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import util from '@src/js/common/util.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  container: {
    display: 'block',
    minWidth: '100%',
    height: '100%',
    position: 'relative'
  },
  mask: {
    position: 'absolute',
    width: '100%',
    height: '100%',
    zIndex: 1000,
    backgroundColor: theme.palette.background.paper,
    opacity: 0.6,
    textAlign: 'center'
  },
  icon: {
    position: 'absolute',
    top: '20%',
    left: 'calc(50% - 20px)',
    zIndex: 1001
  }
})

class Mask extends React.Component {
  render() {
    logger.log(logger.DEBUG, 'Mask.render')

    const { visible, icon, children, styles = {}, classes } = this.props

    return (
      <div className={util.classNames(classes.container, styles.root)}>
        {visible && (
          <React.Fragment>
            <div className={classes.mask}></div>
            {icon ? <div className={classes.icon}>{icon}</div> : null}
          </React.Fragment>
        )}
        {children}
      </div>
    )
  }
}

export default withStyles(styles)(Mask)
