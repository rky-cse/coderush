import { useSelector } from "react-redux";
import { selectTournamentEndTime } from "@/redux/slices/tournamentEndTimeSlice";
import { useEffect, useState } from "react";

const TournamentControlBox = () => {
  const tournamentEndTime = useSelector(selectTournamentEndTime);
  const [timeLeft, setTimeLeft] = useState({ days: 0, hours: 0, minutes: 0, seconds: 0 });

  useEffect(() => {
    if (!tournamentEndTime) return;

    const updateCountdown = () => {
      const now = Date.now();
      const diff = tournamentEndTime - now;

      if (diff <= 0) {
        setTimeLeft({ days: 0, hours: 0, minutes: 0, seconds: 0 });
      } else {
        setTimeLeft({
          days: Math.floor(diff / (1000 * 60 * 60 * 24)),
          hours: Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)),
          minutes: Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60)),
          seconds: Math.floor((diff % (1000 * 60)) / 1000),
        });
      }
    };

    updateCountdown();
    const timer = setInterval(updateCountdown, 1000);

    return () => clearInterval(timer);
  }, [tournamentEndTime]);

  if (!tournamentEndTime) return <p className="text-gray-500 text-sm">Loading...</p>;

  return (
    <div className="flex h-full w-full items-center justify-center bg-gradient-to-r from-gray-800 to-gray-900 text-white px-3 py-2 rounded-md shadow-lg overflow-hidden">
      {tournamentEndTime!=null && tournamentEndTime>0 &&tournamentEndTime - Date.now() <= 0  ? (
        <p className="text-red-500 text-sm md:text-base font-semibold whitespace-nowrap">
          Tournament Ended!
        </p>
      ) : (
        <div className="flex items-center justify-center gap-1 md:gap-2">
            <p className="text-sm md:text-base font-semibold">Tournament Ends In:</p>
          {Object.entries(timeLeft).map(([key, value]) => (
            <span
              key={key}
              className="px-2 py-1 bg-gray-700 rounded-md text-xs md:text-sm lg:text-base font-semibold flex items-center"
            >
              {value}
              <span className="ml-1 text-[10px] md:text-xs">{key[0]}</span>
            </span>
          ))}
        </div>
      )}
    </div>
  );
};

export default TournamentControlBox;
