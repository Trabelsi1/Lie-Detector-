import { Navigate, Route, Routes } from 'react-router-dom'
import GameStartPage from './pages/GameStartPage'
import GameLobbyPage from './pages/GameLobbyPage'
import StatementSubmissionPage from './pages/StatementSubmissionPage'
import DiscussionPage from './pages/DiscussionPage'
import VotingPage from './pages/VotingPage'
import ResultsPage from './pages/ResultsPage'
import MainLayout from './layout/MainLayout'
import HomePage from './pages/HomePage'
import PlayersPage from './pages/PlayersPage'
import RoomsPage from './pages/RoomsPage'
import UsersPage from './pages/UsersPage'
import NotFoundPage from './pages/NotFoundPage'

function App() {
  return (
    <Routes>
      <Route element={<MainLayout />}>
        <Route path="/" element={<HomePage />} />
        <Route path="/users" element={<UsersPage />} />
        <Route path="/players" element={<PlayersPage />} />
        <Route path="/rooms" element={<RoomsPage />} />
        <Route path="/games" element={<GameStartPage />} />
        <Route path="/game/:gameId/lobby/:gameId" element={<GameLobbyPage />} />
        <Route path="/game/:gameId/statement-submission/:roundId" element={<StatementSubmissionPage />} />
        <Route path="/game/:gameId/discussion/:roundId" element={<DiscussionPage />} />
        <Route path="/game/:gameId/voting/:roundId" element={<VotingPage />} />
        <Route path="/game/:gameId/results/:roundId" element={<ResultsPage />} />
        <Route path="/home" element={<Navigate to="/" replace />} />
      </Route>
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  )
}

export default App
