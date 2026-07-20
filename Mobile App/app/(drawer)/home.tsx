import { Ionicons } from "@expo/vector-icons";
import { router } from "expo-router";
import React, { useCallback, useEffect, useRef, useState } from "react";
import {
  Alert,
  Dimensions,
  Image,
  Linking,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
  View,
} from "react-native";
import Animated, { FadeInUp } from "react-native-reanimated";
import { patientService, Appointment, DoctorInfo } from "@/services/patientService";

const PRIMARY = "#0066A2";
const LIGHT = "#F0F6FA";
const WHITE = "#FFFFFF";
const SCREEN_WIDTH = Dimensions.get("window").width;

interface Tip { title: string; text: string; img: number; link: string }

const ALL_TIPS: Tip[] = [
  { title: "Restez hydraté", text: "Buvez au moins 8 verres d'eau par jour pour maintenir une bonne santé.", img: require("../../assets/images/tips/hydratation.jpg"), link: "https://www.who.int/fr/news-room/fact-sheets/detail/drinking-water" },
  { title: "Renforcez votre immunité", text: "Mangez des fruits riches en vitamine C chaque matin.", img: require("../../assets/images/tips/immunite.jpg"), link: "https://www.who.int/fr/news-room/fact-sheets/detail/healthy-diet" },
  { title: "Améliorez votre sommeil", text: "Évitez les écrans 1h avant le coucher pour un sommeil réparateur.", img: require("../../assets/images/tips/sommeil.jpg"), link: "https://www.who.int/fr/news-room/fact-sheets/detail/sleep" },
  { title: "Faites de l'exercice", text: "30 minutes de marche par jour réduisent les risques cardiovasculaires.", img: require("../../assets/images/tips/exercice.jpg"), link: "https://www.who.int/fr/news-room/fact-sheets/detail/physical-activity" },
  { title: "Gérez votre stress", text: "La méditation 5 minutes par jour améliore votre bien-être mental.", img: require("../../assets/images/tips/stress.jpg"), link: "https://www.who.int/fr/news-room/fact-sheets/detail/mental-health-strengthening-our-response" },
  { title: "Alimentation équilibrée", text: "Privilégiez les légumes, protéines maigres et bonnes graisses.", img: require("../../assets/images/tips/alimentation.jpg"), link: "https://www.who.int/fr/news-room/fact-sheets/detail/healthy-diet" },
  { title: "Hygiène des mains", text: "Lavez-vous les mains régulièrement pour éviter les infections.", img: require("../../assets/images/tips/hygiene.jpg"), link: "https://www.who.int/fr/news-room/fact-sheets/detail/hand-hygiene" },
  { title: "Examen annuel", text: "Consultez votre médecin au moins une fois par an pour un bilan.", img: require("../../assets/images/tips/examen.jpg"), link: "https://www.who.int/fr/news-room/fact-sheets/detail/universal-health-coverage-(uhc)" },
];

export default function Home() {

  const [activeTab, setActiveTab] = useState("Upcoming");
  const [tipsIndex, setTipsIndex] = useState(0);
  const [appointments, setAppointments] = useState<Appointment[]>([]);
  const [doctorMap, setDoctorMap] = useState<Record<number, DoctorInfo>>({});
  const [loading, setLoading] = useState(true);
  const scrollRef = useRef<ScrollView>(null);

  const serviceCards = [
    { title: "Bilan général", subtitle: "Examens de santé réguliers", bg: "#EAF4FF", iconBg: "#D5E9FF", icon: "medkit-outline" },
    { title: "Consultation", subtitle: "Visio ou en cabinet", bg: "#FFF5E4", iconBg: "#FFE7C6", icon: "chatbubble-ellipses-outline" },
    { title: "Analyses", subtitle: "Vos résultats au même endroit", bg: "#F1E9FF", iconBg: "#E1D0FF", icon: "document-text-outline" },
    { title: "Bien-être", subtitle: "Sport et hygiène de vie", bg: "#FFECEC", iconBg: "#FFD4D4", icon: "barbell-outline" },
  ];

  const [displayedTips, setDisplayedTips] = useState<Tip[]>([]);

  useEffect(() => {
    const shuffled = [...ALL_TIPS].sort(() => Math.random() - 0.5);
    setDisplayedTips(shuffled.slice(0, 3));
  }, []);

  const openTipLink = async (url: string) => {
    try {
      const supported = await Linking.canOpenURL(url);
      if (supported) await Linking.openURL(url);
      else Alert.alert("Lien indisponible", url);
    } catch {
      Alert.alert("Erreur", "Impossible d'ouvrir le lien.");
    }
  };

  const fetchData = useCallback(async () => {
    try {
      const [apps, doctors] = await Promise.all([
        patientService.getMyAppointments(),
        patientService.getDoctors(),
      ]);
      setAppointments(apps);
      const map: Record<number, DoctorInfo> = {};
      for (const d of doctors) map[d.id] = d;
      setDoctorMap(map);
    } catch {} finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  useEffect(() => {
    if (displayedTips.length === 0) return;
    const timer = setInterval(() => {
      const nextIndex = (tipsIndex + 1) % displayedTips.length;
      setTipsIndex(nextIndex);

      scrollRef.current?.scrollTo({
        x: nextIndex * (SCREEN_WIDTH - 60),
        animated: true,
      });
    }, 4000);

    return () => clearInterval(timer);
  }, [tipsIndex, displayedTips.length]);

  const cancelledAppointment = async (id: number) => {
    try {
      await patientService.cancelAppointment(id);
      fetchData();
    } catch {}
  };

  const statusLabel = (status: string) => {
    const s = status.toUpperCase();
    if (s === "CONFIRMED") return "Confirmé";
    if (s === "PENDING") return "En attente";
    if (s === "COMPLETED") return "Terminé";
    if (s === "CANCELLED") return "Annulé";
    return status;
  };

  const formatDate = (dt: string) => {
    const d = new Date(dt);
    return d.toLocaleDateString("fr-TN", { day: "numeric", month: "short", year: "numeric" });
  };

  const formatTime = (dt: string) => {
    const d = new Date(dt);
    return d.toLocaleTimeString("fr-TN", { hour: "2-digit", minute: "2-digit" });
  }; 

  const filtered = appointments.filter((a) => {
    const s = a.status.toUpperCase();
    if (activeTab === "Upcoming") return s === "PENDING" || s === "CONFIRMED";
    if (activeTab === "Completed") return s === "COMPLETED";
    if (activeTab === "Cancelled") return s === "CANCELLED";
    return true;
  });

  return (
    <ScrollView style={styles.container} showsVerticalScrollIndicator={false}>

      {/* ---------------- HEADER ---------------- */}
      <Animated.View entering={FadeInUp} style={styles.headerWrapper}>
        <View style={styles.headerRow}>
          <TouchableOpacity onPress={() => router.push("/(drawer)/ai-chat")}>
            <Ionicons name="chatbubble-ellipses-outline" size={28} color="white" />
          </TouchableOpacity>

          <View>
            <Text style={styles.headerWelcome}>Bienvenue sur</Text>
            <Text style={styles.headerBig}>MediLink Tunisia</Text>
          </View>

          <Image
            source={require("../../assets/images/profile.png")}
            style={styles.avatar}
          />
        </View>

        <View style={styles.searchBar}>
          <Ionicons name="search-outline" size={20} color="#6B7B80" />
          <TextInput
            placeholder="Rechercher des services..."
            placeholderTextColor="#7A8A8F"
            style={styles.searchInput}
          />
        </View>
      </Animated.View>

      {/* ---------------- GRADIENT BANNER ---------------- */}
      <Animated.View entering={FadeInUp.delay(80)} style={styles.gradientBanner}>
        <View style={styles.curve1} />
        <View style={styles.curve2} />

        <View style={{ flex: 1, zIndex: 3 }}>
          <Text style={styles.bannerTitle}>Votre santé, à portée de main</Text>
          <Text style={styles.bannerSub}>Suivi médical et bien-être.</Text>

          <TouchableOpacity style={styles.bannerBtn}>
            <Text style={styles.bannerBtnText}>Explorer</Text>
          </TouchableOpacity>
        </View>

        <Image
          source={require("../../assets/images/doctor.png")}
          style={styles.bannerDoctor}
          resizeMode="contain"
        />
      </Animated.View>

      {/* ---------------- CATEGORY ---------------- */}
      <View style={styles.sectionHeaderRow}>
        <Text style={styles.sectionTitle}>Catégories</Text>
        <Text style={styles.viewAll}>Tout voir</Text>
      </View>

      <View style={styles.serviceChips}>
        <Text style={styles.chip}>Général</Text>
        <Text style={styles.chip}>Médecins</Text>
        <Text style={styles.chip}>Bien-être</Text>
      </View>

      {/* ---------------- SERVICES GRID ---------------- */}
      <View style={styles.serviceCardGrid}>
        {serviceCards.map((s, i) => (
          <View key={i} style={[styles.serviceCardNew, { backgroundColor: s.bg }]}>
            <View style={[styles.serviceIconWrap, { backgroundColor: s.iconBg }]}>
              <Ionicons name={s.icon as any} size={26} color={PRIMARY} />
            </View>

            <Text style={styles.serviceTitle}>{s.title}</Text>
            <Text style={styles.serviceSubtitle}>{s.subtitle}</Text>

            <TouchableOpacity style={styles.serviceLearnBtn}>
              <Text style={styles.serviceLearnText}>En savoir plus</Text>
            </TouchableOpacity>
          </View>
        ))}
      </View>

      {/* ---------------- AI CONSULTATION (AFTER SERVICES) ---------------- */}
      <TouchableOpacity
        style={styles.aiContainer}
        activeOpacity={0.85}
        onPress={() => router.push("/(drawer)/ai-chat")}
      >
        <View style={styles.aiGradient}>

          <View style={styles.aiAvatarWrap}>
            <Image
              source={require("../../assets/images/avatar.png")}
              style={styles.aiAvatar}
            />
          </View>

          <Text style={styles.aiTitle}>Comment vous sentez-vous ?</Text>
          <Text style={styles.aiSubTitle}>Parlez de vos symptômes à MediLink AI</Text>

          <View style={styles.aiSmallInput}>
            <TextInput
              placeholder="Écrivez ici..."
              placeholderTextColor="#A7B5B5"
              style={styles.aiInputText}
            />
            <Ionicons name={"send" as any} size={20} color={PRIMARY} />
          </View>

          <View style={styles.aiSuggestionRow}>
            {(["Headache", "Fever", "Stress", "Cough"] as string[]).map((s, i) => (
              <Text key={i} style={styles.aiChip}>{s}</Text>
            ))}
          </View>

          <View style={styles.aiActionRow}>
            <TouchableOpacity style={styles.aiMicBtn}>
              <Ionicons name="mic-outline" size={22} color={PRIMARY} />
            </TouchableOpacity>

            <TouchableOpacity style={styles.aiCameraBtn}>
              <Ionicons name="camera" size={20} color="white" />
            </TouchableOpacity>
          </View>

        </View>
      </TouchableOpacity>

      {/* ---------------- HEALTH TIPS ---------------- */}
      <View style={styles.sectionHeaderRow}>
        <Text style={styles.sectionTitle}>Conseils santé</Text>
      </View>

      <ScrollView
        ref={scrollRef}
        horizontal
        showsHorizontalScrollIndicator={false}
        pagingEnabled
        style={{ paddingLeft: 16 }}
      >
        {displayedTips.map((tip, i) => (
          <View key={i} style={styles.tipCard}>
            <Image source={tip.img} style={styles.tipImage} />
            <Text style={styles.tipTitle}>{tip.title}</Text>
            <Text style={styles.tipText}>{tip.text}</Text>

            <TouchableOpacity style={styles.tipBtn} onPress={() => openTipLink(tip.link)}>
              <Text style={styles.tipBtnText}>En savoir plus</Text>
              <Ionicons name="chevron-forward" color={WHITE} size={16} />
            </TouchableOpacity>
          </View>
        ))}
      </ScrollView>

      {/* ---------------- TABS ---------------- */}
      <View style={styles.tabsContainer}>
        {[{ key: "Upcoming", label: "À venir" }, { key: "Completed", label: "Terminés" }, { key: "Cancelled", label: "Annulés" }].map((tab) => (
          <TouchableOpacity
            key={tab.key}
            onPress={() => setActiveTab(tab.key)}
            style={[styles.tabBtn, activeTab === tab.key && styles.tabBtnActive]}
          >
            <Text style={[styles.tabText, activeTab === tab.key && styles.tabTextActive]}>
              {tab.label}
            </Text>
          </TouchableOpacity>
        ))}
      </View>

      {/* ---------------- APPOINTMENT LIST ---------------- */}
      {loading ? (
        <Text style={{ textAlign: "center", color: "#6C7A7A", marginVertical: 20 }}>Chargement...</Text>
      ) : filtered.length === 0 ? (
        <Text style={{ textAlign: "center", color: "#6C7A7A", marginVertical: 20 }}>Aucun rendez-vous {activeTab === "Upcoming" ? "à venir" : activeTab === "Completed" ? "terminé" : "annulé"}</Text>
      ) : (
        filtered.map((appt) => {
          const isCancelled = appt.status.toUpperCase() === "CANCELLED";
          return (
            <View key={appt.id} style={styles.appointmentCard}>
              <View style={styles.appTopRow}>
                <View style={styles.apptInfoLeft}>
                  <Image
                    source={require("../../assets/images/doctor.png")}
                    style={styles.apptAvatarSmall}
                  />
                  <View>
                    <Text style={styles.appDoctor}>
                      {doctorMap[appt.doctorId]
                        ? `${doctorMap[appt.doctorId].firstName} ${doctorMap[appt.doctorId].lastName}`
                        : `Dr. #${appt.doctorId}`}
                    </Text>
                    <Text style={styles.appSpeciality}>
                      {doctorMap[appt.doctorId]?.specialty ?? "Généraliste"}
                    </Text>
                  </View>
                </View>
                <View style={[styles.apptStatusPill, isCancelled ? styles.apptStatusCancelled : appt.status.toUpperCase() === "COMPLETED" ? styles.apptStatusCompleted : styles.apptStatusActive]}>
                  <Text style={[styles.apptStatusPillText, isCancelled ? { color: "#C0392B" } : appt.status.toUpperCase() === "COMPLETED" ? { color: "#28A745" } : { color: "#0066A2" }]}>
                    {statusLabel(appt.status)}
                  </Text>
                </View>
              </View>

              <View style={styles.apptDivider} />

              <View style={styles.appInfoRow}>
                <View style={styles.appInfoItem}>
                  <Ionicons name="calendar-outline" size={16} color="#6B7B80" />
                  <Text style={styles.appInfoText}>{formatDate(appt.dateTime)}</Text>
                </View>
                <View style={styles.appInfoItem}>
                  <Ionicons name="time-outline" size={16} color="#6B7B80" />
                  <Text style={styles.appInfoText}>{formatTime(appt.dateTime)}</Text>
                </View>
              </View>

              {!isCancelled && appt.status.toUpperCase() !== "COMPLETED" && (
                <View style={styles.appBtnRow}>
                  <TouchableOpacity style={styles.appCancelBtn} onPress={() => cancelledAppointment(appt.id)}>
                    <Text style={styles.appCancelText}>Annuler</Text>
                  </TouchableOpacity>
                  <TouchableOpacity style={styles.appRescheduleBtn} onPress={() => router.push("/(tabs)/appointments")}>
                    <Text style={styles.appRescheduleText}>Reprogrammer</Text>
                  </TouchableOpacity>
                </View>
              )}
            </View>
          );
        })
      )}

      <View style={{ height: 40 }} />
    </ScrollView>
  );
}

/* ------------------ STYLES ------------------ */
const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: LIGHT },

  headerWrapper: {
    backgroundColor: "#0066A2",
    paddingTop: 55,
    paddingBottom: 25,
    paddingHorizontal: 20,
    borderBottomLeftRadius: 30,
    borderBottomRightRadius: 30,
  },

  headerRow: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
  },

  headerWelcome: { color: "#A3CCE8", fontSize: 10 },
  headerBig: { color: WHITE, fontSize: 19, fontWeight: "800" },

  avatar: {
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: WHITE,
  },

  searchBar: {
    marginTop: 16,
    backgroundColor: WHITE,
    borderRadius: 14,
    paddingVertical: 12,
    paddingHorizontal: 14,
    flexDirection: "row",
    alignItems: "center",
    elevation: 2,
  },

  searchInput: {
    marginLeft: 8,
    flex: 1,
    fontSize: 15,
    color: "#000",
  },

  /* BANNER */
  gradientBanner: {
    marginTop: -15,
    marginHorizontal: 16,
    borderRadius: 24,
    padding: 18,
    backgroundColor: "#0066A2",
    flexDirection: "row",
    alignItems: "center",
    overflow: "hidden",
    elevation: 5,
  },

  curve1: {
    position: "absolute",
    width: 180,
    height: 180,
    backgroundColor: "#0072B0",
    borderRadius: 90,
    top: -40,
    right: -60,
    opacity: 0.5,
  },

  curve2: {
    position: "absolute",
    width: 120,
    height: 120,
    backgroundColor: "#0080C0",
    borderRadius: 60,
    bottom: -30,
    right: -20,
    opacity: 0.4,
  },

  bannerDoctor: {
    width: 120,
    height: 120,
  },

  bannerTitle: { fontSize: 18, fontWeight: "700", color: WHITE },
  bannerSub: { color: "#D9F3F2", fontSize: 13, marginVertical: 8 },

  bannerBtn: {
    backgroundColor: WHITE,
    paddingVertical: 8,
    paddingHorizontal: 16,
    borderRadius: 10,
  },
  bannerBtnText: { color: PRIMARY, fontWeight: "700" },

  /* SECTION HEADER */
  sectionHeaderRow: {
    flexDirection: "row",
    justifyContent: "space-between",
    paddingHorizontal: 18,
    marginTop: 28,
    marginBottom: 12,
  },

  sectionTitle: {
    fontSize: 20,
    fontWeight: "700",
    color: PRIMARY,
  },

  viewAll: { color: PRIMARY, fontWeight: "600" },

  /* SERVICE CHIPS */
  serviceChips: {
    flexDirection: "row",
    paddingHorizontal: 18,
    marginBottom: 14,
  },

  chip: {
    backgroundColor: "#F2F5F6",
    paddingVertical: 6,
    paddingHorizontal: 14,
    borderRadius: 20,
    fontSize: 14,
    color: PRIMARY,
    fontWeight: "500",
    marginRight: 12,
  },

  /* SERVICE GRID */
  serviceCardGrid: {
    flexDirection: "row",
    flexWrap: "wrap",
    justifyContent: "space-between",
    paddingHorizontal: 16,
  },

  serviceCardNew: {
    width: "48%",
    borderRadius: 18,
    padding: 16,
    marginBottom: 16,
    elevation: 3,
  },

  serviceIconWrap: {
    width: 52,
    height: 52,
    borderRadius: 16,
    justifyContent: "center",
    alignItems: "center",
    marginBottom: 12,
  },

  serviceTitle: { fontSize: 17, fontWeight: "700", color: PRIMARY },
  serviceSubtitle: { fontSize: 13, color: "#6C7A7A", marginTop: 4 },

  serviceLearnBtn: {
    backgroundColor: WHITE,
    paddingVertical: 8,
    paddingHorizontal: 12,
    borderRadius: 10,
    alignSelf: "flex-start",
    marginTop: 12,
    elevation: 1,
  },
  serviceLearnText: { fontSize: 13, fontWeight: "700", color: PRIMARY },

  /* AI CONSULTATION */
  aiContainer: { paddingHorizontal: 16, marginTop: 20 },

  aiGradient: {
    borderRadius: 26,
    padding: 20,
    backgroundColor: "#E8F2FA",
    elevation: 3,
  },

  aiAvatarWrap: {
    alignSelf: "center",
    backgroundColor: "white",
    padding: 10,
    borderRadius: 50,
  },

  aiAvatar: { width: 50, height: 50, borderRadius: 25 },

  aiTitle: {
    fontSize: 18,
    fontWeight: "700",
    color: PRIMARY,
    textAlign: "center",
    marginTop: 10,
  },

  aiSubTitle: {
    fontSize: 13,
    color: "#6A7E7E",
    textAlign: "center",
    marginTop: 4,
    marginBottom: 14,
  },

  aiSmallInput: {
    flexDirection: "row",
    alignItems: "center",
    backgroundColor: WHITE,
    borderRadius: 12,
    paddingHorizontal: 14,
    paddingVertical: 10,
    borderWidth: 1,
    borderColor: "#C5D9E8",
  },

  aiInputText: { flex: 1, fontSize: 15, color: PRIMARY },

  aiSuggestionRow: {
    flexDirection: "row",
    flexWrap: "wrap",
    marginTop: 14,
  },

  aiChip: {
    backgroundColor: WHITE,
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 20,
    borderWidth: 1,
    borderColor: "#C5D9E8",
    color: PRIMARY,
    fontSize: 13,
    fontWeight: "600",
    marginRight: 10,
    marginBottom: 10,
  },

  aiActionRow: {
    flexDirection: "row",
    justifyContent: "center",
    marginTop: 18,
  },

  aiMicBtn: {
    backgroundColor: WHITE,
    padding: 12,
    borderRadius: 14,
    borderWidth: 1,
    borderColor: "#C5D9E8",
    marginRight: 14,
  },

  aiCameraBtn: {
    backgroundColor: PRIMARY,
    padding: 12,
    borderRadius: 14,
  },

  /* TIPS */
  tipCard: {
    width: SCREEN_WIDTH - 60,
    backgroundColor: WHITE,
    padding: 18,
    marginRight: 16,
    borderRadius: 20,
    elevation: 3,
  },

  tipImage: {
    width: "100%",
    height: 130,
    borderRadius: 16,
    marginBottom: 12,
  },

  tipTitle: { fontSize: 18, fontWeight: "700", color: PRIMARY },
  tipText: { fontSize: 14, color: "#6C7A7A", marginTop: 6 },

  tipBtn: {
    flexDirection: "row",
    alignItems: "center",
    marginTop: 14,
    backgroundColor: PRIMARY,
    paddingVertical: 8,
    paddingHorizontal: 14,
    alignSelf: "flex-start",
    borderRadius: 12,
  },

  tipBtnText: { color: WHITE, fontWeight: "700", marginRight: 4 },

  /* TABS */
  tabsContainer: {
    flexDirection: "row",
    backgroundColor: "#E3EEF7",
    marginHorizontal: 16,
    padding: 6,
    borderRadius: 14,
    marginTop: 20,
    marginBottom: 10,
  },

  tabBtn: { flex: 1, paddingVertical: 10, alignItems: "center", borderRadius: 10 },
  tabBtnActive: { backgroundColor: PRIMARY },
  tabText: { color: PRIMARY, fontSize: 15, fontWeight: "600" },
  tabTextActive: { color: WHITE, fontWeight: "700" },

  /* APPOINTMENTS */
  appointmentCard: {
    backgroundColor: WHITE,
    padding: 16,
    borderRadius: 16,
    marginHorizontal: 16,
    marginBottom: 12,
    elevation: 2,
  },

  appTopRow: { flexDirection: "row", justifyContent: "space-between", alignItems: "center" },
  apptInfoLeft: { flexDirection: "row", alignItems: "center", flex: 1 },
  apptAvatarSmall: {
    width: 44, height: 44, borderRadius: 14,
    marginRight: 12,
  },

  appSpeciality: { fontSize: 12, color: "#6C7A7A", marginTop: 2 },
  appDoctor: { fontSize: 16, fontWeight: "700", color: PRIMARY },

  apptStatusPill: {
    paddingHorizontal: 10, paddingVertical: 4,
    borderRadius: 12, marginLeft: 8,
  },
  apptStatusActive: { backgroundColor: "#E8F0FE" },
  apptStatusCompleted: { backgroundColor: "#E8F8F0" },
  apptStatusCancelled: { backgroundColor: "#FDEDED" },
  apptStatusPillText: { fontSize: 11, fontWeight: "700" },

  apptDivider: { height: 1, backgroundColor: "#F0F0F0", marginVertical: 12 },

  appInfoRow: { flexDirection: "row", alignItems: "center", gap: 20 },
  appInfoItem: { flexDirection: "row", alignItems: "center" },
  appInfoText: { fontSize: 14, fontWeight: "500", color: "#6B7B80", marginLeft: 6 },

  appBtnRow: { flexDirection: "row", marginTop: 14, gap: 10 },

  appCancelBtn: {
    flex: 1,
    paddingVertical: 10,
    borderWidth: 1,
    borderColor: "#D0D7D7",
    borderRadius: 12,
    alignItems: "center",
  },

  appCancelText: { color: "#6B7B80", fontSize: 14, fontWeight: "600" },

  appRescheduleBtn: {
    flex: 1,
    backgroundColor: PRIMARY,
    paddingVertical: 10,
    borderRadius: 12,
    alignItems: "center",
  },

  appRescheduleText: { color: WHITE, fontSize: 15, fontWeight: "700" },
});
