import apiClient from './apiClient'

export async function getPlayers() {
  const { data } = await apiClient.get('/players')
  return data
}

export async function createPlayerFromUser(userId) {
  const { data } = await apiClient.post(`/players/from-user/${Number(userId)}`)
  return data
}