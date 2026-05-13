import axios from 'axios'
import { setupInterceptors } from './interceptors'

export function createAxios(options = {}) {
  const defaultOptions = {
    baseURL: 'http://localhost:9999',
    // baseURL: 'http://159.75.11.181:9999',
    timeout: 12000,
    // withCredentials: true, // 自动发送cookie
    headers: {
      'Content-Type': 'application/json'
    }
  }

  const service = axios.create({
    ...defaultOptions,
    ...options,
  })
  
  setupInterceptors(service)
  return service
}

export const request = createAxios()