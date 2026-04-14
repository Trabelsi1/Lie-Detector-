import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { getRoundById, advancePhase } from '../services/roundsApi'
import { getStatementsByRoundId } from '../services/statementsApi'
import { getVotesByRoundId, countVotesForStatement } from '../services/votesApi'

export default function ResultsPage() {
  const { gameId, roundId } = useParams()
  const navigate = useNavigate()

  const [round, setRound] = useState(null)
  const [statements, setStatements] = useState([])
  const [votes, setVotes] = useState([])
  const [voteCounts, setVoteCounts] = useState({})
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

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

      const allVotes = await getVotesByRoundId(roundId)
      setVotes(allVotes)

      // Count votes per statement
      const counts = {}
      for (const stmt of stmts) {
        const count = await countVotesForStatement(roundId, stmt.id)
        counts[stmt.id] = count
      }
      setVoteCounts(counts)
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load round data')
    } finally {
      setLoading(false)
    }
  }

  async function handleNextRound() {
    try {
      // Try to advance phase to complete this round, then go back to lobby
      await advancePhase(roundId)
      navigate(`/game/${gameId}/lobby/${gameId}`)
    } catch (err) {
      // If already at RESULTS, just go back to lobby
      navigate(`/game/${gameId}/lobby/${gameId}`)
    }
  }

  if (loading) return <div className="page-container">Loading...</div>

  const correctStatement = statements.find((s) => s.isLie)
  const correctStatementVotes = correctStatement ? voteCounts[correctStatement.id] || 0 : 0
  const correctPlayers = votes.filter((v) => v.statement.id === correctStatement?.id)

  return (
    <div className="page-container">
      <h1>Results</h1>

      {error && <div className="error-message">{error}</div>}

      {round && (
        <>
          <div className="card">
            <h2>Round {round.roundNumber} Results</h2>
            <p>
              <strong>Speaker ID:</strong> {round.speakerId}
            </p>
          </div>

          <div className="card">
            <h2>The Lie Was:</h2>
            {correctStatement ? (
              <>
                <p style={{ fontSize: '1.2em', fontWeight: 'bold', color: '#d32f2f' }}>
                  {correctStatement.content}
                </p>
                <p>
                  <strong>Votes for correct answer:</strong> {correctStatementVotes}
                </p>
              </>
            ) : (
              <p>Could not determine the lie</p>
            )}
          </div>

          <div className="card">
            <h2>All Votes</h2>
            {statements.length > 0 ? (
              <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                <thead>
                  <tr style={{ borderBottom: '2px solid #ddd' }}>
                    <th style={{ textAlign: 'left', padding: '10px' }}>Statement</th>
                    <th style={{ textAlign: 'center', padding: '10px' }}>Votes</th>
                    <th style={{ textAlign: 'center', padding: '10px' }}>Is Lie</th>
                  </tr>
                </thead>
                <tbody>
                  {statements.map((stmt) => (
                    <tr key={stmt.id} style={{ borderBottom: '1px solid #eee' }}>
                      <td style={{ padding: '10px' }}>{stmt.content}</td>
                      <td style={{ textAlign: 'center', padding: '10px' }}>
                        <strong>{voteCounts[stmt.id] || 0}</strong>
                      </td>
                      <td style={{ textAlign: 'center', padding: '10px' }}>
                        {stmt.isLie ? '✓ YES' : '✗ NO'}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            ) : (
              <p>No statements found</p>
            )}
          </div>

          {correctPlayers.length > 0 && (
            <div className="card">
              <h2>Correct Guesses</h2>
              <p>
                <strong>{correctPlayers.length} players</strong> guessed correctly!
              </p>
              <ul>
                {correctPlayers.map((v) => (
                  <li key={v.id}>
                    Player #{v.voter.id} ({v.voter.user?.username || 'Unknown'})
                  </li>
                ))}
              </ul>
            </div>
          )}

          <button onClick={handleNextRound} className="primary-button">
            Next Round
          </button>

          <button onClick={() => navigate(`/game/${gameId}/lobby/${gameId}`)}>Back to Lobby</button>
        </>
      )}
    </div>
  )
}
