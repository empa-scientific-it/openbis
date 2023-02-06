import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Autocomplete from '@material-ui/lab/Autocomplete'
import TextField from '@material-ui/core/TextField'
import InputAdornment from '@material-ui/core/InputAdornment'
import ArrowDropUpIcon from '@material-ui/icons/ArrowDropUp'
import ArrowDropDownIcon from '@material-ui/icons/ArrowDropDown'
import CircularProgress from '@material-ui/core/CircularProgress'
import FormFieldContainer from '@src/js/components/common/form/FormFieldContainer.jsx'
import FormFieldLabel from '@src/js/components/common/form/FormFieldLabel.jsx'
import FormFieldView from '@src/js/components/common/form/FormFieldView.jsx'
import compare from '@src/js/common/compare.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  paper: {
    margin: 0
  },
  textField: {
    margin: 0
  },
  input: {
    fontSize: theme.typography.body2.fontSize
  },
  option: {
    fontSize: theme.typography.body2.fontSize
  },
  disabled: {
    '& $adornment': {
      color: '#00000042'
    }
  },
  adornment: {
    marginRight: '-4px',
    marginTop: '-16px',
    color: '#0000008a'
  }
})

class AutocompleterFormField extends React.PureComponent {
  static defaultProps = {
    freeSolo: false,
    mode: 'edit',
    variant: 'filled'
  }

  constructor(props) {
    super(props)

    this.state = {
      open: false,
      focused: false,
      inputValue: ''
    }

    this.reference = React.createRef()
    this.handleClick = this.handleClick.bind(this)
    this.handleKeyDown = this.handleKeyDown.bind(this)
    this.handleChange = this.handleChange.bind(this)
    this.handleInputChange = this.handleInputChange.bind(this)
    this.handleFocus = this.handleFocus.bind(this)
    this.handleBlur = this.handleBlur.bind(this)
  }

  handleClick(event) {
    const { onClick, disabled } = this.props

    if (!disabled) {
      this.setState(state => ({
        open: !state.open
      }))
    }

    if (onClick) {
      onClick(event)
    }
  }

  handleKeyDown(event) {
    const { open } = this.state

    switch (event.key) {
      case 'Enter':
      case 'Esc':
      case 'Escape':
      case 'Tab':
        if (open) {
          this.setState({ open: false })
        }
        return
      default:
        if (!open) {
          this.setState({ open: true })
        }
    }
  }

  handleChange(event, value) {
    this.handleEvent(event, value, this.props.onChange)
  }

  handleInputChange(event, value) {
    this.setState({
      inputValue: value
    })
    this.handleEvent(event, value, this.props.onInputChange)
  }

  handleFocus(event) {
    this.setState({
      focused: true
    })
    this.handleEvent(event, event.target.value, this.props.onFocus)
  }

  handleBlur(event) {
    this.setState({
      focused: false,
      open: false
    })

    event.persist()

    const { freeSolo, getOptionLabel } = this.props
    const { inputValue } = this.state

    const value = getOptionLabel
      ? getOptionLabel(this.props.value)
      : this.props.value

    const valueTrimmed = value ? value.trim() : ''
    const inputValueTrimmed = inputValue ? inputValue.trim() : ''

    if (inputValueTrimmed.length === 0 && valueTrimmed.length !== 0) {
      this.handleChange(event, null)
    } else if (inputValueTrimmed !== valueTrimmed) {
      if (freeSolo) {
        this.handleChange(event, inputValue)
      } else {
        this.handleInputChange(event, value)
      }
    }

    setTimeout(() => {
      this.handleEvent(event, this.state.value, this.props.onBlur)
    }, 0)
  }

  handleEvent(event, value, handler) {
    if (handler) {
      const input = this.getReference().current
      const newEvent = {
        ...event,
        target: {
          ...input,
          name: this.props.name,
          value: value
        }
      }
      handler(newEvent)
    }
  }

  render() {
    logger.log(logger.DEBUG, 'AutocompleterFormField.render')

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
    const { label, value } = this.props
    return <FormFieldView label={label} value={value} />
  }

  renderEdit() {
    const {
      name,
      description,
      value,
      disabled,
      error,
      metadata,
      styles,
      classes,
      variant,
      renderOption,
      filterOptions,
      getOptionLabel,
      getOptionSelected,
      getOptionDisabled
    } = this.props

    const { open, inputValue, focused } = this.state

    return (
      <FormFieldContainer
        description={description}
        error={error}
        metadata={metadata}
        styles={styles}
        onClick={this.handleClick}
      >
        <Autocomplete
          disableClearable
          freeSolo={true}
          name={name}
          disabled={disabled}
          options={this.getOptions()}
          renderOption={renderOption}
          filterOptions={filterOptions}
          getOptionLabel={getOptionLabel}
          getOptionSelected={getOptionSelected}
          getOptionDisabled={getOptionDisabled}
          value={value}
          inputValue={inputValue}
          open={open}
          onChange={this.handleChange}
          onInputChange={this.handleInputChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
          onKeyDown={this.handleKeyDown}
          classes={{
            paper: classes.paper,
            option: classes.option
          }}
          renderInput={params => (
            <TextField
              {...params}
              inputRef={this.getReference()}
              InputProps={{
                ...params.InputProps,
                endAdornment: this.renderAdornment(),
                classes: {
                  ...params.InputProps.classes,
                  input: classes.input,
                  disabled: classes.disabled
                }
              }}
              InputLabelProps={{
                shrink: !!value || focused
              }}
              label={this.renderLabel()}
              error={!!error}
              fullWidth={true}
              autoComplete='off'
              variant={variant}
              margin='dense'
              classes={{
                root: classes.textField
              }}
            />
          )}
        />
      </FormFieldContainer>
    )
  }

  renderLabel() {
    const { label, mandatory, styles, onClick } = this.props
    return (
      <FormFieldLabel
        label={label}
        mandatory={mandatory}
        styles={styles}
        onClick={onClick}
      />
    )
  }

  renderAdornment() {
    const { open } = this.state
    const { loading, classes } = this.props

    return (
      <InputAdornment
        position='end'
        classes={{
          root: classes.adornment
        }}
      >
        {loading ? <CircularProgress color='inherit' size={20} /> : null}
        {open ? <ArrowDropUpIcon /> : <ArrowDropDownIcon />}
      </InputAdornment>
    )
  }

  getOptions() {
    const { options, getOptionLabel, sort = true } = this.props

    if (options) {
      let result = Array.from(options)

      if (sort) {
        result.sort((option1, option2) => {
          let label1 = getOptionLabel ? getOptionLabel(option1) : option1
          let label2 = getOptionLabel ? getOptionLabel(option2) : option2
          return compare(label1, label2)
        })
      }

      return result
    } else {
      return []
    }
  }

  getReference() {
    return this.props.reference ? this.props.reference : this.reference
  }
}

export default withStyles(styles)(AutocompleterFormField)
