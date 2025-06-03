import { createBrowserRouter } from 'react-router-dom';
import Layout from '../components/Layout';
import Home from '../pages/Home';
import Convert from '../pages/Convert';
import Compress from '../pages/Compress';
import Security from '../pages/Security';

const router = createBrowserRouter([
  {
    path: '/',
    element: <Layout />,
    children: [
      {
        index: true,
        element: <Home />,
      },
      {
        path: 'convert',
        element: <Convert />,
      },
      {
        path: 'compress',
        element: <Compress />,
      },
      {
        path: 'security',
        element: <Security />,
      },
    ],
  },
]);

export default router; 