import _ from 'lodash'
import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import autoBind from 'auto-bind'
import Dialog from '@src/js/components/common/dialog/Dialog.jsx'
import Message from '@src/js/components/common/form/Message.jsx'
import Button from '@src/js/components/common/form/Button.jsx'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  button: {
    marginLeft: theme.spacing(1)
  }
})

class GridExportWarnings extends React.PureComponent {
  constructor(props) {
    super(props)
    autoBind(this)
  }

  handleDownload() {
    this.props.onDownload()
  }

  handleCancel() {
    this.props.onCancel()
  }

  render() {
    logger.log(logger.DEBUG, 'GridExportWarnings.render')

    const { open, classes } = this.props

    return (
      <Dialog
        open={open}
        title={messages.get(messages.WARNING)}
        content={this.renderWarnings()}
        actions={
          <div>
            <Button
              name='download'
              label={messages.get(messages.DOWNLOAD)}
              styles={{ root: classes.button }}
              onClick={this.handleDownload}
            />
            <Button
              name='cancel'
              label={messages.get(messages.CANCEL)}
              styles={{ root: classes.button }}
              onClick={this.handleCancel}
            />
          </div>
        }
      />
    )
  }

  renderWarnings() {
    const { warnings } = this.props

    if (_.isEmpty(warnings)) {
      return null
    }

    return warnings.map((warning, index) => (
      <Message key={index} type='warning'>
        {warning}
      </Message>
    ))
  }
}

export default withStyles(styles)(GridExportWarnings)
