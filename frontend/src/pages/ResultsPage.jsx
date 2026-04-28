import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { getRoundById } from '../services/roundsApi'
import { getStatementsByRoundId } from '../services/statementsApi'
import { getVotesByRoundId, countVotesForStatement } from '../services/votesApi'
import { startRound, allSpeakersDone, getFinalRankings, getFinalSummary, getCurrentRound } from '../services/gamesApi'

export default function ResultsPage() {
  const { gameId, roundId } = useParams()
  const navigate = useNavigate()

  const [round, setRound] = useState(null)
  const [statements, setStatements] = useState([])
  const [votes, setVotes] = useState([])
  const [voteCounts, setVoteCounts] = useState({})
  const [speakersDone, setSpeakersDone] = useState(false)
  const [finalRankings, setFinalRankings] = useState([])
  const [finalAwards, setFinalAwards] = useState([])
  const [countdown, setCountdown] = useState(3)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    loadRoundData()
  }, [roundId])

  useEffect(() => {
    if (!round || round.phase !== 'RESULTS') return

    if (speakersDone) {
      return
    }

    setCountdown(5)

    let remaining = 5
    const intervalId = setInterval(async () => {
      remaining -= 1

      if (remaining > 0) {
        setCountdown(remaining)
        return
      }

      clearInterval(intervalId)
      try {
        const nextRound = await startRound(gameId)
        navigate(`/game/${gameId}/statement-submission/${nextRound.id}`)
      } catch {
        // Another tab may have started it first. Navigate to whichever round is current.
        try {
          const currentRound = await getCurrentRound(gameId)
          if (!currentRound?.id) {
            navigate(`/game/${gameId}/lobby`)
            return
          }

          if (currentRound.phase === 'STATEMENT_SUBMISSION') {
            navigate(`/game/${gameId}/statement-submission/${currentRound.id}`)
          } else if (currentRound.phase === 'DISCUSSION') {
            navigate(`/game/${gameId}/discussion/${currentRound.id}`)
          } else if (currentRound.phase === 'VOTING') {
            navigate(`/game/${gameId}/voting/${currentRound.id}`)
          } else {
            navigate(`/game/${gameId}/results/${currentRound.id}`)
          }
        } catch {
          navigate(`/game/${gameId}/lobby`)
        }
      }
    }, 1000)

    return () => clearInterval(intervalId)
  }, [gameId, navigate, round, speakersDone])

  async function loadRoundData() {
    try {
      setLoading(true)
      const roundData = await getRoundById(roundId)
      setRound(roundData)

      try {
        const done = await allSpeakersDone(gameId)
        setSpeakersDone(Boolean(done))
        if (done) {
          const rankings = await getFinalRankings(gameId)
          setFinalRankings(Array.isArray(rankings) ? rankings : [])

          const summary = await getFinalSummary(gameId)
          setFinalAwards(Array.isArray(summary?.awards) ? summary.awards : [])
        } else {
          setFinalRankings([])
          setFinalAwards([])
        }
      } catch {
        setSpeakersDone(false)
        setFinalRankings([])
        setFinalAwards([])
      }

      const stmts = await getStatementsByRoundId(roundId)
      const safeStatements = Array.isArray(stmts) ? stmts.filter(Boolean) : []
      setStatements(safeStatements)

      const allVotes = await getVotesByRoundId(roundId)
      const safeVotes = Array.isArray(allVotes) ? allVotes.filter(Boolean) : []
      setVotes(safeVotes)

      // Count votes per statement
      const counts = {}
      for (const stmt of safeStatements) {
        if (!stmt?.id) continue
        const count = await countVotesForStatement(roundId, stmt.id)
        counts[stmt.id] = Number(count) || 0
      }
      setVoteCounts(counts)
    } catch (err) {
      setError(err.message || 'Failed to load round data')
    } finally {
      setLoading(false)
    }
  }

  if (loading) return <div className="page-container">Loading...</div>

  const safeStatements = Array.isArray(statements) ? statements : []
  const safeVotes = Array.isArray(votes) ? votes : []

  const correctStatement = safeStatements.find((s) => s?.isLie === true)
  const correctStatementVotes = correctStatement ? voteCounts[correctStatement.id] || 0 : 0
  const correctPlayers = correctStatement
    ? safeVotes.filter((v) => v?.statement?.id === correctStatement.id)
    : []

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
            {!speakersDone && (
              <p style={{ marginTop: '10px', fontWeight: 'bold', color: '#1565c0' }}>
                Next speaker round starts in {countdown}s...
              </p>
            )}
          </div>

          {speakersDone && finalRankings.length > 0 && (
            <div className="card">
              <h2>Final Rankings</h2>
              <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                <thead>
                  <tr style={{ borderBottom: '2px solid #ddd' }}>
                    <th style={{ textAlign: 'left', padding: '10px' }}>Rank</th>
                    <th style={{ textAlign: 'left', padding: '10px' }}>Player</th>
                    <th style={{ textAlign: 'right', padding: '10px' }}>Score</th>
                  </tr>
                </thead>
                <tbody>
                  {finalRankings.map((rank, index) => (
                    <tr key={rank.playerId} style={{ borderBottom: '1px solid #eee' }}>
                      <td style={{ padding: '10px' }}>#{index + 1}</td>
                      <td style={{ padding: '10px' }}>{rank.playerName}</td>
                      <td style={{ textAlign: 'right', padding: '10px' }}>{rank.score}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          {speakersDone && finalAwards.length > 0 && (
            <div className="card">
              <h2>Game Awards</h2>
              <ul style={{ listStyle: 'none', paddingLeft: 0, margin: 0 }}>
                {finalAwards.map((award) => (
                  <li key={award.key} style={{ marginBottom: '12px', borderBottom: '1px solid #eee', paddingBottom: '10px' }}>
                    <strong>{award.title}:</strong>{' '}
                    {Array.isArray(award.winners) && award.winners.length > 0
                      ? award.winners.map((winner) => `${winner.playerName} (${winner.value})`).join(', ')
                      : 'N/A'}
                    {award.tie ? ' (tie)' : ''}
                  </li>
                ))}
              </ul>
            </div>
          )}

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
            {safeStatements.length > 0 ? (
              <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                <thead>
                  <tr style={{ borderBottom: '2px solid #ddd' }}>
                    <th style={{ textAlign: 'left', padding: '10px' }}>Statement</th>
                    <th style={{ textAlign: 'center', padding: '10px' }}>Votes</th>
                    <th style={{ textAlign: 'center', padding: '10px' }}>Is Lie</th>
                  </tr>
                </thead>
                <tbody>
                  {safeStatements.map((stmt) => (
                    <tr key={stmt.id} style={{ borderBottom: '1px solid #eee' }}>
                      <td style={{ padding: '10px' }}>{stmt.content}</td>
                      <td style={{ textAlign: 'center', padding: '10px' }}>
                        <strong>{voteCounts[stmt.id] || 0}</strong>
                      </td>
                      <td style={{ textAlign: 'center', padding: '10px' }}>
                        {stmt.isLie === true ? '✓ YES' : stmt.isLie === false ? '✗ NO' : '-'}
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
                    Player #{v?.voter?.id ?? 'N/A'} ({v?.voter?.user?.username || 'Unknown'})
                  </li>
                ))}
              </ul>
            </div>
          )}

          <button onClick={() => navigate(`/game/${gameId}/lobby`)}>Back to Lobby</button>
        </>
      )}
    </div>
  )
}
