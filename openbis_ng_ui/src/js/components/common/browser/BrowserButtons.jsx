import _ from 'lodash'
import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Container from '@src/js/components/common/form/Container.jsx'
import Button from '@src/js/components/common/form/Button.jsx'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

import AppController from '@src/js/components/AppController.js'

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

class BrowserButtons extends React.Component {
  render() {
    logger.log(logger.DEBUG, 'BrowserButtons.render')

    const {
      controller,
      classes,
      addEnabled,
      removeEnabled,
      lastObjectModifications
    } = this.props

    return (
      <Container className={classes.container}>
        <Button
          label={messages.get(messages.ADD)}
          name='add'
          styles={{ root: classes.button }}
          onClick={controller.nodeAdd}
          disabled={!addEnabled}
        />
        <Button
          label={messages.get(messages.REMOVE)}
          name='remove'
          styles={{ root: classes.button }}
          onClick={controller.nodeRemove}
          disabled={!removeEnabled}
        />
        {lastObjectModifications}
      </Container>
    )
  }
}

export default _.flow(
  withStyles(styles),
  AppController.withLastObjectModifications()
)(BrowserButtons)
