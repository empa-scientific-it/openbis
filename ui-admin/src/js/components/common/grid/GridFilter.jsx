import _ from 'lodash'
import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import TextField from '@src/js/components/common/form/TextField.jsx'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

const styles = () => ({})

class GridFilter extends React.PureComponent {
  constructor(props) {
    super(props)
    this.handleFilterChange = this.handleFilterChange.bind(this)
    this.ref = React.createRef()
  }

  handleFilterChange(event) {
    const { column, onFilterChange } = this.props
    if (onFilterChange) {
      onFilterChange(column.name, event.target.value)
    }
  }

  componentDidMount() {
    this.renderDOMFilter()
  }

  componentDidUpdate() {
    this.renderDOMFilter()
  }

  render() {
    logger.log(logger.DEBUG, 'GridFilter.render')

    const { column } = this.props

    return (
      <div ref={this.ref}>
        {column.renderDOMFilter ? null : this.renderFilter()}
      </div>
    )
  }

  renderFilter() {
    const { column, filter } = this.props

    if (!column.filterable) {
      return null
    }

    const params = {
      value: filter,
      column,
      onChange: this.handleFilterChange
    }

    const renderedFilter = column.renderFilter ? (
      column.renderFilter(params)
    ) : (
      <TextField
        label={messages.get(messages.FILTER)}
        value={filter}
        onChange={this.handleFilterChange}
        variant='standard'
      />
    )

    if (renderedFilter === null || renderedFilter === undefined) {
      return ''
    } else if (_.isNumber(renderedFilter) || _.isBoolean(renderedFilter)) {
      return String(renderedFilter)
    } else {
      return renderedFilter
    }
  }

  renderDOMFilter() {
    const { column, filter } = this.props

    if (column.filterable && column.renderDOMFilter && this.ref.current) {
      column.renderDOMFilter({
        container: this.ref.current,
        value: filter,
        column,
        onChange: this.handleFilterChange
      })
    }
  }
}

export default withStyles(styles)(GridFilter)
