import apiClient from './apiClient'

export async function getRoundsByGameId(gameId) {
  const { data } = await apiClient.get(`/rounds/game/${gameId}`)
  return data
}

export async function getRoundById(roundId) {
  const { data } = await apiClient.get(`/rounds/${roundId}`)
  return data
}

export async function createRound(payload) {
  const { data } = await apiClient.post('/rounds', payload)
  return data
}

export async function advancePhase(roundId) {
  const { data } = await apiClient.put(`/rounds/${roundId}/advance-phase`)
  return data
}

export async function canPlayerVote(roundId, playerId) {
  const { data } = await apiClient.get(`/rounds/${roundId}/can-vote/${playerId}`)
  return data
}
