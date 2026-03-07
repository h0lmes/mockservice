import {apiText, withApiError} from '@/utils/api'

export const fetchRequestGraph = async () => withApiError(() => apiText('/__webapi__/request-graph'))
