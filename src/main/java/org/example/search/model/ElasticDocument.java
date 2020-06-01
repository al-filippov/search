package org.example.search.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Mapping;
import org.springframework.data.elasticsearch.annotations.Setting;

@Document(indexName = "document-index")
@Setting(settingPath = "/elasticsearch-settings.json")
@Mapping(mappingPath = "/elasticsearch-document-mappings.json")
public class ElasticDocument {
    @Id
    private String path;
    private String text;

    public ElasticDocument(String path, String text) {
        this.path = path;
        this.text = text;
    }

    public String getPath() {
        return path;
    }

    public String getText() {
        return text;
    }
}
