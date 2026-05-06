import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { getRoundById, advancePhase } from '../services/roundsApi'
import { getStatementsByRoundId, createStatement, deleteStatement } from '../services/statementsApi'

export default function StatementSubmissionPage() {
  const { gameId, roundId } = useParams()
  const navigate = useNavigate()

  const [round, setRound] = useState(null)
  const [statements, setStatements] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const [formData, setFormData] = useState({
    content: '',
    position: 0,
    isLie: false,
  })

  const [currentPlayerId] = useState(
    Number(sessionStorage.getItem('currentPlayerId') || localStorage.getItem('currentPlayerId')) || null,
  )

  useEffect(() => {
    loadRoundData()
  }, [roundId])

  useEffect(() => {
    const intervalId = setInterval(async () => {
      try {
        const latestRound = await getRoundById(roundId)
        if (!latestRound) return

        setRound(latestRound)

        if (latestRound.phase === 'DISCUSSION') {
          navigate(`/game/${gameId}/discussion/${roundId}`)
        } else if (latestRound.phase === 'VOTING') {
          navigate(`/game/${gameId}/voting/${roundId}`)
        } else if (latestRound.phase === 'RESULTS') {
          navigate(`/game/${gameId}/results/${roundId}`)
        }
      } catch {
        // keep polling silently
      }
    }, 2000)

    return () => clearInterval(intervalId)
  }, [gameId, roundId, navigate])

  async function loadRoundData() {
    try {
      setLoading(true)
      const roundData = await getRoundById(roundId)
      setRound(roundData)

      // Check if current player is the speaker
      if (currentPlayerId && roundData.speakerId !== currentPlayerId) {
        setError('Only the speaker can submit statements')
        // Auto-redirect to discussion after statements are submitted
        if (roundData.phase !== 'STATEMENT_SUBMISSION') {
          navigate(`/game/${gameId}/discussion/${roundId}`)
        }
      }

      const stmts = await getStatementsByRoundId(roundId)
      setStatements(stmts)
    } catch (err) {
      setError(err.message || 'Failed to load round data')
    } finally {
      setLoading(false)
    }
  }

  async function handleAddStatement(e) {
    e.preventDefault()
    try {
      if (!formData.content.trim()) {
        setError('Statement content cannot be empty')
        return
      }

      const newStatement = await createStatement({
        content: formData.content,
        isLie: formData.isLie,
        position: statements.length + 1,
        round: { id: Number(roundId) },
      })

      setStatements([...statements, newStatement])
      setFormData({
        content: '',
        position: 0,
        isLie: false,
      })
      setError('')
    } catch (err) {
      setError(err.message || 'Failed to add statement')
    }
  }

  async function handleDeleteStatement(statementId) {
    try {
      await deleteStatement(statementId)
      setStatements(statements.filter(stmt => stmt.id !== statementId))
      setError('')
    } catch (err) {
      setError(err.message || 'Failed to delete statement')
    }
  }

  async function handleAdvancePhase() {
    try {
      if (statements.length !== 3) {
        setError('Please submit exactly 3 statements')
        return
      }

      const updatedRound = await advancePhase(roundId)
      setRound(updatedRound)
      navigate(`/game/${gameId}/discussion/${roundId}`)
    } catch (err) {
      setError(err.message || 'Failed to advance phase')
    }
  }

  if (loading) return <div className="page-container">Loading...</div>

  const isAllowedToSubmit =
    round && currentPlayerId && round.speakerId === currentPlayerId && round.phase === 'STATEMENT_SUBMISSION'

  return (
    <div className="page-container">
      <h1>Statement Submission</h1>

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
            <p>
              <strong>Speaker ID:</strong> {round.speakerId}
            </p>
            {isAllowedToSubmit && (
              <p style={{ color: 'green' }}>You are the speaker for this round</p>
            )}
          </div>

          {isAllowedToSubmit && (
            <div className="card">
              <h2>Add Statement ({statements.length}/3)</h2>
              <form onSubmit={handleAddStatement}>
                <div>
                  <label htmlFor="content">Statement Content:</label>
                  <textarea
                    id="content"
                    value={formData.content}
                    onChange={(e) => setFormData({ ...formData, content: e.target.value })}
                    placeholder="Enter your statement..."
                  />
                </div>

                <div>
                  <label htmlFor="isLie">
                    <input
                      type="checkbox"
                      id="isLie"
                      checked={formData.isLie}
                      onChange={(e) => {
                        // If checking and already have a lie, prevent it
                        const hasExistingLie = statements.some(stmt => {
                          return stmt.isLie === true || stmt.isLie === 'true' || !!stmt.isLie
                        })
                        if (e.target.checked && hasExistingLie) {
                          setError('You can only have one lie per round. Uncheck the other statement first.')
                          return
                        }
                        setFormData({ ...formData, isLie: e.target.checked })
                        setError('')
                      }}
                    />
                    Mark as lie
                  </label>
                </div>

                <button type="submit" disabled={statements.length >= 3}>
                  Add Statement
                </button>
              </form>
            </div>
          )}

          <div className="card">
            <h2>Submitted Statements</h2>
            {statements.length > 0 ? (
              <ol>
                {statements.map((stmt) => (
                  <li key={stmt.id} style={{ marginBottom: '10px' }}>
                    <div>{stmt.content}</div>
                    {isAllowedToSubmit && (
                      <button
                        onClick={() => handleDeleteStatement(stmt.id)}
                        style={{
                          marginTop: '5px',
                          padding: '5px 10px',
                          backgroundColor: '#ff6b6b',
                          color: 'white',
                          border: 'none',
                          borderRadius: '4px',
                          cursor: 'pointer',
                        }}
                      >
                        Delete
                      </button>
                    )}
                  </li>
                ))}
              </ol>
            ) : (
              <p>No statements submitted yet</p>
            )}
          </div>

          {isAllowedToSubmit && statements.length === 3 && (
            <button onClick={handleAdvancePhase} className="primary-button">
              All Statements Ready - Go to Discussion
            </button>
          )}

          <button onClick={() => navigate(`/game/${gameId}/lobby`)}>Back to Lobby</button>
        </>
      )}
    </div>
  )
}
