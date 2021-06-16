import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import TextField from '@material-ui/core/TextField'
import FormFieldView from '@src/js/components/common/form/FormFieldView.jsx'
import { KeyboardDateTimePicker } from '@material-ui/pickers'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  container: {
    minWidth: '200px'
  },
  input: {
    fontSize: theme.typography.body2.fontSize
  }
})

class DateField extends React.PureComponent {
  static defaultProps = {
    mode: 'edit'
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
    const { name, label, value, classes, onChange } = this.props

    return (
      <div className={classes.container}>
        <KeyboardDateTimePicker
          name={name}
          variant='inline'
          ampm={false}
          label={label}
          value={value}
          onChange={onChange}
          format='yyyy-MM-dd HH:mm:ss'
          TextFieldComponent={params => (
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
          )}
        />
      </div>
    )
  }
}

export default withStyles(styles)(DateField)
