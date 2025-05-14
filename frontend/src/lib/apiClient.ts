import axios from "axios";

// TODO: Use environment variables for URL
const BACKEND_BASE_URL = "http://localhost:8080";

export const api = axios.create({
    baseURL: BACKEND_BASE_URL,
    headers: {
        "Content-Type": "application/json"
    },
    withCredentials: true
});