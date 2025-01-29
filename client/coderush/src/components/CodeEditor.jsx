import React, { useRef, useState } from 'react';
import MonacoEditor from '@monaco-editor/react';
import axios from 'axios';
import { BiCopy } from 'react-icons/bi';
import { FaSun, FaMoon } from 'react-icons/fa';
import { FiPlus, FiMinus } from 'react-icons/fi';
import { useSelector, useDispatch } from 'react-redux';
import { setCode} from '../redux/slices/codeSlice';

const CodeEditor = () => {
    const dispatch = useDispatch();
    
    // Use Redux for storing code
    const code = useSelector((state) => state.editor.code);

    const [language, setLanguage] = useState('cpp');
    const [theme, setTheme] = useState('dark');
    const [fontSize, setFontSize] = useState(14);
    const editorRef = useRef();

    const handleEditorChange = (value) => {
        dispatch(setCode(value));
    };

    const handleCopyCode = () => {
        navigator.clipboard.writeText(code);
        alert('Code copied to clipboard!');
    };

    const handleThemeToggle = () => {
        setTheme(theme === 'light' ? 'dark' : 'light');
    };

    const handleFontSizeChange = (increment) => {
        setFontSize(fontSize + increment);
    };

    return (
        <div className={`w-full h-screen p-4 flex flex-col ${theme === 'dark' ? 'bg-gray-900' : 'bg-gray-100'}`}>
            <div className="flex justify-between mb-4">
                <select
                    value={language}
                    onChange={(e) => setLanguage(e.target.value)}
                    className="p-2 bg-white border rounded"
                >
                    <option value="javascript">JavaScript</option>
                    <option value="typescript">TypeScript</option>
                    <option value="python">Python</option>
                    <option value="java">Java</option>
                    <option value="csharp">C#</option>
                    <option value="c">C</option>
                    <option value="cpp">C++</option>
                </select>
                <div className="flex items-center space-x-2">
                    <button onClick={() => handleFontSizeChange(1)} className="p-2 bg-gray-200 rounded">
                        <FiPlus />
                    </button>
                    <button onClick={() => handleFontSizeChange(-1)} className="p-2 bg-gray-200 rounded">
                        <FiMinus />
                    </button>
                    <button onClick={handleThemeToggle} className="p-2 bg-gray-200 rounded">
                        {theme === 'light' ? <FaMoon /> : <FaSun />}
                    </button>
                    <button onClick={handleCopyCode} className="p-2 bg-gray-200 rounded">
                        <BiCopy />
                    </button>
                </div>
            </div>
            <div className="flex-grow">
                <MonacoEditor
                    height="100%"
                    language={language}
                    theme={theme === 'light' ? 'light' : 'vs-dark'}
                    value={code}
                    onChange={handleEditorChange}
                    options={{
                        fontSize: fontSize,
                        automaticLayout: true,
                    }}
                    editorDidMount={(editor) => {
                        editorRef.current = editor;
                    }}
                />
            </div>
        </div>
    );
};

export default CodeEditor;
