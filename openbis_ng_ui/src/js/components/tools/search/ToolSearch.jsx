import _ from 'lodash'
import autoBind from 'auto-bind'
import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Container from '@src/js/components/common/form/Container.jsx'
import GridContainer from '@src/js/components/common/grid/GridContainer.jsx'
import AppController from '@src/js/components/AppController.js'
import PluginsGrid from '@src/js/components/tools/common/PluginsGrid.jsx'
import QueriesGrid from '@src/js/components/tools/common/QueriesGrid.jsx'
import Message from '@src/js/components/common/form/Message.jsx'
import FormUtil from '@src/js/components/common/form/FormUtil.js'
import ids from '@src/js/common/consts/ids.js'
import objectTypes from '@src/js/common/consts/objectType.js'
import openbis from '@src/js/services/openbis.js'
import util from '@src/js/common/util.js'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  grid: {
    marginBottom: theme.spacing(2)
  }
})

class ToolSearch extends React.Component {
  constructor(props) {
    super(props)
    autoBind(this)

    this.gridControllers = {}

    this.state = {
      loaded: false
    }
  }

  componentDidMount() {
    this.load()
  }

  async load() {
    try {
      await Promise.all([
        this.loadDynamicPropertyPlugins(),
        this.loadEntityValidationPlugins(),
        this.loadQueries()
      ])
      this.setState(() => ({
        loaded: true
      }))
    } catch (error) {
      AppController.getInstance().errorChange(error)
    }
  }

  async loadDynamicPropertyPlugins() {
    if (!this.shouldLoad(objectTypes.DYNAMIC_PROPERTY_PLUGIN)) {
      return
    }

    const dynamicPropertyPlugins = await this.loadPlugins(
      openbis.PluginType.DYNAMIC_PROPERTY
    )

    this.setState({
      dynamicPropertyPlugins
    })
  }

  async loadEntityValidationPlugins() {
    if (!this.shouldLoad(objectTypes.ENTITY_VALIDATION_PLUGIN)) {
      return
    }

    const entityValidationPlugins = await this.loadPlugins(
      openbis.PluginType.ENTITY_VALIDATION
    )

    this.setState({
      entityValidationPlugins
    })
  }

  async loadPlugins(pluginType) {
    const criteria = new openbis.PluginSearchCriteria()
    criteria.withPluginType().thatEquals(pluginType)

    const fo = new openbis.PluginFetchOptions()
    fo.withScript()
    fo.withRegistrator()

    const result = await openbis.searchPlugins(criteria, fo)

    return util
      .filter(result.objects, this.props.searchText, ['name', 'description'])
      .map(plugin => {
        const entityKinds = _.get(plugin, 'entityKinds', [])

        return {
          id: _.get(plugin, 'name'),
          name: FormUtil.createField({ value: _.get(plugin, 'name') }),
          description: FormUtil.createField({
            value: _.get(plugin, 'description')
          }),
          pluginKind: FormUtil.createField({
            value: _.get(plugin, 'pluginKind')
          }),
          entityKind: FormUtil.createField({
            value:
              entityKinds && entityKinds.length === 1 ? entityKinds[0] : null
          }),
          script: FormUtil.createField({ value: _.get(plugin, 'script') }),
          registrator: FormUtil.createField({
            value: _.get(plugin, 'registrator.userId')
          })
        }
      })
  }

  async loadQueries() {
    if (!this.shouldLoad(objectTypes.QUERY)) {
      return
    }

    const fo = new openbis.QueryFetchOptions()
    fo.withRegistrator()

    const result = await openbis.searchQueries(
      new openbis.QuerySearchCriteria(),
      fo
    )

    const queries = util
      .filter(result.objects, this.props.searchText, ['name', 'description'])
      .map(query => ({
        id: _.get(query, 'name'),
        name: FormUtil.createField({ value: _.get(query, 'name') }),
        description: FormUtil.createField({
          value: _.get(query, 'description')
        }),
        database: FormUtil.createField({
          value: _.get(query, 'databaseLabel')
        }),
        queryType: FormUtil.createField({
          value: _.get(query, 'queryType')
        }),
        entityTypeCodePattern: FormUtil.createField({
          value: _.get(query, 'entityTypeCodePattern')
        }),
        sql: FormUtil.createField({
          value: _.get(query, 'sql')
        }),
        publicFlag: FormUtil.createField({
          value: _.get(query, 'publicFlag')
        }),
        registrator: FormUtil.createField({
          value: _.get(query, 'registrator.userId')
        })
      }))

    this.setState({
      queries
    })
  }

  shouldLoad(objectType) {
    return this.props.objectType === objectType || !this.props.objectType
  }

  handleContainerClick() {
    for (let gridObjectType in this.gridControllers) {
      this.gridControllers[gridObjectType].selectRow(null)
    }
  }

  handleSelectedRowChange(objectType) {
    return row => {
      if (!row) {
        return
      }
      for (let gridObjectType in this.gridControllers) {
        if (gridObjectType !== objectType) {
          this.gridControllers[gridObjectType].selectRow(null)
        }
      }
    }
  }

  render() {
    logger.log(logger.DEBUG, 'ToolSearch.render')

    if (!this.state.loaded) {
      return null
    }

    return (
      <GridContainer onClick={this.handleContainerClick}>
        {this.renderNoResultsFoundMessage()}
        {this.renderDynamicPropertyPlugins()}
        {this.renderEntityValidationPlugins()}
        {this.renderQueries()}
      </GridContainer>
    )
  }

  renderNoResultsFoundMessage() {
    const { objectType } = this.props
    const {
      dynamicPropertyPlugins = [],
      entityValidationPlugins = [],
      queries = []
    } = this.state

    if (
      !objectType &&
      dynamicPropertyPlugins.length === 0 &&
      entityValidationPlugins.length === 0 &&
      queries.length === 0
    ) {
      return (
        <Container>
          <Message type='info'>
            {messages.get(messages.NO_RESULTS_FOUND)}
          </Message>
        </Container>
      )
    } else {
      return null
    }
  }

  renderDynamicPropertyPlugins() {
    if (
      this.shouldRender(
        objectTypes.DYNAMIC_PROPERTY_PLUGIN,
        this.state.dynamicPropertyPlugins
      )
    ) {
      const { classes } = this.props
      return (
        <div className={classes.grid}>
          <PluginsGrid
            id={ids.DYNAMIC_PROPERTY_PLUGINS_GRID_ID}
            controllerRef={controller =>
              (this.gridControllers[objectTypes.DYNAMIC_PROPERTY_PLUGIN] =
                controller)
            }
            pluginType={openbis.PluginType.DYNAMIC_PROPERTY}
            rows={this.state.dynamicPropertyPlugins}
            onSelectedRowChange={this.handleSelectedRowChange(
              objectTypes.DYNAMIC_PROPERTY_PLUGIN
            )}
          />
        </div>
      )
    } else {
      return null
    }
  }

  renderEntityValidationPlugins() {
    if (
      this.shouldRender(
        objectTypes.ENTITY_VALIDATION_PLUGIN,
        this.state.entityValidationPlugins
      )
    ) {
      const { classes } = this.props
      return (
        <div className={classes.grid}>
          <PluginsGrid
            id={ids.ENTITY_VALIDATION_PLUGINS_GRID_ID}
            controllerRef={controller =>
              (this.gridControllers[objectTypes.ENTITY_VALIDATION_PLUGIN] =
                controller)
            }
            pluginType={openbis.PluginType.ENTITY_VALIDATION}
            rows={this.state.entityValidationPlugins}
            onSelectedRowChange={this.handleSelectedRowChange(
              objectTypes.ENTITY_VALIDATION_PLUGIN
            )}
          />
        </div>
      )
    } else {
      return null
    }
  }

  renderQueries() {
    if (this.shouldRender(objectTypes.QUERY, this.state.queries)) {
      const { classes } = this.props
      return (
        <div className={classes.grid}>
          <QueriesGrid
            id={ids.QUERIES_GRID_ID}
            controllerRef={controller =>
              (this.gridControllers[objectTypes.QUERY] = controller)
            }
            rows={this.state.queries}
            onSelectedRowChange={this.handleSelectedRowChange(
              objectTypes.QUERY
            )}
          />
        </div>
      )
    } else {
      return null
    }
  }

  shouldRender(objectType, types) {
    return this.props.objectType === objectType || (types && types.length > 0)
  }
}

export default withStyles(styles)(ToolSearch)
