'use client';

import { useState, useEffect } from 'react';
import api from '@/services/api';
import notify from '@/services/notify';

function Checker({ questionId }) {
  const [file, setFile] = useState(null);
  const [text, setText] = useState('');
  const [fileName, setFileName] = useState('');
  const [loading, setLoading] = useState(false);
  const [checkerExists, setCheckerExists] = useState(false);
  const [checkerFileName, setCheckerFileName] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    checkExistingChecker();
  }, [questionId]);

  const checkExistingChecker = async () => {
    try {
      const response = await api.get(`/api/questions/checker/${questionId}`);
      setCheckerExists(true);
      const contentDisposition = response.headers['content-disposition'];
      if (contentDisposition) {
        const match = contentDisposition.match(/filename="(.+)"/);
        if (match) setCheckerFileName(match[1]);
      }
    } catch (err) {
      // 404/204/etc. simply means no checker yet — not really an error.
      if (err.status === 404 || err.status === 204) {
        setCheckerExists(false);
      }
      // Auth/network failures are surfaced by the api interceptor; we stay quiet here.
    }
  };

  const handleUploadChecker = async () => {
    if (!file && (!text || !fileName)) {
      setError('Please provide either a file or text content with a filename');
      return;
    }

    setLoading(true);
    setError('');
    setSuccess('');

    const formData = new FormData();
    if (file) {
      formData.append('file', file);
    } else {
      const blob = new Blob([text], { type: 'text/plain' });
      formData.append('file', new File([blob], fileName));
    }

    try {
      const method = checkerExists ? 'put' : 'post';
      await api[method](`/api/questions/checker/${questionId}`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });

      const msg = checkerExists ? 'Checker updated.' : 'Checker uploaded.';
      setSuccess(msg);
      notify.success(msg);

      setCheckerExists(true);
      setCheckerFileName(file ? file.name : fileName);
      setFile(null);
      setText('');
      setFileName('');

      const fileInput = document.getElementById('checker-file-input');
      if (fileInput) fileInput.value = '';
    } catch (err) {
      setError(err.message || 'Failed to upload checker');
      notify.error(err.message || 'Failed to upload checker');
    } finally {
      setLoading(false);
    }
  };

  const handleDownloadChecker = async () => {
    if (!checkerExists) return;
    setLoading(true);
    setError('');
    try {
      const response = await api.get(`/api/questions/checker/${questionId}`, {
        responseType: 'blob',
      });
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', checkerFileName || 'checker-file');
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      setError(err.message || 'Failed to download checker');
      notify.error(err.message || 'Failed to download checker');
    } finally {
      setLoading(false);
    }
  };

  const handleViewChecker = async () => {
    if (!checkerExists) return;
    setLoading(true);
    setError('');
    try {
      const response = await api.get(`/api/questions/checker/${questionId}`, {
        responseType: 'text',
      });
      setText(response.data);
      setFileName(checkerFileName);
    } catch (err) {
      setError(err.message || 'Failed to load checker content');
      notify.error(err.message || 'Failed to load checker content');
    } finally {
      setLoading(false);
    }
  };

  const clearForm = () => {
    setFile(null);
    setText('');
    setFileName('');
    setError('');
    setSuccess('');
    const fileInput = document.getElementById('checker-file-input');
    if (fileInput) fileInput.value = '';
  };

  return (
    <div className="bg-white p-6 rounded-lg shadow-sm">
      <h2 className="text-xl font-semibold mb-4">Checker</h2>

      {error && (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
          {error}
        </div>
      )}
      {success && (
        <div className="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded mb-4">
          {success}
        </div>
      )}

      {checkerExists && (
        <div className="border p-4 rounded-md mb-4 bg-gray-50">
          <h3 className="font-medium mb-2">Current Checker File</h3>
          <div className="flex items-center justify-between">
            <span className="text-sm text-gray-600">📄 {checkerFileName || 'Checker file'}</span>
            <div className="space-x-2">
              <button
                onClick={handleViewChecker}
                disabled={loading}
                className="px-3 py-1 bg-blue-500 text-white rounded hover:bg-blue-600 disabled:opacity-50"
              >
                View/Edit
              </button>
              <button
                onClick={handleDownloadChecker}
                disabled={loading}
                className="px-3 py-1 bg-green-500 text-white rounded hover:bg-green-600 disabled:opacity-50"
              >
                Download
              </button>
            </div>
          </div>
        </div>
      )}

      <div className="space-y-4">
        <h3 className="font-medium">{checkerExists ? 'Update Checker' : 'Upload Checker'}</h3>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Upload File</label>
          <input
            id="checker-file-input"
            type="file"
            className="w-full p-2 border rounded-md"
            onChange={(e) => {
              setFile(e.target.files?.[0] || null);
              if (e.target.files?.[0]) {
                setText('');
                setFileName('');
              }
            }}
          />
        </div>

        <div className="text-center text-gray-500">OR</div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Paste Content</label>
          <textarea
            className="w-full p-3 border rounded-md min-h-[200px] font-mono text-sm"
            value={text}
            onChange={(e) => {
              setText(e.target.value);
              if (e.target.value) {
                setFile(null);
                const fileInput = document.getElementById('checker-file-input');
                if (fileInput) fileInput.value = '';
              }
            }}
            placeholder="Paste checker code here..."
          />
          <input
            type="text"
            className="w-full p-2 border rounded-md mt-2"
            placeholder="File name (e.g., checker.cpp, checker.py)"
            value={fileName}
            onChange={(e) => setFileName(e.target.value)}
          />
        </div>

        <div className="flex space-x-2 justify-end">
          <button
            onClick={clearForm}
            className="px-4 py-2 bg-gray-500 text-white rounded hover:bg-gray-600"
          >
            Clear
          </button>
          <button
            onClick={handleUploadChecker}
            disabled={loading || (!file && (!text || !fileName))}
            className={`px-4 py-2 rounded ${
              loading || (!file && (!text || !fileName))
                ? 'bg-gray-300 cursor-not-allowed'
                : 'bg-indigo-600 text-white hover:bg-indigo-700'
            }`}
          >
            {loading ? 'Processing...' : checkerExists ? 'Update Checker' : 'Upload Checker'}
          </button>
        </div>
      </div>
    </div>
  );
}

export default Checker;
