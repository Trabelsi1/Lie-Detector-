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

  const selectedRoom = useMemo(
    () => rooms.find((room) => String(room.id) === String(selectedRoomId)),
    [rooms, selectedRoomId],
  )

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
    // Load current player ID from localStorage (should be set after player selection)
    const playerId = localStorage.getItem('currentPlayerId')
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
      const createdGame = await createGame({
        status: 'CREATED',
        currentRoundIndex: 0,
        gameRoom: { id: Number(selectedRoom.id) },
      })
      setMessage(`Game #${createdGame.id ?? 'N/A'} created successfully! Entering game lobby...`)
      
      // Navigate to game lobby after a short delay
      setTimeout(() => {
        navigate(`/game/${createdGame.id}/lobby/${createdGame.id}`)
      }, 1500)
      
      await loadGamesForRoom(selectedRoom.id)
      await loadRooms()
    } catch (apiError) {
      setError(apiError.message || 'Failed to create game')
    }
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

        <button type="submit">Start game</button>
      </form>

      {error ? <p className="feedback error">{error}</p> : null}
      {message ? <p className="feedback success">{message}</p> : null}

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
            </li>
          ))}
        </ul>
      </section>
    </section>
  )
}

export default GameStartPage
