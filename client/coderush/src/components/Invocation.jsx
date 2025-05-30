// pages/invocation/[questionId].js
import { useState } from 'react';
import axios from 'axios';
import { getCookie } from 'cookies-next';

const Invocation = ({ questionId }) => {
  const [status, setStatus] = useState('idle');
  const [error, setError] = useState(null);
  const baseUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';


  const invokeQuestion = async () => {
    setStatus('loading');
    setError(null);
    try {
      // Retrieve token from cookies
      const token = getCookie('token');
      const headers = {};
      if (token) {
        headers.Authorization = `Bearer ${token}`;
      }

      const response = await axios.get(
        `${baseUrl}/api/questions/invocation/${questionId}`,
        { headers }
      );

      if (response.status === 200) {
        setStatus('success');
      } else {
        setStatus('error');
        setError(`Unexpected response: ${response.status}`);
      }
    } catch (err) {
      setStatus('error');
      setError(err.response?.data || err.message);
    }
  };

  return (
    <div style={{ padding: '2rem' }}>
      <h1>Invoke Question</h1>
      <p>Question ID: {questionId}</p>

      <button
        onClick={invokeQuestion}
        disabled={status === 'loading'}
        style={{ padding: '0.5rem 1rem', fontSize: '1rem' }}
      >
        {status === 'loading' ? 'Invoking...' : 'Invoke Now'}
      </button>

      {status === 'success' && <p style={{ color: 'green' }}>Invocation sent successfully.</p>}
      {status === 'error' && <p style={{ color: 'red' }}>Error: {error}</p>}
    </div>
  );
};

export async function getServerSideProps(context) {
  const { questionId } = context.params;
  return { props: { questionId } };
}

export default Invocation;
