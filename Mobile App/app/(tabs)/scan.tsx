import { Ionicons } from "@expo/vector-icons";
import { router } from "expo-router";
import React from "react";
import {
  Image,
  StyleSheet,
  Text,
  TouchableOpacity,
  View,
} from "react-native";

const PRIMARY = "#0066A2";
const WHITE = "#FFFFFF";

export default function ScanScreen() {
  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.headerTitle}>Scanner une ordonnance</Text>
        <Text style={styles.headerSub}>
          Prenez une photo de votre ordonnance pour que nous puissions
          l'enregistrer automatiquement.
        </Text>
      </View>

      <View style={styles.scanFrame}>
        <View style={styles.scanArea}>
          <Ionicons name="scan-outline" size={80} color={PRIMARY} />
          <Text style={styles.scanHint}>
            Placez l'ordonnance dans le cadre
          </Text>
        </View>
      </View>

      <View style={styles.actions}>
        <TouchableOpacity style={styles.cameraBtn} activeOpacity={0.85}>
          <Ionicons name="camera" size={28} color={WHITE} />
          <Text style={styles.cameraBtnText}>Prendre une photo</Text>
        </TouchableOpacity>

        <TouchableOpacity
          style={styles.galleryBtn}
          activeOpacity={0.85}
        >
          <Ionicons name="images-outline" size={24} color={PRIMARY} />
          <Text style={styles.galleryBtnText}>Choisir depuis la galerie</Text>
        </TouchableOpacity>
      </View>

      <TouchableOpacity
        style={styles.manualBtn}
        activeOpacity={0.7}
        onPress={() => router.push("/(drawer)/ai-chat")}
      >
        <Ionicons name="chatbubble-ellipses-outline" size={20} color={PRIMARY} />
        <Text style={styles.manualBtnText}>
          Saisir manuellement via l'IA
        </Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#F5F8FA",
    paddingTop: 60,
    paddingHorizontal: 24,
  },
  header: {
    marginBottom: 30,
  },
  headerTitle: {
    fontSize: 26,
    fontWeight: "700",
    color: "#1E293B",
    marginBottom: 8,
  },
  headerSub: {
    fontSize: 15,
    color: "#64748B",
    lineHeight: 22,
  },
  scanFrame: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
    maxHeight: 340,
  },
  scanArea: {
    width: "100%",
    height: 280,
    borderWidth: 2,
    borderStyle: "dashed",
    borderColor: PRIMARY,
    borderRadius: 24,
    justifyContent: "center",
    alignItems: "center",
    backgroundColor: "rgba(0, 102, 162, 0.05)",
  },
  scanHint: {
    marginTop: 16,
    fontSize: 14,
    color: "#64748B",
    textAlign: "center",
  },
  actions: {
    gap: 14,
    marginBottom: 24,
  },
  cameraBtn: {
    backgroundColor: PRIMARY,
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "center",
    paddingVertical: 16,
    borderRadius: 16,
    gap: 10,
    shadowColor: PRIMARY,
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.25,
    shadowRadius: 8,
    elevation: 4,
  },
  cameraBtnText: {
    color: WHITE,
    fontSize: 17,
    fontWeight: "600",
  },
  galleryBtn: {
    backgroundColor: WHITE,
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "center",
    paddingVertical: 16,
    borderRadius: 16,
    borderWidth: 1.5,
    borderColor: PRIMARY,
    gap: 8,
  },
  galleryBtnText: {
    color: PRIMARY,
    fontSize: 16,
    fontWeight: "600",
  },
  manualBtn: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "center",
    gap: 8,
    paddingVertical: 14,
    marginBottom: 30,
  },
  manualBtnText: {
    color: PRIMARY,
    fontSize: 15,
    fontWeight: "500",
  },
});
