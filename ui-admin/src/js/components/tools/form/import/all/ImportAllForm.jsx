import React from 'react'
import autoBind from 'auto-bind'
import { withStyles } from '@material-ui/core/styles'
import ComponentContext from '@src/js/components/common/ComponentContext.js'
import PageWithTwoPanels from '@src/js/components/common/page/PageWithTwoPanels.jsx'
import ImportAllFormController from '@src/js/components/tools/form/import/all/ImportAllFormController.js'
import ImportAllFormFacade from '@src/js/components/tools/form/import/all/ImportAllFormFacade.js'
import ImportAllFormParameters from '@src/js/components/tools/form/import/all/ImportAllFormParameters.jsx'
import Container from '@src/js/components/common/form/Container.jsx'
import Header from '@src/js/components/common/form/Header.jsx'
import Button from '@src/js/components/common/form/Button.jsx'
import Link from '@src/js/components/common/form/Link.jsx'
import Collapse from '@material-ui/core/Collapse'
import SourceCodeField from '@src/js/components/common/form/SourceCodeField.jsx'
import Message from '@src/js/components/common/form/Message.jsx'
import ids from '@src/js/common/consts/ids.js'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  buttons: {
    display: 'flex',
    justifyContent: 'flex-end'
  },
  result: {
    fontSize: theme.typography.body2.fontSize,
    marginTop: theme.spacing(1)
  },
  error: {
    marginTop: theme.spacing(1)
  }
})

class ImportAllForm extends React.PureComponent {
  constructor(props) {
    super(props)
    autoBind(this)

    this.state = {}

    if (this.props.controller) {
      this.controller = this.props.controller
    } else {
      this.controller = new ImportAllFormController(new ImportAllFormFacade())
    }

    this.controller.init(new ComponentContext(this))
  }

  componentDidMount() {
    this.controller.load()
  }

  render() {
    logger.log(logger.DEBUG, 'ImportAllForm.render')

    const { loaded, loading } = this.state

    if (!loaded) {
      return null
    }

    return (
      <PageWithTwoPanels
        id={ids.IMPORT_ALL_FORM_ID}
        loading={loading}
        loaded={loaded}
        object={{}}
        renderMainPanel={() => this.renderMainPanel()}
        renderAdditionalPanel={() => this.renderAdditionalPanel()}
        renderButtons={() => this.renderButtons()}
      />
    )
  }

  renderMainPanel() {
    const { result } = this.state

    if (result === null) {
      return <Container></Container>
    }

    return (
      <Container>
        <Header>{messages.get(messages.RESULT)}</Header>
        <Message type={result.success ? 'success' : 'error'}>
          {messages.get(
            result.success ? messages.IMPORT_SUCCEEDED : messages.IMPORT_FAILED
          )}
        </Message>
        {this.renderResult()}
      </Container>
    )
  }

  renderResult() {
    const { classes } = this.props
    const { result } = this.state

    if (result.success) {
      return null
    }

    return (
      <div className={classes.result}>
        <Link onClick={this.controller.handleToggleResult}>
          {result.visible
            ? messages.get(messages.HIDE_DETAILS)
            : messages.get(messages.SHOW_DETAILS)}
        </Link>
        <div className={classes.error}>
          <Collapse
            in={result.visible}
            mountOnEnter={true}
            unmountOnExit={true}
          >
            <SourceCodeField
              language='log'
              label={messages.get(messages.ERROR)}
              readOnly={true}
              value={result.output}
            />
          </Collapse>
        </div>
      </div>
    )
  }

  renderAdditionalPanel() {
    const { fields, selection } = this.state
    return (
      <ImportAllFormParameters
        fields={fields}
        selection={selection}
        onChange={this.controller.handleChange}
        onBlur={this.controller.handleBlur}
      />
    )
  }

  renderButtons() {
    const { classes } = this.props
    return (
      <Container className={classes.buttons}>
        <Button
          name='import'
          label={messages.get(messages.IMPORT)}
          onClick={this.controller.import}
        />
      </Container>
    )
  }
}

export default withStyles(styles)(ImportAllForm)
