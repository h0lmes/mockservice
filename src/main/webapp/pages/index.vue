<template>
    <div class="monospace">
        <div class="component-toolbar mb-3">
            <div class="toolbar-item">
                <input ref="search"
                       type="text"
                       class="form-control monospace"
                       placeholder="type in or click on values (group, path, etc)"
                       @keydown.enter.exact.stop="filter(($event.target as HTMLInputElement).value)"/>
            </div>
            <button type="button" class="toolbar-item-w-fixed-auto btn" @click="filter('')">Clear search</button>
            <ToggleSwitch v-model="jsSearch" class="toolbar-item toolbar-item-w-fixed-auto">JS</ToggleSwitch>
        </div>

        <div class="component-toolbar mb-3">
            <ToggleSwitch v-model="showRoutes" class="toolbar-item toolbar-item-w-fixed-auto">Routes</ToggleSwitch>
            <ToggleSwitch v-model="showScenarios" class="toolbar-item toolbar-item-w-fixed-auto">Scenarios</ToggleSwitch>
            <ToggleSwitch v-model="showRequests" class="toolbar-item toolbar-item-w-fixed-auto">Requests</ToggleSwitch>
            <ToggleSwitch v-model="showTests" class="toolbar-item toolbar-item-w-fixed-auto">Tests</ToggleSwitch>
            <ViewSelector class="toolbar-item toolbar-item-w-fixed-auto"></ViewSelector>
            <ToggleSwitch :model-value="selecting" class="toolbar-item toolbar-item-w-fixed-auto" @toggle="setSelect">Select</ToggleSwitch>
        </div>

        <div class="component-toolbar mb-5">
            <button type="button" class="toolbar-item-w-fixed-auto btn" @click="addRouteEntity">Add route</button>
            <button type="button" class="toolbar-item-w-fixed-auto btn" @click="addScenarioEntity">Add scenario</button>
            <button type="button" class="toolbar-item-w-fixed-auto btn" @click="addRequestEntity">Add request</button>
            <button type="button" class="toolbar-item-w-fixed-auto btn" @click="addTestEntity">Add test</button>
            <button v-show="selecting" type="button" class="toolbar-item-w-fixed-auto btn btn-danger mr-3" @click="deleteSelected">Delete selected</button>
        </div>

        <Routes :entities="filteredEntities"></Routes>

        <div class="color-secondary mt-6 text-indent">
            <div class="mt-2 bold">Tips</div>
            <p class="mt-2">
                - Click on any value (GROUP, METHOD, PATH, etc.; you'll see underline on hover) to filter by that value.
            </p>
            <p class="mt-2">
                - For more precise filtering enable 'JS' and use Javascript expressions in search field (example: e.group=='default' && !e.disabled).
            </p>
            <p class="mt-2">
                - Middle-click to edit, press ESC in any field to cancel.
            </p>
            <p class="mt-2">
                - To alter multiple entities quickly consider navigating to Config page and editing it as plain text.
                Use Backup button there for extra caution.
            </p>
        </div>
        <div class="color-secondary mt-6 text-indent">
            <div class="mt-2 bold">Test plan syntax</div>
            <p class="mt-2">- Each line is exactly one command.</p>
            <p class="mt-2">- x = some string - set value of variable 'x' in global context (see Context page).</p>
            <p class="mt-2">- x = ${item.capacity} - set value of variable 'x' in global context, value comes from variable 'item.capacity' from latest request response (assuming latest response was like <span>{"item": {"capacity": 100}}</span>) or from global context.</p>
            <p class="mt-2">- GET localhost:8081/products/${id} - execute inline request; variable comes from global context.</p>
            <p class="mt-2">- POST localhost:8081/v2/store/order {"x": 400} -> 400,404 - execute inline request and test status code is 400 or 404; if test fails - test run stops with FAILED.</p>
            <p class="mt-2">- request_id - execute request with ID 'request_id'.</p>
            <p class="mt-2">- request_id -> 200 - execute request with ID 'request_id' and test status code is 200; if test fails - test run stops with FAILED.</p>
            <p class="mt-2">- x == 2 - non-strictly test variable 'x' from the latest request response; if test fails - test run proceeds with WARNING.</p>
            <p class="mt-2">- x === some value - strictly test variable 'x' from the latest request response; if test fails - test run stops with FAILED.</p>
        </div>
        <div class="color-secondary mt-6 text-indent">
            <div class="mt-2 bold">Scenario types</div>
            <p class="mt-2">- MAP: goes over the LIST OF ROUTES in a scenario top-to-bottom looking for the first match of requested METHOD + PATH pair, returns route with matching ALT. If no match found - uses default behavior (returns route with matching METHOD + PATH and an empty ALT).</p>
            <p class="mt-2">- QUEUE: same as MAP but looks only at the topmost route, removes route from a queue if it matches. If topmost route does not match (or all routes were already matched and thus removed from a queue) - uses default behavior (returns route with matching METHOD + PATH and an empty ALT).</p>
            <p class="mt-2">- RING: same as QUEUE but restarts the queue when it depletes.</p>
        </div>

        <Loading v-if="pageLoading"></Loading>
    </div>
</template>

<script setup lang="ts">
import {computed, ref} from 'vue'
import type {RequestEntity, RouteEntity, ScenarioEntity, TestEntity} from '@/types/models'
import {usePageLoader, useWorkingAction} from '@/composables/useAsyncState'
import {useSyncedSearch} from '@/composables/useSyncedSearch'
import Loading from '../components/other/Loading'
import ToggleSwitch from '../components/other/ToggleSwitch'
import ViewSelector from '../components/other/ViewSelector'
import Routes from '../components/route/Routes'
import {frontendAppState, setApiSearchExpression} from '@/state/app'
import {addRequest, deleteRequests, fetchRequests, selectAllRequests, useRequestsState} from '@/state/requests'
import {addRoute, deleteRoutes, fetchRoutes, selectAllRoutes, useRoutesState} from '@/state/routes'
import {addScenario, deleteScenarios, fetchScenarios, selectAllScenarios, useScenariosState} from '@/state/scenarios'
import {addTest, deleteTests, fetchTests, selectAllTests, useTestsState} from '@/state/tests'
import {_isRequest, _isRoute, _isScenario, _isTest} from '@/js/common'

type ApiEntity = RouteEntity | RequestEntity | ScenarioEntity | TestEntity

const { pageLoading, runWhilePageLoading } = usePageLoader()
const { runWhileWorking } = useWorkingAction()
const showRoutes = ref(true)
const showRequests = ref(true)
const showScenarios = ref(true)
const showTests = ref(true)
const jsSearch = ref(false)
const searchExpression = computed(() => (frontendAppState.apiSearchExpression || '').trim())
const { search, query } = useSyncedSearch(searchExpression)
const routes = computed(() => showRoutes.value ? useRoutesState().state.routes : [])
const requests = computed(() => showRequests.value ? useRequestsState().state.requests : [])
const scenarios = computed(() => showScenarios.value ? useScenariosState().state.scenarios : [])
const tests = computed(() => showTests.value ? useTestsState().state.tests : [])
const entities = computed<ApiEntity[]>(() => [...routes.value, ...requests.value, ...scenarios.value, ...tests.value])

const getSearchFn = () => {
  if (jsSearch.value) {
    return Function('e', 'return ' + query.value + ';') as (entity: ApiEntity) => boolean
  }
  if (!query.value) {
    return () => true
  }

  const nextQuery = query.value
  return (entity: ApiEntity) => {
    if ('method' in entity && 'path' in entity && 'id' in entity) {
      return entity.group.includes(nextQuery) || entity.type.includes(nextQuery) || entity.method.includes(nextQuery) || entity.path.includes(nextQuery) || entity.id.includes(nextQuery)
    }
    if ('method' in entity && 'path' in entity) {
      return entity.group.includes(nextQuery) || entity.type.includes(nextQuery) || entity.method.includes(nextQuery) || entity.path.includes(nextQuery) || entity.alt.includes(nextQuery)
    }
    if ('alias' in entity && 'plan' in entity) {
      return entity.group.includes(nextQuery) || entity.alias.includes(nextQuery)
    }
    return entity.group.includes(nextQuery) || entity.alias.includes(nextQuery) || entity.type.includes(nextQuery)
  }
}

const filteredEntities = computed<ApiEntity[]>(() => {
  if (!query.value) return entities.value

  try {
    return entities.value.filter(getSearchFn())
  } catch (error) {
    console.error(error)
    return []
  }
})

const selecting = computed(() => {
  if (!filteredEntities.value || filteredEntities.value.length === 0) return false
  return filteredEntities.value[0]._selected !== undefined && filteredEntities.value[0]._selected !== null
})

const filter = (value: string) => {
  setApiSearchExpression(value)
}

const loadPage = async () => runWhilePageLoading(async () => {
  await fetchRoutes()
  await fetchRequests()
  await fetchScenarios()
  await fetchTests()
})

const addRouteEntity = () => {
  showRoutes.value = true
  addRoute()
}

const addRequestEntity = () => {
  showRequests.value = true
  addRequest()
}

const addScenarioEntity = () => {
  showScenarios.value = true
  addScenario()
}

const addTestEntity = () => {
  showTests.value = true
  addTest()
}

const setSelect = (value: boolean | null) => {
  if (value) {
    selectAllRoutes(false)
    selectAllRequests(false)
    selectAllScenarios(false)
    selectAllTests(false)
  } else {
    selectAllRoutes(null)
    selectAllRequests(null)
    selectAllScenarios(null)
    selectAllTests(null)
  }
}

const deleteSelected = async () => {
  if (!confirm('Are you sure you want to delete all selected entities?')) {
    return
  }

  await runWhileWorking(async () => {
    const selectedRoutes = filteredEntities.value.filter((entity) => _isRoute(entity) && entity._selected) as RouteEntity[]
    const selectedRequests = filteredEntities.value.filter((entity) => _isRequest(entity) && entity._selected) as RequestEntity[]
    const selectedScenarios = filteredEntities.value.filter((entity) => _isScenario(entity) && entity._selected) as ScenarioEntity[]
    const selectedTests = filteredEntities.value.filter((entity) => _isTest(entity) && entity._selected) as TestEntity[]

    if (selectedRoutes.length > 0) await deleteRoutes(selectedRoutes)
    if (selectedRequests.length > 0) await deleteRequests(selectedRequests)
    if (selectedScenarios.length > 0) await deleteScenarios(selectedScenarios)
    if (selectedTests.length > 0) await deleteTests(selectedTests)
  })
}

await loadPage()
</script>

<style scoped>
.text-indent > p {
    margin-left: 1rem;
    text-indent: -1.1rem;
}
</style>

