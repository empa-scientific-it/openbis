import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import logger from '@src/js/common/logger.js'
import pages from '@src/js/common/consts/pages.js'
import objectType from '@src/js/common/consts/objectType.js'
import Content from '@src/js/components/common/content/Content.jsx'
import ContentTab from '@src/js/components/common/content/ContentTab.jsx'
import ToolBrowser from '@src/js/components/tools/browser/ToolBrowser.jsx'
import ToolSearch from '@src/js/components/tools/search/ToolSearch.jsx'
import PluginForm from '@src/js/components/tools/form/plugin/PluginForm.jsx'
import QueryForm from '@src/js/components/tools/form/query/QueryForm.jsx'
import messages from '@src/js/common/messages.js'

const styles = () => ({
  container: {
    display: 'flex',
    width: '100%'
  }
})

class Tools extends React.Component {
  render() {
    logger.log(logger.DEBUG, 'Tools.render')

    const classes = this.props.classes

    return (
      <div className={classes.container}>
        <ToolBrowser />
        <Content
          page={pages.TOOLS}
          renderComponent={this.renderComponent}
          renderTab={this.renderTab}
        />
      </div>
    )
  }

  renderComponent(tab) {
    const { object } = tab
    if (
      object.type === objectType.NEW_DYNAMIC_PROPERTY_PLUGIN ||
      object.type === objectType.NEW_ENTITY_VALIDATION_PLUGIN ||
      object.type === objectType.DYNAMIC_PROPERTY_PLUGIN ||
      object.type === objectType.ENTITY_VALIDATION_PLUGIN
    ) {
      return <PluginForm object={object} />
    } else if (
      object.type === objectType.NEW_QUERY ||
      object.type === objectType.QUERY
    ) {
      return <QueryForm object={object} />
    } else if (object.type === objectType.SEARCH) {
      return <ToolSearch searchText={object.id} />
    } else if (object.type === objectType.OVERVIEW) {
      return <ToolSearch objectType={object.id} />
    }
  }

  renderTab(tab) {
    const { object, changed } = tab

    let label = null

    if (object.type === objectType.OVERVIEW) {
      const labels = {
        [objectType.DYNAMIC_PROPERTY_PLUGIN]: messages.get(
          messages.DYNAMIC_PROPERTY_PLUGINS
        ),
        [objectType.ENTITY_VALIDATION_PLUGIN]: messages.get(
          messages.ENTITY_VALIDATION_PLUGINS
        ),
        [objectType.QUERY]: messages.get(messages.QUERIES)
      }
      label = labels[object.id]
    } else {
      const prefixes = {
        [objectType.NEW_DYNAMIC_PROPERTY_PLUGIN]:
          messages.get(messages.NEW_DYNAMIC_PROPERTY_PLUGIN) + ' ',
        [objectType.NEW_ENTITY_VALIDATION_PLUGIN]:
          messages.get(messages.NEW_ENTITY_VALIDATION_PLUGIN) + ' ',
        [objectType.NEW_QUERY]: messages.get(messages.NEW_QUERY) + ' ',
        [objectType.DYNAMIC_PROPERTY_PLUGIN]:
          messages.get(messages.DYNAMIC_PROPERTY_PLUGIN) + ': ',
        [objectType.ENTITY_VALIDATION_PLUGIN]:
          messages.get(messages.ENTITY_VALIDATION_PLUGIN) + ': ',
        [objectType.QUERY]: messages.get(messages.QUERY) + ': ',
        [objectType.SEARCH]: messages.get(messages.SEARCH) + ': '
      }
      label = prefixes[object.type] + object.id
    }

    return <ContentTab label={label} changed={changed} />
  }
}

export default withStyles(styles)(Tools)
