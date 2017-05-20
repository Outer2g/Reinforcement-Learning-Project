#!/bin/bash


BASEDIR=$(dirname "$0")

echo "Cleaning maven..."
sudo mvn clean
echo "Done."

echo "Packaging .jar file..."
sudo mvn package
echo "done!"

echo "Copying jar file..."

sudo cp ${BASEDIR}/target/AgentDeep-1.0-SNAPSHOT-jar-with-dependencies.jar ${BASEDIR}/../../../fightingGame/FTG3.10/FightingICEver.3.10/data/ai/AgentDeep.jar
sudo cp ${BASEDIR}/target/AgentDeep-1.0-SNAPSHOT-jar-with-dependencies.jar ~/workspace/FightingICE/data/ai/AgentDeep.jar

echo "Done!" 

