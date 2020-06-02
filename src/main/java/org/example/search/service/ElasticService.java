package org.example.search.service;

import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.ScoreSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.example.search.model.DocumentDto;
import org.example.search.model.ElasticDocument;
import org.example.search.repository.ElasticRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHitsIterator;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

@Service
public class ElasticService {
    private final ElasticRepository elasticRepository;
    private final ElasticsearchOperations operations;

    public ElasticService(ElasticRepository elasticRepository,
                          ElasticsearchOperations operations) {
        this.elasticRepository = elasticRepository;
        this.operations = operations;
    }

    public DocumentDto addDocument(String path, String text) {
        if (elasticRepository.findByPath(path) != null) {
            throw new IllegalArgumentException(
                    String.format("The document with path '%s' is already exists", path));
        }
        final ElasticDocument document = new ElasticDocument(path, text);
        return new DocumentDto(elasticRepository.save(document));
    }

    public DocumentDto removeDocument(String path) {
        final ElasticDocument elasticDocument = elasticRepository.findByPath(path);
        if (elasticDocument == null) {
            throw new IllegalArgumentException(
                    String.format("The document with path '%s' is not exists", path));
        }
        elasticRepository.delete(elasticDocument);
        return new DocumentDto(elasticDocument);
    }

    public List<DocumentDto> searchDocuments(String textQuery) {
        final QueryStringQueryBuilder query = queryStringQuery(textQuery);
        query.field("text");
        final NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withFields()
                .withQuery(query)
                .withHighlightFields(new HighlightBuilder.Field("*"))
                .withSort(new ScoreSortBuilder().order(SortOrder.DESC))
                .withPageable(PageRequest.of(0, 10))
                .build();
        final SearchHitsIterator<ElasticDocument> stream = operations
                .searchForStream(searchQuery, ElasticDocument.class);
        final List<DocumentDto> documents = new ArrayList<>();
        while (stream.hasNext()) {
            final var searchHit = stream.next();
            documents.add(new DocumentDto(searchHit.getContent(),
                    searchHit.getHighlightField("text").stream()
                            .map(str -> str.replaceAll("\n", "\n\t"))
                            .collect(Collectors.joining("\n\t")),
                    searchHit.getScore()));
        }
        return documents;
    }

    public long getDocumentsCount() {
        return elasticRepository.count();
    }

    public long clear() {
        final long total = getDocumentsCount();
        elasticRepository.deleteAll();
        return total;
    }
}
