<template>
    <div>
        <div class="tester-buttons">
            <button type="button" class="btn btn-sm btn-primary" @click="test">retry</button>
            <ButtonEdit @click="emit('edit')"></ButtonEdit>
            <button type="button" class="btn btn-sm btn-default" @click="emit('close')">close</button>
        </div>
        <div class="tester-result">
            <pre class="form-control form-control-sm monospace">{{ testResult }}</pre>
        </div>
    </div>
</template>

<script setup lang="ts">
import {ref} from 'vue'
import ButtonEdit from '@/components/other/ButtonEdit'
import {executeRequest} from '@/state/requests'
import type {RequestEntity} from '@/types/models'

const props = defineProps<{
  request: RequestEntity
}>()

const emit = defineEmits<{
  edit: []
  close: []
}>()

const testResult = ref('Press retry to execute this saved request.\n')

const println = (text: unknown) => {
  testResult.value += String(text) + '\n'
}

const clearOutput = () => {
  testResult.value = ''
}

const test = async () => {
  testResult.value = ''
  println('working ...')

  try {
    const body = await executeRequest(props.request.id)
    clearOutput()
    println(body)
  } catch (err) {
    println('----------')
    println(err)
  }
}
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
