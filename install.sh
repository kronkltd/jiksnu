# sudo apt-get update
# sudo apt-get install -y byobu
# sudo apt-get install -y emacs
# sudo apt-get install -y git
# sudo apt-get install -y leiningen
# sudo apt-get install -y rlwrap
# sudo apt-get install -y openjdk-6-jdk 
# sudo apt-get install -y maven2

rm -rf jiksnu

cp -r /vagrant jiksnu
chown -R vagrant:vagrant jiksnu
cd jiksnu
su vagrant
mvn install:install-file -DgroupId=org.clojure -DartifactId=google-closure-library -Dversion=0.0-1589 -Dpackaging=jar -Dfile=google-closure-library-0.0-1589.jar
lein deps
lein run
