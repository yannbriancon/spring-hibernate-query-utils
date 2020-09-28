package com.yannbriancon.interceptor;

class SelectQueriesInfo {
    private final String initialSelectQuery;

    private Integer selectQueriesCount = 1;

    public SelectQueriesInfo(String initialSelectQuery) {
        this.initialSelectQuery = initialSelectQuery;
    }

    public String getInitialSelectQuery() {
        return initialSelectQuery;
    }

    public Integer getSelectQueriesCount() {
        return selectQueriesCount;
    }

    public void setSelectQueriesCount(Integer selectQueriesCount) {
        this.selectQueriesCount = selectQueriesCount;
    }

    SelectQueriesInfo incrementSelectQueriesCount() {
        selectQueriesCount = selectQueriesCount + 1;
        return this;
    }

    SelectQueriesInfo resetSelectQueriesCount() {
        selectQueriesCount = 1;
        return this;
    }
}
