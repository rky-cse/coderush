import axios from 'axios';

const languageVersionMap = {
  javascript: '18.15.0',
  typescript: '5.0.3',
  python: '3.10.0',
  java: '15.0.2',
  csharp: '6.12.0',
  c: '10.2.0',
  cpp: '10.2.0',
};

export async function runCode({ language, code, customInput }) {
  const version = languageVersionMap[language];

  try {
    const response = await axios.post('https://emkc.org/api/v2/piston/execute', {
      language,
      version,
      files: [{ content: code }],
      stdin: customInput,
    });
    const runData = response.data.run;

    if (runData.signal === "SIGKILL") {
      return { output: runData.output, error: true, errorMessage: 'Time Limit exceeded!' };
    } else if (response.data.compile && response.data.compile.code === 1) {
      return { output: runData.output, error: true, errorMessage: 'Compilation Error!' };
    } else if (runData.code === 1) {
      return { output: runData.output, error: true, errorMessage: 'Runtime Error!' };
    } else if (runData.code === 0) {
      return { output: runData.output, error: false, successMessage: 'Code executed successfully!' };
    }
  } catch (error) {
    console.error('Error running code:', error);
    return { output: 'Error running code', error: true, errorMessage: 'Error running code.' };
  }
}
