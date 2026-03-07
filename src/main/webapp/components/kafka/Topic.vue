<template>
    <div class="component component-row monospace"
         :class="{ open }"
         @click.middle.stop.prevent="edit"
         @keydown.esc.exact="cancel">

        <div v-show="!viewing" class="mock-col w2">
            <div class="mock-col-header">GROUP</div>
            <div v-show="!editing" class="mock-col-value link" @click="filter(topic.group)">{{ topic.group }}</div>
            <input v-show="editing" v-model="editingData.group" type="text" class="form-control form-control-sm"/>
        </div>

        <div v-show="!viewing" class="mock-col w3">
            <div class="mock-col-header">TOPIC</div>
            <div v-show="!editing" class="mock-col-value link" @click="filter(topic.topic)">{{ topic.topic }}</div>
            <input ref="topicField" v-show="editing" v-model="editingData.topic" type="text" class="form-control form-control-sm"/>
        </div>

        <div v-show="!viewing" class="mock-col w1">
            <div class="mock-col-header">PARTITION</div>
            <div v-show="!editing" class="mock-col-value">{{ topic.partition }}</div>
            <input v-show="editing" v-model.number="editingData.partition" type="text" class="form-control form-control-sm"/>
        </div>

        <div v-show="!editing && !viewing" class="mock-col w2">
            <div class="mock-col-header">OFFSETS</div>
            <div class="mock-col-value">{{ topic.producerOffset }}/{{ topic.consumerOffset }}</div>
        </div>

        <div v-show="!viewing" class="mock-col w-fixed-auto">
            <div v-show="editing" class="mock-col-header"></div>
            <div class="mock-col-value">
                <ButtonView @click="view"></ButtonView>
                <ButtonEdit @click="edit"></ButtonEdit>
                <ButtonDelete @click="del"></ButtonDelete>
            </div>
        </div>

        <div v-show="editing" class="mock-col w100 mt-2 mb-2">
            <div class="color-secondary mb-2">
                INITIAL DATA
                <button type="button" class="btn btn-sm ml-2" @click="addRecordToInitialData">add record</button>
            </div>
            <AutoSizeTextArea v-model="editingData.initialData"></AutoSizeTextArea>
        </div>

        <div v-show="viewing" class="mock-col w100">
            <TopicRecordProducer :topic="topic.topic" :partition="numericPartition" @added="recordAdded" />
        </div>

        <div v-show="viewing" class="mock-col w100 mt-3">
            <div class="mb-2 color-secondary">RECORDS IN TOPIC</div>
            <TopicRecords :entities="topicRecords" :show-timestamp="showTimestamp" :show-headers="showHeaders" />
        </div>
        <div v-show="viewing" class="mock-col w1">
            <ToggleSwitch v-model="showHeaders" class="mock-col-value">SHOW HEADERS</ToggleSwitch>
        </div>
        <div v-show="viewing" class="mock-col w-fixed-auto">
            <Pagination :limit="limit" :offset="offset" :total="total" @page="page" />
        </div>

        <div v-show="viewing" class="mock-col w100 text-right mt-4">
            <ButtonEdit @click="edit"></ButtonEdit>
            <button type="button" class="btn btn-sm btn-primary" @click="view">close</button>
        </div>

        <div v-show="editing" class="mock-col w-fixed-auto">
            <button type="button" class="btn btn-sm btn-primary" @click="save">SAVE</button>
            <button type="button" class="btn btn-sm btn-default" @click="saveAsCopy">SAVE AS COPY</button>
            <button type="button" class="btn btn-sm btn-default" @click="cancel">CANCEL</button>
        </div>
    </div>
</template>

<script setup lang="ts">
import {computed, nextTick, onMounted, ref} from 'vue'
import {useWorkingAction} from '@/composables/useAsyncState'
import ButtonDelete from '@/components/other/ButtonDelete.vue'
import ButtonEdit from '@/components/other/ButtonEdit.vue'
import ButtonView from '@/components/other/ButtonView.vue'
import Pagination from '@/components/other/Pagination.vue'
import AutoSizeTextArea from '@/components/other/AutoSizeTextArea.vue'
import ToggleSwitch from '@/components/other/ToggleSwitch.vue'
import {setKafkaSearchExpression} from '@/state/app'
import {appendKafkaItem, deleteKafkaTopics, fetchKafkaRecords, saveKafkaTopics} from '@/state/kafka'
import type {KafkaRecordEntity, KafkaTopicDraft, KafkaTopicEntity} from '@/types/models'
import TopicRecordProducer from '@/components/kafka/TopicRecordProducer.vue'
import TopicRecords from '@/components/kafka/TopicRecords.vue'

const createEmptyTopic = (): KafkaTopicDraft => ({
  group: '',
  topic: 'new topic',
  partition: 0,
  initialData: '',
})

const props = defineProps<{
  topic: KafkaTopicEntity
}>()

const { runWhileWorking } = useWorkingAction()
const topicField = ref<HTMLInputElement | null>(null)
const editing = ref(false)
const editingData = ref<KafkaTopicDraft>(createEmptyTopic())
const viewing = ref(false)
const topicRecords = ref<KafkaRecordEntity[]>([])
const offset = ref(0)
const limit = ref(10)
const total = ref(0)
const showTimestamp = ref(false)
const showHeaders = ref(false)
const open = computed(() => editing.value || viewing.value)
const numericPartition = computed(() => Number(props.topic.partition))

const filter = (value: string) => setKafkaSearchExpression(value)

const loadRecords = async () => runWhileWorking(async () => {
  const response = await fetchKafkaRecords({
    topic: props.topic.topic,
    partition: props.topic.partition,
    offset: offset.value,
    limit: limit.value,
  })
  topicRecords.value = response?.records ?? []
  total.value = response?.total ?? 0
})

const page = async (value: number) => {
  offset.value = value * limit.value
  if (viewing.value) await loadRecords()
}

const view = async () => {
  cancel()
  viewing.value = !viewing.value
  if (viewing.value) await loadRecords()
}

const recordAdded = async () => {
  if (viewing.value) await loadRecords()
}

const addRecordToInitialData = async () => runWhileWorking(async () => {
  const response = await appendKafkaItem({
    text: editingData.value.initialData || '',
    topic: props.topic.topic,
    partition: props.topic.partition,
  })
  editingData.value.initialData = response ?? editingData.value.initialData
})

const edit = async () => {
  viewing.value = false
  editingData.value = { initialData: '', ...props.topic, partition: Number(props.topic.partition) }
  if (!editing.value) editing.value = true
  else cancel()
  if (editing.value) {
    await nextTick()
    topicField.value?.focus()
  }
}

const cancel = () => {
  if (props.topic._new) {
    deleteKafkaTopics([props.topic])
  } else {
    editing.value = false
    editingData.value = createEmptyTopic()
  }
}

const del = () => {
  if (props.topic._new) {
    deleteKafkaTopics([props.topic])
  } else if (confirm('Sure you want to delete?')) {
    void runWhileWorking(() => deleteKafkaTopics([props.topic]))
  }
}

const save = () => {
  void runWhileWorking(async () => {
    await saveKafkaTopics([props.topic, editingData.value])
    editing.value = false
  })
}

const saveAsCopy = () => {
  void runWhileWorking(async () => {
    await saveKafkaTopics([{}, editingData.value])
    editing.value = false
  })
}

onMounted(() => {
  if (props.topic._new) {
    edit()
  }
})
</script>

<style scoped>
</style>



