import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import TextField from '@material-ui/core/TextField'
import FormFieldView from '@src/js/components/common/form/FormFieldView.jsx'
import {
  KeyboardDatePicker,
  KeyboardDateTimePicker
} from '@material-ui/pickers'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  container: {
    minWidth: '200px'
  },
  label: {
    fontSize: theme.typography.body2.fontSize
  },
  input: {
    fontSize: theme.typography.body2.fontSize
  }
})

class DateField extends React.PureComponent {
  static defaultProps = {
    mode: 'edit',
    dateTime: true
  }

  constructor(props) {
    super(props)
    this.handleChange = this.handleChange.bind(this)
    this.renderEditInput = this.renderEditInput.bind(this)
  }

  handleChange(date, string) {
    const { name, onChange } = this.props

    if (onChange) {
      onChange({
        target: {
          name: name,
          value: date && false === Number.isNaN(date.getTime()) ? date : null,
          valueString: string
        }
      })
    }
  }

  render() {
    logger.log(logger.DEBUG, 'DateField.render')

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
    const { dateTime, name, value, classes } = this.props

    if (dateTime) {
      return (
        <div className={classes.container}>
          <KeyboardDateTimePicker
            name={name}
            variant='inline'
            ampm={false}
            label={this.renderEditLabel()}
            value={value}
            onChange={this.handleChange}
            format={'yyyy-MM-dd HH:mm:ss'}
            TextFieldComponent={this.renderEditInput}
          />
        </div>
      )
    } else {
      return (
        <div className={classes.container}>
          <KeyboardDatePicker
            name={name}
            variant='inline'
            label={this.renderEditLabel()}
            value={value}
            onChange={this.handleChange}
            format={'yyyy-MM-dd'}
            TextFieldComponent={this.renderEditInput}
          />
        </div>
      )
    }
  }

  renderEditLabel() {
    const { label, classes } = this.props
    return <span className={classes.label}>{label}</span>
  }

  renderEditInput(params) {
    const { classes } = this.props

    return (
      <TextField
        {...params}
        InputProps={{
          ...params.InputProps,
          classes: {
            ...params.InputProps.classes,
            input: classes.input
          }
        }}
      />
    )
  }
}

export default withStyles(styles)(DateField)
