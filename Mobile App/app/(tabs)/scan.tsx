import * as ImagePicker from "expo-image-picker";
import { router } from "expo-router";
import React, { useState } from "react";
import {
  ActivityIndicator,
  Alert,
  StyleSheet,
  Text,
  TouchableOpacity,
  View,
} from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { bilanService } from "@/services/bilanService";

const PRIMARY = "#0066A2";
const WHITE = "#FFFFFF";

export default function ScanScreen() {
  const [loading, setLoading] = useState(false);

  const requestCameraPermission = async () => {
    const { status } = await ImagePicker.requestCameraPermissionsAsync();
    if (status !== "granted") {
      Alert.alert(
        "Permission refusée",
        "L'accès à la caméra est nécessaire pour scanner un bilan."
      );
      return false;
    }
    return true;
  };

  const processImage = async (uri: string) => {
    setLoading(true);
    try {
      const response = await bilanService.scan(uri);
      router.push({
        pathname: "/(tabs)/scan-result",
        params: { bilanId: response.id },
      });
    } catch (error: any) {
      let msg = "Impossible de traiter l'image. Vérifiez votre connexion et réessayez.";
      if (error?.response) {
        if (error.response.status === 401) {
          msg = "Session expirée. Veuillez vous reconnecter.";
        } else if (error.response.status === 422) {
          msg = error.response.data?.error || "Image illisible. Essayez avec une meilleure photo.";
        } else if (error.response.status >= 500) {
          msg = "Erreur serveur. Réessayez plus tard.";
        }
      } else if (error?.code === "ERR_NETWORK" || error?.message?.includes("Network")) {
        msg = "Impossible de joindre le serveur. Vérifiez que vous êtes sur le même réseau Wi-Fi.";
      }
      Alert.alert("Erreur", msg);
    } finally {
      setLoading(false);
    }
  };

  const handleCamera = async () => {
    const granted = await requestCameraPermission();
    if (!granted) return;

    const result = await ImagePicker.launchCameraAsync({
      mediaTypes: ["images"],
      quality: 0.8,
      allowsEditing: false,
    });

    if (!result.canceled && result.assets[0]) {
      await processImage(result.assets[0].uri);
    }
  };

  const handleGallery = async () => {
    const { status } = await ImagePicker.requestMediaLibraryPermissionsAsync();
    if (status !== "granted") {
      Alert.alert(
        "Permission refusée",
        "L'accès à la galerie est nécessaire pour choisir une image."
      );
      return;
    }

    const result = await ImagePicker.launchImageLibraryAsync({
      mediaTypes: ["images"],
      quality: 0.8,
    });

    if (!result.canceled && result.assets[0]) {
      await processImage(result.assets[0].uri);
    }
  };

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.headerTitle}>Scanner un bilan</Text>
        <Text style={styles.headerSub}>
          Prenez une photo de votre bilan médical pour l'enregistrer
          automatiquement dans votre dossier.
        </Text>
      </View>

      <View style={styles.scanFrame}>
        <View style={styles.scanArea}>
          <Ionicons name="scan-outline" size={80} color={PRIMARY} />
          <Text style={styles.scanHint}>
            Placez le bilan dans le cadre
          </Text>
          <Text style={styles.scanHintSub}>
            Assurez-vous que le texte est bien lisible
          </Text>
        </View>
      </View>

      {loading && (
        <View style={styles.loadingOverlay}>
          <ActivityIndicator size="large" color={PRIMARY} />
          <Text style={styles.loadingText}>
            Analyse du bilan en cours...
          </Text>
        </View>
      )}

      <View style={styles.actions}>
        <TouchableOpacity
          style={styles.cameraBtn}
          activeOpacity={0.85}
          onPress={handleCamera}
          disabled={loading}
        >
          <Ionicons name="camera" size={28} color={WHITE} />
          <Text style={styles.cameraBtnText}>Prendre une photo</Text>
        </TouchableOpacity>

        <TouchableOpacity
          style={styles.galleryBtn}
          activeOpacity={0.85}
          onPress={handleGallery}
          disabled={loading}
        >
          <Ionicons name="images-outline" size={24} color={PRIMARY} />
          <Text style={styles.galleryBtnText}>Choisir depuis la galerie</Text>
        </TouchableOpacity>
      </View>

      <TouchableOpacity
        style={styles.manualBtn}
        activeOpacity={0.7}
        onPress={() => router.push("/(drawer)/ai-chat")}
        disabled={loading}
      >
        <Ionicons
          name="chatbubble-ellipses-outline"
          size={20}
          color={PRIMARY}
        />
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
    marginBottom: 24,
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
    maxHeight: 300,
  },
  scanArea: {
    width: "100%",
    height: 240,
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
    fontSize: 16,
    fontWeight: "600",
    color: "#1E293B",
    textAlign: "center",
  },
  scanHintSub: {
    marginTop: 4,
    fontSize: 13,
    color: "#94A3B8",
    textAlign: "center",
  },
  loadingOverlay: {
    alignItems: "center",
    marginBottom: 16,
    gap: 8,
  },
  loadingText: {
    fontSize: 14,
    color: PRIMARY,
    fontWeight: "500",
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
