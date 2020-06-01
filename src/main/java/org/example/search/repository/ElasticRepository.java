package org.example.search.repository;

import org.example.search.model.ElasticDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElasticRepository extends ElasticsearchRepository<ElasticDocument, String> {
    ElasticDocument findByPath(String path);
}
