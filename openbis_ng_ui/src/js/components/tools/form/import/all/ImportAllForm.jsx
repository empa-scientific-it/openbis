import React from 'react'
import autoBind from 'auto-bind'
import { withStyles } from '@material-ui/core/styles'
import ComponentContext from '@src/js/components/common/ComponentContext.js'
import PageWithTwoPanels from '@src/js/components/common/page/PageWithTwoPanels.jsx'
import ImportAllFormController from '@src/js/components/tools/form/import/all/ImportAllFormController.js'
import ImportAllFormFacade from '@src/js/components/tools/form/import/all/ImportAllFormFacade.js'
import logger from '@src/js/common/logger.js'

const styles = () => ({})

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

  render() {
    logger.log(logger.DEBUG, 'ImportAllForm.render')

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
    return <div>Additional Panel</div>
  }

  renderButtons() {
    return <div>Buttons</div>
  }
}

export default withStyles(styles)(ImportAllForm)
