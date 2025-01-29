'use client'
import { useRef } from 'react'
import { Provider } from 'react-redux'
import { makeStore } from '../lib/store'
import { initializeCount } from '../lib/features/counter/counterSlice'

export default function StoreProvider({ count, children }) {
  const storeRef = useRef(null)
  if (!storeRef.current) {
    storeRef.current = makeStore()
    storeRef.current.dispatch(initializeCount(count))
  }

  return <Provider store={storeRef.current}>{children}</Provider>
}