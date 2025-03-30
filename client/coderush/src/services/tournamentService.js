import webSocketService from '@/services/webSocketService';

export function submitTournament({
  tournamentType,
  index,
  tournamentId,
  username,
  token,
  question,
  language,
  code,
  toast,
  onComplete,
  userOutput,
}) {
  try {
    if (tournamentType === 'FREE_STYLE') {
      const payload = {
        index,
        tournamentId,
        userOutput: userOutput, // In FREE_STYLE, you may send output directly; adjust as needed.
      };
      console.log('Submitting payload (FREE_STYLE):', payload);
      webSocketService.send('/app/tournament/freeStyleSubmit', payload);

      webSocketService.subscribe(
        `/topic/tournament/freeStyleSubmit/${username}/${index}`,
        (response) => {
          console.log('Received FREE_STYLE response:', response);
          toast.dismiss();
          onComplete();
          if (response === true || (response && response.verdict === 'AC')) {
            toast.success('ACCEPTED!');
          } else if (response === false || (response && response.verdict === 'WA')) {
            toast.error('WRONG ANSWER!');
          } else if(response && response.verdict === "TLE"){
            toast.error('TIME LIMIT EXCEEDED!');
          } else if(response && response.verdict === "CE"){
            toast.error('COMPILATION ERROR!');
          } else if(response && response.verdict === "RE"){
            toast.error('RUNTIME ERROR!');
          } else if(response && response.verdict === "MLE"){
            toast.error('MEMORY LIMIT EXCEEDED!');
          } else {
            toast.error('Unexpected response. Please contact support.');
          }
        }
      );
    } else if (tournamentType === 'CLASSIC') {
      const payload = {
        index,
        tournamentId,
        questionId: question.questionId,
        language: language === 'cpp' ? 'c++' : language,
        code,
      };

      console.log('Submitting payload (CLASSIC):', payload);
      webSocketService.send('/app/tournament/classicSubmit', payload);

      webSocketService.subscribe(
        `/topic/tournament/classicSubmit/${username}/${index}`,
        (response) => {
          console.log('Received CLASSIC response:', response);
          toast.dismiss();
          onComplete();
          if (response === true || (response && response.verdict === 'AC')) {
            toast.success('ACCEPTED!');
          } else if (response === false || (response && response.verdict === 'WA')) {
            toast.error('WRONG ANSWER!');
          } else if(response && response.verdict === "TLE"){
            toast.error('TIME LIMIT EXCEEDED!');
          } else if(response && response.verdict === "CE"){
            toast.error('COMPILATION ERROR!');
          } else if(response && response.verdict === "RE"){
            toast.error('RUNTIME ERROR!');
          } else if(response && response.verdict === "MLE"){
            toast.error('MEMORY LIMIT EXCEEDED!');
          } else {
            toast.error('Unexpected response. Please contact support.');
          }
        }
      );
    } else {
      console.error("submitTournament: Unknown tournament type:", tournamentType);
      toast.dismiss();
      toast.error("Unknown tournament type. Cannot submit.");
    }
  } catch (error) {
    console.error('Error sending submission:', error);
    toast.dismiss();
    toast.error('An error occurred while submitting.');
  }
}
