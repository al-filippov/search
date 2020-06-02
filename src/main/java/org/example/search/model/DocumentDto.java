package org.example.search.model;

public class DocumentDto {
    private String path;
    private String text;
    private String textHighlight;
    private float score;

    public DocumentDto(ElasticDocument elasticDocument) {
        this(elasticDocument, null, 0f);
    }

    public DocumentDto(ElasticDocument elasticDocument, String highlight, float score) {
        this.path = elasticDocument.getPath();
        this.text = elasticDocument.getText();
        this.textHighlight = highlight;
        this.score = score;
    }

    public String getPath() {
        return path;
    }

    public String getText() {
        return text;
    }

    public String getTextHighlight() {
        return textHighlight;
    }

    public float getScore() {
        return score;
    }

    @Override
    public String toString() {
        return getPath();
    }
}
