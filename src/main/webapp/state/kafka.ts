import {reactive} from 'vue'
import type {KafkaRecordsResponse, KafkaTopicDraft, KafkaTopicEntity} from '@/types/models'
import {apiFetch, apiJson, handleApiError, withApiError} from '@/utils/api'

const createEmptyTopic = (): KafkaTopicEntity => ({
  group: '',
  topic: 'new topic',
  partition: 0,
  initialData: '',
  _new: true,
})

const state = reactive({
  topics: [] as KafkaTopicEntity[]
})

export const fetchKafkaTopics = async () => {
  const data = await withApiError(() => apiJson<KafkaTopicEntity[]>('/__kafka__'))
  if (data) {
    state.topics = data
  }
}

export const saveKafkaTopics = async (topics: [KafkaTopicEntity | Record<string, never>, KafkaTopicDraft]) => {
  const data = await withApiError(() => apiJson<KafkaTopicEntity[]>('/__kafka__', {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(topics)
  }))
  if (data) {
    state.topics = data
  }
}

export const deleteKafkaTopics = async (topics: KafkaTopicEntity[]) => {
  const data = await withApiError(() => apiJson<KafkaTopicEntity[]>('/__kafka__', {
    method: 'DELETE',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(topics)
  }))
  if (data) {
    state.topics = data
  }
}

export const addKafkaTopic = () => {
  state.topics.unshift(createEmptyTopic())
}

export const fetchKafkaRecords = async (search: Record<string, string | number>) => {
  return withApiError(async () => {
    const query = new URLSearchParams(Object.entries(search).map(([key, value]) => [key, String(value)]))
    return apiJson<KafkaRecordsResponse>(`/__kafka__/records?${query.toString()}`)
  })
}

export const produceKafkaRecords = async (data: unknown[]) => {
  return withApiError(async () => {
    const response = await apiFetch('/__kafka__/producer', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    })
    await handleApiError(response)
    return response.text()
  })
}

export const appendKafkaItem = async (payload: { text: string; topic: string; partition: string | number }) => {
  return withApiError(async () => {
    const query = new URLSearchParams({ topic: payload.topic, partition: String(payload.partition) })
    const response = await apiFetch(`/__kafka__/append-item?${query.toString()}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: payload.text
    })
    await handleApiError(response)
    const data = await response.json()
    return JSON.stringify(data, null, 4)
  })
}

export const useKafkaState = () => ({
  state,
  fetchKafkaTopics,
  saveKafkaTopics,
  deleteKafkaTopics,
  addKafkaTopic,
  fetchKafkaRecords,
  produceKafkaRecords,
  appendKafkaItem,
  createEmptyTopic,
})

