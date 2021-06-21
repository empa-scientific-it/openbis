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
      newValue[field] = {
        value: event.target.value,
        valueString: event.target.valueString
      }

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
    const { value, classes } = this.props

    return (
      <div className={classes.container}>
        <DateField
          label='From'
          value={value && value.from ? value.from.valueString : null}
          onChange={this.handleFromChange}
        />
        <DateField
          label='To'
          value={value && value.to ? value.to.valueString : null}
          onChange={this.handleToChange}
        />
      </div>
    )
  }
}

export default withStyles(styles)(DateRangeField)
