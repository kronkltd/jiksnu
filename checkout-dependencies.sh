#!/bin/sh

rm -rf checkouts
mkdir checkouts
cd checkouts

# Potemkin
git clone git://github.com/duck1123/potemkin.git
cd potemkin
git checkout kronkltd
lein install
cd ..
echo ""
echo ""

# Lamina
git clone git://github.com/duck1123/lamina.git
cd lamina
git checkout kronkltd
lein install
cd ..
echo ""
echo ""

# Gloss
git clone git://github.com/duck1123/gloss.git
cd gloss
git checkout kronkltd
lein install
cd ..
echo ""
echo ""

# Aleph
git clone git://github.com/duck1123/aleph.git
cd aleph
git checkout kronkltd
lein install
cd ..
echo ""
echo ""

# Inflections
git clone git://github.com/duck1123/inflections-clj.git
cd inflections-clj
git checkout kronkltd
lein install
cd ..
echo ""
echo ""

# Hiccup
git clone git://github.com/duck1123/hiccup.git
cd hiccup
git checkout kronkltd
lein install
cd ..
echo ""
echo ""

# Midje
git clone git://github.com/duck1123/Midje.git
cd Midje
git checkout kronkltd
lein install
cd ..
echo ""
echo ""


# Karras
git clone git://github.com/duck1123/karras.git
cd karras
git checkout kronkltd
lein install
cd ..
echo ""
echo ""

# clj-gravatar
git clone git://github.com/duck1123/clj-gravatar.git
cd clj-gravatar
git checkout kronkltd
lein install
cd ..
echo ""
echo ""

# abdera-activitystreams
git clone git://github.com/duck1123/abdera-activitystreams.git
cd abdera-activitystreams
git checkout master
mvn install
cd ..
echo ""
echo ""

# abdera-activitystreams
git clone git://github.com/duck1123/java-salmon.git
cd java-salmon/java-salmon
git checkout master
mvn install
cd ../..
echo ""
echo ""









# clj-tigase
git clone git://github.com/duck1123/clj-tigase.git
cd clj-tigase
git checkout master
mvn install
cd ..
echo ""
echo ""

# clj-factory
git clone git://github.com/duck1123/clj-factory.git
cd clj-factory
git checkout master
mvn install
cd ..
echo ""
echo ""

# ciste
git clone git://github.com/duck1123/ciste.git
cd ciste
git checkout master
mvn install
cd ..
echo ""
echo ""

