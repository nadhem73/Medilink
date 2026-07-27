import Ionicons from "@expo/vector-icons/Ionicons";
import type { BottomTabBarButtonProps } from "@react-navigation/bottom-tabs";
import { Tabs } from "expo-router";
import React from "react";
import { Platform, StyleSheet, TouchableOpacity, View } from "react-native";

const PRIMARY = "#0066A2";
const WHITE = "#FFFFFF";
const TAB_BG = "#FAFBFC";
const BORDER = "#E8ECF0";
const INACTIVE = "#8E8E93";

export default function TabLayout() {
  return (
    <Tabs
      screenOptions={{
        headerShown: false,
        tabBarActiveTintColor: PRIMARY,
        tabBarInactiveTintColor: INACTIVE,
        tabBarStyle: styles.tabBar,
        tabBarLabelStyle: styles.tabLabel,
        tabBarItemStyle: styles.tabItem,
      }}
    >
      <Tabs.Screen
        name="home"
        options={{
          title: "Accueil",
          tabBarIcon: ({ color, size }) => (
            <Ionicons name="home-outline" size={24} color={color} />
          ),
        }}
      />
      <Tabs.Screen
        name="appointments"
        options={{
          title: "Rendez-vous",
          tabBarIcon: ({ color, size }) => (
            <Ionicons name="calendar-outline" size={24} color={color} />
          ),
        }}
      />
      <Tabs.Screen
        name="scan"
        options={{
          title: "",
          tabBarLabel: () => null,
          tabBarButton: ScanButton,
          tabBarIcon: ({ focused }) => (
            <Ionicons
              name="scan-outline"
              size={28}
              color={WHITE}
            />
          ),
        }}
      />
      <Tabs.Screen
        name="reports"
        options={{
          title: "Dossier",
          tabBarIcon: ({ color, size }) => (
            <Ionicons name="clipboard-outline" size={24} color={color} />
          ),
        }}
      />
      <Tabs.Screen
        name="scan-result"
        options={{
          title: "Résultat",
          href: null,
        }}
      />
      <Tabs.Screen
        name="profile"
        options={{
          title: "Profil",
          tabBarIcon: ({ color, size }) => (
            <Ionicons name="person-outline" size={24} color={color} />
          ),
        }}
      />
    </Tabs>
  );
}

function ScanButton(props: BottomTabBarButtonProps) {
  const focused = props.accessibilityState?.selected;
  return (
    <TouchableOpacity
      onPress={props.onPress ?? undefined}
      onLongPress={props.onLongPress ?? undefined}
      activeOpacity={0.85}
      style={styles.scanWrap}
    >
      <View
        style={[
          styles.scanBtn,
          {
            backgroundColor: focused ? "#00558A" : PRIMARY,
            shadowColor: focused ? "#00558A" : PRIMARY,
          },
        ]}
      >
        {props.children}
      </View>
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  tabBar: {
    backgroundColor: TAB_BG,
    borderTopColor: BORDER,
    borderTopWidth: 1,
    paddingBottom: Platform.OS === "ios" ? 24 : 10,
    paddingTop: 10,
    height: Platform.OS === "ios" ? 84 : 66,
    elevation: 8,
    shadowColor: "#000",
    shadowOffset: { width: 0, height: -2 },
    shadowOpacity: 0.06,
    shadowRadius: 6,
  },
  tabLabel: {
    fontSize: 11,
    fontWeight: "600",
    marginTop: 2,
  },
  tabItem: {
    paddingTop: 2,
  },
  scanWrap: {
    top: -18,
    justifyContent: "center",
    alignItems: "center",
    zIndex: 10,
  },
  scanBtn: {
    width: 58,
    height: 58,
    borderRadius: 29,
    justifyContent: "center",
    alignItems: "center",
    shadowOffset: { width: 0, height: 6 },
    shadowOpacity: 0.35,
    shadowRadius: 12,
    elevation: 8,
  },
});
