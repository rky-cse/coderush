export default function CreateButton({ onClick }) {
    return (
      <button onClick={onClick} style={buttonStyles}>
        CREATE NEW TOURNAMENT
      </button>
    );
  }
  
  const buttonStyles = {
    backgroundColor: '#1d4ed8', // Tailwind Blue-700
    color: 'white',
    padding: '0.5rem 1rem',
    borderRadius: '0.375rem',
    transition: 'background-color 0.2s ease',
    border: 'none',
    cursor: 'pointer',
  };
  
  buttonStyles[':hover'] = {
    backgroundColor: '#2563eb', // Tailwind Blue-600
  };
  