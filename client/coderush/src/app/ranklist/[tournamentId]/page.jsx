'use client'

import { useState, useEffect } from 'react'
import { useParams } from 'next/navigation'
import api from '@/services/api'
import notify from '@/services/notify'

export default function RankList() {
  const params = useParams()
  const tournamentId = params.tournamentId
  const [ranks, setRanks] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    if (!tournamentId) return

    const fetchRankList = async () => {
      try {
        const { data } = await api.get(`/api/tournaments/${tournamentId}/ranks`)
        setRanks(data)
      } catch (err) {
        setError(err.message || 'Failed to fetch rank list')
        notify.error(err.message || 'Failed to fetch rank list')
      } finally {
        setLoading(false)
      }
    }

    fetchRankList()
  }, [tournamentId])

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-xl font-semibold">Loading tournament ranks...</div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="p-4 text-red-600 bg-red-100 rounded-lg">
          Error: {error}
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8 px-4 sm:px-6 lg:px-8">
      <div className="max-w-4xl mx-auto">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-gray-900">
            Tournament #{tournamentId} Rankings
          </h1>
          <p className="mt-2 text-lg text-gray-600">
            Current standings and player performance
          </p>
        </div>

        <div className="bg-white shadow overflow-hidden sm:rounded-lg">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Rank
                </th>
                <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Username
                </th>
                <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Score
                </th>
                <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Penalty
                </th>
                <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Rating
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {ranks.map((rank, index) => (
                <tr key={rank.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                    {index + 1}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {rank.userName}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {rank.score}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {rank.penalty}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {rank.rating}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {ranks.length === 0 && (
          <div className="mt-8 text-center text-gray-500">
            No rankings available for this tournament yet
          </div>
        )}
      </div>
    </div>
  )
}