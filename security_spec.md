# Security Specification — FESC Synapse

## 1. Data Invariants

1. **Authentication & Ownership Invariant:** Every document in `/users/{userId}`, `/study_tables/{tableId}`, `/sos_alerts/{alertId}`, and `/collaboration_messages/{messageId}` must be created, read, updated, or deleted only by an authenticated user (`request.auth != null`) whose `request.auth.uid` matches `userId` (`resource.data.userId == request.auth.uid` on read/update/delete, and `request.resource.data.userId == request.auth.uid` on create).
2. **Schema & Bounds Invariant:** Every `create` and `update` operation must pass `isValidStudentProfile`, `isValidStudyTable`, `isValidSosAlert`, or `isValidCollaborationMessage` respectively, enforcing strict type checks, string length bounds, integer range bounds, and bounded list sizes.
3. **Temporal & Immutable Fields Invariant:** `createdAt` and `updatedAt` must be valid Firestore `timestamp` values (`<= request.time`). On `update`, `userId` and `createdAt` are strictly immutable (`incoming().userId == existing().userId && incoming().createdAt == existing().createdAt`).
4. **Query Enforcer Invariant:** Every `allow list` rule enforces `resource.data.userId == request.auth.uid`, prohibiting unauthenticated or unfiltered cross-user collection scans.

## 2. The "Dirty Dozen" Payloads

1. **Unauthenticated Read:** Calling `get` or `list` on `/study_tables` with `auth == null`.
2. **Cross-User Profile Read (PII Leak):** User `alice_123` attempting to `get` `/users/bob_456`.
3. **Identity Spoofing on Create:** User `alice_123` creating a `/study_tables/table_1` document with `userId: "bob_456"`.
4. **Shadow Update (Ghost Field Injection):** Updating `/study_tables/table_1` with an undeclared field `isAdminVerified: true`.
5. **Immutable Field Mutation:** Updating `/sos_alerts/sos_1` while changing `createdAt` or `userId`.
6. **Value Poisoning (String Overflow):** Creating a `SosAlert` with a 2,000-character `questionTitle` (exceeding the 140-char limit).
7. **Invalid Seat Bounds:** Creating or updating a `StudyTable` with `maxSeats: 50` or `occupiedSeats: 0`.
8. **Unbounded Array Poisoning:** Writing 25 elements into `participantNames` (exceeding max 12).
9. **Wrong Element Type in Array:** Writing `[12345]` (number instead of string) into `helperNames`.
10. **Unfiltered Collection Query:** Executing `db.collection("study_tables").get()` without `.where("userId", "==", uid)`.
11. **Future Timestamp Injection:** Writing a `createdAt` timestamp set in the future (`> request.time`).
12. **Cross-User Delete:** User `alice_123` attempting to delete `/sos_alerts/bob_sos`.
