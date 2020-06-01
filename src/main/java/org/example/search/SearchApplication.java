package org.example.search;

import org.example.search.model.DocumentDto;
import org.example.search.service.ElasticService;
import org.example.search.service.LoadService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootApplication
public class SearchApplication implements CommandLineRunner {
	private final ElasticService elasticService;
	private final LoadService loadService;

	public SearchApplication(ElasticService elasticService,
							 LoadService loadService) {
		this.elasticService = elasticService;
		this.loadService = loadService;
	}

	public static void main(String[] args) {
		SpringApplication.run(SearchApplication.class, args);
	}

	private void loadDocuments(String path) throws IOException {
		System.out.printf("Try to load documents from the path '%s'%n", path);
		if (!Files.isDirectory(Paths.get(path))) {
			System.err.printf("Bad path '%s'%n", path);
			System.exit(-1);
		}
		Files.walk(Paths.get(path))
				.filter(Files::isRegularFile)
				.forEach(file -> {
					final String fileName = file.toAbsolutePath().toString();
					final DocumentDto document = elasticService
							.addDocument(fileName, loadService.getDocumentText(fileName));
					System.out.printf("Success: %s%n", document);
				});
		System.out.println("Success");
	}

	private void searchDocuments(String query) {
		System.out.printf("Try to search documents based on the query '%s'%n", query);
		elasticService.searchDocuments(query).forEach(doc ->
				System.out.printf("Path %s; score %s %n", doc.getPath(), doc.getScore()));
	}

	private void clearDocuments() {
		System.out.println("Try to clear ES");
		System.out.printf("%s documents was deleted%n", elasticService.clear());
	}

	private void printHelp() {
		System.out.println("Arguments:");
		System.out.printf("\t\thelp -- show this message%n");
		System.out.printf("\t\tload=<path> -- load documents from a path%n");
		System.out.printf("\t\tsearch=<query> -- search documents based on a query%n");
		System.out.printf("\t\tclear -- remove all documents from ES%n");
	}

	@Override
	public void run(String... args) {
		System.out.println("Hello!");
		if (args.length != 1) {
			System.err.println("You must specify one argument");
			System.exit(-1);
		}
		try {
			final String[] arg = args[0].split("=");
			switch (arg[0]) {
				case "load":
					loadDocuments(arg[1]);
					break;
				case "search":
					searchDocuments(arg[1]);
					break;
				case "clear":
					clearDocuments();
					break;
				default:
					printHelp();
			}
			System.exit(0);
		} catch (Exception e) {
			System.err.printf("Some error: %s%n", e.getMessage());
			e.printStackTrace();
			System.exit(-1);
		}
	}
}
