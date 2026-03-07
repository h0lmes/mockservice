export type ApiKind = 'REST' | 'SOAP'
export type RequestKind = 'REST'
export type HttpMethod = 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE'
export type ScenarioType = 'MAP' | 'QUEUE' | 'RING'
export type KafkaPartition = number
export type KafkaHeaderValue = string | number | boolean | null

export interface SelectableEntity {
  _new?: boolean
  _selected?: boolean | null
}

export interface RouteDraft {
  group: string
  type: ApiKind
  method: HttpMethod
  path: string
  alt: string
  response: string
  responseCode: string
  requestBodySchema: string
  disabled: boolean
  triggerRequest: boolean
  triggerRequestIds: string
  triggerRequestDelay: string
}

export interface RouteEntity extends RouteDraft, SelectableEntity {
  variables?: string[]
}

export interface RequestDraft {
  id: string
  group: string
  type: RequestKind
  method: HttpMethod
  path: string
  headers: string
  body: string
  responseToVars: boolean
  disabled: boolean
  triggerRequest: boolean
  triggerRequestIds: string
  triggerRequestDelay: string
}

export interface RequestEntity extends RequestDraft, SelectableEntity {}

export interface ScenarioDraft {
  id: string
  group: string
  alias: string
  type: ScenarioType
  data: string
  active: boolean
}

export interface ScenarioEntity extends ScenarioDraft, SelectableEntity {}

export interface TestDraft {
  group: string
  alias: string
  plan: string
}

export interface TestEntity extends TestDraft, SelectableEntity {}

export interface KafkaTopicDraft {
  group: string
  topic: string
  partition: KafkaPartition
  initialData: string
}

export interface KafkaTopicEntity extends KafkaTopicDraft, SelectableEntity {
  producerOffset?: number
  consumerOffset?: number
}

export interface KafkaRecordEntity {
  offset: number | string
  timestamp: number | string
  key: string | null
  value: string | null
  headers: string | Record<string, KafkaHeaderValue>
}

export interface KafkaRecordsResponse {
  records: KafkaRecordEntity[]
  total: number
}

export interface ServiceSettings {
  randomAlt?: boolean
  quantum?: boolean
  alt400OnFailedRequestValidation?: boolean
  proxyEnabled?: boolean
  proxyLocation?: string
  certificate?: string | null
  useContextInRouteResponse?: boolean
}

export interface FocusableField {
  focus: () => void
}
