import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Link from '@material-ui/core/Link'

const styles = theme => ({
  link: {
    fontSize: 'inherit',
    fontFamily: theme.typography.fontFamily
  }
})

class LinkComponent extends React.Component {
  render() {
    const { href, children, classes } = this.props
    return (
      <Link href={href} classes={{ root: classes.link }}>
        {children}
      </Link>
    )
  }
}

export default withStyles(styles)(LinkComponent)
