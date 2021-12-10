import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Checkbox from '@material-ui/core/Checkbox'
import Typography from '@material-ui/core/Typography'
import FormFieldContainer from '@src/js/components/common/form/FormFieldContainer.jsx'
import FormFieldLabel from '@src/js/components/common/form/FormFieldLabel.jsx'
import logger from '@src/js/common/logger.js'

const styles = () => ({
  container: {
    display: 'flex',
    alignItems: 'center'
  },
  label: {
    cursor: 'pointer'
  },
  labelDisabled: {
    cursor: 'inherit'
  },
  checkbox: {
    padding: '2px',
    marginRight: '4px'
  }
})

class CheckboxFormField extends React.PureComponent {
  static defaultProps = {
    mode: 'edit'
  }

  constructor(props) {
    super(props)
    this.reference = React.createRef()
    this.action = null
    this.handleLabelClick = this.handleLabelClick.bind(this)
    this.handleChange = this.handleChange.bind(this)
    this.handleFocus = this.handleFocus.bind(this)
  }

  handleLabelClick() {
    this.getReference().current.click()
  }

  handleChange(event) {
    this.handleEvent(event, this.props.onChange)
  }

  handleFocus(event) {
    this.handleEvent(event, this.props.onFocus)
    if (this.action) {
      this.action.focusVisible()
    }
  }

  handleEvent(event, handler) {
    if (handler) {
      const newEvent = {
        ...event,
        target: {
          ...event.target,
          name: event.target.value,
          value: event.target.checked
        }
      }
      delete newEvent.target.checked
      handler(newEvent)
    }
  }

  render() {
    logger.log(logger.DEBUG, 'CheckboxFormField.render')

    const {
      name,
      label,
      description,
      value,
      mandatory,
      disabled,
      indeterminate,
      error,
      metadata,
      mode,
      styles,
      classes,
      onClick
    } = this.props

    if (mode !== 'view' && mode !== 'edit') {
      throw 'Unsupported mode: ' + mode
    }

    const isDisabled = disabled || mode !== 'edit'

    return (
      <FormFieldContainer
        description={description}
        error={error}
        metadata={metadata}
        styles={styles}
        onClick={onClick}
      >
        <div className={classes.container}>
          <Checkbox
            inputRef={this.getReference()}
            action={action => (this.action = action)}
            value={name}
            checked={!!value}
            disabled={isDisabled}
            indeterminate={indeterminate}
            onChange={this.handleChange}
            onFocus={this.handleFocus}
            onClick={onClick}
            classes={{ root: classes.checkbox }}
            size='small'
          />
          <Typography
            component='label'
            className={isDisabled ? classes.labelDisabled : classes.label}
            onClick={this.handleLabelClick}
          >
            <FormFieldLabel
              label={label}
              mandatory={mandatory}
              styles={styles}
              onClick={onClick}
            />
          </Typography>
        </div>
      </FormFieldContainer>
    )
  }

  getReference() {
    return this.props.reference ? this.props.reference : this.reference
  }
}

export default withStyles(styles)(CheckboxFormField)
