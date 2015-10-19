#!/bin/bash

# To use this script, copy SIM problems without equality 
# to directory ~/SIM_NO_EQ

for prob in ~/SIM_NO_EQ/SIM*.p;
  do
    {
      echo "------------- "${prob}  
      java -enableassertions -Xss300m -jar ./target/alabai-2.2-SNAPSHOT-jar-with-dependencies.jar -t 4 -c config.xml -p logic.is.power.alabai.sample_plugins.SamplePlugIn1 --print-input true --max-num-of-terminal-clauses 1  ${prob}   
    }
  done



