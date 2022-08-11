import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import TextField from '@material-ui/core/TextField'
import FormFieldView from '@src/js/components/common/form/FormFieldView.jsx'
import {
  KeyboardDatePicker,
  KeyboardDateTimePicker
} from '@material-ui/pickers'
import DateFnsUtils from '@date-io/date-fns'
import date from '@src/js/common/date.js'
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
    variant: 'filled',
    dateTime: true
  }

  constructor(props) {
    super(props)
    this.handleChange = this.handleChange.bind(this)
    this.handleBlur = this.handleBlur.bind(this)
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

  handleBlur(event) {
    const { name, dateTime, onChange } = this.props

    if (event.target.value === null || event.target.value === undefined) {
      return
    }

    if (onChange) {
      let string = null
      let date = null

      if (dateTime) {
        const match = event.target.value
          .trim()
          .match(/^(.{4})-(.{2})-(.{2}) (.{2}):(.{2}):(.{2})$/)

        if (match) {
          const year = match[1]
          const month = match[2] === '__' ? '01' : match[2]
          const day = match[3] === '__' ? '01' : match[3]
          const hour = match[4] === '__' ? '00' : match[4]
          const minute = match[5] === '__' ? '00' : match[5]
          const second = match[6] === '__' ? '00' : match[6]

          string = `${year}-${month}-${day} ${hour}:${minute}:${second}`
          date = new DateFnsUtils().parse(string, 'yyyy-MM-dd HH:mm:ss')
        }
      } else {
        const match = event.target.value.trim().match(/^(.{4})-(.{2})-(.{2})$/)

        if (match) {
          const year = match[1]
          const month = match[2] === '__' ? '01' : match[2]
          const day = match[3] === '__' ? '01' : match[3]

          string = `${year}-${month}-${day}`
          date = new DateFnsUtils().parse(string, 'yyyy-MM-dd')
        }
      }

      if (date !== null && false === Number.isNaN(date.getTime())) {
        onChange({
          target: {
            name,
            value: date,
            valueString: string
          }
        })
      }
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
    return (
      <FormFieldView label={label} value={value ? date.format(value) : null} />
    )
  }

  renderEdit() {
    const { dateTime, name, value, variant, disabled, classes } = this.props

    if (dateTime) {
      return (
        <div className={classes.container}>
          <KeyboardDateTimePicker
            name={name}
            ampm={false}
            label={this.renderEditLabel()}
            value={value}
            onChange={this.handleChange}
            onBlur={this.handleBlur}
            format={'yyyy-MM-dd HH:mm:ss'}
            fullWidth={true}
            inputVariant={variant}
            disabled={disabled}
            TextFieldComponent={this.renderEditInput}
          />
        </div>
      )
    } else {
      return (
        <div className={classes.container}>
          <KeyboardDatePicker
            name={name}
            label={this.renderEditLabel()}
            value={value}
            onChange={this.handleChange}
            onBlur={this.handleBlur}
            format={'yyyy-MM-dd'}
            fullWidth={true}
            inputVariant={variant}
            disabled={disabled}
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
