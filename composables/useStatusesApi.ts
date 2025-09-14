import {
  fetchDetailedStatuses,
  createStatus,
  updateStatus,
  deleteStatus,
  type StatusDTO,
  type CreateStatusRequest
} from './useStatuses'

export async function fetchStatusesApi(): Promise<StatusDTO[]> {
  return await fetchDetailedStatuses()
}

export async function createStatusApi(request: CreateStatusRequest): Promise<StatusDTO | null> {
  return await createStatus(request)
}

export async function updateStatusApi(id: string, request: CreateStatusRequest): Promise<StatusDTO | null> {
  return await updateStatus(id, request)
}

export async function deleteStatusApi(id: string): Promise<boolean> {
  return await deleteStatus(id)
}