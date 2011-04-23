(ns jiksnu.abdera-test
  (:use ciste.core
        ciste.factory
        ciste.sections
        ciste.sections.default
        jiksnu.abdera
        jiksnu.core-test
        jiksnu.model
        jiksnu.session
        jiksnu.namespace
        jiksnu.view
        [lazytest.describe :only (describe testing do-it)]
        [lazytest.expect :only (expect)]))

(describe fetch-resource)

(describe fetch-document)

(describe fetch-feed)

(describe fetch-entries)

(describe parse-stream)

;; (describe parse-xml-string
;;   (do-it "should be an entry"
;;     (let [entry (slurp-classpath "entry.xml")
;;           response (parse-xml-string entry)]
;;       (expect (instance? Entry response)))))

(describe not-namespace)

(describe make-object)

(describe find-children)
