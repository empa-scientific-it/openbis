import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Typography from '@material-ui/core/Typography'
import messages from '@src/js/common/messages.js'

const styles = theme => ({
  container: {
    padding: theme.spacing(2),
    height: '100%',
    overflow: 'scroll',
    boxSizing: 'border-box'
  },
  header: {
    paddingBottom: theme.spacing(2)
  }
})

class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props)
    this.state = {}
  }

  componentDidCatch(error, errorInfo) {
    this.setState({ error, errorInfo })
  }

  render() {
    const { classes } = this.props
    const { error, errorInfo } = this.state

    if (error) {
      return (
        <section className={classes.container}>
          <Typography variant='h4' className={classes.header}>
            {messages.get(messages.CRASH)}
          </Typography>
          {this.renderError(error)}
          {this.renderErrorInfo(errorInfo)}
        </section>
      )
    } else {
      return <React.Fragment>{this.props.children}</React.Fragment>
    }
  }

  renderError(error) {
    if (error) {
      return <div>{error.toString()}</div>
    } else {
      return null
    }
  }

  renderErrorInfo(errorInfo) {
    if (errorInfo && errorInfo.componentStack) {
      const frames = errorInfo.componentStack.split('\n')
      const elements = []

      frames.forEach((frame, index) => {
        if (frame.trim().length > 0) {
          elements.push(<div key={index}>{frame.trim()}</div>)
        }
      })

      return elements
    } else {
      return null
    }
  }
}

export default withStyles(styles)(ErrorBoundary)
