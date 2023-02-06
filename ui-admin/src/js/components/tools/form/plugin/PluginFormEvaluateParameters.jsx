import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Container from '@src/js/components/common/form/Container.jsx'
import Header from '@src/js/components/common/form/Header.jsx'
import EntityKind from '@src/js/components/common/dto/EntityKind.js'
import EntityAutocompleterField from '@src/js/components/common/form/EntityAutocompleterField.jsx'
import SelectField from '@src/js/components/common/form/SelectField.jsx'
import CheckboxField from '@src/js/components/common/form/CheckboxField.jsx'
import PluginFormSelectionType from '@src/js/components/tools/form/plugin/PluginFormSelectionType.js'
import openbis from '@src/js/services/openbis.js'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  parameter: {
    paddingBottom: theme.spacing(1)
  }
})

class PluginFormEvaluateParameters extends React.PureComponent {
  constructor(props) {
    super(props)
    this.handleChange = this.handleChange.bind(this)
  }

  handleChange(event) {
    this.props.onChange(PluginFormSelectionType.EVALUATE_PARAMETER, {
      field: event.target.name,
      value: event.target.value
    })
  }

  render() {
    logger.log(logger.DEBUG, 'PluginFormEvaluateParameters.render')

    return (
      <Container>
        <Header>{messages.get(messages.TESTER)}</Header>
        {this.renderEntityKind()}
        {this.renderEntity()}
        {this.renderEntityIsNew()}
      </Container>
    )
  }

  renderEntityKind() {
    const { parameters, classes } = this.props

    const options = openbis.EntityKind.values.map(entityKind => {
      return {
        label: new EntityKind(entityKind).getLabel(),
        value: entityKind
      }
    })

    return (
      <div className={classes.parameter}>
        <SelectField
          label={messages.get(messages.ENTITY_KIND)}
          name='entityKind'
          value={parameters.entityKind.value}
          disabled={!parameters.entityKind.enabled}
          options={options}
          onChange={this.handleChange}
        />
      </div>
    )
  }

  renderEntity() {
    const { parameters, classes } = this.props

    return (
      <div className={classes.parameter}>
        <EntityAutocompleterField
          key={parameters.entityKind.value}
          label={messages.get(messages.ENTITY)}
          name='entity'
          entityKind={parameters.entityKind.value}
          value={parameters.entity.value}
          disabled={!parameters.entity.enabled}
          onChange={this.handleChange}
        />
      </div>
    )
  }

  renderEntityIsNew() {
    const { plugin, parameters, classes } = this.props

    if (plugin.pluginType === openbis.PluginType.ENTITY_VALIDATION) {
      return (
        <div className={classes.parameter}>
          <CheckboxField
            label={messages.get(messages.IS_NEW_ENTITY)}
            name='entityIsNew'
            value={parameters.entityIsNew.value}
            disabled={!parameters.entityIsNew.enabled}
            onChange={this.handleChange}
          />
        </div>
      )
    } else {
      return null
    }
  }
}

export default withStyles(styles)(PluginFormEvaluateParameters)
