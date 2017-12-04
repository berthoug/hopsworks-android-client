package io.hops.android.streams.hopsworks;


import java.util.List;

public class SchemaDTO {


    private String name;
    private String contents;
    private int version;
    private List<Integer> versions;

    public SchemaDTO() {
    }

    public SchemaDTO(String name, String contents, int version) {
        this.name = name;
        this.contents = contents;
        this.version = version;
    }

    public SchemaDTO(String name, List<Integer> versions) {
        this.name = name;
        this.versions = versions;
    }

    public SchemaDTO(String name, int version) {
        this.name = name;
        this.version = version;
    }

    public SchemaDTO(String contents) {
        this.contents = contents;
    }

    public String getContents() {
        return this.contents;
    }

    public String getName() {
        return this.name;
    }

    public int getVersion() {
        return this.version;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public List<Integer> getVersions() {
        return this.versions;
    }

    public void setVersions(List<Integer> versions) {
        this.versions = versions;
    }
}
