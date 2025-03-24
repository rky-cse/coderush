import React from 'react';

export default function IOBox({ customInput, setCustomInput, output, setOutput }) {
  const handleInputChange = (e) => {
    setCustomInput(e.target.value);
  };

  const handleOutputChange = (e) => {
    setOutput(e.target.value);
  };

  return (
    <>
      <div className="h-[40%] p-4 border-b">
        <h3 className="font-semibold mb-2">Input Box</h3>
        <textarea
          className="w-full h-full p-2 border rounded resize-none"
          placeholder="Enter input here..."
          value={customInput}
          onChange={handleInputChange}
        />
      </div>

      <div className="h-[40%] p-4 border-b">
        <h3 className="font-semibold mb-2">Output Box</h3>
        <textarea
          className="w-full h-full p-2 border rounded resize-none"
          placeholder="Output will appear here..."
          value={output}
          onChange={handleOutputChange}
        />
      </div>
    </>
  );
}
