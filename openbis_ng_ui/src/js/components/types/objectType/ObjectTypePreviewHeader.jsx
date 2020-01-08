import _ from 'lodash'
import React from 'react'
import TextField from '../../common/form/TextField.jsx'
import { withStyles } from '@material-ui/core/styles'
import ObjectTypeHeader from './ObjectTypeHeader.jsx'
import logger from '../../../common/logger.js'

const styles = theme => ({
  field: {
    marginBottom: theme.spacing(2)
  }
})

class ObjectTypePreviewHeader extends React.PureComponent {
  constructor(props) {
    super(props)
    this.state = {
      values: {}
    }
    this.handleChange = this.handleChange.bind(this)
  }

  handleChange(event) {
    const name = event.target.name
    const value = event.target.value

    this.setState(state => ({
      values: {
        ...state.values,
        [name]: value
      }
    }))
  }

  render() {
    logger.log(logger.DEBUG, 'ObjectTypePreviewHeader.render')

    return (
      <div>
        {this.renderTitle()}
        {this.renderCode()}
        {this.renderParents()}
        {this.renderContainer()}
      </div>
    )
  }

  renderTitle() {
    const { classes } = this.props
    return (
      <div className={classes.field}>
        <ObjectTypeHeader>Form Preview</ObjectTypeHeader>
      </div>
    )
  }

  renderCode() {
    const { type, classes } = this.props

    const value = type.autoGeneratedCode
      ? type.generatedCodePrefix
      : this.state.values.code

    const disabled = type.autoGeneratedCode

    return (
      <div className={classes.field}>
        <TextField
          name='code'
          label='Code'
          value={value}
          disabled={disabled}
          onChange={this.handleChange}
        />
      </div>
    )
  }

  renderParents() {
    const { type, classes } = this.props

    if (type.showParents) {
      return (
        <div className={classes.field}>
          <TextField
            name='parents'
            label='Parents'
            value={this.state.values.parents}
            onChange={this.handleChange}
          />
        </div>
      )
    } else {
      return null
    }
  }

  renderContainer() {
    const { type, classes } = this.props

    if (type.showContainer) {
      return (
        <div className={classes.field}>
          <TextField
            name='container'
            label='Container'
            value={this.state.values.container}
            onChange={this.handleChange}
          />
        </div>
      )
    } else {
      return null
    }
  }
}

export default _.flow(withStyles(styles))(ObjectTypePreviewHeader)
