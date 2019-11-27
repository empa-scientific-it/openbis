import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Typography from '@material-ui/core/Typography'
import CheckboxField from '../../common/form/CheckboxField.jsx'
import TextField from '../../common/form/TextField.jsx'
import logger from '../../../common/logger.js'

const styles = theme => ({
  container: {
    padding: theme.spacing(2)
  },
  header: {
    paddingBottom: theme.spacing(2)
  },
  field: {
    paddingBottom: theme.spacing(2)
  }
})

class ObjectTypeParametersType extends React.PureComponent {
  constructor(props) {
    super(props)
    this.handleChange = this.handleChange.bind(this)
  }

  handleChange(event) {
    let params = {
      field: event.target.name,
      value: event.target.value
    }
    this.props.onChange('type', params)
  }

  render() {
    logger.log(logger.DEBUG, 'ObjectTypeParametersType.render')

    let { classes, type, selection } = this.props

    if (
      selection &&
      (selection.type === 'section' || selection.type === 'property')
    ) {
      return null
    }

    return (
      <div className={classes.container}>
        <Typography variant='h6' className={classes.header}>
          Type
        </Typography>
        <div className={classes.field}>
          <TextField
            label='Code'
            name='code'
            disabled={type.used}
            value={type.code || ''}
            onChange={this.handleChange}
          />
        </div>
        <div className={classes.field}>
          <TextField
            label='Description'
            name='description'
            value={type.description || ''}
            onChange={this.handleChange}
          />
        </div>
        <div className={classes.field}>
          <CheckboxField
            label='Generate codes automatically'
            name='autoGeneratedCode'
            value={type.autoGeneratedCode}
            onChange={this.handleChange}
          />
        </div>
        {type.autoGeneratedCode && (
          <div className={classes.field}>
            <TextField
              label='Generated code prefix'
              name='generatedCodePrefix'
              value={type.generatedCodePrefix || ''}
              onChange={this.handleChange}
            />
          </div>
        )}
        <div className={classes.field}>
          <CheckboxField
            label='Unique subcodes'
            name='subcodeUnique'
            value={type.subcodeUnique}
            onChange={this.handleChange}
          />
        </div>
      </div>
    )
  }
}

export default withStyles(styles)(ObjectTypeParametersType)
