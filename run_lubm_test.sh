

for prob in ./Test/univ-bench.query*.tptp;
  do
    {
       java -enableassertions  -Xss10m -jar ./target/alabai-2.2-SNAPSHOT-jar-with-dependencies.jar -I ./Test -c lubm_query_answering_config.xml $prob
    }
 done