import React from 'react'
import Button from '@material-ui/core/Button'
import { withStyles } from '@material-ui/core/styles'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  container: {
    padding: `${theme.spacing(1)}px ${theme.spacing(2)}px`,
    display: 'flex'
  },
  button: {
    marginRight: theme.spacing(1),
    whiteSpace: 'nowrap'
  }
})

class TypeFormButtons extends React.PureComponent {
  isSectionOrPropertySelected() {
    const { selection } = this.props
    return (
      selection &&
      (selection.type === 'property' || selection.type === 'section')
    )
  }

  render() {
    logger.log(logger.DEBUG, 'TypeFormButtons.render')

    const {
      classes,
      onAddSection,
      onAddProperty,
      onRemove,
      onSave
    } = this.props

    return (
      <div className={classes.container}>
        <Button
          name='addSection'
          classes={{ root: classes.button }}
          variant='contained'
          color='secondary'
          onClick={onAddSection}
        >
          Add Section
        </Button>
        <Button
          name='addProperty'
          classes={{ root: classes.button }}
          variant='contained'
          color='secondary'
          disabled={!this.isSectionOrPropertySelected()}
          onClick={onAddProperty}
        >
          Add Property
        </Button>
        <Button
          name='remove'
          classes={{ root: classes.button }}
          variant='contained'
          color='secondary'
          disabled={!this.isSectionOrPropertySelected()}
          onClick={onRemove}
        >
          Remove
        </Button>
        <Button
          name='save'
          classes={{ root: classes.button }}
          variant='contained'
          color='primary'
          onClick={onSave}
        >
          Save
        </Button>
      </div>
    )
  }
}

export default withStyles(styles)(TypeFormButtons)
