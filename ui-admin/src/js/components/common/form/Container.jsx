import React from 'react'
import { withStyles } from '@material-ui/core/styles'

const styles = theme => ({
  containerDefault: {
    padding: `${theme.spacing(1)}px ${theme.spacing(2)}px`
  },
  containerSquare: {
    padding: `${theme.spacing(2)}px ${theme.spacing(2)}px`
  }
})

class Container extends React.Component {
  render() {
    const { square = false, children, onClick, className, classes } = this.props

    return (
      <div
        className={`${
          square ? classes.containerSquare : classes.containerDefault
        } ${className}`}
        onClick={onClick}
      >
        {children}
      </div>
    )
  }
}

export default withStyles(styles)(Container)
