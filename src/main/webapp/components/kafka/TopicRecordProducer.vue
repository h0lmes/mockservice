<template>
    <div class="holder">
        <div class="item">
            <div class="mb-2 color-secondary">KEY</div>
            <input v-model="recordKey" type="text" class="form-control form-control-sm"/>
        </div>
        <div class="item">
            <div class="mb-2 color-secondary">VALUE</div>
            <input v-model="recordValue" type="text" class="form-control form-control-sm"/>
        </div>
        <div class="item">
            <div class="mb-2 color-secondary">HEADERS</div>
            <input v-model="recordHeaders" type="text" class="form-control form-control-sm"/>
        </div>

        <div class="item item-fixed">
            <div class="mb-2 color-secondary">&nbsp;</div>
            <button type="button" class="btn btn-sm btn-default" @click="add">add record</button>
        </div>
    </div>
</template>

<script setup lang="ts">
import {ref} from 'vue'
import {useWorkingAction} from '@/composables/useAsyncState'
import {setLastError} from '@/state/app'
import {produceKafkaRecords} from '@/state/kafka'

const props = defineProps<{
  topic: string
  partition: number
}>()

const emit = defineEmits<{
  added: []
}>()

const { runWhileWorking } = useWorkingAction()
const recordKey = ref('')
const recordValue = ref('')
const recordHeaders = ref('')

const add = async () => {
  let record = null
  try {
    record = {
      topic: props.topic,
      partition: props.partition,
      timestamp: 0,
      key: recordKey.value === '' ? null : recordKey.value,
      value: recordValue.value === '' ? null : recordValue.value,
      headers: recordHeaders.value === '' ? {} : JSON.parse(recordHeaders.value),
    }
  } catch (error) {
    const message = error instanceof Error ? error.message : String(error)
    console.error('Error building record: ', error)
    setLastError('Error building record: ' + message)
    return
  }

  await runWhileWorking(async () => {
    await produceKafkaRecords([record])
    emit('added')
  })
}
</script>

<style scoped>
.holder {
    display: flex;
    gap: 0.5rem 0.5rem;
    flex-wrap: wrap;
    justify-content: center;
    align-content: center;
    align-items: center;
    margin: 0;
    border: none;
}
.item {
    flex: 1 1 0;
    position: relative;
    box-sizing: border-box;
    min-width: 7rem;
}
.item-fixed {
    flex: 0 0 auto;
    min-width: 1rem;
}
</style>
