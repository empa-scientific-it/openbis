import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import VocabularyTypeFormParametersVocabulary from '@src/js/components/types/form/vocabularytype/VocabularyTypeFormParametersVocabulary.jsx'
import VocabularyTypeFormParametersTerm from '@src/js/components/types/form/vocabularytype/VocabularyTypeFormParametersTerm.jsx'
import logger from '@src/js/common/logger.js'

const styles = () => ({})

class VocabularyTypeFormParameters extends React.PureComponent {
  constructor(props) {
    super(props)
  }

  render() {
    logger.log(logger.DEBUG, 'VocabularyTypeFormParameters.render')

    const {
      controller,
      vocabulary,
      terms,
      selection,
      selectedRow,
      mode,
      onChange,
      onSelectionChange,
      onBlur
    } = this.props

    return (
      <div>
        <VocabularyTypeFormParametersVocabulary
          controller={controller}
          vocabulary={vocabulary}
          selection={selection}
          mode={mode}
          onChange={onChange}
          onSelectionChange={onSelectionChange}
          onBlur={onBlur}
        />
        <VocabularyTypeFormParametersTerm
          controller={controller}
          vocabulary={vocabulary}
          terms={terms}
          selection={selection}
          selectedRow={selectedRow}
          mode={mode}
          onChange={onChange}
          onSelectionChange={onSelectionChange}
          onBlur={onBlur}
        />
      </div>
    )
  }
}

export default withStyles(styles)(VocabularyTypeFormParameters)
