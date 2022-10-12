import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import FormFieldView from '@src/js/components/common/form/FormFieldView.jsx'
import DateField from '@src/js/components/common/form/DateField.jsx'
import logger from '@src/js/common/logger.js'

const styles = () => ({})

class DateRangeField extends React.PureComponent {
  static defaultProps = {
    mode: 'edit'
  }

  constructor(props) {
    super(props)
    this.handleFromChange = this.handleFromChange.bind(this)
    this.handleToChange = this.handleToChange.bind(this)
  }

  handleFromChange(event) {
    this.handleChange(event, 'from')
  }

  handleToChange(event) {
    this.handleChange(event, 'to')
  }

  handleChange(event, field) {
    const { name, value, onChange } = this.props

    if (onChange) {
      const newValue = value ? { ...value } : {}
      newValue[field] = event.target.value

      onChange({
        ...event,
        target: {
          ...event.target,
          name: name,
          value: newValue
        }
      })
    }
  }

  render() {
    logger.log(logger.DEBUG, 'DateRangeField.render')

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
    const { dateTime, value, variant, classes } = this.props

    return (
      <div className={classes.container}>
        <DateField
          label='From'
          dateTime={dateTime}
          value={value ? value.from : null}
          variant={variant}
          onChange={this.handleFromChange}
        />
        <DateField
          label='To'
          dateTime={dateTime}
          value={value ? value.to : null}
          variant={variant}
          onChange={this.handleToChange}
        />
      </div>
    )
  }
}

export default withStyles(styles)(DateRangeField)
