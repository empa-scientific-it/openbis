import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Link from '@material-ui/core/Link'

const styles = theme => ({
  link: {
    fontSize: 'inherit',
    fontFamily: theme.typography.fontFamily,
    cursor: 'pointer'
  }
})

class LinkComponent extends React.Component {
  render() {
    const { href, onClick, children, classes } = this.props
    return (
      <Link href={href} onClick={onClick} classes={{ root: classes.link }}>
        {children}
      </Link>
    )
  }
}

export default withStyles(styles)(LinkComponent)
