const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_student_1";
const BOB_UID = "bob_student_2";
const ADMIN_UID = "admin_basel_1";
const ADMIN_EMAIL = "baselali2464@gmail.com";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function pastDate(msOffset = 2000) {
  return new Date(Date.now() - msOffset);
}

function validUserProfile(uid, overrides = {}) {
  const now = pastDate(5000);
  return {
    uid,
    displayName: "باسل الحربي",
    email: `${uid}@example.com`,
    major: "علوم الحاسب",
    studyYear: "السنة الثالثة",
    skills: ["Python", "SQL", "Algorithms"],
    preferredLanguage: "ar",
    aiStyle: "academic",
    role: "student",
    accountStatus: "active",
    onboardingDone: true,
    dailyTokensUsed: 420,
    gpa: 4.85,
    completedLessonsCount: 18,
    studyHoursWeek: 14.5,
    createdAt: now,
    updatedAt: now,
    ...overrides,
  };
}

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

test("Unauthenticated users are denied all reads and writes", async () => {
  const unauth = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauth.collection("users").doc(ALICE_UID).get());
  await assertFails(unauth.collection("communityPosts").where("visibility", "==", "public").get());
  await assertFails(unauth.collection("adminConfig").doc("aiSettings").get());
  await assertFails(unauth.collection("users").doc(ALICE_UID).set(validUserProfile(ALICE_UID)));
});

test("User profile: owner create/update succeeds; role escalation, ghost field, and cross-user PII read fail", async () => {
  const alice = testEnv.authenticatedContext(ALICE_UID, { email: "alice@example.com", email_verified: true }).firestore();
  const bob = testEnv.authenticatedContext(BOB_UID, { email: "bob@example.com", email_verified: true }).firestore();

  // Valid create
  await assertSucceeds(alice.collection("users").doc(ALICE_UID).set(validUserProfile(ALICE_UID)));

  // Valid partial update by owner
  await assertSucceeds(
    alice.collection("users").doc(ALICE_UID).update({
      preferredLanguage: "en",
      aiStyle: "code",
      updatedAt: pastDate(1000),
    })
  );

  // Role escalation by student fails
  await assertFails(
    alice.collection("users").doc(ALICE_UID).update({
      role: "admin",
      updatedAt: pastDate(500),
    })
  );

  // Shadow update with ghost field fails
  await assertFails(
    alice.collection("users").doc(ALICE_UID).update({
      isVerified: true,
      updatedAt: pastDate(500),
    })
  );

  // Cross-user PII read fails
  await assertFails(bob.collection("users").doc(ALICE_UID).get());
  await assertFails(bob.collection("users").get());

  // Unverified spoofed admin email fails admin escalation
  const spoofedAdmin = testEnv.authenticatedContext("spoof_1", { email: ADMIN_EMAIL, email_verified: false }).firestore();
  await assertFails(spoofedAdmin.collection("users").doc("spoof_1").set(validUserProfile("spoof_1", { role: "admin" })));
  await assertFails(spoofedAdmin.collection("users").get());
});

test("Verified bootstrapped admin can list users, change roles, configure AI settings, and inspect library assets", async () => {
  const alice = testEnv.authenticatedContext(ALICE_UID, { email: "alice@example.com", email_verified: true }).firestore();
  const admin = testEnv.authenticatedContext(ADMIN_UID, { email: ADMIN_EMAIL, email_verified: true }).firestore();

  await assertSucceeds(alice.collection("users").doc(ALICE_UID).set(validUserProfile(ALICE_UID)));
  await assertSucceeds(admin.collection("users").doc(ADMIN_UID).set(validUserProfile(ADMIN_UID, { email: ADMIN_EMAIL, role: "admin" })));

  // Admin can list users & update role
  await assertSucceeds(admin.collection("users").get());
  await assertSucceeds(
    admin.collection("users").doc(ALICE_UID).update({
      role: "instructor",
      updatedAt: pastDate(500),
    })
  );

  // Admin writes AI config; student can read get() but cannot write
  const ts = pastDate(3000);
  await assertSucceeds(
    admin.collection("adminConfig").doc("aiSettings").set({
      dailyStudentTokenLimit: 25000,
      activeModel: "gemini-3.8-flash",
      codeModeEnabled: true,
      examHintsEnabled: true,
      safetyGuardrailsStrict: true,
      createdAt: ts,
      updatedAt: ts,
    })
  );
  await assertSucceeds(alice.collection("adminConfig").doc("aiSettings").get());
  await assertFails(alice.collection("adminConfig").doc("aiSettings").update({ dailyStudentTokenLimit: 999999, updatedAt: pastDate(500) }));

  // Admin writes Library Asset Audit; student can query catalog but cannot write
  await assertSucceeds(
    admin.collection("libraryAssets").doc("clean_code").set({
      bookId: "clean_code",
      title: "Clean Code",
      category: "Software Engineering",
      fileSizeMb: 12.4,
      scanStatus: "verified",
      sha256Prefix: "a94f8c21e0",
      visibility: "catalog",
      createdAt: ts,
      updatedAt: ts,
    })
  );
  await assertSucceeds(alice.collection("libraryAssets").where("visibility", "==", "catalog").get());
  await assertFails(alice.collection("libraryAssets").doc("clean_code").update({ scanStatus: "quarantined", updatedAt: pastDate(500) }));
});

test("User-scoped subcollections: courseProgress, lessonNotes, readingState, highlights, examAttempts, aiMessages, studyPlans", async () => {
  const alice = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bob = testEnv.authenticatedContext(BOB_UID).firestore();
  const ts = pastDate(5000);

  // Course progress
  await assertSucceeds(
    alice.collection("users").doc(ALICE_UID).collection("courseProgress").doc("cloud_arch").set({
      courseId: "cloud_arch",
      completedLessonIds: ["l1", "l2"],
      lastLessonId: "l2",
      progressPercent: 45,
      createdAt: ts,
      updatedAt: ts,
    })
  );
  await assertSucceeds(alice.collection("users").doc(ALICE_UID).collection("courseProgress").get());
  await assertFails(bob.collection("users").doc(ALICE_UID).collection("courseProgress").get());

  // Lesson notes
  await assertSucceeds(
    alice.collection("users").doc(ALICE_UID).collection("lessonNotes").doc("note_1").set({
      courseId: "cloud_arch",
      lessonId: "l2",
      timestampLabel: "08:42",
      content: "Binary search reduces complexity to O(log n).",
      createdAt: ts,
      updatedAt: ts,
    })
  );
  await assertSucceeds(alice.collection("users").doc(ALICE_UID).collection("lessonNotes").get());

  // Reading state & highlights
  await assertSucceeds(
    alice.collection("users").doc(ALICE_UID).collection("readingState").doc("clean_code").set({
      bookId: "clean_code",
      currentPage: 142,
      totalPages: 464,
      progressPercent: 31,
      bookmarked: true,
      createdAt: ts,
      updatedAt: ts,
    })
  );
  await assertSucceeds(
    alice.collection("users").doc(ALICE_UID).collection("highlights").doc("hl_1").set({
      bookId: "clean_code",
      page: 142,
      quote: "Functions should do one thing. They should do it well.",
      note: "Apply SRP to ViewModels",
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Exam attempts (terminal score locking)
  await assertSucceeds(
    alice.collection("users").doc(ALICE_UID).collection("examAttempts").doc("att_1").set({
      examId: "dsa_midterm",
      examTitle: "اختبار الخوارزميات وهياكل البيانات",
      scorePercent: 93,
      correctCount: 14,
      totalQuestions: 15,
      durationSeconds: 840,
      status: "completed",
      createdAt: ts,
      updatedAt: ts,
    })
  );
  // Owner can attach AI diagnostics
  await assertSucceeds(
    alice.collection("users").doc(ALICE_UID).collection("examAttempts").doc("att_1").update({
      aiDiagnostics: "أداء ممتاز في الأشجار الثنائية، راجع تحليل التعقيد الزمني.",
      updatedAt: pastDate(1000),
    })
  );
  // Owner cannot tamper with completed score
  await assertFails(
    alice.collection("users").doc(ALICE_UID).collection("examAttempts").doc("att_1").update({
      scorePercent: 100,
      updatedAt: pastDate(500),
    })
  );

  // AI messages & study plans
  await assertSucceeds(
    alice.collection("users").doc(ALICE_UID).collection("aiMessages").doc("msg_1").set({
      sender: "user",
      mode: "code",
      text: "اشرح الفرق بين BFS و DFS بلغة بايثون",
      tokensEstimate: 42,
      createdAt: ts,
    })
  );
  await assertSucceeds(
    alice.collection("users").doc(ALICE_UID).collection("studyPlans").doc("plan_ai_track").set({
      trackId: "ai_engineer",
      trackTitle: "مسار مهندس الذكاء الاصطناعي",
      weeklyHours: 12,
      pace: "balanced",
      scheduleMarkdown: "الأحد: بايثون وهياكل البيانات (3 ساعات)",
      createdAt: ts,
      updatedAt: ts,
    })
  );
  // Future timestamp rejected
  await assertFails(
    alice.collection("users").doc(ALICE_UID).collection("aiMessages").doc("msg_future").set({
      sender: "user",
      mode: "simple",
      text: "Tampered timestamp",
      tokensEstimate: 10,
      createdAt: new Date(Date.now() + 60000),
    })
  );
});

test("Community posts: filtered query succeeds, unfiltered fails, upvote & author edits enforced", async () => {
  const alice = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bob = testEnv.authenticatedContext(BOB_UID).firestore();
  const ts = pastDate(5000);

  await assertSucceeds(
    alice.collection("communityPosts").doc("post_1").set({
      authorId: ALICE_UID,
      authorName: "أحمد السالم",
      authorRole: "طالب علوم حاسب",
      category: "خوارزميات",
      title: "مقارنة بين Dijkstra و A* في الرسوم البيانية",
      body: "متى يكون استخدام A* أسرع عملياً من خوارزمية Dijkstra؟",
      codeSnippet: "heapq.heappush(pq, (f_score, node))",
      visibility: "public",
      upvotes: 0,
      repliesCount: 0,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Filtered query matches rule
  await assertSucceeds(bob.collection("communityPosts").where("visibility", "==", "public").get());
  // Unfiltered query denied
  await assertFails(bob.collection("communityPosts").get());

  // Bob can upvote (+1)
  await assertSucceeds(
    bob.collection("communityPosts").doc("post_1").update({
      upvotes: 1,
      updatedAt: pastDate(1000),
    })
  );
  // Bob cannot jump upvotes by +100 or edit Alice's body
  await assertFails(
    bob.collection("communityPosts").doc("post_1").update({
      upvotes: 101,
      updatedAt: pastDate(500),
    })
  );
  await assertFails(
    bob.collection("communityPosts").doc("post_1").update({
      body: "Hacked content",
      updatedAt: pastDate(500),
    })
  );
  // Alice can edit her own post
  await assertSucceeds(
    alice.collection("communityPosts").doc("post_1").update({
      body: "تحديث: أضفت مثالاً عملياً باستخدام heapq في بايثون.",
      updatedAt: pastDate(500),
    })
  );
});
