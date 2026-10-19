const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read study_tables or sos_alerts", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("study_tables").get());
  await assertFails(unauthDb.collection("sos_alerts").get());
});

test("Authenticated user: cannot read another user's study table or profile", async () => {
  const now = new Date();
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("study_tables").doc("bob_table").set({
      userId: BOB_UID,
      subject: "Calculo Diferencial",
      topic: "Derivadas implicitas",
      campusZone: "LIBRARY",
      spotReference: "Mesa 4",
      studyVibe: "Practico",
      hostName: "Bob",
      hostProgram: "Ing. Software",
      maxSeats: 4,
      occupiedSeats: 2,
      participantNames: ["Bob", "Carlos"],
      isJoinedByCurrentUser: false,
      isActive: true,
      createdAt: now,
      updatedAt: now,
    });
    await context.firestore().collection("users").doc(BOB_UID).set({
      userId: BOB_UID,
      displayName: "Bob",
      program: "Ing. Software",
      semester: 4,
      currentZone: "LIBRARY",
      statusMode: "STUDYING",
      enrolledSubjects: ["Calculo"],
      helpedPeersCount: 2,
      createdAt: now,
      updatedAt: now,
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("study_tables").doc("bob_table").get());
  await assertFails(aliceDb.collection("users").doc(BOB_UID).get());
});

test("Authenticated user: can create and query own study tables with matching filter", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date();

  await assertSucceeds(
    aliceDb.collection("study_tables").doc("alice_table_1").set({
      userId: ALICE_UID,
      subject: "Estructura de Datos",
      topic: "Arboles AVL y Grafos",
      campusZone: "LIBRARY",
      spotReference: "Biblioteca Piso 2 - Mesa 3",
      studyVibe: "Resolucion de ejercicios",
      hostName: "Alice",
      hostProgram: "Ingenieria de Software",
      maxSeats: 5,
      occupiedSeats: 2,
      participantNames: ["Alice", "Mateo"],
      isJoinedByCurrentUser: true,
      isActive: true,
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("study_tables").where("userId", "==", ALICE_UID).get()
  );
});

test("Authenticated user: fails query without userId filter (rules are not filters)", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("study_tables").get());
  await assertFails(aliceDb.collection("sos_alerts").get());
});

test("Shadow update & identity spoofing: rejects undeclared ghost fields or spoofed userId", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date();

  await assertFails(
    aliceDb.collection("sos_alerts").doc("spoof_sos").set({
      userId: BOB_UID,
      subject: "Contabilidad",
      questionTitle: "Balance general",
      questionDetail: "No cuadra el activo con pasivo",
      examUrgency: "En 1 hora",
      campusZone: "CUBICLES",
      spotReference: "Cubiculo 2",
      authorName: "Alice",
      authorProgram: "Financiera",
      isResolved: false,
      helperNames: [],
      isCurrentUserHelping: false,
      responsesCount: 0,
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertFails(
    aliceDb.collection("sos_alerts").doc("ghost_sos").set({
      userId: ALICE_UID,
      subject: "Contabilidad",
      questionTitle: "Balance general",
      questionDetail: "No cuadra el activo con pasivo",
      examUrgency: "En 1 hora",
      campusZone: "CUBICLES",
      spotReference: "Cubiculo 2",
      authorName: "Alice",
      authorProgram: "Financiera",
      isResolved: false,
      helperNames: [],
      isCurrentUserHelping: false,
      responsesCount: 0,
      isVerifiedAdmin: true,
      createdAt: now,
      updatedAt: now,
    })
  );
});
