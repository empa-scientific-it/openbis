import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import autoBind from 'auto-bind'
import Container from '@src/js/components/common/form/Container.jsx'
import Button from '@src/js/components/common/form/Button.jsx'
import Message from '@src/js/components/common/form/Message.jsx'
import UnsavedChangesDialog from '@src/js/components/common/dialog/UnsavedChangesDialog.jsx'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  container: {
    display: 'flex'
  },
  leftContainer: {
    flexGrow: 1,
    display: 'flex',
    justifyContent: 'flex-start',
    '& $button': {
      marginRight: theme.spacing(1)
    },
    alignItems: 'center'
  },
  rightContainer: {
    flexGrow: 1,
    display: 'flex',
    justifyContent: 'flex-end',
    '& $button': {
      marginLeft: theme.spacing(1)
    },
    alignItems: 'center'
  },
  button: {
    whiteSpace: 'nowrap'
  }
})

class PageButtons extends React.PureComponent {
  constructor(props) {
    super(props)
    this.state = {
      unsavedChangesDialogOpen: false
    }
    autoBind(this)
  }

  handleCancel() {
    const { changed, onCancel } = this.props

    if (changed) {
      this.setState({
        unsavedChangesDialogOpen: true
      })
    } else {
      onCancel()
    }
  }

  handleCancelConfirm() {
    const { onCancel } = this.props

    this.setState({
      unsavedChangesDialogOpen: false
    })
    onCancel()
  }

  handleCancelCancel() {
    this.setState({
      unsavedChangesDialogOpen: false
    })
  }

  render() {
    logger.log(logger.DEBUG, 'PageButtons.render')

    const { mode } = this.props

    if (mode === 'view') {
      return this.renderView()
    } else if (mode === 'edit') {
      return this.renderEdit()
    } else {
      throw 'Unsupported mode: ' + mode
    }
  }

  renderView() {
    const { classes, onEdit } = this.props

    return (
      <Container className={classes.container}>
        <div className={classes.rightContainer}>
          {onEdit && (
            <Button
              name='edit'
              label='Edit'
              styles={{ root: classes.button }}
              onClick={onEdit}
            />
          )}
        </div>
      </Container>
    )
  }

  renderEdit() {
    const {
      classes,
      onSave,
      onCancel,
      changed,
      renderAdditionalButtons
    } = this.props

    const additionalButtons = renderAdditionalButtons
      ? renderAdditionalButtons(classes)
      : null

    return (
      <Container className={classes.container}>
        <div className={classes.leftContainer}>{additionalButtons}</div>
        <div className={classes.rightContainer}>
          {changed && (
            <React.Fragment>
              <Message type='warning'>You have unsaved changes.</Message>
              <UnsavedChangesDialog
                open={this.state.unsavedChangesDialogOpen}
                onConfirm={this.handleCancelConfirm}
                onCancel={this.handleCancelCancel}
              />
            </React.Fragment>
          )}
          {onSave && (
            <Button
              name='save'
              label='Save'
              type='final'
              styles={{ root: classes.button }}
              onClick={onSave}
            />
          )}
          {onCancel && (
            <Button
              name='cancel'
              label='Cancel'
              styles={{ root: classes.button }}
              onClick={this.handleCancel}
            />
          )}
        </div>
      </Container>
    )
  }
}

export default withStyles(styles)(PageButtons)
