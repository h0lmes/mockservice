<template>
    <div class="component component-row monospace"
         :class="{ open }"
         @click.middle.stop.prevent="edit"
         @keydown.esc.exact="cancel">

        <div v-show="!open" class="mock-col w-fixed-auto">
            <Icon class="component-row-icon color-accent-one"/>
        </div>

        <div v-show="selecting && !open" class="mock-col w-fixed-auto">
            <ToggleSwitch class="mock-col-value" :value="selected" @toggle="select"></ToggleSwitch>
        </div>

        <div class="mock-col w2">
            <div class="mock-col-header">GROUP</div>
            <div v-show="!editing" class="mock-col-value link" @click="filter(scenario.group)">{{ scenario.group }}</div>
            <input v-show="editing" v-model="editingScenario.group" type="text" class="form-control form-control-sm"/>
        </div>

        <div class="mock-col w1">
            <div class="mock-col-header">SCENARIO TYPE</div>
            <div v-show="!editing" class="mock-col-value link" @click="filter(scenario.type)">{{ scenario.type }}</div>
            <select v-show="editing" v-model="editingScenario.type" class="form-control form-control-sm">
                <option>MAP</option>
                <option>QUEUE</option>
                <option>RING</option>
            </select>
        </div>

        <div class="mock-col w3">
            <div class="mock-col-header">SCENARIO ALIAS</div>
            <div v-show="!editing" class="mock-col-value link" @click="filter(scenario.alias)">{{ scenario.alias }}</div>
            <input v-show="editing" v-model="editingScenario.alias" type="text" class="form-control form-control-sm"/>
        </div>

        <div class="mock-col w2 text-center">
            <div v-show="editing" class="mock-col-header"></div>
            <ToggleSwitch v-model="activeSwitch" @toggle="activeToggled()">Active</ToggleSwitch>
        </div>

        <div class="mock-col w-fixed-auto">
            <div v-show="editing" class="mock-col-header"></div>
            <div class="mock-col-value">
                <ButtonExecute class="disabled"></ButtonExecute>
                <ButtonEdit @click="edit"></ButtonEdit>
                <ButtonDelete @click="del"></ButtonDelete>
            </div>
        </div>

        <div v-show="editing" class="mock-col w100">
            <div class="mb-2 color-secondary">LIST OF ROUTES</div>
            <AutoSizeTextArea ref="data" v-model="editingScenario.data" placeholder="click SHOW ROUTES to add routes; or just type them in as METHOD;PATH;ALT"></AutoSizeTextArea>
        </div>

        <div v-show="editing" class="mock-col w1 mt-1">
            <ToggleSwitch v-model="showRoutes" class="mock-col-value">SHOW ROUTES</ToggleSwitch>
        </div>
        <div v-show="editing" class="mock-col w-fixed-auto">
            <button type="button" class="btn btn-sm btn-primary" @click="save">SAVE</button>
            <button type="button" class="btn btn-sm btn-default" @click="saveAsCopy">SAVE AS COPY</button>
            <button type="button" class="btn btn-sm btn-default" @click="cancel">CANCEL</button>
        </div>

        <div v-if="editing && showRoutes" class="mock-col w100">
            <RoutesToAdd :routes="routes" @add="add($event)"></RoutesToAdd>
        </div>

    </div>
</template>

<script setup lang="ts">
import {computed, nextTick, onMounted, ref} from 'vue'
import Icon from '@/assets/icons/scroll.svg?component'
import ButtonDelete from '@/components/other/ButtonDelete.vue'
import ButtonEdit from '@/components/other/ButtonEdit.vue'
import ButtonExecute from '@/components/other/ButtonExecute.vue'
import AutoSizeTextArea from '@/components/other/AutoSizeTextArea.vue'
import ToggleSwitch from '@/components/other/ToggleSwitch.vue'
import {useWorkingAction} from '@/composables/useAsyncState'
import {setApiSearchExpression} from '@/state/app'
import {useRoutesState} from '@/state/routes'
import {activateScenario, deactivateScenario, deleteScenarios, saveScenarios, selectScenario} from '@/state/scenarios'
import type {FocusableField, RouteEntity, ScenarioDraft, ScenarioEntity} from '@/types/models'
import RoutesToAdd from '@/components/route/RoutesToAdd.vue'

const createEmptyScenario = (): ScenarioDraft => ({
  id: '',
  group: '',
  alias: 'New Scenario',
  type: 'MAP',
  data: '',
  active: false,
})

const props = defineProps<{
  scenario: ScenarioEntity
}>()

const { runWhileWorking } = useWorkingAction()
const routesState = useRoutesState()
const data = ref<FocusableField | null>(null)
const editing = ref(false)
const editingScenario = ref<ScenarioDraft>(createEmptyScenario())
const activeSwitch = ref(false)
const showRoutes = ref(false)
const testing = ref(false)
const routes = computed(() => routesState.state.routes)
const open = computed(() => editing.value || testing.value)
const active = computed(() => !!props.scenario.active)
const selected = computed(() => props.scenario?._selected)
const selecting = computed(() => selected.value !== undefined && selected.value !== null)

const filter = (value: string) => setApiSearchExpression(value)
const select = (value: boolean) => selectScenario({ scenario: props.scenario, selected: value })

const activate = () => {
  void runWhileWorking(async () => {
    await activateScenario(props.scenario.alias)
    activeSwitch.value = active.value
  })
}

const deactivate = () => {
  void runWhileWorking(async () => {
    await deactivateScenario(props.scenario.alias)
    activeSwitch.value = active.value
  })
}

const activeToggled = () => {
  if (activeSwitch.value) activate()
  else deactivate()
}

const edit = async () => {
  editingScenario.value = { ...createEmptyScenario(), ...props.scenario }
  if (!editing.value) editing.value = true
  else cancel()
  if (editing.value) {
    await nextTick()
    data.value?.focus()
  }
}

const cancel = () => {
  if (props.scenario._new) {
    deleteScenarios([props.scenario])
  } else {
    editing.value = false
    editingScenario.value = createEmptyScenario()
  }
}

const del = () => {
  if (props.scenario._new) {
    deleteScenarios([props.scenario])
    return
  }
  if (confirm('Sure you want to delete?')) {
    void runWhileWorking(() => deleteScenarios([props.scenario]))
  }
}

const save = () => {
  void runWhileWorking(async () => {
    await saveScenarios([props.scenario, editingScenario.value])
    editing.value = false
  })
}

const saveAsCopy = () => {
  editingScenario.value.id = ''
  void runWhileWorking(async () => {
    await saveScenarios([{}, editingScenario.value])
    editing.value = false
  })
}

const add = (route: RouteEntity) => {
  editingScenario.value.data = editingScenario.value.data || ''
  if (editingScenario.value.data) editingScenario.value.data += '\n'
  editingScenario.value.data += route.method + ';' + route.path + ';' + route.alt
  data.value?.focus()
}

onMounted(() => {
  activeSwitch.value = active.value
  if (props.scenario._new) {
    edit()
  }
})
</script>

<style scoped>
</style>




