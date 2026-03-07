import {ref} from 'vue'
import {useFrontendApp} from '@/state/app'

export const useWorkingAction = () => {
  const frontendApp = useFrontendApp()

  const runWhileWorking = async <T>(action: () => Promise<T>) => {
    frontendApp.setWorking(true)
    try {
      return await action()
    } finally {
      frontendApp.setWorking(false)
    }
  }

  return {
    runWhileWorking,
  }
}

export const usePageLoader = () => {
  const pageLoading = ref(false)

  const runWhilePageLoading = async <T>(action: () => Promise<T>) => {
    pageLoading.value = true
    try {
      return await action()
    } finally {
      pageLoading.value = false
    }
  }

  return {
    pageLoading,
    runWhilePageLoading,
  }
}
