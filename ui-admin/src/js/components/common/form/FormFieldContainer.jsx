import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import FormControl from '@material-ui/core/FormControl'
import FormHelperText from '@material-ui/core/FormHelperText'
import InfoIcon from '@material-ui/icons/Info'
import Tooltip from '@src/js/components/common/form/Tooltip.jsx'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  container: {},
  subcontainer: {
    display: 'flex',
    flexDirection: 'row',
    alignItems: 'center'
  },
  tooltip: {
    fontSize: theme.typography.body2.fontSize
  },
  metadataDefault: {
    fontSize: theme.typography.label.fontSize,
    color: theme.typography.label.color,
    display: 'inline-block',
    margin: 0,
    marginBottom: theme.spacing(1) / 2
  },
  controlDefault: {
    flex: '1 1 auto'
  },
  descriptionDefault: {
    flex: '0 0 auto',
    marginLeft: theme.spacing(1) / 2,
    lineHeight: '0.7rem',
    '& svg': {
      color: theme.palette.hint.main
    },
    cursor: 'pointer'
  },
  errorDefault: {
    fontSize: theme.typography.label.fontSize,
    color: theme.palette.error.main
  }
})

class FormFieldContainer extends React.PureComponent {
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
    logger.log(logger.DEBUG, 'FormField.render')

    const { styles = {}, classes } = this.props

    return (
      <FormControl
        fullWidth={true}
        margin='none'
        classes={{ root: classes.container }}
      >
        <div
          data-part='container'
          onClick={this.handleClick}
          className={styles.container}
        >
          {this.renderMetadata()}
          <div className={classes.subcontainer}>
            {this.renderControl()}
            {this.renderDescription()}
          </div>
          {this.renderError()}
        </div>
      </FormControl>
    )
  }

  renderMetadata() {
    const { metadata, classes, styles = {} } = this.props

    if (metadata) {
      return (
        <pre
          data-part='metadata'
          onClick={this.handleClick}
          className={`${classes.metadataDefault} ${styles.metadata}`}
        >
          {metadata}
        </pre>
      )
    } else {
      return null
    }
  }

  renderControl() {
    const { children, classes, styles = {} } = this.props
    return (
      <div
        data-part='control'
        onClick={this.handleClick}
        className={`${classes.controlDefault} ${styles.control}`}
      >
        {children}
      </div>
    )
  }

  renderDescription() {
    const { description, classes, styles = {} } = this.props

    if (description) {
      return (
        <span
          data-part='description'
          onClick={this.handleClick}
          className={`${classes.descriptionDefault} ${styles.description}`}
        >
          <Tooltip title={description}>
            <InfoIcon fontSize='small' />
          </Tooltip>
        </span>
      )
    } else {
      return null
    }
  }

  renderError() {
    const { error, classes, styles = {} } = this.props

    if (error) {
      return (
        <FormHelperText>
          <span
            data-part='error'
            onClick={this.handleClick}
            className={`${classes.errorDefault} ${styles.error}`}
          >
            {error}
          </span>
        </FormHelperText>
      )
    } else {
      return null
    }
  }
}

export default withStyles(styles)(FormFieldContainer)
