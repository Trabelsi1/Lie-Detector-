import { useEffect, useState } from 'react'
import { createPlayerFromUser, getPlayers } from '../services/playersApi'
import { getUsers } from '../services/usersApi'

const initialForm = {
  userId: '',
}

function PlayersPage() {
  const [players, setPlayers] = useState([])
  const [users, setUsers] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const [form, setForm] = useState(initialForm)
  const [selectedPlayerId, setSelectedPlayerId] = useState(localStorage.getItem('currentPlayerId') || null)

  async function loadPlayers() {
    try {
      setLoading(true)
      setError('')
      const data = await getPlayers()
      setPlayers(data)
    } catch (apiError) {
      setError(apiError.message || 'Failed to load players')
    } finally {
      setLoading(false)
    }
  }

  async function loadUsers() {
    try {
      const data = await getUsers()
      setUsers(Array.isArray(data) ? data : [])
    } catch (apiError) {
      setError(apiError.message || 'Failed to load users')
    }
  }

  useEffect(() => {
    loadPlayers()
    loadUsers()
  }, [])

  async function onSubmit(event) {
    event.preventDefault()

    if (!form.userId) {
      setError('User ID is required')
      return
    }

    try {
      setError('')
      setMessage('')
      const newPlayer = await createPlayerFromUser(Number(form.userId))
      setForm(initialForm)
      setMessage('Player created')
      setSelectedPlayerId(newPlayer.id)
      localStorage.setItem('currentPlayerId', newPlayer.id)
      await loadPlayers()
    } catch (apiError) {
      setError(apiError.message || 'Failed to create player')
    }
  }

  function handleSelectAsMe(playerId) {
    setSelectedPlayerId(playerId)
    localStorage.setItem('currentPlayerId', playerId)
    setMessage(`You are now player #${playerId}`)
  }

  return (
    <section className="page">
      <h2>Players</h2>

      <form className="card form" onSubmit={onSubmit}>
        <h3>Create a player from a user</h3>
        <label>
          User (ID - name)
          <select
            value={form.userId}
            onChange={(event) => setForm((prev) => ({ ...prev, userId: event.target.value }))}
          >
            <option value="">Select a user</option>
            {users.map((user) => (
              <option key={user.id} value={user.id}>
                #{user.id} - {user.username}
              </option>
            ))}
          </select>
        </label>
        <button type="submit">Create player</button>
      </form>

      {error ? <p className="feedback error">{error}</p> : null}
      {message ? <p className="feedback success">{message}</p> : null}

      {selectedPlayerId && (
        <div className="card" style={{ backgroundColor: '#e8f5e9' }}>
          <p>
            <strong>Your current player ID:</strong> {selectedPlayerId}
          </p>
        </div>
      )}

      <section className="card">
        <h3>Players list</h3>
        {loading ? <p>Loading players...</p> : null}
        {!loading && players.length === 0 ? <p>No players found.</p> : null}
        <ul className="list">
          {players.map((player) => (
            <li
              key={player.id || player.user?.id}
              style={{
                backgroundColor: selectedPlayerId === String(player.id) ? '#fff3e0' : 'transparent',
              }}
            >
              <div style={{ marginBottom: '10px' }}>
                <strong>
                  Player #{player.id ?? 'N/A'}
                  {selectedPlayerId === String(player.id) && ' (YOU)'}
                </strong>
              </div>
              <span>
                User: #{player.user?.id ?? 'N/A'} {player.user?.username ?? 'Unknown'}
              </span>
              <span>Profile: {player.profile?.id ? `#${player.profile.id}` : 'missing'}</span>
              <button
                onClick={() => handleSelectAsMe(player.id)}
                style={{
                  marginTop: '10px',
                  padding: '5px 10px',
                  backgroundColor: selectedPlayerId === String(player.id) ? '#4CAF50' : '#2196F3',
                  color: 'white',
                  border: 'none',
                  borderRadius: '3px',
                  cursor: 'pointer',
                }}
              >
                {selectedPlayerId === String(player.id) ? 'Selected' : 'Select as Me'}
              </button>
            </li>
          ))}
        </ul>
      </section>
    </section>
  )
}

export default PlayersPage