<template>
    <div>
        <div class="route-tester-buttons">
            <button type="button" class="btn btn-sm btn-primary" @click="test">retry</button>
            <ButtonEdit @click="emit('edit')"></ButtonEdit>
            <button type="button" class="btn btn-sm btn-default" @click="emit('close')">close</button>
        </div>
        <div class="route-tester-result">
            <pre class="form-control form-control-sm monospace">{{ testResult }}</pre>
        </div>
    </div>
</template>

<script setup lang="ts">
import {computed, ref} from 'vue'
import ButtonEdit from '@/components/other/ButtonEdit.vue'
import {useFrontendApp} from '@/state/app'
import type {RouteEntity} from '@/types/models'

const props = defineProps<{
  route: RouteEntity
}>()

const emit = defineEmits<{
  edit: []
  close: []
}>()

const frontendApp = useFrontendApp()
const testResult = ref('Press retry to execute this route sample.\n')
const host = computed(() => frontendApp.state.baseUrl)
const encodedPath = computed(() => {
  let path = props.route.path
  if (path.startsWith('/')) path = path.substring(1)
  return path.replaceAll('/', '-')
})
const contentType = computed(() => props.route.type === 'SOAP' ? 'text/xml' : 'application/json')
const testHeaders = computed<Record<string, string>>(() => {
  if (props.route.alt) {
    return {
      'Content-Type': contentType.value,
      'Cache-Control': 'no-cache',
      'Mock-Alt': encodedPath.value + '/' + props.route.alt,
    }
  }
  return {
    'Content-Type': contentType.value,
    'Cache-Control': 'no-cache',
  }
})

const println = (text: unknown) => {
  testResult.value += String(text) + '\n'
}

const test = async () => {
  testResult.value = ''
  println(props.route.method.toUpperCase() + ' ' + host.value + props.route.path)
  println(JSON.stringify(testHeaders.value))
  println('fetching ...')

  try {
    const startTime = Date.now()
    const response = await fetch(host.value + props.route.path, {
      method: props.route.method,
      headers: testHeaders.value,
    })
    const body = await response.text()
    const elapsed = Date.now() - startTime
    println('----- response in ' + elapsed + ' ms with status ' + response.status + ' -----')
    println(body)
  } catch (err) {
    println('----------')
    println(err)
  }
}
</script>

<style lang="scss" scoped>
.route-tester-result {
    padding: 0;
    text-align: initial;
}
.route-tester-buttons {
    padding: 0.3rem 0 0.7rem;
    text-align: end;
}
</style>

