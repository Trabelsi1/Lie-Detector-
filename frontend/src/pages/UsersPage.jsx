import { useEffect, useState } from 'react'
import { createUser, getUsers } from '../services/usersApi'

const initialForm = {
  username: '',
  email: '',
  password: '',
}

const usernameRegex = /^[A-Za-z0-9]+$/
const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/

function UsersPage() {
  const [users, setUsers] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [form, setForm] = useState(initialForm)

  async function loadUsers() {
    try {
      setLoading(true)
      setError('')
      const data = await getUsers()
      setUsers(data)
    } catch (apiError) {
      setError(apiError.message || 'Failed to load users')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadUsers()
  }, [])

  async function onSubmit(event) {
    event.preventDefault()

    const username = form.username.trim()
    const email = form.email.trim()

    if (!username || !email) {
      setError('Username and email are required')
      return
    }

    if (!usernameRegex.test(username)) {
      setError('Username must be alphanumeric only (letters and digits)')
      return
    }

    if (!emailRegex.test(email)) {
      setError('Please enter a valid email address format')
      return
    }

    try {
      setError('')
      await createUser({
        ...form,
        username,
        email,
      })
      setForm(initialForm)
      await loadUsers()
    } catch (apiError) {
      setError(apiError.message || 'Failed to create user')
    }
  }

  return (
    <section className="page">
      <h2>Users</h2>

      <form className="card form" onSubmit={onSubmit}>
        <h3>Create a user</h3>
        <label>
          Username
          <input
            value={form.username}
            onChange={(event) => setForm((prev) => ({ ...prev, username: event.target.value }))}
            placeholder="alice"
            pattern="[A-Za-z0-9]+"
            title="Use only letters and digits"
          />
        </label>
        <label>
          Email
          <input
            type="email"
            value={form.email}
            onChange={(event) => setForm((prev) => ({ ...prev, email: event.target.value }))}
            placeholder="alice@example.com"
            title="Use a valid email format like name@example.com"
          />
        </label>
        <label>
          Password
          <input
            type="password"
            value={form.password}
            onChange={(event) => setForm((prev) => ({ ...prev, password: event.target.value }))}
            placeholder="optional for now"
          />
        </label>
        <button type="submit">Create</button>
      </form>

      {error ? <p className="feedback error">{error}</p> : null}

      <section className="card">
        <h3>Users list</h3>
        {loading ? <p>Loading users...</p> : null}
        {!loading && users.length === 0 ? <p>No users found.</p> : null}
        <ul className="list">
          {users.map((user) => (
            <li key={user.id || user.email}>
              <strong>
                #{user.id ?? 'N/A'} {user.username}
              </strong>
              <span>{user.email}</span>
              <span>
                Player: {user.player?.id ? `#${user.player.id}` : 'not created yet'}
              </span>
            </li>
          ))}
        </ul>
      </section>
    </section>
  )
}

export default UsersPage