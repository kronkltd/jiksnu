(ns jiksnu.atom.view-test
  (:use jiksnu.atom.view
        jiksnu.file
        jiksnu.view
        [lazytest.describe :only (describe it testing given do-it)]
        [lazytest.expect :only (expect)])
  (:import org.apache.abdera.model.Entry))

(describe parse-stream)

(describe parse-xml-string
  (do-it "should be an entry"
    (let [entry (slurp-classpath "entry.xml")
          response (parse-xml-string entry)]
      (expect (instance? Entry response)))))

(describe not-namespace)

