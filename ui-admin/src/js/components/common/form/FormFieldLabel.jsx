import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  labelDefault: {
    fontSize: theme.typography.body2.fontSize,
    marginRight: theme.spacing(1)
  },
  mandatoryDefault: {
    fontWeight: 'bold',
    color: theme.palette.error.main
  }
})

class FormFieldLabel extends React.PureComponent {
  constructor(props) {
    super(props)
    this.handleClick = this.handleClick.bind(this)
  }

  handleClick(event) {
    const { onClick } = this.props
    if (onClick) {
      event.stopPropagation()
      event.originalTarget = event.target
      event.target = event.currentTarget
      onClick(event)
    }
  }

  render() {
    logger.log(logger.DEBUG, 'FormFieldLabel.render')

    const { label, mandatory, styles = {}, classes } = this.props

    return (
      <span className={classes.container}>
        {label && (
          <span
            data-part='label'
            onClick={this.handleClick}
            className={`${classes.labelDefault} ${styles.label}`}
          >
            {label}
          </span>
        )}
        {mandatory && (
          <span
            data-part='mandatory'
            onClick={this.handleClick}
            className={`${classes.mandatoryDefault} ${styles.mandatory}`}
          >
            *
          </span>
        )}
      </span>
    )
  }
}

export default withStyles(styles)(FormFieldLabel)
