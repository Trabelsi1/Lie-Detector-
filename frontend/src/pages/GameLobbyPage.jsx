import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import {
  getGameById,
  startRound,
  getCurrentRound,
  allSpeakersDone,
  getFinalRankings,
  getSpeakerProgress,
} from '../services/gamesApi'
import { getRoomById, getRoomPlayers } from '../services/roomsApi'

export default function GameLobbyPage() {
  const { gameId } = useParams()
  const navigate = useNavigate()

  const [game, setGame] = useState(null)
  const [currentRound, setCurrentRound] = useState(null)
  const [room, setRoom] = useState(null)
  const [players, setPlayers] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [speakersDone, setSpeakersDone] = useState(false)
  const [speakerProgress, setSpeakerProgress] = useState({ completedSpeakers: 0, totalPlayers: 0, allSpeakersDone: false })
  const [rankings, setRankings] = useState([])
  const [showRankings, setShowRankings] = useState(false)

  useEffect(() => {
    loadGameData()
  }, [gameId])

  async function loadGameData() {
    try {
      setLoading(true)
      setError('')
      setGame(null)
      setRoom(null)
      setPlayers([])
      setCurrentRound(null)
      setSpeakersDone(false)
      setSpeakerProgress({ completedSpeakers: 0, totalPlayers: 0, allSpeakersDone: false })
      setRankings([])
      setShowRankings(false)

      const gameData = await getGameById(gameId)
      setGame(gameData)

      // Load room with players
      const roomId = gameData.gameRoom?.id
      if (roomId) {
        try {
          const roomData = await getRoomById(roomId)
          setRoom(roomData)
          const roomPlayers = await getRoomPlayers(roomId)
          setPlayers(Array.isArray(roomPlayers) ? roomPlayers.filter(Boolean) : [])
        } catch {
          setRoom(gameData.gameRoom || null)
          setPlayers([])
        }
      }

      // Load current round
      try {
        const round = await getCurrentRound(gameId)
        setCurrentRound(round || null)
      } catch {
        setCurrentRound(null)
      }

      // Check if all speakers are done
      try {
        const progress = await getSpeakerProgress(gameId)
        setSpeakerProgress(progress || { completedSpeakers: 0, totalPlayers: players.length, allSpeakersDone: false })

        const done = await allSpeakersDone(gameId)
        setSpeakersDone(done)
        if (done) {
          const finalRankings = await getFinalRankings(gameId)
          setRankings(finalRankings)
        }
      } catch {
        setSpeakersDone(false)
      }
    } catch (err) {
      setError(err.message || 'Failed to load game data')
    } finally {
      setLoading(false)
    }
  }

  async function handleStartRound() {
    try {
      const newRound = await startRound(gameId)
      setCurrentRound(newRound)
      navigate(`/game/${gameId}/statement-submission/${newRound.id}`)
    } catch (err) {
      setError(err.message || 'Failed to start round')
    }
  }

  if (loading) return <div className="page-container">Loading...</div>

  return (
    <div className="page-container">
      <h1>Game Lobby</h1>

      {error && <div className="error-message">{error}</div>}

      {game && (
        <>
          <div className="card">
            <h2>Game Information</h2>
            <p>
              <strong>Room:</strong> {room?.roomCode || '#' + room?.id}
            </p>
            <p>
              <strong>Players:</strong> {players.length}
            </p>
            <p>
              <strong>Status:</strong> {game.status}
            </p>
            <p>
              <strong>Current Round:</strong> {currentRound?.roundNumber || 'Not started'}
            </p>
            <p>
              <strong>Cycle:</strong> {speakerProgress.currentCycle || 1} / {speakerProgress.targetCycles || game.targetCycles || 2}
            </p>
            <p>
              <strong>Speakers Completed:</strong> {speakerProgress.completedSpeakers} / {speakerProgress.totalPlayers || players.length}
            </p>
          </div>

          {!speakersDone && currentRound ? (
            <div className="card">
              <h2>Game Phase</h2>
              <p>
                <strong>Phase:</strong> {currentRound.phase}
              </p>
              <p>
                <strong>Speaker ID:</strong> {currentRound.speakerId}
              </p>
              <div className="button-group">
                {currentRound.phase === 'STATEMENT_SUBMISSION' && (
                  <button onClick={() => navigate(`/game/${gameId}/statement-submission/${currentRound.id}`)}>
                    Go to Statement Submission
                  </button>
                )}
                {currentRound.phase === 'DISCUSSION' && (
                  <button onClick={() => navigate(`/game/${gameId}/discussion/${currentRound.id}`)}>
                    Go to Discussion
                  </button>
                )}
                {currentRound.phase === 'VOTING' && (
                  <button onClick={() => navigate(`/game/${gameId}/voting/${currentRound.id}`)}>
                    Go to Voting
                  </button>
                )}
                {currentRound.phase === 'RESULTS' && (
                  <>
                    <button onClick={() => navigate(`/game/${gameId}/results/${currentRound.id}`)}>
                      View Results
                    </button>
                      {!speakersDone && (
                      <button onClick={handleStartRound} className="primary-button">
                          Start Next Round (Speaker {speakerProgress.completedSpeakers + 1}/{speakerProgress.totalPlayers || players.length})
                      </button>
                    )}
                  </>
                )}
              </div>
            </div>
          ) : currentRound === null ? (
            <div className="card">
              <h2>Game Not Started</h2>
              <button onClick={handleStartRound} className="primary-button">
                Start First Round (Speaker 1/{speakerProgress.totalPlayers || players.length})
              </button>
            </div>
          ) : null}

          {speakersDone && (
            <div className="card" style={{ backgroundColor: '#fff3cd', borderLeft: '4px solid #ffc107' }}>
              <h2>✓ All Speakers Complete!</h2>
              <p style={{ fontSize: '0.9em', color: '#666', marginBottom: '15px' }}>
                All players have had a turn as speakers. The game is complete!
              </p>
              <button
                onClick={() => setShowRankings(!showRankings)}
                className="primary-button"
                style={{ marginBottom: '15px' }}
              >
                {showRankings ? 'Hide' : 'View'} Final Rankings
              </button>
            </div>
          )}

          {showRankings && rankings.length > 0 && (
            <div className="card">
              <h2>Final Rankings</h2>
              <table
                style={{
                  width: '100%',
                  borderCollapse: 'collapse',
                  marginTop: '15px',
                }}
              >
                <thead>
                  <tr style={{ backgroundColor: '#f0f0f0', borderBottom: '2px solid #ddd' }}>
                    <th style={{ padding: '10px', textAlign: 'left' }}>Rank</th>
                    <th style={{ padding: '10px', textAlign: 'left' }}>Player</th>
                    <th style={{ padding: '10px', textAlign: 'right' }}>Score</th>
                  </tr>
                </thead>
                <tbody>
                  {rankings.map((rank, index) => (
                    <tr key={rank.playerId} style={{ borderBottom: '1px solid #eee' }}>
                      <td style={{ padding: '10px' }}>
                        <strong>#{index + 1}</strong>
                      </td>
                      <td style={{ padding: '10px' }}>{rank.playerName}</td>
                      <td style={{ padding: '10px', textAlign: 'right' }}>
                        <strong>{rank.score}</strong>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          <div className="card">
            <h2>Players</h2>
            {players.length > 0 ? (
              <ul>
                {players.map((player, index) => (
                  <li key={player.id}>
                    #{player.id} - {player.username || player.user?.username || 'Unknown'}
                    {index < speakerProgress.completedSpeakers && (
                      <span style={{ fontSize: '0.9em', color: '#666', marginLeft: '8px' }}>(Speaker ✓)</span>
                    )}
                    {players.length > 0 && index === speakerProgress.completedSpeakers % players.length && currentRound && !speakersDone && (
                      <span
                        style={{
                          fontSize: '0.9em',
                          color: '#2196F3',
                          marginLeft: '8px',
                          fontWeight: 'bold',
                        }}
                      >
                        (Current Speaker)
                      </span>
                    )}
                  </li>
                ))}
              </ul>
            ) : (
              <p>No players in this room</p>
            )}
          </div>
        </>
      )}
    </div>
  )
}
