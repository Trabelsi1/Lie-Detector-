import { Link } from 'react-router-dom'

function NotFoundPage() {
  return (
    <section className="page">
      <h2>Page not found</h2>
      <p>This route does not exist in the current frontend iteration.</p>
      <Link to="/">Go back home</Link>
    </section>
  )
}

export default NotFoundPage