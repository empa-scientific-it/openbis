import _ from 'lodash'
import React from 'react'
import autoBind from 'auto-bind'
import { connect } from 'react-redux'
import { withStyles } from '@material-ui/core/styles'
import ComponentContext from '@src/js/components/common/ComponentContext.js'
import PageWithTwoPanels from '@src/js/components/common/page/PageWithTwoPanels.jsx'
import ImportAllFormController from '@src/js/components/tools/form/import/all/ImportAllFormController.js'
import ImportAllFormFacade from '@src/js/components/tools/form/import/all/ImportAllFormFacade.js'
import ImportAllFormParameters from '@src/js/components/tools/form/import/all/ImportAllFormParameters.jsx'
import Container from '@src/js/components/common/form/Container.jsx'
import Button from '@src/js/components/common/form/Button.jsx'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

const styles = () => ({
  buttons: {
    display: 'flex',
    justifyContent: 'flex-end'
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

    const { loaded } = this.state

    if (!loaded) {
      return null
    }

    return (
      <PageWithTwoPanels
        loading={false}
        loaded={true}
        object={{}}
        renderMainPanel={() => this.renderMainPanel()}
        renderAdditionalPanel={() => this.renderAdditionalPanel()}
        renderButtons={() => this.renderButtons()}
      />
    )
  }

  renderMainPanel() {
    return <div>Main Panel</div>
  }

  renderAdditionalPanel() {
    const { fields } = this.state
    return (
      <ImportAllFormParameters
        fields={fields}
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

export default _.flow(connect(), withStyles(styles))(ImportAllForm)
