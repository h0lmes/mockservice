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

        <div v-if="!testing" class="mock-col w2">
            <div class="mock-col-header">GROUP</div>
            <div v-show="!editing" class="mock-col-value link" @click="filter(test.group)">{{ test.group }}</div>
            <input v-show="editing" v-model="editingTest.group" type="text" class="form-control form-control-sm"/>
        </div>

        <div v-show="!editing && !testing" class="mock-col w1">
            <div class="mock-col-header">TYPE</div>
            <div v-show="!editing" class="mock-col-value">TEST</div>
        </div>

        <div v-if="!testing" class="mock-col w3">
            <div class="mock-col-header">ALIAS</div>
            <div v-show="!editing" class="mock-col-value link" @click="filter(test.alias)">{{ test.alias }}</div>
            <input v-show="editing" v-model="editingTest.alias" type="text" class="form-control form-control-sm"/>
        </div>

        <div v-show="!editing && !testing" class="mock-col w2">
            <div class="mock-col-header"></div>
            <div class="mock-col-value"></div>
        </div>

        <div v-if="!testing" class="mock-col w-fixed-auto">
            <div v-show="editing" class="mock-col-header"></div>
            <div class="mock-col-value">
                <ButtonExecute @click="testRun"></ButtonExecute>
                <ButtonEdit @click="edit"></ButtonEdit>
                <ButtonDelete @click="del"></ButtonDelete>
            </div>
        </div>

        <div v-show="editing" class="mock-col w100 mt-2">
            <div class="mb-2 color-secondary">
                TEST PLAN
            </div>
            <AutoSizeTextArea ref="data" v-model="editingTest.plan" placeholder="See test plan syntax at the bottom of page"></AutoSizeTextArea>
        </div>

        <div v-show="editing" class="mock-col w1 mt-1"></div>

        <div v-show="editing" class="mock-col w-fixed-auto">
            <button type="button" class="btn btn-sm btn-primary" @click="save">SAVE</button>
            <button type="button" class="btn btn-sm btn-default" @click="saveAsCopy">SAVE AS COPY</button>
            <button type="button" class="btn btn-sm btn-default" @click="cancel">CANCEL</button>
        </div>

        <div v-if="testing" class="mock-col w100">
            <TestRun :test="test" @close="testing = false" @edit="edit"></TestRun>
        </div>
    </div>
</template>

<script setup lang="ts">
import {computed, nextTick, onMounted, ref} from 'vue'
import Icon from '@/assets/icons/question.svg?component'
import ButtonDelete from '@/components/other/ButtonDelete'
import ButtonEdit from '@/components/other/ButtonEdit'
import ButtonExecute from '@/components/other/ButtonExecute'
import AutoSizeTextArea from '../other/AutoSizeTextArea'
import ToggleSwitch from '../other/ToggleSwitch'
import {useWorkingAction} from '@/composables/useAsyncState'
import {setApiSearchExpression} from '@/state/app'
import {deleteTests, saveTests, selectTest} from '@/state/tests'
import type {FocusableField, TestDraft, TestEntity} from '@/types/models'
import TestRun from '@/components/route/TestRun'

const createEmptyTest = (): TestDraft => ({
  group: '',
  alias: 'New Test',
  plan: '',
})

const props = defineProps<{
  test: TestEntity
}>()

const { runWhileWorking } = useWorkingAction()
const data = ref<FocusableField | null>(null)
const editing = ref(false)
const editingTest = ref<TestDraft>(createEmptyTest())
const testing = ref(false)
const open = computed(() => editing.value || testing.value)
const selected = computed(() => props.test?._selected)
const selecting = computed(() => selected.value !== undefined && selected.value !== null)

const filter = (value: string) => setApiSearchExpression(value)
const select = (value: boolean) => selectTest({ test: props.test, selected: value })

const edit = async () => {
  testing.value = false
  editingTest.value = { ...createEmptyTest(), ...props.test }
  if (!editing.value) editing.value = true
  else cancel()
  if (editing.value) {
    await nextTick()
    data.value?.focus()
  }
}

const cancel = () => {
  if (props.test._new) {
    deleteTests([props.test])
  } else {
    editing.value = false
    editingTest.value = createEmptyTest()
  }
}

const del = () => {
  if (props.test._new) {
    deleteTests([props.test])
    return
  }
  if (confirm('Sure you want to delete?')) {
    void runWhileWorking(() => deleteTests([props.test]))
  }
}

const save = () => {
  void runWhileWorking(async () => {
    await saveTests([props.test, editingTest.value])
    editing.value = false
  })
}

const saveAsCopy = () => {
  void runWhileWorking(async () => {
    await saveTests([{}, editingTest.value])
    editing.value = false
  })
}

const testRun = () => {
  cancel()
  testing.value = !testing.value
}

onMounted(() => {
  if (props.test._new) {
    edit()
  }
})
</script>

<style scoped>
</style>


