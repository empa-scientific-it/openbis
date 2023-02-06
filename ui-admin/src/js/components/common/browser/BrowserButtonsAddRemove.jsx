import React from 'react'
import autoBind from 'auto-bind'
import { withStyles } from '@material-ui/core/styles'
import Container from '@src/js/components/common/form/Container.jsx'
import ConfirmationDialog from '@src/js/components/common/dialog/ConfirmationDialog.jsx'
import Button from '@src/js/components/common/form/Button.jsx'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  container: {
    display: 'flex',
    borderWidth: '1px 0px 0px 0px',
    borderColor: theme.palette.border.primary,
    borderStyle: 'solid'
  },
  button: {
    marginRight: theme.spacing(1),
    whiteSpace: 'nowrap'
  }
})

class BrowserButtonsAddRemove extends React.Component {
  constructor(props) {
    super(props)
    autoBind(this)
    this.state = {
      removeDialogOpen: false
    }
  }

  handleAdd() {
    this.props.onAdd()
  }

  handleRemove() {
    this.setState({
      removeDialogOpen: true
    })
  }

  handleConfirmRemove() {
    this.setState({
      removeDialogOpen: false
    })
    this.props.onRemove()
  }

  handleCancelRemove() {
    this.setState({
      removeDialogOpen: false
    })
  }

  render() {
    logger.log(logger.DEBUG, 'BrowserButtonsAddRemove.render')

    const { addEnabled, removeEnabled, classes } = this.props
    const { removeDialogOpen } = this.state

    return (
      <Container className={classes.container}>
        <Button
          label={messages.get(messages.ADD)}
          name='add'
          styles={{ root: classes.button }}
          onClick={this.handleAdd}
          disabled={!addEnabled}
        />
        <Button
          label={messages.get(messages.REMOVE)}
          name='remove'
          styles={{ root: classes.button }}
          onClick={this.handleRemove}
          disabled={!removeEnabled}
        />
        <ConfirmationDialog
          open={removeDialogOpen}
          onConfirm={this.handleConfirmRemove}
          onCancel={this.handleCancelRemove}
          title={this.getDialogTitle()}
          content={this.getDialogContent()}
        />
      </Container>
    )
  }

  getDialogTitle() {
    const { selectedObject } = this.props

    if (selectedObject) {
      return messages.get(messages.CONFIRMATION_REMOVE, selectedObject.id)
    } else {
      return null
    }
  }

  getDialogContent() {
    const { selectedObject } = this.props

    if (selectedObject) {
      return messages.get(messages.CONFIRMATION_REMOVE, selectedObject.id)
    } else {
      return null
    }
  }
}

export default withStyles(styles)(BrowserButtonsAddRemove)
