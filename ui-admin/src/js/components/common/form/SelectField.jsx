import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import TextField from '@material-ui/core/TextField'
import MenuItem from '@material-ui/core/MenuItem'
import FormFieldContainer from '@src/js/components/common/form/FormFieldContainer.jsx'
import FormFieldLabel from '@src/js/components/common/form/FormFieldLabel.jsx'
import FormFieldView from '@src/js/components/common/form/FormFieldView.jsx'
import compare from '@src/js/common/compare.js'
import util from '@src/js/common/util.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  textField: {
    margin: 0
  },
  select: {
    fontSize: theme.typography.body2.fontSize
  },
  selectDisabled: {
    pointerEvents: 'none'
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
    variant: 'filled',
    fullWidth: true,
    emptyOption: null
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
    const { label, value, options, emptyOption } = this.props

    if (!util.empty(value)) {
      const option = options.find(option => option.value === value)
      return <FormFieldView label={label} value={this.getOptionText(option)} />
    } else if (
      this.getOptionSelectable(emptyOption) &&
      this.getOptionText(emptyOption)
    ) {
      return (
        <FormFieldView label={label} value={this.getOptionText(emptyOption)} />
      )
    } else {
      return <FormFieldView label={label} />
    }
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
      emptyOption,
      metadata,
      styles,
      onChange,
      onClick,
      classes,
      variant,
      fullWidth
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
                onClick={onClick}
              />
            ) : null
          }
          name={name}
          value={util.empty(value) ? '' : value}
          error={!!error}
          disabled={disabled}
          onChange={onChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
          fullWidth={fullWidth}
          InputLabelProps={{
            shrink:
              !util.empty(value) ||
              (this.getOptionSelectable(emptyOption) &&
                !!this.getOptionText(emptyOption))
          }}
          SelectProps={{
            displayEmpty: this.getOptionSelectable(emptyOption),
            MenuProps: {
              getContentAnchorEl: null,
              anchorOrigin: {
                vertical: 'bottom',
                horizontal: 'left'
              }
            },
            classes: {
              root: classes.select,
              disabled: classes.selectDisabled
            }
          }}
          variant={variant}
          margin='dense'
          classes={{
            root: classes.textField
          }}
        >
          {this.getOptions().map(option => this.renderOption(option))}
        </TextField>
      </FormFieldContainer>
    )
  }

  renderOption(option) {
    const { classes } = this.props

    return (
      <MenuItem
        key={util.empty(option.value) ? '' : option.value}
        value={util.empty(option.value) ? '' : option.value}
        classes={{ root: classes.option }}
      >
        {this.getOptionText(option)}
      </MenuItem>
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

  getOptions() {
    const { options, emptyOption, sort = true } = this.props

    if (options) {
      let result = Array.from(options)

      if (sort) {
        result.sort((option1, option2) => {
          const text1 = this.getOptionText(option1)
          const text2 = this.getOptionText(option2)
          return compare(text1, text2)
        })
      }

      if (emptyOption) {
        result.unshift(emptyOption)
      }

      return result
    } else {
      return []
    }
  }

  getOptionText(option) {
    if (option) {
      return option.label || option.value || ''
    } else {
      return ''
    }
  }

  getOptionSelectable(option) {
    if (option) {
      return option.selectable === undefined || option.selectable
    } else {
      return false
    }
  }
}

export default withStyles(styles)(SelectFormField)
