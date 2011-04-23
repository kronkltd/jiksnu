(ns jiksnu.view-test
  (:use clj-tigase.core
        ciste.core
        ciste.factory
        ciste.sections
        ciste.sections.default
        ciste.views
        jiksnu.core-test
        jiksnu.file
        jiksnu.model
        jiksnu.session
        jiksnu.namespace
        jiksnu.view
        jiksnu.xmpp.element
        [lazytest.describe :only (describe testing do-it)]
        [lazytest.expect :only (expect)])
  (:require [jiksnu.file :as file]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Activity
           jiksnu.model.User
           org.apache.abdera.model.Entry))

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
