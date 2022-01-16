docker run -p 5000:5000 -d --rm tyrrr/android_logs:latest
cd "Main module\adb-reader"
java -Dfile.encoding=windows-1252 -m pl.edu.agh.student.adbreader/pl.edu.agh.student.adbreader.HelloApplication