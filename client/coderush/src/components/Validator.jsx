'use client';

import { useState, useEffect } from 'react';
import api from '@/services/api';
import notify from '@/services/notify';

function Validator({ questionId }) {
  const [file, setFile] = useState(null);
  const [text, setText] = useState('');
  const [fileName, setFileName] = useState('');
  const [loading, setLoading] = useState(false);
  const [validatorExists, setValidatorExists] = useState(false);
  const [validatorFileName, setValidatorFileName] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    checkExistingValidator();
  }, [questionId]);

  const checkExistingValidator = async () => {
    try {
      const response = await api.get(`/api/questions/validator/${questionId}`);
      setValidatorExists(true);
      const contentDisposition = response.headers['content-disposition'];
      if (contentDisposition) {
        const match = contentDisposition.match(/filename="(.+)"/);
        if (match) setValidatorFileName(match[1]);
      }
    } catch (err) {
      if (err.status === 404 || err.status === 204) {
        setValidatorExists(false);
      }
    }
  };

  const handleUploadValidator = async () => {
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
      const method = validatorExists ? 'put' : 'post';
      await api[method](`/api/questions/validator/${questionId}`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });

      const msg = validatorExists ? 'Validator updated.' : 'Validator uploaded.';
      setSuccess(msg);
      notify.success(msg);

      setValidatorExists(true);
      setValidatorFileName(file ? file.name : fileName);
      setFile(null);
      setText('');
      setFileName('');

      const fileInput = document.getElementById('validator-file-input');
      if (fileInput) fileInput.value = '';
    } catch (err) {
      setError(err.message || 'Failed to upload validator');
      notify.error(err.message || 'Failed to upload validator');
    } finally {
      setLoading(false);
    }
  };

  const handleDownloadValidator = async () => {
    if (!validatorExists) return;
    setLoading(true);
    setError('');
    try {
      const response = await api.get(`/api/questions/validator/${questionId}`, {
        responseType: 'blob',
      });
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', validatorFileName || 'validator-file');
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      setError(err.message || 'Failed to download validator');
      notify.error(err.message || 'Failed to download validator');
    } finally {
      setLoading(false);
    }
  };

  const handleViewValidator = async () => {
    if (!validatorExists) return;
    setLoading(true);
    setError('');
    try {
      const response = await api.get(`/api/questions/validator/${questionId}`, {
        responseType: 'text',
      });
      setText(response.data);
      setFileName(validatorFileName);
    } catch (err) {
      setError(err.message || 'Failed to load validator content');
      notify.error(err.message || 'Failed to load validator content');
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
    const fileInput = document.getElementById('validator-file-input');
    if (fileInput) fileInput.value = '';
  };

  return (
    <div className="bg-white p-6 rounded-lg shadow-sm">
      <h2 className="text-xl font-semibold mb-4">Validator</h2>

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

      {validatorExists && (
        <div className="border p-4 rounded-md mb-4 bg-gray-50">
          <h3 className="font-medium mb-2">Current Validator File</h3>
          <div className="flex items-center justify-between">
            <span className="text-sm text-gray-600">📄 {validatorFileName || 'Validator file'}</span>
            <div className="space-x-2">
              <button
                onClick={handleViewValidator}
                disabled={loading}
                className="px-3 py-1 bg-blue-500 text-white rounded hover:bg-blue-600 disabled:opacity-50"
              >
                View/Edit
              </button>
              <button
                onClick={handleDownloadValidator}
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
        <h3 className="font-medium">{validatorExists ? 'Update Validator' : 'Upload Validator'}</h3>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Upload File</label>
          <input
            id="validator-file-input"
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
                const fileInput = document.getElementById('validator-file-input');
                if (fileInput) fileInput.value = '';
              }
            }}
            placeholder="Paste validator code here..."
          />
          <input
            type="text"
            className="w-full p-2 border rounded-md mt-2"
            placeholder="File name (e.g., validator.cpp, validator.py)"
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
            onClick={handleUploadValidator}
            disabled={loading || (!file && (!text || !fileName))}
            className={`px-4 py-2 rounded ${
              loading || (!file && (!text || !fileName))
                ? 'bg-gray-300 cursor-not-allowed'
                : 'bg-indigo-600 text-white hover:bg-indigo-700'
            }`}
          >
            {loading ? 'Processing...' : validatorExists ? 'Update Validator' : 'Upload Validator'}
          </button>
        </div>
      </div>
    </div>
  );
}

export default Validator;
