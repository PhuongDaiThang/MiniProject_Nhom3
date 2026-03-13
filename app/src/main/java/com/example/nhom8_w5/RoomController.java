package com.example.nhom8_w5;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Controller (MVC): Xử lý toàn bộ business logic giữa Model (Room/RoomRepository)
 * và View (MainActivity / RoomAdapter).
 * Các tính năng: tìm kiếm, lọc, sắp xếp, thêm/sửa/xóa phòng.
 */
public class RoomController {

    // -------------------- Sort Options --------------------
    public enum SortOption {
        DEFAULT, NAME_ASC, NAME_DESC, PRICE_ASC, PRICE_DESC
    }

    // -------------------- Filter Options --------------------
    public enum FilterOption {
        ALL, AVAILABLE, RENTED
    }

    // -------------------- Fields --------------------
    private final RoomRepository repository;
    private List<Room> filteredList;  // danh sách hiển thị sau filter/search/sort

    private String currentQuery = "";
    private FilterOption currentFilter = FilterOption.ALL;
    private SortOption currentSort = SortOption.DEFAULT;

    // -------------------- Constructor --------------------
    public RoomController(RoomRepository repository) {
        this.repository = repository;
        this.filteredList = new ArrayList<>(repository.getRoomList());
    }

    // -------------------- Public API --------------------

    /** Trả về danh sách đã lọc/sắp xếp để hiển thị lên RecyclerView */
    public List<Room> getDisplayList() {
        return filteredList;
    }

    /** Thêm phòng mới. Trả về true nếu thành công, false nếu ID đã tồn tại. */
    public boolean addRoom(Room room) {
        if (repository.isIdExists(room.getId())) return false;
        repository.addRoom(room);
        applyFiltersAndSort();
        return true;
    }

    /** Cập nhật phòng theo vị trí trong danh sách gốc (repository). */
    public void updateRoom(Room room) {
        // tìm vị trí thực trong repository
        List<Room> all = repository.getRoomList();
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getId().equals(room.getId())) {
                repository.updateRoom(i, room);
                break;
            }
        }
        applyFiltersAndSort();
    }

    /** Xóa phòng theo đối tượng. */
    public void deleteRoom(Room room) {
        List<Room> all = repository.getRoomList();
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getId().equals(room.getId())) {
                repository.deleteRoom(i);
                break;
            }
        }
        applyFiltersAndSort();
    }

    /** Kiểm tra mã phòng đã tồn tại chưa. */
    public boolean isIdExists(String id) {
        return repository.isIdExists(id);
    }

    // -------------------- Search / Filter / Sort --------------------

    public void search(String query) {
        this.currentQuery = query == null ? "" : query.trim().toLowerCase();
        applyFiltersAndSort();
    }

    public void setFilter(FilterOption filter) {
        this.currentFilter = filter;
        applyFiltersAndSort();
    }

    public void setSort(SortOption sort) {
        this.currentSort = sort;
        applyFiltersAndSort();
    }

    public FilterOption getCurrentFilter() { return currentFilter; }
    public SortOption getCurrentSort() { return currentSort; }

    // -------------------- Statistics --------------------

    public int getTotalRooms() {
        return repository.getRoomList().size();
    }

    public int getAvailableCount() {
        int count = 0;
        for (Room r : repository.getRoomList()) {
            if (!r.isRented()) count++;
        }
        return count;
    }

    public int getRentedCount() {
        int count = 0;
        for (Room r : repository.getRoomList()) {
            if (r.isRented()) count++;
        }
        return count;
    }

    public double getTotalRevenue() {
        double total = 0;
        for (Room r : repository.getRoomList()) {
            if (r.isRented()) total += r.getPrice();
        }
        return total;
    }

    // -------------------- Internal --------------------

    private void applyFiltersAndSort() {
        List<Room> result = new ArrayList<>(repository.getRoomList());

        // 1. Filter theo trạng thái
        if (currentFilter == FilterOption.AVAILABLE) {
            result.removeIf(r -> r.isRented());
        } else if (currentFilter == FilterOption.RENTED) {
            result.removeIf(r -> !r.isRented());
        }

        // 2. Filter theo từ khóa tìm kiếm
        if (!currentQuery.isEmpty()) {
            result.removeIf(r ->
                !r.getName().toLowerCase().contains(currentQuery) &&
                !r.getId().toLowerCase().contains(currentQuery) &&
                !r.getRenterName().toLowerCase().contains(currentQuery)
            );
        }

        // 3. Sort
        switch (currentSort) {
            case NAME_ASC:
                Collections.sort(result, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));
                break;
            case NAME_DESC:
                Collections.sort(result, (a, b) -> b.getName().compareToIgnoreCase(a.getName()));
                break;
            case PRICE_ASC:
                Collections.sort(result, (a, b) -> Double.compare(a.getPrice(), b.getPrice()));
                break;
            case PRICE_DESC:
                Collections.sort(result, (a, b) -> Double.compare(b.getPrice(), a.getPrice()));
                break;
            default:
                break;
        }

        filteredList.clear();
        filteredList.addAll(result);
    }

    /** Lấy danh sách gốc (dùng khi save/restore state) */
    public List<Room> getRawList() {
        return repository.getRoomList();
    }
}
