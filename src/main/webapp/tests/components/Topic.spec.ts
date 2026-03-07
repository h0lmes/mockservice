import {mount} from '@vue/test-utils'
import {beforeEach, describe, expect, it, vi} from 'vitest'
import type {KafkaTopicEntity} from '@/types/models'
import Topic from '@/components/kafka/Topic.vue'

const mocks = vi.hoisted(() => ({
  appendKafkaItem: vi.fn(),
  deleteKafkaTopics: vi.fn(),
  fetchKafkaRecords: vi.fn(),
  saveKafkaTopics: vi.fn(),
  setKafkaSearchExpression: vi.fn(),
}))

vi.mock('@/composables/useAsyncState', () => ({
  useWorkingAction: () => ({
    runWhileWorking: async <T>(action: () => Promise<T> | T) => await action(),
  }),
}))

vi.mock('@/state/app', () => ({
  setKafkaSearchExpression: mocks.setKafkaSearchExpression,
}))

vi.mock('@/state/kafka', () => ({
  appendKafkaItem: mocks.appendKafkaItem,
  deleteKafkaTopics: mocks.deleteKafkaTopics,
  fetchKafkaRecords: mocks.fetchKafkaRecords,
  saveKafkaTopics: mocks.saveKafkaTopics,
}))

vi.mock('@/components/other/ButtonDelete.vue', () => ({
  default: { emits: ['click'], template: '<button data-test="delete" @click="$emit(\'click\')">delete</button>' },
}))

vi.mock('@/components/other/ButtonEdit.vue', () => ({
  default: { emits: ['click'], template: '<button data-test="edit" @click="$emit(\'click\')">edit</button>' },
}))

vi.mock('@/components/other/ButtonView.vue', () => ({
  default: { emits: ['click'], template: '<button data-test="view" @click="$emit(\'click\')">view</button>' },
}))

vi.mock('@/components/other/Pagination.vue', () => ({
  default: { template: '<div data-test="pagination" />' },
}))

vi.mock('@/components/other/ToggleSwitch.vue', () => ({
  default: {
    props: ['modelValue', 'value'],
    emits: ['toggle', 'update:modelValue'],
    template: '<button data-test="toggle" @click="$emit(\'update:modelValue\', !(modelValue ?? value ?? false)); $emit(\'toggle\', !(modelValue ?? value ?? false))"><slot /></button>',
  },
}))

vi.mock('@/components/other/AutoSizeTextArea.vue', () => ({
  default: {
    props: ['modelValue'],
    emits: ['update:modelValue'],
    methods: { focus() {} },
    template: '<textarea :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
  },
}))

vi.mock('@/components/kafka/TopicRecordProducer.vue', () => ({
  default: { template: '<div data-test="producer" />' },
}))

vi.mock('@/components/kafka/TopicRecords.vue', () => ({
  default: { template: '<div data-test="records" />' },
}))

const topic: KafkaTopicEntity = {
  group: 'orders',
  topic: 'orders.created',
  partition: 2,
  initialData: '',
  producerOffset: 5,
  consumerOffset: 3,
}

describe('Topic', () => {
  beforeEach(() => {
    mocks.appendKafkaItem.mockReset()
    mocks.deleteKafkaTopics.mockReset()
    mocks.fetchKafkaRecords.mockReset()
    mocks.saveKafkaTopics.mockReset()
    mocks.setKafkaSearchExpression.mockReset()
    mocks.fetchKafkaRecords.mockResolvedValue({ records: [], total: 0 })
    mocks.saveKafkaTopics.mockResolvedValue(undefined)
  })

  it('loads records when opening the topic viewer', async () => {
    const wrapper = mount(Topic, { props: { topic } })

    await wrapper.get('[data-test="view"]').trigger('click')

    expect(mocks.fetchKafkaRecords).toHaveBeenCalledWith({
      topic: 'orders.created',
      partition: 2,
      offset: 0,
      limit: 10,
    })
  })

  it('keeps partition numeric when saving edited topic data', async () => {
    const wrapper = mount(Topic, { props: { topic } })

    await wrapper.get('[data-test="edit"]').trigger('click')
    await wrapper.findAll('input.form-control')[2]!.setValue('3')
    await wrapper.findAll('button').find((button) => button.text() === 'SAVE')!.trigger('click')

    expect(mocks.saveKafkaTopics).toHaveBeenCalledWith([
      topic,
      expect.objectContaining({ partition: 3 }),
    ])
  })
})



