import apiClient from './apiClient'

export async function getStatementsByRoundId(roundId) {
  const { data } = await apiClient.get(`/statements/round/${roundId}`)
  return data
}

export async function createStatement(payload) {
  const { data } = await apiClient.post('/statements', payload)
  return data
}

export async function deleteStatement(statementId) {
  await apiClient.delete(`/statements/${statementId}`)
}
