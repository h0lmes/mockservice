<template>
    <div>
        <div class="tester-buttons">
            <button type="button" class="btn btn-sm btn-primary" @click="testExecute">run test</button>
            <button type="button" class="btn btn-sm btn-default" @click="testStop">stop</button>
            <button type="button" class="btn btn-sm btn-default" @click="testFetchResult">get log</button>
            <button type="button" class="btn btn-sm btn-danger" @click="testClear">clear log</button>
            <button type="button" class="btn btn-sm btn-default" @click="copyToClipboard()">
                copy log
                <span v-show="copied">&#9989;</span>
            </button>
            <ButtonEdit @click="emit('edit')"></ButtonEdit>
            <button type="button" class="btn btn-sm btn-default" @click="emit('close')">close</button>
        </div>
        <div class="tester-result">
            <pre class="form-control form-control-sm monospace" v-html="resultProcessed"></pre>
        </div>

        <Loading v-if="loading"></Loading>
    </div>
</template>

<script setup lang="ts">
import {computed, onBeforeUnmount, onMounted, ref} from 'vue'
import ButtonEdit from '@/components/other/ButtonEdit'
import Loading from '@/components/other/Loading'
import copy from '../../js/clipboard'
import {frontendAppState} from '@/state/app'
import {clearTest, executeTest, fetchTestResult, stopTest} from '@/state/tests'
import type {TestEntity} from '@/types/models'

const props = defineProps<{
  test: TestEntity
}>()

const emit = defineEmits<{
  edit: []
  close: []
}>()

const loading = ref(false)
const result = ref('...')
const copied = ref(false)
const ws = ref<WebSocket | null>(null)
const wsUrl = computed(() => frontendAppState.wsUrl)
const resultProcessed = computed(() => result.value
  .replaceAll('SUCCESS', '<span class="green">SUCCESS</span>')
  .replaceAll('WARNING', '<span class="orange-yellow">WARNING</span>')
  .replaceAll('FAILED', '<span class="red">FAILED</span>'))

const println = (text: unknown) => {
  result.value += String(text ?? '') + '\n'
  copied.value = false
}

const clearOutput = () => {
  result.value = ''
  copied.value = false
}

const onWsMessage = (event: MessageEvent<string>) => {
  const data = JSON.parse(event.data)
  if (data != null && data.event === 'TEST_RESULT' && data.id === props.test.alias) {
    clearOutput()
    println(data.data)
  }
}

const onWsOpen = () => {
  console.log('WebSocket::test_run::open')
}

const onWsClose = () => {
  console.log('WebSocket::test_run::closed')
  ws.value = null
}

const onWsError = (event: Event) => {
  console.error('WebSocket::test_run::error: ', event)
}

const startWebSocket = () => {
  if (ws.value != null || !wsUrl.value) {
    return
  }

  ws.value = new WebSocket(wsUrl.value)
  ws.value.onmessage = onWsMessage
  ws.value.onopen = onWsOpen
  ws.value.onclose = onWsClose
  ws.value.onerror = onWsError
}

const testFetchResult = async () => {
  loading.value = true
  fetchTestResult(props.test.alias)
    .then((nextResult) => {
      clearOutput()
      println(nextResult)
    })
    .catch((error) => {
      println('----------')
      println(error)
    })
    .finally(() => {
      loading.value = false
    })
}

const testExecute = async () => {
  if (ws.value == null) startWebSocket()
  try {
    await executeTest(props.test.alias)
  } catch (err) {
    println('----------')
    println(err)
  }
}

const testStop = async () => {
  try {
    await stopTest(props.test.alias)
  } catch (err) {
    println('----------')
    println(err)
  }
}

const testClear = async () => {
  try {
    await clearTest(props.test.alias)
    await testFetchResult()
  } catch (err) {
    println('----------')
    println(err)
  }
}

const copyToClipboard = () => {
  copy(result.value).then(
    () => copied.value = true
  ).catch(
    console.error
  )
}

onMounted(() => {
  testFetchResult()
  startWebSocket()
})

onBeforeUnmount(() => {
  if (ws.value == null) return
  ws.value.close()
  ws.value = null
})
</script>

<style lang="scss" scoped>
.tester-result {
    padding: 0;
    text-align: initial;
}
.tester-buttons {
    padding: 0.3rem 0 0.7rem;
    text-align: end;
}
</style>
