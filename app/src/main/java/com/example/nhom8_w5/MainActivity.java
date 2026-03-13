package com.example.nhom8_w5;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * View + thin Controller glue (MVC):
 *  - View: hiển thị danh sách, thống kê, thanh tìm kiếm, chip lọc.
 *  - Mọi business logic đều đi qua RoomController.
 */
public class MainActivity extends AppCompatActivity implements RoomAdapter.OnItemClickListener {

    // -------------------- Views --------------------
    private RecyclerView rvRooms;
    private ExtendedFloatingActionButton fabAdd;
    private SearchView searchView;
    private ChipGroup chipGroupFilter;
    private Chip chipAll, chipAvailable, chipRented;
    private TextView tvTotalRooms, tvAvailableCount, tvRentedCount, tvRevenue;
    private TextView tvEmptyState;

    // -------------------- MVC --------------------
    private RoomController controller;   // Controller
    private RoomAdapter adapter;         // View (Adapter)

    // -------------------- Misc --------------------
    private boolean isDarkMode = false;
    private final DecimalFormat decimalFormat = new DecimalFormat("###,###,###");

    // ==================== Lifecycle ====================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Load theme trước khi setContentView
        SharedPreferences prefs = getSharedPreferences("theme_prefs", MODE_PRIVATE);
        isDarkMode = prefs.getBoolean("is_dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
                isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initViews();
        initController(savedInstanceState);
        setupRecyclerView();
        setupChipFilter();
        setupSearch();
        updateStats();

        fabAdd.setOnClickListener(v -> showRoomDialog(null));

        rvRooms.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) fabAdd.shrink();
                else fabAdd.extend();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Lưu danh sách gốc (chưa lọc)
        outState.putSerializable("room_list", (ArrayList<Room>) controller.getRawList());
    }

    // ==================== Init ====================

    private void initViews() {
        rvRooms        = findViewById(R.id.rvRooms);
        fabAdd         = findViewById(R.id.fabAdd);
        searchView     = findViewById(R.id.searchView);
        chipGroupFilter= findViewById(R.id.chipGroupFilter);
        chipAll        = findViewById(R.id.chipAll);
        chipAvailable  = findViewById(R.id.chipAvailable);
        chipRented     = findViewById(R.id.chipRented);
        tvTotalRooms   = findViewById(R.id.tvTotalRooms);
        tvAvailableCount = findViewById(R.id.tvAvailableCount);
        tvRentedCount  = findViewById(R.id.tvRentedCount);
        tvRevenue      = findViewById(R.id.tvRevenue);
        tvEmptyState   = findViewById(R.id.tvEmptyState);
    }

    @SuppressWarnings("unchecked")
    private void initController(Bundle savedInstanceState) {
        RoomRepository repository;
        if (savedInstanceState != null) {
            ArrayList<Room> saved = (ArrayList<Room>) savedInstanceState.getSerializable("room_list");
            repository = new RoomRepository(saved);
        } else {
            repository = new RoomRepository();
        }
        controller = new RoomController(repository);
    }

    private void setupRecyclerView() {
        adapter = new RoomAdapter(controller.getDisplayList(), this);
        rvRooms.setLayoutManager(new LinearLayoutManager(this));
        rvRooms.setAdapter(adapter);
        updateEmptyState();
    }

    // ==================== Search ====================

    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                controller.search(query);
                adapter.notifyDataSetChanged();
                updateEmptyState();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                controller.search(newText);
                adapter.notifyDataSetChanged();
                updateEmptyState();
                return true;
            }
        });

        searchView.setOnCloseListener(() -> {
            controller.search("");
            adapter.notifyDataSetChanged();
            updateEmptyState();
            return false;
        });
    }

    // ==================== Chip Filter ====================

    private void setupChipFilter() {
        chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chipAll) {
                controller.setFilter(RoomController.FilterOption.ALL);
            } else if (id == R.id.chipAvailable) {
                controller.setFilter(RoomController.FilterOption.AVAILABLE);
            } else if (id == R.id.chipRented) {
                controller.setFilter(RoomController.FilterOption.RENTED);
            }
            adapter.notifyDataSetChanged();
            updateEmptyState();
        });
    }

    // ==================== Stats ====================

    private void updateStats() {
        tvTotalRooms.setText(String.valueOf(controller.getTotalRooms()));
        tvAvailableCount.setText(String.valueOf(controller.getAvailableCount()));
        tvRentedCount.setText(String.valueOf(controller.getRentedCount()));
        String revenue = decimalFormat.format(controller.getTotalRevenue()).replace(',', '.') + " VNĐ";
        tvRevenue.setText(revenue);
    }

    private void updateEmptyState() {
        if (controller.getDisplayList().isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            rvRooms.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvRooms.setVisibility(View.VISIBLE);
        }
    }

    // ==================== Menu ====================

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem item = menu.findItem(R.id.action_theme);
        if (item != null) {
            item.setIcon(isDarkMode ? android.R.drawable.ic_menu_day : android.R.drawable.ic_menu_compass);
            item.setTitle(isDarkMode ? "Chế độ sáng" : "Chế độ tối");
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_theme) {
            toggleTheme();
            return true;
        } else if (itemId == R.id.action_sort_name_asc) {
            controller.setSort(RoomController.SortOption.NAME_ASC);
            adapter.notifyDataSetChanged();
            return true;
        } else if (itemId == R.id.action_sort_name_desc) {
            controller.setSort(RoomController.SortOption.NAME_DESC);
            adapter.notifyDataSetChanged();
            return true;
        } else if (itemId == R.id.action_sort_price_asc) {
            controller.setSort(RoomController.SortOption.PRICE_ASC);
            adapter.notifyDataSetChanged();
            return true;
        } else if (itemId == R.id.action_sort_price_desc) {
            controller.setSort(RoomController.SortOption.PRICE_DESC);
            adapter.notifyDataSetChanged();
            return true;
        } else if (itemId == R.id.action_sort_default) {
            controller.setSort(RoomController.SortOption.DEFAULT);
            adapter.notifyDataSetChanged();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void toggleTheme() {
        isDarkMode = !isDarkMode;
        SharedPreferences.Editor editor = getSharedPreferences("theme_prefs", MODE_PRIVATE).edit();
        editor.putBoolean("is_dark_mode", isDarkMode);
        editor.apply();
        AppCompatDelegate.setDefaultNightMode(
                isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    // ==================== Dialog Thêm / Sửa Phòng ====================

    private void showRoomDialog(Room existingRoom) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_room, null);
        AlertDialog dialog = new MaterialAlertDialogBuilder(this).setView(view).create();

        // Bind views
        TextView tvTitle          = view.findViewById(R.id.tvDialogTitle);
        TextInputEditText etId    = view.findViewById(R.id.etRoomId);
        TextInputEditText etName  = view.findViewById(R.id.etRoomName);
        TextInputEditText etPrice = view.findViewById(R.id.etPrice);
        MaterialSwitch swIsRented = view.findViewById(R.id.swIsRented);
        TextInputEditText etRenter= view.findViewById(R.id.etRenterName);
        TextInputEditText etPhone = view.findViewById(R.id.etPhone);
        LinearLayout llRenterFields = view.findViewById(R.id.llRenterFields);
        Button btnSave            = view.findViewById(R.id.btnSave);
        Button btnCancel          = view.findViewById(R.id.btnCancel);

        TextInputLayout tilId    = (TextInputLayout) etId.getParent().getParent();
        TextInputLayout tilName  = (TextInputLayout) etName.getParent().getParent();
        TextInputLayout tilPrice = (TextInputLayout) etPrice.getParent().getParent();
        TextInputLayout tilRenter= (TextInputLayout) etRenter.getParent().getParent();
        TextInputLayout tilPhone = (TextInputLayout) etPhone.getParent().getParent();

        // Format giá tiền khi nhập
        etPrice.addTextChangedListener(new PriceTextWatcher(etPrice, decimalFormat));

        swIsRented.setOnCheckedChangeListener((btn, checked) ->
                llRenterFields.setVisibility(checked ? View.VISIBLE : View.GONE)
        );

        boolean isEdit = (existingRoom != null);
        if (isEdit) {
            tvTitle.setText("Chỉnh sửa phòng");
            etId.setText(existingRoom.getId());
            etId.setEnabled(false);
            etName.setText(existingRoom.getName());
            etPrice.setText(decimalFormat.format(existingRoom.getPrice()).replace(',', '.'));
            swIsRented.setChecked(existingRoom.isRented());
            etRenter.setText(existingRoom.getRenterName());
            etPhone.setText(existingRoom.getPhoneNumber());
            llRenterFields.setVisibility(existingRoom.isRented() ? View.VISIBLE : View.GONE);
        } else {
            tvTitle.setText("Thêm phòng mới");
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String id      = etId.getText().toString().trim();
            String name    = etName.getText().toString().trim();
            String priceStr= etPrice.getText().toString().trim().replace(".", "");
            boolean rented = swIsRented.isChecked();
            String renter  = etRenter.getText().toString().trim();
            String phone   = etPhone.getText().toString().trim();

            boolean hasError = false;

            // Validate ID
            if (TextUtils.isEmpty(id)) {
                tilId.setError("Vui lòng nhập mã phòng");
                hasError = true;
            } else if (!isEdit && controller.isIdExists(id)) {
                tilId.setError("Mã phòng đã tồn tại");
                hasError = true;
            } else {
                tilId.setError(null);
            }

            // Validate Name
            if (TextUtils.isEmpty(name)) {
                tilName.setError("Vui lòng nhập tên phòng");
                hasError = true;
            } else {
                tilName.setError(null);
            }

            // Validate Price
            if (TextUtils.isEmpty(priceStr)) {
                tilPrice.setError("Vui lòng nhập giá phòng");
                hasError = true;
            } else {
                tilPrice.setError(null);
            }

            // Validate Renter Info
            if (rented) {
                if (TextUtils.isEmpty(renter)) {
                    tilRenter.setError("Vui lòng nhập tên người thuê");
                    hasError = true;
                } else {
                    tilRenter.setError(null);
                }
                if (TextUtils.isEmpty(phone)) {
                    tilPhone.setError("Vui lòng nhập SĐT");
                    hasError = true;
                } else if (phone.length() < 10 || phone.length() > 11) {
                    tilPhone.setError("SĐT không hợp lệ (10-11 số)");
                    hasError = true;
                } else {
                    tilPhone.setError(null);
                }
            } else {
                tilRenter.setError(null);
                tilPhone.setError(null);
            }

            if (hasError) return;

            try {
                double price = Double.parseDouble(priceStr);

                if (isEdit) {
                    // Cập nhật đối tượng rồi thông báo Controller
                    existingRoom.setName(name);
                    existingRoom.setPrice(price);
                    existingRoom.setRented(rented);
                    existingRoom.setRenterName(rented ? renter : "");
                    existingRoom.setPhoneNumber(rented ? phone : "");
                    controller.updateRoom(existingRoom);
                    Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                } else {
                    Room newRoom = new Room(id, name, price, rented, rented ? renter : "", rented ? phone : "");
                    controller.addRoom(newRoom);
                    Toast.makeText(this, "Thêm phòng thành công!", Toast.LENGTH_SHORT).show();
                }

                adapter.notifyDataSetChanged();
                updateStats();
                updateEmptyState();
                dialog.dismiss();

            } catch (NumberFormatException e) {
                tilPrice.setError("Giá phòng không hợp lệ");
            }
        });

        if (dialog.getWindow() != null) {
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        dialog.show();
    }

    // ==================== OnItemClickListener ====================

    @Override
    public void onItemClick(Room room, int position) {
        showRoomDialog(room);
    }

    @Override
    public void onItemLongClick(Room room, int position) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc muốn xóa \"" + room.getName() + "\" khỏi danh sách?")
                .setPositiveButton("Xóa ngay", (dlg, which) -> {
                    controller.deleteRoom(room);
                    adapter.notifyDataSetChanged();
                    updateStats();
                    updateEmptyState();
                    Toast.makeText(this, "Đã xóa phòng", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onCallClick(String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber)) {
            Toast.makeText(this, "Số điện thoại không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        callIntent.setData(Uri.parse("tel:" + phoneNumber));
        startActivity(callIntent);
    }

    // ==================== Inner class: PriceTextWatcher ====================

    /**
     * TextWatcher dùng để định dạng giá tiền khi người dùng nhập.
     * Tách ra để MainActivity gọn hơn.
     */
    private static class PriceTextWatcher implements TextWatcher {
        private String current = "";
        private final TextInputEditText editText;
        private final DecimalFormat fmt;

        PriceTextWatcher(TextInputEditText editText, DecimalFormat fmt) {
            this.editText = editText;
            this.fmt = fmt;
        }

        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void afterTextChanged(Editable s) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.toString().equals(current)) return;
            editText.removeTextChangedListener(this);
            String clean = s.toString().replaceAll("[^\\d]", "");
            if (!clean.isEmpty()) {
                try {
                    double parsed = Double.parseDouble(clean);
                    String formatted = fmt.format(parsed).replace(',', '.');
                    current = formatted;
                    editText.setText(formatted);
                    editText.setSelection(formatted.length());
                } catch (NumberFormatException ignored) {}
            } else {
                current = "";
                editText.setText("");
            }
            editText.addTextChangedListener(this);
        }
    }
}
