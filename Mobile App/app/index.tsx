import { ActivityIndicator, View } from "react-native";

export default function Index() {
  return (
    <View style={{ flex: 1, justifyContent: "center", alignItems: "center", backgroundColor: "#F6FAF9" }}>
      <ActivityIndicator size="large" color="#0C5D5F" />
    </View>
  );
}
