import { Ionicons } from "@expo/vector-icons";
import React, { useCallback, useEffect, useMemo, useState } from "react";
import {
  Alert,
  Platform,
  ScrollView,
  StyleSheet,
  Text,
  TouchableOpacity,
  View,
} from "react-native";
import { patientService, AvailableSlot, Appointment } from "@/services/patientService";

const PRIMARY = "#0066A2";
const WHITE = "#FFFFFF";
const LIGHT_BG = "#F0F6FA";
const TEXT_DARK = "#1E293B";
const TEXT_MUTED = "#64748B";

const SPECIALITY_ICONS: Record<string, { icon: string; color: string }> = {
  Cardiologie: { icon: "heart-outline", color: "#FEE2E2" },
  Dermatologie: { icon: "color-palette-outline", color: "#FCE7F3" },
  Pédiatrie: { icon: "happy-outline", color: "#FEF9C3" },
  Neurologie: { icon: "pulse-outline", color: "#EDE9FE" },
  Orthopédie: { icon: "medkit-outline", color: "#DBEAFE" },
  Gynécologie: { icon: "woman-outline", color: "#FCE4EC" },
  Ophtalmologie: { icon: "eye-outline", color: "#E0F2FE" },
  ORL: { icon: "ear-outline", color: "#F3E8FF" },
  "Médecine générale": { icon: "person-outline", color: "#D1FAE5" },
  Psychiatrie: { icon: "chatbubble-ellipses-outline", color: "#FFE4E6" },
};

const DEFAULT_ICON = { icon: "medkit-outline", color: "#F1F5F9" };

const FRENCH_DAYS = ["Dim", "Lun", "Mar", "Mer", "Jeu", "Ven", "Sam"];
const FRENCH_MONTHS = [
  "Jan", "Fév", "Mar", "Avr", "Mai", "Juin",
  "Juil", "Aoû", "Sep", "Oct", "Nov", "Déc",
];

function getNext7Days() {
  const days: { key: string; day: string; date: string; fullLabel: string }[] = [];
  for (let i = 0; i < 7; i++) {
    const d = new Date();
    d.setDate(d.getDate() + i);
    days.push({
      key: d.toISOString().split("T")[0],
      day: FRENCH_DAYS[d.getDay()],
      date: String(d.getDate()),
      fullLabel: `${FRENCH_DAYS[d.getDay()]} ${d.getDate()} ${FRENCH_MONTHS[d.getMonth()]}`,
    });
  }
  return days;
}

function extractTime(raw: string | undefined | null): string {
  if (!raw) return "";
  const isoMatch = raw.match(/T(\d{2}:\d{2})/);
  if (isoMatch) return isoMatch[1];
  const hhmmMatch = raw.match(/^(\d{1,2}:\d{2})/);
  if (hhmmMatch) return hhmmMatch[1];
  return raw;
}

function formatTimeDisplay(time: string): string {
  const t = extractTime(time);
  const match = t.match(/^(\d{1,2}):(\d{2})/);
  if (!match) return time;
  const h = parseInt(match[1], 10);
  const m = match[2];
  return `${h.toString().padStart(2, "0")}h${m}`;
}

function getSection(time: string): string {
  const t = extractTime(time);
  const match = t.match(/^(\d{1,2})/);
  if (!match) return "Autre";
  const h = parseInt(match[1], 10);
  if (h < 12) return "Matin";
  if (h < 17) return "Après-midi";
  return "Soirée";
}

function getStatusLabel(status: string): string {
  switch (status.toUpperCase()) {
    case "SCHEDULED": return "Confirmé";
    case "CONFIRMED": return "Confirmé";
    case "COMPLETED": return "Terminé";
    case "CANCELLED": return "Annulé";
    default: return status;
  }
}

function getStatusColor(status: string): string {
  switch (status.toUpperCase()) {
    case "SCHEDULED": return "#10B981";
    case "CONFIRMED": return "#10B981";
    case "COMPLETED": return "#64748B";
    case "CANCELLED": return "#EF4444";
    default: return "#64748B";
  }
}

export default function AppointmentsScreen() {
  const days = useMemo(() => getNext7Days(), []);

  const [allDoctors, setAllDoctors] = useState<any[]>([]);
  const [selectedSpeciality, setSelectedSpeciality] = useState("");
  const [selectedDoctorId, setSelectedDoctorId] = useState<number | null>(null);
  const [selectedDayKey, setSelectedDayKey] = useState<string | null>(days[0]?.key ?? null);
  const [selectedSlot, setSelectedSlot] = useState<string | null>(null);
  const [slots, setSlots] = useState<AvailableSlot[]>([]);
  const [loadingDoctors, setLoadingDoctors] = useState(true);
  const [loadingSlots, setLoadingSlots] = useState(false);
  const [booking, setBooking] = useState(false);
  const [myAppointments, setMyAppointments] = useState<Appointment[]>([]);
  const [loadingMine, setLoadingMine] = useState(true);
  const [showMyAppts, setShowMyAppts] = useState(false);

  useEffect(() => {
    (async () => {
      try {
        const data = await patientService.getDoctors();
        const docs = Array.isArray(data) ? data : [];
        setAllDoctors(docs);
        if (docs.length > 0) {
          const spec = docs[0].specialty || "";
          setSelectedSpeciality(spec);
          setSelectedDoctorId(docs[0].id);
        }
      } catch (e) {
        console.warn("getDoctors failed", e);
      } finally {
        setLoadingDoctors(false);
      }
    })();
    (async () => {
      try {
        const data = await patientService.getMyAppointments();
        setMyAppointments(Array.isArray(data) ? data : []);
      } catch (e) {
        console.warn("getMyAppointments failed", e);
      } finally {
        setLoadingMine(false);
      }
    })();
  }, []);

  const specialities = useMemo(() => {
    const set = new Set<string>();
    allDoctors.forEach((d: any) => {
      const s = d.specialty || "";
      if (s) set.add(s);
    });
    return Array.from(set);
  }, [allDoctors]);

  const filteredDoctors = useMemo(
    () => allDoctors.filter((d: any) => (d.specialty || "") === selectedSpeciality),
    [allDoctors, selectedSpeciality],
  );

  const selectedDoctor = filteredDoctors.find((d: any) => d.id === selectedDoctorId);

  useEffect(() => {
    if (!filteredDoctors.find((d: any) => d.id === selectedDoctorId) && filteredDoctors[0]) {
      setSelectedDoctorId(filteredDoctors[0].id);
      setSelectedSlot(null);
    }
  }, [filteredDoctors, selectedDoctorId]);

  const fetchSlots = useCallback(async () => {
    if (!selectedDoctorId || !selectedDayKey) {
      setSlots([]);
      return;
    }
    setLoadingSlots(true);
    try {
      console.log("fetchSlots", { doctorId: selectedDoctorId, date: selectedDayKey });
      const data = await patientService.getAvailableSlots(selectedDoctorId, selectedDayKey);
      console.log("fetchSlots response", data);
      setSlots(Array.isArray(data) ? data : []);
    } catch (e) {
      console.warn("fetchSlots failed", e);
      setSlots([]);
    } finally {
      setLoadingSlots(false);
    }
  }, [selectedDoctorId, selectedDayKey]);

  useEffect(() => {
    fetchSlots();
  }, [fetchSlots]);

  const availableSlots = slots
    .filter((s) => s.available && s.time != null)
    .map((s) => s.time);

  const timeSections = useMemo(() => {
    const sections: Record<string, string[]> = {};
    availableSlots.forEach((t) => {
      const sec = getSection(t);
      if (!sections[sec]) sections[sec] = [];
      sections[sec].push(t);
    });
    return Object.entries(sections).map(([label, slotList]) => ({
      label,
      slots: slotList.sort(),
    }));
  }, [availableSlots]);

  const selectedDayLabel = days.find((d) => d.key === selectedDayKey)?.fullLabel || "Choisir une date";
  const canBook = selectedDoctor && selectedDayKey && selectedSlot;

  const handleBook = async () => {
    if (!canBook || !selectedDoctorId || !selectedDayKey || !selectedSlot) return;
    setBooking(true);
    try {
      await patientService.bookAppointment({
        doctorId: selectedDoctorId,
        dateTime: `${selectedDayKey}T${selectedSlot}:00`,
        mode: "PRESENTIEL",
      });
      Alert.alert("Succès", "Rendez-vous réservé avec succès !");
      setSelectedSlot(null);
      setSelectedDayKey(null);
      const data = await patientService.getMyAppointments();
      setMyAppointments(Array.isArray(data) ? data : []);
    } catch {
      Alert.alert("Erreur", "Échec de la réservation. Veuillez réessayer.");
    } finally {
      setBooking(false);
    }
  };

  const nameInitials = (firstName?: string, lastName?: string) => {
    const a = (firstName?.[0] || "").toUpperCase();
    const b = (lastName?.[0] || "").toUpperCase();
    return a + b || "?";
  };

  return (
    <View style={styles.screen}>
      <View style={styles.headerWrapper}>
        <View style={styles.headerDecor} />
        <Text style={styles.headerTitle}>Rendez-vous</Text>
        <Text style={styles.headerSubtitle}>
          Réservez une consultation avec un médecin
        </Text>
      </View>

      <View style={styles.toggleRow}>
        <TouchableOpacity
          style={[styles.toggleBtn, !showMyAppts && styles.toggleActive]}
          onPress={() => setShowMyAppts(false)}
        >
          <Text style={[styles.toggleText, !showMyAppts && styles.toggleTextActive]}>
            Nouveau
          </Text>
        </TouchableOpacity>
        <TouchableOpacity
          style={[styles.toggleBtn, showMyAppts && styles.toggleActive]}
          onPress={() => setShowMyAppts(true)}
        >
          <Text style={[styles.toggleText, showMyAppts && styles.toggleTextActive]}>
            Mes rendez-vous
          </Text>
        </TouchableOpacity>
      </View>

      {showMyAppts ? (
        <ScrollView
          style={styles.scroll}
          contentContainerStyle={{ paddingBottom: 120 }}
          showsVerticalScrollIndicator={false}
        >
          {loadingMine ? (
            <Text style={styles.emptyText}>Chargement...</Text>
          ) : myAppointments.length === 0 ? (
            <Text style={styles.emptyText}>Aucun rendez-vous pour le moment</Text>
          ) : (
            myAppointments.map((appt) => {
              const doc = allDoctors.find((d: any) => d.id === appt.doctorId);
              const d = new Date(appt.dateTime);
              const dateStr = `${FRENCH_DAYS[d.getDay()]} ${d.getDate()} ${FRENCH_MONTHS[d.getMonth()]} ${d.getFullYear()}`;
              const timeStr = `${d.getHours().toString().padStart(2, "0")}h${d.getMinutes().toString().padStart(2, "0")}`;
              const status = getStatusLabel(appt.status);
              const statusColor = getStatusColor(appt.status);
              return (
                <View key={appt.id} style={styles.apptCard}>
                  <View style={styles.apptLeft}>
                    <View style={styles.apptDateBox}>
                      <Text style={styles.apptDateDay}>{String(d.getDate())}</Text>
                      <Text style={styles.apptDateMonth}>{FRENCH_MONTHS[d.getMonth()]}</Text>
                    </View>
                  </View>
                  <View style={styles.apptCenter}>
                    <Text style={styles.apptDocName}>
                      Dr. {doc?.firstName || ""} {doc?.lastName || ""}
                    </Text>
                    <Text style={styles.apptSpecialty}>{doc?.specialty || ""}</Text>
                    <Text style={styles.apptTime}>{timeStr}</Text>
                  </View>
                  <View style={styles.apptRight}>
                    <View style={[styles.statusBadge, { backgroundColor: statusColor + "18" }]}>
                      <Text style={[styles.statusText, { color: statusColor }]}>{status}</Text>
                    </View>
                  </View>
                </View>
              );
            })
          )}
        </ScrollView>
      ) : (
        <>
          <ScrollView
            style={styles.scroll}
            contentContainerStyle={{ paddingBottom: 110 }}
            showsVerticalScrollIndicator={false}
          >
            {/* SPÉCIALITÉS */}
            <View style={styles.section}>
              <Text style={styles.sectionTitle}>Spécialités</Text>
              <ScrollView
                horizontal
                showsHorizontalScrollIndicator={false}
                contentContainerStyle={{ paddingVertical: 4 }}
              >
                {specialities.map((spec) => {
                  const meta = SPECIALITY_ICONS[spec] || DEFAULT_ICON;
                  const active = spec === selectedSpeciality;
                  return (
                    <TouchableOpacity
                      key={spec}
                      style={[
                        styles.specChip,
                        { backgroundColor: meta.color },
                        active && styles.specChipActive,
                      ]}
                      onPress={() => {
                        setSelectedSpeciality(spec);
                        setSelectedSlot(null);
                      }}
                    >
                      <View style={styles.specIconCircle}>
                        <Ionicons name={meta.icon as any} size={18} color={active ? PRIMARY : "#4B5563"} />
                      </View>
                      <Text style={[styles.specText, active && { color: PRIMARY }]}>
                        {spec}
                      </Text>
                    </TouchableOpacity>
                  );
                })}
              </ScrollView>
            </View>

            {/* MÉDECINS */}
            <View style={styles.section}>
              <Text style={styles.sectionTitle}>Médecins</Text>
              {loadingDoctors ? (
                <Text style={styles.emptyText}>Chargement...</Text>
              ) : (
                filteredDoctors.map((doc: any) => {
                  const active = doc.id === selectedDoctorId;
                  return (
                    <TouchableOpacity
                      key={doc.id}
                      style={[styles.docCard, active && styles.docCardActive]}
                      onPress={() => {
                        setSelectedDoctorId(doc.id);
                        setSelectedSlot(null);
                      }}
                    >
                      <View style={styles.docAvatar}>
                        <Text style={styles.docAvatarText}>
                          {nameInitials(doc.firstName, doc.lastName)}
                        </Text>
                      </View>
                      <View style={styles.docInfo}>
                        <Text style={styles.docName}>
                          Dr. {doc.firstName || ""} {doc.lastName || ""}
                        </Text>
                        <Text style={styles.docSpecialty}>{doc.specialty || ""}</Text>
                        {doc.hospital ? (
                          <View style={styles.docMetaRow}>
                            <Ionicons name="location-outline" size={13} color={TEXT_MUTED} />
                            <Text style={styles.docMetaText}>{doc.hospital}</Text>
                          </View>
                        ) : null}
                      </View>
                      <Ionicons name="chevron-forward" size={18} color={TEXT_MUTED} />
                    </TouchableOpacity>
                  );
                })
              )}
            </View>

            {/* DATE */}
            <View style={styles.section}>
              <Text style={styles.sectionTitle}>Date</Text>
              <ScrollView
                horizontal
                showsHorizontalScrollIndicator={false}
                contentContainerStyle={{ paddingVertical: 4 }}
              >
                {days.map((d) => {
                  const active = d.key === selectedDayKey;
                  return (
                    <TouchableOpacity
                      key={d.key}
                      style={[styles.dayCard, active && styles.dayCardActive]}
                      onPress={() => {
                        setSelectedDayKey(d.key);
                        setSelectedSlot(null);
                      }}
                    >
                      <Text style={[styles.dayName, active && { color: PRIMARY }]}>
                        {d.day}
                      </Text>
                      <Text style={[styles.dayDate, active && { color: PRIMARY }]}>
                        {d.date}
                      </Text>
                    </TouchableOpacity>
                  );
                })}
              </ScrollView>
            </View>

            {/* CRÉNEAUX */}
            <View style={styles.section}>
              <Text style={styles.sectionTitle}>Horaire</Text>
              {!loadingSlots && slots.length > 0 && (
                <Text style={{ fontSize: 11, color: TEXT_MUTED, marginBottom: 6 }}>
                  Debug: {slots.length} reçus, {availableSlots.length} dispo
                </Text>
              )}
              {!selectedDayKey ? (
                <Text style={styles.emptyText}>Choisissez une date</Text>
              ) : loadingSlots ? (
                <Text style={styles.emptyText}>Chargement...</Text>
              ) : timeSections.length === 0 ? (
                <Text style={styles.emptyText}>
                  {slots.length === 0
                    ? "Aucun créneau reçu de l'API"
                    : "Aucun créneau disponible"}
                </Text>
              ) : (
                timeSections.map((sec) => (
                  <View key={sec.label} style={{ marginBottom: 14 }}>
                    <Text style={styles.timeSectionLabel}>{sec.label}</Text>
                    <View style={styles.slotRow}>
                      {sec.slots.map((slot) => {
                        const active = slot === selectedSlot;
                        return (
                          <TouchableOpacity
                            key={slot}
                            style={[styles.slotChip, active && styles.slotChipActive]}
                            onPress={() => setSelectedSlot(slot)}
                          >
                            <Text style={[styles.slotText, active && { color: PRIMARY }]}>
                              {formatTimeDisplay(slot)}
                            </Text>
                          </TouchableOpacity>
                        );
                      })}
                    </View>
                  </View>
                ))
              )}
            </View>
          </ScrollView>

          {/* BOTTOM BAR */}
          <View style={styles.bottomBar}>
            <View style={{ flex: 1 }}>
              <Text style={styles.bottomTitle}>
                {selectedDoctor
                  ? `Dr. ${selectedDoctor.firstName || ""} ${selectedDoctor.lastName || ""}`
                  : "Sélectionnez un médecin"}
              </Text>
              <Text style={styles.bottomSub}>
                {selectedDayLabel}{selectedSlot ? ` · ${formatTimeDisplay(selectedSlot)}` : ""}
              </Text>
            </View>
            <TouchableOpacity
              style={[styles.bookBtn, (!canBook || booking) && { opacity: 0.5 }]}
              disabled={!canBook || booking}
              onPress={handleBook}
            >
              <Text style={styles.bookBtnText}>{booking ? "Réservation..." : "Réserver"}</Text>
            </TouchableOpacity>
          </View>
        </>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  screen: {
    flex: 1,
    backgroundColor: LIGHT_BG,
  },

  /* HEADER */
  headerWrapper: {
    paddingTop: Platform.OS === "android" ? 55 : 60,
    paddingBottom: 22,
    paddingHorizontal: 24,
    backgroundColor: PRIMARY,
    borderBottomLeftRadius: 28,
    borderBottomRightRadius: 28,
    overflow: "hidden",
  },
  headerDecor: {
    position: "absolute",
    top: -30,
    right: -30,
    width: 160,
    height: 160,
    borderRadius: 80,
    backgroundColor: "rgba(255,255,255,0.07)",
  },
  headerTitle: {
    fontSize: 26,
    fontWeight: "700",
    color: WHITE,
  },
  headerSubtitle: {
    marginTop: 6,
    color: "rgba(255,255,255,0.8)",
    fontSize: 14,
  },

  /* TOGGLE */
  toggleRow: {
    flexDirection: "row",
    marginHorizontal: 20,
    marginTop: 16,
    marginBottom: 4,
    backgroundColor: WHITE,
    borderRadius: 14,
    padding: 4,
    shadowColor: "#000",
    shadowOpacity: 0.04,
    shadowRadius: 6,
    elevation: 2,
  },
  toggleBtn: {
    flex: 1,
    paddingVertical: 10,
    borderRadius: 12,
    alignItems: "center",
  },
  toggleActive: {
    backgroundColor: PRIMARY,
  },
  toggleText: {
    fontSize: 14,
    fontWeight: "600",
    color: TEXT_MUTED,
  },
  toggleTextActive: {
    color: WHITE,
  },

  /* SCROLL */
  scroll: {
    flex: 1,
    paddingHorizontal: 20,
    marginTop: 16,
  },
  section: {
    marginBottom: 24,
  },
  sectionTitle: {
    fontSize: 17,
    fontWeight: "700",
    color: TEXT_DARK,
    marginBottom: 12,
  },
  emptyText: {
    color: TEXT_MUTED,
    fontSize: 14,
    textAlign: "center",
    marginTop: 8,
  },

  /* SPÉCIALITÉS */
  specChip: {
    flexDirection: "row",
    alignItems: "center",
    paddingVertical: 10,
    paddingHorizontal: 14,
    borderRadius: 20,
    marginRight: 10,
  },
  specChipActive: {
    borderWidth: 2,
    borderColor: PRIMARY,
  },
  specIconCircle: {
    width: 30,
    height: 30,
    borderRadius: 15,
    backgroundColor: "rgba(255,255,255,0.8)",
    justifyContent: "center",
    alignItems: "center",
    marginRight: 8,
  },
  specText: {
    fontSize: 13,
    color: "#4B5563",
    fontWeight: "600",
  },

  /* MÉDECINS */
  docCard: {
    backgroundColor: WHITE,
    borderRadius: 18,
    padding: 16,
    marginBottom: 12,
    flexDirection: "row",
    alignItems: "center",
    shadowColor: "#000",
    shadowOpacity: 0.04,
    shadowRadius: 8,
    elevation: 2,
  },
  docCardActive: {
    borderWidth: 2,
    borderColor: PRIMARY,
    backgroundColor: "#F0F6FA",
  },
  docAvatar: {
    width: 50,
    height: 50,
    borderRadius: 16,
    backgroundColor: PRIMARY,
    justifyContent: "center",
    alignItems: "center",
    marginRight: 14,
  },
  docAvatarText: {
    fontSize: 18,
    fontWeight: "700",
    color: WHITE,
  },
  docInfo: {
    flex: 1,
  },
  docName: {
    fontSize: 16,
    fontWeight: "700",
    color: TEXT_DARK,
  },
  docSpecialty: {
    fontSize: 13,
    color: TEXT_MUTED,
    marginTop: 2,
  },
  docMetaRow: {
    flexDirection: "row",
    alignItems: "center",
    marginTop: 4,
  },
  docMetaText: {
    marginLeft: 4,
    fontSize: 12,
    color: TEXT_MUTED,
  },

  /* DATE */
  dayCard: {
    width: 60,
    height: 76,
    borderRadius: 16,
    backgroundColor: WHITE,
    justifyContent: "center",
    alignItems: "center",
    marginRight: 10,
    shadowColor: "#000",
    shadowOpacity: 0.04,
    shadowRadius: 6,
    elevation: 2,
  },
  dayCardActive: {
    borderWidth: 2,
    borderColor: PRIMARY,
    backgroundColor: "#F0F6FA",
  },
  dayName: {
    fontSize: 12,
    color: TEXT_MUTED,
  },
  dayDate: {
    marginTop: 4,
    fontSize: 20,
    fontWeight: "700",
    color: TEXT_DARK,
  },

  /* CRÉNEAUX */
  timeSectionLabel: {
    fontSize: 13,
    color: TEXT_MUTED,
    fontWeight: "600",
    marginBottom: 8,
  },
  slotRow: {
    flexDirection: "row",
    flexWrap: "wrap",
  },
  slotChip: {
    paddingVertical: 8,
    paddingHorizontal: 16,
    borderRadius: 14,
    backgroundColor: WHITE,
    marginRight: 10,
    marginBottom: 10,
    borderWidth: 1,
    borderColor: "#E2E8F0",
  },
  slotChipActive: {
    borderColor: PRIMARY,
    backgroundColor: "#F0F6FA",
  },
  slotText: {
    fontSize: 14,
    color: TEXT_DARK,
    fontWeight: "600",
  },

  /* BOTTOM BAR */
  bottomBar: {
    position: "absolute",
    left: 0,
    right: 0,
    bottom: 0,
    paddingHorizontal: 20,
    paddingVertical: 14,
    paddingBottom: Platform.OS === "ios" ? 34 : 20,
    backgroundColor: WHITE,
    flexDirection: "row",
    alignItems: "center",
    borderTopLeftRadius: 20,
    borderTopRightRadius: 20,
    shadowColor: "#000",
    shadowOpacity: 0.1,
    shadowRadius: 16,
    elevation: 10,
  },
  bottomTitle: {
    fontSize: 15,
    fontWeight: "700",
    color: TEXT_DARK,
  },
  bottomSub: {
    marginTop: 2,
    fontSize: 12,
    color: TEXT_MUTED,
  },
  bookBtn: {
    marginLeft: 16,
    paddingHorizontal: 24,
    paddingVertical: 12,
    borderRadius: 50,
    backgroundColor: PRIMARY,
  },
  bookBtnText: {
    color: WHITE,
    fontWeight: "700",
    fontSize: 14,
  },

  /* MES RENDEZ-VOUS */
  apptCard: {
    backgroundColor: WHITE,
    borderRadius: 18,
    padding: 16,
    marginBottom: 12,
    flexDirection: "row",
    alignItems: "center",
    shadowColor: "#000",
    shadowOpacity: 0.04,
    shadowRadius: 8,
    elevation: 2,
  },
  apptLeft: {
    marginRight: 14,
  },
  apptDateBox: {
    width: 54,
    height: 60,
    borderRadius: 14,
    backgroundColor: LIGHT_BG,
    justifyContent: "center",
    alignItems: "center",
  },
  apptDateDay: {
    fontSize: 20,
    fontWeight: "700",
    color: PRIMARY,
  },
  apptDateMonth: {
    fontSize: 11,
    color: TEXT_MUTED,
    marginTop: -2,
  },
  apptCenter: {
    flex: 1,
  },
  apptDocName: {
    fontSize: 15,
    fontWeight: "700",
    color: TEXT_DARK,
  },
  apptSpecialty: {
    fontSize: 12,
    color: TEXT_MUTED,
    marginTop: 2,
  },
  apptTime: {
    fontSize: 12,
    color: PRIMARY,
    fontWeight: "600",
    marginTop: 2,
  },
  apptRight: {},
  statusBadge: {
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: 12,
  },
  statusText: {
    fontSize: 12,
    fontWeight: "700",
  },
});
