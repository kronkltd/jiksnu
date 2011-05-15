(ns jiksnu.view-test
  (:use clj-tigase.core
        ciste.core
        ciste.sections
        ciste.sections.default
        ciste.views
        jiksnu.core-test
        jiksnu.view
        [lazytest.describe :only (describe testing do-it)]
        [lazytest.expect :only (expect)]))

(describe link-to ":default")

(describe full-uri ":default")

(describe navigation-section)

(describe devel-environment-section)

(describe page-template-content)

(describe apply-template ":html")

(describe apply-view-by-format ":atom")

(describe apply-view-by-format ":xmpp")

(describe serialize-as ":http")

(describe serialize-as ":xmpp")

(describe get-text)
