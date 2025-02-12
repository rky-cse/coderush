'use client';
import { useState } from 'react';
import TournamentFormModal from '@/components/TournamentFormModal';
import { getCookie } from 'cookies-next';

const CreateTournament = () => {
  const [isModalOpen, setIsModalOpen] = useState(false);

  const handleModalToggle = () => setIsModalOpen((prev) => !prev);

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col items-center py-12">
      <h1 className="text-4xl font-bold mb-6 text-indigo-600">Create a Tournament</h1>
      <button
        onClick={handleModalToggle}
        className="px-6 py-3 text-white bg-indigo-600 hover:bg-indigo-700 rounded-lg font-semibold"
      >
        Open Tournament Form
      </button>

      {isModalOpen && (
        <div className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50 p-4">
          <div className="bg-white rounded-lg shadow-lg p-6 max-w-4xl w-full max-h-screen overflow-y-auto">
            <TournamentFormModal closeModal={handleModalToggle} />
          </div>
        </div>
      )}
    </div>
  );
};

export default CreateTournament;
