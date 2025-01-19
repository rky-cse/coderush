export const metadata = {
  title: 'Tournament Manager',
  description: 'Manage and create tournaments effortlessly.',
};

const RootLayout = ({ children }) => {
  return (
    <html lang="en">
      <body style={bodyStyles}>
        {children}
      </body>
    </html>
  );
};

const bodyStyles = {
  margin: 0,
  padding: 0,
  fontFamily: 'Arial, sans-serif',
  backgroundColor: '#f3f4f6',
  color: '#1f2937',
};

export default RootLayout;
