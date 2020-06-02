Install Elasticsearch:
1. Download https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-oss-7.6.2-windows-x86_64.zip
2. Unzip
3. Run bin\elasticsearch.bat

To build jar file run:
gradlew.bat clean build

To execute jar file run:
java -jar build\libs\search-0.0.1-SNAPSHOT.jar <arg>

To load documents run:
java -jar build\libs\search-0.0.1-SNAPSHOT.jar load="./documents"

To clear ES storage (remove all documents) run:
java -jar build\libs\search-0.0.1-SNAPSHOT.jar clear

To search documents based on the query run:
java -jar build\libs\search-0.0.1-SNAPSHOT.jar search="term1 term2 term3"
