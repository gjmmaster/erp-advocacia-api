import { Navigate, Route, Routes } from 'react-router-dom'
import SuperAdminRoute from './components/SuperAdminRoute'
import SuperAdminDashboardPage from './pages/SuperAdminDashboardPage'
import SuperAdminLoginPage from './pages/SuperAdminLoginPage'

function App() {
  return (
    <Routes>
      <Route path="/super-admin/login" element={<SuperAdminLoginPage />} />
      <Route
        path="/super-admin/dashboard"
        element={
          <SuperAdminRoute>
            <SuperAdminDashboardPage />
          </SuperAdminRoute>
        }
      />
      <Route path="/" element={<Navigate to="/super-admin/login" />} />
    </Routes>
  )
}

export default App