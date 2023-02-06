import React from 'react'
import { MuiPickersUtilsProvider } from '@material-ui/pickers'
import DateFnsUtils from '@date-io/date-fns'

class DatePickerProvider extends React.Component {
  render() {
    return (
      <MuiPickersUtilsProvider utils={DateFnsUtils}>
        {this.props.children}
      </MuiPickersUtilsProvider>
    )
  }
}

export default DatePickerProvider
