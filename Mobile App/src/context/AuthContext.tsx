import AsyncStorage from "@react-native-async-storage/async-storage";
import React, { useCallback, useContext, useEffect, useState } from "react";
import {
  authService,
  AuthResponse,
  RegisterData,
  UserDto,
} from "../services/authService";

type AuthContextType = {
  user: UserDto | null;
  loading: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (data: RegisterData) => Promise<void>;
  logout: () => Promise<void>;
};

const AuthContext = React.createContext<AuthContextType>({
  user: null,
  loading: true,
  login: async () => {},
  register: async () => {},
  logout: async () => {},
});

export const useAuth = () => useContext(AuthContext);

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
  const [user, setUser] = useState<UserDto | null>(null);
  const [loading, setLoading] = useState(true);

  const restoreSession = useCallback(async () => {
    try {
      const token = await AsyncStorage.getItem("accessToken");
      const storedUser = await AsyncStorage.getItem("user");
      if (token && storedUser) {
        setUser(JSON.parse(storedUser));
        try {
          const freshUser = await authService.getMe();
          setUser(freshUser);
          await AsyncStorage.setItem("user", JSON.stringify(freshUser));
        } catch {
          await AsyncStorage.multiRemove(["accessToken", "refreshToken", "user"]);
          setUser(null);
        }
      }
    } catch {
      setUser(null);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    restoreSession();
  }, [restoreSession]);

  const login = useCallback(async (email: string, password: string) => {
    await AsyncStorage.multiRemove(["accessToken", "refreshToken", "user"]);
    const res: AuthResponse = await authService.login(email, password);
    await AsyncStorage.setItem("accessToken", res.accessToken);
    await AsyncStorage.setItem("refreshToken", res.refreshToken);
    await AsyncStorage.setItem("user", JSON.stringify(res.user));
    setUser(res.user);
  }, []);

  const register = useCallback(async (data: RegisterData) => {
    await authService.register(data);
  }, []);

  const logout = useCallback(async () => {
    await AsyncStorage.multiRemove(["accessToken", "refreshToken", "user"]);
    setUser(null);
  }, []);

  return (
    <AuthContext.Provider value={{ user, loading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
};
