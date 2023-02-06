import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import EntityTypeFormParametersType from '@src/js/components/types/form/entitytype/EntityTypeFormParametersType.jsx'
import EntityTypeFormParametersProperty from '@src/js/components/types/form/entitytype/EntityTypeFormParametersProperty.jsx'
import EntityTypeFormParametersSection from '@src/js/components/types/form/entitytype/EntityTypeFormParametersSection.jsx'
import logger from '@src/js/common/logger.js'

const styles = () => ({})

class EntityTypeFormParameters extends React.PureComponent {
  constructor(props) {
    super(props)
  }

  render() {
    logger.log(logger.DEBUG, 'EntityTypeFormParameters.render')

    const {
      controller,
      type,
      sections,
      properties,
      selection,
      mode,
      onChange,
      onSelectionChange,
      onBlur
    } = this.props

    return (
      <div>
        <EntityTypeFormParametersType
          controller={controller}
          type={type}
          selection={selection}
          mode={mode}
          onChange={onChange}
          onSelectionChange={onSelectionChange}
          onBlur={onBlur}
        />
        <EntityTypeFormParametersSection
          sections={sections}
          selection={selection}
          mode={mode}
          onChange={onChange}
          onSelectionChange={onSelectionChange}
          onBlur={onBlur}
        />
        <EntityTypeFormParametersProperty
          controller={controller}
          type={type}
          properties={properties}
          selection={selection}
          mode={mode}
          onChange={onChange}
          onSelectionChange={onSelectionChange}
          onBlur={onBlur}
        />
      </div>
    )
  }
}

export default withStyles(styles)(EntityTypeFormParameters)
