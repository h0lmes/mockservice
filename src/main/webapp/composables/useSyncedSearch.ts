import { onMounted, ref, watch, type Ref } from 'vue'

export const useSyncedSearch = (searchExpression: Ref<string>) => {
  const search = ref<HTMLInputElement | null>(null)
  const query = ref('')

  const syncSearchValue = (value: string) => {
    if (search.value) {
      search.value.value = value
    }
    query.value = value
  }

  watch(searchExpression, syncSearchValue)
  onMounted(() => syncSearchValue(searchExpression.value))

  return {
    search,
    query,
    syncSearchValue,
  }
}
