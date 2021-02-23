import _ from 'lodash'
import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import PageMode from '@src/js/components/common/page/PageMode.js'
import Header from '@src/js/components/common/form/Header.jsx'
import TextField from '@src/js/components/common/form/TextField.jsx'
import TypeFormSelectionType from '@src/js/components/types/form/TypeFormSelectionType.js'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  field: {
    marginBottom: theme.spacing(1)
  }
})

class TypeFormPreviewHeader extends React.PureComponent {
  constructor(props) {
    super(props)
    this.handleChange = this.handleChange.bind(this)
  }

  handleChange(event) {
    this.props.onChange(TypeFormSelectionType.PREVIEW, {
      field: event.target.name,
      value: event.target.value
    })
  }

  render() {
    logger.log(logger.DEBUG, 'TypeFormPreviewHeader.render')

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
    return <Header>{messages.get(messages.FORM_PREVIEW)}</Header>
  }

  renderCode() {
    const { mode, type, preview, classes } = this.props

    const value =
      type.autoGeneratedCode && type.autoGeneratedCode.value
        ? type.generatedCodePrefix.value
        : _.get(preview, 'code.value')

    const disabled =
      mode !== PageMode.EDIT ||
      (type.autoGeneratedCode && type.autoGeneratedCode.value)

    return (
      <div className={classes.field}>
        <TextField
          name='code'
          label={messages.get(messages.CODE)}
          value={value}
          disabled={disabled}
          mode={PageMode.EDIT}
          onChange={this.handleChange}
        />
      </div>
    )
  }

  renderParents() {
    const { mode, type, preview, classes } = this.props

    if (type.showParents && type.showParents.value) {
      return (
        <div className={classes.field}>
          <TextField
            name='parents'
            label={messages.get(messages.PARENTS)}
            value={_.get(preview, 'parents.value')}
            disabled={mode !== PageMode.EDIT}
            mode={PageMode.EDIT}
            onChange={this.handleChange}
          />
        </div>
      )
    } else {
      return null
    }
  }

  renderContainer() {
    const { mode, type, preview, classes } = this.props

    if (type.showContainer && type.showContainer.value) {
      return (
        <div className={classes.field}>
          <TextField
            name='container'
            label={messages.get(messages.CONTAINER)}
            value={_.get(preview, 'container.value')}
            disabled={mode !== PageMode.EDIT}
            mode={PageMode.EDIT}
            onChange={this.handleChange}
          />
        </div>
      )
    } else {
      return null
    }
  }
}

export default _.flow(withStyles(styles))(TypeFormPreviewHeader)
