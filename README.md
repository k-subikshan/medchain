# MedChain

### Blockchain-Based Medicine Traceability & Prescription Verification

MedChain is a secure healthcare workflow platform designed to improve **medicine traceability, prescription authorization, patient verification, and dispensing control**.

The system combines a relational database with cryptographic hashing and a blockchain-based audit layer to create a transparent and tamper-evident record of important healthcare operations.

---

## ✨ Features

- 🔐 Role-based access control
- 💊 Medicine and batch registration
- 🔗 SHA-256 based data integrity
- ⛓️ Blockchain/hash-based audit trail
- 👨‍⚕️ Doctor prescription management
- 👨‍⚕️ Doctor-selected prescription validation
- 🏥 Patient medical-history awareness
- 💊 Prescription dose/quantity protection
- 📧 Patient email verification using OTP
- 🔑 Optional OTP verification before medicine dispensing
- 🏪 Pharmacy medicine verification
- 📋 Pharmacy prescription verification
- 👤 Patient-specific medical and prescription history
- 🛡️ Prevention of prescription reuse after dose exhaustion
- 📊 Admin audit and blockchain monitoring
- 🚦 API request rate limiting

---

## 🏗️ System Workflow

```text
                    ┌──────────────────┐
                    │   Manufacturer   │
                    └────────┬─────────┘
                             │
                    Register Medicine
                             │
                       Create Batch
                             │
                      Generate Hash
                             │
                             ▼
                    ┌──────────────────┐
                    │     Doctor       │
                    └────────┬─────────┘
                             │
                    Select Patient
                             │
                    Review History
                             │
                   Create Prescription
                             │
                 ┌───────────┴───────────┐
                 │                       │
            Normal Flow           Validation Required
                 │                       │
              ACTIVE              Select Doctor
                                         │
                                  PENDING_VALIDATION
                                         │
                                  Assigned Doctor
                                         │
                                      Validate
                                         │
                                       ACTIVE
                 │                       │
                 └───────────┬───────────┘
                             │
                             ▼
                    ┌──────────────────┐
                    │     Pharmacy     │
                    └────────┬─────────┘
                             │
                    Verify Medicine
                             │
                    Verify Prescription
                             │
                    Request Dispensing
                             │
                     Optional OTP
                             │
                             ▼
                         Dispense
                             │
                             ▼
                    Blockchain / Audit
