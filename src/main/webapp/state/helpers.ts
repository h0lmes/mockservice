import type {SelectableEntity} from '@/types/models'

export const withSelection = <T extends SelectableEntity>(items: T[]) => {
  return items.map((item) => ({ ...item, _selected: null }))
}

export const setAllSelected = <T extends SelectableEntity>(items: T[], selected: boolean | null) => {
  for (const item of items) {
    item._selected = selected
  }
}
