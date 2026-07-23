import { Stack } from "expo-router";

const PRIMARY = "#0066A2";

export default function DrawerLayout() {
  return (
    <Stack
      screenOptions={{
        headerShown: true,
        headerStyle: { backgroundColor: PRIMARY },
        headerTintColor: "#FFFFFF",
        headerTitleStyle: { fontWeight: "600" },
      }}
    >
      <Stack.Screen name="home" options={{ headerShown: false }} />
      <Stack.Screen name="ai-chat" options={{ headerShown: false }} />
      <Stack.Screen name="explore" options={{ title: "Médecins" }} />
      <Stack.Screen name="appointments" options={{ headerShown: false }} />
      <Stack.Screen name="reports" options={{ headerShown: false }} />
      <Stack.Screen name="profile" options={{ headerShown: false }} />
      <Stack.Screen name="settings" options={{ title: "Paramètres" }} />
      <Stack.Screen
        name="modal"
        options={{
          title: "Aide",
          presentation: "modal",
        }}
      />
    </Stack>
  );
}
