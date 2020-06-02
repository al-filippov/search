package org.example.search.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

@Document(indexName = "document-index")
@Setting(settingPath = "/elasticsearch-settings.json")
public class ElasticDocument {
    @Id
    private String path;
    @Field(type = FieldType.Text, analyzer = "myanalyzer", searchAnalyzer = "myanalyzer")
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
