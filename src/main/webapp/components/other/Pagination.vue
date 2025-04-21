<template>
    <div class="pagination-holder">
        <button type="button" class="btn btn-sm" @click="first"><<</button>
        <button type="button" class="btn btn-sm" @click="minusTen">-10</button>
        <button type="button" class="btn btn-sm" @click="minusOne"><</button>
        <div class="pagination-separator left" aria-hidden="true" role="presentation"></div>
        <div>{{ pageLabel }}</div>
        <div class="pagination-separator" aria-hidden="true" role="presentation"></div>
        <button type="button" class="btn btn-sm" @click="plusOne">></button>
        <button type="button" class="btn btn-sm" @click="plusTen">+10</button>
        <button type="button" class="btn btn-sm" @click="last">>></button>
    </div>
</template>
<script>
export default {
    name: "Pagination",
    components: {},
    data() {
        return {}
    },
    props: {
        offset: {type: Number, default: 0},
        limit: {type: Number, default: 10},
        total: {type: Number, default: 0},
    },
    computed: {
        page() {
            return this.offset / this.limit;
        },
        totalPages() {
            return Math.ceil(this.total / this.limit);
        },
        pageLabel() {
            if (this.totalPages === 0) return 'empty';
            return `Page ${this.page + 1} of ${this.totalPages}`;
        },
    },
    methods: {
        plusOne() {
            if (this.page >= this.totalPages - 1) return;
            this.emitPage(this.page + 1);
        },
        plusTen() {
            if (this.page >= this.totalPages - 1) return;
            this.emitPage(this.page + Math.min(10, this.totalPages - this.page - 1));
        },
        last() {
            if (this.page >= this.totalPages - 1) return;
            this.emitPage(this.totalPages - 1);
        },
        minusOne() {
            if (this.page <= 0) return;
            this.emitPage(this.page - 1);
        },
        minusTen() {
            if (this.page <= 0) return;
            this.emitPage(this.page - Math.min(10, this.page));
        },
        first() {
            if (this.page <= 0) return;
            this.emitPage(0);
        },
        emitPage(value) {
            this.$emit('page', value);
        },
    }
}
</script>
<style lang="scss" scoped>
.pagination-holder {
    display: flex;
    gap: 0 0.25rem;
    flex-wrap: wrap;
    justify-content: center;
    align-content: center;
    align-items: center;
    width: auto;
    margin: 0;
    padding: 0;
}
.pagination-separator {
    flex: 0 0 auto;
    border: none;
    width: 1px;
    height: 1rem;
    border-left: 1px solid var(--form-control-border);
    margin: 0.5rem;
}
</style>
