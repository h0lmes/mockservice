<template>
    <div class="component component-row" :class="{ open }" @click="toggle">
        <div class="mock-col w-fixed-auto">
            <button type="button" class="btn btn-sm btn-default" @click.stop="emit('add', route)">{{ addLabel }}</button>
        </div>
        <div class="mock-col">
            <div class="mock-col-value" :class="{ 'color-accent-one': exists }">{{ existsLabel }}</div>
        </div>
        <div class="mock-col">
            <div class="mock-col-value">{{ route.group }}</div>
        </div>
        <div class="mock-col">
            <div class="mock-col-value">
                <RouteMethod :value="route.method">{{ route.method }}</RouteMethod>
            </div>
        </div>
        <div class="mock-col w3">
            <div class="mock-col-value">{{ route.path }}</div>
        </div>
        <div class="mock-col">
            <div class="mock-col-value">{{ route.alt }}</div>
        </div>
        <div class="mock-col w2">
            <div class="mock-col-value link" :class="{ 'color-accent-one': more }">{{ moreLabel }}</div>
        </div>
        <div v-show="open" class="mock-col w100">
            RESPONSE (GENERATED)
        </div>
        <div v-show="open" class="mock-col w100" @click.stop>
            <AutoSizeTextArea v-model="route.response" placeholder="NO RESPONSE"></AutoSizeTextArea>
        </div>
        <div v-show="open" class="mock-col w100">
            REQUEST BODY SCHEMA
        </div>
        <div v-show="open" class="mock-col w100" @click.stop>
            <AutoSizeTextArea v-model="route.requestBodySchema" placeholder="NO REQUEST BODY SCHEMA"></AutoSizeTextArea>
        </div>
    </div>
</template>

<script setup lang="ts">
import {computed, ref} from 'vue'
import type {RouteEntity} from '@/types/models'
import AutoSizeTextArea from './AutoSizeTextArea'
import RouteMethod from '../route/RouteMethod'

const props = defineProps<{
  route: RouteEntity
  existingRoutes: RouteEntity[]
}>()

const emit = defineEmits<{
  add: [route: RouteEntity]
}>()

const open = ref(false)
const more = computed(() => !!props.route.response || !!props.route.requestBodySchema)
const moreLabel = computed(() => {
  let label = ''
  if (props.route.response) label += 'has response'
  if (props.route.requestBodySchema) label += (label ? ', ' : '') + 'has request body schema'
  return label || '-'
})
const exists = computed(() => props.existingRoutes.some((entity) => entity.method === props.route.method && entity.path === props.route.path && entity.alt === props.route.alt))
const existsLabel = computed(() => exists.value ? 'exists' : '-')
const addLabel = computed(() => exists.value ? 'overwrite route' : '\u00A0\u00A0 add route \u00A0\u00A0')

const toggle = () => {
  open.value = !open.value
}
</script>

<style lang="scss" scoped>
</style>
