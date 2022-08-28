import _ from 'lodash'
import autoBind from 'auto-bind'
import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Container from '@src/js/components/common/form/Container.jsx'
import GridContainer from '@src/js/components/common/grid/GridContainer.jsx'
import AppController from '@src/js/components/AppController.js'
import ServerInformation from '@src/js/components/common/dto/ServerInformation.js'
import PluginsGrid from '@src/js/components/tools/common/PluginsGrid.jsx'
import QueriesGrid from '@src/js/components/tools/common/QueriesGrid.jsx'
import PersonalAccessTokensGrid from '@src/js/components/tools/common/PersonalAccessTokensGrid.jsx'
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
        this.loadQueries(),
        this.loadPersonalAccessTokens()
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

  async loadPersonalAccessTokens() {
    const personalAccessTokensEnabled =
      AppController.getInstance().getServerInformation(
        ServerInformation.PERSONAL_ACCESS_TOKENS_ENABLED
      )

    if (personalAccessTokensEnabled !== 'true') {
      return
    }

    if (!this.shouldLoad(objectTypes.PERSONAL_ACCESS_TOKEN)) {
      return
    }

    const fo = new openbis.PersonalAccessTokenFetchOptions()
    fo.withOwner()
    fo.withRegistrator()
    fo.withModifier()

    const result = await openbis.searchPersonalAccessTokens(
      new openbis.PersonalAccessTokenSearchCriteria(),
      fo
    )

    const pats = util
      .filter(result.objects, this.props.searchText, ['hash', 'sessionName'])
      .map(pat => {
        const validFromDate = _.get(pat, 'validFromDate', null)
        const validToDate = _.get(pat, 'validToDate', null)
        const registrationDate = _.get(pat, 'registrationDate', null)
        const accessDate = _.get(pat, 'accessDate', null)

        return {
          id: _.get(pat, 'hash'),
          hash: FormUtil.createField({
            value: _.get(pat, 'hash', null)
          }),
          sessionName: FormUtil.createField({
            value: _.get(pat, 'sessionName', null)
          }),
          validFromDate: FormUtil.createField({
            value: validFromDate
              ? {
                  dateObject: new Date(validFromDate)
                }
              : null
          }),
          validToDate: FormUtil.createField({
            value: validToDate
              ? {
                  dateObject: new Date(validToDate)
                }
              : null
          }),
          owner: FormUtil.createField({
            value: pat.owner ? pat.owner.userId : null
          }),
          registrator: FormUtil.createField({
            value: pat.registrator ? pat.registrator.userId : null
          }),
          registrationDate: FormUtil.createField({
            value: registrationDate
              ? {
                  dateObject: new Date(registrationDate)
                }
              : null
          }),
          accessDate: FormUtil.createField({
            value: accessDate
              ? {
                  dateObject: new Date(accessDate)
                }
              : null
          })
        }
      })

    this.setState({
      pats
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
        {this.renderPersonalAccessTokens()}
      </GridContainer>
    )
  }

  renderNoResultsFoundMessage() {
    const { objectType } = this.props
    const {
      dynamicPropertyPlugins = [],
      entityValidationPlugins = [],
      queries = [],
      pats = []
    } = this.state

    if (
      !objectType &&
      dynamicPropertyPlugins.length === 0 &&
      entityValidationPlugins.length === 0 &&
      queries.length === 0 &&
      pats.length === 0
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

  renderPersonalAccessTokens() {
    const personalAccessTokensEnabled =
      AppController.getInstance().getServerInformation(
        ServerInformation.PERSONAL_ACCESS_TOKENS_ENABLED
      )

    if (personalAccessTokensEnabled !== 'true') {
      return
    }

    if (this.shouldRender(objectTypes.PERSONAL_ACCESS_TOKEN, this.state.pats)) {
      const { classes } = this.props
      return (
        <div className={classes.grid}>
          <PersonalAccessTokensGrid
            id={ids.PERSONAL_ACCESS_TOKEN_GRID_ID}
            controllerRef={controller =>
              (this.gridControllers[objectTypes.PERSONAL_ACCESS_TOKEN] =
                controller)
            }
            rows={this.state.pats}
            onSelectedRowChange={this.handleSelectedRowChange(
              objectTypes.PERSONAL_ACCESS_TOKEN
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
