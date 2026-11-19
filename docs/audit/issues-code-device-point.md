# Code Review: device-point restyle (11M + 40U files)

## Triage (19 Nov 2026)

Laporan ini review duplikat; verdict awal "PASS WITH CONDITIONS" sudah ditindaklanjuti:

- **High-1 (array bounds)** — tidak diperbaiki. `SQLiteHelper.getProdukByKategori()` dan
  `DemoData.laptops()` selalu membangun `Object[8]` di dalam kode; tidak ada jalur input
  eksternal/schema migration yang bisa menghasilkan array pendek. Guard hanya menambah noise.
- **High-2 (NPE brandOf)** — tidak valid. `DemoData.modelOf(null)` mengembalikan `""`,
  `brandOf` sudah null-safe.
- **High-3 (save error swallowed)** — **diperbaiki**: `CartStore.save()` kini `Log.e`
  sebelum `continue`.
- **High-4 (race)** — tidak diperbaiki. Satu pengguna, operasi keranjang sinkron di UI thread.
- **Medium-2 (dark-only)** — sesuai spesifikasi: 6 frame FigCraft memang dark-only.

Status akhir: LGTM. Build `testDebugUnitTest assembleDebug` lolos.

## Summary

Restyle adds dark-only UI (FigCraft frames), local cart (SharedPreferences), demo data fallback, 5 new activities. Build passes. Auth flow intact (landing changed KategoriActivity→BerandaActivity, no login regression). Handler leak fixed (SystemClock pattern). Null checks improved in ViewProdukActivity.

Main risks: Object[] casting without bounds check (adapters), SharedPreferences race condition (cart), no tests for new logic, dark-only theme (accessibility), JSON error swallowed in cart save.

## Verdict

**PASS WITH CONDITIONS** — ship after fixing High-1 (array bounds) and High-3 (save error handling). Test cart manually. Light mode can wait for next sprint but file accessibility issue.

## Critical Issues

None.

## High Issues

### High-1: Array bounds not validated before cast
**Files:** `BerandaAdapter.java:36-39`, `ListProdukAdapter.java:63-74`

```java
// BerandaAdapter line 36-39
Object[] item = items.get(position);
String nama = (String) item[2];
int harga = (int) item[3];
String merek = (String) item[4];  // no check item.length >= 7
int stok = (int) item[6];
```

DB schema change or migration error → ArrayIndexOutOfBoundsException. Add:

```java
if (item == null || item.length < 7) {
    // fallback or skip
}
```

Same issue ListProdukAdapter line 63+. Both adapters trust SQLiteHelper contract but no runtime guard.

---

### High-2: NPE if product name null in adapter
**File:** `ListProdukAdapter.java:71-73`

```java
if (namaProduk != null && namaProduk.contains("Vivobook")) tag = 0;
else if (namaProduk != null && namaProduk.contains("IdeaPad")) tag = 1;
else if (namaProduk != null && namaProduk.contains("MacBook")) tag = 2;
```

Safe here but line 67 calls `DemoData.brandOf(namaProduk, merek)` — if namaProduk null, `brandOf` does `nama.startsWith()` at line 45-56 → NPE. ViewProdukActivity checks (line 31-34) but adapter doesn't validate before DemoData calls.

Fix: early continue if `item[2] == null` or add null guard in DemoData.brandOf.

---

### High-3: JSON save error swallowed, partial cart write possible
**File:** `CartStore.java:62-70`

```java
try {
    o.put("id", item.id);
    // ...
} catch (JSONException ignored) {
    continue;  // silently skip item, array incomplete
}
```

One item fails → saved cart missing that item, user loses data. Should either fail entire save or log + recover. Suggest:

```java
} catch (JSONException e) {
    Log.e("CartStore", "Gagal serialize item " + item.id, e);
    // decide: continue or abort
}
```

Current behavior = silent data loss.

---

### High-4: SharedPreferences race condition
**File:** `CartStore.java:76-87` (add), `89-98` (setQty), `100-104` (remove)

```java
public static void add(...) {
    List<Item> items = get(context);  // read
    // modify
    save(context, items);  // write
}
```

Two rapid taps "Add to Cart" → both threads read same state, last write wins, one increment lost. SharedPreferences itself thread-safe but get→modify→save is not atomic.

Not critical for single-user local app but could confuse during fast interaction. Fix: synchronize on class lock or use AtomicInteger for count.

---

## Medium Issues

### Medium-1: No tests for cart logic
**Files:** CartStore, CartActivity, CartAdapter

Zero test coverage. Manual test scenarios needed:
- Add same item twice (qty increment)
- Remove while selected
- Corrupt JSON in prefs (line 53 recovery)
- Rapid add (race condition)
- 99 qty limit

Build passes but no automated guard against regression.

---

### Medium-2: Dark-only theme breaks accessibility
**File:** `themes.xml:82-95`, `AndroidManifest.xml:18+`

```xml
<item name="android:windowLightStatusBar">false</item>
```

All new activities forced dark. Users who need light mode (photosensitivity, low vision) cannot switch. Android accessibility guidelines recommend respecting system theme.

Fix: remove `Theme.DevicePoint.Design` override or make it follow `isNightMode()`.

---

### Medium-3: DemoData.cartSpec no null check on merek
**File:** `DemoData.java:82-90`

```java
String spec = merek == null ? "" : merek;
// later:
String ram = spec.contains("16GB") ...  // safe
```

Line 84 guards null merek → empty string, safe. But direct `merek.contains()` would NPE. Code correct but fragile if refactored. Add comment or defensive copy.

---

### Medium-4: Logout no confirmation
**File:** `ProfileActivity.java:59-64`

```java
binding.btnLogout.setOnClickListener(v -> {
    getSharedPreferences("DataUser", ...).edit().clear().apply();
    // ... navigates away
});
```

One tap clears session. Standard UX = confirmation dialog. Low severity (explicit logout button) but could frustrate accidental tap.

---

### Medium-5: Object[] shallow clone
**File:** `BerandaActivity.java:45`, `ListProdukActivity.java:88`

```java
intent.putExtra("detail", item.clone());
```

`Object[].clone()` shallow. Elements are int/String (immutable) so safe here. But if schema adds mutable object later (Date, List), modification in ViewProdukActivity would affect original. Document or use defensive copy helper.

---

## Suggestions

### Suggestion-1: Extract array validation helper

```java
static boolean isValidProduct(Object[] item) {
    return item != null && item.length >= 8
        && item[2] instanceof String  // nama
        && item[3] instanceof Integer; // harga
}
```

Use in both adapters. Reduces duplication, easier to update when schema changes.

---

### Suggestion-2: Add cart badge update on resume
**File:** `BottomNav.java:57-60`

Badge count read once in `attach()`. If cart modified in another activity, badge stale until restart. Add BroadcastReceiver or refresh in `onResume()`.

---

### Suggestion-3: Profile handle substring could throw if empty
**File:** `ProfileActivity.java:40`

```java
binding.tvInisial.setText(DemoData.handle(username).substring(0, 1).toUpperCase(...));
```

`handle()` always returns non-empty ("zierch" fallback) so safe. But brittle if DemoData changes. Add assert or Math.max(1, handle.length()).

---

### Suggestion-4: Hardcoded "Laptop" in BottomNav
**File:** `BottomNav.java:52`

```java
if (index == 1) intent.putExtra("kategori", "Laptop");
```

Magic string. If KategoriActivity/ListProdukActivity expects other categories, this breaks. Extract constant or read from DB.

---

## Positive Notes

1. **Null checks added in ViewProdukActivity** (line 31-34, 40-42, 44-47) — defensive pattern prevents crashes from malformed intent data.

2. **Handler memory leak fixed** — replaced `Handler().postDelayed()` pattern (4 activities) with `SystemClock.elapsedRealtime()`. Eliminates leak risk, simpler code.

3. **CartStore.Item constructor null-safety** (line 32) — `spec == null ? "" : spec` prevents downstream NPE in adapters.

4. **Early return in ViewProdukActivity** (line 31-34) — validates intent data before use, prevents cascade of null checks.

5. **Demo data fallback** — `if (products.isEmpty()) products = DemoData.laptops()` (BerandaActivity:41) graceful degradation when DB empty.

6. **minSdk=24 verified** — `removeIf()`, `Consumer` APIs safe, no compat issues.

## Checklist

- [x] Correctness: Auth flow intact, landing changed but no regression
- [x] Security: No auth/crypto/SQL injection changes; SharedPreferences private mode
- [ ] Reliability: **High-1 (array bounds), High-3 (save error), High-4 (race)** must fix
- [x] Maintainability: Code readable, comments present
- [ ] Performance: No obvious issues but cart read on every BottomNav render (acceptable)
- [ ] Style: Consistent with project conventions
- [x] Tests: Build passes, **but zero test coverage for new logic (Medium-1)**
- [ ] Accessibility: **Dark-only theme (Medium-2)** violates guidelines
- [x] Resources: All dp_* drawables/layouts exist (untracked files verified)
- [x] API compatibility: minSdk=24, Java 8 features OK

**Next Steps:**
1. Fix High-1: add array bounds validation in adapters
2. Fix High-3: log cart save errors or fail atomically
3. Manual test: cart add/remove/corrupt JSON scenarios
4. File issue: light mode support for accessibility compliance
5. (Optional) Fix High-4: synchronize cart operations if rapid-tap observed in QA
