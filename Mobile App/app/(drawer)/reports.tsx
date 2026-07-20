import { Ionicons } from "@expo/vector-icons";
import { useAuth } from "@/context/AuthContext";
import React, { useCallback, useEffect, useState } from "react";
import {
  RefreshControl,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from "react-native";
import {
  patientService,
  MedicalRecord,
  PrescriptionResponse,
} from "@/services/patientService";

const PRIMARY = "#0066A2";
const LIGHT_BG = "#F0F6FA";
const WHITE = "#FFFFFF";
const TEXT_DARK = "#1E293B";
const TEXT_MUTED = "#64748B";
const FRENCH_MONTHS = ["Jan", "Fév", "Mar", "Avr", "Mai", "Juin", "Juil", "Aoû", "Sep", "Oct", "Nov", "Déc"];

export default function ReportScreen() {
  const { user } = useAuth();
  const [record, setRecord] = useState<MedicalRecord | null>(null);
  const [prescriptions, setPrescriptions] = useState<PrescriptionResponse[]>([]);
  const [refreshing, setRefreshing] = useState(false);

  const loadData = useCallback(async () => {
    try {
      const [mr, rxns] = await Promise.all([
        patientService.getMyMedicalRecord(),
        user?.id ? patientService.getPatientPrescriptions(user.id) : Promise.resolve([]),
      ]);
      setRecord(mr);
      setPrescriptions(Array.isArray(rxns) ? rxns : []);
    } catch {}
  }, [user]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const onRefresh = useCallback(async () => {
    setRefreshing(true);
    await loadData();
    setRefreshing(false);
  }, [loadData]);

  const initials = user
    ? `${(user.firstName?.[0] || "").toUpperCase()}${(user.lastName?.[0] || "").toUpperCase()}`
    : "?";

  const fullName = user
    ? `${user.firstName || ""} ${user.lastName || ""}`.trim()
    : "Patient";

  const getRxStatus = (s: string) => {
    switch (s.toUpperCase()) {
      case "SOUMISE": return "Soumise";
      case "EN_PREPARATION": return "En préparation";
      case "PREPAREE": return "Prête";
      case "RETIREE": return "Retirée";
      case "DISPENSEE": return "Dispensée";
      case "ANNULEE": return "Annulée";
      case "ARCHIVEE": return "Archivée";
      default: return s.charAt(0) + s.slice(1).toLowerCase();
    }
  };

  return (
    <ScrollView
      style={styles.container}
      showsVerticalScrollIndicator={false}
      refreshControl={
        <RefreshControl refreshing={refreshing} onRefresh={onRefresh} tintColor={PRIMARY} />
      }
    >
      {/* ================= HEADER ================= */}
      <View style={styles.header}>
        <View style={styles.headerDecor} />
        <View style={styles.headerRow}>
          <View style={styles.avatar}>
            <Text style={styles.avatarText}>{initials}</Text>
          </View>
          <View style={styles.headerInfo}>
            <Text style={styles.headerName}>{fullName}</Text>
            <Text style={styles.headerSub}>Dossier médical</Text>
          </View>
        </View>
      </View>



      {/* ================= MÉDECINE ================= */}
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Données médicales</Text>
        <View style={styles.medicalGrid}>
          <MedicalBox
            icon="resize-outline"
            label="Taille"
            value={record?.height ? `${record.height} cm` : "—"}
            color="#2563EB"
          />
          <MedicalBox
            icon="scale-outline"
            label="Poids"
            value={record?.weight ? `${record.weight} kg` : "—"}
            color="#F59E0B"
          />
          <MedicalBox
            icon="water-outline"
            label="Groupe sanguin"
            value={record?.bloodGroup || "—"}
            color="#E11D48"
          />
          <MedicalBox
            icon="fitness-outline"
            label="IMC"
            value={
              record?.height && record?.weight
                ? (record.weight / ((record.height / 100) * (record.height / 100))).toFixed(1)
                : "—"
            }
            color="#8B5CF6"
          />
        </View>
      </View>

      {/* ================= ASSURANCE ================= */}
      {record?.insuranceCompany || record?.insuranceNumber ? (
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Assurance</Text>
          <View style={styles.card}>
            {record.insuranceCompany ? (
              <InfoRow icon="shield-checkmark-outline" label="Compagnie" value={record.insuranceCompany} />
            ) : null}
            {record.insuranceNumber ? (
              <InfoRow icon="card-outline" label="N° d'adhérent" value={record.insuranceNumber} last />
            ) : null}
          </View>
        </View>
      ) : null}

      {/* ================= ANTÉCÉDENTS ================= */}
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Antécédents médicaux</Text>
        <View style={styles.card}>
          <Text style={styles.anteTitle}>Allergies</Text>
          <Text style={styles.anteValue}>{record?.allergies || "Aucune"}</Text>
          <View style={styles.anteSep} />
          <Text style={styles.anteTitle}>Maladies chroniques</Text>
          <Text style={styles.anteValue}>{record?.chronicDiseases || "Aucune"}</Text>
          <View style={styles.anteSep} />
          <Text style={styles.anteTitle}>Traitements en cours</Text>
          <Text style={styles.anteValue}>{record?.currentTreatments || "Aucun"}</Text>
        </View>
      </View>

      {/* ================= CONTACT URGENCE ================= */}
      {record?.emergencyContactName || record?.emergencyContactPhone ? (
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Contact d{"'"}urgence</Text>
          <View style={styles.emergencyCard}>
            <View style={styles.emergencyIcon}>
              <Ionicons name="alert-circle" size={28} color="#EF4444" />
            </View>
            <View style={styles.emergencyInfo}>
              <Text style={styles.emergencyName}>{record?.emergencyContactName || "—"}</Text>
              <Text style={styles.emergencyPhone}>{record?.emergencyContactPhone || "—"}</Text>
            </View>
          </View>
        </View>
      ) : null}

      {/* ================= DERNIÈRE ORDONNANCE ================= */}
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Dernière ordonnance</Text>
        {prescriptions.length === 0 ? (
          <Text style={styles.emptyText}>Aucune ordonnance</Text>
        ) : (
          (() => {
            const last = prescriptions.reduce((a, b) =>
              new Date(a.createdAt) > new Date(b.createdAt) ? a : b
            );
            const created = new Date(last.createdAt);
            const createdStr = `${created.getDate()} ${FRENCH_MONTHS[created.getMonth()]} ${created.getFullYear()}`;
            return (
              <View style={styles.rxCard}>
                <View style={styles.rxHeader}>
                  <View style={styles.rxHeaderLeft}>
                    <View style={styles.rxIcon}>
                      <Ionicons name="document-text-outline" size={22} color={PRIMARY} />
                    </View>
                    <View>
                      <Text style={styles.rxDate}>{createdStr}</Text>
                      <Text style={styles.rxStatus}>{getRxStatus(last.status)}</Text>
                    </View>
                  </View>
                  <Text style={styles.rxCount}>{last.items.length} médicament{(last.items.length > 1 ? "s" : "")}</Text>
                </View>
                {last.items.map((item) => (
                  <View key={item.id} style={styles.rxItem}>
                    <View style={styles.rxItemDot} />
                    <View style={styles.rxItemContent}>
                      <Text style={styles.rxMedName}>{item.medicamentName}</Text>
                      <Text style={styles.rxMedDetail}>
                        {[item.dosage, item.forme, item.voieAdministration].filter(Boolean).join(" — ")}
                      </Text>
                      {item.posologie ? (
                        <Text style={styles.rxPosologie}>{item.posologie}</Text>
                      ) : null}
                      <View style={styles.rxItemFooter}>
                        {item.dureeTraitement ? (
                          <Text style={styles.rxDuree}>{item.dureeTraitement} jours</Text>
                        ) : null}
                        {item.quantitePrescrite ? (
                          <Text style={styles.rxQte}>Qté: {item.quantitePrescrite}</Text>
                        ) : null}
                      </View>
                    </View>
                  </View>
                ))}
                {last.notes ? (
                  <Text style={styles.rxNotes}>{last.notes}</Text>
                ) : null}
              </View>
            );
          })()
        )}
      </View>

      <View style={{ height: 100 }} />
    </ScrollView>
  );
}

function InfoRow({ icon, label, value, last }: { icon: string; label: string; value: string; last?: boolean }) {
  return (
    <View style={[styles.infoRow, last && styles.infoRowLast]}>
      <View style={styles.infoIcon}>
        <Ionicons name={icon as any} size={18} color={PRIMARY} />
      </View>
      <View style={styles.infoContent}>
        <Text style={styles.infoLabel}>{label}</Text>
        <Text style={styles.infoValue}>{value}</Text>
      </View>
    </View>
  );
}

function MedicalBox({ icon, label, value, color }: { icon: string; label: string; value: string; color: string }) {
  return (
    <View style={styles.medicalBox}>
      <View style={[styles.medicalIcon, { backgroundColor: color + "18" }]}>
        <Ionicons name={icon as any} size={24} color={color} />
      </View>
      <Text style={styles.medicalLabel}>{label}</Text>
      <Text style={[styles.medicalValue, { color }]}>{value}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: LIGHT_BG,
  },

  /* HEADER */
  header: {
    paddingTop: 60,
    paddingBottom: 28,
    paddingHorizontal: 24,
    backgroundColor: PRIMARY,
    borderBottomLeftRadius: 28,
    borderBottomRightRadius: 28,
    overflow: "hidden",
  },
  headerDecor: {
    position: "absolute",
    top: -40,
    right: -40,
    width: 180,
    height: 180,
    borderRadius: 90,
    backgroundColor: "rgba(255,255,255,0.06)",
  },
  headerRow: {
    flexDirection: "row",
    alignItems: "center",
  },
  avatar: {
    width: 56,
    height: 56,
    borderRadius: 20,
    backgroundColor: "rgba(255,255,255,0.2)",
    justifyContent: "center",
    alignItems: "center",
    marginRight: 16,
  },
  avatarText: {
    fontSize: 22,
    fontWeight: "700",
    color: WHITE,
  },
  headerInfo: {
    flex: 1,
  },
  headerName: {
    fontSize: 22,
    fontWeight: "700",
    color: WHITE,
  },
  headerSub: {
    fontSize: 14,
    color: "rgba(255,255,255,0.8)",
    marginTop: 2,
  },

  /* SECTIONS */
  section: {
    marginTop: 24,
    paddingHorizontal: 20,
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

  /* CARD */
  card: {
    backgroundColor: WHITE,
    borderRadius: 18,
    paddingHorizontal: 16,
    paddingVertical: 4,
    shadowColor: "#000",
    shadowOpacity: 0.04,
    shadowRadius: 8,
    shadowOffset: { width: 0, height: 2 },
    elevation: 2,
  },

  /* INFO ROWS */
  infoRow: {
    flexDirection: "row",
    alignItems: "center",
    paddingVertical: 14,
    borderBottomWidth: 1,
    borderBottomColor: "#F1F5F9",
  },
  infoRowLast: {
    borderBottomWidth: 0,
  },
  infoIcon: {
    width: 36,
    height: 36,
    borderRadius: 12,
    backgroundColor: LIGHT_BG,
    justifyContent: "center",
    alignItems: "center",
    marginRight: 14,
  },
  infoContent: {
    flex: 1,
  },
  infoLabel: {
    fontSize: 12,
    color: TEXT_MUTED,
  },
  infoValue: {
    fontSize: 15,
    fontWeight: "600",
    color: TEXT_DARK,
    marginTop: 1,
  },

  /* MEDICAL GRID */
  medicalGrid: {
    flexDirection: "row",
    flexWrap: "wrap",
    gap: 12,
  },
  medicalBox: {
    width: "47%",
    backgroundColor: WHITE,
    borderRadius: 18,
    padding: 16,
    alignItems: "center",
    shadowColor: "#000",
    shadowOpacity: 0.04,
    shadowRadius: 8,
    shadowOffset: { width: 0, height: 2 },
    elevation: 2,
  },
  medicalIcon: {
    width: 48,
    height: 48,
    borderRadius: 16,
    justifyContent: "center",
    alignItems: "center",
    marginBottom: 10,
  },
  medicalLabel: {
    fontSize: 12,
    color: TEXT_MUTED,
    marginBottom: 4,
  },
  medicalValue: {
    fontSize: 18,
    fontWeight: "700",
  },

  /* ANTÉCÉDENTS */
  anteTitle: {
    fontSize: 13,
    fontWeight: "600",
    color: PRIMARY,
    marginBottom: 4,
    marginTop: 12,
  },
  anteValue: {
    fontSize: 15,
    color: TEXT_DARK,
    lineHeight: 20,
  },
  anteSep: {
    height: 1,
    backgroundColor: "#F1F5F9",
    marginVertical: 8,
  },

  /* URGENCE */
  emergencyCard: {
    backgroundColor: "#FEF2F2",
    borderRadius: 18,
    padding: 18,
    flexDirection: "row",
    alignItems: "center",
    borderWidth: 1,
    borderColor: "#FEE2E2",
  },
  emergencyIcon: {
    marginRight: 14,
  },
  emergencyInfo: {
    flex: 1,
  },
  emergencyName: {
    fontSize: 16,
    fontWeight: "700",
    color: TEXT_DARK,
  },
  emergencyPhone: {
    fontSize: 14,
    color: TEXT_MUTED,
    marginTop: 2,
  },

  /* PRESCRIPTION CARD */
  rxCard: {
    backgroundColor: WHITE,
    borderRadius: 18,
    padding: 18,
    shadowColor: "#000",
    shadowOpacity: 0.04,
    shadowRadius: 8,
    shadowOffset: { width: 0, height: 2 },
    elevation: 2,
  },
  rxHeader: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    marginBottom: 14,
    paddingBottom: 14,
    borderBottomWidth: 1,
    borderBottomColor: "#F1F5F9",
  },
  rxHeaderLeft: {
    flexDirection: "row",
    alignItems: "center",
  },
  rxIcon: {
    width: 40,
    height: 40,
    borderRadius: 14,
    backgroundColor: LIGHT_BG,
    justifyContent: "center",
    alignItems: "center",
    marginRight: 12,
  },
  rxDate: {
    fontSize: 15,
    fontWeight: "700",
    color: TEXT_DARK,
  },
  rxStatus: {
    fontSize: 12,
    color: PRIMARY,
    fontWeight: "600",
    marginTop: 1,
  },
  rxCount: {
    fontSize: 13,
    color: TEXT_MUTED,
    fontWeight: "500",
  },
  rxItem: {
    flexDirection: "row",
    marginBottom: 12,
  },
  rxItemDot: {
    width: 8,
    height: 8,
    borderRadius: 4,
    backgroundColor: PRIMARY,
    marginTop: 6,
    marginRight: 12,
  },
  rxItemContent: {
    flex: 1,
  },
  rxMedName: {
    fontSize: 15,
    fontWeight: "700",
    color: TEXT_DARK,
  },
  rxMedDetail: {
    fontSize: 12,
    color: TEXT_MUTED,
    marginTop: 2,
  },
  rxPosologie: {
    fontSize: 13,
    color: "#0369A1",
    fontWeight: "500",
    marginTop: 4,
    backgroundColor: "#E0F2FE",
    paddingHorizontal: 8,
    paddingVertical: 3,
    borderRadius: 6,
    alignSelf: "flex-start",
  },
  rxItemFooter: {
    flexDirection: "row",
    gap: 12,
    marginTop: 4,
  },
  rxDuree: {
    fontSize: 12,
    color: TEXT_MUTED,
    fontWeight: "500",
  },
  rxQte: {
    fontSize: 12,
    color: TEXT_MUTED,
    fontWeight: "500",
  },
  rxNotes: {
    fontSize: 13,
    color: TEXT_MUTED,
    fontStyle: "italic",
    marginTop: 8,
    paddingTop: 12,
    borderTopWidth: 1,
    borderTopColor: "#F1F5F9",
  },
});
