(ns jiksnu.abdera.entry-test
  (:use jiksnu.factory
        jiksnu.file
        jiksnu.mock
        jiksnu.model
        jiksnu.abdera.entry
        [karras.entity :only (make)]
        [lazytest.describe :only (describe it testing given do-it)]
        [lazytest.expect :only (expect)])
  (:require [jiksnu.xmpp.session :as session]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Activity
           jiksnu.model.User
           org.apache.abdera.model.Element
           org.apache.abdera.model.Entry
           javax.xml.namespace.QName))

