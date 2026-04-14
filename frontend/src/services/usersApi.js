import apiClient from './apiClient'

export async function getUsers() {
  const { data } = await apiClient.get('/users')
  return data
}

export async function createUser(payload) {
  const { data } = await apiClient.post('/users', payload)
  return data
}