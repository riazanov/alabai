#!/bin/bash

# To use this script, select names of appropriate problems 
# into CNF_SAT, FOF_SAT and FOF_CSA.

for prob in ~/TPTP-v6.2.0/Problems/*/*.p;
  do
    {
      short=`basename ${prob}`

      if grep -sq ${short} CNF_SAT FOF_SAT FOF_CSA
        then
          {
            echo ${short}  
            java -enableassertions  -Xss10m  -Xss10m -jar ./target/alabai-2.2-SNAPSHOT-jar-with-dependencies.jar -I ~/TPTP-v6.2.0/ -c config.xml -p logic.is.power.alabai.sample_plugins.SamplePlugIn1 --print-input false --max-num-of-terminal-clauses 1  ${prob}      
          }
        fi

    }
  done
