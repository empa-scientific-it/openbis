import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import { Resizable } from 're-resizable'
import Container from '@src/js/components/common/form/Container.jsx'
import Loading from '@src/js/components/common/loading/Loading.jsx'
import Message from '@src/js/components/common/form/Message.jsx'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  container: {
    height: '100%',
    display: 'flex',
    flexDirection: 'column'
  },
  content: {
    display: 'flex',
    flexDirection: 'row',
    flex: '1 1 auto',
    overflow: 'auto',
    height: '100%'
  },
  mainPanel: {
    height: '100%',
    flex: '1 1 auto',
    display: 'flex',
    flexDirection: 'column',
    overflow: 'auto'
  },
  additionalPanel: {
    borderLeft: `1px solid ${theme.palette.border.primary}`,
    height: '100%',
    overflow: 'auto',
    flex: '0 0 auto'
  },
  buttons: {
    flex: '0 0 auto',
    borderWidth: '1px 0px 0px 0px',
    borderColor: theme.palette.border.primary,
    borderStyle: 'solid'
  }
})

class PageWithTwoPanels extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'PageWithTwoPanels.render')

    const { loaded, loading } = this.props

    return <Loading loading={loading}>{loaded && this.doRender()}</Loading>
  }

  doRender() {
    const { object } = this.props

    if (object) {
      return this.doRenderExisting()
    } else {
      return this.doRenderNonExistent()
    }
  }

  doRenderExisting() {
    const { classes, renderMainPanel, renderAdditionalPanel, renderButtons } =
      this.props

    const mainPanel = renderMainPanel ? renderMainPanel() : <div></div>
    const additionalPanel = renderAdditionalPanel ? (
      renderAdditionalPanel()
    ) : (
      <div></div>
    )
    const buttons = renderButtons ? renderButtons() : <div></div>

    return (
      <div className={classes.container}>
        <div className={classes.content}>
          {<div className={classes.mainPanel}>{mainPanel}</div>}
          {
            <Resizable
              defaultSize={{
                width: 400,
                height: '100%'
              }}
              enable={{
                left: true,
                top: false,
                right: false,
                bottom: false,
                topRight: false,
                bottomRight: false,
                bottomLeft: false,
                topLeft: false
              }}
            >
              <div className={classes.additionalPanel}>{additionalPanel}</div>
            </Resizable>
          }
        </div>
        {<div className={classes.buttons}>{buttons}</div>}
      </div>
    )
  }

  doRenderNonExistent() {
    return (
      <Container>
        <Message type='info'>
          {messages.get(messages.OBJECT_DOES_NOT_EXIST)}
        </Message>
      </Container>
    )
  }
}

export default withStyles(styles)(PageWithTwoPanels)
