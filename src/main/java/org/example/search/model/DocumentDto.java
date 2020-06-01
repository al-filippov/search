package org.example.search.model;

public class DocumentDto {
    private String path;
    private String text;
    private float score;

    public DocumentDto(ElasticDocument elasticDocument) {
        this(elasticDocument, 0f);
    }

    public DocumentDto(ElasticDocument elasticDocument, float score) {
        this.path = elasticDocument.getPath();
        this.text = elasticDocument.getText();
        this.score = score;
    }

    public String getPath() {
        return path;
    }

    public String getText() {
        return text;
    }

    public float getScore() {
        return score;
    }

    @Override
    public String toString() {
        return getPath();
    }
}
