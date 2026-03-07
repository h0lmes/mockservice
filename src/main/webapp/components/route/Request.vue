<template>
    <div class="component component-row monospace"
         :class="{ open, disabled: request.disabled }"
         @click.middle.stop.prevent="edit"
         @keydown.esc.exact="cancel">

        <div v-show="!open" class="mock-col w-fixed-auto">
            <Icon class="component-row-icon color-accent-one"/>
        </div>

        <div v-show="selecting && !open" class="mock-col w-fixed-auto">
            <ToggleSwitch class="mock-col-value" :value="selected" @toggle="select"></ToggleSwitch>
        </div>

        <div v-show="editing" class="mock-col w1">
            <div class="mock-col-header">TYPE</div>
            <select v-model="editingData.type" class="form-control form-control-sm">
                <option>REST</option>
            </select>
        </div>

        <div v-if="!testing" class="mock-col w2">
            <div class="mock-col-header">GROUP</div>
            <div v-show="!editing" class="mock-col-value link" @click="filter(request.group)">{{ request.group }}</div>
            <input v-show="editing" v-model="editingData.group" type="text" class="form-control form-control-sm"/>
        </div>

        <div v-if="!testing" class="mock-col w1">
            <div class="mock-col-header">METHOD</div>
            <div v-show="!editing" class="mock-col-value link" @click="filter(request.method)">
                <RouteMethod :value="request.method" :disabled="request.disabled"></RouteMethod>
            </div>
            <select v-show="editing" v-model="editingData.method" class="form-control form-control-sm">
                <option>GET</option>
                <option>POST</option>
                <option>PUT</option>
                <option>DELETE</option>
                <option>PATCH</option>
            </select>
        </div>

        <div v-show="!editing && !testing" class="mock-col w3">
            <div class="mock-col-header">URL</div>
            <div class="mock-col-value link" @click="filter(request.path)">{{ request.path }}</div>
        </div>

        <div v-show="editing" class="mock-col w1">
            <div class="mock-col-header">ID</div>
            <input v-model="editingData.id" type="text" class="form-control form-control-sm" placeholder="empty to generate"/>
        </div>
        <div v-show="!editing && !testing" class="mock-col w2">
            <div class="mock-col-header">ID</div>
            <div class="mock-col-value link" @click="filter(request.id)">{{ request.id }}</div>
        </div>

        <div v-if="!testing" class="mock-col w-fixed-auto">
            <div v-show="editing" class="mock-col-header"></div>
            <div class="mock-col-value">
                <ButtonExecute @click="test"></ButtonExecute>
                <ButtonEdit @click="edit"></ButtonEdit>
                <ButtonDelete @click="del"></ButtonDelete>
            </div>
        </div>

        <div v-show="editing" class="mock-col w100">
            <div class="mb-2 color-secondary">URL</div>
            <input v-model="editingData.path" type="text" class="form-control form-control-sm" placeholder="http://server:port/api/v1/path"/>
        </div>

        <div v-show="editing" class="mock-col w100">
            <div class="mb-2 color-secondary">REQUEST HEADERS</div>
            <AutoSizeTextArea v-model="editingData.headers" placeholder="Content-Type: application/json"></AutoSizeTextArea>
        </div>

        <div v-show="editing" class="mock-col w100">
            <div class="mb-2 color-secondary">REQUEST BODY</div>
            <AutoSizeTextArea ref="body" v-model="editingData.body" placeholder='{"id": 1, "name": "admin"}'></AutoSizeTextArea>
        </div>

        <div v-show="editing" class="mock-col w100 mt-1">
            <ToggleSwitch v-model="editingData.responseToVars" class="mock-col-value">SAVE RESPONSE IN GLOBAL VARS</ToggleSwitch>
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
            <RequestTester :request="request" @close="testing = false" @edit="edit"></RequestTester>
        </div>
    </div>
</template>

<script setup lang="ts">
import {computed, nextTick, onMounted, ref} from 'vue'
import Icon from '@/assets/icons/bolt.svg?component'
import ButtonDelete from '@/components/other/ButtonDelete'
import ButtonEdit from '@/components/other/ButtonEdit'
import ButtonExecute from '@/components/other/ButtonExecute'
import AutoSizeTextArea from '../other/AutoSizeTextArea'
import ToggleSwitch from '../other/ToggleSwitch'
import {useWorkingAction} from '@/composables/useAsyncState'
import {setApiSearchExpression} from '@/state/app'
import {deleteRequests, saveRequests, selectRequest} from '@/state/requests'
import type {FocusableField, RequestDraft, RequestEntity} from '@/types/models'
import RequestTester from './RequestTester'
import RouteMethod from './RouteMethod'

const createEmptyRequest = (): RequestDraft => ({
  id: '',
  group: '',
  type: 'REST',
  method: 'GET',
  path: '/',
  headers: '',
  body: '',
  responseToVars: false,
  disabled: false,
  triggerRequest: false,
  triggerRequestIds: '',
  triggerRequestDelay: '',
})

const props = defineProps<{
  request: RequestEntity
}>()

const { runWhileWorking } = useWorkingAction()
const body = ref<FocusableField | null>(null)
const editing = ref(false)
const editingData = ref<RequestDraft>(createEmptyRequest())
const testing = ref(false)
const open = computed(() => editing.value || testing.value)
const selected = computed(() => props.request?._selected)
const selecting = computed(() => selected.value !== undefined && selected.value !== null)

const filter = (value: string) => setApiSearchExpression(value)
const select = (value: boolean) => selectRequest({ request: props.request, selected: value })

const edit = async () => {
  testing.value = false
  editingData.value = { ...createEmptyRequest(), ...props.request }
  if (!editing.value) editing.value = true
  else cancel()
  if (editing.value) {
    await nextTick()
    body.value?.focus()
  }
}

const cancel = () => {
  if (props.request._new) {
    deleteRequests([props.request])
  } else {
    editing.value = false
    editingData.value = createEmptyRequest()
  }
}

const test = () => {
  cancel()
  testing.value = !testing.value
}

const del = () => {
  if (props.request._new) {
    deleteRequests([props.request])
  } else if (confirm('Sure you want to delete?')) {
    void runWhileWorking(() => deleteRequests([props.request]))
  }
}

const save = () => {
  void runWhileWorking(async () => {
    await saveRequests([props.request, editingData.value])
    editing.value = false
  })
}

const saveAsCopy = () => {
  void runWhileWorking(async () => {
    await saveRequests([{}, editingData.value])
    editing.value = false
  })
}

onMounted(() => {
  if (props.request._new) {
    edit()
  }
})
</script>

<style scoped>
</style>


