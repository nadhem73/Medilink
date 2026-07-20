import { useAuth } from "@/context/AuthContext";
import { Ionicons } from "@expo/vector-icons";
import { LinearGradient } from "expo-linear-gradient";
import { useRouter } from "expo-router";
import { useState } from "react";
import {
  Dimensions,
  KeyboardAvoidingView,
  Platform,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
  View,
} from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";

const COLORS = {
  primary: "#0066A2",
  secondary: "#00A8B5",
};

export default function LoginScreen() {
  const router = useRouter();
  const { login } = useAuth();
  const insets = useSafeAreaInsets();

  const [identifier, setIdentifier] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [errorMsg, setErrorMsg] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleLogin() {
    if (!identifier || !password) {
      setErrorMsg("Veuillez remplir tous les champs");
      return;
    }
    setLoading(true);
    setErrorMsg("");
    try {
      await login(identifier, password);
      router.replace("/(drawer)/home");
    } catch (error: any) {
      setErrorMsg(
        error?.response?.data?.message || error?.message || "Échec de connexion"
      );
    } finally {
      setLoading(false);
    }
  }

  return (
    <View style={styles.root}>
      <LinearGradient
        colors={["#c4e0f1", "#e6f2fb", "#d2e9f6"]}
        locations={[0, 0.48, 1]}
        style={StyleSheet.absoluteFill}
      />
      <View style={[styles.bgDecor, { top: -60, right: -60 }]}>
        <View style={[styles.decoCircle, { backgroundColor: "rgba(0,102,162,0.08)" }]} />
      </View>
      <View style={[styles.bgDecor, { bottom: -40, left: -40 }]}>
        <View style={[styles.decoCircle, { width: 200, height: 200, backgroundColor: "rgba(0,168,181,0.06)" }]} />
      </View>

      <KeyboardAvoidingView
        behavior={Platform.OS === "ios" ? "padding" : "height"}
        style={{ flex: 1 }}
      >
        <ScrollView
          contentContainerStyle={{ flexGrow: 1, paddingTop: insets.top + 20, paddingBottom: 40 }}
          keyboardShouldPersistTaps="handled"
          showsVerticalScrollIndicator={false}
        >
          <View style={styles.card}>
            {/* Brand */}
            <View style={styles.brandRow}>
              <View style={styles.brandIcon}>
                <Ionicons name="shield-checkmark" size={24} color={COLORS.primary} />
              </View>
              <Text style={styles.brandName}>MediLink Tunisia</Text>
            </View>

            {/* Header */}
            <Text style={styles.title}>Connexion</Text>
            <Text style={styles.subtitle}>
              Entrez vos identifiants pour accéder à votre espace santé.
            </Text>

            {/* Form */}
            <View style={styles.form}>
              <View style={styles.inputGroup}>
                <Text style={styles.inputLabel}>Email</Text>
                <View style={styles.inputWrap}>
                  <Ionicons name="mail-outline" size={18} color="#5A6A7A" style={styles.inputIcon} />
                  <TextInput
                    style={styles.input}
                    placeholder="exemple@email.com"
                    placeholderTextColor="rgba(90,106,122,0.5)"
                    value={identifier}
                    onChangeText={setIdentifier}
                    autoCapitalize="none"
                    keyboardType="email-address"
                  />
                </View>
              </View>

              <View style={styles.inputGroup}>
                <Text style={styles.inputLabel}>Mot de passe</Text>
                <View style={styles.inputWrap}>
                  <Ionicons name="lock-closed-outline" size={18} color="#5A6A7A" style={styles.inputIcon} />
                  <TextInput
                    style={styles.input}
                    placeholder="••••••••"
                    placeholderTextColor="rgba(90,106,122,0.5)"
                    value={password}
                    onChangeText={setPassword}
                    secureTextEntry={!showPassword}
                  />
                  <TouchableOpacity onPress={() => setShowPassword(!showPassword)} style={styles.eyeBtn}>
                    <Ionicons
                      name={showPassword ? "eye-off-outline" : "eye-outline"}
                      size={20}
                      color="#5A6A7A"
                    />
                  </TouchableOpacity>
                </View>
              </View>

              <TouchableOpacity style={styles.forgotRow}>
                <Text style={[styles.forgotText, { color: COLORS.secondary }]}>
                  Mot de passe oublié ?
                </Text>
              </TouchableOpacity>

              {errorMsg ? (
                <View style={styles.errorBanner}>
                  <Ionicons name="alert-circle" size={16} color="#b23b30" />
                  <Text style={styles.errorText}>{errorMsg}</Text>
                </View>
              ) : null}

              <TouchableOpacity
                style={[styles.submitBtn, loading && { opacity: 0.5 }]}
                onPress={handleLogin}
                disabled={loading}
              >
                <Ionicons name="log-in-outline" size={18} color="#FFF" />
                <Text style={styles.submitText}>
                  {loading ? "Connexion..." : "Connexion"}
                </Text>
              </TouchableOpacity>
            </View>

            {/* Footer */}
            <Text style={styles.footer}>
              Vous n'avez pas de compte ?{" "}
              <Text
                style={[styles.footerLink, { color: COLORS.secondary }]}
                onPress={() => router.push("/(auth)/register")}
              >
                Créer un compte
              </Text>
            </Text>
          </View>
        </ScrollView>
      </KeyboardAvoidingView>
    </View>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1 },
  bgDecor: { position: "absolute" },
  decoCircle: { width: 250, height: 250, borderRadius: 125 },

  card: {
    marginHorizontal: 20,
    marginTop: 80,
    backgroundColor: "rgba(255,255,255,0.94)",
    borderRadius: 26,
    padding: 28,
    borderWidth: 1,
    borderColor: "rgba(0,102,162,0.16)",
    shadowColor: "#142850",
    shadowOffset: { width: 0, height: 24 },
    shadowOpacity: 0.12,
    shadowRadius: 60,
    elevation: 12,
  },

  brandRow: { flexDirection: "row", alignItems: "center", marginBottom: 20 },
  brandIcon: {
    width: 40, height: 40, borderRadius: 12,
    backgroundColor: "rgba(0,102,162,0.1)",
    justifyContent: "center", alignItems: "center", marginRight: 10,
  },
  brandName: { fontSize: 18, fontWeight: "800", color: COLORS.primary, letterSpacing: -0.2 },

  title: { fontSize: 28, fontWeight: "800", color: COLORS.primary, marginBottom: 4, letterSpacing: -0.5 },
  subtitle: { fontSize: 14, color: "#5A6A7A", marginBottom: 24 },

  form: { gap: 16 },
  inputGroup: { gap: 6 },
  inputLabel: { fontSize: 13, color: "#5A6A7A", fontWeight: "600" },
  inputWrap: {
    flexDirection: "row", alignItems: "center",
    backgroundColor: "#FFF", borderWidth: 1,
    borderColor: "rgba(0,102,162,0.16)", borderRadius: 12,
    paddingHorizontal: 14, height: 48,
  },
  inputIcon: { marginRight: 10 },
  input: { flex: 1, fontSize: 15, color: "#1A2B3C", padding: 0 },
  eyeBtn: { padding: 6 },

  forgotRow: { alignItems: "flex-end", marginTop: -4 },
  forgotText: { fontSize: 13, fontWeight: "600" },

  errorBanner: {
    flexDirection: "row", alignItems: "center", gap: 8,
    padding: 11, borderRadius: 12,
    backgroundColor: "rgba(232,93,78,0.12)",
    borderWidth: 1, borderColor: "rgba(232,93,78,0.4)",
  },
  errorText: { fontSize: 13, color: "#b23b30", flex: 1 },

  submitBtn: {
    flexDirection: "row", alignItems: "center", justifyContent: "center", gap: 8,
    paddingVertical: 14, borderRadius: 12, marginTop: 4,
    backgroundColor: COLORS.primary,
    shadowColor: COLORS.primary,
    shadowOffset: { width: 0, height: 12 },
    shadowOpacity: 0.3,
    shadowRadius: 28,
    elevation: 8,
  },
  submitText: { color: "#FFF", fontSize: 15, fontWeight: "700" },

  footer: { marginTop: 22, textAlign: "center", fontSize: 13.5, color: "#5A6A7A" },
  footerLink: { fontWeight: "700" },
});
