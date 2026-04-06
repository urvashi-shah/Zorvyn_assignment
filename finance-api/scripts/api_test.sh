#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
ADMIN_EMAIL="${ADMIN_EMAIL:-admin@zorvyn.com}"
ADMIN_PASSWORD="${ADMIN_PASSWORD:-admin123}"

if ! command -v jq >/dev/null 2>&1; then
  echo "jq is required. Please install jq and rerun."
  exit 1
fi

PASS=0
TOTAL=0

print_test() {
  echo ""
  echo "[$1] $2"
}

assert_status() {
  local actual="$1"
  local expected="$2"
  local name="$3"
  TOTAL=$((TOTAL+1))
  if [[ "$actual" == "$expected" ]]; then
    PASS=$((PASS+1))
    echo "PASS: $name (expected $expected, got $actual)"
  else
    echo "FAIL: $name (expected $expected, got $actual)"
    exit 1
  fi
}

request() {
  local method="$1"
  local url="$2"
  local body="${3:-}"
  local token="${4:-}"
  local tmp
  tmp="$(mktemp)"
  local code
  if [[ -n "$token" ]]; then
    code=$(curl -s -o "$tmp" -w "%{http_code}" -X "$method" "$url" \
      -H "Authorization: Bearer $token" \
      -H "Content-Type: application/json" \
      ${body:+-d "$body"})
  else
    code=$(curl -s -o "$tmp" -w "%{http_code}" -X "$method" "$url" \
      -H "Content-Type: application/json" \
      ${body:+-d "$body"})
  fi
  RESPONSE_BODY="$(cat "$tmp")"
  rm -f "$tmp"
  echo "$code"
}

print_test "AUTH" "Admin login"
code=$(request "POST" "$BASE_URL/api/auth/login" "{\"email\":\"$ADMIN_EMAIL\",\"password\":\"$ADMIN_PASSWORD\"}")
assert_status "$code" "200" "Admin login should succeed"
ADMIN_TOKEN="$(echo "$RESPONSE_BODY" | jq -r '.token')"

print_test "AUTH" "Invalid login"
code=$(request "POST" "$BASE_URL/api/auth/login" "{\"email\":\"$ADMIN_EMAIL\",\"password\":\"wrong123\"}")
assert_status "$code" "401" "Invalid login should return 401"

print_test "USERS" "Create analyst and viewer users"
code=$(request "POST" "$BASE_URL/api/users" "{\"email\":\"analyst@zorvyn.com\",\"password\":\"analyst123\",\"role\":\"ANALYST\"}" "$ADMIN_TOKEN")
if [[ "$code" != "201" && "$code" != "200" && "$code" != "400" ]]; then
  assert_status "$code" "201" "Create analyst user"
fi
code=$(request "POST" "$BASE_URL/api/users" "{\"email\":\"viewer@zorvyn.com\",\"password\":\"viewer123\",\"role\":\"VIEWER\"}" "$ADMIN_TOKEN")
if [[ "$code" != "201" && "$code" != "200" && "$code" != "400" ]]; then
  assert_status "$code" "201" "Create viewer user"
fi
PASS=$((PASS+2))
TOTAL=$((TOTAL+2))
echo "PASS: User create calls accepted (or already existed)"

print_test "AUTH" "Login analyst and viewer"
code=$(request "POST" "$BASE_URL/api/auth/login" "{\"email\":\"analyst@zorvyn.com\",\"password\":\"analyst123\"}")
assert_status "$code" "200" "Analyst login should succeed"
ANALYST_TOKEN="$(echo "$RESPONSE_BODY" | jq -r '.token')"
code=$(request "POST" "$BASE_URL/api/auth/login" "{\"email\":\"viewer@zorvyn.com\",\"password\":\"viewer123\"}")
assert_status "$code" "200" "Viewer login should succeed"
VIEWER_TOKEN="$(echo "$RESPONSE_BODY" | jq -r '.token')"

print_test "RECORDS" "Create and read records"
code=$(request "POST" "$BASE_URL/api/records" "{\"amount\":5000,\"type\":\"INCOME\",\"category\":\"Salary\",\"date\":\"2026-04-01\",\"notes\":\"April Salary\"}" "$ADMIN_TOKEN")
assert_status "$code" "200" "Admin can create income record"
RECORD_ID="$(echo "$RESPONSE_BODY" | jq -r '.id')"
code=$(request "GET" "$BASE_URL/api/records" "" "$ANALYST_TOKEN")
assert_status "$code" "200" "Analyst can list records"
code=$(request "GET" "$BASE_URL/api/records" "" "$VIEWER_TOKEN")
assert_status "$code" "403" "Viewer cannot list records"

print_test "RBAC" "Non-admin cannot create records"
code=$(request "POST" "$BASE_URL/api/records" "{\"amount\":100,\"type\":\"EXPENSE\",\"category\":\"Food\",\"date\":\"2026-04-02\",\"notes\":\"Lunch\"}" "$ANALYST_TOKEN")
assert_status "$code" "403" "Analyst cannot create records"

print_test "VALIDATION" "Invalid amount should fail"
code=$(request "POST" "$BASE_URL/api/records" "{\"amount\":-10,\"type\":\"EXPENSE\",\"category\":\"Food\",\"date\":\"2026-04-02\",\"notes\":\"Bad\"}" "$ADMIN_TOKEN")
assert_status "$code" "400" "Negative amount should return 400"

print_test "FILTERS" "Category and type filters"
code=$(request "GET" "$BASE_URL/api/records?category=Salary" "" "$ANALYST_TOKEN")
assert_status "$code" "200" "Category filter should work"
count="$(echo "$RESPONSE_BODY" | jq 'length')"
if [[ "$count" -lt 1 ]]; then
  echo "FAIL: Category filter returned empty data unexpectedly"
  exit 1
fi
code=$(request "GET" "$BASE_URL/api/records?type=INCOME" "" "$ANALYST_TOKEN")
assert_status "$code" "200" "Type filter should work"

print_test "DASHBOARD" "Summary endpoints visibility"
code=$(request "GET" "$BASE_URL/api/dashboard/summary" "" "$VIEWER_TOKEN")
assert_status "$code" "200" "Viewer can read summary"
code=$(request "GET" "$BASE_URL/api/dashboard/categories" "" "$ANALYST_TOKEN")
assert_status "$code" "200" "Analyst can read category totals"
code=$(request "GET" "$BASE_URL/api/dashboard/trends" "" "$ADMIN_TOKEN")
assert_status "$code" "200" "Admin can read trends"
code=$(request "GET" "$BASE_URL/api/dashboard/recent" "" "$ADMIN_TOKEN")
assert_status "$code" "200" "Admin can read recent records"

print_test "RECORDS" "Delete record"
code=$(request "DELETE" "$BASE_URL/api/records/$RECORD_ID" "" "$ADMIN_TOKEN")
assert_status "$code" "200" "Admin can delete record"

print_test "SECURITY" "No token should fail"
code=$(request "GET" "$BASE_URL/api/dashboard/summary")
assert_status "$code" "401" "No token should return 401"

echo ""
echo "----------------------------------------"
echo "API TEST RESULT: $PASS / $TOTAL checks passed"
echo "All core assignment scenarios are working."
echo "----------------------------------------"
