import { useAuth } from "@/context/AuthContext";
import { Ionicons } from "@expo/vector-icons";
import { LinearGradient } from "expo-linear-gradient";
import { useRouter } from "expo-router";
import { useState } from "react";
import {
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

export default function RegisterScreen() {
  const router = useRouter();
  const { register } = useAuth();
  const insets = useSafeAreaInsets();

  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [email, setEmail] = useState("");
  const [phone, setPhone] = useState("");
  const [cin, setCin] = useState("");
  const [password, setPassword] = useState("");
  const [confirm, setConfirm] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [errorMsg, setErrorMsg] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleRegister() {
    if (!firstName || !lastName || !email || !phone || !cin || !password || !confirm) {
      setErrorMsg("Veuillez remplir tous les champs");
      return;
    }
    if (password !== confirm) {
      setErrorMsg("Les mots de passe ne correspondent pas");
      return;
    }
    setLoading(true);
    setErrorMsg("");
    try {
      await register({ firstName, lastName, email, phone, cin, password });
      router.replace("/(auth)/login");
    } catch (error: any) {
      setErrorMsg(
        error?.response?.data?.message || error?.message || "Échec d'inscription"
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
            <Text style={styles.title}>Inscription</Text>
            <Text style={styles.subtitle}>
              Rejoignez MediLink Tunisia et gérez votre santé.
            </Text>

            {/* Form */}
            <View style={styles.form}>
              <View style={styles.row2}>
                <View style={[styles.inputGroup, { flex: 1 }]}>
                  <Text style={styles.inputLabel}>Prénom</Text>
                  <View style={styles.inputWrap}>
                    <Ionicons name="person-outline" size={18} color="#5A6A7A" style={styles.inputIcon} />
                    <TextInput
                      style={styles.input}
                      placeholder="Prénom"
                      placeholderTextColor="rgba(90,106,122,0.5)"
                      value={firstName}
                      onChangeText={setFirstName}
                    />
                  </View>
                </View>
                <View style={[styles.inputGroup, { flex: 1 }]}>
                  <Text style={styles.inputLabel}>Nom</Text>
                  <View style={styles.inputWrap}>
                    <Ionicons name="person-outline" size={18} color="#5A6A7A" style={styles.inputIcon} />
                    <TextInput
                      style={styles.input}
                      placeholder="Nom"
                      placeholderTextColor="rgba(90,106,122,0.5)"
                      value={lastName}
                      onChangeText={setLastName}
                    />
                  </View>
                </View>
              </View>

              <View style={styles.inputGroup}>
                <Text style={styles.inputLabel}>Email</Text>
                <View style={styles.inputWrap}>
                  <Ionicons name="mail-outline" size={18} color="#5A6A7A" style={styles.inputIcon} />
                  <TextInput
                    style={styles.input}
                    placeholder="exemple@email.com"
                    placeholderTextColor="rgba(90,106,122,0.5)"
                    value={email}
                    onChangeText={setEmail}
                    autoCapitalize="none"
                    keyboardType="email-address"
                  />
                </View>
              </View>

              <View style={styles.inputGroup}>
                <Text style={styles.inputLabel}>Téléphone</Text>
                <View style={styles.inputWrap}>
                  <Ionicons name="call-outline" size={18} color="#5A6A7A" style={styles.inputIcon} />
                  <TextInput
                    style={styles.input}
                    placeholder="+216 XX XXX XXX"
                    placeholderTextColor="rgba(90,106,122,0.5)"
                    value={phone}
                    onChangeText={setPhone}
                    keyboardType="phone-pad"
                  />
                </View>
              </View>

              <View style={styles.inputGroup}>
                <Text style={styles.inputLabel}>CIN</Text>
                <View style={styles.inputWrap}>
                  <Ionicons name="card-outline" size={18} color="#5A6A7A" style={styles.inputIcon} />
                  <TextInput
                    style={styles.input}
                    placeholder="Numéro de carte d'identité"
                    placeholderTextColor="rgba(90,106,122,0.5)"
                    value={cin}
                    onChangeText={setCin}
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

              <View style={styles.inputGroup}>
                <Text style={styles.inputLabel}>Confirmer le mot de passe</Text>
                <View style={styles.inputWrap}>
                  <Ionicons name="checkmark-circle-outline" size={18} color="#5A6A7A" style={styles.inputIcon} />
                  <TextInput
                    style={styles.input}
                    placeholder="••••••••"
                    placeholderTextColor="rgba(90,106,122,0.5)"
                    value={confirm}
                    onChangeText={setConfirm}
                    secureTextEntry
                  />
                </View>
              </View>

              {errorMsg ? (
                <View style={styles.errorBanner}>
                  <Ionicons name="alert-circle" size={16} color="#b23b30" />
                  <Text style={styles.errorText}>{errorMsg}</Text>
                </View>
              ) : null}

              <TouchableOpacity
                style={[styles.submitBtn, loading && { opacity: 0.5 }]}
                onPress={handleRegister}
                disabled={loading}
              >
                <Ionicons name="person-add-outline" size={18} color="#FFF" />
                <Text style={styles.submitText}>
                  {loading ? "Inscription..." : "Créer un compte"}
                </Text>
              </TouchableOpacity>
            </View>

            {/* Footer */}
            <Text style={styles.footer}>
              Déjà un compte ?{" "}
              <Text
                style={[styles.footerLink, { color: COLORS.secondary }]}
                onPress={() => router.push("/(auth)/login")}
              >
                Se connecter
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
    marginHorizontal: 16,
    marginTop: 30,
    backgroundColor: "rgba(255,255,255,0.94)",
    borderRadius: 26,
    padding: 24,
    borderWidth: 1,
    borderColor: "rgba(0,102,162,0.16)",
    shadowColor: "#142850",
    shadowOffset: { width: 0, height: 24 },
    shadowOpacity: 0.12,
    shadowRadius: 60,
    elevation: 12,
  },

  brandRow: { flexDirection: "row", alignItems: "center", marginBottom: 16 },
  brandIcon: {
    width: 40, height: 40, borderRadius: 12,
    backgroundColor: "rgba(0,102,162,0.1)",
    justifyContent: "center", alignItems: "center", marginRight: 10,
  },
  brandName: { fontSize: 18, fontWeight: "800", color: COLORS.primary, letterSpacing: -0.2 },

  title: { fontSize: 26, fontWeight: "800", color: COLORS.primary, marginBottom: 4, letterSpacing: -0.5 },
  subtitle: { fontSize: 14, color: "#5A6A7A", marginBottom: 22 },

  form: { gap: 14 },
  row2: { flexDirection: "row", gap: 10 },
  inputGroup: { gap: 6 },
  inputLabel: { fontSize: 13, color: "#5A6A7A", fontWeight: "600" },
  inputWrap: {
    flexDirection: "row", alignItems: "center",
    backgroundColor: "#FFF", borderWidth: 1,
    borderColor: "rgba(0,102,162,0.16)", borderRadius: 12,
    paddingHorizontal: 14, height: 46,
  },
  inputIcon: { marginRight: 10 },
  input: { flex: 1, fontSize: 15, color: "#1A2B3C", padding: 0 },
  eyeBtn: { padding: 6 },

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

  footer: { marginTop: 20, textAlign: "center", fontSize: 13.5, color: "#5A6A7A" },
  footerLink: { fontWeight: "700" },
});
