package io.hops.android.streams.storage;

public abstract class Table {

    protected abstract Column[] getColumns();

    protected abstract String getTableName();

    public String create(){
        String command = "CREATE TABLE " + getTableName() + "(";
        for (Column column: getColumns()){
            command = command + column.name + " " + column.type + ",";
        }
        command = command.substring(0, command.length()-1) + ")";
        return command;
    }

    public String drop(){
        return "DROP TABLE IF EXISTS " + getTableName();
    }

}
