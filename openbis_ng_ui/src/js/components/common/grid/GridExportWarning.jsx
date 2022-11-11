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

class GridExportWarning extends React.PureComponent {
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
    logger.log(logger.DEBUG, 'GridExportWarning.render')

    const { open, message, classes } = this.props

    return (
      <Dialog
        open={open}
        title={messages.get(messages.WARNING)}
        content={<Message type='warning'>{message}</Message>}
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
}

export default withStyles(styles)(GridExportWarning)
