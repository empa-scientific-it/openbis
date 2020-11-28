import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Typography from '@material-ui/core/Typography'

const styles = theme => ({
  label: {
    fontSize: theme.typography.label.fontSize,
    color: theme.typography.label.color
  },
  value: {
    paddingBottom: theme.spacing(1) / 2,
    borderBottomWidth: '1px',
    borderBottomStyle: 'solid',
    borderBottomColor: theme.palette.border.secondary
  }
})

class FormFieldView extends React.PureComponent {
  render() {
    const { label, value, classes } = this.props
    return (
      <div>
        <Typography variant='body2' component='div' className={classes.label}>
          {label}
        </Typography>
        <Typography variant='body2' component='div' className={classes.value}>
          {value ? value : <span>&nbsp;</span>}
        </Typography>
      </div>
    )
  }
}

export default withStyles(styles)(FormFieldView)
