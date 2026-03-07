<template>
    <div>
        <p v-if="importedRoutes.length === 0">No routes</p>
        <div v-for="(route, index) in importedRoutes" :key="route.method + route.path + route.alt">
            <div v-if="groupStart(route, index)" class="component-row-group-boundary"></div>
            <ImportedRoute
                    :route="route"
                    :existing-routes="existingRoutes"
                    @add="emit('add', $event)"
            ></ImportedRoute>
        </div>
    </div>
</template>

<script setup lang="ts">
import type {RouteEntity} from '@/types/models'
import ImportedRoute from './ImportedRoute'

const props = withDefaults(defineProps<{
  importedRoutes?: RouteEntity[]
  existingRoutes?: RouteEntity[]
}>(), {
  importedRoutes: () => [],
  existingRoutes: () => [],
})

const emit = defineEmits<{
  add: [route: RouteEntity]
}>()

const groupStart = (route: RouteEntity, index: number) => index > 0 && route.group !== props.importedRoutes[index - 1].group
</script>

<style scoped>
</style>
