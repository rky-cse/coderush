'use client';
import { useState } from 'react';
import TournamentFormModal from '@/components/TournamentFormModal';

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

      {isModalOpen && <TournamentFormModal closeModal={handleModalToggle} />}
    </div>
  );
};

export default CreateTournament;
