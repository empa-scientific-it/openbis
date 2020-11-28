import _ from 'lodash'
import React from 'react'
import autoBind from 'auto-bind'
import { connect } from 'react-redux'
import { withStyles } from '@material-ui/core/styles'
import ComponentContext from '@src/js/components/common/ComponentContext.js'
import PageWithTwoPanels from '@src/js/components/common/page/PageWithTwoPanels.jsx'
import QueryFormController from '@src/js/components/tools/form/query/QueryFormController.js'
import QueryFormFacade from '@src/js/components/tools/form/query/QueryFormFacade.js'
import QueryFormParameters from '@src/js/components/tools/form/query/QueryFormParameters.jsx'
import QueryFormSql from '@src/js/components/tools/form/query/QueryFormSql.jsx'
import QueryFormButtons from '@src/js/components/tools/form/query/QueryFormButtons.jsx'
import logger from '@src/js/common/logger.js'

const styles = () => ({})

class QueryForm extends React.PureComponent {
  constructor(props) {
    super(props)
    autoBind(this)

    this.state = {}

    if (this.props.controller) {
      this.controller = this.props.controller
    } else {
      this.controller = new QueryFormController(new QueryFormFacade())
    }

    this.controller.init(new ComponentContext(this))
  }

  componentDidMount() {
    this.controller.load()
  }

  render() {
    logger.log(logger.DEBUG, 'QueryForm.render')

    const { loading, loaded, query } = this.state

    return (
      <PageWithTwoPanels
        loading={loading}
        loaded={loaded}
        object={query}
        renderMainPanel={() => this.renderMainPanel()}
        renderAdditionalPanel={() => this.renderAdditionalPanel()}
        renderButtons={() => this.renderButtons()}
      />
    )
  }

  renderMainPanel() {
    const { controller } = this
    const { query, selection, mode } = this.state

    return (
      <QueryFormSql
        query={query}
        selection={selection}
        mode={mode}
        onChange={controller.handleChange}
        onSelectionChange={controller.handleSelectionChange}
        onBlur={controller.handleBlur}
      />
    )
  }

  renderAdditionalPanel() {
    const { controller } = this
    const { query, selection, dictionaries, mode } = this.state

    return (
      <QueryFormParameters
        query={query}
        selection={selection}
        dictionaries={dictionaries}
        mode={mode}
        onChange={controller.handleChange}
        onSelectionChange={controller.handleSelectionChange}
        onBlur={controller.handleBlur}
      />
    )
  }

  renderButtons() {
    const { controller } = this
    const { query, changed, mode } = this.state

    return (
      <QueryFormButtons
        onEdit={controller.handleEdit}
        onSave={controller.handleSave}
        onCancel={controller.handleCancel}
        query={query}
        changed={changed}
        mode={mode}
      />
    )
  }
}

export default _.flow(connect(), withStyles(styles))(QueryForm)
