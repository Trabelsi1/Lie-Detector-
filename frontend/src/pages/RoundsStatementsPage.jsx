import { useEffect, useState } from 'react'
import { getGames } from '../services/gamesApi'
import { createRound, getRoundsByGameId } from '../services/roundsApi'
import { createStatement, getStatementsByRoundId } from '../services/statementsApi'

const initialRoundForm = {
  roundNumber: 1,
  phase: 'STATEMENT',
  speakerId: '',
}

const initialStatementForm = {
  content: '',
  isLie: false,
  position: 1,
}

function RoundsStatementsPage() {
  const [games, setGames] = useState([])
  const [selectedGameId, setSelectedGameId] = useState('')
  const [rounds, setRounds] = useState([])
  const [selectedRoundId, setSelectedRoundId] = useState('')
  const [statements, setStatements] = useState([])
  const [roundForm, setRoundForm] = useState(initialRoundForm)
  const [statementForm, setStatementForm] = useState(initialStatementForm)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')

  async function loadGames() {
    try {
      const data = await getGames()
      setGames(Array.isArray(data) ? data : [])
    } catch (apiError) {
      setError(apiError.message || 'Failed to load games')
    }
  }

  async function loadRounds(gameId) {
    if (!gameId) {
      setRounds([])
      return
    }

    try {
      const data = await getRoundsByGameId(Number(gameId))
      setRounds(Array.isArray(data) ? data : [])
    } catch (apiError) {
      setError(apiError.message || 'Failed to load rounds')
      setRounds([])
    }
  }

  async function loadStatements(roundId) {
    if (!roundId) {
      setStatements([])
      return
    }

    try {
      const data = await getStatementsByRoundId(Number(roundId))
      setStatements(Array.isArray(data) ? data : [])
    } catch (apiError) {
      setError(apiError.message || 'Failed to load statements')
      setStatements([])
    }
  }

  useEffect(() => {
    loadGames()
  }, [])

  useEffect(() => {
    setSelectedRoundId('')
    setStatements([])
    loadRounds(selectedGameId)
  }, [selectedGameId])

  useEffect(() => {
    loadStatements(selectedRoundId)
  }, [selectedRoundId])

  async function onCreateRound(event) {
    event.preventDefault()

    if (!selectedGameId) {
      setError('Please select a game first')
      return
    }

    if (!Number(roundForm.roundNumber) || Number(roundForm.roundNumber) < 1) {
      setError('Round number must be at least 1')
      return
    }

    try {
      setError('')
      setMessage('')
      const createdRound = await createRound({
        roundNumber: Number(roundForm.roundNumber),
        phase: roundForm.phase,
        speakerId: roundForm.speakerId ? Number(roundForm.speakerId) : null,
        game: { id: Number(selectedGameId) },
      })
      setMessage(`Round #${createdRound.id ?? 'N/A'} created`)
      setRoundForm(initialRoundForm)
      await loadRounds(selectedGameId)
      if (createdRound.id) {
        setSelectedRoundId(String(createdRound.id))
      }
    } catch (apiError) {
      setError(apiError.message || 'Failed to create round')
    }
  }

  async function onCreateStatement(event) {
    event.preventDefault()

    if (!selectedRoundId) {
      setError('Please select a round first')
      return
    }

    if (!statementForm.content.trim()) {
      setError('Statement content is required')
      return
    }

    if (!Number(statementForm.position) || Number(statementForm.position) < 1) {
      setError('Position must be at least 1')
      return
    }

    try {
      setError('')
      setMessage('')
      await createStatement({
        content: statementForm.content.trim(),
        lie: Boolean(statementForm.isLie),
        isLie: Boolean(statementForm.isLie),
        position: Number(statementForm.position),
        round: { id: Number(selectedRoundId) },
      })
      setMessage('Statement created')
      setStatementForm(initialStatementForm)
      await loadStatements(selectedRoundId)
    } catch (apiError) {
      setError(apiError.message || 'Failed to create statement')
    }
  }

  return (
    <section className="page">
      <h2>Rounds + Statements Flow</h2>

      <div className="cards-row">
        <form className="card form" onSubmit={onCreateRound}>
          <h3>Create round</h3>
          <label>
            Game (ID - status)
            <select value={selectedGameId} onChange={(event) => setSelectedGameId(event.target.value)}>
              <option value="">Select a game</option>
              {games.map((game) => (
                <option key={game.id} value={game.id}>
                  #{game.id} - {game.status ?? 'N/A'}
                </option>
              ))}
            </select>
          </label>
          <label>
            Round number
            <input
              type="number"
              min="1"
              value={roundForm.roundNumber}
              onChange={(event) => setRoundForm((prev) => ({ ...prev, roundNumber: event.target.value }))}
            />
          </label>
          <label>
            Phase
            <input
              value={roundForm.phase}
              onChange={(event) => setRoundForm((prev) => ({ ...prev, phase: event.target.value }))}
              placeholder="STATEMENT"
            />
          </label>
          <label>
            Speaker player ID (optional)
            <input
              type="number"
              min="1"
              value={roundForm.speakerId}
              onChange={(event) => setRoundForm((prev) => ({ ...prev, speakerId: event.target.value }))}
            />
          </label>
          <button type="submit">Create round</button>
        </form>

        <form className="card form" onSubmit={onCreateStatement}>
          <h3>Create statement</h3>
          <label>
            Round (ID - number)
            <select value={selectedRoundId} onChange={(event) => setSelectedRoundId(event.target.value)}>
              <option value="">Select a round</option>
              {rounds.map((round) => (
                <option key={round.id} value={round.id}>
                  #{round.id} - round {round.roundNumber}
                </option>
              ))}
            </select>
          </label>
          <label>
            Content
            <input
              value={statementForm.content}
              onChange={(event) => setStatementForm((prev) => ({ ...prev, content: event.target.value }))}
              placeholder="I was in Paris yesterday"
            />
          </label>
          <label>
            Position
            <input
              type="number"
              min="1"
              value={statementForm.position}
              onChange={(event) => setStatementForm((prev) => ({ ...prev, position: event.target.value }))}
            />
          </label>
          <label>
            Is lie?
            <select
              value={statementForm.isLie ? 'true' : 'false'}
              onChange={(event) =>
                setStatementForm((prev) => ({ ...prev, isLie: event.target.value === 'true' }))
              }
            >
              <option value="false">No</option>
              <option value="true">Yes</option>
            </select>
          </label>
          <button type="submit">Create statement</button>
        </form>
      </div>

      {error ? <p className="feedback error">{error}</p> : null}
      {message ? <p className="feedback success">{message}</p> : null}

      <section className="card">
        <h3>Statements in selected round</h3>
        {!selectedRoundId ? <p>Select a round to view its statements.</p> : null}
        {selectedRoundId && statements.length === 0 ? <p>No statements for this round yet.</p> : null}
        <ul className="list">
          {statements.map((statement) => (
            <li key={statement.id}>
              <strong>Statement #{statement.id ?? 'N/A'}</strong>
              <span>Position: {statement.position}</span>
              <span>Lie: {statement.lie ?? statement.isLie ? 'Yes' : 'No'}</span>
              <span>{statement.content}</span>
            </li>
          ))}
        </ul>
      </section>
    </section>
  )
}

export default RoundsStatementsPage
