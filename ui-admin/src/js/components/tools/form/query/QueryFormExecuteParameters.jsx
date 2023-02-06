import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Container from '@src/js/components/common/form/Container.jsx'
import Header from '@src/js/components/common/form/Header.jsx'
import TextField from '@src/js/components/common/form/TextField.jsx'
import QueryFormSelectionType from '@src/js/components/tools/form/query/QueryFormSelectionType.js'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  parameter: {
    paddingBottom: theme.spacing(1)
  }
})

class QueryFormExecuteParameters extends React.PureComponent {
  constructor(props) {
    super(props)
    this.handleChange = this.handleChange.bind(this)
  }

  handleChange(event) {
    this.props.onChange(QueryFormSelectionType.EXECUTE_PARAMETER, {
      field: event.target.name,
      value: event.target.value
    })
  }

  render() {
    logger.log(logger.DEBUG, 'QueryFormExecuteParameters.render')

    const { names, values } = this.props.parameters

    if (names.length > 0) {
      return (
        <Container>
          <Header>{messages.get(messages.PARAMETERS)}</Header>
          {names.map(name => this.renderParameter(name, values[name]))}
        </Container>
      )
    } else {
      return null
    }
  }

  renderParameter(name, value) {
    const { classes } = this.props
    return (
      <div key={name} className={classes.parameter}>
        <TextField
          label={name}
          name={name}
          value={value}
          onChange={this.handleChange}
        />
      </div>
    )
  }
}

export default withStyles(styles)(QueryFormExecuteParameters)
