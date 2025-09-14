export const useAuthStore = defineStore("auth", () => {
  const isAuthenticated = ref(false);
  const user = ref<any>(null);

  const login = async (_credentials: unknown) => {
    // Mock login logic
    isAuthenticated.value = true;
    user.value = { name: "User", email: "user@example.com" };
  };

  const logout = () => {
    isAuthenticated.value = false;
    user.value = null;
  };

  return {
    isAuthenticated,
    user,
    login,
    logout,
  };
});

function defineStore(
  arg0: string,
  arg1: () => {
    isAuthenticated: globalThis.Ref<boolean, boolean>;
    user: globalThis.Ref<any, any>;
    login: (_credentials: unknown) => Promise<void>;
    logout: () => void;
  }
) {
  throw new Error("Function not implemented.");
}
