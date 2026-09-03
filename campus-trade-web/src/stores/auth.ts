import { defineStore } from "pinia";

export interface AuthUser {
  id: number;
  username: string;
  email: string;
  role: string;
}

const TOKEN_KEY = "campus-trade-token";
const USER_KEY = "campus-trade-user";

function readStoredUser(): AuthUser | null {
  const rawUser = sessionStorage.getItem(USER_KEY);
  if (!rawUser) return null;

  try {
    return JSON.parse(rawUser) as AuthUser;
  } catch {
    sessionStorage.removeItem(USER_KEY);
    return null;
  }
}

export const useAuthStore = defineStore("auth", {
  state: () => ({
    token: sessionStorage.getItem(TOKEN_KEY) as string | null,
    user: readStoredUser(),
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.token),
  },
  actions: {
    setSession(token: string, user: AuthUser) {
      this.token = token;
      this.user = user;
      sessionStorage.setItem(TOKEN_KEY, token);
      sessionStorage.setItem(USER_KEY, JSON.stringify(user));
    },
    clearSession() {
      this.token = null;
      this.user = null;
      sessionStorage.removeItem(TOKEN_KEY);
      sessionStorage.removeItem(USER_KEY);
    },
  },
});
