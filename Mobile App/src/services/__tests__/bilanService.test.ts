import { bilanService, BilanSummary, ScanBilanResponse } from "../bilanService";
import { API_BASE_URL } from "@/config";

let mockedAxios: any;

jest.mock("axios", () => {
  const instance = {
    get: jest.fn(),
    post: jest.fn(),
    put: jest.fn(),
    delete: jest.fn(),
    interceptors: {
      request: { use: jest.fn() },
      response: { use: jest.fn() },
    },
  };
  mockedAxios = instance;
  const create = jest.fn((config) => {
    const baseURL: string = config?.baseURL ?? "";
    const wrap = <T extends (...args: any[]) => any>(fn: T): jest.Mock => {
      const mock = fn as unknown as jest.Mock;
      return jest.fn((url: string, ...args: any[]) => mock(`${baseURL}${url}`, ...args));
    };
    return {
      get: wrap(instance.get),
      post: wrap(instance.post),
      put: wrap(instance.put),
      delete: wrap(instance.delete),
      interceptors: instance.interceptors,
    };
  });
  return {
    create,
    isAxiosError: jest.fn(),
    ...instance,
  };
});

const mockSummary: BilanSummary = {
  id: "uuid-1",
  typeBilan: "Bilan sanguin",
  dateBilan: "2026-06-15",
  status: "PENDING",
  resultCount: 5,
  abnormalCount: 2,
  reviewStatus: "EN_ATTENTE",
  patientId: 1,
  doctorId: 2,
};

const mockBilan: ScanBilanResponse = {
  id: "uuid-1",
  typeBilan: "Bilan sanguin",
  format: "NOUVEAU_PATIENT",
  dateBilan: "2026-06-15",
  laboratoire: "Laboratoire Tunis",
  status: "PENDING",
  reviewStatus: "EN_ATTENTE",
  patientId: 1,
  doctorId: 2,
  resultats: [
    {
      id: 1,
      testName: "Glycemie",
      valeur: "5.2",
      unite: "mmol/L",
      referenceMin: 3.9,
      referenceMax: 6.1,
      referenceText: null,
      valeurAncienne: null,
      dateAncienne: null,
      status: "NORMAL",
    },
  ],
};

describe("bilanService", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("fetchDoctors should GET /auth/doctors", async () => {
    const mockDoctors = [
      { id: 1, firstName: "Yasmine", lastName: "Ben Salem", email: "y@test.com", phone: "20111111", specialty: "Cardiologie", hospital: "Clinique", licenseNumber: "12345" },
    ];
    mockedAxios.get.mockResolvedValue({ data: mockDoctors });

    const result = await bilanService.fetchDoctors();

    expect(mockedAxios.get).toHaveBeenCalledWith(`${API_BASE_URL}/auth/doctors`);
    expect(result).toEqual(mockDoctors);
  });

  it("assignDoctor should PUT /bilans/{id}/assign-doctor", async () => {
    mockedAxios.put.mockResolvedValue({ data: mockBilan });

    const result = await bilanService.assignDoctor("uuid-1", 2);

    expect(mockedAxios.put).toHaveBeenCalledWith(
      `${API_BASE_URL}/bilans/uuid-1/assign-doctor`,
      { doctorId: 2 }
    );
    expect(result.doctorId).toBe(2);
  });

  it("scan should POST /bilans/scan with form data", async () => {
    mockedAxios.post.mockResolvedValue({ data: mockBilan });

    const result = await bilanService.scan("file://image.jpg");

    expect(mockedAxios.post).toHaveBeenCalled();
    const callArgs = mockedAxios.post.mock.calls[0];
    expect(callArgs[0]).toBe(`${API_BASE_URL}/bilans/scan`);
    expect(callArgs[1] instanceof FormData).toBe(true);
    expect(callArgs[2]?.headers?.["Content-Type"]).toBe("multipart/form-data");
    expect(result.id).toBe("uuid-1");
  });

  it("confirm should PUT /bilans/{id}/confirm", async () => {
    const confirmed = { ...mockBilan, status: "CONFIRMED" };
    mockedAxios.put.mockResolvedValue({ data: confirmed });

    const result = await bilanService.confirm("uuid-1");

    expect(mockedAxios.put).toHaveBeenCalledWith(
      `${API_BASE_URL}/bilans/uuid-1/confirm`
    );
    expect(result.status).toBe("CONFIRMED");
  });

  it("getBilans should GET /bilans with pagination", async () => {
    const mockPage = { content: [mockSummary] };
    mockedAxios.get.mockResolvedValue({ data: mockPage });

    const result = await bilanService.getBilans(0, 20);

    expect(mockedAxios.get).toHaveBeenCalledWith(
      `${API_BASE_URL}/bilans?page=0&size=20`
    );
    expect(result.content.length).toBe(1);
    expect(result.content[0].typeBilan).toBe("Bilan sanguin");
  });

  it("getBilan should GET /bilans/{id}", async () => {
    mockedAxios.get.mockResolvedValue({ data: mockBilan });

    const result = await bilanService.getBilan("uuid-1");

    expect(mockedAxios.get).toHaveBeenCalledWith(
      `${API_BASE_URL}/bilans/uuid-1`
    );
    expect(result.resultats.length).toBe(1);
  });

  it("deleteBilan should DELETE /bilans/{id}", async () => {
    mockedAxios.delete.mockResolvedValue({ data: null });

    const result = await bilanService.deleteBilan("uuid-1");

    expect(mockedAxios.delete).toHaveBeenCalledWith(
      `${API_BASE_URL}/bilans/uuid-1`
    );
    expect(result).toBeNull();
  });

  it("updateResult should PUT /bilans/{bilanId}/results/{resultId}", async () => {
    mockedAxios.put.mockResolvedValue({ data: mockBilan });

    const result = await bilanService.updateResult("uuid-1", 1, { valeur: "6.5" });

    expect(mockedAxios.put).toHaveBeenCalledWith(
      `${API_BASE_URL}/bilans/uuid-1/results/1`,
      { valeur: "6.5" }
    );
  });

  it("should handle network error", async () => {
    mockedAxios.get.mockRejectedValue(new Error("Network error"));

    await expect(bilanService.getBilans(0, 20)).rejects.toThrow("Network error");
  });
});
