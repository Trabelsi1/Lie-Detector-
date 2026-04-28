import apiClient from './apiClient'

export async function getMessagesByRoundId(roundId) {
  const { data } = await apiClient.get(`/messages/round/${roundId}`)
  return data
}

export async function createMessage(message) {
  const { data } = await apiClient.post('/messages', message)
  return data
}
