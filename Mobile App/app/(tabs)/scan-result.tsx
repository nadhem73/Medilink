import { useLocalSearchParams, router } from "expo-router";
import React, { useCallback, useEffect, useState } from "react";
import {
  ActivityIndicator,
  Alert,
  Modal,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
  View,
} from "react-native";
import { Ionicons } from "@expo/vector-icons";
import {
  bilanService,
  BilanResultDto,
  DoctorDto,
  ScanBilanResponse,
} from "@/services/bilanService";

const PRIMARY = "#0066A2";
const WHITE = "#FFFFFF";
const BG = "#F5F8FA";

type ResultStatus = "NORMAL" | "ANORMAL" | "CRITIQUE" | "NON_APPLICABLE";

const STATUS_COLORS: Record<ResultStatus, { bg: string; text: string; label: string }> = {
  NORMAL: { bg: "#DCFCE7", text: "#166534", label: "Normal" },
  ANORMAL: { bg: "#FEF3C7", text: "#92400E", label: "Anormal" },
  CRITIQUE: { bg: "#FEE2E2", text: "#991B1B", label: "Critique" },
  NON_APPLICABLE: { bg: "#F1F5F9", text: "#64748B", label: "N/A" },
};

export default function ScanResultScreen() {
  const { bilanId } = useLocalSearchParams<{ bilanId: string }>();
  const [bilan, setBilan] = useState<ScanBilanResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [confirming, setConfirming] = useState(false);

  const [doctorModalVisible, setDoctorModalVisible] = useState(false);
  const [doctors, setDoctors] = useState<DoctorDto[]>([]);
  const [selectedDoctorId, setSelectedDoctorId] = useState<number | null>(null);
  const [loadingDoctors, setLoadingDoctors] = useState(false);
  const [assigning, setAssigning] = useState(false);

  const [editingResult, setEditingResult] = useState<BilanResultDto | null>(null);
  const [editValue, setEditValue] = useState("");
  const [editAncienneValue, setEditAncienneValue] = useState("");
  const [editRefText, setEditRefText] = useState("");
  const [saving, setSaving] = useState(false);

  const loadBilan = useCallback(async () => {
    try {
      const data = await bilanService.getBilan(bilanId!);
      setBilan(data);
    } catch {
      Alert.alert("Erreur", "Impossible de charger les données du bilan.", [
        { text: "Retour", onPress: () => router.back() },
      ]);
    } finally {
      setLoading(false);
    }
  }, [bilanId]);

  useEffect(() => {
    if (bilanId) {
      loadBilan();
    }
  }, [loadBilan, bilanId]);

  const handleConfirm = async () => {
    setConfirming(true);
    try {
      await bilanService.confirm(bilanId!);
      setConfirming(false);
      setDoctorModalVisible(true);
      setLoadingDoctors(true);
      try {
        const doctorList = await bilanService.fetchDoctors();
        setDoctors(doctorList);
      } catch {
        // Doctors fetch failed, modal shows empty state
      }
    } catch {
      Alert.alert("Erreur", "Impossible de confirmer le bilan.");
    } finally {
      setConfirming(false);
      setLoadingDoctors(false);
    }
  };

  const handleAssignDoctor = async () => {
    if (!selectedDoctorId) {
      router.back();
      return;
    }
    setAssigning(true);
    try {
      await bilanService.assignDoctor(bilanId!, selectedDoctorId);
      router.back();
    } catch {
      Alert.alert("Erreur", "Impossible d'assigner le médecin.");
    } finally {
      setAssigning(false);
    }
  };

  const handleDelete = () => {
    Alert.alert("Supprimer", "Voulez-vous annuler ce bilan ?", [
      { text: "Non", style: "cancel" },
      {
        text: "Oui",
        style: "destructive",
        onPress: async () => {
          try {
            await bilanService.deleteBilan(bilanId!);
            router.back();
          } catch {
            Alert.alert("Erreur", "Impossible de supprimer le bilan.");
          }
        },
      },
    ]);
  };

  const openEdit = (result: BilanResultDto) => {
    setEditingResult(result);
    setEditValue(result.valeur || "");
    setEditAncienneValue(result.valeurAncienne || "");
    setEditRefText(result.referenceText || "");
  };

  const closeEdit = () => {
    setEditingResult(null);
    setEditValue("");
    setEditAncienneValue("");
    setEditRefText("");
  };

  const handleSave = async () => {
    if (!editingResult) return;
    setSaving(true);
    try {
      const data: { valeur?: string; valeurAncienne?: string; referenceText?: string | null } = {};
      if (editValue !== editingResult.valeur) data.valeur = editValue;
      if (editAncienneValue !== editingResult.valeurAncienne) data.valeurAncienne = editAncienneValue;
      if (editRefText !== (editingResult.referenceText || "")) data.referenceText = editRefText || null;
      if (Object.keys(data).length === 0) { closeEdit(); return; }
      const updated = await bilanService.updateResult(bilanId!, editingResult.id, data);
      setBilan(updated);
      closeEdit();
    } catch {
      Alert.alert("Erreur", "Impossible de modifier la valeur.");
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <View style={styles.center}>
        <ActivityIndicator size="large" color={PRIMARY} />
        <Text style={styles.loadingText}>Chargement des données...</Text>
      </View>
    );
  }

  if (!bilan) {
    return (
      <View style={styles.center}>
        <Text style={styles.errorText}>Bilan introuvable</Text>
        <TouchableOpacity onPress={() => router.back()}>
          <Text style={styles.backLink}>Retour</Text>
        </TouchableOpacity>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <TouchableOpacity onPress={() => router.back()} style={styles.backBtn}>
          <Ionicons name="arrow-back" size={24} color="#1E293B" />
        </TouchableOpacity>
        <Text style={styles.headerTitle}>Résultat du scan</Text>
      </View>

      <ScrollView
        style={styles.scroll}
        contentContainerStyle={styles.scrollContent}
      >
        <View style={styles.infoCard}>
          <View style={styles.infoRow}>
            <Text style={styles.infoLabel}>Type</Text>
            <Text style={styles.infoValue}>
              {bilan.typeBilan || "Non détecté"}
            </Text>
          </View>
          <View style={styles.infoRow}>
            <Text style={styles.infoLabel}>Date</Text>
            <Text style={styles.infoValue}>
              {bilan.dateBilan || "Non détectée"}
            </Text>
          </View>
          {bilan.laboratoire && (
            <View style={styles.infoRow}>
              <Text style={styles.infoLabel}>Laboratoire</Text>
              <Text style={styles.infoValue}>{bilan.laboratoire}</Text>
            </View>
          )}
          <View style={styles.infoRow}>
            <Text style={styles.infoLabel}>Format</Text>
            <Text style={styles.infoValue}>
              {bilan.format === "ANCIEN_PATIENT"
                ? "Avec historique"
                : "Standard"}
            </Text>
          </View>
        </View>

        <Text style={styles.sectionTitle}>
          Résultats ({bilan.resultats.length})
        </Text>

        {bilan.resultats.length === 0 ? (
          <View style={styles.emptyCard}>
            <Ionicons name="document-text-outline" size={40} color="#94A3B8" />
            <Text style={styles.emptyText}>
              Aucun résultat détecté. Vous pouvez réessayer avec une meilleure
              photo.
            </Text>
          </View>
        ) : (
          <View style={styles.table}>
            <View style={styles.tableHeader}>
              <Text style={[styles.tableHeaderCell, { flex: 2 }]}>Test</Text>
              <Text style={[styles.tableHeaderCell, { flex: 1 }]}>Valeur</Text>
              <Text style={[styles.tableHeaderCell, { flex: 1.2 }]}>
                Référence
              </Text>
              {bilan.format === "ANCIEN_PATIENT" && (
                <Text style={[styles.tableHeaderCell, { flex: 1 }]}>
                  Ancien
                </Text>
              )}
            </View>
            {bilan.resultats.map((r) => (
              <ResultRow
                key={r.id}
                result={r}
                showAncien={bilan.format === "ANCIEN_PATIENT"}
                onEdit={openEdit}
              />
            ))}
          </View>
        )}
      </ScrollView>

      <View style={styles.footer}>
        <TouchableOpacity
          style={styles.confirmBtn}
          onPress={handleConfirm}
          disabled={confirming}
        >
          {confirming ? (
            <ActivityIndicator color={WHITE} />
          ) : (
            <>
              <Ionicons name="checkmark-circle" size={22} color={WHITE} />
              <Text style={styles.confirmBtnText}>
                Confirmer et enregistrer
              </Text>
            </>
          )}
        </TouchableOpacity>
        <TouchableOpacity style={styles.deleteBtn} onPress={handleDelete}>
          <Ionicons name="trash-outline" size={20} color="#DC2626" />
          <Text style={styles.deleteBtnText}>Annuler</Text>
        </TouchableOpacity>
      </View>

      <Modal visible={doctorModalVisible} transparent animationType="slide">
        <View style={styles.modalOverlay}>
          <View style={styles.doctorModal}>
            <View style={styles.modalHeader}>
              <Text style={styles.modalTitle}>Envoyer à un médecin</Text>
              <TouchableOpacity onPress={() => router.back()}>
                <Ionicons name="close" size={24} color="#64748B" />
              </TouchableOpacity>
            </View>
            <Text style={styles.doctorModalSubtitle}>
              Choisissez un médecin pour partager ce bilan (optionnel)
            </Text>

            {loadingDoctors ? (
              <ActivityIndicator
                size="large"
                color={PRIMARY}
                style={{ marginVertical: 40 }}
              />
            ) : doctors.length === 0 ? (
              <View style={styles.emptyCard}>
                <Ionicons name="people-outline" size={40} color="#94A3B8" />
                <Text style={styles.emptyText}>
                  Aucun médecin disponible pour le moment.
                </Text>
              </View>
            ) : (
              <ScrollView style={styles.doctorList}>
                {doctors.map((doc) => (
                  <TouchableOpacity
                    key={doc.id}
                    style={[
                      styles.doctorCard,
                      selectedDoctorId === doc.id &&
                        styles.doctorCardSelected,
                    ]}
                    onPress={() => setSelectedDoctorId(doc.id)}
                    activeOpacity={0.7}
                  >
                    <View style={styles.doctorAvatar}>
                      <Text style={styles.doctorAvatarText}>
                        {doc.firstName[0]}
                        {doc.lastName[0]}
                      </Text>
                    </View>
                    <View style={styles.doctorInfo}>
                      <Text style={styles.doctorName}>
                        Dr. {doc.firstName} {doc.lastName}
                      </Text>
                      {doc.specialty && (
                        <Text style={styles.doctorSpecialty}>
                          {doc.specialty}
                        </Text>
                      )}
                      {doc.hospital && (
                        <Text style={styles.doctorHospital}>
                          {doc.hospital}
                        </Text>
                      )}
                    </View>
                    {selectedDoctorId === doc.id && (
                      <Ionicons
                        name="checkmark-circle"
                        size={24}
                        color={PRIMARY}
                      />
                    )}
                  </TouchableOpacity>
                ))}
              </ScrollView>
            )}

            <View style={styles.doctorActions}>
              <TouchableOpacity
                style={styles.skipBtn}
                onPress={() => router.back()}
              >
                <Text style={styles.skipBtnText}>Passer</Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={[
                  styles.assignBtn,
                  (!selectedDoctorId || assigning) && { opacity: 0.6 },
                ]}
                onPress={handleAssignDoctor}
                disabled={!selectedDoctorId || assigning}
              >
                {assigning ? (
                  <ActivityIndicator color={WHITE} size="small" />
                ) : (
                  <>
                    <Ionicons name="send" size={18} color={WHITE} />
                    <Text style={styles.assignBtnText}>Envoyer</Text>
                  </>
                )}
              </TouchableOpacity>
            </View>
          </View>
        </View>
      </Modal>

      <Modal visible={!!editingResult} transparent animationType="slide">
        <View style={styles.modalOverlay}>
          <View style={styles.modalContent}>
            <View style={styles.modalHeader}>
              <Text style={styles.modalTitle}>Modifier la valeur</Text>
              <TouchableOpacity onPress={closeEdit}>
                <Ionicons name="close" size={24} color="#64748B" />
              </TouchableOpacity>
            </View>
            {editingResult && (
              <>
                <Text style={styles.modalLabel}>{editingResult.testName}</Text>
                {editingResult.unite && (
                  <Text style={styles.modalUnit}>{editingResult.unite}</Text>
                )}
                <Text style={styles.inputLabel}>Valeur</Text>
                <TextInput
                  style={styles.modalInput}
                  value={editValue}
                  onChangeText={setEditValue}
                  placeholder="Entrez la valeur"
                  keyboardType="numeric"
                />
                <Text style={styles.inputLabel}>Référence</Text>
                <TextInput
                  style={styles.modalInput}
                  value={editRefText}
                  onChangeText={setEditRefText}
                  placeholder="Ex: 11.5 - 15.5, < 0.5, Négatif"
                />
                {bilan?.format === "ANCIEN_PATIENT" && (
                  <>
                    <Text style={styles.inputLabel}>Valeur ancienne</Text>
                    <TextInput
                      style={styles.modalInput}
                      value={editAncienneValue}
                      onChangeText={setEditAncienneValue}
                      placeholder="Entrez la valeur ancienne"
                      keyboardType="numeric"
                    />
                  </>
                )}
                <View style={styles.modalActions}>
                  <TouchableOpacity style={styles.modalCancelBtn} onPress={closeEdit}>
                    <Text style={styles.modalCancelText}>Annuler</Text>
                  </TouchableOpacity>
                  <TouchableOpacity
                    style={[styles.modalSaveBtn, saving && { opacity: 0.6 }]}
                    onPress={handleSave}
                    disabled={saving}
                  >
                    {saving ? (
                      <ActivityIndicator color={WHITE} size="small" />
                    ) : (
                      <Text style={styles.modalSaveText}>Enregistrer</Text>
                    )}
                  </TouchableOpacity>
                </View>
              </>
            )}
          </View>
        </View>
      </Modal>
    </View>
  );
}

const CONFIDENCE_COLORS: Record<string, string> = {
  haute: "#16A34A",
  moyenne: "#D97706",
  faible: "#DC2626",
};

function ResultRow({
  result,
  showAncien,
  onEdit,
}: {
  result: BilanResultDto;
  showAncien: boolean;
  onEdit: (r: BilanResultDto) => void;
}) {
  const statusInfo =
    STATUS_COLORS[(result.status as ResultStatus) || "NON_APPLICABLE"];

  const confianceColor = result.confiance
    ? CONFIDENCE_COLORS[result.confiance] || "#D97706"
    : null;

  const refText = result.referenceText ||
    (result.referenceMin != null && result.referenceMax != null
      ? `${result.referenceMin} - ${result.referenceMax}`
      : "-");

  return (
    <TouchableOpacity style={styles.tableRow} onPress={() => onEdit(result)} activeOpacity={0.6}>
      <View style={[styles.tableCell, { flex: 2 }]}>
        <Text style={styles.testName} numberOfLines={2}>
          {result.testName}
        </Text>
        {result.unite && (
          <Text style={styles.unitText}>{result.unite}</Text>
        )}
      </View>
      <View style={[styles.tableCell, { flex: 1 }]}>
        <Text
          style={[
            styles.valueText,
            confianceColor && { color: confianceColor, fontWeight: "700" },
          ]}
        >
          {result.valeur || "-"}
        </Text>

        <View style={styles.statusBadge}>
          <Text style={styles.statusTextNeutral}>
            {statusInfo.label}
          </Text>
        </View>
      </View>
      <View style={[styles.tableCell, { flex: 1.2 }]}>
        <Text style={styles.refText}>{refText}</Text>
      </View>
      {showAncien && (
        <View style={[styles.tableCell, { flex: 1 }]}>
          <Text style={styles.valueText}>
            {result.valeurAncienne || "-"}
          </Text>
          {result.dateAncienne && (
            <Text style={styles.dateText}>{result.dateAncienne}</Text>
          )}
        </View>
      )}
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: BG },
  center: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
    backgroundColor: BG,
    gap: 12,
  },
  loadingText: { fontSize: 15, color: "#64748B", marginTop: 12 },
  errorText: { fontSize: 16, color: "#DC2626" },
  backLink: { fontSize: 16, color: PRIMARY, marginTop: 12 },
  header: {
    flexDirection: "row",
    alignItems: "center",
    paddingTop: 60,
    paddingHorizontal: 20,
    paddingBottom: 16,
    backgroundColor: BG,
  },
  backBtn: { marginRight: 12, padding: 4 },
  headerTitle: { fontSize: 22, fontWeight: "700", color: "#1E293B" },
  scroll: { flex: 1 },
  scrollContent: { paddingHorizontal: 20, paddingBottom: 20 },

  infoCard: {
    backgroundColor: WHITE,
    borderRadius: 16,
    padding: 16,
    marginBottom: 20,
    shadowColor: "#000",
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 4,
    elevation: 2,
  },
  infoRow: {
    flexDirection: "row",
    justifyContent: "space-between",
    paddingVertical: 8,
    borderBottomWidth: 1,
    borderBottomColor: "#F1F5F9",
  },
  infoLabel: { fontSize: 14, color: "#64748B" },
  infoValue: { fontSize: 14, fontWeight: "600", color: "#1E293B" },

  sectionTitle: {
    fontSize: 18,
    fontWeight: "700",
    color: "#1E293B",
    marginBottom: 12,
  },

  table: {
    backgroundColor: WHITE,
    borderRadius: 16,
    overflow: "hidden",
    shadowColor: "#000",
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 4,
    elevation: 2,
  },
  tableHeader: {
    flexDirection: "row",
    backgroundColor: "#F8FAFC",
    borderBottomWidth: 1,
    borderBottomColor: "#E2E8F0",
    paddingVertical: 12,
    paddingHorizontal: 12,
  },
  tableHeaderCell: {
    fontSize: 12,
    fontWeight: "700",
    color: "#64748B",
    textTransform: "uppercase",
  },
  tableRow: {
    flexDirection: "row",
    borderBottomWidth: 1,
    borderBottomColor: "#F1F5F9",
    paddingVertical: 12,
    paddingHorizontal: 12,
    alignItems: "center",
  },
  tableCell: { justifyContent: "center" },
  testName: { fontSize: 14, fontWeight: "500", color: "#1E293B" },
  unitText: { fontSize: 11, color: "#94A3B8", marginTop: 2 },
  valueText: { fontSize: 14, fontWeight: "600", color: "#1E293B" },
  refText: { fontSize: 13, color: "#64748B" },
  dateText: { fontSize: 11, color: "#94A3B8", marginTop: 2 },
  statusBadge: {
    alignSelf: "flex-start",
    paddingHorizontal: 8,
    paddingVertical: 2,
    borderRadius: 8,
    marginTop: 4,
    backgroundColor: "#F1F5F9",
  },
  statusText: { fontSize: 11, fontWeight: "600" },
  statusTextNeutral: { fontSize: 11, fontWeight: "600", color: "#64748B" },

  emptyCard: {
    backgroundColor: WHITE,
    borderRadius: 16,
    padding: 32,
    alignItems: "center",
    gap: 12,
  },
  emptyText: { fontSize: 14, color: "#64748B", textAlign: "center" },

  footer: {
    padding: 20,
    paddingBottom: 32,
    backgroundColor: BG,
    gap: 12,
  },
  confirmBtn: {
    backgroundColor: PRIMARY,
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "center",
    paddingVertical: 16,
    borderRadius: 16,
    gap: 8,
    shadowColor: PRIMARY,
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.25,
    shadowRadius: 8,
    elevation: 4,
  },
  confirmBtnText: {
    color: WHITE,
    fontSize: 16,
    fontWeight: "600",
  },
  deleteBtn: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "center",
    gap: 6,
    paddingVertical: 10,
  },
  deleteBtnText: {
    color: "#DC2626",
    fontSize: 14,
    fontWeight: "500",
  },

  modalOverlay: {
    flex: 1,
    backgroundColor: "rgba(0,0,0,0.5)",
    justifyContent: "flex-end",
  },
  modalContent: {
    backgroundColor: WHITE,
    borderTopLeftRadius: 24,
    borderTopRightRadius: 24,
    padding: 24,
    paddingBottom: 40,
  },
  modalHeader: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    marginBottom: 20,
  },
  modalTitle: { fontSize: 20, fontWeight: "700", color: "#1E293B" },
  modalLabel: { fontSize: 16, fontWeight: "600", color: "#1E293B", marginBottom: 4 },
  modalUnit: { fontSize: 13, color: "#94A3B8", marginBottom: 16 },
  inputLabel: { fontSize: 13, fontWeight: "600", color: "#64748B", marginBottom: 6, marginTop: 12 },
  modalInput: {
    borderWidth: 1,
    borderColor: "#E2E8F0",
    borderRadius: 12,
    padding: 14,
    fontSize: 16,
    color: "#1E293B",
    backgroundColor: "#F8FAFC",
  },
  modalActions: {
    flexDirection: "row",
    gap: 12,
    marginTop: 24,
  },
  modalCancelBtn: {
    flex: 1,
    paddingVertical: 14,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: "#E2E8F0",
    alignItems: "center",
  },
  modalCancelText: { fontSize: 15, fontWeight: "600", color: "#64748B" },
  modalSaveBtn: {
    flex: 1,
    paddingVertical: 14,
    borderRadius: 12,
    backgroundColor: PRIMARY,
    alignItems: "center",
  },
  modalSaveText: { fontSize: 15, fontWeight: "600", color: WHITE },

  doctorModal: {
    backgroundColor: WHITE,
    borderTopLeftRadius: 24,
    borderTopRightRadius: 24,
    padding: 24,
    paddingBottom: 40,
    maxHeight: "80%",
  },
  doctorModalSubtitle: {
    fontSize: 14,
    color: "#64748B",
    marginBottom: 20,
    lineHeight: 20,
  },
  doctorList: { maxHeight: 320 },
  doctorCard: {
    flexDirection: "row",
    alignItems: "center",
    padding: 14,
    borderRadius: 12,
    backgroundColor: "#F8FAFC",
    marginBottom: 8,
    borderWidth: 1.5,
    borderColor: "#F1F5F9",
  },
  doctorCardSelected: {
    borderColor: PRIMARY,
    backgroundColor: "#EFF6FF",
  },
  doctorAvatar: {
    width: 44,
    height: 44,
    borderRadius: 22,
    backgroundColor: PRIMARY,
    justifyContent: "center",
    alignItems: "center",
    marginRight: 12,
  },
  doctorAvatarText: {
    color: WHITE,
    fontSize: 16,
    fontWeight: "700",
  },
  doctorInfo: { flex: 1 },
  doctorName: {
    fontSize: 15,
    fontWeight: "600",
    color: "#1E293B",
  },
  doctorSpecialty: {
    fontSize: 13,
    color: PRIMARY,
    marginTop: 2,
  },
  doctorHospital: {
    fontSize: 12,
    color: "#94A3B8",
    marginTop: 1,
  },
  doctorActions: {
    flexDirection: "row",
    gap: 12,
    marginTop: 20,
  },
  skipBtn: {
    flex: 1,
    paddingVertical: 14,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: "#E2E8F0",
    alignItems: "center",
  },
  skipBtnText: { fontSize: 15, fontWeight: "600", color: "#64748B" },
  assignBtn: {
    flex: 1,
    paddingVertical: 14,
    borderRadius: 12,
    backgroundColor: PRIMARY,
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "center",
    gap: 6,
  },
  assignBtnText: { fontSize: 15, fontWeight: "600", color: WHITE },
});
