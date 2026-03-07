import {apiText, withApiError} from '@/utils/api'

export const fetchLog = async () => withApiError(() => apiText('/__webapi__/log'))
