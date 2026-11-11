import { useAuth } from "@/context/AuthContext";
import Ionicons from "@expo/vector-icons/Ionicons";
import { LinearGradient } from "expo-linear-gradient";
import { useRouter } from "expo-router";
import React from "react";
import {
  Alert,
  Image,
  ScrollView,
  StatusBar,
  StyleSheet,
  Text,
  TouchableOpacity,
  View,
} from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";

const profileLocalImage = require("../../assets/images/profile.png");

const PRIMARY = "#0066A2";
const LIGHT_BG = "#F0F6FA";
const WHITE = "#FFFFFF";
const TEXT_DARK = "#1E293B";
const TEXT_MUTED = "#64748B";

const FRENCH_MONTHS = ["Jan", "Fév", "Mar", "Avr", "Mai", "Juin", "Juil", "Aoû", "Sep", "Oct", "Nov", "Déc"];

export default function ProfileScreen() {
  const router = useRouter();
  const insets = useSafeAreaInsets();
  const { user, logout } = useAuth();

  const fullName = user
    ? `${user.firstName || ""} ${user.lastName || ""}`.trim()
    : "Patient";

  const formatDate = (iso: string) => {
    const d = new Date(iso);
    return `${d.getDate()} ${FRENCH_MONTHS[d.getMonth()]} ${d.getFullYear()}`;
  };

  const genderLabel = user?.gender === "MASCULIN" ? "Homme" : user?.gender === "FEMININ" ? "Femme" : user?.gender || "";

  const handleLogout = () => {
    Alert.alert(
      "Déconnexion",
      "Êtes-vous sûr de vouloir vous déconnecter ?",
      [
        { text: "Annuler", style: "cancel" },
        { text: "Se déconnecter", style: "destructive", onPress: async () => { await logout(); router.replace("/(drawer)/home"); } },
      ]
    );
  };

  return (
    <View style={styles.container}>
      <StatusBar barStyle="light-content" />

      <View style={styles.headerWrapper}>
        <LinearGradient
          colors={[PRIMARY, "#00497A"]}
          start={{ x: 0, y: 0 }}
          end={{ x: 1, y: 1 }}
          style={[styles.header, { paddingTop: insets.top }]}
        >
          <View style={styles.decoCircleLarge} />
          <View style={styles.decoCircleSmall} />

          <View style={styles.navBar}>
            <TouchableOpacity
              onPress={() => router.back()}
              hitSlop={{ top: 10, bottom: 10, left: 10, right: 10 }}
              style={styles.iconButton}
            >
              <Ionicons name="chevron-back" size={26} color="#FFF" />
            </TouchableOpacity>
            <Text style={styles.headerTitle}>Mon compte</Text>
            <View style={{ width: 40 }} />
          </View>

          <View style={styles.profileSection}>
            <View style={styles.avatarContainer}>
              <Image source={profileLocalImage} style={styles.avatar} />
            </View>
            <View style={styles.identityContent}>
              <Text style={styles.userName}>{fullName}</Text>
              {genderLabel || user?.birthDate ? (
                <Text style={styles.userSub}>
                  {[genderLabel, user?.birthDate ? formatDate(user.birthDate) : null]
                    .filter(Boolean)
                    .join(" · ")}
                </Text>
              ) : null}
            </View>
          </View>
        </LinearGradient>
      </View>

      <ScrollView
        showsVerticalScrollIndicator={false}
        contentContainerStyle={{ paddingBottom: insets.bottom + 100 }}
      >
        <View style={styles.sectionOuter}>
          <Text style={styles.sectionTitle}>Informations personnelles</Text>
          <View style={styles.card}>
            <ProfileRow icon="person-outline" label="Nom complet" value={fullName} />
            <View style={styles.divider} />
            <ProfileRow icon="calendar-outline" label="Date de naissance" value={user?.birthDate ? formatDate(user.birthDate) : "—"} />
            <View style={styles.divider} />
            <ProfileRow icon="male-female-outline" label="Sexe" value={genderLabel || "—"} />
            <View style={styles.divider} />
            <ProfileRow icon="call-outline" label="Téléphone" value={user?.phone || "—"} />
            <View style={styles.divider} />
            <ProfileRow icon="mail-outline" label="Email" value={user?.email || "—"} />
            <View style={styles.divider} />
            <ProfileRow icon="location-outline" label="Adresse" value={user?.address || "—"} last />
          </View>
        </View>

        <View style={styles.sectionOuter}>
          <TouchableOpacity style={styles.logoutBtn} onPress={handleLogout} activeOpacity={0.7}>
            <Ionicons name="log-out-outline" size={20} color="#EF4444" />
            <Text style={styles.logoutText}>Se déconnecter</Text>
          </TouchableOpacity>
        </View>
      </ScrollView>
    </View>
  );
}

function ProfileRow({ icon, label, value, last }: { icon: string; label: string; value: string; last?: boolean }) {
  return (
    <View style={[styles.profileRow, last && { borderBottomWidth: 0 }]}>
      <View style={styles.profileIcon}>
        <Ionicons name={icon as any} size={18} color={PRIMARY} />
      </View>
      <View style={styles.profileContent}>
        <Text style={styles.profileLabel}>{label}</Text>
        <Text style={styles.profileValue}>{value}</Text>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: LIGHT_BG },

  headerWrapper: {
    borderBottomLeftRadius: 32, borderBottomRightRadius: 32, overflow: "hidden",
  },
  header: {
    paddingHorizontal: 24, paddingBottom: 36, position: "relative",
  },
  decoCircleLarge: {
    position: "absolute", top: -50, right: -50, width: 250, height: 250,
    borderRadius: 125, backgroundColor: "#FFFFFF", opacity: 0.06,
  },
  decoCircleSmall: {
    position: "absolute", bottom: 40, left: -20, width: 100, height: 100,
    borderRadius: 50, backgroundColor: "#FFFFFF", opacity: 0.04,
  },

  navBar: {
    flexDirection: "row", alignItems: "center", justifyContent: "space-between",
    marginBottom: 12, marginTop: 8,
  },
  headerTitle: { color: "#FFF", fontSize: 17, fontWeight: "700" },
  iconButton: { width: 40, height: 40, justifyContent: "center" },

  profileSection: { flexDirection: "row", alignItems: "center" },
  avatarContainer: { marginRight: 16 },
  avatar: {
    width: 68, height: 68, borderRadius: 22,
    borderWidth: 3, borderColor: "rgba(255,255,255,0.9)",
  },
  identityContent: { flex: 1 },
  userName: { color: "#FFF", fontSize: 22, fontWeight: "700" },
  userSub: { color: "rgba(255,255,255,0.75)", fontSize: 13, marginTop: 2 },

  sectionOuter: { marginTop: 24, paddingHorizontal: 20 },
  sectionTitle: {
    fontSize: 17, fontWeight: "700", color: TEXT_DARK,
    marginBottom: 12, marginLeft: 4,
  },

  card: {
    backgroundColor: WHITE, borderRadius: 18, paddingHorizontal: 16, paddingVertical: 4,
    shadowColor: "#000", shadowOpacity: 0.04, shadowRadius: 8, shadowOffset: { width: 0, height: 2 },
    elevation: 2,
  },

  profileRow: {
    flexDirection: "row", alignItems: "center", paddingVertical: 14,
    borderBottomWidth: 1, borderBottomColor: "#F1F5F9",
  },
  profileIcon: {
    width: 36, height: 36, borderRadius: 12, backgroundColor: LIGHT_BG,
    justifyContent: "center", alignItems: "center", marginRight: 14,
  },
  profileContent: { flex: 1 },
  profileLabel: { fontSize: 12, color: TEXT_MUTED },
  profileValue: { fontSize: 15, fontWeight: "600", color: TEXT_DARK, marginTop: 1 },

  divider: { height: 1, backgroundColor: "#F1F5F9" },

  logoutBtn: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "center",
    backgroundColor: "#FEF2F2",
    borderRadius: 18,
    paddingVertical: 16,
    borderWidth: 1,
    borderColor: "#FEE2E2",
    gap: 10,
  },
  logoutText: {
    fontSize: 15,
    fontWeight: "700",
    color: "#EF4444",
  },
});