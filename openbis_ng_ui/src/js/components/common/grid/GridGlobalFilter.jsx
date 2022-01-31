import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import GridFilterOptions from '@src/js/components/common/grid/GridFilterOptions.js'
import TextField from '@src/js/components/common/form/TextField.jsx'
import SelectField from '@src/js/components/common/form/SelectField.jsx'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  container: {
    display: 'flex',
    alignItems: 'center'
  },
  operator: {
    flex: '0 0 auto',
    marginRight: theme.spacing(1)
  },
  text: {
    width: '100%'
  }
})

class GridGlobalFilter extends React.PureComponent {
  constructor(props) {
    super(props)
    this.handleGlobalFilterChange = this.handleGlobalFilterChange.bind(this)
  }

  handleGlobalFilterChange(event) {
    const { globalFilter, onGlobalFilterChange } = this.props
    if (onGlobalFilterChange) {
      const newGlobalFilter = { ...globalFilter }
      newGlobalFilter[event.target.name] = event.target.value
      onGlobalFilterChange(newGlobalFilter)
    }
  }

  render() {
    logger.log(logger.DEBUG, 'GridGlobalFilter.render')

    const { globalFilter, classes } = this.props

    return (
      <div className={classes.container}>
        <div className={classes.operator}>
          <SelectField
            name='operator'
            label={messages.get(messages.OPERATOR)}
            options={[
              {
                label: messages.get(messages.OPERATOR_AND),
                value: GridFilterOptions.OPERATOR_AND
              },
              {
                label: messages.get(messages.OPERATOR_OR),
                value: GridFilterOptions.OPERATOR_OR
              }
            ]}
            value={globalFilter.operator}
            onChange={this.handleGlobalFilterChange}
            variant='standard'
          />
        </div>
        <div className={classes.text}>
          <TextField
            name='text'
            label={messages.get(messages.FILTER)}
            value={globalFilter.text}
            onChange={this.handleGlobalFilterChange}
            variant='standard'
          />
        </div>
      </div>
    )
  }
}

export default withStyles(styles)(GridGlobalFilter)
