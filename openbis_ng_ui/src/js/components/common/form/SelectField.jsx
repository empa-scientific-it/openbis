import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import TextField from '@material-ui/core/TextField'
import MenuItem from '@material-ui/core/MenuItem'
import logger from '@src/js/common/logger.js'

import FormFieldContainer from './FormFieldContainer.jsx'
import FormFieldLabel from './FormFieldLabel.jsx'
import FormFieldView from './FormFieldView.jsx'

const styles = theme => ({
  textField: {
    margin: 0
  },
  select: {
    fontSize: theme.typography.body2.fontSize
  },
  option: {
    '&:after': {
      content: '"\\00a0"'
    },
    fontSize: theme.typography.body2.fontSize
  }
})

class SelectFormField extends React.PureComponent {
  static defaultProps = {
    mode: 'edit',
    variant: 'filled'
  }

  constructor(props) {
    super(props)
    this.inputReference = React.createRef()
    this.handleFocus = this.handleFocus.bind(this)
    this.handleBlur = this.handleBlur.bind(this)
  }

  handleFocus(event) {
    this.handleEvent(event, this.props.onFocus)
  }

  handleBlur(event) {
    this.handleEvent(event, this.props.onBlur)
  }

  handleEvent(event, handler) {
    if (handler) {
      const newEvent = {
        ...event,
        target: {
          name: this.props.name,
          value: this.props.value
        }
      }
      handler(newEvent)
    }
  }

  render() {
    logger.log(logger.DEBUG, 'SelectFormField.render')

    const { mode } = this.props

    if (mode === 'view') {
      return this.renderView()
    } else if (mode === 'edit') {
      return this.renderEdit()
    } else {
      throw 'Unsupported mode: ' + mode
    }
  }

  renderView() {
    const { label, value, options } = this.props
    const option = options.find(option => option.value === value)
    return <FormFieldView label={label} value={option ? option.label : null} />
  }

  renderEdit() {
    const {
      reference,
      name,
      label,
      description,
      value,
      mandatory,
      disabled,
      error,
      options,
      metadata,
      styles,
      onChange,
      onClick,
      classes,
      variant
    } = this.props

    this.fixReference(reference)

    return (
      <FormFieldContainer
        description={description}
        error={error}
        metadata={metadata}
        styles={styles}
        onClick={onClick}
      >
        <TextField
          select
          inputRef={this.inputReference}
          label={
            label ? (
              <FormFieldLabel
                label={label}
                mandatory={mandatory}
                styles={styles}
              />
            ) : null
          }
          name={name}
          value={value || ''}
          error={!!error}
          disabled={disabled}
          onChange={onChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
          fullWidth={true}
          InputLabelProps={{ shrink: !!value }}
          SelectProps={{
            MenuProps: {
              getContentAnchorEl: null,
              anchorOrigin: {
                vertical: 'bottom',
                horizontal: 'left'
              }
            },
            classes: {
              root: classes.select
            }
          }}
          variant={variant}
          margin='dense'
          classes={{
            root: classes.textField
          }}
        >
          {options &&
            options.map(option => (
              <MenuItem
                key={option.value || ''}
                value={option.value || ''}
                classes={{ root: classes.option }}
              >
                {option.label || option.value || ''}
              </MenuItem>
            ))}
        </TextField>
      </FormFieldContainer>
    )
  }

  fixReference(reference) {
    if (reference) {
      reference.current = {
        focus: () => {
          if (this.inputReference.current && this.inputReference.current.node) {
            const input = this.inputReference.current.node
            const div = input.previousSibling
            div.focus()
          }
        }
      }
    }
  }
}

export default withStyles(styles)(SelectFormField)
