import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import TextField from '@material-ui/core/TextField'
import InputAdornment from '@material-ui/core/InputAdornment'
import FormFieldContainer from '@src/js/components/common/form/FormFieldContainer.jsx'
import FormFieldLabel from '@src/js/components/common/form/FormFieldLabel.jsx'
import FormFieldView from '@src/js/components/common/form/FormFieldView.jsx'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  startAdornment: {
    marginRight: 0
  },
  endAdornment: {
    marginLeft: 0
  },
  textField: {
    margin: 0,
    '& textarea': {
      minHeight: '19px !important'
    }
  },
  input: {
    fontSize: theme.typography.body2.fontSize
  },
  inputDisabled: {
    pointerEvents: 'none'
  }
})

class TextFormField extends React.PureComponent {
  static defaultProps = {
    mode: 'edit',
    variant: 'filled',
    autoComplete: 'off'
  }

  render() {
    logger.log(logger.DEBUG, 'TextFormField.render')

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
    const { label, value, description } = this.props
    return <FormFieldView label={label} value={value} description={description} />
  }

  renderEdit() {
    const {
      reference,
      id,
      type,
      name,
      label,
      description,
      value,
      mandatory,
      disabled,
      autoComplete,
      error,
      multiline,
      metadata,
      startAdornment,
      endAdornment,
      styles,
      classes,
      variant,
      onClick,
      onKeyPress,
      onChange,
      onFocus,
      onBlur
    } = this.props

    return (
      <FormFieldContainer
        description={description}
        error={error}
        metadata={metadata}
        styles={styles}
        onClick={onClick}
      >
        <TextField
          inputRef={reference}
          id={id}
          type={type}
          label={
            <FormFieldLabel
              label={label}
              mandatory={mandatory}
              styles={styles}
              onClick={onClick}
            />
          }
          InputProps={{
            startAdornment: startAdornment ? (
              <InputAdornment
                position='start'
                classes={{ positionStart: classes.startAdornment }}
              >
                {startAdornment}
              </InputAdornment>
            ) : null,
            endAdornment: endAdornment ? (
              <InputAdornment
                position='end'
                classes={{ positionEnd: classes.endAdornment }}
              >
                {endAdornment}
              </InputAdornment>
            ) : null,
            classes: {
              input: classes.input,
              disabled: classes.inputDisabled
            }
          }}
          name={name}
          value={value || ''}
          error={!!error}
          disabled={disabled}
          multiline={multiline}
          onKeyPress={onKeyPress}
          onChange={onChange}
          onFocus={onFocus}
          onBlur={onBlur}
          fullWidth={true}
          autoComplete={autoComplete}
          variant={variant}
          margin='dense'
          classes={{
            root: classes.textField
          }}
        />
      </FormFieldContainer>
    )
  }
}

export default withStyles(styles)(TextFormField)
