#!/bin/bash

# Script extracts all interaction.log files from given directory and appends it to specified folder under logs/
# Parameters:
#   1. MalDroid dataset Capturing_logs dir path
#   2. directory files from MalDroid dataset
#   3. malicious | benign
#   4. output base directory
#
# Example call:
#   extract BANKING malicious

for entry in $1/$2/*.tar.gz
do
    if [ -d "$entry" ]
        then
            if [ -f "$entry/sample_for_analysis.apk.logcat.tar.gz" ]
                then
                    tar -xzf $entry/sample_for_analysis.apk.logcat.tar.gz -C $entry/
                    if [ -s "$entry/sample_for_analysis.apk.logcat" ]
                        then
                            i=$(ls $4/$3/ | wc -l)
                            mv $entry/sample_for_analysis.apk.logcat $4/$3/log$i
                            
                            echo "($i) Extracted log from directory $entry"
                        else
                            echo "Directory ommitted: empty log ($entry)"
                    fi
                else
                    echo "Directory ommitted: no logcat ($entry)"
            fi

        else
            tar -xzf $entry -C $1/$2/ &> /dev/null
            exitCode=$?

            if [ $exitCode -eq 0 ]
                then
                    if [ -f "$1/$2/sample_for_analysis.apk/sample_for_analysis.apk.logcat.tar.gz" ]
                        then
                            tar -xzf $1/$2/sample_for_analysis.apk/sample_for_analysis.apk.logcat.tar.gz -C $1/$2/sample_for_analysis.apk/
                            if [ -s "$1/$2/sample_for_analysis.apk/sample_for_analysis.apk.logcat" ]
                                then
                                    i=$(ls $4/$3/ | wc -l)
                                    mv $1/$2/sample_for_analysis.apk/sample_for_analysis.apk.logcat $4/$3/log$i
                                    
                                    echo "($i) Extracted log from file $entry"
                                else
                                    echo "File ommitted: empty log ($entry)"
                            fi
                        else
                            echo "File ommitted: no logcat file ($entry)"
                    fi

                    rm -rf $1/$2/sample_for_analysis.apk
                else
                    echo "File ommitted: tar error (exit code $exitCode) ($entry)"
            fi
    fi
done
