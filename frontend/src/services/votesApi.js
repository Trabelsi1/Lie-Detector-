import apiClient from './apiClient'

export async function createVote(roundId, voterId, statementId) {
  const { data } = await apiClient.post(
    `/votes/round/${roundId}/voter/${voterId}/statement/${statementId}`
  )
  return data
}

export async function getVotesByRoundId(roundId) {
  const { data } = await apiClient.get(`/votes/round/${roundId}`)
  return data
}

export async function hasPlayerVoted(roundId, voterId) {
  const { data } = await apiClient.get(`/votes/round/${roundId}/voter/${voterId}`)
  return data
}

export async function countVotesForStatement(roundId, statementId) {
  const { data } = await apiClient.get(
    `/votes/round/${roundId}/statement/${statementId}/count`
  )
  return data
}
