import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import TextField from '@material-ui/core/TextField'
import FormFieldView from '@src/js/components/common/form/FormFieldView.jsx'
import FormFieldLabel from '@src/js/components/common/form/FormFieldLabel.jsx'
import FormFieldContainer from '@src/js/components/common/form/FormFieldContainer.jsx'
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
    this.inputContainerReference = React.createRef()
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
          value: {
            dateObject:
              date && false === Number.isNaN(date.getTime()) ? date : null,
            dateString: string
          }
        }
      })
    }
  }

  handleBlur(event) {
    const { name, dateTime, onChange, onBlur } = this.props

    if (onChange) {
      let string = null
      let date = null

      if (event.target.value !== null && event.target.value !== undefined) {
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
          const match = event.target.value
            .trim()
            .match(/^(.{4})-(.{2})-(.{2})$/)

          if (match) {
            const year = match[1]
            const month = match[2] === '__' ? '01' : match[2]
            const day = match[3] === '__' ? '01' : match[3]

            string = `${year}-${month}-${day}`
            date = new DateFnsUtils().parse(string, 'yyyy-MM-dd')
          }
        }
      }

      onChange({
        target: {
          name,
          value: {
            dateObject:
              date && false === Number.isNaN(date.getTime()) ? date : null,
            dateString: string
          }
        }
      })
    }

    if (onBlur) {
      setTimeout(() => {
        onBlur()
      }, 1)
    }
  }

  componentDidMount() {
    this.fixReference()
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
      <FormFieldView
        label={label}
        value={value && value.dateObject ? date.format(value.dateObject) : null}
      />
    )
  }

  renderEdit() {
    const { dateTime, name, value, error, variant, disabled, classes } =
      this.props

    const dateObject = value && value.dateObject ? value.dateObject : null
    const dateString = value && value.dateString ? value.dateString : null

    if (dateTime) {
      return (
        <FormFieldContainer error={error}>
          <div className={classes.container}>
            <KeyboardDateTimePicker
              name={name}
              ampm={false}
              label={this.renderEditLabel()}
              invalidDateMessage={null}
              value={dateObject}
              inputValue={dateString}
              onChange={this.handleChange}
              onBlur={this.handleBlur}
              onClose={this.props.onBlur}
              format={'yyyy-MM-dd HH:mm:ss'}
              fullWidth={true}
              variant='inline'
              inputVariant={variant}
              disabled={disabled}
              error={!!error}
              TextFieldComponent={this.renderEditInput}
            />
          </div>
        </FormFieldContainer>
      )
    } else {
      return (
        <FormFieldContainer error={error}>
          <div className={classes.container}>
            <KeyboardDatePicker
              name={name}
              label={this.renderEditLabel()}
              invalidDateMessage={null}
              value={dateObject}
              inputValue={dateString}
              onChange={this.handleChange}
              onBlur={this.handleBlur}
              onClose={this.props.onBlur}
              format={'yyyy-MM-dd'}
              fullWidth={true}
              variant='inline'
              inputVariant={variant}
              disabled={disabled}
              error={!!error}
              TextFieldComponent={this.renderEditInput}
            />
          </div>
        </FormFieldContainer>
      )
    }
  }

  renderEditLabel() {
    const { label, mandatory } = this.props
    return <FormFieldLabel label={label} mandatory={mandatory} />
  }

  renderEditInput(params) {
    const { classes } = this.props

    return (
      <div ref={this.inputContainerReference}>
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
      </div>
    )
  }

  fixReference() {
    // hack to set the input reference (setting it normally via inputRef of TextField tag breaks the date popup location)
    const { reference } = this.props
    if (reference) {
      reference.current = {
        focus: () => {
          if (
            this.inputContainerReference &&
            this.inputContainerReference.current
          ) {
            const inputs =
              this.inputContainerReference.current.getElementsByTagName('input')
            if (inputs && inputs.length === 1) {
              inputs[0].focus()
            }
          }
        }
      }
    }
  }
}

export default withStyles(styles)(DateField)
