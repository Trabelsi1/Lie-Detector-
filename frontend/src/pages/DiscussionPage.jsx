import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { getRoundById, advancePhase } from '../services/roundsApi'
import { getStatementsByRoundId } from '../services/statementsApi'
import { getMessagesByRoundId, createMessage } from '../services/messagesApi'

export default function DiscussionPage() {
  const { gameId, roundId } = useParams()
  const navigate = useNavigate()

  const [round, setRound] = useState(null)
  const [statements, setStatements] = useState([])
  const [messages, setMessages] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const [messageContent, setMessageContent] = useState('')

  const [currentPlayerId] = useState(
    Number(sessionStorage.getItem('currentPlayerId') || localStorage.getItem('currentPlayerId')) || null,
  )

  useEffect(() => {
    loadRoundData()
  }, [roundId])

  useEffect(() => {
    const intervalId = setInterval(async () => {
      try {
        const latestRound = await getRoundById(roundId)
        if (!latestRound) return

        setRound(latestRound)

        const msg = await getMessagesByRoundId(roundId)
        setMessages(msg)

        if (latestRound.phase === 'VOTING') {
          navigate(`/game/${gameId}/voting/${roundId}`)
        } else if (latestRound.phase === 'RESULTS') {
          navigate(`/game/${gameId}/results/${roundId}`)
        }
      } catch {
        // keep polling silently
      }
    }, 2000)

    return () => clearInterval(intervalId)
  }, [gameId, roundId, navigate])

  async function loadRoundData() {
    try {
      setLoading(true)
      const roundData = await getRoundById(roundId)
      setRound(roundData)

      const stmts = await getStatementsByRoundId(roundId)
      setStatements(Array.isArray(stmts) ? stmts.filter(Boolean) : [])

      const msgs = await getMessagesByRoundId(roundId)
      setMessages(Array.isArray(msgs) ? msgs.filter(Boolean) : [])
    } catch (err) {
      setError(err.message || 'Failed to load round data')
    } finally {
      setLoading(false)
    }
  }

  async function handleSendMessage(e) {
    e.preventDefault()
    try {
      if (!messageContent.trim()) {
        setError('Message cannot be empty')
        return
      }

      if (!currentPlayerId || !round) {
        setError('Missing player or round information')
        return
      }

      const newMessage = await createMessage({
        content: messageContent,
        sender: { id: currentPlayerId },
        round: { id: Number(roundId) },
      })

      setMessages((currentMessages) => [...(Array.isArray(currentMessages) ? currentMessages : []), newMessage])
      setMessageContent('')
      setError('')
    } catch (err) {
      setError(err.message || 'Failed to send message')
    }
  }

  async function handleAdvanceToVoting() {
    try {
      const updatedRound = await advancePhase(roundId)
      setRound(updatedRound)
      navigate(`/game/${gameId}/voting/${roundId}`)
    } catch (err) {
      setError(err.message || 'Failed to advance phase')
    }
  }

  if (loading) return <div className="page-container">Loading...</div>

  return (
    <div className="page-container">
      <h1>Discussion Phase</h1>

      {error && <div className="error-message">{error}</div>}

      {round && (
        <>
          <div className="card">
            <h2>Round Information</h2>
            <p>
              <strong>Round Number:</strong> {round.roundNumber}
            </p>
            <p>
              <strong>Phase:</strong> {round.phase}
            </p>
          </div>

          <div className="card">
            <h2>Statements to Discuss</h2>
            <p style={{ fontStyle: 'italic', marginBottom: '15px' }}>
              Review these statements and discuss which one is the lie:
            </p>
            {statements.length > 0 ? (
              <ol>
                {statements.map((stmt) => (
                  <li key={stmt.id} style={{ marginBottom: '15px' }}>
                    <div>{stmt.content}</div>
                  </li>
                ))}
              </ol>
            ) : (
              <p>No statements found</p>
            )}
          </div>

          <div className="card">
            <h2>Discussion Chat</h2>
            <div
              style={{
                border: '1px solid #ddd',
                borderRadius: '4px',
                padding: '12px',
                height: '300px',
                overflowY: 'auto',
                backgroundColor: '#f9f9f9',
                marginBottom: '15px',
              }}
            >
              {(Array.isArray(messages) ? messages : []).length > 0 ? (
                (Array.isArray(messages) ? messages : []).map((msg) => (
                  <div key={msg.id} style={{ marginBottom: '12px', paddingBottom: '8px', borderBottom: '1px solid #eee' }}>
                    <div style={{ fontSize: '0.9em', color: '#666' }}>
                      <strong>{msg.senderName || `Player #${msg.senderId}`}</strong>
                      {msg.sentAt && <span style={{ marginLeft: '8px' }}>({new Date(msg.sentAt).toLocaleTimeString()})</span>}
                    </div>
                    <div style={{ marginTop: '4px' }}>{msg.content}</div>
                  </div>
                ))
              ) : (
                <p style={{ color: '#999', fontStyle: 'italic' }}>No messages yet. Start the discussion!</p>
              )}
            </div>

            <form onSubmit={handleSendMessage}>
              <div style={{ display: 'flex', gap: '8px' }}>
                <textarea
                  value={messageContent}
                  onChange={(e) => setMessageContent(e.target.value)}
                  placeholder="Type your message..."
                  rows="2"
                  style={{ flex: 1 }}
                />
                <button type="submit" className="primary-button" style={{ alignSelf: 'flex-start' }}>
                  Send
                </button>
              </div>
            </form>
          </div>

          <button onClick={handleAdvanceToVoting} className="primary-button">
            Discussion Over - Go to Voting
          </button>

          <button onClick={() => navigate(`/game/${gameId}/lobby`)}>Back to Lobby</button>
        </>
      )}
    </div>
  )
}
