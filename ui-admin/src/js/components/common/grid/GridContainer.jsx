import React from 'react'
import { withStyles } from '@material-ui/core/styles'

const styles = () => ({
  container: {
    padding: 0,
    height: '100%',
    boxSizing: 'border-box'
  }
})

class GridContainer extends React.Component {
  render() {
    const { classes, onClick, children } = this.props
    return (
      <div className={classes.container} onClick={onClick}>
        {children}
      </div>
    )
  }
}

export default withStyles(styles)(GridContainer)
