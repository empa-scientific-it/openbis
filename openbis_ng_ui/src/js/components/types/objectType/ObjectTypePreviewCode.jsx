import _ from 'lodash'
import React from 'react'
import TextField from '@material-ui/core/TextField'
import { withStyles } from '@material-ui/core/styles'
import logger from '../../../common/logger.js'

const styles = () => ({})

class ObjectTypePreviewCode extends React.PureComponent {
  constructor(props) {
    super(props)
  }

  render() {
    logger.log(logger.DEBUG, 'ObjectTypePreviewCode.render')

    return (
      <TextField
        label='Code'
        value={this.getValue()}
        disabled={this.getDisabled()}
        variant='filled'
      />
    )
  }

  getValue() {
    let { type } = this.props
    return type.autoGeneratedCode ? type.generatedCodePrefix : ''
  }

  getDisabled() {
    let { type } = this.props
    return type.autoGeneratedCode
  }
}

export default _.flow(withStyles(styles))(ObjectTypePreviewCode)
