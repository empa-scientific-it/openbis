import React from 'react'
import Grid from '@src/js/components/common/grid/Grid.jsx'
import ids from '@src/js/common/consts/ids.js'
import openbis from '@src/js/services/openbis.js'
import logger from '@src/js/common/logger.js'

class Search extends React.Component {
  constructor(props) {
    super(props)

    this.state = {
      loaded: false
    }

    this.load = this.load.bind(this)
  }

  componentDidMount() {
    this.load().then(users => {
      this.setState(() => ({
        users,
        loaded: true
      }))
    })
  }

  load() {
    let query = this.props.objectId

    let criteria = new openbis.PersonSearchCriteria()
    let fo = new openbis.PersonFetchOptions()

    criteria.withOrOperator()
    criteria.withUserId().thatContains(query)
    criteria.withFirstName().thatContains(query)
    criteria.withLastName().thatContains(query)

    return openbis
      .searchPersons(criteria, fo)
      .then(result => {
        return result.objects.map(user => ({
          ...user,
          id: user.userId
        }))
      })
      .catch(error => {
        openbis.catch(error)
      })
  }

  render() {
    logger.log(logger.DEBUG, 'Search.render')

    if (!this.state.loaded) {
      return null
    }

    return (
      <Grid
        id={ids.USERS_GRID_ID}
        columns={[
          {
            field: 'userId'
          },
          {
            field: 'firstName'
          },
          {
            field: 'lastName'
          }
        ]}
        data={this.state.users}
      />
    )
  }
}

export default Search
