'use client';
import { useState, useEffect, useRef } from 'react';
import { useSearchParams, useRouter } from 'next/navigation';
import { Sparkles, Loader2, CheckCircle2, XCircle, ChevronDown, ChevronUp, Code, FileText, Play } from 'lucide-react';
import api from '@/services/api';
import notify from '@/services/notify';

const STAGES = [
  { id: 1, label: 'Draft Problem' },
  { id: 2, label: 'Generate Solution, Validator & Checker' },
  { id: 3, label: 'Generate Test Inputs' },
  { id: 4, label: 'Verify with Judge' },
  { id: 5, label: 'Save' },
];

export default function GenerateQuestionPage() {
  const searchParams = useSearchParams();
  const router = useRouter();
  const [phase, setPhase] = useState('form'); // form | progress | result
  const [form, setForm] = useState({ topic: '', difficulty: 'EASY', language: 'cpp', additionalNotes: '', mode: 'AUTO' });
  const [jobId, setJobId] = useState(null);
  const [job, setJob] = useState(null);
  const [progress, setProgress] = useState([]);
  const [submitting, setSubmitting] = useState(false);
  const pollRef = useRef(null);

  // Restore jobId from URL on mount
  useEffect(() => {
    const id = searchParams.get('jobId');
    if (id) {
      setJobId(id);
      setPhase('progress');
    }
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.topic.trim()) { notify.error('Topic is required.'); return; }
    setSubmitting(true);
    try {
      const { data } = await api.post('/api/problem/generate', form);
      setJobId(data.jobId);
      setPhase('progress');
      // Persist jobId in URL so refresh works
      router.replace(`/generateQuestion?jobId=${data.jobId}`, { scroll: false });
    } catch (err) {
      notify.error(err.message || 'Failed to start generation.');
    } finally {
      setSubmitting(false);
    }
  };

  // Poll job status
  useEffect(() => {
    if (phase !== 'progress' || !jobId) return;
    const poll = async () => {
      try {
        const { data } = await api.get(`/api/problem/job/${jobId}`);
        setJob(data);
        setProgress(data.progress || []);
        if (data.status === 'DONE' || data.status === 'FAILED') {
          clearInterval(pollRef.current);
          setPhase('result');
        }
      } catch { /* ignore transient errors */ }
    };
    poll();
    pollRef.current = setInterval(poll, 3000);
    return () => clearInterval(pollRef.current);
  }, [phase, jobId]);

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-3xl mx-auto px-4">
        <div className="flex items-center gap-3 mb-8">
          <Sparkles className="h-7 w-7 text-purple-600" />
          <h1 className="text-2xl font-bold text-gray-900">AI Problem Generator</h1>
        </div>

        {phase === 'form' && <GenerateForm form={form} setForm={setForm} onSubmit={handleSubmit} submitting={submitting} />}
        {phase === 'progress' && <ProgressView progress={progress} job={job} />}
        {phase === 'result' && <ResultView job={job} onReset={() => { setPhase('form'); setJob(null); setJobId(null); setProgress([]); router.replace('/generateQuestion', { scroll: false }); }} />}
      </div>
    </div>
  );
}

// ─── Form ─────────────────────────────────────────────────────────────────────

function GenerateForm({ form, setForm, onSubmit, submitting }) {
  const update = (field) => (e) => setForm((f) => ({ ...f, [field]: e.target.value }));

  return (
    <form onSubmit={onSubmit} className="bg-white rounded-xl shadow-sm border p-6 space-y-5">
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">Topic / Theme *</label>
        <input type="text" value={form.topic} onChange={update('topic')} placeholder="e.g. arrays, dynamic programming, graphs"
          className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-purple-500 focus:border-transparent" required />
      </div>

      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Difficulty</label>
          <select value={form.difficulty} onChange={update('difficulty')}
            className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-purple-500">
            <option value="EASY">Easy</option>
            <option value="MEDIUM">Medium</option>
            <option value="HARD">Hard</option>
          </select>
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Language</label>
          <select value={form.language} onChange={update('language')}
            className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-purple-500">
            <option value="cpp">C++</option>
            <option value="java">Java</option>
            <option value="python">Python</option>
          </select>
        </div>
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">Additional Notes (optional)</label>
        <textarea value={form.additionalNotes} onChange={update('additionalNotes')} rows={2} placeholder="Any specific requirements..."
          className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-purple-500" />
      </div>

      <button type="submit" disabled={submitting}
        className={`w-full flex items-center justify-center gap-2 py-3 rounded-lg text-white font-medium transition
          ${submitting ? 'bg-purple-400 cursor-not-allowed' : 'bg-purple-600 hover:bg-purple-700'}`}>
        {submitting ? <Loader2 className="h-5 w-5 animate-spin" /> : <Sparkles className="h-5 w-5" />}
        {submitting ? 'Starting...' : 'Generate Problem'}
      </button>
    </form>
  );
}

// ─── Progress ─────────────────────────────────────────────────────────────────

function ProgressView({ progress, job }) {
  const currentStage = job?.currentStage || 1;

  return (
    <div className="bg-white rounded-xl shadow-sm border p-6">
      <h2 className="text-lg font-semibold text-gray-900 mb-4">Generating...</h2>
      <div className="space-y-3">
        {STAGES.map((stage) => {
          const stageEvents = progress.filter((p) => p.stage === stage.id);
          const isDone = stageEvents.some((e) => e.status === 'DONE');
          const isRunning = !isDone && stage.id === currentStage;
          const isPending = stage.id > currentStage;

          return (
            <div key={stage.id} className={`flex items-center gap-3 px-4 py-3 rounded-lg border
              ${isDone ? 'bg-green-50 border-green-200' : isRunning ? 'bg-purple-50 border-purple-200' : 'bg-gray-50 border-gray-200'}`}>
              {isDone && <CheckCircle2 className="h-5 w-5 text-green-600 shrink-0" />}
              {isRunning && <Loader2 className="h-5 w-5 text-purple-600 animate-spin shrink-0" />}
              {isPending && <div className="h-5 w-5 rounded-full border-2 border-gray-300 shrink-0" />}
              <span className={`text-sm font-medium ${isDone ? 'text-green-800' : isRunning ? 'text-purple-800' : 'text-gray-500'}`}>
                Stage {stage.id}: {stage.label}
              </span>
              {isDone && stageEvents.length > 0 && (
                <span className="ml-auto text-xs text-green-600">{stageEvents[stageEvents.length - 1].message}</span>
              )}
            </div>
          );
        })}
      </div>
      <p className="mt-4 text-xs text-gray-500 text-center">This usually takes 60–90 seconds...</p>
    </div>
  );
}

// ─── Result ───────────────────────────────────────────────────────────────────

function ResultView({ job, onReset }) {
  const [expanded, setExpanded] = useState({});
  const toggle = (key) => setExpanded((e) => ({ ...e, [key]: !e[key] }));

  if (!job) return null;
  const failed = job.status === 'FAILED';

  return (
    <div className="space-y-4">
      {/* Status banner */}
      <div className={`rounded-xl border p-4 flex items-center gap-3 ${failed ? 'bg-red-50 border-red-200' : 'bg-green-50 border-green-200'}`}>
        {failed ? <XCircle className="h-6 w-6 text-red-600" /> : <CheckCircle2 className="h-6 w-6 text-green-600" />}
        <div>
          <p className={`font-semibold ${failed ? 'text-red-800' : 'text-green-800'}`}>
            {failed ? 'Generation Failed' : 'Problem Generated Successfully!'}
          </p>
          {failed && <p className="text-sm text-red-600 mt-1">{job.errorReason}</p>}
          {!failed && job.questionId && <p className="text-sm text-green-600">Question ID: {job.questionId}</p>}
        </div>
      </div>

      {/* Draft */}
      {job.draft && (
        <CollapsibleSection title={`Problem: ${job.draft.name}`} icon={<FileText className="h-5 w-5" />} defaultOpen>
          <div className="space-y-2 text-sm">
            <p className="text-gray-700 whitespace-pre-wrap">{job.draft.legend}</p>
            <div className="grid grid-cols-2 gap-4 mt-3">
              <div><span className="font-medium text-gray-600">Input:</span><pre className="mt-1 bg-gray-100 p-2 rounded text-xs">{job.draft.inputFormat}</pre></div>
              <div><span className="font-medium text-gray-600">Output:</span><pre className="mt-1 bg-gray-100 p-2 rounded text-xs">{job.draft.outputFormat}</pre></div>
            </div>
            <div><span className="font-medium text-gray-600">Constraints:</span> {job.draft.constraints}</div>
            <div className="grid grid-cols-2 gap-4">
              <div><span className="font-medium text-gray-600">Sample Input:</span><pre className="mt-1 bg-gray-100 p-2 rounded text-xs">{job.draft.sampleInput}</pre></div>
              <div><span className="font-medium text-gray-600">Sample Output:</span><pre className="mt-1 bg-gray-100 p-2 rounded text-xs">{job.draft.sampleOutput}</pre></div>
            </div>
          </div>
        </CollapsibleSection>
      )}

      {/* Solution */}
      {job.solution && (
        <CollapsibleSection title="Solution" icon={<Code className="h-5 w-5" />}>
          <pre className="bg-gray-900 text-green-300 p-4 rounded-lg text-xs overflow-x-auto">{job.solution.code}</pre>
          {job.solution.explanation && <p className="mt-2 text-sm text-gray-600">{job.solution.explanation}</p>}
          {job.solution.complexity && <p className="text-xs text-gray-500 mt-1">{job.solution.complexity}</p>}
        </CollapsibleSection>
      )}

      {/* Test Inputs */}
      {job.testInputs && (
        <CollapsibleSection title={`Test Cases (${job.testInputs.validatedCount} validated)`} icon={<Play className="h-5 w-5" />}>
          <div className="space-y-2 max-h-60 overflow-y-auto">
            {job.testInputs.inputs.map((input, i) => (
              <div key={i} className="flex items-start gap-2">
                <span className={`text-xs px-1.5 py-0.5 rounded ${job.testInputs.validationStatus?.[i] === 'PASS' ? 'bg-green-100 text-green-700' : 'bg-yellow-100 text-yellow-700'}`}>
                  #{i + 1}
                </span>
                <pre className="text-xs bg-gray-100 p-2 rounded flex-1 overflow-x-auto">{input}</pre>
              </div>
            ))}
          </div>
        </CollapsibleSection>
      )}

      {/* Actions */}
      <div className="flex gap-3 pt-2">
        <button onClick={onReset} className="flex-1 py-2.5 rounded-lg border border-gray-300 text-gray-700 font-medium hover:bg-gray-50 transition">
          Generate Another
        </button>
        {!failed && job.questionId && (
          <a href={`/questions/${job.questionId}`} className="flex-1 py-2.5 rounded-lg bg-purple-600 text-white font-medium text-center hover:bg-purple-700 transition">
            View Question
          </a>
        )}
      </div>
    </div>
  );
}

// ─── Collapsible Section ──────────────────────────────────────────────────────

function CollapsibleSection({ title, icon, children, defaultOpen = false }) {
  const [open, setOpen] = useState(defaultOpen);
  return (
    <div className="bg-white rounded-xl shadow-sm border overflow-hidden">
      <button onClick={() => setOpen(!open)} className="w-full flex items-center gap-3 px-5 py-4 hover:bg-gray-50 transition">
        {icon}
        <span className="font-medium text-gray-900 flex-1 text-left">{title}</span>
        {open ? <ChevronUp className="h-4 w-4 text-gray-500" /> : <ChevronDown className="h-4 w-4 text-gray-500" />}
      </button>
      {open && <div className="px-5 pb-5 border-t pt-4">{children}</div>}
    </div>
  );
}
