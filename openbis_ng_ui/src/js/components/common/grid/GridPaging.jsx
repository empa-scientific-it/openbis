import _ from 'lodash'
import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Typography from '@material-ui/core/Typography'
import FormControlLabel from '@material-ui/core/FormControlLabel'
import IconButton from '@material-ui/core/IconButton'
import FirstPageIcon from '@material-ui/icons/FirstPage'
import KeyboardArrowLeft from '@material-ui/icons/KeyboardArrowLeft'
import KeyboardArrowRight from '@material-ui/icons/KeyboardArrowRight'
import LastPageIcon from '@material-ui/icons/LastPage'
import SelectField from '@src/js/components/common/form/SelectField.jsx'
import GridPagingOptions from '@src/js/components/common/grid/GridPagingOptions.js'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  container: {
    display: 'flex',
    alignItems: 'center',
    flexShrink: 0
  },
  pageSizeLabelPlacement: {
    marginRight: 0
  },
  pageSizeLabel: {
    fontSize: theme.typography.body2.fontSize,
    marginRight: '12px',
    whiteSpace: 'nowrap',
    lineHeight: '46px'
  },
  pageRange: {
    marginLeft: '24px'
  },
  pageButtons: {
    marginLeft: '12px',
    marginRight: '-12px'
  }
})

class GridPaging extends React.PureComponent {
  constructor(props) {
    super(props)
    this.handlePageSizeChange = this.handlePageSizeChange.bind(this)
    this.handleFirstPageButtonClick = this.handleFirstPageButtonClick.bind(this)
    this.handleBackButtonClick = this.handleBackButtonClick.bind(this)
    this.handleNextButtonClick = this.handleNextButtonClick.bind(this)
    this.handleLastPageButtonClick = this.handleLastPageButtonClick.bind(this)
  }

  handlePageSizeChange(event) {
    this.props.onPageSizeChange(event.target.value)
  }

  handleFirstPageButtonClick() {
    this.props.onPageChange(0)
  }

  handleBackButtonClick() {
    this.props.onPageChange(this.props.page - 1)
  }

  handleNextButtonClick() {
    this.props.onPageChange(this.props.page + 1)
  }

  handleLastPageButtonClick() {
    this.props.onPageChange(
      Math.max(0, Math.ceil(this.props.count / this.props.pageSize) - 1)
    )
  }

  render() {
    logger.log(logger.DEBUG, 'GridPaging.render')

    const { classes, count, page, pageSize } = this.props

    return (
      <div className={classes.container}>
        <div className={classes.pageSize}>
          <FormControlLabel
            control={
              <SelectField
                value={pageSize}
                options={GridPagingOptions.PAGE_SIZE_OPTIONS.map(pageSize => ({
                  label: pageSize,
                  value: pageSize
                }))}
                onChange={this.handlePageSizeChange}
                variant='standard'
              />
            }
            classes={{
              label: classes.pageSizeLabel,
              labelPlacementStart: classes.pageSizeLabelPlacement
            }}
            label={messages.get(messages.ROWS_PER_PAGE)}
            labelPlacement='start'
          />
        </div>
        <div className={classes.pageRange}>
          <Typography variant='body2' data-part='range'>
            {Math.min(count, page * pageSize + 1)}-
            {Math.min(count, (page + 1) * pageSize)} of {count}
          </Typography>
        </div>
        <div className={classes.pageButtons}>
          <IconButton
            onClick={this.handleFirstPageButtonClick}
            disabled={page === 0}
            aria-label={messages.get(messages.FIRST_PAGE)}
            data-part='firstPage'
          >
            <FirstPageIcon fontSize='small' />
          </IconButton>
          <IconButton
            onClick={this.handleBackButtonClick}
            disabled={page === 0}
            aria-label={messages.get(messages.PREVIOUS_PAGE)}
            data-part='prevPage'
          >
            <KeyboardArrowLeft fontSize='small' />
          </IconButton>
          <IconButton
            onClick={this.handleNextButtonClick}
            disabled={page >= Math.ceil(count / pageSize) - 1}
            aria-label={messages.get(messages.NEXT_PAGE)}
            data-part='nextPage'
          >
            <KeyboardArrowRight fontSize='small' />
          </IconButton>
          <IconButton
            onClick={this.handleLastPageButtonClick}
            disabled={page >= Math.ceil(count / pageSize) - 1}
            aria-label={messages.get(messages.LAST_PAGE)}
            data-part='lastPage'
          >
            <LastPageIcon fontSize='small' />
          </IconButton>
        </div>
      </div>
    )
  }
}

export default _.flow(withStyles(styles))(GridPaging)
