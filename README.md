# Gradient UI – Android Library

**AuroraView** là một `FrameLayout` tự vẽ **viền gradient bo góc** + **nền gradient tuỳ chọn**.
Viền và nền là 2 lớp tách rời, mỗi lớp set hướng gradient riêng được và đổi được lúc chạy — nên
không phải đẻ ra một file drawable cho mỗi tổ hợp màu / mỗi trạng thái item.

Library id: `vn.minh_nguyen.vkey.gradient.ui`

---

## Cài đặt (qua JitPack)

Thêm vào `settings.gradle.kts` (hoặc `build.gradle` project):

```
	dependencyResolutionManagement {
		repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
		repositories {
			google()
			mavenCentral()
			maven { url = uri("https://jitpack.io") }
		}
	}
```

Thêm dependency vào `build.gradle` (module):

```
	dependencies {
	        implementation("com.github.minh-nguyen-2003.Gradient-UI-Android:gradient-ui:1.0.0")
	}
```

Thay `1.0.0` bằng tag release. Coordinate chính xác thì xem log build trên trang JitPack của
repo — module publish với `artifactId = gradient-ui`, còn `groupId`/`version` lấy theo tham số
JitPack truyền vào nên tự khớp với tag, không phải sửa tay trong `build.gradle.kts`.

## Cách sử dụng cơ bản

Khai trong XML, content đặt bên trong như `FrameLayout` thường:

```xml
<vn.minh_nguyen.vkey.gradient.ui.AuroraView
    android:layout_width="match_parent"
    android:layout_height="52dp"
    app:aurora_pill="true">

    <TextView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:gravity="center"
        android:text="Nâng cấp Premium"
        android:textColor="#FFFFFF" />
</vn.minh_nguyen.vkey.gradient.ui.AuroraView>
```

Không khai gì thêm thì mặc định: viền 1dp gradient cam → hồng → tím, bo 12dp, nền trong suốt.

## Thuộc tính XML

| Attribute | Kiểu | Mặc định | Mô tả |
|----------------------------|-----------|--------------------|------------------------------------------------------------------------------------------|
| `aurora_strokeColors` | reference | cam → hồng → tím | Trỏ tới một `<array>` gồm các `@color`. Mảng 1 màu ⇒ viền đơn sắc; mảng rỗng ⇒ không vẽ viền. |
| `aurora_fillColors` | reference | *(không set)* | Mảng màu nền. Không set ⇒ **nền trong suốt**. |
| `aurora_strokeWidth` | dimension | `1dp` | Độ dày viền; tự clamp về nửa cạnh ngắn. |
| `aurora_cornerRadius` | dimension | `12dp` | Bán kính bo; tự clamp về nửa cạnh ngắn. Bị bỏ qua khi bật `aurora_pill`. |
| `aurora_pill` | boolean | `false` | `true`: bán kính = nửa cạnh ngắn ⇒ view vuông ra **hình tròn**, view ngang ra **pill**. |
| `aurora_orientation` | enum | `left_right` | Hướng gradient dùng chung — đóng vai trò default cho cả 2 lớp. |
| `aurora_strokeOrientation` | enum | *(theo `aurora_orientation`)* | Override hướng gradient riêng cho viền. |
| `aurora_fillOrientation` | enum | *(theo `aurora_orientation`)* | Override hướng gradient riêng cho nền. |
| `aurora_clipContent` | boolean | `false` | Bo góc luôn content bên trong (`clipToOutline`) — bật khi bên trong có ảnh tràn viền. |

## Enum hướng gradient

Giá trị khớp hằng số trong `AuroraOrientation` nên đổi bằng XML hay bằng code đều như nhau.

| Enum XML | Hằng số Kotlin | Gradient chạy từ |
|--------------------------|-----------------------------------------------|------------------------|
| `left_right` | `AuroraOrientation.LEFT_RIGHT` | Trái → phải (mặc định) |
| `top_bottom` | `AuroraOrientation.TOP_BOTTOM` | Trên → dưới |
| `top_left_bottom_right` | `AuroraOrientation.TOP_LEFT_BOTTOM_RIGHT` | Góc trên-trái → dưới-phải |
| `top_right_bottom_left` | `AuroraOrientation.TOP_RIGHT_BOTTOM_LEFT` | Góc trên-phải → dưới-trái |

## API trong code

Mỗi attribute đều có một property tương ứng, set xong tự `invalidate()`:

| Property | Kiểu | Ghi chú |
|---------------------|-------------|--------------------------------------------------------------|
| `strokeColors` | `IntArray` | Mảng rỗng ⇒ không vẽ viền. |
| `fillColors` | `IntArray?` | `null` ⇒ nền trong suốt. |
| `strokeWidth` | `Float` | Đơn vị **px**. |
| `cornerRadius` | `Float` | px. |
| `isPill` | `Boolean` | Bật thì `cornerRadius` bị bỏ qua. |
| `strokeOrientation` | `Int` | Nhận hằng số `AuroraOrientation`. |
| `fillOrientation` | `Int` | |
| `isClipContent` | `Boolean` | |

```kotlin
  view.strokeColors = resources.getIntArray(R.array.item_selected_stroke)
  view.strokeWidth = 2 * resources.displayMetrics.density
  view.setOrientation(AuroraOrientation.TOP_BOTTOM)   // set 1 lần cho cả viền lẫn nền
```

Đổi cả bộ trong một lần — tham số nào bỏ trống thì giữ nguyên giá trị hiện tại. Đây là kiểu
dùng chính khi bind item `RecyclerView`:

```kotlin
  fun bind(item: Item) = holder.aurora.applyAurora(
      strokeColors = resources.getIntArray(
          if (item.selected) R.array.item_selected_stroke else R.array.item_idle_stroke
      ),
      fillColors = if (item.selected) resources.getIntArray(R.array.item_selected_fill) else null
  )
```

## Đổi mặc định cho cả app

Khai lại resource **cùng tên** trong app là ghi đè được default của library (resource của app
luôn thắng resource của thư viện):

```xml
<!-- app/src/main/res/values/aurora_overrides.xml -->
<resources>
    <color name="aurora_stroke_start">#42E695</color>
    <color name="aurora_stroke_center">#3BB2B8</color>
    <color name="aurora_stroke_end">#2D6CDF</color>
    <!-- Dự án dùng sdp thì trỏ thẳng sang sdp cho khớp scale màn hình -->
    <dimen name="aurora_corner_radius">@dimen/_12sdp</dimen>
    <dimen name="aurora_stroke_width">@dimen/_1sdp</dimen>
</resources>
```

## Lưu ý

- `aurora_strokeColors` / `aurora_fillColors` nhận **reference tới `<array>`**, không nhận màu
  trực tiếp. Muốn đơn sắc thì khai `<array>` một phần tử — library tự nhân đôi thành gradient
  phẳng chứ không ném `IllegalArgumentException` như `LinearGradient` thuần.
- Viền vẽ **thụt vào nửa độ dày** để mép ngoài trùng mép view, bán kính giảm tương ứng nên hình
  tròn / pill vẫn tròn đều.
- Gradient **không tự đảo chiều theo RTL**.
- Dùng làm nút thì thêm `android:clickable="true"` và
  `android:foreground="?attr/selectableItemBackground"`; bật `aurora_clipContent` thì ripple tự
  bo theo góc.

## Ví dụ hoàn chỉnh

```xml
<!-- Nút CTA: nền gradient ngang, viền sáng chạy dọc -->
<vn.minh_nguyen.vkey.gradient.ui.AuroraView
    android:layout_width="match_parent"
    android:layout_height="52dp"
    app:aurora_fillColors="@array/cta_fill"
    app:aurora_fillOrientation="left_right"
    app:aurora_pill="true"
    app:aurora_strokeColors="@array/cta_stroke"
    app:aurora_strokeOrientation="top_bottom" />

<!-- Thẻ: viền gradient, nền trắng rất mờ, bo góc luôn content -->
<vn.minh_nguyen.vkey.gradient.ui.AuroraView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:aurora_clipContent="true"
    app:aurora_cornerRadius="24dp"
    app:aurora_fillColors="@array/card_fill"
    app:aurora_orientation="top_left_bottom_right"
    app:aurora_strokeColors="@array/card_stroke" />
```

## Cấu trúc project

| Module | Vai trò |
|---------------|--------------------------------------------------------------------------|
| `gradient_ui` | Library — **không phụ thuộc thư viện nào** ngoài kotlin-stdlib, `minSdk 21`. |
| `app` | App demo: pill / CTA / card / 3 trạng thái item + 2 control chỉnh lúc chạy. |

Phần tính toán nằm trong `AuroraGeometry` (thuần Kotlin, không import `android.*`) nên test
được thẳng trên JVM:

```bash
./gradlew :gradient_ui:testDebugUnitTest
```

## License

MIT — xem [LICENSE](LICENSE).
