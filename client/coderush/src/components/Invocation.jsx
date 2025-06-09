import { useState, useEffect, useRef } from 'react'
import axios from 'axios'
import { getCookie } from 'cookies-next'
import webSocketService from '@/services/webSocketService'

export default function Invocation({ questionId }) {
  const [status, setStatus]       = useState('idle')   // idle | waiting | done | error
  const [error, setError]         = useState(null)
  const [result, setResult]       = useState(null)
  const wsConnectedRef            = useRef(false)

  const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8084'
  const WS_URL  = `${process.env.NEXT_PUBLIC_WS_URL ||
                   'http://localhost:8084'}/ws`

  const token = getCookie('token')

  // cleanup on unmount
  useEffect(() => {
    return () => {
      webSocketService.unsubscribe(`/topic/invocation-result/${questionId}`)
      webSocketService.disconnect()
    }
  }, [questionId])

  const handleMessage = (payload) => {
    setResult(payload)
    setStatus('done')
    webSocketService.unsubscribe(`/topic/invocation-result/${questionId}`)
  }

  const invoke = async () => {
    setError(null)
    setResult(null)
    setStatus('waiting')

    try {
      // 1) connect WebSocket (if not already)
      if (!wsConnectedRef.current) {
        webSocketService.connect(WS_URL, token)
        wsConnectedRef.current = true
      }

      // 2) subscribe to the result topic
      webSocketService.subscribe(
        `/topic/invocation-result/${questionId}`,
        handleMessage
      )

      // 3) send invocation HTTP POST
      await axios.post(
        `${API_URL}/api/questions/invocation/${questionId}`,
        {},
        { headers: { Authorization: `Bearer ${token}` } }
      )
    } catch (err) {
      setError(err.response?.data || err.message)
      setStatus('error')
    }
  }

  return (
    <div className="max-w-3xl mx-auto p-6 space-y-6">
      <h1 className="text-2xl font-bold">Invoke Question #{questionId}</h1>

      <button
        onClick={invoke}
        disabled={status === 'waiting'}
        className={`px-4 py-2 rounded font-medium 
          ${status === 'waiting'
            ? 'bg-gray-400 cursor-not-allowed'
            : 'bg-blue-600 hover:bg-blue-700 text-white'}`}
      >
        {status === 'waiting' ? 'Invoking…' : 'Invoke Now'}
      </button>

      {status === 'waiting' && (
        <div className="text-gray-600">Waiting for result…</div>
      )}
      {error && (
        <div className="p-4 bg-red-100 text-red-800 rounded">
          Error: {error}
        </div>
      )}

      {result && (
        <div className="space-y-4">
          <div className="p-4 bg-green-50 rounded border border-green-200">
            <p className="text-lg">
              Verdict:
              <span className="font-semibold ml-2">{result.verdict}</span>
            </p>
            <p>
              Elapsed: <span className="ml-1">{result.elapsedMillis} ms</span>
            </p>
          </div>

          <table className="w-full table-auto border-collapse">
            <thead>
              <tr className="bg-gray-100">
                <th className="border px-3 py-2 text-left">Testcase</th>
                <th className="border px-3 py-2 text-left">Status</th>
                <th className="border px-3 py-2 text-left">Time (ms)</th>
                <th className="border px-3 py-2 text-left">Memory (B)</th>
              </tr>
            </thead>
            <tbody>
              {result?.testcaseResults?.map(tc => (
                <tr key={tc.testcaseId} className="hover:bg-gray-50">
                  <td className="border px-3 py-2">{tc.testcaseId}</td>
                  <td className="border px-3 py-2">
                    <span className={`px-2 py-0.5 rounded text-sm font-medium ${
                      tc.status === 'OK'
                        ? 'bg-green-100 text-green-800'
                        : tc.status.startsWith('TLE')
                          ? 'bg-yellow-100 text-yellow-800'
                          : 'bg-red-100 text-red-800'
                    }`}>
                      {tc.status}
                    </span>
                  </td>
                  <td className="border px-3 py-2">
                    {(tc.timeNano / 1e6).toFixed(2)}
                  </td>
                  <td className="border px-3 py-2">
                    {tc.memoryBytes.toLocaleString()}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}

export async function getServerSideProps({ params }) {
  return { props: { questionId: params.questionId } }
}