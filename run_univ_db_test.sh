

for prob in ./Test/MyUniversityDB/lubm-style.query*.tptp;
  do
    {
	java -enableassertions -Xss300m -jar ./target/alabai-2.2-SNAPSHOT-jar-with-dependencies.jar -I ./Test/MyUniversityDB -c univ_db_query_answering_config.xml --print-index-performance-summary true $prob
    }
 done