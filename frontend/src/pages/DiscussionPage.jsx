import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { getRoundById, advancePhase } from '../services/roundsApi'
import { getStatementsByRoundId } from '../services/statementsApi'

export default function DiscussionPage() {
  const { gameId, roundId } = useParams()
  const navigate = useNavigate()

  const [round, setRound] = useState(null)
  const [statements, setStatements] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const [discussionTime, setDiscussionTime] = useState(5) // 5 minutes default

  useEffect(() => {
    loadRoundData()
  }, [roundId])

  async function loadRoundData() {
    try {
      setLoading(true)
      const roundData = await getRoundById(roundId)
      setRound(roundData)

      const stmts = await getStatementsByRoundId(roundId)
      setStatements(stmts)
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load round data')
    } finally {
      setLoading(false)
    }
  }

  async function handleAdvanceToVoting() {
    try {
      const updatedRound = await advancePhase(roundId)
      setRound(updatedRound)
      navigate(`/game/${gameId}/voting/${roundId}`)
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to advance phase')
    }
  }

  if (loading) return <div className="page-container">Loading...</div>

  return (
    <div className="page-container">
      <h1>Discussion Phase</h1>

      {error && <div className="error-message">{error}</div>}

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
          </div>

          <div className="card">
            <h2>Statements</h2>
            <p style={{ fontStyle: 'italic', marginBottom: '10px' }}>
              Discuss these statements. Which one is a lie?
            </p>
            {statements.length > 0 ? (
              <ol>
                {statements.map((stmt) => (
                  <li key={stmt.id} style={{ marginBottom: '10px' }}>
                    {stmt.content}
                  </li>
                ))}
              </ol>
            ) : (
              <p>No statements found</p>
            )}
          </div>

          <div className="card">
            <h2>Discussion Timer</h2>
            <p>
              <strong>Time given:</strong> {discussionTime} minutes
            </p>
            <p style={{ fontSize: '0.9em', color: '#666' }}>
              players have time to discuss and identify which statement is the lie
            </p>
          </div>

          <button onClick={handleAdvanceToVoting} className="primary-button">
            Time's Up - Go to Voting
          </button>

          <button onClick={() => navigate(`/game/${gameId}/lobby/${gameId}`)}>Back to Lobby</button>
        </>
      )}
    </div>
  )
}
