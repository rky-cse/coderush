import { useSelector, useDispatch } from 'react-redux';
import { useRouter } from 'next/navigation';
import { logout } from '../redux/slices/authSlice';
import Link from 'next/link';



export default function Navbar() {
  const { user } = useSelector((state) => state.auth);
  const router = useRouter();
  const dispatch = useDispatch();

  const handleLogout = () => {
    dispatch(logout());  // Clear Redux state
    localStorage.removeItem('token'); // Remove token from localStorage
    localStorage.removeItem('username'); // Remove stored username
    router.push('/login'); // Redirect to login page
  };

  return (
    <nav className="bg-white shadow-md">
      <div className="container mx-auto px-4 py-3 flex justify-between items-center">
        <Link href="/" className="text-xl font-bold text-gray-700">
          Coderush
        </Link>
        <div className="flex items-center space-x-4">
          {user ? (
            <>
              <Link href="/dashboard" className="text-gray-600 hover:text-gray-800">
                Dashboard
              </Link>
              <Link href="/createTournament" className="text-gray-600 hover:text-gray-800">
                Create Tournament
              </Link>
              <Link href="/joinTournament" className="text-gray-600 hover:text-gray-800">
                Join Tournament
              </Link>
              <button
                onClick= {handleLogout}
                className="text-gray-600 hover:text-gray-800"
              >
                Logout
              </button>
            </>
          ) : (
            <Link href="/login" className="text-gray-600 hover:text-gray-800">
              Login
            </Link>
          )}
        </div>
      </div>
    </nav>
  );
}
