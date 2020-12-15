import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Container from '@src/js/components/common/form/Container.jsx'
import Header from '@src/js/components/common/form/Header.jsx'
import Typography from '@material-ui/core/Typography'
import Loading from '@src/js/components/common/loading/Loading.jsx'
import openbis from '@src/js/services/openbis.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  result: {
    fontSize: theme.typography.body2.fontSize
  }
})

class PluginFormEvaluateResults extends React.PureComponent {
  constructor(props) {
    super(props)
  }

  render() {
    logger.log(logger.DEBUG, 'PluginFormEvaluateResults.render')

    const { results } = this.props

    if (results) {
      const { loading, loaded } = results
      return (
        <Loading loading={loading}>
          {loaded && (
            <Container>
              <Header>Result</Header>
              {this.renderResult()}
            </Container>
          )}
        </Loading>
      )
    } else {
      return null
    }
  }

  renderResult() {
    const {
      plugin,
      results: { result },
      classes
    } = this.props

    if (result) {
      if (plugin.pluginType === openbis.PluginType.DYNAMIC_PROPERTY) {
        return (
          <Typography className={classes.result}>{result.value}</Typography>
        )
      } else if (plugin.pluginType === openbis.PluginType.ENTITY_VALIDATION) {
        return (
          <Typography className={classes.result}>{result.error}</Typography>
        )
      } else {
        throw new Error('Unsupported pluginType: ' + plugin.pluginType)
      }
    } else {
      return null
    }
  }
}

export default withStyles(styles)(PluginFormEvaluateResults)
