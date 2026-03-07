<template>
    <div class="table">
        <p v-if="routes.length === 0">No routes</p>
        <div v-for="(route, index) in routes" :key="route.method + route.path + route.alt" class="row">
            <RouteToAdd :route="route"
                        :group-start="groupStart(route, index)"
                        @add="emit('add', $event)"
            ></RouteToAdd>
        </div>
    </div>
</template>

<script setup lang="ts">
import type {RouteEntity} from '@/types/models'
import RouteToAdd from '@/components/route/RouteToAdd.vue'

const props = withDefaults(defineProps<{
  routes?: RouteEntity[]
}>(), {
  routes: () => [],
})

const emit = defineEmits<{
  add: [route: RouteEntity]
}>()

const groupStart = (route: RouteEntity, index: number) => index === 0 || route.group !== props.routes[index - 1].group
</script>

<style lang="scss" scoped>
.table {
    margin: 0;
    width: 100%;
    min-height: 3rem;
    max-height: 25em;
    background-color: var(--bg-page);
    border: 1px solid var(--form-control-border);
    border-radius: var(--form-control-border-radius);
    overflow: auto;

    & > .row:nth-child(2n+1) {
        background-color: var(--bg-component);
    }

    & > .row:nth-child(2n) {
        background-color: var(--bg-page);
    }
}
</style>

