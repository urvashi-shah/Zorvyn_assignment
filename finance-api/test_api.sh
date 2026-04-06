#!/usr/bin/env bash
set -euo pipefail

# Simple API smoke test for Zorvyn assignment
# Usage:
#   1) Start server: mvn spring-boot:run
#   2) Run: bash test_api.sh

BASE_URL="${BASE_URL:-http://localhost:8080}"
ADMIN_EMAIL="${ADMIN_EMAIL:-admin@zorvyn.com}"
ADMIN_PASSWORD="${ADMIN_PASSWORD:-admin123}"

if ! command -v curl >/dev/null 2>&1; then
  echo "curl is required"
  exit 1
fi

if ! command -v python >/dev/null 2>&1; then
  echo "python is required"
  exit 1
fi

PASS=0
TOTAL=0

check() {
  local name="$1"
  local expected="$2"
  local actual="$3"
  TOTAL=$((TOTAL + 1))
  if [[ "$expected" == "$actual" ]]; then
    PASS=$((PASS + 1))
    echo "PASS - $name (HTTP $actual)"
  else
    echo "FAIL - $name (expected $expected, got $actual)"
    exit 1
  fi
}

req() {
  local method="$1"
  local url="$2"
  local body="${3:-}"
  local token="${4:-}"
  local out
  out="$(mktemp)"

  local code
  if [[ -n "$token" ]]; then
    code=$(curl -s -o "$out" -w "%{http_code}" -X "$method" "$url" \
      -H "Authorization: Bearer $token" \
      -H "Content-Type: application/json" \
      ${body:+-d "$body"})
  else
    code=$(curl -s -o "$out" -w "%{http_code}" -X "$method" "$url" \
      -H "Content-Type: application/json" \
      ${body:+-d "$body"})
  fi

  RESPONSE="$(cat "$out")"
  HTTP_CODE="$code"
  rm -f "$out"
}

json_field() {
  local json="$1"
  local field="$2"
  JSON_INPUT="$json" FIELD_NAME="$field" python - <<'PY'
import json
import os

field = os.environ.get("FIELD_NAME", "")
raw = os.environ.get("JSON_INPUT", "")
try:
    data = json.loads(raw) if raw else {}
except Exception:
    print("")
    raise SystemExit(0)
value = data.get(field, "")
print("" if value is None else value)
PY
}

echo "Running simple API checks on $BASE_URL"

# 1) Admin login
req "POST" "$BASE_URL/api/auth/login" "{\"email\":\"$ADMIN_EMAIL\",\"password\":\"$ADMIN_PASSWORD\"}"
check "Admin login" "200" "$HTTP_CODE"
ADMIN_TOKEN=$(json_field "$RESPONSE" "token")

# 2) Create analyst (ignore duplicate as 400)
req "POST" "$BASE_URL/api/users" "{\"email\":\"analyst@test.com\",\"password\":\"analyst123\",\"role\":\"ANALYST\"}" "$ADMIN_TOKEN"
if [[ "$HTTP_CODE" != "200" && "$HTTP_CODE" != "201" && "$HTTP_CODE" != "400" ]]; then
  check "Create analyst user" "201" "$HTTP_CODE"
else
  TOTAL=$((TOTAL + 1)); PASS=$((PASS + 1)); echo "PASS - Create analyst user (or already exists)"
fi

# 3) Create viewer (ignore duplicate as 400)
req "POST" "$BASE_URL/api/users" "{\"email\":\"viewer@test.com\",\"password\":\"viewer123\",\"role\":\"VIEWER\"}" "$ADMIN_TOKEN"
if [[ "$HTTP_CODE" != "200" && "$HTTP_CODE" != "201" && "$HTTP_CODE" != "400" ]]; then
  check "Create viewer user" "201" "$HTTP_CODE"
else
  TOTAL=$((TOTAL + 1)); PASS=$((PASS + 1)); echo "PASS - Create viewer user (or already exists)"
fi

# 4) Analyst login
req "POST" "$BASE_URL/api/auth/login" "{\"email\":\"analyst@test.com\",\"password\":\"analyst123\"}"
check "Analyst login" "200" "$HTTP_CODE"
ANALYST_TOKEN=$(json_field "$RESPONSE" "token")

# 5) Viewer login
req "POST" "$BASE_URL/api/auth/login" "{\"email\":\"viewer@test.com\",\"password\":\"viewer123\"}"
check "Viewer login" "200" "$HTTP_CODE"
VIEWER_TOKEN=$(json_field "$RESPONSE" "token")

# 6) Admin creates record
req "POST" "$BASE_URL/api/records" "{\"amount\":1000,\"type\":\"INCOME\",\"category\":\"Salary\",\"date\":\"2026-04-01\",\"notes\":\"salary\"}" "$ADMIN_TOKEN"
check "Admin create record" "200" "$HTTP_CODE"
RECORD_ID=$(json_field "$RESPONSE" "id")

# 7) Analyst can list records
req "GET" "$BASE_URL/api/records" "" "$ANALYST_TOKEN"
check "Analyst list records" "200" "$HTTP_CODE"

# 8) Viewer cannot list records
req "GET" "$BASE_URL/api/records" "" "$VIEWER_TOKEN"
check "Viewer blocked from records list" "403" "$HTTP_CODE"

# 9) Validation check
req "POST" "$BASE_URL/api/records" "{\"amount\":-5,\"type\":\"EXPENSE\",\"category\":\"Food\",\"date\":\"2026-04-02\",\"notes\":\"bad\"}" "$ADMIN_TOKEN"
check "Negative amount rejected" "400" "$HTTP_CODE"

# 10) Dashboard visible for viewer
req "GET" "$BASE_URL/api/dashboard/summary" "" "$VIEWER_TOKEN"
check "Viewer dashboard summary" "200" "$HTTP_CODE"

# 11) Delete record as admin
req "DELETE" "$BASE_URL/api/records/$RECORD_ID" "" "$ADMIN_TOKEN"
check "Admin delete record" "200" "$HTTP_CODE"

# 12) No token should fail
req "GET" "$BASE_URL/api/dashboard/summary"
check "No token rejected" "401" "$HTTP_CODE"

echo ""
echo "Done: $PASS/$TOTAL checks passed"
echo "Swagger: $BASE_URL/swagger-ui/index.html"
