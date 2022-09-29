import _ from 'lodash'
import React from 'react'
import autoBind from 'auto-bind'
import { withStyles } from '@material-ui/core/styles'
import IconButton from '@material-ui/core/IconButton'
import Sort from '@material-ui/icons/Sort'
import Popover from '@material-ui/core/Popover'
import Container from '@src/js/components/common/form/Container.jsx'
import RadioGroupField from '@src/js/components/common/form/RadioGroupField.jsx'
import logger from '@src/js/common/logger.js'

const styles = () => ({})

class BrowserSortings extends React.PureComponent {
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

  handleChange(event) {
    const { node, onChange } = this.props
    if (onChange) {
      onChange(node.id, event.target.value)
    }
  }

  render() {
    logger.log(logger.DEBUG, 'BrowserSortings.render')

    const { node } = this.props

    if (
      _.isEmpty(node) ||
      _.isEmpty(node.sortings) ||
      _.isEmpty(node.sortingId)
    ) {
      return null
    }

    const { el } = this.state

    return (
      <div>
        <IconButton onClick={this.handleOpen}>
          <Sort fontSize='small' />
        </IconButton>
        <Popover
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
          <Container square={true}>{this.renderSortings()}</Container>
        </Popover>
      </div>
    )
  }

  renderSortings() {
    const { node } = this.props

    const options = Object.entries(node.sortings)
      .map(([id, sorting]) => ({
        value: id,
        label: sorting.label,
        index: sorting.index
      }))
      .sort(option => option.index)

    return (
      <RadioGroupField
        name='sorting'
        value={node.sortingId}
        options={options}
        onChange={this.handleChange}
      />
    )
  }
}

export default withStyles(styles)(BrowserSortings)
