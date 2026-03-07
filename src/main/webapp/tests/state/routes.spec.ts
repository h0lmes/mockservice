import {beforeEach, describe, expect, it} from 'vitest'
import {addRoute, selectAllRoutes, selectRoute, useRoutesState} from '@/state/routes'

describe('routes state', () => {
  beforeEach(() => {
    useRoutesState().state.routes = []
  })

  it('adds a new editable route with default flags', () => {
    addRoute()

    expect(useRoutesState().state.routes).toHaveLength(1)
    expect(useRoutesState().state.routes[0]).toMatchObject({
      method: 'GET',
      path: '/',
      type: 'REST',
      _new: true,
      _selected: null,
    })
  })

  it('updates selection for matching routes only', () => {
    addRoute()
    addRoute()
    useRoutesState().state.routes[1].path = '/a'
    useRoutesState().state.routes[0].path = '/b'

    selectRoute({ route: useRoutesState().state.routes[1], selected: true })

    expect(useRoutesState().state.routes[1]._selected).toBe(true)
    expect(useRoutesState().state.routes[0]._selected).toBeNull()
  })

  it('can toggle selection for all routes', () => {
    addRoute()
    addRoute()

    selectAllRoutes(false)
    expect(useRoutesState().state.routes.every((route) => route._selected === false)).toBe(true)

    selectAllRoutes(null)
    expect(useRoutesState().state.routes.every((route) => route._selected === null)).toBe(true)
  })
})
