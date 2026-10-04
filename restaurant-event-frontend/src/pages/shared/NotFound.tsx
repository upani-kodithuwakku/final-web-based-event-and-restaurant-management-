import { Link } from 'react-router-dom';

export default function NotFound() {
  return (
    <div className="empty page-enter" style={{ minHeight: '60vh', justifyContent: 'center' }}>
      <div className="empty-icon">✧</div>
      <h3 style={{ fontSize: 28 }}>Page not found</h3>
      <p>The page you're looking for doesn't exist or has moved.</p>
      <Link className="button primary" to="/" style={{ marginTop: 8 }}>
        Back to Gather
      </Link>
    </div>
  );
}
