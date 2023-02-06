import React from 'react'
import GridContainer from '@src/js/components/common/grid/GridContainer.jsx'
import HistoryGrid from '@src/js/components/tools/common/HistoryGrid.jsx'
import logger from '@src/js/common/logger.js'

class HistoryForm extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'HistoryForm.render')

    const { id } = this.props.object

    return (
      <GridContainer>
        <HistoryGrid eventType={id} />
      </GridContainer>
    )
  }
}

export default HistoryForm
