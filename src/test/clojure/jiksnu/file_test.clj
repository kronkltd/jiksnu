(ns jiksnu.file-test
  (:use jiksnu.file
        [lazytest.describe :only (describe do-it testing)]))

(describe classpath-file)

(describe read-xml)

(describe slurp-classpath
  (do-it "should return a string"
    (let [filename "entry.xml"
          response (slurp-classpath filename)]
      (string? response))))

(describe read-clojure)
