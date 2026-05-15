function HomePage() {
  return (
    <section className="page">
      <h2>Frontend Started</h2>
      <p>
        Welcome to Lie Detector Arena.
      </p>
      <div className="cards-row">
        <article className="card">
          <h3>Users</h3>
          <p>Create and list platform users, and see whether a player has been created for them.</p>
        </article>
        <article className="card">
          <h3>Players</h3>
          <p>Create players from existing users before they join a room.</p>
        </article>
        <article className="card">
          <h3>Rooms</h3>
          <p>Create game rooms and join players to them while respecting capacity limits.</p>
        </article>
        <article className="card">
          <h3>Games</h3>
          <p>Start a game in a selected room only when enough players have joined.</p>
        </article>
        <article className="card">
          <h3>Rounds & Statements</h3>
          <p>Create rounds for a game, then submit and view statements per round context.</p>
        </article>
      </div>
    </section>
  )
}

export default HomePage