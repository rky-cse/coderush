'use client';
import CodeEditor from '@/components/CodeEditor';
import React from 'react';
import { useDispatch, useSelector } from 'react-redux';
import axios from 'axios';
import { setLanguage } from '@/redux/slices/codeSlice';

export default function TournamentPage() {
    const [customInput, setCustomInput] = React.useState('');
    const [output, setOutput] = React.useState('');
    const dispatch = useDispatch();
    const { language, code } = useSelector((state) => state.editor);

    const handleInputChange = (e) => {
        setCustomInput(e.target.value);
    };

    const handleRunCode = async () => {
        const languageVersionMap = {
            javascript: '18.15.0',
            typescript: '5.0.3',
            python: '3.10.0',
            java: '15.0.2',
            csharp: '6.12.0',
            c: '10.2.0',
            cpp: '10.2.0',
        };

        const version = languageVersionMap[language];

        try {
            const response = await axios.post('https://emkc.org/api/v2/piston/execute', {
                language: language,
                version: version,
                files: [{ content: code }], // Corrected the files field
                stdin: customInput,
            });

            setOutput(response.data.run.output);
        } catch (error) {
            console.error('Error running code:', error);
            setOutput('Error running code');
        }
    };

    return (
        <div className="flex flex-col h-screen">
            {/* 1. Tournament Control Box (5% vertical) */}
            <div className="h-[5vh] w-full bg-blue-200 flex items-center justify-center">
                <p className="font-bold">Tournament Control Box</p>
            </div>

            {/* Main area (95% vertical) split into 70% (left) + 30% (right) */}
            <div className="flex h-[95vh] w-full">
                {/* Left side: a 'fixed-like' container with question and code editor */}
                <div className="relative w-[70%] h-full p-4 border-r">
                    {/* The questionAndEditorBox is absolute & scrollable within the parent */}
                    <div className="questionAndEditorBox absolute inset-0 overflow-auto bg-white p-4">
                        {/* Question Box */}
                        <div className="mb-8">
                            <h2 className="text-xl font-bold mb-2">Question Box</h2>
                            <p>
                                Long question content goes here. If it overflows, you 
                                can scroll down to see the code editor.
                                Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vestibulum non libero non nisi fermentum feugiat. Sed interdum libero vel convallis posuere. Quisque tempor felis a augue aliquam, eget elementum lacus pharetra. Nam luctus, ligula ac consectetur tempor, nulla ante dictum urna, sit amet euismod velit magna nec dui. Integer ut vehicula ligula, sit amet fringilla nunc. Sed posuere ante id tristique bibendum. Nulla at nunc lorem. Sed gravida, est nec fringilla efficitur, enim lorem pharetra magna, ac facilisis mauris ex sit amet ex. Quisque sagittis euismod nisl, vel gravida turpis sodales at.
                            </p>
                        </div>

                        {/* Code Editor Box */}
                        <div className="mb-8">
                            <h2 className="text-xl font-bold mb-2">Code Editor</h2>
                            
                            <CodeEditor/>
                        </div>
                    </div>
                </div>

                {/* Right side (30% width) */}
                <div className="w-[30%] h-full bg-gray-100 flex flex-col">
                    {/* Input Box (40% of right-side height) */}
                    <div className="h-[40%] p-4 border-b">
                        <h3 className="font-semibold mb-2">Input Box</h3>
                        <textarea
                            className="w-full h-full p-2 border rounded resize-none"
                            placeholder="Enter input here..."
                            value={customInput}
                            onChange={handleInputChange}
                        />
                    </div>

                    {/* Output Box (40% of right-side height) */}
                    <div className="h-[40%] p-4 border-b">
                        <h3 className="font-semibold mb-2">Output Box</h3>
                        <div className="w-full h-full bg-white border rounded p-2">
                            {output}
                        </div>
                    </div>

                    {/* Buttons (20% of right-side height) */}
                    <div className="h-[20%] flex items-center justify-evenly p-4">
                        <button className="bg-gray-400 text-white px-4 py-2 rounded">
                            Prev
                        </button>
                        <button className="bg-green-600 text-white px-4 py-2 rounded" onClick={handleRunCode}>
                            Run Code
                        </button>
                        <button className="bg-blue-600 text-white px-4 py-2 rounded">
                            Submit
                        </button>
                        <button className="bg-gray-400 text-white px-4 py-2 rounded">
                            Next
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
}