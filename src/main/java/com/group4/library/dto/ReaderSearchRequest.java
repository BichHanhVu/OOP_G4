package com.group4.library.dto;

public class ReaderSearchRequest {
    private final String keyword;
    private final String type;
    private final String sortBy;
    private final String sortDirection;
    private final int page;
    private final int size;

    public ReaderSearchRequest(String keyword, String type, String sortBy, String sortDirection, Integer page, Integer size) {
        this.keyword = keyword;
        this.type = type;
        this.sortBy = normalizeSortBy(sortBy);
        this.sortDirection = normalizeSortDirection(sortDirection);
        this.page = normalizePage(page);
        this.size = normalizeSize(size);
    }

    private String normalizeSortBy(String sortBy) {
        if ("name".equalsIgnoreCase(sortBy)) {
            return "name";
        }
        return "id";
    }

    private String normalizeSortDirection(String sortDirection) {
        if ("desc".equalsIgnoreCase(sortDirection)) {
            return "desc";
        }
        return "asc";
    }

    private int normalizePage(Integer page) {
        if (page == null || page < 0) {
            return 0;
        }
        return page;
    }

    private int normalizeSize(Integer size) {
        if (size == null || size <= 0) {
            return 10;
        }
        if (size > 50) {
            return 50;
        }
        return size;
    }

    public String getKeyword() { return keyword; }
    public String getType() { return type; }
    public String getSortBy() { return sortBy; }
    public String getSortDirection() { return sortDirection; }
    public int getPage() { return page; }
    public int getSize() { return size; }
}