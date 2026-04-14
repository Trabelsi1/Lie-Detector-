import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { getRoundById, advancePhase } from '../services/roundsApi'
import { getStatementsByRoundId, createStatement } from '../services/statementsApi'

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

  const [currentPlayerId] = useState(Number(localStorage.getItem('currentPlayerId')) || null)

  useEffect(() => {
    loadRoundData()
  }, [roundId])

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
      setError(err.response?.data?.message || 'Failed to load round data')
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
      setError(err.response?.data?.message || 'Failed to add statement')
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
      setError(err.response?.data?.message || 'Failed to advance phase')
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
                      onChange={(e) => setFormData({ ...formData, isLie: e.target.checked })}
                    />
                    This statement is the lie
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
                  <li key={stmt.id}>
                    {stmt.content}
                    {stmt.isLie && ' <-- THIS IS THE LIE'}
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

          {!isAllowedToSubmit && statements.length === 3 && (
            <button onClick={() => navigate(`/game/${gameId}/discussion/${roundId}`)}>
              Go to Discussion
            </button>
          )}
        </>
      )}
    </div>
  )
}
