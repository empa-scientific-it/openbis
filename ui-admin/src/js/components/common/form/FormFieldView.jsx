import _ from 'lodash'
import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Typography from '@material-ui/core/Typography'
import InfoIcon from '@material-ui/icons/Info'
import Tooltip from '@src/js/components/common/form/Tooltip.jsx'

const styles = theme => ({
  container: {
    display: 'flex',
    flexDirection: 'row',
    alignItems: 'center'
  },
  control: {
    flex: '1 1 auto'
  },
  label: {
    fontSize: theme.typography.label.fontSize,
    color: theme.typography.label.color
  },
  value: {
    paddingBottom: theme.spacing(1) / 2,
    borderBottomWidth: '1px',
    borderBottomStyle: 'solid',
    borderBottomColor: theme.palette.border.secondary
  },
  description: {
    flex: '0 0 auto',
    '& svg': {
      color: theme.palette.hint.main
    },
    cursor: 'pointer'
  }
})

class FormFieldView extends React.PureComponent {
  render() {
    const { label, value, description, classes } = this.props
    return (
      <div className={classes.container}>
        <div className={classes.control}>
          <Typography variant='body2' component='div' className={classes.label}>
            {label}
          </Typography>
          <Typography variant='body2' component='div' className={classes.value}>
            {value ? value : <span>&nbsp;</span>}
          </Typography>
        </div>
        {!_.isNil(description) && (
          <div className={classes.description}>
            <Tooltip title={description}>
              <InfoIcon fontSize='small' />
            </Tooltip>
          </div>
        )}
      </div>
    )
  }
}

export default withStyles(styles)(FormFieldView)
