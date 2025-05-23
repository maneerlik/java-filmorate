package ru.yandex.practicum.filmorate.model.enumeration;

public enum SearchParameter {

    DIRECTOR("d.name"), TITLE("f.name");

    private String sqlField;

    SearchParameter(String sqlField) {
        this.sqlField = sqlField;
    }

    public String getSqlField() {
        return sqlField;
    }
}
