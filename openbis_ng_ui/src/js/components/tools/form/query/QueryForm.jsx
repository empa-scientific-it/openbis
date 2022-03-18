import React from 'react'
import autoBind from 'auto-bind'
import ComponentContext from '@src/js/components/common/ComponentContext.js'
import PageWithTwoPanels from '@src/js/components/common/page/PageWithTwoPanels.jsx'
import QueryFormController from '@src/js/components/tools/form/query/QueryFormController.js'
import QueryFormFacade from '@src/js/components/tools/form/query/QueryFormFacade.js'
import QueryFormParameters from '@src/js/components/tools/form/query/QueryFormParameters.jsx'
import QueryFormSql from '@src/js/components/tools/form/query/QueryFormSql.jsx'
import QueryFormExecuteParameters from '@src/js/components/tools/form/query/QueryFormExecuteParameters.jsx'
import QueryFormExecuteResults from '@src/js/components/tools/form/query/QueryFormExecuteResults.jsx'
import QueryFormButtons from '@src/js/components/tools/form/query/QueryFormButtons.jsx'
import logger from '@src/js/common/logger.js'

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
    const {
      query,
      executeParameters,
      executeResults,
      selection,
      dictionaries,
      mode
    } = this.state

    return (
      <React.Fragment>
        <QueryFormSql
          query={query}
          selection={selection}
          mode={mode}
          onChange={controller.handleChange}
          onSelectionChange={controller.handleSelectionChange}
          onBlur={controller.handleBlur}
        />
        <QueryFormExecuteParameters
          parameters={executeParameters}
          onChange={controller.handleChange}
        />
        <QueryFormExecuteResults
          query={query}
          results={executeResults}
          dictionaries={dictionaries}
        />
      </React.Fragment>
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
        onExecute={controller.handleExecute}
        query={query}
        changed={changed}
        mode={mode}
      />
    )
  }
}

export default QueryForm
