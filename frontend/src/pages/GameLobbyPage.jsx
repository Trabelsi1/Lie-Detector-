import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { getGameById, startRound, getCurrentRound } from '../services/gamesApi'
import { getRoomById } from '../services/roomsApi'

export default function GameLobbyPage() {
  const { gameId } = useParams()
  const navigate = useNavigate()

  const [game, setGame] = useState(null)
  const [currentRound, setCurrentRound] = useState(null)
  const [room, setRoom] = useState(null)
  const [players, setPlayers] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    loadGameData()
  }, [gameId])

  async function loadGameData() {
    try {
      setLoading(true)
      const gameData = await getGameById(gameId)
      setGame(gameData)

      // Load room with players
      if (gameData.gameRoom?.id) {
        const roomData = await getRoomById(gameData.gameRoom.id)
        setRoom(roomData)
        setPlayers(roomData.players || [])
      }

      // Load current round
      const round = await getCurrentRound(gameId)
      setCurrentRound(round)

      setError('')
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load game data')
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
      setError(err.response?.data?.message || 'Failed to start round')
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
              <strong>Room:</strong> {room?.code || '#' + room?.id}
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
          </div>

          <div className="card">
            <h2>Game Phase</h2>
            {currentRound ? (
              <>
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
                    <button onClick={() => navigate(`/game/${gameId}/results/${currentRound.id}`)}>
                      View Results
                    </button>
                  )}
                </div>
              </>
            ) : (
              <button onClick={handleStartRound} className="primary-button">
                Start New Round
              </button>
            )}
          </div>

          <div className="card">
            <h2>Players</h2>
            {players.length > 0 ? (
              <ul>
                {players.map((player) => (
                  <li key={player.id}>
                    #{player.id} - {player.user?.username || 'Unknown'}
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
