<template>
    <div style="width: 100%">
        <p v-if="entities.length === 0">
            Nothing here ¯\_(ツ)_/¯
        </p>
        <div v-for="(entity, index) in sortedEntities" :key="entityKey(entity)">
            <div v-if="groupStart(sortedEntities, entity, index)" class="component-row-group-boundary"></div>

            <Route v-if="isRoute(entity)" :route="entity"></Route>
            <Scenario v-if="isScenario(entity)" :scenario="entity"></Scenario>
            <Request v-if="isRequest(entity)" :request="entity"></Request>
            <Test v-if="isTest(entity)" :test="entity"></Test>
        </div>
    </div>
</template>

<script setup lang="ts">
import {computed} from 'vue'
import type {RequestEntity, RouteEntity, ScenarioEntity, TestEntity} from '@/types/models'
import Request from './Request'
import Route from './Route'
import Scenario from './Scenario'
import Test from './Test'
import {_isRequest, _isRoute, _isScenario, _isTest} from '@/js/common'

type RouteListEntity = RouteEntity | RequestEntity | ScenarioEntity | TestEntity

const props = withDefaults(defineProps<{
  entities?: RouteListEntity[]
}>(), {
  entities: () => [],
})

const compare = (a: unknown, b: unknown) => {
  if (a == null && b == null) return 0
  if (a == null) return 1
  if (b == null) return -1
  if (a < b) return -1
  if (a > b) return 1
  return 0
}

const isRoute = (entity: RouteListEntity): entity is RouteEntity => _isRoute(entity)
const isRequest = (entity: RouteListEntity): entity is RequestEntity => _isRequest(entity)
const isScenario = (entity: RouteListEntity): entity is ScenarioEntity => _isScenario(entity)
const isTest = (entity: RouteListEntity): entity is TestEntity => _isTest(entity)

const sortedEntities = computed(() => [...props.entities].sort((a, b) => {
  let c = compare(a._new, b._new)
  if (c !== 0) return c

  c = compare(a.group, b.group)
  if (c !== 0) return c

  if (isTest(a)) {
    if (!isTest(b)) return -1
    return compare(a.alias, b.alias)
  }

  if (isRequest(a)) {
    if (isTest(b)) return 1
    if (!isRequest(b)) return -1
    return compare(a.id, b.id)
  }

  if (isScenario(a)) {
    if (isTest(b)) return 1
    if (isRequest(b)) return 1
    if (isRoute(b)) return -1
    return compare(a.alias, b.alias)
  }

  const routeA = isRoute(a)
  const routeB = isRoute(b)
  c = compare(routeA, routeB)
  if (c !== 0) return c
  if (!routeA || !routeB) return 0

  c = compare(a.type, b.type)
  if (c !== 0) return c
  c = compare(a.path, b.path)
  if (c !== 0) return c
  c = compare(a.method, b.method)
  if (c !== 0) return c
  return compare(a.alt, b.alt)
}))

const groupStart = (entities: RouteListEntity[], entity: RouteListEntity, index: number) => index > 0 && entity.group !== entities[index - 1].group
const entityKey = (entity: RouteListEntity) => {
  if (isRoute(entity)) return entity.method + entity.path + entity.alt
  if (isRequest(entity)) return entity.id
  return entity.alias
}
</script>

<style scoped>
</style>
