import { useSelector, useDispatch } from 'react-redux';
import { useRouter } from 'next/navigation';
import { logout } from '../redux/slices/authSlice';
import Link from 'next/link';
import { deleteCookie } from 'cookies-next';



export default function Navbar() {
  const { user } = useSelector((state) => state.auth);
  const router = useRouter();
  const dispatch = useDispatch();

  console.log(user)

 

const handleLogout = () => {
  dispatch(logout());  // Clear Redux state
  deleteCookie('token'); // Remove token from cookies
  deleteCookie('username'); // Remove stored username
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
              <Link href={`/dashboard/${user}`} className="text-gray-600 hover:text-gray-800">
  Dashboard
</Link>
              <Link href="/tournaments" className="text-gray-600 hover:text-gray-800">
                Tournaments
              </Link>
              <Link href="/createTournament" className="text-gray-600 hover:text-gray-800">
                Create Tournament
              </Link>
              
              <Link href="/myQuestions" className="text-gray-600 hover:text-gray-800">
                My Questions
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
