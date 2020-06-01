package org.example.search.service;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;

@Service
public class LoadService {
    public String getDocumentText(String fileName) {
        try (FileInputStream fis = new FileInputStream(Paths.get(fileName).toFile())) {
            HWPFDocument document = new HWPFDocument(fis);
            WordExtractor extractor = new WordExtractor(document);
            return extractor.getText();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
