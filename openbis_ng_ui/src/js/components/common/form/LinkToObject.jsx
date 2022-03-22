import React from 'react'
import autoBind from 'auto-bind'
import { withStyles } from '@material-ui/core/styles'
import AppController from '@src/js/components/AppController.js'
import Link from '@material-ui/core/Link'

const styles = () => ({
  link: {
    fontSize: 'inherit'
  }
})

class LinkToObject extends React.Component {
  constructor(props) {
    super(props)
    autoBind(this)
  }

  handleClick() {
    const { page, object } = this.props
    AppController.getInstance().objectOpen(page, object.type, object.id)
  }

  render() {
    const { children, classes } = this.props
    return (
      <Link
        component='button'
        classes={{ root: classes.link }}
        onClick={this.handleClick}
      >
        {children}
      </Link>
    )
  }
}

export default withStyles(styles)(LinkToObject)
