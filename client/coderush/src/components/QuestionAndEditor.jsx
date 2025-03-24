import React from 'react';
import { useSelector } from 'react-redux';
import Question from '@/components/Question';
import CodeEditor from '@/components/CodeEditor';

export default function QuestionAndEditor({ tournamentId, token }) {
  const index = useSelector((state) => state.index) ?? 0;

  return (
    <div
      className="questionAndEditorBox relative h-full overflow-auto bg-white p-4"
      style={{ scrollPaddingTop: '80px' }} // Adjust if needed
    >
      {/* Question Section */}
      <div className="question-section mb-8">
        {tournamentId && index !== undefined && token ? (
          <Question tournamentId={tournamentId} index={index} token={token} />
        ) : (
          <p>Error loading question. Required data is missing.</p>
        )}
      </div>
      {/* Code Editor Section - sticky so its top remains visible */}
      <div className="code-editor-section sticky top-0 bg-white pt-2">
        <h2 className="text-xl font-bold mb-2">Code Editor</h2>
        <CodeEditor />
      </div>
    </div>
  );
}
