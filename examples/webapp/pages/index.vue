<template>
    <div class="monospace">
        <div class="component-toolbar mb-5">
            <div class="toolbar-item">
                <input ref="search"
                       type="text"
                       class="form-control monospace"
                       placeholder="filter"
                       @keydown.enter.exact.stop="setFilter($event.target.value)"/>
            </div>
            <div>
                <button type="button" class="toolbar-item-w-fixed-auto btn" @click="setFilter('')">Clear filter</button>
                <button type="button" class="toolbar-item-w-fixed-auto btn" @click="$fetch">Update</button>
            </div>
        </div>

        <div class="component-toolbar mb-5">
            <ViewSelector class="toolbar-item toolbar-item-w-fixed-auto"></ViewSelector>
        </div>

        <StoreItems :entities="filteredEntities" @filter="setFilter($event)"></StoreItems>

        <Loading v-if="$fetchState.pending"></Loading>
    </div>
</template>
<script>
    import {mapActions} from 'vuex';
    import StoreItems from "../components/route/StoreItems";
    import Loading from "../components/Loading";
    import ViewSelector from "../components/ViewSelector";
    import ToggleSwitch from "../components/ToggleSwitch";

    export default {
        name: "index",
        components: {StoreItems, Loading, ViewSelector, ToggleSwitch},
        data() {
            return {
                query: '',
                timeout: null,
            }
        },
        async fetch() {
            return this.fetchItems();
        },
        fetchDelay: 0,
        computed: {
            items() {
                return this.$store.state.items.items;
            },
            entities() {
                return [...this.items];
            },
            filteredEntities() {
                if (!this.query) {
                    return this.entities;
                }

                try {
                    let query = this.query.toUpperCase();
                    return this.entities.filter(
                        e => e.title.toUpperCase().includes(query) || e.category.toUpperCase().includes(query) || e.description.toUpperCase().includes(query)
                    );
                } catch (e) {
                    console.error(e);
                    return [];
                }
            },
        },
        methods: {
            ...mapActions({
                fetchItems: 'items/fetch',
            }),
            setFilter(value) {
                this.$refs.search.value = value.trim();
                this.query = value.trim();
            },
        }
    }
</script>
<style scoped>
</style>
