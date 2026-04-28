import apiClient from './apiClient'

export async function getRooms() {
  const { data } = await apiClient.get('/rooms')
  return data
}

export async function getRoomById(roomId) {
  const { data } = await apiClient.get(`/rooms/${roomId}`)
  return data
}

export async function getRoomPlayers(roomId) {
  const { data } = await apiClient.get(`/rooms/${roomId}/players`)
  return data
}

export async function createRoom(payload) {
  const { data } = await apiClient.post('/rooms', payload)
  return data
}

export async function joinPlayerToRoom(roomId, playerId) {
  const { data } = await apiClient.post(`/rooms/${roomId}/players/${playerId}`)
  return data
}