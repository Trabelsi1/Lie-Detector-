import { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { createGame, getGamesByRoomId } from '../services/gamesApi'
import { getRooms } from '../services/roomsApi'

const MIN_PLAYERS_TO_START = 2

function GameStartPage() {
  const navigate = useNavigate()
  const [rooms, setRooms] = useState([])
  const [games, setGames] = useState([])
  const [selectedRoomId, setSelectedRoomId] = useState('')
  const [currentPlayerId, setCurrentPlayerId] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const [targetCycles, setTargetCycles] = useState(2)

  const selectedRoom = useMemo(
    () => rooms.find((room) => String(room.id) === String(selectedRoomId)),
    [rooms, selectedRoomId],
  )

  const isCurrentPlayerInSelectedRoom = useMemo(() => {
    if (!selectedRoom || !currentPlayerId) return false
    return Array.isArray(selectedRoom.players)
      ? selectedRoom.players.some((player) => String(player?.id) === String(currentPlayerId))
      : false
  }, [selectedRoom, currentPlayerId])

  async function loadRooms() {
    try {
      setLoading(true)
      setError('')
      const data = await getRooms()
      setRooms(Array.isArray(data) ? data : [])
    } catch (apiError) {
      setError(apiError.message || 'Failed to load rooms')
    } finally {
      setLoading(false)
    }
  }

  async function loadGamesForRoom(roomId) {
    if (!roomId) {
      setGames([])
      return
    }

    try {
      const data = await getGamesByRoomId(Number(roomId))
      setGames(Array.isArray(data) ? data : [])
    } catch (apiError) {
      setError(apiError.message || 'Failed to load games for room')
      setGames([])
    }
  }

  useEffect(() => {
    loadRooms()
    // Load current player ID from sessionStorage (per-tab), fallback to old localStorage values.
    const playerId = sessionStorage.getItem('currentPlayerId') || localStorage.getItem('currentPlayerId')
    if (playerId) {
      setCurrentPlayerId(playerId)
    }
  }, [])

  useEffect(() => {
    loadGamesForRoom(selectedRoomId)
  }, [selectedRoomId])

  async function onCreateGame(event) {
    event.preventDefault()

    if (!selectedRoom) {
      setError('Please select a room first')
      return
    }

    const currentPlayers = selectedRoom.players?.length ?? 0
    if (currentPlayers < MIN_PLAYERS_TO_START) {
      setError(`Not enough players to start a game. Need at least ${MIN_PLAYERS_TO_START}.`)
      return
    }

    try {
      setError('')
      setMessage('')
      const normalizedCycles = Math.max(1, Math.min(10, Number(targetCycles) || 2))

      const createdGame = await createGame({
        status: 'CREATED',
        currentRoundIndex: 0,
        targetCycles: normalizedCycles,
        gameRoom: { id: Number(selectedRoom.id) },
      })
      setMessage(`Game #${createdGame.id ?? 'N/A'} created successfully! Entering game lobby...`)
      
      // Navigate to game lobby after a short delay
      setTimeout(() => {
        navigate(`/game/${createdGame.id}/lobby`)
      }, 1500)
      
      await loadGamesForRoom(selectedRoom.id)
      await loadRooms()
    } catch (apiError) {
      setError(apiError.message || 'Failed to create game')
    }
  }

  function handleJoinGame(gameId) {
    if (!isCurrentPlayerInSelectedRoom) {
      setError('You must be a player in this room before joining its game')
      return
    }
    navigate(`/game/${gameId}/lobby`)
  }

  function isGameJoinable(game) {
    const status = String(game?.status || '').toUpperCase()
    return status !== 'FINISHED' && status !== 'COMPLETED' && status !== 'ENDED'
  }

  return (
    <section className="page">
      <h2>Start Game in a Room</h2>

      <form className="card form" onSubmit={onCreateGame}>
        <h3>Create game</h3>
        <label>
          Room (ID - code)
          <select value={selectedRoomId} onChange={(event) => setSelectedRoomId(event.target.value)}>
            <option value="">Select a room</option>
            {rooms.map((room) => (
              <option key={room.id} value={room.id}>
                #{room.id} - {room.roomCode}
              </option>
            ))}
          </select>
        </label>

        {selectedRoom ? (
          <p className="hint">
            Room status: {selectedRoom.status} | Players: {selectedRoom.players?.length ?? 0}/{selectedRoom.maxPlayers}
          </p>
        ) : null}

        <label>
          Number of cycles (full speaker rotations)
          <input
            type="number"
            min="1"
            max="10"
            value={targetCycles}
            onChange={(event) => setTargetCycles(event.target.value)}
          />
        </label>
        <p className="hint">Default: 2 cycles, max: 10 cycles</p>

        <button type="submit">Start game</button>
      </form>

      {error ? <p className="feedback error">{error}</p> : null}
      {message ? <p className="feedback success">{message}</p> : null}
      {currentPlayerId ? <p className="hint">Current player in this tab: #{currentPlayerId}</p> : null}

      <section className="card">
        <h3>Games in selected room</h3>
        {loading ? <p>Loading rooms...</p> : null}
        {selectedRoomId && games.length === 0 ? <p>No games found for this room.</p> : null}
        {!selectedRoomId ? <p>Select a room to view its games.</p> : null}
        <ul className="list">
          {games.map((game) => (
            <li key={game.id}>
              <strong>Game #{game.id ?? 'N/A'}</strong>
              <span>Status: {game.status ?? 'N/A'}</span>
              <span>Current round index: {game.currentRoundIndex ?? 0}</span>
              {isGameJoinable(game) ? (
                <button
                  type="button"
                  onClick={() => handleJoinGame(game.id)}
                  disabled={!isCurrentPlayerInSelectedRoom}
                >
                  Join Lobby
                </button>
              ) : (
                <span>Game is finished</span>
              )}
            </li>
          ))}
        </ul>
      </section>
    </section>
  )
}

export default GameStartPage
