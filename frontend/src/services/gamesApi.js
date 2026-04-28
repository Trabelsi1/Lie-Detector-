import apiClient from './apiClient'

export async function getGames() {
  const { data } = await apiClient.get('/games')
  return data
}

export async function getGamesByRoomId(roomId) {
  const { data } = await apiClient.get(`/games/room/${roomId}`)
  return data
}

export async function createGame(payload) {
  const { data } = await apiClient.post('/games', payload)
  return data
}

export async function getGameById(gameId) {
  const { data } = await apiClient.get(`/games/${gameId}`)
  return data
}

export async function startRound(gameId) {
  const { data } = await apiClient.post(`/games/${gameId}/start-round`)
  return data
}

export async function getCurrentRound(gameId) {
  const { data } = await apiClient.get(`/games/${gameId}/current-round`)
  return data
}

export async function allSpeakersDone(gameId) {
  const { data } = await apiClient.get(`/games/${gameId}/all-speakers-done`)
  return data
}

export async function getSpeakerProgress(gameId) {
  const { data } = await apiClient.get(`/games/${gameId}/speaker-progress`)
  return data
}

export async function getFinalRankings(gameId) {
  const { data } = await apiClient.get(`/games/${gameId}/final-rankings`)
  return data
}

export async function getFinalSummary(gameId) {
  const { data } = await apiClient.get(`/games/${gameId}/final-summary`)
  return data
}
