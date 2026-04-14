import { useEffect, useState } from 'react'
import { createRoom, getRooms, joinPlayerToRoom } from '../services/roomsApi'
import { getPlayers } from '../services/playersApi'

const initialRoomForm = {
  roomCode: '',
  status: 'OPEN',
  maxPlayers: 6,
}

const initialAssignForm = {
  roomId: '',
  playerId: '',
}

function RoomsPage() {
  const [rooms, setRooms] = useState([])
  const [players, setPlayers] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const [roomForm, setRoomForm] = useState(initialRoomForm)
  const [assignForm, setAssignForm] = useState(initialAssignForm)

  async function loadRooms() {
    try {
      setLoading(true)
      setError('')
      const data = await getRooms()
      setRooms(data)
    } catch (apiError) {
      setError(apiError.message || 'Failed to load rooms')
    } finally {
      setLoading(false)
    }
  }

  async function loadPlayers() {
    try {
      const data = await getPlayers()
      setPlayers(Array.isArray(data) ? data : [])
    } catch (apiError) {
      setError(apiError.message || 'Failed to load players')
    }
  }

  useEffect(() => {
    loadRooms()
    loadPlayers()
  }, [])

  async function onCreateRoom(event) {
    event.preventDefault()

    if (!roomForm.roomCode) {
      setError('Room code is required')
      return
    }

    try {
      setError('')
      setMessage('')
      await createRoom({
        ...roomForm,
        maxPlayers: Number(roomForm.maxPlayers),
      })
      setRoomForm(initialRoomForm)
      setMessage('Room created')
      await loadRooms()
    } catch (apiError) {
      setError(apiError.message || 'Failed to create room')
    }
  }

  async function onAssignUser(event) {
    event.preventDefault()

    if (!assignForm.roomId || !assignForm.playerId) {
      setError('Room ID and Player ID are required')
      return
    }

    try {
      setError('')
      setMessage('')
      await joinPlayerToRoom(Number(assignForm.roomId), Number(assignForm.playerId))
      setAssignForm(initialAssignForm)
      setMessage('Player joined room')
      await loadRooms()
    } catch (apiError) {
      setError(apiError.message || 'Failed to add player to room')
    }
  }

  return (
    <section className="page">
      <h2>Rooms</h2>

      <div className="cards-row">
        <form className="card form" onSubmit={onCreateRoom}>
          <h3>Create a room</h3>
          <label>
            Room code
            <input
              value={roomForm.roomCode}
              onChange={(event) => setRoomForm((prev) => ({ ...prev, roomCode: event.target.value }))}
              placeholder="ROOM-42"
            />
          </label>
          <label>
            Status
            <input
              value={roomForm.status}
              onChange={(event) => setRoomForm((prev) => ({ ...prev, status: event.target.value }))}
              placeholder="OPEN"
            />
          </label>
          <label>
            Max players
            <input
              type="number"
              min="2"
              value={roomForm.maxPlayers}
              onChange={(event) => setRoomForm((prev) => ({ ...prev, maxPlayers: event.target.value }))}
            />
          </label>
          <button type="submit">Create room</button>
        </form>

        <form className="card form" onSubmit={onAssignUser}>
          <h3>Join player to room</h3>
          <p className="hint">Create the player first on the Players page.</p>
          <label>
            Room (ID - code)
            <select
              value={assignForm.roomId}
              onChange={(event) => setAssignForm((prev) => ({ ...prev, roomId: event.target.value }))}
            >
              <option value="">Select a room</option>
              {rooms.map((room) => (
                <option key={room.id} value={room.id}>
                  #{room.id} - {room.roomCode}
                </option>
              ))}
            </select>
          </label>
          <label>
            Player (ID - user)
            <select
              value={assignForm.playerId}
              onChange={(event) => setAssignForm((prev) => ({ ...prev, playerId: event.target.value }))}
            >
              <option value="">Select a player</option>
              {players.map((player) => (
                <option key={player.id} value={player.id}>
                  #{player.id} - {player.user?.username ?? 'Unknown user'}
                </option>
              ))}
            </select>
          </label>
          <button type="submit">Join player</button>
        </form>
      </div>

      {error ? <p className="feedback error">{error}</p> : null}
      {message ? <p className="feedback success">{message}</p> : null}

      <section className="card">
        <h3>Rooms list</h3>
        {loading ? <p>Loading rooms...</p> : null}
        {!loading && rooms.length === 0 ? <p>No rooms found.</p> : null}
        <ul className="list">
          {rooms.map((room) => (
            <li key={room.id || room.roomCode}>
              <strong>
                #{room.id ?? 'N/A'} {room.roomCode}
              </strong>
              <span>
                Status: {room.status} - Players: {room.players?.length ?? 0}/{room.maxPlayers}
              </span>
              {Array.isArray(room.players) && room.players.length > 0 ? (
                <span>
                  Members: {room.players.map((player) => `#${player.id}`).join(', ')}
                </span>
              ) : null}
            </li>
          ))}
        </ul>
      </section>
    </section>
  )
}

export default RoomsPage