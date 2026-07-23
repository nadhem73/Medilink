import { Ionicons } from "@expo/vector-icons";
import { useNavigation } from "@react-navigation/native";
import { LinearGradient } from "expo-linear-gradient";
import React, { useEffect, useRef, useState } from "react";
import {
  ActivityIndicator,
  Animated,
  FlatList,
  Keyboard,
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
  View,
} from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { aiService, DoctorInfo } from "@/services/aiService";

const PRIMARY = "#0066A2";
const LIGHT_BG = "#F0F6FA";
const WHITE = "#FFFFFF";
const USER_BG = "#0066A2";

interface Suggestion {
  id: string;
  text: string;
  sender: "user" | "bot";
  doctor_suggestion?: DoctorInfo[];
  urgency?: string;
  specialty?: string;
}

const QUICK_QUESTIONS = [
  "J'ai mal à la tête depuis 3 jours",
  "J'ai de la fièvre et je tousse",
  "Douleur au ventre après les repas",
  "Je me sens fatigué tout le temps",
];

const URGENCY_STYLES: Record<string, { label: string; color: string }> = {
  URGENCE: { label: "Urgence", color: "#E74C3C" },
  HAUTE: { label: "Haute priorité", color: "#E74C3C" },
  MOYENNE: { label: "Priorité moyenne", color: "#F39C12" },
  BASSE: { label: "Priorité basse", color: "#28A745" },
};

function BotAvatar() {
  return (
    <View style={styles.botAvatar}>
      <Ionicons name="medkit" size={16} color={PRIMARY} />
    </View>
  );
}

function UserAvatar() {
  return (
    <View style={styles.userAvatar}>
      <Ionicons name="person" size={16} color={WHITE} />
    </View>
  );
}

function UrgencyBadge({ urgency }: { urgency?: string }) {
  if (!urgency) return null;
  const info = URGENCY_STYLES[urgency.toUpperCase()];
  if (!info) return null;
  return (
    <View style={[styles.badge, { backgroundColor: info.color + "15" }]}>
      <View style={[styles.badgeDot, { backgroundColor: info.color }]} />
      <Text style={[styles.badgeText, { color: info.color }]}>{info.label}</Text>
    </View>
  );
}

function SpecialtyBadge({ specialty }: { specialty?: string }) {
  if (!specialty) return null;
  return (
    <View style={styles.specBadge}>
      <Ionicons name="medical-outline" size={13} color={PRIMARY} />
      <Text style={styles.specText}>{specialty}</Text>
    </View>
  );
}

function DoctorCard({ doctor }: { doctor: DoctorInfo }) {
  const nav = useNavigation();
  return (
    <TouchableOpacity
      style={styles.docCard}
      onPress={() => nav.navigate("explore" as never)}
      activeOpacity={0.7}
    >
      <View style={styles.docAvatar}>
        <Ionicons name="person" size={20} color={PRIMARY} />
      </View>
      <View style={styles.docInfo}>
        <Text style={styles.docName}>{doctor.name}</Text>
        <Text style={styles.docSpec}>{doctor.specialty}</Text>
        <Text style={styles.docCity}>{doctor.city}</Text>
      </View>
      <Ionicons name="chevron-forward" size={18} color="#94A3B8" />
    </TouchableOpacity>
  );
}

function AnimatedMessage({ item, index }: { item: Suggestion; index: number }) {
  const nav = useNavigation();
  const fade = useRef(new Animated.Value(0)).current;
  const slide = useRef(new Animated.Value(25)).current;

  useEffect(() => {
    Animated.parallel([
      Animated.timing(fade, { toValue: 1, duration: 350, delay: 30, useNativeDriver: true }),
      Animated.timing(slide, { toValue: 0, duration: 350, delay: 30, useNativeDriver: true }),
    ]).start();
  }, [fade, slide]);

  const isUser = item.sender === "user";

  return (
    <Animated.View style={[styles.msgRow, isUser && styles.msgRowUser, { opacity: fade, transform: [{ translateY: slide }] }]}>
      {!isUser && <BotAvatar />}
      <View style={[styles.bubble, isUser ? styles.userBubble : styles.botBubble]}>
        <UrgencyBadge urgency={item.urgency} />
        <SpecialtyBadge specialty={item.specialty} />
        <Text style={[styles.msgText, isUser && styles.msgTextUser]}>{item.text}</Text>
        {item.doctor_suggestion && item.doctor_suggestion.length > 0 && (
          <View style={styles.docSection}>
            <Text style={styles.docSectionTitle}>Médecins disponibles</Text>
            {item.doctor_suggestion.map((d) => <DoctorCard key={d.id} doctor={d} />)}
            <TouchableOpacity style={styles.bookBtn} onPress={() => nav.navigate("appointments" as never)} activeOpacity={0.8}>
              <Ionicons name="calendar-outline" size={16} color={WHITE} />
              <Text style={styles.bookBtnText}>Prendre rendez-vous</Text>
            </TouchableOpacity>
          </View>
        )}
      </View>
      {isUser && <UserAvatar />}
    </Animated.View>
  );
}

function TypingIndicator() {
  const d1 = useRef(new Animated.Value(0)).current;
  const d2 = useRef(new Animated.Value(0)).current;
  const d3 = useRef(new Animated.Value(0)).current;

  useEffect(() => {
    const pulse = (d: Animated.Value, delay: number) =>
      Animated.loop(Animated.sequence([
        Animated.delay(delay),
        Animated.timing(d, { toValue: 1, duration: 400, useNativeDriver: true }),
        Animated.timing(d, { toValue: 0, duration: 400, useNativeDriver: true }),
        Animated.delay(600),
      ]));
    pulse(d1, 0).start();
    pulse(d2, 200).start();
    pulse(d3, 400).start();
  }, [d1, d2, d3]);

  const t = (d: Animated.Value) => d.interpolate({ inputRange: [0, 1], outputRange: [0.3, 1] });
  const s = (d: Animated.Value) => d.interpolate({ inputRange: [0, 1], outputRange: [0.8, 1.2] });

  return (
    <View style={styles.msgRow}>
      <BotAvatar />
      <View style={styles.typingBubble}>
        <Animated.View style={[styles.tDot, { opacity: t(d1), transform: [{ scale: s(d1) }] }]} />
        <Animated.View style={[styles.tDot, { opacity: t(d2), transform: [{ scale: s(d2) }] }]} />
        <Animated.View style={[styles.tDot, { opacity: t(d3), transform: [{ scale: s(d3) }] }]} />
      </View>
    </View>
  );
}

export default function AiChatScreen() {
  const [messages, setMessages] = useState<Suggestion[]>([
    {
      id: "0",
      text: "Bonjour ! Je suis MediLink AI, votre assistant santé. Décrivez-moi vos symptômes pour vous orienter vers le bon spécialiste.",
      sender: "bot",
    },
  ]);
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(false);
  const [conversationId, setConversationId] = useState<string | undefined>();
  const [showQuestions, setShowQuestions] = useState(true);
  const [keyboardHeight, setKeyboardHeight] = useState(0);
  const flatRef = useRef<FlatList>(null);

  useEffect(() => {
    const show = Keyboard.addListener("keyboardDidShow", (e) => setKeyboardHeight(e.endCoordinates.height));
    const hide = Keyboard.addListener("keyboardDidHide", () => setKeyboardHeight(0));
    return () => { show.remove(); hide.remove(); };
  }, []);
  const nav = useNavigation();
  const insets = useSafeAreaInsets();

  const send = async (text: string) => {
    if (!text.trim() || loading) return;
    Keyboard.dismiss();
    setInput("");
    setShowQuestions(false);
    setMessages((prev) => [...prev, { id: Date.now().toString(), text: text.trim(), sender: "user" }]);
    setLoading(true);
    try {
      const res = await aiService.sendMessage(text.trim(), conversationId);
      setConversationId(res.conversation_id);
      setMessages((prev) => [...prev, {
        id: (Date.now() + 1).toString(),
        text: res.answer,
        sender: "bot",
        doctor_suggestion: res.doctors,
        urgency: res.urgency_level ?? undefined,
        specialty: res.recommended_specialty ?? undefined,
      }]);
    } catch {
      setMessages((prev) => [...prev, {
        id: (Date.now() + 1).toString(),
        text: "Désolé, une erreur est survenue. Veuillez réessayer.",
        sender: "bot",
      }]);
    } finally {
      setLoading(false);
    }
  };

  const reset = () => {
    setMessages([{ id: "0", text: "Bonjour ! Je suis MediLink AI, votre assistant santé. Décrivez-moi vos symptômes pour vous orienter vers le bon spécialiste.", sender: "bot" }]);
    setConversationId(undefined);
    setShowQuestions(true);
  };

  const data = loading ? [...messages, { id: "typing", text: "", sender: "bot" as const }] : messages;

  return (
    <View style={styles.flex}>
      <LinearGradient
        colors={[PRIMARY, "#00497A"]}
        start={{ x: 0, y: 0 }}
        end={{ x: 1, y: 1 }}
        style={[styles.header, { paddingTop: insets.top + 8 }]}
        onLayout={(e) => { const _h = e.nativeEvent.layout.height; }}
      >
        <View style={styles.headerDeco1} />
        <View style={styles.headerDeco2} />
        <View style={styles.headerRow}>
          <TouchableOpacity onPress={() => nav.goBack()} hitSlop={{ top: 10, bottom: 10, left: 10, right: 10 }}>
            <Ionicons name="chevron-back" size={26} color={WHITE} />
          </TouchableOpacity>
          <View style={styles.headerCenter}>
            <View style={styles.headerDot} />
            <Text style={styles.headerTitle}>MediLink AI</Text>
          </View>
          <TouchableOpacity onPress={reset} hitSlop={{ top: 10, bottom: 10, left: 10, right: 10 }}>
            <Ionicons name="refresh" size={24} color={WHITE} />
          </TouchableOpacity>
        </View>
        <View style={styles.headerSubRow}>
          <Text style={styles.headerSub}>Assistant santé intelligent</Text>
        </View>
      </LinearGradient>

      <View style={styles.flex}>
        <FlatList
          ref={flatRef}
          data={data}
          keyExtractor={(item) => item.id}
          renderItem={({ item, index }) => {
            if (item.id === "typing") return <TypingIndicator />;
            return <AnimatedMessage item={item} index={index} />;
          }}
          onContentSizeChange={() => flatRef.current?.scrollToEnd()}
          onScroll={() => Keyboard.dismiss()}
          scrollEventThrottle={16}
          style={styles.flex}
          contentContainerStyle={styles.listPad}
          showsVerticalScrollIndicator={false}
          keyboardShouldPersistTaps="handled"
        />

        {showQuestions && (
          <View style={styles.qSection}>
            <Text style={styles.qTitle}>Questions fréquentes</Text>
            <View style={styles.qRow}>
              {QUICK_QUESTIONS.map((q, i) => (
                <TouchableOpacity key={i} style={styles.qChip} onPress={() => send(q)} activeOpacity={0.7}>
                  <Text style={styles.qChipText}>{q}</Text>
                </TouchableOpacity>
              ))}
            </View>
          </View>
        )}
      </View>

      <View style={[styles.inputBar, { paddingBottom: Math.max(insets.bottom, 8) + 8, marginBottom: keyboardHeight }]}>
        <View style={styles.inputWrap}>
          <TextInput
            style={styles.input}
            placeholder="Décrivez vos symptômes..."
            placeholderTextColor="#94A3B8"
            value={input}
            onChangeText={setInput}
            multiline
            maxLength={2000}
          />
          <TouchableOpacity
            style={[styles.sendBtn, (!input.trim() || loading) && styles.sendBtnDisabled]}
            onPress={() => send(input)}
            disabled={!input.trim() || loading}
            activeOpacity={0.8}
          >
            {loading ? (
              <ActivityIndicator color={WHITE} size="small" />
            ) : (
              <Ionicons name="send" size={18} color={WHITE} />
            )}
          </TouchableOpacity>
        </View>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  flex: { flex: 1, backgroundColor: LIGHT_BG },

  /* HEADER */
  header: {
    paddingBottom: 16,
    paddingHorizontal: 16,
    overflow: "hidden",
    borderBottomLeftRadius: 28,
    borderBottomRightRadius: 28,
  },
  headerDeco1: {
    position: "absolute", top: -40, right: -30, width: 160, height: 160,
    borderRadius: 80, backgroundColor: WHITE, opacity: 0.06,
  },
  headerDeco2: {
    position: "absolute", bottom: -20, left: -60, width: 120, height: 120,
    borderRadius: 60, backgroundColor: WHITE, opacity: 0.04,
  },
  headerRow: {
    flexDirection: "row", alignItems: "center", justifyContent: "space-between",
  },
  headerCenter: { flexDirection: "row", alignItems: "center" },
  headerDot: {
    width: 10, height: 10, borderRadius: 5, backgroundColor: "#28A745", marginRight: 8,
  },
  headerTitle: { color: WHITE, fontSize: 18, fontWeight: "700" },
  headerSubRow: { alignItems: "center", marginTop: 4 },
  headerSub: { color: "rgba(255,255,255,0.7)", fontSize: 13 },

  /* LIST */
  listPad: { paddingVertical: 16, paddingHorizontal: 12 },

  /* MESSAGES */
  msgRow: {
    flexDirection: "row", marginBottom: 14, alignItems: "flex-end",
  },
  msgRowUser: { justifyContent: "flex-end" },

  bubble: {
    maxWidth: "78%", padding: 14, borderRadius: 20, elevation: 2,
    shadowColor: "#000", shadowOpacity: 0.05, shadowRadius: 6, shadowOffset: { width: 0, height: 2 },
  },
  botBubble: {
    backgroundColor: WHITE, borderBottomLeftRadius: 4,
    marginLeft: 10,
  },
  userBubble: {
    backgroundColor: USER_BG, borderBottomRightRadius: 4,
    marginRight: 10,
  },
  msgText: { fontSize: 15, color: "#1E293B", lineHeight: 22 },
  msgTextUser: { color: WHITE },

  /* AVATARS */
  botAvatar: {
    width: 34, height: 34, borderRadius: 12,
    backgroundColor: "#D5E9FF", justifyContent: "center", alignItems: "center",
  },
  userAvatar: {
    width: 34, height: 34, borderRadius: 12,
    backgroundColor: PRIMARY, justifyContent: "center", alignItems: "center",
  },

  /* BADGES */
  badge: {
    flexDirection: "row", alignItems: "center", alignSelf: "flex-start",
    paddingHorizontal: 10, paddingVertical: 4, borderRadius: 8, marginBottom: 6,
  },
  badgeDot: { width: 8, height: 8, borderRadius: 4, marginRight: 6 },
  badgeText: { fontSize: 12, fontWeight: "700" },

  specBadge: {
    flexDirection: "row", alignItems: "center", alignSelf: "flex-start",
    backgroundColor: "#E8F2FA", paddingHorizontal: 10, paddingVertical: 4,
    borderRadius: 8, marginBottom: 8,
  },
  specText: { color: PRIMARY, fontSize: 12, fontWeight: "600", marginLeft: 5 },

  /* DOCTOR CARDS */
  docSection: { marginTop: 12, paddingTop: 12, borderTopWidth: 1, borderTopColor: "#E2E8F0" },
  docSectionTitle: { fontSize: 13, fontWeight: "700", color: PRIMARY, marginBottom: 8 },
  docCard: {
    flexDirection: "row", alignItems: "center", backgroundColor: "#F8FAFC",
    padding: 12, borderRadius: 14, marginBottom: 6,
  },
  docAvatar: {
    width: 38, height: 38, borderRadius: 14,
    backgroundColor: "#E8F2FA", justifyContent: "center", alignItems: "center", marginRight: 12,
  },
  docInfo: { flex: 1 },
  docName: { fontSize: 14, fontWeight: "700", color: "#1E293B" },
  docSpec: { fontSize: 12, color: PRIMARY, marginTop: 1 },
  docCity: { fontSize: 11, color: "#94A3B8", marginTop: 1 },

  bookBtn: {
    flexDirection: "row", alignItems: "center", justifyContent: "center",
    backgroundColor: PRIMARY, paddingVertical: 12, borderRadius: 14, marginTop: 8,
  },
  bookBtnText: { color: WHITE, fontSize: 14, fontWeight: "700", marginLeft: 6 },

  /* TYPING */
  typingBubble: {
    flexDirection: "row", alignItems: "center",
    backgroundColor: WHITE, paddingHorizontal: 18, paddingVertical: 14,
    borderRadius: 20, borderBottomLeftRadius: 4, marginLeft: 10,
    gap: 6, elevation: 2,
    shadowColor: "#000", shadowOpacity: 0.05, shadowRadius: 6, shadowOffset: { width: 0, height: 2 },
  },
  tDot: { width: 10, height: 10, borderRadius: 5, backgroundColor: PRIMARY },

  /* QUICK QUESTIONS */
  qSection: { paddingHorizontal: 16, paddingBottom: 6 },
  qTitle: { fontSize: 13, fontWeight: "600", color: "#94A3B8", marginBottom: 8 },
  qRow: { flexDirection: "row", flexWrap: "wrap", gap: 8 },
  qChip: {
    backgroundColor: WHITE, paddingHorizontal: 16, paddingVertical: 10,
    borderRadius: 24, borderWidth: 1, borderColor: "#E2E8F0",
    elevation: 1,
    shadowColor: "#000", shadowOpacity: 0.03, shadowRadius: 4, shadowOffset: { width: 0, height: 1 },
  },
  qChipText: { color: PRIMARY, fontSize: 13, fontWeight: "500" },

  /* INPUT */
  inputBar: {
    backgroundColor: WHITE, borderTopWidth: 1, borderTopColor: "#E2E8F0",
    paddingHorizontal: 12, paddingTop: 10,
  },
  inputWrap: {
    flexDirection: "row", alignItems: "flex-end",
    backgroundColor: LIGHT_BG, borderRadius: 24,
    paddingLeft: 16, paddingRight: 4, paddingVertical: 4,
  },
  input: {
    flex: 1, fontSize: 15, color: "#1E293B",
    maxHeight: 100, paddingVertical: 8,
  },
  sendBtn: {
    backgroundColor: PRIMARY, width: 38, height: 38, borderRadius: 19,
    justifyContent: "center", alignItems: "center",
  },
  sendBtnDisabled: { opacity: 0.5 },
});