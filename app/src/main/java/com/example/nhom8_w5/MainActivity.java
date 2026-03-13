package com.example.nhom8_w5;

import android.content.SharedPreferences;
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
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements RoomAdapter.OnItemClickListener {

    private RecyclerView rvRooms;
    private ExtendedFloatingActionButton fabAdd;
    private RoomRepository repository;
    private RoomAdapter adapter;
    private boolean isDarkMode = false;
    private final DecimalFormat decimalFormat = new DecimalFormat("###,###,###");

    // Statistics Views
    private TextView tvTotalRevenue, tvTotalRooms, tvAvailableRooms, tvRentedRooms;
    private LinearLayout llEmptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Load theme preference
        SharedPreferences prefs = getSharedPreferences("theme_prefs", MODE_PRIVATE);
        isDarkMode = prefs.getBoolean("is_dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initViews();
        
        if (savedInstanceState != null) {
            ArrayList<Room> savedList = (ArrayList<Room>) savedInstanceState.getSerializable("room_list");
            repository = new RoomRepository(savedList);
        } else {
            repository = new RoomRepository();
        }
        
        setupRecyclerView();
        updateUI();

        fabAdd.setOnClickListener(v -> showRoomDialog(null, -1));
        
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
        outState.putSerializable("room_list", (ArrayList<Room>) repository.getRoomList());
    }

    private void initViews() {
        rvRooms = findViewById(R.id.rvRooms);
        fabAdd = findViewById(R.id.fabAdd);
        
        // Stats views
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
        tvTotalRooms = findViewById(R.id.tvTotalRooms);
        tvAvailableRooms = findViewById(R.id.tvAvailableRooms);
        tvRentedRooms = findViewById(R.id.tvRentedRooms);
        llEmptyState = findViewById(R.id.llEmptyState);
    }

    private void setupRecyclerView() {
        adapter = new RoomAdapter(repository.getRoomList(), this);
        rvRooms.setLayoutManager(new LinearLayoutManager(this));
        rvRooms.setAdapter(adapter);
    }

    private void updateUI() {
        // Update Stats
        tvTotalRevenue.setText(decimalFormat.format(repository.getTotalRevenue()) + " VNĐ");
        tvTotalRooms.setText(String.valueOf(repository.getTotalRooms()));
        tvRentedRooms.setText(String.valueOf(repository.getRentedRooms()));
        tvAvailableRooms.setText(String.valueOf(repository.getTotalRooms() - repository.getRentedRooms()));

        // Update Empty State
        if (repository.getRoomList().isEmpty()) {
            llEmptyState.setVisibility(View.VISIBLE);
            rvRooms.setVisibility(View.GONE);
        } else {
            llEmptyState.setVisibility(View.GONE);
            rvRooms.setVisibility(View.VISIBLE);
        }
    }

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
        if (item.getItemId() == R.id.action_theme) {
            isDarkMode = !isDarkMode;
            SharedPreferences.Editor editor = getSharedPreferences("theme_prefs", MODE_PRIVATE).edit();
            editor.putBoolean("is_dark_mode", isDarkMode);
            editor.apply();

            AppCompatDelegate.setDefaultNightMode(isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showRoomDialog(Room room, int position) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_room, null);
        AlertDialog dialog = new MaterialAlertDialogBuilder(this).setView(view).create();

        TextView tvTitle = view.findViewById(R.id.tvDialogTitle);
        TextInputEditText etId = view.findViewById(R.id.etRoomId);
        TextInputEditText etName = view.findViewById(R.id.etRoomName);
        TextInputEditText etPrice = view.findViewById(R.id.etPrice);
        MaterialSwitch swIsRented = view.findViewById(R.id.swIsRented);
        TextInputEditText etRenter = view.findViewById(R.id.etRenterName);
        TextInputEditText etPhone = view.findViewById(R.id.etPhone);
        LinearLayout llRenterFields = view.findViewById(R.id.llRenterFields);
        Button btnSave = view.findViewById(R.id.btnSave);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        TextInputLayout tilId = (TextInputLayout) etId.getParent().getParent();
        TextInputLayout tilName = (TextInputLayout) etName.getParent().getParent();
        TextInputLayout tilPrice = (TextInputLayout) etPrice.getParent().getParent();
        TextInputLayout tilRenter = (TextInputLayout) etRenter.getParent().getParent();
        TextInputLayout tilPhone = (TextInputLayout) etPhone.getParent().getParent();

        etPrice.addTextChangedListener(new TextWatcher() {
            private String current = "";
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals(current)) {
                    etPrice.removeTextChangedListener(this);
                    String cleanString = s.toString().replaceAll("[^\\d]", "");
                    if (!cleanString.isEmpty()) {
                        try {
                            double parsed = Double.parseDouble(cleanString);
                            String formatted = decimalFormat.format(parsed).replace(',', '.');
                            current = formatted;
                            etPrice.setText(formatted);
                            etPrice.setSelection(formatted.length());
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    } else {
                        current = "";
                        etPrice.setText("");
                    }
                    etPrice.addTextChangedListener(this);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        swIsRented.setOnCheckedChangeListener((buttonView, isChecked) -> {
            llRenterFields.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        boolean isEdit = (room != null);
        if (isEdit) {
            tvTitle.setText("Chỉnh sửa phòng");
            etId.setText(room.getId());
            etId.setEnabled(false);
            etName.setText(room.getName());
            etPrice.setText(decimalFormat.format(room.getPrice()).replace(',', '.'));
            swIsRented.setChecked(room.isRented());
            etRenter.setText(room.getRenterName());
            etPhone.setText(room.getPhoneNumber());
            llRenterFields.setVisibility(room.isRented() ? View.VISIBLE : View.GONE);
        } else {
            tvTitle.setText("Thêm phòng mới");
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String id = etId.getText().toString().trim();
            String name = etName.getText().toString().trim();
            String priceStr = etPrice.getText().toString().trim().replace(".", "");
            boolean isRented = swIsRented.isChecked();
            String renter = etRenter.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();

            boolean hasError = false;
            
            // Validate ID
            if (TextUtils.isEmpty(id)) {
                tilId.setError("Vui lòng nhập mã phòng");
                hasError = true;
            } else if (!isEdit && repository.isIdExists(id)) {
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

            // Validate Renter Info if rented
            if (isRented) {
                if (TextUtils.isEmpty(renter)) {
                    tilRenter.setError("Vui lòng nhập tên khách");
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
                    room.setName(name);
                    room.setPrice(price);
                    room.setRented(isRented);
                    room.setRenterName(isRented ? renter : "");
                    room.setPhoneNumber(isRented ? phone : "");
                    adapter.notifyItemChanged(position);
                    Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                } else {
                    Room newRoom = new Room(id, name, price, isRented, isRented ? renter : "", isRented ? phone : "");
                    repository.addRoom(newRoom);
                    adapter.notifyItemInserted(repository.getRoomList().size() - 1);
                    rvRooms.smoothScrollToPosition(repository.getRoomList().size() - 1);
                    Toast.makeText(this, "Thêm phòng thành công", Toast.LENGTH_SHORT).show();
                }
                updateUI();
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

    @Override
    public void onItemClick(Room room, int position) {
        if (position >= 0 && position < repository.getRoomList().size()) {
            showRoomDialog(room, position);
        }
    }

    @Override
    public void onItemLongClick(Room room, int position) {
        if (position < 0 || position >= repository.getRoomList().size()) return;
        
        new MaterialAlertDialogBuilder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa " + room.getName() + " khỏi danh sách?")
                .setPositiveButton("Xóa ngay", (dialog, which) -> {
                    if (position < repository.getRoomList().size()) {
                        repository.deleteRoom(position);
                        adapter.notifyItemRemoved(position);
                        adapter.notifyItemRangeChanged(position, repository.getRoomList().size());
                        updateUI();
                        Toast.makeText(this, "Đã xóa phòng", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
