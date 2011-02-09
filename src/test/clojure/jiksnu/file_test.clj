(ns jiksnu.file-test
  (:use jiksnu.file
        [lazytest.describe :only (describe it testing given)]))

(describe classpath-file)

(describe read-xml)

(describe slurp-classpath
  (given [filename "entry.xml"]
    (it "should return a string"
      (let [response (slurp-classpath filename)]
        (string? response)))))

(describe read-clojure)
