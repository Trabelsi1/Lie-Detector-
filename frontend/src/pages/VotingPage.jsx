import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { getRoundById, advancePhase, getVotingStatus } from '../services/roundsApi'
import { getStatementsByRoundId } from '../services/statementsApi'
import { createVote, hasPlayerVoted } from '../services/votesApi'

export default function VotingPage() {
  const { gameId, roundId } = useParams()
  const navigate = useNavigate()

  const [round, setRound] = useState(null)
  const [statements, setStatements] = useState([])
  const [selectedStatementId, setSelectedStatementId] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [votingStatus, setVotingStatus] = useState({ requiredVotes: 0, submittedVotes: 0, complete: false })

  const [currentPlayerId] = useState(
    Number(sessionStorage.getItem('currentPlayerId') || localStorage.getItem('currentPlayerId')) || null,
  )
  const [hasVoted, setHasVoted] = useState(false)

  useEffect(() => {
    loadRoundData()
  }, [roundId])

  useEffect(() => {
    const intervalId = setInterval(async () => {
      try {
        const latestRound = await getRoundById(roundId)
        if (!latestRound) return

        setRound(latestRound)

        if (latestRound.phase === 'RESULTS') {
          navigate(`/game/${gameId}/results/${roundId}`)
        }
      } catch {
        // keep polling silently; page-level error handling is already in loadRoundData.
      }
    }, 2000)

    return () => clearInterval(intervalId)
  }, [gameId, roundId, navigate])

  async function loadRoundData() {
    try {
      setLoading(true)
      const roundData = await getRoundById(roundId)
      setRound(roundData)

      const stmts = await getStatementsByRoundId(roundId)
      setStatements(stmts)

      const status = await getVotingStatus(roundId)
      setVotingStatus(status)

      // Check if current player has already voted
      if (currentPlayerId) {
        const voted = await hasPlayerVoted(roundId, currentPlayerId)
        setHasVoted(voted)
      }
    } catch (err) {
      setError(err.message || 'Failed to load round data')
    } finally {
      setLoading(false)
    }
  }

  async function handleVote(statementId) {
    try {
      if (!currentPlayerId) {
        setError('No player ID found. Please ensure you are logged in.')
        return
      }

      await createVote(roundId, currentPlayerId, statementId)
      setSuccess('Vote cast successfully!')
      setSelectedStatementId(statementId)
      setHasVoted(true)
      const status = await getVotingStatus(roundId)
      setVotingStatus(status)
      setError('')
    } catch (err) {
      setError(err.message || 'Failed to cast vote')
    }
  }

  async function handleAdvanceToResults() {
    try {
      const updatedRound = await advancePhase(roundId)
      setRound(updatedRound)
      navigate(`/game/${gameId}/results/${roundId}`)
    } catch (err) {
      setError(err.message || 'Failed to advance phase')
    }
  }

  const isCurrentPlayerSpeaker = round && currentPlayerId && round.speakerId === currentPlayerId
  const canVote = !isCurrentPlayerSpeaker && round?.phase === 'VOTING'

  if (loading) return <div className="page-container">Loading...</div>

  return (
    <div className="page-container">
      <h1>Voting Phase</h1>

      {error && <div className="error-message">{error}</div>}
      {success && <div style={{ color: 'green', marginBottom: '10px' }}>{success}</div>}

      {round && (
        <>
          <div className="card">
            <h2>Round Information</h2>
            <p>
              <strong>Round Number:</strong> {round.roundNumber}
            </p>
            <p>
              <strong>Phase:</strong> {round.phase}
            </p>
            {isCurrentPlayerSpeaker && (
              <p style={{ color: 'orange' }}>You are the speaker. You cannot vote.</p>
            )}
          </div>

          <div className="card">
            <h2>Vote on the Lie</h2>
            {isCurrentPlayerSpeaker ? (
              <p style={{ color: 'orange' }}>
                Only other players can vote. Wait for voting to complete.
              </p>
            ) : hasVoted ? (
              <p style={{ color: 'green' }}>You have voted. Waiting for other players...</p>
            ) : (
              <p>Which statement is the lie?</p>
            )}

            <p style={{ marginTop: '8px' }}>
              Votes submitted: {votingStatus.submittedVotes}/{votingStatus.requiredVotes}
            </p>

            {statements.length > 0 ? (
              <div style={{ marginTop: '20px' }}>
                {statements.map((stmt, index) => (
                  <button
                    key={stmt.id}
                    onClick={() => handleVote(stmt.id)}
                    disabled={!canVote || hasVoted}
                    style={{
                      display: 'block',
                      width: '100%',
                      padding: '15px',
                      margin: '10px 0',
                      backgroundColor:
                        selectedStatementId === stmt.id && hasVoted ? '#4CAF50' : '#f0f0f0',
                      color: selectedStatementId === stmt.id && hasVoted ? 'white' : 'black',
                      border: '2px solid #ddd',
                      borderRadius: '5px',
                      cursor: canVote && !hasVoted ? 'pointer' : 'not-allowed',
                      fontSize: '1em',
                    }}
                  >
                    <strong>Statement {index + 1}:</strong> {stmt.content}
                  </button>
                ))}
              </div>
            ) : (
              <p>No statements found</p>
            )}
          </div>

          <button onClick={handleAdvanceToResults} className="primary-button" disabled={!votingStatus.complete}>
            All Votes In - Show Results
          </button>

          <button onClick={() => navigate(`/game/${gameId}/lobby`)}>Back to Lobby</button>
        </>
      )}
    </div>
  )
}
