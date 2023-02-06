import _ from 'lodash'
import React from 'react'
import autoBind from 'auto-bind'
import { withStyles } from '@material-ui/core/styles'
import Popover from '@material-ui/core/Popover'
import Mask from '@src/js/components/common/loading/Mask.jsx'
import Container from '@src/js/components/common/form/Container.jsx'
import RadioGroupField from '@src/js/components/common/form/RadioGroupField.jsx'
import Button from '@src/js/components/common/form/Button.jsx'
import GridFilterOptions from '@src/js/components/common/grid/GridFilterOptions.js'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  container: {
    display: 'flex',
    alignItems: 'center',
    paddingRight: theme.spacing(1)
  }
})

class GridFiltersConfig extends React.PureComponent {
  constructor(props) {
    super(props)
    autoBind(this)
    this.state = {
      el: null
    }
  }

  handleOpen(event) {
    this.setState({
      el: event.currentTarget
    })
  }

  handleClose() {
    this.setState({
      el: null
    })
  }

  handleFilterModeChange(event) {
    const { onFilterModeChange } = this.props
    if (onFilterModeChange) {
      onFilterModeChange(event.target.value)
    }
  }

  render() {
    logger.log(logger.DEBUG, 'GridFiltersConfig.render')

    const { id, filterModes, loading, classes } = this.props
    const { el } = this.state

    if (filterModes && filterModes.length <= 1) {
      return null
    }

    return (
      <div className={classes.container}>
        <Button
          id={id + '.filters-button-id'}
          label={messages.get(messages.FILTERS)}
          color='default'
          variant='outlined'
          onClick={this.handleOpen}
        />
        <Popover
          id={id + '.filters-popup-id'}
          open={Boolean(el)}
          anchorEl={el}
          onClose={this.handleClose}
          anchorOrigin={{
            vertical: 'bottom',
            horizontal: 'left'
          }}
          transformOrigin={{
            vertical: 'top',
            horizontal: 'left'
          }}
        >
          <Mask visible={loading}>
            <Container square={true}>{this.renderFilters()}</Container>
          </Mask>
        </Popover>
      </div>
    )
  }

  renderFilters() {
    const { filterModes, filterMode } = this.props

    const allOptions = [
      {
        value: GridFilterOptions.COLUMN_FILTERS,
        label: messages.get(messages.COLUMN_FILTERS)
      },
      {
        value: GridFilterOptions.GLOBAL_FILTER,
        label: messages.get(messages.GLOBAL_FILTER)
      }
    ]

    const chosenOptions = allOptions.filter(
      option =>
        filterModes === null ||
        filterModes === undefined ||
        filterModes.includes(option.value)
    )

    return (
      <RadioGroupField
        name='filterMode'
        value={filterMode}
        options={chosenOptions}
        onChange={this.handleFilterModeChange}
      />
    )
  }
}

export default _.flow(withStyles(styles))(GridFiltersConfig)
