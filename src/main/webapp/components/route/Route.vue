<template>
    <div class="component component-row monospace"
         :class="{ open, disabled: route.disabled }"
         @click.middle.stop.prevent="edit"
         @keydown.esc.exact="cancel">

        <div v-show="!open" class="mock-col w-fixed-auto">
            <Icon class="component-row-icon grey"/>
        </div>

        <div v-show="selecting && !open" class="mock-col w-fixed-auto">
            <ToggleSwitch class="mock-col-value" :value="selected" @toggle="select"></ToggleSwitch>
        </div>

        <div v-show="editing" class="mock-col w1">
            <div class="mock-col-header">TYPE</div>
            <select v-model="editingData.type" class="form-control form-control-sm">
                <option>REST</option>
                <option>SOAP</option>
            </select>
        </div>

        <div v-show="!testing" class="mock-col w2">
            <div class="mock-col-header">GROUP</div>
            <div v-show="!editing" class="mock-col-value link" @click="filter(route.group)">{{ route.group }}</div>
            <input v-show="editing" v-model="editingData.group" type="text" class="form-control form-control-sm"/>
        </div>

        <div v-show="!testing" class="mock-col w1">
            <div class="mock-col-header">METHOD</div>
            <div v-show="!editing" class="mock-col-value link" @click="filter(route.method)">
                <RouteMethod :value="route.method" :disabled="route.disabled"></RouteMethod>
            </div>
            <select v-show="editing" v-model="editingData.method" class="form-control form-control-sm">
                <option>GET</option>
                <option>POST</option>
                <option>PUT</option>
                <option>PATCH</option>
                <option>DELETE</option>
            </select>
        </div>

        <div v-show="!editing && !testing" class="mock-col w3">
            <div class="mock-col-header">PATH</div>
            <div class="mock-col-value link" @click="filter(route.path)">{{ route.path }}</div>
        </div>

        <div v-show="!editing && !testing" class="mock-col w2">
            <div class="mock-col-header">ALT</div>
            <div class="mock-col-value link" @click="filter(route.alt)">{{ route.alt }}</div>
        </div>

        <div v-show="!testing" class="mock-col w-fixed-auto">
            <div v-show="editing" class="mock-col-header"></div>
            <div class="mock-col-value">
                <ButtonExecute @click="test"></ButtonExecute>
                <ButtonEdit @click="edit"></ButtonEdit>
                <ButtonDelete @click="del"></ButtonDelete>
            </div>
        </div>

        <div v-show="editing" class="mock-col w100">
            <div class="mb-2 color-secondary">PATH</div>
            <input v-model="editingData.path" type="text" class="form-control form-control-sm" placeholder="/api/v1/etc"/>
        </div>

        <div v-show="editing" class="mock-group w100">
            <div class="mock-col w1">
                <div class="mb-2 color-secondary">ALT</div>
                <input v-model="editingData.alt" type="text" class="form-control form-control-sm" placeholder="can contain condition like: id = 1"/>
            </div>
            <div class="mock-col w1">
                <div class="mb-2 color-secondary">RESPONSE CODE</div>
                <input v-model="editingData.responseCode" type="text" class="form-control form-control-sm" placeholder="200"/>
            </div>
        </div>

        <div v-show="editing" class="mock-col w100">
            <div class="mb-2 color-secondary">RESPONSE BODY</div>
            <AutoSizeTextArea ref="response" v-model="editingData.response" placeholder='{"id": 1, "name": "admin"}'></AutoSizeTextArea>
        </div>

        <div v-show="editing" class="mock-col w100 mt-1">
            <ToggleSwitch v-model="showRequestBodySchema" class="mock-col-value">SHOW REQUEST BODY SCHEMA</ToggleSwitch>
        </div>
        <div v-show="editing && showRequestBodySchema" class="mock-col w100">
            <AutoSizeTextArea v-model="editingData.requestBodySchema" :min-rows="1" :max-rows="40" placeholder="REQUEST BODY VALIDATION SCHEMA (JSON)"></AutoSizeTextArea>
        </div>

        <div v-show="editing" class="mock-group w100">
            <div v-show="editing" class="mock-col w100">
                <ToggleSwitch v-model="editingData.triggerRequest" class="mock-col-value">TRIGGER REQUESTS</ToggleSwitch>
            </div>
            <div v-show="editing && editingData.triggerRequest" class="mock-col w2">
                <input v-model="editingData.triggerRequestIds" type="text" class="form-control form-control-sm" placeholder="Comma separated request IDs to trigger after this one"/>
            </div>
            <div v-show="editing && editingData.triggerRequest">after (millis)</div>
            <div v-show="editing && editingData.triggerRequest" class="mock-col w1">
                <input v-model="editingData.triggerRequestDelay" type="text" class="form-control form-control-sm" placeholder="Comma separated delays (default is 100)"/>
            </div>
        </div>

        <div v-show="editing" class="mock-col w1">
            <ToggleSwitch v-model="editingData.disabled" class="mock-col-value">DISABLED</ToggleSwitch>
        </div>

        <div v-show="editing" class="mock-col w-fixed-auto">
            <button type="button" class="btn btn-sm btn-primary" @click="save">SAVE</button>
            <button type="button" class="btn btn-sm btn-default" @click="saveAsCopy">SAVE AS COPY</button>
            <button type="button" class="btn btn-sm btn-default" @click="cancel">CANCEL</button>
        </div>

        <div v-if="testing" class="mock-col w100">
            <RouteTester :route="route" @close="testing = false" @edit="edit"></RouteTester>
        </div>
    </div>
</template>

<script setup lang="ts">
import {computed, nextTick, onMounted, ref} from 'vue'
import Icon from '@/assets/icons/star.svg?component'
import ButtonDelete from '@/components/other/ButtonDelete.vue'
import ButtonEdit from '@/components/other/ButtonEdit.vue'
import ButtonExecute from '@/components/other/ButtonExecute.vue'
import AutoSizeTextArea from '@/components/other/AutoSizeTextArea.vue'
import ToggleSwitch from '@/components/other/ToggleSwitch.vue'
import {useWorkingAction} from '@/composables/useAsyncState'
import {setApiSearchExpression} from '@/state/app'
import {deleteRoutes, saveRoutes, selectRoute} from '@/state/routes'
import type {FocusableField, RouteDraft, RouteEntity} from '@/types/models'
import RouteMethod from '@/components/route/RouteMethod.vue'
import RouteTester from '@/components/route/RouteTester.vue'

const createEmptyRoute = (): RouteDraft => ({
  group: '',
  type: 'REST',
  method: 'GET',
  path: '/',
  alt: '',
  response: '',
  responseCode: '200',
  requestBodySchema: '',
  disabled: false,
  triggerRequest: false,
  triggerRequestIds: '',
  triggerRequestDelay: '',
})

const props = defineProps<{
  route: RouteEntity
}>()

const { runWhileWorking } = useWorkingAction()
const response = ref<FocusableField | null>(null)
const editing = ref(false)
const editingData = ref<RouteDraft>(createEmptyRoute())
const testing = ref(false)
const showRequestBodySchema = ref(false)
const open = computed(() => editing.value || testing.value)
const selected = computed(() => props.route?._selected)
const selecting = computed(() => selected.value !== undefined && selected.value !== null)

const filter = (value: string) => setApiSearchExpression(value)
const select = (value: boolean) => selectRoute({ route: props.route, selected: value })

const edit = async () => {
  testing.value = false
  editingData.value = { ...createEmptyRoute(), ...props.route }
  showRequestBodySchema.value = !!editingData.value.requestBodySchema
  if (!editing.value) editing.value = true
  else cancel()
  if (editing.value) {
    await nextTick()
    response.value?.focus()
  }
}

const cancel = () => {
  if (props.route._new) {
    deleteRoutes([props.route])
  } else {
    editing.value = false
    editingData.value = createEmptyRoute()
  }
}

const test = () => {
  cancel()
  testing.value = !testing.value
}

const del = () => {
  if (props.route._new) {
    deleteRoutes([props.route])
  } else if (confirm('Sure you want to delete?')) {
    void runWhileWorking(() => deleteRoutes([props.route]))
  }
}

const save = () => {
  void runWhileWorking(async () => {
    await saveRoutes([props.route, editingData.value])
    editing.value = false
  })
}

const saveAsCopy = () => {
  void runWhileWorking(async () => {
    await saveRoutes([{}, editingData.value])
    editing.value = false
  })
}

onMounted(() => {
  if (props.route._new) {
    edit()
  }
})
</script>

<style scoped>
</style>




