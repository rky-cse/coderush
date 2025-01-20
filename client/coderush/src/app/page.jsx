'use client';



import React from 'react';
import { useRouter } from 'next/navigation';

const HomePage = () => {
  const router = useRouter();

  const handleCreateTournamentClick = () => {
    router.push('/createTournament');
  };

  return (
    <div style={containerStyles}>
      <h1 style={headingStyles}>Welcome to the Tournament Manager</h1>
      <p style={subHeadingStyles}>
        Manage and create tournaments effortlessly. Get started by creating a new tournament.
      </p>
      <button onClick={handleCreateTournamentClick} style={buttonStyles}>
        CREATE NEW TOURNAMENT
      </button>
    </div>
  );
};

const containerStyles = {
  display: 'flex',
  flexDirection: 'column',
  alignItems: 'center',
  justifyContent: 'center',
  minHeight: '100vh',
  backgroundColor: '#f3f4f6',
  color: '#1f2937',
  fontFamily: 'Arial, sans-serif',
};

const headingStyles = {
  fontSize: '2rem',
  fontWeight: 'bold',
  marginBottom: '1rem',
};

const subHeadingStyles = {
  fontSize: '1.2rem',
  marginBottom: '2rem',
  textAlign: 'center',
  maxWidth: '600px',
};

const buttonStyles = {
  backgroundColor: '#1d4ed8',
  color: 'white',
  padding: '0.75rem 1.5rem',
  borderRadius: '0.5rem',
  fontSize: '1rem',
  border: 'none',
  cursor: 'pointer',
  transition: 'background-color 0.3s ease',
};

buttonStyles[':hover'] = {
  backgroundColor: '#2563eb',
};

export default HomePage;
