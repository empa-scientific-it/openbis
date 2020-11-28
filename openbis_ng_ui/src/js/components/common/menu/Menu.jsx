import React from 'react'
import AppBar from '@material-ui/core/AppBar'
import Toolbar from '@material-ui/core/Toolbar'
import Tabs from '@material-ui/core/Tabs'
import Tab from '@material-ui/core/Tab'
import TextField from '@material-ui/core/TextField'
import InputAdornment from '@material-ui/core/InputAdornment'
import SearchIcon from '@material-ui/icons/Search'
import CloseIcon from '@material-ui/icons/Close'
import LogoutIcon from '@material-ui/icons/PowerSettingsNew'
import { fade } from '@material-ui/core/styles/colorManipulator'
import { connect } from 'react-redux'
import { withStyles } from '@material-ui/core/styles'
import actions from '@src/js/store/actions/actions.js'
import selectors from '@src/js/store/selectors/selectors.js'
import Button from '@src/js/components/common/form/Button.jsx'
import pages from '@src/js/common/consts/pages.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  appBar: {
    position: 'relative',
    zIndex: 400
  },
  toolBar: {
    paddingLeft: theme.spacing(2),
    paddingRight: theme.spacing(2)
  },
  tabs: {
    flexGrow: 1
  },
  search: {
    color: theme.palette.background.paper,
    backgroundColor: fade(theme.palette.background.paper, 0.15),
    '&:hover': {
      backgroundColor: fade(theme.palette.background.paper, 0.25)
    },
    borderRadius: theme.shape.borderRadius,
    paddingLeft: theme.spacing(1),
    paddingRight: theme.spacing(1),
    marginRight: theme.spacing(1),
    transition: theme.transitions.create('width'),
    width: '200px',
    '&:focus-within': {
      width: '300px'
    },
    fontSize: '14px'
  },
  searchIcon: {
    paddingLeft: theme.spacing(1) / 2,
    paddingRight: theme.spacing(1),
    cursor: 'default'
  },
  searchClear: {
    cursor: 'pointer'
  }
})

function mapStateToProps(state) {
  return {
    currentPage: selectors.getCurrentPage(state),
    searchText: selectors.getSearch(state)
  }
}

function mapDispatchToProps(dispatch, ownProps) {
  return {
    currentPageChange: (event, value) =>
      dispatch(actions.currentPageChange(value)),
    searchChange: value => dispatch(actions.searchChange(value)),
    search: value => dispatch(actions.search(ownProps.page, value)),
    logout: () => dispatch(actions.logout())
  }
}

class Menu extends React.Component {
  constructor(props) {
    super(props)
    this.searchRef = React.createRef()
    this.handleSearchChange = this.handleSearchChange.bind(this)
    this.handleSearchKeyPress = this.handleSearchKeyPress.bind(this)
    this.handleSearchClear = this.handleSearchClear.bind(this)
  }

  handleSearchChange(event) {
    this.props.searchChange(event.target.value)
  }

  handleSearchKeyPress(event) {
    if (event.key === 'Enter') {
      this.props.search(this.props.searchText)
    }
  }

  handleSearchClear(event) {
    event.preventDefault()
    this.props.searchChange('')
    this.searchRef.current.focus()
  }

  render() {
    logger.log(logger.DEBUG, 'Menu.render')

    const { classes, searchText } = this.props

    return (
      <AppBar position='static' classes={{ root: classes.appBar }}>
        <Toolbar variant='dense' classes={{ root: classes.toolBar }}>
          <Tabs
            value={this.props.currentPage}
            onChange={this.props.currentPageChange}
            classes={{ root: classes.tabs }}
          >
            <Tab value={pages.TYPES} label='Types' />
            <Tab value={pages.USERS} label='Users' />
            <Tab value={pages.TOOLS} label='Tools' />
          </Tabs>
          <TextField
            placeholder='Search...'
            value={searchText}
            onChange={this.handleSearchChange}
            onKeyPress={this.handleSearchKeyPress}
            InputProps={{
              inputRef: this.searchRef,
              disableUnderline: true,
              startAdornment: this.renderSearchIcon(),
              endAdornment: this.renderSearchClearIcon(),
              classes: {
                root: classes.search
              }
            }}
          />
          <Button
            label={<LogoutIcon fontSize='small' />}
            type='final'
            onClick={this.props.logout}
          />
        </Toolbar>
      </AppBar>
    )
  }

  renderSearchIcon() {
    const { classes } = this.props
    return (
      <InputAdornment>
        <SearchIcon classes={{ root: classes.searchIcon }} fontSize='small' />
      </InputAdornment>
    )
  }

  renderSearchClearIcon() {
    const { classes, searchText } = this.props
    if (searchText) {
      return (
        <InputAdornment>
          <CloseIcon
            classes={{ root: classes.searchClear }}
            onMouseDown={this.handleSearchClear}
            fontSize='small'
          />
        </InputAdornment>
      )
    } else {
      return <React.Fragment></React.Fragment>
    }
  }
}

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(withStyles(styles)(Menu))
